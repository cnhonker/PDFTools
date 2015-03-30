package ry.utils;

import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 *
 * @author ry
 */
public class IconUtils {

    public static Icon getIcon(String name) {
        Icon ico = new ImageIcon(IconUtils.class.getResource("/ry/img/"+name));
        return ico;
    }
    
}
