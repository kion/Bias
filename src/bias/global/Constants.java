/**
 * Created on Oct 19, 2006
 */
package bias.global;

import bias.gui.extension.Extension;
import bias.gui.extension.MissingExtensionInformer;

/**
 * @author kion
 */
public class Constants {

    public static final String UNICODE_ENCODING = "UTF-8";

    public static final String EMPTY_STR = "";
    public static final String NULL_STR = "NULL";
    public static final String EMPTY_STR_PATTERN = "\\s*";

    public static final String ZIP_PATH_SEPARATOR = "/";
    public static final String PACKAGE_PATH_SEPARATOR = ".";

    public static final String ARCHIVE_FILE_PATTERN = "(?i).*\\.(jar)$";
    
    public static final String EXTENSION_FILE_PATTERN = "(?i).*\\.(class|jar)$";
    public static final String EXTENSION_FILE_PATTERN_DESCRIPTION = "Java Class, JAR";

    public static final String ANY_CHARACTERS_PATTERN = ".*";
    
    public static final String DATA_FILE_PATTERN = "data/[\\d/]+\\.data";
    public static final String DATA_FILE_ENDING_PATTERN = "\\.data";
    public static final String DATA_FILE_ENDING = ".data";
    public static final String CONFIG_FILE_PATH = "conf/config.properties";
    public static final String METADATA_FILE_PATH = "data/metadata.xml";
    public static final String DATA_DIR_PATTERN = "data/";
    public static final String DATA_DIR = "data/";
    public static final String VISUAL_COMPONENT_FILE_ENDING = ".class";
    public static final String VISUAL_COMPONENT_DIR_PATH = 
        Extension.class.getPackage().getName().replaceAll("\\.", ZIP_PATH_SEPARATOR);
    public static final String VISUAL_COMPONENT_FILE_PATTERN = VISUAL_COMPONENT_DIR_PATH + "/[\\w/]+\\.class";
    public static final String VISUAL_COMPONENT_SKIP_FILE_PATH = 
        "(" + Extension.class.getName().replaceAll("\\.", ZIP_PATH_SEPARATOR) + "|" 
        + MissingExtensionInformer.class.getName().replaceAll("\\.", ZIP_PATH_SEPARATOR) + ")" 
        + "\\" + VISUAL_COMPONENT_FILE_ENDING;
    public static final String VISUAL_COMPONENT_SKIP_FILE_NAME = 
        "(" + Extension.class.getSimpleName() + "|" 
        + MissingExtensionInformer.class.getSimpleName() + ")" 
        + "\\" + VISUAL_COMPONENT_FILE_ENDING;
    public static final String ICON_FORMAT = "PNG";
    public static final String ICON_FILE_ENDING = ".png";
    public static final String ICONS_DIR = "icons/";
    public static final String ICON_FILE_PATH_PATTERN = ICONS_DIR + ".*\\" + ICON_FILE_ENDING;
    public static final String ICON_FILE_PATTERN = "(?i).*\\.(png|gif|jpg|jpeg)";
    public static final String ICON_FILE_PATTERN_DESCRIPTION = "Icon File (PNG/GIF/JPG)";
    
    public static final String PROPERTY_WINDOW_COORDINATE_X = "WINDOW_COORDINATE_X";
    public static final String PROPERTY_WINDOW_COORDINATE_Y = "WINDOW_COORDINATE_Y";
    public static final String PROPERTY_WINDOW_WIDTH = "WINDOW_WIDTH";
    public static final String PROPERTY_WINDOW_HEIGHT = "WINDOW_HEIGHT";
    public static final String PROPERTY_LAST_SELECTED_ID = "LAST_SELECTED_ID";

    public static final String ENTRY_PROTOCOL_PREFIX = "entry://";
    
    public static final String XML_ELEMENT_ROOT_CONTAINER = "root-container";
    public static final String XML_ELEMENT_CATEGORY = "category";
    public static final String XML_ELEMENT_ENTRY = "entry";
    public static final String XML_ELEMENT_ATTRIBUTE_ID = "id";
    public static final String XML_ELEMENT_ATTRIBUTE_NUMBER = "number";
    public static final String XML_ELEMENT_ATTRIBUTE_CAPTION = "caption";
    public static final String XML_ELEMENT_ATTRIBUTE_ICON = "icon";
    public static final String XML_ELEMENT_ATTRIBUTE_TYPE = "type";
    public static final String XML_ELEMENT_ATTRIBUTE_PLACEMENT = "placement";
    public static final String XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX = "active-idx";

}
