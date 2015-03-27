package ry.cmd;

import ry.MainFrame;
import ry.cmd.api.CMD;
import ry.gui.table.PDF417DefaultTableModel;
import ry.gui.table.PDF417TableUI;

/**
 *
 * @author ry
 */
public class DECODE implements CMD {

    @Override
    public void execute(String para) {
        switch (para) {
            case ("PDF417"):
                MainFrame.addTab("PDF417", new PDF417TableUI(new PDF417DefaultTableModel()));
                break;
            default:
        }
    }
}
