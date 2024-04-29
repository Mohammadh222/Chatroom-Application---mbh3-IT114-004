package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.event.ContainerEvent;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;

import Project.Client.ClientUtils;
import Project.Client.ICardControls;

public class UserListPanel extends JPanel {
    JPanel userListArea;
    private static Logger logger = Logger.getLogger(UserListPanel.class.getName());

    public UserListPanel(ICardControls controls) {
        super(new BorderLayout(10, 10));
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setAlignmentY(Component.BOTTOM_ALIGNMENT);

        // wraps a viewport to provide scroll capabilities
        JScrollPane scroll = new JScrollPane(content);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        // scroll.setBorder(BorderFactory.createEmptyBorder());
        // no need to add content specifically because scroll wraps it

        userListArea = content;

        wrapper.add(scroll);
        this.add(wrapper, BorderLayout.CENTER);

        userListArea.addContainerListener(new ContainerListener() {

            @Override
            public void componentAdded(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

            @Override
            public void componentRemoved(ContainerEvent e) {
                if (userListArea.isVisible()) {
                    userListArea.revalidate();
                    userListArea.repaint();
                }
            }

        });
    }

    protected void addUserListItem(long clientId, String clientName) {
        logger.log(Level.INFO, "Adding user to list: " + clientName);
        JPanel content = userListArea;
        logger.log(Level.INFO, "Userlist: " + content.getSize());
        JPanel userItemPanel = new JPanel(new BorderLayout());
        // msh52
        //11/23/2023
        JEditorPane textContainer = new JEditorPane("text/plain", clientName);
        textContainer.setName(clientId + "");
        // sizes the panel to attempt to take up the width of the container
        // and expand in height based on word wrapping
        textContainer.setLayout(null);
        textContainer.setPreferredSize(
        new Dimension(content.getWidth(), ClientUtils.calcHeightForText(this, clientName, content.getWidth())));
        textContainer.setMaximumSize(textContainer.getPreferredSize());
        textContainer.setEditable(false);
        // remove background and border (comment these out to see what it looks like
        // otherwise)
        ClientUtils.clearBackground(textContainer);
        // Add the user item panel to the user list area
        // add to container
        content.add(textContainer);
    }
 
    //mbh3
    //04/23/24
    protected void updateUserListItem(long clientId) {
        // Log information about the user list item being updated
        logger.log(Level.INFO, "Updating display color for user with ID: " + clientId);
    
        // Retrieve all components (user list items) from the user list area
        Component[] cs = userListArea.getComponents();
    
        // Iterate over each component in the user list
        for (Component c : cs) {
            // Check if the component's name matches the client ID
            boolean isUser = c.getName().equals(clientId + "");
    
            // If the component is the user being updated, set its foreground color to red
            // Otherwise, set the color to black
            ((JEditorPane) c).setForeground((isUser ? Color.RED : Color.black));
        }
    }

    // mbh3
    // 04/23/24
    // This method updates the user list item to indicate that the user is muted
    public void muteUser(long userId) {
        logger.log(Level.INFO, "Muting user list item for id " + userId);
        Component[] components = userListArea.getComponents();
        for (Component component : components) {
            if (component.getName().equals(Long.toString(userId))) {
                // Change the text color to gray to indicate the user is muted
                component.setForeground(Color.GRAY);
                break; // No need to continue the loop once we've found the user
            }
        }
        userListArea.revalidate();
        userListArea.repaint();
    }

    // mbh3
// 04/23/24
    // This method updates the user list item to indicate that the user is unmuted
    public void unmuteUser(long userId) {
        logger.log(Level.INFO, "Unmuting user list item for id " + userId);
        Component[] components = userListArea.getComponents();
        for (Component component : components) {
            if (component.getName().equals(Long.toString(userId))) {
                // Change the text color back to the default color (e.g., black) to indicate the user is unmuted
                component.setForeground(Color.BLACK);
                break; // No need to continue the loop once we've found the user
            }
        }
        userListArea.revalidate();
        userListArea.repaint();
    }

    
    
    protected void removeUserListItem(long clientId) {
        logger.log(Level.INFO, "removing user list item for id " + clientId);
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            if (c.getName().equals(clientId + "")) {
                userListArea.remove(c);
                break;
            }
        }
    }
    
    protected void clearUserList() {
        Component[] cs = userListArea.getComponents();
        for (Component c : cs) {
            userListArea.remove(c);
        }
    }
}