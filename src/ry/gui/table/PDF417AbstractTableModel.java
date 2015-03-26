package ry.gui.table;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author ry
 */
public abstract class PDF417AbstractTableModel extends AbstractTableModel{
    
    public abstract void addRows(Map<Path, List<String>> data);
    
    public abstract void clear();
}
