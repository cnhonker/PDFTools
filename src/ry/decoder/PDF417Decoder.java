package ry.decoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.pdf417.PDF417Reader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

/**
 *
 * @author ry
 */
public final class PDF417Decoder {

    private static final Logger LOG = Logger.getLogger(PDF417Decoder.class.getName());
    private final PDF417Reader reader;
    private final Map<DecodeHintType, Object> hints;
    private final List<PDPage> pages;
    private final Map<Integer, String> result;
    private PDDocument doc;

    public PDF417Decoder() {
        doc = null;
        reader = new PDF417Reader();
        hints = new HashMap<>();
        pages = new ArrayList<>();
        result = new HashMap<>();
        initEndcodingHints();
    }

    private void initEndcodingHints() {
        hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.PDF_417);
        hints.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    }

    public void setDocument(PDDocument document) {
        doc = document;
        loadPages();
    }

    private void loadPages() {
        pages.clear();
        List list = doc.getDocumentCatalog().getAllPages();
        for (Object o : list) {
            if (o instanceof PDPage) {
                pages.add((PDPage) o);
            }
        }
    }

    public Map<Integer, String> getResult() {
        return Collections.unmodifiableMap(result);
    }

    public List<String> getResultAsList() {
        List<String> l = new ArrayList<>();
        for (String s : result.values()) {
            if (s != null && !s.isEmpty()) {
                l.add(s);
            }
        }
        return l;
    }

    public void decodePage(int nr) {
        if (nr > 0 && nr < pages.size()) {
            decode(pages.get(nr));
        }
    }

    public void decodeAll() {
        for (PDPage p : pages) {
            decode(p);
        }
    }

    private void decode(PDPage page) {
        try {
            BufferedImage img = page.convertToImage(BufferedImage.TYPE_INT_ARGB, 600);
            Result r = reader.decode(createBitmap(img), hints);
            result.put(pages.indexOf(page), r.getText());
        } catch (IOException | NotFoundException | FormatException | ChecksumException ex) {
            result.put(pages.indexOf(page), null);
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        if (doc != null) {
            try {
                doc.close();
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
    }

    private BinaryBitmap createBitmap(BufferedImage img) {
        LuminanceSource src = new BufferedImageLuminanceSource(img);
        Binarizer bin = new HybridBinarizer(src);
        return new BinaryBitmap(bin);
    }
}
