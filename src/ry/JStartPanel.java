package ry;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import com.google.common.io.Files;
import net.miginfocom.swing.MigLayout;
import ry.gui.table.PDF417AbstractTableModel;
import ry.gui.table.PDF417DefaultTableModel;
import ry.gui.table.PDF417Worker;

/**
 *
 * @author ry
 */
public class JStartPanel extends JPanel implements PropertyChangeListener {

    private final JProgressBar progressBar;
    private final PDF417AbstractTableModel model;
    private final JFileChooser fc;
    private Path loc;

    public JStartPanel() {
        model = new PDF417DefaultTableModel();
        fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        setLayout(new BorderLayout());
        add(progressBar, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(model)), BorderLayout.CENTER);
        add(createButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel createButtonBar() {
        JPanel panel = new JPanel(new MigLayout());
        panel.add(new JButton(chooser()));
        panel.add(new JButton(decode()));
        panel.add(new JButton(save()));
        return panel;
    }

    private Action chooser() {
        Action a = new AbstractAction("Open") {

            @Override
            public void actionPerformed(ActionEvent e) {
                int i = fc.showOpenDialog(null);
                if (i == JFileChooser.APPROVE_OPTION) {
                    loc = fc.getSelectedFile().toPath();
                    model.clear();
                }
            }
        };
        return a;
    }

    private Action decode() {
        Action a = new AbstractAction("Decode") {

            @Override
            public void actionPerformed(ActionEvent e) {
                model.clear();
                if (loc != null) {
                    PDF417Worker worker = new PDF417Worker(loc, model);
                    worker.addPropertyChangeListener(JStartPanel.this);
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                    worker.execute();
                }
            }
        };
        return a;
    }

    private Action save() {
        Action a = new AbstractAction("Save") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (model.getRowCount() > 0) {
                    JFileChooser fc = new JFileChooser();
                    fc.setMultiSelectionEnabled(false);
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    int i = fc.showSaveDialog(null);
                    if (i == JFileChooser.APPROVE_OPTION) {
                        try (BufferedWriter out = Files.newWriter(fc.getSelectedFile(), StandardCharsets.UTF_8)) {
                            for(int row = 0; row < model.getRowCount(); row++) {
                                StringBuilder build = new StringBuilder();
                                for(int col = 0; col < model.getRowCount(); col++) {
                                    build.append(model.getValueAt(row, col));
                                    build.append(";");
                                }
                                out.write(build.toString());
                                out.newLine();
                            }
                        } catch(IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

        };
        return a;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("WORKDONE")) {
            progressBar.setIndeterminate(false);
            progressBar.setString(null);
            progressBar.setVisible(false);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                createAndShowGui();
            }
        });
    }

    private static void createAndShowGui() {
        JFrame frame = new JFrame("PDF417 Decoder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.setPreferredSize(new Dimension(800, 600));
        frame.add(new JStartPanel(), BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

}
