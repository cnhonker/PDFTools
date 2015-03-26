package ry.gui.table;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingWorker;
import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import org.apache.pdfbox.pdmodel.PDDocument;
import ry.decoder.PDF417Decoder;

/**
 *
 * @author ry
 */
public class PDF417Worker extends SwingWorker<Map<Path, List<String>>, Map<Path, List<String>>> {
    
    private static final PDF417Decoder DECODER = PDF417Decoder.getInstance();
    private static final Map<Path, List<String>> RESULT = new HashMap<>();
    private final Path pool;
    private final PDF417AbstractTableModel model;
    
    public PDF417Worker(Path folder, PDF417AbstractTableModel tableModel) {
        pool = folder;
        model = tableModel;
    }
    
    @Override
    protected Map<Path, List<String>> doInBackground() throws Exception {
        Files.walkFileTree(pool, new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.probeContentType(file).equals(MediaType.PDF.toString())) {
                    PDDocument pdf = PDDocument.load(file.toFile());
                    DECODER.setDocument(pdf);
                    DECODER.decodeAll();
                    DECODER.close();
                    Map<Path, List<String>> chunk = ImmutableMap.of(file, DECODER.getResultAsList());
                    RESULT.putAll(chunk);
                    publish(chunk);
                }
                return super.visitFile(file, attrs);
            }
        });
        return RESULT;
    }
    
    @Override
    protected void process(List<Map<Path, List<String>>> chunks) {
        for(Map<Path, List<String>> chunk : chunks) {
            model.addRows(chunk);
        }
    }
    
    @Override
    protected void done() {
        firePropertyChange("WORKDONE", false, true);
    }
}
