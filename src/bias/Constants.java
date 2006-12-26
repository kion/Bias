/**
 * Created on Oct 19, 2006
 */
package bias;

import bias.extension.Extension;

/**
 * @author kion
 */
public class Constants {

    public static final String UNICODE_ENCODING = "UTF-8";

    public static final String EMPTY_STR = "";
    public static final String SPACE_STR = " ";
    public static final String NULL_STR = "NULL";
    public static final String EMPTY_STR_PATTERN = "\\s*";

    public static final String ZIP_PATH_SEPARATOR = "/";
    public static final String PACKAGE_PATH_SEPARATOR = ".";
    public static final String PACKAGE_PREFIX_PATTERN = "^.*\\.";

    public static final String JAR_FILE_PATTERN = "(?i).*\\.jar$";
    
    public static final String EXTENSION_FILE_PATTERN = "(?i).*\\.(class|jar)$";
    public static final String EXTENSION_FILE_PATTERN_DESCRIPTION = "Java Class, JAR";

    public static final String ANY_CHARACTERS_PATTERN = ".*";
    
    public static final String CONFIG_FILE_PATH = "conf/config.properties";
    public static final String METADATA_FILE_PATH = "data/metadata.xml";
    public static final String DATA_FILE_ENDING = ".data";
    public static final String DATA_FILE_ENDING_PATTERN = "\\" + DATA_FILE_ENDING + "$";
    public static final String DATA_DIR_PATTERN = "^data/";
    public static final String DATA_FILE_PATTERN = DATA_DIR_PATTERN + ".+\\" + DATA_FILE_ENDING;
    public static final String DATA_DIR = "data/";
    public static final String EXTENSION_FILE_ENDING = ".class";
    public static final String EXTENSION_FILE_PATH_PATTERN = "classes/.+\\" + EXTENSION_FILE_ENDING;
    public static final String EXTENSION_DIR_PACKAGE_NAME = Extension.class.getPackage().getName();
    public static final String EXTENSION_DIR_PATH = EXTENSION_DIR_PACKAGE_NAME.replaceAll("\\.", ZIP_PATH_SEPARATOR);
    public static final String EXTENSION_PATTERN = EXTENSION_DIR_PATH + "/[\\w/]+/[\\w/]+\\" + EXTENSION_FILE_ENDING;
    public static final String CLASS_FILE_PREFIX_PATTERN = "^classes/";
    public static final String RESOURCE_FILE_PATTERN = "^resources/.+";
    public static final String RESOURCE_FILE_PREFIX_PATTERN = "^resources/";
    public static final String RESOURCES_DIR = "bias/res/";
    public static final String ICON_FORMAT = "PNG";
    public static final String ICON_FILE_ENDING = ".png";
    public static final String ICONS_DIR = "icons/";
    public static final String ICONS_DIR_PATTERN = "^icons/";
    public static final String ICON_FILE_PATH_PATTERN = ICONS_DIR_PATTERN + ".+\\" + ICON_FILE_ENDING;
    public static final String ATTACHMENTS_DIR = "atts/";
    public static final String ATTACHMENTS_DIR_PATTERN = "^atts/";
    public static final String ATTACHMENT_FILE_PATH_PATTERN = ATTACHMENTS_DIR_PATTERN + ".+/.+";
    
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
    public static final String XML_ELEMENT_ATTRIBUTE_CAPTION = "caption";
    public static final String XML_ELEMENT_ATTRIBUTE_ICON = "icon";
    public static final String XML_ELEMENT_ATTRIBUTE_TYPE = "type";
    public static final String XML_ELEMENT_ATTRIBUTE_PLACEMENT = "placement";
    public static final String XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX = "active-idx";

}
