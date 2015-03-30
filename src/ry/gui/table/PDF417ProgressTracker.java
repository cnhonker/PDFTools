package ry.gui.table;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import net.miginfocom.swing.MigLayout;
import ry.utils.IconUtils;

/**
 *
 * @author ry
 */
final class PDF417ProgressTracker extends JPanel {

    PDF417ProgressTracker() {
        super(new MigLayout());
        setBorder(BorderFactory.createMatteBorder(1, 0, 1, 0, Color.BLACK));
    }

    void addProgressBar(PDF417Worker worker) {
        add(new ProgressPanel(worker), "pushx, growx, wrap");
        revalidate();
        repaint();
    }
    
    private void removeProgressBar(ProgressPanel p) {
        remove(p);
        revalidate();
        repaint();
    }
    
    private class ProgressPanel extends JPanel implements PropertyChangeListener{
        
        private final JButton cancelWorker;
        private final JProgressBar progress;
        private final PDF417Worker worker;

        private ProgressPanel(PDF417Worker aWorker) {
            worker = aWorker;
            worker.addPropertyChangeListener(ProgressPanel.this);
            progress = new JProgressBar();
            progress.setValue(0);
            progress.setStringPainted(true);
            cancelWorker = new JButton(cancelAction());
            cancelWorker.setBorderPainted(false);
            cancelWorker.setContentAreaFilled(false);
            setLayout(new MigLayout());
            add(progress, "push, grow");
            add(cancelWorker);
        }
        
        private Action cancelAction() {
            Action a = new AbstractAction() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    worker.cancel(true);
                    removeProgressBar(ProgressPanel.this);
                }
            };
            a.putValue(Action.LARGE_ICON_KEY, IconUtils.getIcon("cancel.png"));
            return a;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if ("progress".equals(evt.getPropertyName())) {
                 int perc = (Integer)evt.getNewValue();
                 progress.setString(perc + " %");
                 progress.setValue(perc);
             }
        }
    }
}
