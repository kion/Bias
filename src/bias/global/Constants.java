/**
 * Created on Oct 19, 2006
 */
package bias.global;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.ImageIcon;

/**
 * @author kion
 */
public class Constants {

    public static final String PATH_SEPARATOR = System.getProperty("file.separator");

    public static final String EMPTY_STR = "";
    public static final String NULL_STR = "NULL";
    public static final String EMPTY_STR_PATTERN = "\\s*";
    
    public static final String DATA_FILE_PATTERN = "data" + PATH_SEPARATOR + "\\d+\\.data";
    public static final String DATA_FILE_ENDING_PATTERN = "\\.data";
    public static final String DATA_FILE_ENDING = ".data";
    public static final String CONFIG_FILE_PATH = "res" + PATH_SEPARATOR + "config.properties";
    public static final String DATA_DIR_PATTERN = "data" + PATH_SEPARATOR;
    public static final String DATA_DIR = "data" + PATH_SEPARATOR;

    public static final String PROPERTY_WINDOW_COORDINATE_X = "WINDOW_COORDINATE_X";
    public static final String PROPERTY_WINDOW_COORDINATE_Y = "WINDOW_COORDINATE_Y";
    public static final String PROPERTY_WINDOW_WIDTH = "WINDOW_WIDTH";
    public static final String PROPERTY_WINDOW_HEIGHT = "WINDOW_HEIGHT";
    public static final String PROPERTY_LAST_SELECTED_TAB_INDEX = "LAST_SELECTED_TAB_INDEX";

    public static final Integer FONT_SIZE_XX_SMALL = new Integer(8);
    public static final Integer FONT_SIZE_X_SMALL = new Integer(10);
    public static final Integer FONT_SIZE_SMALL = new Integer(12);
    public static final Integer FONT_SIZE_MEDIUM = new Integer(14);
    public static final Integer FONT_SIZE_LARGE = new Integer(18);
    public static final Integer FONT_SIZE_X_LARGE = new Integer(24);
    public static final Integer FONT_SIZE_XX_LARGE = new Integer(36);
    
    public static final Map<String, Integer> FONT_SIZES = fontSizes();

    private static final Map<String, Integer> fontSizes() {
        Map<String, Integer> fontSizes = new LinkedHashMap<String, Integer>();
        fontSizes.put("xx-small", FONT_SIZE_XX_SMALL);
        fontSizes.put("x-small", FONT_SIZE_X_SMALL);
        fontSizes.put("small", FONT_SIZE_SMALL);
        fontSizes.put("medium", FONT_SIZE_MEDIUM);
        fontSizes.put("large", FONT_SIZE_LARGE);
        fontSizes.put("x-large", FONT_SIZE_X_LARGE);
        fontSizes.put("xx-large", FONT_SIZE_XX_LARGE);
        return fontSizes;
    }

    public static final ImageIcon ICON_ADD = 
        new ImageIcon(Constants.class.getResource("/bias/res/add.png"));
    public static final ImageIcon ICON_RENAME = 
        new ImageIcon(Constants.class.getResource("/bias/res/rename.png"));
    public static final ImageIcon ICON_DELETE = 
        new ImageIcon(Constants.class.getResource("/bias/res/delete.png"));
    public static final ImageIcon ICON_IMPORT_DATA = 
        new ImageIcon(Constants.class.getResource("/bias/res/import_data.png"));
    public static final ImageIcon ICON_SWITCH_MODE = 
        new ImageIcon(Constants.class.getResource("/bias/res/switch_mode.png"));
    public static final ImageIcon ICON_TEXT_BOLD = 
        new ImageIcon(Constants.class.getResource("/bias/res/text_bold.png"));
    public static final ImageIcon ICON_TEXT_ITALIC = 
        new ImageIcon(Constants.class.getResource("/bias/res/text_italic.png"));
    public static final ImageIcon ICON_TEXT_UNDERLINE = 
        new ImageIcon(Constants.class.getResource("/bias/res/text_underlined.png"));
    
}
