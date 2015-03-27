package ry.ocr;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.common.base.Preconditions;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 *
 * @author ry
 */
public final class OCREngine {

    private static final Logger LOG = Logger.getLogger(OCREngine.class.getName());
    private static final Tesseract OCR = Tesseract.getInstance();
    private static OCREngine instance;
    private static String result = null;
    
    public static OCREngine getInstance() {
        if(instance == null) {
            instance = new OCREngine();
            instance.setLanguage("deu");
        }
        return instance;
    }
    
    private OCREngine() {
    }
    
    public void decodeImage(BufferedImage img) {
        decode(img);
    }
    
    private void decode(BufferedImage img) {
        try {
            result = OCR.doOCR(img);
        } catch (TesseractException ex) {
            result = null;
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void setLanguage(String lang) {
        Preconditions.checkArgument(lang != null && !lang.isEmpty());
        OCR.setLanguage(lang);
    }
    
    public String getResult() {
        return result;
    }
}
