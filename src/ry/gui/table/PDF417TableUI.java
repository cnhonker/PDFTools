package ry.gui.table;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import com.google.common.io.Files;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author ry
 */
public class PDF417TableUI extends JPanel implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(PDF417TableUI.class.getName());
    private final JProgressBar progressBar;
    private final JTable table;
    private final PDF417AbstractTableModel model;
    private Path loc;

    public PDF417TableUI(PDF417AbstractTableModel tableModel) {
        model = tableModel;
        table = new JTable(model);
        progressBar = new JProgressBar();
        progressBar.setVisible(false);
        layoutComponent();
    }

    private void layoutComponent() {
        setLayout(new BorderLayout());
        add(progressBar, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(new ButtonBar(), BorderLayout.SOUTH);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("WORKDONE")) {
            progressBar.setIndeterminate(false);
            progressBar.setString(null);
            progressBar.setVisible(false);
        }
    }

    private final class ButtonBar extends JPanel {

        private final JFileChooser fc;
        private final JButton openButton;
        private final JButton decodeButton;
        private final JButton saveButton;
        private final JLabel label;
        private final JSpinner threadCounter;

        public ButtonBar() {
            super(new MigLayout());
            openButton = new JButton(open());
            decodeButton = new JButton(decode());
            saveButton = new JButton(save());
            label = new JLabel("Threads: ");
            threadCounter = getThreadCounter();
            fc = new JFileChooser();

            initComponent();
            layoutComponent();
        }

        private void initComponent() {
            fc.setMultiSelectionEnabled(false);
        }

        private void layoutComponent() {
            add(openButton, "sg");
            add(decodeButton, "sg");
            add(saveButton, "sg");
            add(label);
            add(threadCounter, "sg");
        }

        private JSpinner getThreadCounter() {
            JSpinner spinner = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
            ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
            return spinner;
        }

        private Action open() {
            Action a = new AbstractAction("Open") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
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
                        worker.addPropertyChangeListener(PDF417TableUI.this);
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
                        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                        int i = fc.showSaveDialog(null);
                        if (i == JFileChooser.APPROVE_OPTION) {
                            try (BufferedWriter out = Files.newWriter(fc.getSelectedFile(), StandardCharsets.UTF_8)) {
                                for (int row = 0; row < model.getRowCount(); row++) {
                                    StringBuilder build = new StringBuilder();
                                    for (int col = 0; col < model.getRowCount(); col++) {
                                        build.append(model.getValueAt(row, col));
                                        build.append(";");
                                    }
                                    out.write(build.toString());
                                    out.newLine();
                                }
                            } catch (IOException ex) {
                                LOGGER.log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            };
            return a;
        }
    }
}
