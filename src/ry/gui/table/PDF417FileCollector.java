package ry.gui.table;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.io.FileUtils;
import ry.utils.IconUtils;

/**
 *
 * @author ry
 */
final class PDF417FileCollector extends JDialog implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(PDF417FileCollector.class.getName());
    private final JProgressBar progress;
    private final JButton cancel;
    private final JLabel pathLabel;
    private final FileWorker worker;
    private final List<Path> pdfFiles;

    PDF417FileCollector(Path start) {
        worker = new FileWorker(start);
        worker.addPropertyChangeListener(this);
        pathLabel = new JLabel("searching for pdf files ...");
        progress = new JProgressBar();
        progress.setStringPainted(true);
        progress.setValue(0);
        pdfFiles = new ArrayList<>();
        cancel = new JButton(cancelA());
        cancel.setBorderPainted(false);
        cancel.setContentAreaFilled(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setPreferredSize(new Dimension(400, 200));
        setLayout(new MigLayout());
        setModal(true);
        layoutComponent();
    }

    private Action cancelA() {
        Action a = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                worker.cancel(true);
                pdfFiles.clear();
            }
        };
        a.putValue(Action.LARGE_ICON_KEY, IconUtils.getIcon("cancel.png"));
        return a;
    }

    private void layoutComponent() {
        add(pathLabel, "pushx, growx, wrap");
        add(progress, "pushx, growx");
    }

    void showDialog(Component parent) {
        worker.execute();
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    List<Path> getResult() {
        return pdfFiles;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        switch (propName) {
            case "progress":
                int perc = (int) evt.getNewValue();
                progress.setString(perc + " %");
                progress.setValue(perc);
                break;
            case "DONE":
                progress.setValue(100);
                try {
                    pdfFiles.addAll(worker.get());
                } catch (InterruptedException | ExecutionException ex) {
                    pdfFiles.clear();
                    LOGGER.log(Level.SEVERE, null, ex);
                }
                dispose();
                break;
            default:
        }
    }

    private class FileWorker extends SwingWorker<List<Path>, String> {

        private final List<Path> filePath;
        private final Path root;

        public FileWorker(Path start) {
            filePath = new ArrayList<>();
            root = start;
        }

        @Override
        protected List<Path> doInBackground() throws Exception {
            Collection<File> files = FileUtils.listFiles(root.toFile(), new String[]{"pdf"}, true);
            final int size = files.size();
            double i = 0;
            for (File f : files) {
                i++;
                double d = i / size;
                setProgress((int) (d * 100));
                filePath.add(f.toPath());
                publish(f.getCanonicalPath());
            }
            return filePath;
        }

        @Override
        protected void process(List<String> chunks) {
            for (String c : chunks) {
                pathLabel.setText(">> " + c);
            }
        }

        @Override
        protected void done() {
            firePropertyChange("DONE", false, true);
        }
    }
}
