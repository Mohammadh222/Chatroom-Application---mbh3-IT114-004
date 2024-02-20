package Part3HW;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;


public class Server {
    int port = 3001;
    // connected clients
    private List<ServerThread> clients = new ArrayList<ServerThread>();

    private boolean gameActive = false;
    private int hiddenNumber;

    private void start(int port) {
        this.port = port;
        // server listening
        try (ServerSocket serverSocket = new ServerSocket(port);) {
            Socket incoming_client = null;
            System.out.println("Server is listening on port " + port);
            do {
                System.out.println("waiting for next client");
                if (incoming_client != null) {
                    System.out.println("Client connected");
                    ServerThread sClient = new ServerThread(incoming_client, this);
                    
                    clients.add(sClient);
                    sClient.start();
                    incoming_client = null;
                    
                }
            } while ((incoming_client = serverSocket.accept()) != null);
        } catch (IOException e) {
            System.err.println("Error accepting connection");
            e.printStackTrace();
        } finally {
            System.out.println("closing server socket");
        }
    }
    protected synchronized void disconnect(ServerThread client) {
		long id = client.getId();
        client.disconnect();
		broadcast("Disconnected", id);
	}
    
    protected synchronized void broadcast(String message, long id) {
        if(processCommand(message, id)){

            return;
        }
        // let's temporarily use the thread id as the client identifier to
        // show in all client's chat. This isn't good practice since it's subject to
        // change as clients connect/disconnect
        message = String.format("User[%d]: %s", id, message);
        // end temp identifier
        
        // loop over clients and send out the message
        Iterator<ServerThread> it = clients.iterator();
        while (it.hasNext()) {
            ServerThread client = it.next();
            boolean wasSuccessful = client.send(message);
            if (!wasSuccessful) {
                System.out.println(String.format("Removing disconnected client[%s] from list", client.getId()));
                it.remove();
                broadcast("Disconnected", id);
            }
        }
    }

    private boolean processCommand(String message, long clientId){
        System.out.println("Checking command: " + message);
        if(message.equalsIgnoreCase("disconnect")){
            Iterator<ServerThread> it = clients.iterator();
            while (it.hasNext()) {
                ServerThread client = it.next();
                if(client.getId() == clientId){
                    it.remove();
                    disconnect(client);
                    
                    break;
                }
            }
            return true;
        }
    
        //Mohammad Hussain
        //mbh3 
        // 2/20/2024 

    //. Coin toss  - random heads or tails 

        if (message.equals("flip") || message.equals("toss") || message.equals("coin")){
            String userName = getClientName(clientId); // gets the users name(used 'Bob' in this example)
            String result;
            Random random = new Random(); // Randomly genarates true or false
            if (random.nextBoolean()){
                result = "heads";
            } else {
                result = "tails";
            }
            String response = String.format( userName +" flipped a coin and got "+result + " "); // displays the messages 
            broadcast(response, clientId);
            return true; 

        }

	    // Mohammad Hussain
        // mbh3
        // 02/20/2024

    //.   number guess 

        if (message.equalsIgnoreCase("start")) { // starts the game 
            if (!gameActive) { // if the game not active 
                gameActive = true; // activates the game
                hiddenNumber = generateRandomNumber(); // calling the random number genarator function 
                broadcast("Game started! Guess the number.", 0); // displays this message 
            }
            return true;
        } else if (message.equalsIgnoreCase("stop")) { // ends the game
            if (gameActive) { // if the game is active
                gameActive = false; // kills the game
                broadcast("Game stopped. Guesses will be treated as regular messages.", 0);
            } // displays this message 
            return true;
        } else if (message.toLowerCase().startsWith("guess ")) {
            if (gameActive) {
                String guessCommand = message.substring(6); // Remove "guess " from the message
                try {
                    int guess = Integer.parseInt(guessCommand);// takes the number entered with guess
                    String result = checkGuess(clientId, guess); // calls the checkguess funtion 
                    broadcast(result, clientId); // gives an output based on if the guess is correct or not 
                } catch (NumberFormatException e) {
                    broadcast("Invalid guess format. Please use 'guess <number>'.", clientId);
                }
            } else {
                broadcast("Game is not active. Guesses will not be considered.", clientId);
            }
            return true;
        }

        return false;
    }
    private String checkGuess(long clientId, int guess) {
        if (guess == hiddenNumber) {
            String userName = getClientName(clientId); // gets the users name(used 'Bob' in this example)
            gameActive = false;
            return String.format(userName +" guessed "+ guess+" and it is correct!"); // when the guess is correct
        } else {
            String userName = getClientName(clientId);
            return String.format(userName +" guessed "+ guess+" and it is not correct!"); // when the guess is not correct
        }
    }

    private int generateRandomNumber() {
        Random random = new Random();
        return random.nextInt(10) + 1; // Generates a random number between 1 and 10
    }
    
    private String getClientName(long clientId) {return "bob";}
    public static void main(String[] args) {
        System.out.println("Starting Server");
        Server server = new Server();
        int port = 3000;
        try {
            port = Integer.parseInt(args[0]);
        } catch (Exception e) {
            // can ignore, will either be index out of bounds or type mismatch
            // will default to the defined value prior to the try/catch
        }
        server.start(port);
        System.out.println("Server Stopped");
    }
}