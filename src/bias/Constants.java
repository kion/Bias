/**
 * Created on Oct 19, 2006
 */
package bias;

import bias.extension.Extension;
import bias.laf.LookAndFeelManager;

/**
 * @author kion
 */
public class Constants {
    
    public static final String UNICODE_ENCODING = "UTF-8";
    
    public static final String FILE_PROTOCOL_PREFIX = "file:";

    public static final String META_INF_DIR = "META-INF";
        
    public static final String EMPTY_STR = "";
    public static final String SPACE_STR = " ";
    public static final String NEW_LINE = "\n";
    public static final String NULL_STR = "NULL";
    public static final String EMPTY_STR_PATTERN = "\\s*";

    public static final String ZIP_PATH_SEPARATOR = "/";
    public static final String PACKAGE_PATH_SEPARATOR = ".";
    public static final String PACKAGE_PATH_SEPARATOR_PATTERN = "\\.";
    public static final String PACKAGE_PREFIX_PATTERN = "^.*\\.";

    public static final String ADDON_FILE_PATTERN = "(?i).+\\.jar$";
    public static final String ADDON_FILE_PATTERN_DESCRIPTION = "JAR file";

    public static final String ANY_CHARACTERS_PATTERN = ".*";
    
    public static final String CONFIG_DIR = "conf/";
    public static final String CONFIG_FILE_PATH = CONFIG_DIR + "config.properties";
    public static final String METADATA_FILE_PATH = "data/metadata.xml";
    public static final String DATA_FILE_ENDING = ".data";
    public static final String DATA_FILE_ENDING_PATTERN = "\\" + DATA_FILE_ENDING + "$";
    public static final String DATA_DIR_PATTERN = "^data/";
    public static final String DATA_FILE_PATTERN = DATA_DIR_PATTERN + ".+\\" + DATA_FILE_ENDING;
    public static final String DATA_DIR = "data/";
    public static final String CLASS_FILE_ENDING = ".class";
    public static final String EXTENSION_FILE_PATH_PATTERN = "cls/.+\\" + CLASS_FILE_ENDING;
    public static final String EXTENSION_DIR_PACKAGE_NAME = Extension.class.getPackage().getName();
    public static final String EXTENSION_DIR_PATH = EXTENSION_DIR_PACKAGE_NAME.replaceAll("\\.", ZIP_PATH_SEPARATOR);
    public static final String EXTENSION_PATTERN = EXTENSION_DIR_PATH + "/[\\w/]+/[\\w/]+\\" + CLASS_FILE_ENDING;
    public static final String EXT_CLASS_PREFIX_PATTERN = "^cls/";
    public static final String RESOURCE_FILE_PATH_PATTERN = "^res/.+";
    public static final String RESOURCE_FILE_PREFIX_PATTERN = "^res/";
    public static final String RESOURCES_DIR = "bias/res/";
    public static final String ICON_FORMAT = "PNG";
    public static final String ICON_FILE_ENDING = ".png";
    public static final String ICONS_DIR = "icons/";
    public static final String ICONS_DIR_PATTERN = "^icons/";
    public static final String ICON_FILE_PATH_PATTERN = ICONS_DIR_PATTERN + ".+\\" + ICON_FILE_ENDING;
    public static final String ATTACHMENTS_DIR = "atts/";
    public static final String ATTACHMENTS_DIR_PATTERN = "^atts/";
    public static final String ATTACHMENT_FILE_PATH_PATTERN = ATTACHMENTS_DIR_PATTERN + ".+/.+";
    public static final String LIB_DIR_PATTERN = "^lib/";
    public static final String LIB_FILE_PATH_PATTERN = "(?i)" + LIB_DIR_PATTERN + "[^/]+\\.jar$";
    public static final String LAF_MANAGER_PATH_PATTERN = "lfm/.+\\" + CLASS_FILE_ENDING;
    public static final String LAF_MANAGER_CLASS_PREFIX_PATTERN = "^lfm/";
    public static final String LAF_DIR_PACKAGE_NAME = LookAndFeelManager.class.getPackage().getName();
    public static final String LAF_DIR_PATH = LAF_DIR_PACKAGE_NAME.replaceAll("\\.", ZIP_PATH_SEPARATOR);
    public static final String LAF_PATTERN = LAF_DIR_PATH + "/[\\w/]+/[\\w/]+\\" + CLASS_FILE_ENDING;
    public static final String EXT_LIB_INSTALL_LOG_FILE_ENDING = ".ext.lib.inst.log";
    public static final String LAF_LIB_INSTALL_LOG_FILE_ENDING = ".laf.lib.inst.log";
    public static final String LAF_CONFIG_FILE_ENDING = ".laf.conf";
    
    public static final String PROPERTY_LOOK_AND_FEEL = "LOOK_AND_FEEL";
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
