package ry.ocr;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.common.base.Preconditions;
import net.sourceforge.tess4j.Tesseract1;
import net.sourceforge.tess4j.TesseractException;

/**
 *
 * @author ry
 */
public final class OCREngine {

    private static final Logger LOG = Logger.getLogger(OCREngine.class.getName());
    private final Tesseract1 engine;
    private String result;
    
    public OCREngine() {
        engine = new Tesseract1();
        engine.setLanguage("deu");
        result = null;
    }
    
    public void decodeImage(BufferedImage img) {
        if(img == null) {
            return;
        }
        try {
            result = engine.doOCR(img);
        } catch (TesseractException ex) {
            result = null;
            LOG.log(Level.SEVERE, null, ex);
        }
    }
    
    public void setLanguage(String lang) {
        Preconditions.checkArgument(lang != null && !lang.isEmpty());
        engine.setLanguage(lang);
    }
    
    public String getResult() {
        return result;
    }
}
