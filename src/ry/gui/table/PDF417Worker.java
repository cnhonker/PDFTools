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
public final class PDF417Worker extends SwingWorker<Map<Path, List<String>>, Map<Path, List<String>>> {

    private final PDF417Decoder decoder;
    private final PDF417AbstractTableModel model;
    private final Map<Path, List<String>> result;
    private final Path[] pool;
    

    public PDF417Worker(Path[] files, PDF417AbstractTableModel tableModel) {
        pool = files;
        model = tableModel;
        decoder = new PDF417Decoder();
        result = new HashMap<>();
    }

    @Override
    protected Map<Path, List<String>> doInBackground() throws Exception {
        double cnt = 0;
        for(Path p : pool) {
            walkFileTree(p);
            cnt++;
            double percent = cnt / pool.length;
            setProgress((int)(percent * 100));
        }
        return result;
    }

    private void walkFileTree(Path p) throws IOException {
        Files.walkFileTree(p, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (Files.probeContentType(file).equals(MediaType.PDF.toString())) {
                    publish(decodeFile(file));
                }
                return super.visitFile(file, attrs);
            }
        });
    }

    private Map<Path, List<String>> decodeFile(Path p) throws IOException {
        decoder.setDocument(PDDocument.load(p.toFile()));
        decoder.decodeAll();
        decoder.close();
        Map<Path, List<String>> chunk = ImmutableMap.of(p, decoder.getResultAsList());
        result.putAll(chunk);
        return chunk;
    }

    @Override
    protected void process(List<Map<Path, List<String>>> chunks) {
        for (Map<Path, List<String>> chunk : chunks) {
            model.addRows(chunk);
        }
    }

    @Override
    protected void done() {
        firePropertyChange("WORKDONE", false, true);
    }
}
