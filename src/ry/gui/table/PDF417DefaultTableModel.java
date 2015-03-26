package ry.gui.table;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.common.collect.Lists;

/**
 *
 * @author ry
 */
public class PDF417DefaultTableModel extends PDF417AbstractTableModel {

    private static final List<String> COLUMNS = Lists.newArrayList("Path", "PDF417");
    private final List<Map<Path, List<String>>> tableData;

    public PDF417DefaultTableModel() {
        tableData = new ArrayList<>();
    }
    
    @Override
    public String getColumnName(int column) {
        return column != -1 && column < COLUMNS.size() ? COLUMNS.get(column) : "";
    }

    @Override
    public int getRowCount() {
        return tableData.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.size();
    }
    
    private String transform(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for(String s : list) {
            builder.append(s);
        }
        return builder.toString();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if(rowIndex != -1 && rowIndex < tableData.size()) {
            switch(getColumnName(columnIndex)) {
                case "Path":
                    return tableData.get(rowIndex).keySet().iterator().next().toString();
                case "PDF417":
                    return transform(tableData.get(rowIndex).values().iterator().next());
                default:
                    return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public void addRows(Map<Path, List<String>> data) {
        tableData.add(data);
        fireTableRowsInserted(tableData.size(), tableData.size());
    }

    @Override
    public void clear() {
        tableData.clear();
        fireTableDataChanged();
    }
}
