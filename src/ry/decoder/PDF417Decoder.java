package ry.decoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final PDF417Reader READER = new PDF417Reader();
    private static final Map<DecodeHintType, Object> HINTS = new HashMap<>();
    private static final List<PDPage> PAGES = new ArrayList<>();
    private static final Map<Integer, String> RESULT = new HashMap<>();

    static {
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.PDF_417);
        HINTS.put(DecodeHintType.CHARACTER_SET, "ISO-8859-1");
        HINTS.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
    }

    private PDDocument doc;

    private boolean hasError = false;

    private static PDF417Decoder instance;

    public static PDF417Decoder getInstance() {
        if (PDF417Decoder.instance == null) {
            instance = new PDF417Decoder();
        }
        instance.reset();
        return instance;
    }

    private PDF417Decoder() {
    }

    private void reset() {
        PAGES.clear();
        hasError = false;
    }

    public void setDocument(PDDocument document) {
        reset();
        doc = document;
        loadPages();
    }

    private void loadPages() {
        PAGES.clear();
        List list = doc.getDocumentCatalog().getAllPages();
        for (Object o : list) {
            if (o instanceof PDPage) {
                PAGES.add((PDPage) o);
            }
        }
    }

    public Map<Integer, String> getResult() {
        return Collections.unmodifiableMap(RESULT);
    }

    public List<String> getResultAsList() {
        List<String> l = new ArrayList<>();
        for (String s : RESULT.values()) {
            if (s != null && !s.isEmpty()) {
                l.add(s);
            }
        }
        return l;
    }

    public boolean hasError() {
        return hasError;
    }

    public void decodeAll() {
        for (PDPage p : PAGES) {
            decode(p);
        }
    }

    public void decode(int nr) {
        if (nr > 0 && nr < PAGES.size()) {
            decode(PAGES.get(nr));
        }
    }

    private void decode(PDPage page) {
        try {
            BufferedImage img = page.convertToImage(BufferedImage.TYPE_INT_ARGB, 600);
            Result r = READER.decode(createBitmap(img), HINTS);
            RESULT.put(PAGES.indexOf(page), r.getText());
        } catch (IOException | NotFoundException | FormatException | ChecksumException ex) {
            RESULT.put(PAGES.indexOf(page), null);
        }
    }

    public void close() {
        if (doc != null) {
            try {
                doc.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private BinaryBitmap createBitmap(BufferedImage img) {
        LuminanceSource src = new BufferedImageLuminanceSource(img);
        Binarizer bin = new HybridBinarizer(src);
        return new BinaryBitmap(bin);
    }
}
