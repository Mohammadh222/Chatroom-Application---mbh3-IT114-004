package Project.Client.Views;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
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

    //mbh3
    //04/24/24 
    //

    protected void addUserListItem(long clientId, String clientName) {
        logger.log(Level.INFO, "Adding user to list: " + clientName);
        JPanel content = userListArea;
        logger.log(Level.INFO, "Userlist: " + content.getSize());
        JPanel userItemPanel = new JPanel(new BorderLayout());
       
        JEditorPane textContainer = new JEditorPane("text/plain", clientName);
        textContainer.setName(clientId + "");
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
    //04/24/24 
    //background color, graying out user, 
    
    protected void updateUserListItem(long clientId) {
        logger.log(Level.INFO, "Updating display color for user with ID: " + clientId);
    
        Component[] cs = userListArea.getComponents();
    
        for (Component c : cs) {
            boolean isUser = c.getName().equals(clientId + "");
    
            ((JEditorPane) c).setForeground((isUser ? Color.BLUE : Color.black));
        }
    }

    public void muteUser(long userId) {
        logger.log(Level.INFO, "Muting user list item for id " + userId);
        Component[] components = userListArea.getComponents();
        for (Component component : components) {
            if (component.getName().equals(Long.toString(userId))) {
                component.setForeground(Color.GRAY);
                break; 
            }
        }
        userListArea.revalidate();
        userListArea.repaint();
    }
   
    public void unmuteUser(long userId) {
        logger.log(Level.INFO, "Unmuting user list item for id " + userId);
        Component[] components = userListArea.getComponents();
        for (Component component : components) {
            if (component.getName().equals(Long.toString(userId))) {
                component.setForeground(Color.BLACK);
                break;
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
