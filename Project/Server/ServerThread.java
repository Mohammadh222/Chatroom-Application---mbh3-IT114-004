package Project.Server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import Project.Common.Payload;
import Project.Common.PayloadType;
import Project.Common.RoomResultsPayload;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A server-side representation of a single client
 */
public class ServerThread extends Thread {
    private Socket client;
    private String clientName;
    private boolean isRunning = false;
    private ObjectOutputStream out;// exposed here for send()
    // private Server server;// ref to our server so we can call methods on it
    // more easily
    private Room currentRoom;
    private static Logger logger = Logger.getLogger(ServerThread.class.getName());
    private long myId;
    private boolean isMuted = false;
    //private ConcurrentHashMap<String, Boolean> muteList = new ConcurrentHashMap<>();
    private Set<String> muteList = Collections.synchronizedSet(new HashSet<>());



    public void setClientId(long id) {
        myId = id;
    }

    public long getClientId() {
        return myId;
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void info(String message) {
        System.out.println(String.format("Thread[%s]: %s", getId(), message));
    }

    public ServerThread(Socket myClient, Room room) {
        info("Thread created");
        loadMuteListFromFile();
        // get communication channels to single client
        this.client = myClient;
        this.currentRoom = room;

    }

    //mbh3
    //04/24/24 

    protected void setClientName(String name) {
        if (name == null || name.isBlank()) {
            System.err.println("Invalid client name being set");
            return;
        }
        clientName = name;
    }

    public String getClientName() {
        return clientName;
    }

    protected synchronized Room getCurrentRoom() {
        return currentRoom;
    }

    protected synchronized void setCurrentRoom(Room room) {
        if (room != null) {
            currentRoom = room;
        } else {
            info("Passed in room was null, this shouldn't happen");
        }
    }

    public void disconnect() {
        sendConnectionStatus(myId, getClientName(), false);
        info("Thread being disconnected by server");
        isRunning = false;
        cleanup();
    }

    // send methods

    public boolean sendRoomName(String name) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.JOIN_ROOM);
        p.setMessage(name);
        return send(p);
    }

    public boolean sendRoomsList(String[] rooms, String message) {
        RoomResultsPayload payload = new RoomResultsPayload();
        payload.setRooms(rooms);
        if(message != null){
            payload.setMessage(message);
        }
        return send(payload);
    }

    public boolean sendExistingClient(long clientId, String clientName) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.SYNC_CLIENT);
        p.setClientId(clientId);
        p.setClientName(clientName);
        return send(p);
    }

    public boolean sendResetUserList() {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.RESET_USER_LIST);
        return send(p);
    }

    public boolean sendClientId(long id) {
        Payload p = new Payload();
        p.setPayloadType(PayloadType.CLIENT_ID);
        p.setClientId(id);
        return send(p);
    }

    public boolean sendMessage(long clientId, String message) {
        if (!isMuted && !isRecipientMuted(clientId)) {
            // Process text triggers
            message = TextMessage (message);
        
            Payload p = new Payload();
            p.setPayloadType(PayloadType.MESSAGE);
            p.setClientId(clientId);
            p.setMessage(message);
            return send(p);
        }
        return false;
    }
    

    public boolean sendConnectionStatus(long clientId, String who, boolean isConnected) {
        Payload p = new Payload();
        p.setPayloadType(isConnected ? PayloadType.CONNECT : PayloadType.DISCONNECT);
        p.setClientId(clientId);
        p.setClientName(who);
        p.setMessage(isConnected ? "connected" : "disconnected");
        return send(p);
    }

    private boolean send(Payload payload) {
        // added a boolean so we can see if the send was successful
        try {
            // TODO add logger
            logger.log(Level.FINE, "Outgoing payload: " + payload);
            out.writeObject(payload);
            logger.log(Level.INFO, "Sent payload: " + payload);
            return true;
        } catch (IOException e) {
            info("Error sending message to client (most likely disconnected)");
            // comment this out to inspect the stack trace
            // e.printStackTrace();
            cleanup();
            return false;
        } catch (NullPointerException ne) {
            info("Message was attempted to be sent before outbound stream was opened: " + payload);
            return true;// true since it's likely pending being opened
        }
    }

    // end send methods
    @Override
    public void run() {
        info("Thread starting");
        try (ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(client.getInputStream());) {
            this.out = out;
            isRunning = true;
            Payload fromClient;
            while (isRunning && // flag to let us easily control the loop
                    (fromClient = (Payload) in.readObject()) != null // reads an object from inputStream (null would
                                                                     // likely mean a disconnect)
            ) {

                info("Received from client: " + fromClient);
                processPayload(fromClient);

            } // close while loop
        } catch (Exception e) {
            // happens when client disconnects
            e.printStackTrace();
            info("Client disconnected");
        } finally {
            isRunning = false;
            info("Exited thread loop. Cleaning up connection");
            cleanup();
        }
    }

    //mbh3 
    //04/24/24 
    //process rollcommand applied 

    void processPayload(Payload p) {
        switch (p.getPayloadType()) {
            case CONNECT:
                setClientName(p.getClientName());
                saveMuteListToFile();
                break;
            case DISCONNECT:
                Room.disconnectClient(this, getCurrentRoom());
                saveMuteListToFile();
                break;
            case MESSAGE: 

            /// TODO migrate to lobby 
//mbh3
//04/24/24 

                String message = p.getMessage();
        
                if (message.startsWith("/roll")) {
                    processRollCommand(message);

                } else if (message.startsWith("/flip")) {
                    processFlipCommand();
               
                } else if (message.startsWith("@")) {
                    processPrivateMessage(message);
            
                } else if (message.startsWith("mute ")){
                    processMuteCommand(message);
             
                } else if (message.startsWith("unmute ")){
                    processUnmuteCommand(message);
                } else {               
                    if (currentRoom != null) {
                        currentRoom.sendMessage(this, p.getMessage());
                    } else {
                        logger.log(Level.INFO, "Migrating to lobby on message with null room");
                        Room.joinRoom("lobby", this);
                    }
                }
                break;
            case GET_ROOMS:
                Room.getRooms(p.getMessage().trim(), this);
                break;
            case CREATE_ROOM:
                Room.createRoom(p.getMessage().trim(), this);
                break;
            case JOIN_ROOM:
                Room.joinRoom(p.getMessage().trim(), this);
                break;
            default:
                break;

        }


    }

    //mbh3
    //04/24/24 
    /// message coloring italic and bolds 

    private String TextMessage (String message) {

        message = message.replaceAll("\\*(.*?)\\*", "<b>$1</b>"); //makes the text bold 

        message = message.replaceAll("_(.*?)_", "<i>$1</i>");   /// makes the text italics 
        
        message = message.replaceAll("\\+(.*?)\\+", "<u>$1</u>"); // makes the text underline 

        message = message.replaceAll("\\[color:red\\](.*?)\\[/color\\]", "<font color=\"red\">$1</font>"); //red color
        message = message.replaceAll("\\[color:blue\\](.*?)\\[/color\\]", "<font color=\"blue\">$1</font>"); // blue color
        message = message.replaceAll("\\[color:green\\](.*?)\\[/color\\]", "<font color=\"green\">$1</font>"); //green color 

        return message;
    }

    //mbh3 
    //04/24/24 
    //rolling dice game 
    
    private void processRollCommand(String message) { 
        try {
        if (message.contains("d")) {
        
            String[] rollParts = message.substring(6).split("d");
            if (rollParts.length == 2) {
               
                int numDice = Integer.parseInt(rollParts[0]);
                int numSides = Integer.parseInt(rollParts[1]);
                if (numDice > 0 && numSides > 0) {
                    int result = 0;
                    StringBuilder rollResult = new StringBuilder("<b> Outcomes From Rolling Dice :</b> ");
                    for (int i = 0; i < numDice; i++) {
                        int roll = (int) (Math.random() * numSides) + 1;
                        result += roll;
                        rollResult.append(roll);
                        if (i < numDice - 1) {
                            rollResult.append("<b>, </b>");
                        }
                    }
                    rollResult.append("<b>. RESULTS: ").append(result).append("</b>");
                    if (currentRoom != null) {
                        currentRoom.sendMessage(this, rollResult.toString());
                    }
                }
            }
        } else {
            
            int max = Integer.parseInt(message.substring(6).trim());
            if (max > 0) {
                int result = (int) (Math.random() * max) + 1;
                String rollResult = "<b> Outcomes From Rolling Dice:  " + result+" </b>";
                if (currentRoom != null) {
                    currentRoom.sendMessage(this, rollResult);
                }
            }
        }
    } catch (NumberFormatException e) {
        logger.log(Level.WARNING, "Invalid roll command: " + message);
    }
}

