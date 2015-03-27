package ry;

import ry.gui.ButtonTabComponent;
import java.awt.BorderLayout;
import java.awt.Component;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

/**
 *
 * @author ry
 */
final class MainPanel extends JPanel {
    
    private static MainPanel instance;
    
    private final JTabbedPane tabpane;
    
    static MainPanel getInstance() {
        if(instance == null) {
            instance = new MainPanel();
        }
        return instance;
    }
    
    
    private MainPanel() {
        tabpane = new JTabbedPane();
        layoutComponent();
    }
    
    private void layoutComponent() {
        setLayout(new BorderLayout());
        add(tabpane, BorderLayout.CENTER);
    }

    void addTab(String title, Component c) {
        tabpane.addTab(title, c);
        tabpane.setTabComponentAt(tabpane.getTabCount()-1, new ButtonTabComponent(tabpane));
    }
}
