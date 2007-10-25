/**
 * Created on Oct 19, 2006
 */
package bias;

import java.io.File;

import bias.extension.Extension;
import bias.laf.LookAndFeel;

/**
 * @author kion
 */
public class Constants {
    
    public static final File ROOT_DIR = Bias.getJarFile().getParentFile();
    public static final File ADDONS_DIR = new File(ROOT_DIR, "addons");
    public static final File LIBS_DIR = new File(ROOT_DIR, "libs");
    public static final File CONFIG_DIR = new File(ROOT_DIR, "conf");
    public static final File DATA_DIR = new File(ROOT_DIR, "data");
    public static final File ATTACHMENTS_DIR = new File(ROOT_DIR, "atts");
    public static final File ICONS_DIR = new File(ROOT_DIR, "icons");
    public static final File TMP_DIR = new File(ROOT_DIR, "tmp");
    
    static {
        if (!ADDONS_DIR.exists()) {
            ADDONS_DIR.mkdir();
        }
        if (!LIBS_DIR.exists()) {
            LIBS_DIR.mkdir();
        }
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdir();
        }
        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdir();
        }
        if (!ATTACHMENTS_DIR.exists()) {
            ATTACHMENTS_DIR.mkdir();
        }
        if (!ICONS_DIR.exists()) {
            ICONS_DIR.mkdir();
        }
        if (!TMP_DIR.exists()) {
            TMP_DIR.mkdir();
        }
    }
    
    public static final String COMMENT_ADDON_IMPORTED = "Imported  [restart needed]";
    public static final String COMMENT_ADDON_INSTALLED = "Installed [restart needed]";
    
    public static final String UNICODE_ENCODING = "UTF-8";
    
    public static final String FILE_PROTOCOL_PREFIX = "file:";

    public static final String CIPHER_ALGORITHM = "PBEWithMD5AndDES";
    
    public static final byte[] CIPHER_SALT = "kn+kv=lv".getBytes();
    
    public static final String EMPTY_STR = "";
    public static final String NEW_LINE = "\n";

    public static final String PATH_PREFIX_PATTERN = "^.*/";
    public static final String PATH_SEPARATOR = "/";
    
    public static final String ADDON_LIB_FILENAME_SEPARATOR = "_";
    public static final String CLASSPATH_SEPARATOR = ":";
    
    public static final String PACKAGE_PATH_SEPARATOR = ".";
    public static final String PACKAGE_PREFIX_PATTERN = "^.*\\.";

    public static final String ADDON_PACK_PATTERN = "(?i).+\\.jar$";
    public static final String ADDON_FILE_PATTERN_DESCRIPTION = "JAR file";

    public static final String GLOBAL_CONFIG_FILE = "config.properties";
    public static final String PREFERENCES_FILE = "preferences.properties";
    public static final String SYNC_TABLE_FILE = "sync_table.properties";
    public static final String SYNC_TABLE_REMOTE_FILE = "sync_table_remote.properties";
    public static final String CLASSPATH_CONFIG_FILE = "classpath.conf";
    public static final String METADATA_FILE_NAME = "metadata.xml";
    public static final String ICONS_CONFIG_FILE = "icons.conf";
    public static final String DATA_FILE_SUFFIX = ".data";
    public static final String TOOL_DATA_FILE_SUFFIX = ".tooldata";
    public static final String FILE_SUFFIX_PATTERN = "\\..*$";
    public static final String CLASS_FILE_SUFFIX = ".class";
    public static final String MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE = "Bias-Add-On-Name";
    public static final String EXTENSION_DIR_PATTERN = "^ext/";
    public static final String LAF_DIR_PATTERN = "^laf/";
    public static final String EXTENSION_JAR_FILE_PATH_PATTERN = "(?i)" + EXTENSION_DIR_PATTERN + "[^/]+\\.jar$";
    public static final String EXTENSION_JAR_FILE_PATTERN = "(?i)" + ".+\\.ext\\.jar$";
    public static final String LAF_JAR_FILE_PATH_PATTERN = "(?i)" + LAF_DIR_PATTERN + "[^/]+\\.jar$";
    public static final String LAF_JAR_FILE_PATTERN = "(?i)" + ".+\\.laf\\.jar$";
    public static final String EXTENSION_JAR_FILE_SUFFIX = ".ext.jar";
    public static final String LAF_JAR_FILE_SUFFIX = ".laf.jar";
    public static final String EXTENSION_DIR_PACKAGE_NAME = Extension.class.getPackage().getName();
    public static final String LAF_DIR_PACKAGE_NAME = LookAndFeel.class.getPackage().getName();
    public static final String ICON_FORMAT = "PNG";
    public static final String ICON_FILE_SUFFIX = ".png";
    public static final String LIBS_DIR_PATTERN = "^libs/";
    public static final String LIBS_FILE_PATH_PATTERN = "(?i)" + LIBS_DIR_PATTERN + "[^/]+\\.jar$";
    public static final String EXTENSION_CONFIG_FILE_SUFFIX = ".ext.conf";
    public static final String LAF_CONFIG_FILE_SUFFIX = ".laf.conf";
    public static final String DATA_ENTRY_CONFIG_FILE_SUFFIX = ".data.conf";
    
    public static final String PROPERTY_LOOK_AND_FEEL = "LOOK_AND_FEEL";
    public static final String PROPERTY_WINDOW_COORDINATE_X = "WINDOW_COORDINATE_X";
    public static final String PROPERTY_WINDOW_COORDINATE_Y = "WINDOW_COORDINATE_Y";
    public static final String PROPERTY_WINDOW_WIDTH = "WINDOW_WIDTH";
    public static final String PROPERTY_WINDOW_HEIGHT = "WINDOW_HEIGHT";
    public static final String PROPERTY_LAST_SELECTED_ID = "LAST_SELECTED_ID";

    public static final String ENTRY_PROTOCOL_PREFIX = "entry://";
    
    public static final String XML_ELEMENT_ROOT_CONTAINER = "root-container";
    public static final String XML_ELEMENT_PREFERENCE = "preference";
    public static final String XML_ELEMENT_CATEGORY = "category";
    public static final String XML_ELEMENT_ENTRY = "entry";
    public static final String XML_ELEMENT_ATTRIBUTE_ID = "id";
    public static final String XML_ELEMENT_ATTRIBUTE_CAPTION = "caption";
    public static final String XML_ELEMENT_ATTRIBUTE_ICON = "icon";
    public static final String XML_ELEMENT_ATTRIBUTE_TYPE = "type";
    public static final String XML_ELEMENT_ATTRIBUTE_PLACEMENT = "placement";
    public static final String XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX = "active-idx";
    public static final String XML_ELEMENT_ATTRIBUTE_VALUE = "value";

}