//mbh3
///04/24/24 
// flip game head or tails 
//private message method 


    private void processFlipCommand() {
    
        String result = (Math.random() < 0.5) ? "Heads" : "Tails";
        String flipResult = "<b>Coin Toss Outcome: " + result+" </b>";
        if (currentRoom != null) {
            currentRoom.sendMessage(this, flipResult);
        }
    }
    private void processPrivateMessage(String message) {
        int spaceIndex = message.indexOf(" ");
        if (spaceIndex != -1) {
            String receiverName = message.substring(1, spaceIndex);
            String privateMessage = message.substring(spaceIndex + 1);
            
            if (currentRoom != null) {
                ServerThread receiver = currentRoom.findClientByName(receiverName);
                if (receiver != null) {
                    sendMessage(getClientId(), " typing a private text to  "+ receiverName + ": " + privateMessage);
                    receiver.sendMessage(getClientId(),getClientName()+"  text to you : " + privateMessage);
                } else {
                    sendMessage(getClientId(), "User " + receiverName + " cannot be found in the room ");
                }
            }
        }
    }   

    //mbh3
    //04/24/24 
    //mutes target users command 

    private void processMuteCommand(String message) {
        String targetUsername = message.substring(5).trim();
        
        if (currentRoom != null) {
            ServerThread targetClient = currentRoom.findClientByName(targetUsername);
            
            if (targetClient != null) {
                if (muteList.contains(targetUsername)) {
                    sendMessage(getClientId(), targetUsername + " is already muted");
                } else {
                    muteList.add(targetUsername);
                    saveMuteListToFile();
                    targetClient.sendMessage(getClientId(), getClientName() + " muted you");
                    sendMessage(getClientId(), "You muted " + targetUsername);
                }
            } else {
                sendMessage(getClientId(), "User " + targetUsername + " not found in the room");
            }
        } else {
            sendMessage(getClientId(), "Currently not in a room");
        }
    }

    

    //mbh3
    //04/14/14 
    //unmute command target users 
        
    private void processUnmuteCommand(String message) {
        String targetUsername = message.substring(7).trim();
        
        if (currentRoom != null) {
            ServerThread targetClient = currentRoom.findClientByName(targetUsername);
            
            if (targetClient != null) {
                if (!muteList.contains(targetUsername)) {
                    sendMessage(getClientId(), targetUsername + " is not muted");
                } else {
                    muteList.remove(targetUsername);
                    saveMuteListToFile();
                    targetClient.sendMessage(getClientId(), getClientName() + " unmuted you");
                    sendMessage(getClientId(), "You unmuted " + targetUsername);
                }
            } else {
                sendMessage(getClientId(), "User " + targetUsername + " not found in the room");
            }
        } else {
            sendMessage(getClientId(), "Currently not in a room");
        }
    }
    
    public boolean isMuted() {
        return isMuted;
    }
    public void setMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }


    //mbh3
    //04/24/24
    // list of muted Users and loads the uers as well 

    private void saveMuteListToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(clientName + "_muteList.csv"))) {
            for (String mutedClient : muteList) {
                writer.write(mutedClient + ",");
                writer.newLine();
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error writing to mute list file", e);
        }
    }

    private void loadMuteListFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(clientName + "_muteList.csv"))) {
            String[] mutedClients = reader.readLine().split(",");
            Collections.addAll(muteList, mutedClients);
        } catch (IOException e) {
        }
    }

    public boolean isRecipientMuted(long clientId) {
        ServerThread targetClient = currentRoom.findClientById(clientId);
        if (targetClient != null) {
            String targetUsername = targetClient.getClientName();
            return muteList.contains(targetUsername);
        }
        return false;
    }

    public boolean hasUserMuted(String username) {
        return muteList.contains(username);
    }

    
    private void cleanup() {
        info("Thread cleanup() start");
        try {
            client.close();
        } catch (IOException e) {
            info("Client already closed");
        }
        info("Thread cleanup() complete");
    }

    public void setCurrentRoom(Project.Common.Room room) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setCurrentRoom'");
    }
}
