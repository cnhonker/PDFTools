package ry;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import ry.cmd.api.CMD;

/**
 *
 * @author ry
 */
public class CommandLine extends JPanel {

    private static final Logger LOGGER = Logger.getLogger(CommandLine.class.getName());
    private final JTextField tfield;

    public CommandLine() {
        tfield = new JTextField();
        setLayout(new BorderLayout());
        add(tfield, BorderLayout.CENTER);
        initCommandField();
    }

    private void initCommandField() {
        tfield.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String input = tfield.getText().toUpperCase();
                int paraIndex = input.indexOf(" ");
                String cmd = paraIndex != -1 ? input.substring(0, paraIndex) : input;
                String para = paraIndex != -1 ? input.substring(paraIndex + 1, input.length()) : "";
                Class<CMD> cmdClass = checkClass(cmd);
                if (cmdClass != null) {
                    try {
                        cmdClass.newInstance().execute(para);
                    } catch (InstantiationException | IllegalAccessException ex) {
                        LOGGER.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private Class<CMD> checkClass(String className) {
        String packages[] = {"ry.cmd."};
        for (int j = 0; j < packages.length; j++) {
            try {
                return (Class<CMD>) Class.forName(packages[j] + className);
            } catch (ClassNotFoundException ex) {
                JOptionPane.showMessageDialog(CommandLine.this, "Command not found");
            }
        }
        return null;
    }
}
