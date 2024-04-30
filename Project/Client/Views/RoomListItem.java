package Project.Client.Views;

import java.util.function.Consumer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RoomListItem extends JPanel {
    private JTextField roomName;
    private JButton joinButton;

    public RoomListItem(String room, Consumer<String> onJoin) {
      
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        roomName = new JTextField(room);
        roomName.setEditable(false);
        joinButton = new JButton("Join");
        joinButton.addActionListener((event) -> {
            onJoin.accept(roomName.getText());
        });
        this.add(roomName);
        this.add(joinButton);
    }

    public String getRoomName() {
        return roomName.getText();
    }
}
