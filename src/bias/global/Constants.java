/**
 * Created on Oct 19, 2006
 */
package bias.global;


import javax.swing.ImageIcon;


/**
 * @author kion
 */
public class Constants {

    public static final String UNICODE_ENCODING = "UTF-8";

    public static final String EMPTY_STR = "";
    public static final String NULL_STR = "NULL";
    public static final String EMPTY_STR_PATTERN = "\\s*";
    
    public static final String DATA_FILE_PATTERN = "data/\\d+\\.data";
    public static final String DATA_FILE_ENDING_PATTERN = "\\.data";
    public static final String DATA_FILE_ENDING = ".data";
    public static final String CONFIG_FILE_PATH = "conf/config.properties";
    public static final String METADATA_FILE_PATH = "data/metadata.xml";
    public static final String DATA_DIR_PATTERN = "data/";
    public static final String DATA_DIR = "data/";

    public static final String PROPERTY_WINDOW_COORDINATE_X = "WINDOW_COORDINATE_X";
    public static final String PROPERTY_WINDOW_COORDINATE_Y = "WINDOW_COORDINATE_Y";
    public static final String PROPERTY_WINDOW_WIDTH = "WINDOW_WIDTH";
    public static final String PROPERTY_WINDOW_HEIGHT = "WINDOW_HEIGHT";
    public static final String PROPERTY_LAST_SELECTED_CATEGORY_INDEX = "LAST_SELECTED_CATEGORY_INDEX";
    public static final String PROPERTY_LAST_SELECTED_ENTRY_INDEX = "LAST_SELECTED_ENTRY_INDEX";

    public static final ImageIcon ICON_APP = 
        new ImageIcon(Constants.class.getResource("/bias/res/app_icon.png"));
    public static final ImageIcon ICON_ADD = 
        new ImageIcon(Constants.class.getResource("/bias/res/add.png"));
    public static final ImageIcon ICON_RENAME = 
        new ImageIcon(Constants.class.getResource("/bias/res/rename.png"));
    public static final ImageIcon ICON_DELETE = 
        new ImageIcon(Constants.class.getResource("/bias/res/delete.png"));
    public static final ImageIcon ICON_IMPORT_DATA = 
        new ImageIcon(Constants.class.getResource("/bias/res/import_data.png"));
    public static final ImageIcon ICON_ABOUT = 
        new ImageIcon(Constants.class.getResource("/bias/res/about.png"));
    
}
