package ry;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import javax.swing.JFrame;
import ry.gui.table.PDF417DefaultTableModel;
import ry.gui.table.PDF417TableUI;

/**
 *
 * @author ry
 */
public class MainFrame extends JFrame {
    
    private static final MainPanel tabbedPane = MainPanel.getInstance();

    public MainFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));
        add(tabbedPane, BorderLayout.CENTER);
        add(new CommandLine(), BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        addTab("PDF417", new PDF417TableUI(new PDF417DefaultTableModel()));
    }
    
    public static void addTab(String title, Component c) {
        tabbedPane.addTab(title, c);
    }
    
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
}
