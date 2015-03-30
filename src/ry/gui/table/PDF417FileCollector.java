package ry.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import com.google.common.net.MediaType;
import net.miginfocom.swing.MigLayout;
import ry.utils.IconUtils;

/**
 *
 * @author ry
 */
final class PDF417FileCollector extends JPanel implements PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(PDF417FileCollector.class.getName());
    private final JProgressBar progress;
    private final JButton cancel;
    private final JLabel pathLabel;
    private final FileWorker worker;
    private final List<Path> pdfFiles;
    private final JDialog dialog;

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
        dialog = new JDialog();
        setLayout(new MigLayout());
        layoutComponent();
        initDialog();
    }

    private void initDialog() {
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setPreferredSize(new Dimension(400, 200));
        dialog.setLayout(new BorderLayout());
        dialog.setModal(true);
        dialog.getContentPane().add(this, BorderLayout.CENTER);
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
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
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
                dialog.dispose();
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

            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String mime;
                    try {
                        mime = Files.probeContentType(file);
                    } catch (IOException ex) {
                        mime = null;
                    }
                    if (mime != null && mime.equals(MediaType.PDF.toString())) {
                        filePath.add(file);
                    }
                    return super.visitFile(file, attrs);
                }
            });
            final int size = filePath.size();
            double i = 0;
            for (Path p : filePath) {
                i++;
                double d = i / size;
                setProgress((int) (d * 100));
                publish(p.toString());
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
            FileWorker.this.firePropertyChange("DONE", false, true);
        }
    }
}
