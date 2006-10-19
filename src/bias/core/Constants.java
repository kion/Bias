/**
 * Created on Oct 19, 2006
 */
package bias.core;

/**
 * @author kion
 */
public interface Constants {

    public static final String PATH_SEPARATOR = System.getProperty("file.separator");

    public static final String EMPTY_STR = "";
    
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

}
