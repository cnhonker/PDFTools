package ry.gui.table;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import net.miginfocom.swing.MigLayout;

/**
 *
 * @author ry
 */
public final class PDF417TableUI extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(PDF417TableUI.class.getName());
    private final JTable table;
    private final PDF417AbstractTableModel model;
    private final PDF417ProgressTracker tracker;
    private final ButtonBar bBar;
    private Path loc;

    public PDF417TableUI(PDF417AbstractTableModel tableModel) {
        model = tableModel;
        table = new JTable(model);
        bBar = new ButtonBar();
        tracker = new PDF417ProgressTracker();
        tracker.addPropertyChangeListener("ALLDONE", bBar);
        layoutComponent();
    }

    private void layoutComponent() {
        setLayout(new MigLayout("gap 0, hidemode 1"));
        add(new JScrollPane(table), "push, grow, height 70%, wrap");
        add(tracker, "push, grow, height 30%");
        add(bBar, "dock south");
    }

    private final class ButtonBar extends JPanel implements PropertyChangeListener {

        private final JFileChooser fc;
        private final JButton openButton;
        private final JButton decodeButton;
        private final JButton saveButton;
        private final JCheckBox ocrSel;
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
            ocrSel = new JCheckBox("OCR Pass");
            initComponent();
            layoutComponent();
        }

        private void initComponent() {
            fc.setMultiSelectionEnabled(false);
            ((JSpinner.DefaultEditor) threadCounter.getEditor()).getTextField().setEditable(false);
        }

        private void layoutComponent() {
            add(openButton, "sg");
            add(decodeButton, "sg");
            add(saveButton, "sg");
            add(ocrSel);
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
                        enableButtonBar(false);
                        tracker.removeAll();
                        executeWorkers();
                    }
                }
            };
            return a;
        }

        private void enableButtonBar(boolean enable) {
            openButton.setEnabled(enable);
            decodeButton.setEnabled(enable);
            saveButton.setEnabled(enable);
            ocrSel.setEnabled(enable);
            threadCounter.setEnabled(enable);
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

        private void executeWorkers() {
            int size = (int) threadCounter.getModel().getValue();
            size = size == 1 ? size : size - 1;

            PDF417FileCollector dialog = new PDF417FileCollector(loc);
            dialog.showDialog(SwingUtilities.getAncestorOfClass(java.awt.Window.class, this));
            List<Path> pdfFiles = dialog.getResult();
            int splitSize = pdfFiles.size() < size ? pdfFiles.size() : (pdfFiles.size() / size);
            if (!pdfFiles.isEmpty() && splitSize > 0) {
                List<List<Path>> parts = Lists.partition(pdfFiles, splitSize);
                for (List<Path> pList : parts) {
                    PDF417Worker w = new PDF417Worker(new LinkedList<>(pList), model, ocrSel.isSelected());
                    tracker.addProgressBar(w);
                    w.execute();
                }
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            enableButtonBar(true);
        }
    }
}
