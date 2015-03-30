package ry.gui.table;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import net.miginfocom.swing.MigLayout;
import ry.utils.IconUtils;

/**
 *
 * @author ry
 */
final class PDF417ProgressTracker extends JPanel {

    private final List<PDF417Worker> workers;

    PDF417ProgressTracker() {
        super(new MigLayout());
        setBorder(BorderFactory.createTitledBorder("Working Threads"));
        workers = new ArrayList<>();
    }

    void addProgressBar(PDF417Worker worker) {
        workers.add(worker);
        add(new ProgressPanel(worker), "pushx, growx, wrap");
        revalidate();
        repaint();
    }

    private void removeProgressBar(ProgressPanel p) {
        remove(p);
        revalidate();
        repaint();
    }

    private class ProgressPanel extends JPanel implements PropertyChangeListener {

        private final JButton cancel;
        private final JProgressBar progress;
        private final PDF417Worker worker;

        private ProgressPanel(PDF417Worker aWorker) {
            worker = aWorker;
            worker.addPropertyChangeListener(ProgressPanel.this);
            progress = new JProgressBar();
            progress.setValue(0);
            progress.setStringPainted(true);
            cancel = new JButton(cancelAction());
            cancel.setBorderPainted(false);
            cancel.setContentAreaFilled(false);
            setLayout(new MigLayout());
            add(progress, "push, grow");
            add(cancel);
        }

        private Action cancelAction() {
            Action a = new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    worker.cancel(true);
                    workers.remove(worker);
                    Queue<Path> rest = worker.getRemainingElements();
                    if (!workers.isEmpty()) {
                        workers.get(0).pushToQueue(rest);
                    } else {
                        InfoDialog info = new InfoDialog();
                        info.addInfo(rest);
                        info.showDialog(SwingUtilities.getAncestorOfClass(Window.class, PDF417ProgressTracker.this));
                    }
                    removeProgressBar(ProgressPanel.this);
                }
            };
            a.putValue(Action.LARGE_ICON_KEY, IconUtils.getIcon("cancel.png"));
            return a;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            switch (propName) {
                case "progress":
                    int perc = (Integer) evt.getNewValue();
                    progress.setString(perc + " %");
                    progress.setValue(perc);
                    break;
                case "WORKDONE":
                    progress.setString("100 %");
                    progress.setValue(100);
                    if(!inProgress()) {
                        PDF417ProgressTracker.this.firePropertyChange("ALLDONE", false, true);
                    }
                    break;
                default:
            }
        }
        
        private boolean inProgress() {
            for(PDF417Worker w : workers) {
                if(!w.isDone()) {
                    return true;
                }
            }
            return false;
        }
    }

    private class InfoDialog extends JPanel {

        private final JTextArea info;
        private final JDialog dialog;

        private InfoDialog() {
            dialog = new JDialog();
            dialog.setTitle("unprocessed files ...");
            info = new JTextArea();
            info.setEditable(false);
            setLayout(new BorderLayout());
            add(new JScrollPane(info), BorderLayout.CENTER);
            initDialog();
        }

        private void initDialog() {
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setPreferredSize(new Dimension(400, 600));
            dialog.getContentPane().setLayout(new BorderLayout());
            dialog.getContentPane().add(InfoDialog.this, BorderLayout.CENTER);
            dialog.setModal(true);
            dialog.pack();
        }

        private void addInfo(Queue<Path> paths) {
            while (!paths.isEmpty()) {
                Path p = paths.remove();
                info.append(p.toString() + "\n");
            }
        }

        private void showDialog(Component c) {
            dialog.setLocationRelativeTo(c);
            dialog.setVisible(true);
        }

    }
}
