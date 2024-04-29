package Project.Client.Views;


import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import Project.Client.CardView;
import Project.Client.ICardControls;

//mbh3
//04/24/24

public class Menu extends JMenuBar {
    public Menu(ICardControls controls) {
        JMenu roomsMenu = new JMenu("Rooms");
        JMenuItem roomsSearch = new JMenuItem("Search");
        roomsSearch.addActionListener((event) -> {
            controls.show(CardView.ROOMS.name());
        });
       
        JMenuItem exportMenuItem = new JMenuItem("Export Chat History");
        exportMenuItem.addActionListener(e -> controls.exportChatHistory()); 
        
        roomsMenu.add(roomsSearch);
        roomsMenu.add(exportMenuItem);
        this.add(roomsMenu);
        roomsMenu.add(roomsSearch);
        this.add(roomsMenu);
    }
}
