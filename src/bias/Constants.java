/**
 * Created on Oct 19, 2006
 */
package bias;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import bias.extension.Extension;
import bias.i18n.I18nService;
import bias.skin.Skin;
import bias.utils.FSUtils;

/**
 * @author kion
 */
public class Constants {
    
    public static enum TRANSFER_TYPE {
        IMPORT,
        EXPORT
    }
    
    public static final File ROOT_DIR = Launcher.getRootDir();
    public static final File ADDONS_DIR = new File(ROOT_DIR, "addons");
    private static final String DIRNAME_ADDON_INFO = "ADDON-INFO";
    public static final File ADDON_INFO_DIR = new File(ADDONS_DIR, DIRNAME_ADDON_INFO);
    public static final File CONFIG_DIR = new File(ROOT_DIR, "conf");
    public static final File DATA_DIR = new File(ROOT_DIR, "data");
    public static final File ATTACHMENTS_DIR = new File(ROOT_DIR, "atts");
    public static final File ICONS_DIR = new File(ROOT_DIR, "icons");
    public static final File TMP_DIR = new File(ROOT_DIR, ".tmp");
    
    static {
        if (!ADDONS_DIR.exists()) {
            ADDONS_DIR.mkdir();
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
        if (TMP_DIR.exists()) {
            FSUtils.delete(TMP_DIR);
        }
        TMP_DIR.mkdir();
    }
    
    public static final String USERNAME = System.getProperty("user.name");
    
    public static final String REPOSITORY_BASE_URL = "https://raw.githubusercontent.com/kion/Bias/master/res/META-INF/ADDON-INFO/";
    public static final String ONLINE_REPOSITORY_DESCRIPTOR_FILE_NAME = "repository.xml";
    
    public static final String HTML_PREFIX = "<html>";
    public static final String HTML_COLOR_NORMAL = "<font color=\"#000000\">";
    public static final String HTML_COLOR_HIGHLIGHT_LINK = "<font color=\"blue\">";
    public static final String HTML_COLOR_HIGHLIGHT_OK = "<font color=\"#00A000\">";
    public static final String HTML_COLOR_HIGHLIGHT_INFO = "<font color=\"#0000AF\">";
    public static final String HTML_COLOR_HIGHLIGHT_ERROR = "<font color=\"#FA0000\">";
    public static final String HTML_COLOR_HIGHLIGHT_WARNING = "<font color=\"#FA0000\">";
    public static final String HTML_COLOR_SUFFIX = "</font>";
    public static final String HTML_SUFFIX = "</html>";
    public static final String ADDON_FIELD_VALUE_NA = "N/A";
    
    public static enum ADDON_STATUS {
        Imported,
        Installed,
        Updated,
        Loaded,
        New,
        Update,
        Unused,
        Broken,
        BrokenDependencies,
        RegisteredInstalled,
        NotRegisteredInstalled,
    }
    
    private static Map<ADDON_STATUS, String> statuses;
    private static final String ADDON_STATUS_KEY_PREFIX = "AddOnStatus";
    public static String getAddOnStatusCaption(ADDON_STATUS status) {
        if (statuses == null) {
            statuses = new HashMap<ADDON_STATUS, String>();
            Map<String, String> messages = I18nService.getInstance().getMessages();
            statuses.put(ADDON_STATUS.Imported, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_INFO + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.Imported.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.Installed, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_INFO + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.Installed.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.Updated, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_INFO + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.Updated.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.Loaded, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_OK + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.Loaded.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.New, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_OK + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.New.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.Update, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_INFO + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.Update.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.Unused, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_INFO + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.Unused.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.Broken, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_ERROR + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.Broken.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.BrokenDependencies, HTML_PREFIX + HTML_COLOR_HIGHLIGHT_ERROR + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.BrokenDependencies.name()) + HTML_COLOR_SUFFIX + HTML_SUFFIX);
            statuses.put(ADDON_STATUS.RegisteredInstalled, HTML_COLOR_HIGHLIGHT_OK + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.RegisteredInstalled.name()) + HTML_COLOR_SUFFIX);
            statuses.put(ADDON_STATUS.NotRegisteredInstalled, HTML_COLOR_HIGHLIGHT_ERROR + messages.get(ADDON_STATUS_KEY_PREFIX + ADDON_STATUS.NotRegisteredInstalled.name()) + HTML_COLOR_SUFFIX);
        }
        return statuses.get(status);
    }
    
    public static final String DEFAULT_ENCODING = "UTF-8";
    
    public static final String DIGEST_ALGORITHM = "MD5";
    public static final String CIPHER_ALGORITHM = "PBEWithMD5AndDES";
    
    public static final byte[] CIPHER_SALT = "kn+kv=lv".getBytes();
    
    public static final String EMPTY_STR = "";
    public static final String BLANK_STR = " ";
    public static final String NEW_LINE = "\n";

    public static final String PATH_PREFIX_PATTERN = "^.*/";
    public static final String PATH_SEPARATOR = "/";

    public static final String VALUES_SEPARATOR = "_";
    public static final String JAR_FILE_SUFFIX = ".jar";
    public static final String ZIP_FILE_SUFFIX = ".zip";
    
    public static final String PROPERTY_VALUES_SEPARATOR = ",";
    
    public static final String PACKAGE_PATH_SEPARATOR = ".";
    public static final String PACKAGE_PREFIX_PATTERN = "^.*\\.";

    public static final String JAR_FILE_PATTERN = "(?i).+\\.jar$";
    public static final String JAR_FILE_PATTERN_DESCRIPTION = "Java Archive (JAR) file";
    public static final String JAR_FILE_ADDON_INFO_DIR_PATH = "META-INF/ADDON-INFO/";
    public static final String JAR_FILE_ICONSET_REG_PATH = "META-INF/ADDON-INFO/iconset.reg";

    public static final String ZIP_FILE_PATTERN = "(?i).+\\.zip$";
    public static final String ZIP_FILE_PATTERN_DESCRIPTION = "ZIP archive file";

    public static final String ENTRY_PROTOCOL_PREFIX = "entry://";
    
    public static final String GLOBAL_CONFIG_FILE = "config.properties";
    public static final String PREFERENCES_FILE = "preferences.properties";
    public static final String UNINSTALL_CONFIG_FILE = "uninstall.conf";
    public static final String METADATA_FILE_NAME = "metadata.xml";
    public static final String ICONS_CONFIG_FILE = "icons.conf";
    public static final String ADDON_INFO_FILENAME_SUFFIX = ".info.html";
    public static final String DATA_FILE_SUFFIX = ".data";
    public static final String TOOL_DATA_FILE_SUFFIX = ".tooldata";
    public static final String FILE_SUFFIX_PATTERN = "\\..*$";
    public static final String EXTENSION_JAR_FILE_SUFFIX = ".ext.jar";
    public static final String SKIN_JAR_FILE_SUFFIX = ".skin.jar";
    public static final String LIB_JAR_FILE_SUFFIX = ".lib.jar";
    public static final String IMPORT_CONFIG_FILE_SUFFIX = ".import.properties";
    public static final String EXPORT_CONFIG_FILE_SUFFIX = ".export.properties";
    public static final String TRANSFER_OPTIONS_CONFIG_FILE_SUFFIX = ".transfer.conf";
    public static final String EXTENSION_PACKAGE_NAME = Extension.class.getPackage().getName();
    public static final String SKIN_PACKAGE_NAME = Skin.class.getPackage().getName();
    public static final String DEFAULT_IMAGE_FORMAT = "PNG";
    public static final String ICON_FILE_SUFFIX = ".png";
    public static final String CHECKSUM_CONFIG_FILE_SUFFIX = ".checksum.properties";
    public static final String EXTENSION_CONFIG_FILE_SUFFIX = ".ext.conf";
    public static final String SKIN_CONFIG_FILE_SUFFIX = ".skin.conf";
    public static final String DATA_ENTRY_CONFIG_FILE_SUFFIX = ".data.conf";
    public static final String ADDON_EXTENSION_INFO_FILE_SUFFIX = ".ext.info";
    public static final String ADDON_SKIN_INFO_FILE_SUFFIX = ".skin.info";
    public static final String ADDON_LIB_INFO_FILE_SUFFIX = ".lib.info";
    public static final String ADDON_ICONSET_INFO_FILE_SUFFIX = ".iconset.info";
    public static final String ICONSET_REGISTRY_FILE_SUFFIX = ".iconset.reg";
    public static final String UPDATE_FILE_PREFIX = "update_";
    public static final String APP_CORE_FILE_NAME = "appcore.jar";
    public static final String APP_LAUNCHER_FILE_NAME = "bias.jar";

    public static final String ATTRIBUTE_APP_CORE_VERSION = "AppCore-Version";
    public static final String ATTRIBUTE_APP_LAUNCHER_VERSION = "AppLauncher-Version";
    public static final String ATTRIBUTE_ADD_ON_NAME = "Bias-Add-On-Name";
    public static final String ATTRIBUTE_ADD_ON_TYPE = "Bias-Add-On-Type";
    public static final String ATTRIBUTE_ADD_ON_VERSION = "Bias-Add-On-Version";
    public static final String ATTRIBUTE_ADD_ON_AUTHOR = "Bias-Add-On-Author";
    public static final String ATTRIBUTE_ADD_ON_DESCRIPTION = "Bias-Add-On-Description";
    public static final String ATTRIBUTE_ADD_ON_DEPENDENCIES = "Bias-Add-On-Dependencies";

    public static final String DATA_TREE_ROOT_NODE_CAPTION = "ALL DATA";
    public static final String OPTION_TRANSFER_PROVIDER = "TRANSFER_PROVIDER";
    public static final String OPTION_CONFIG_NAME = "CONFIG_NAME";
    public static final String OPTION_EXPORT_ALL = "EXPORT_ALL";
    public static final String OPTION_SELECTED_RECURSIVE_IDS = "SELECTED_RECURSIVE_IDS";
    public static final String OPTION_SELECTED_IDS = "SELECTED_IDS";
    public static final String OPTION_DATA_PASSWORD = "DATA_PASSWORD";
    public static final String OPTION_PROCESS_PREFERENCES = "PROCESS_PREFERENCES";
    public static final String OPTION_OVERWRITE_PREFERENCES = "OVERWRITE_PREFERENCES";
    public static final String OPTION_PROCESS_DATA_ENTRIES = "PROCESS_DATA_ENTRIES";
    public static final String OPTION_OVERWRITE_DATA_ENTRIES = "OVERWRITE_DATA_ENTRIES";
    public static final String OPTION_PROCESS_ONLY_RELATED_DATA_ENTRY_CONFIGS = "PROCESS_ONLY_RELATED_DATA_ENTRY_CONFIGS";
    public static final String OPTION_PROCESS_DATA_ENTRY_CONFIGS = "PROCESS_DATA_ENTRY_CONFIGS";
    public static final String OPTION_OVERWRITE_DATA_ENTRY_CONFIGS = "OVERWRITE_DATA_ENTRY_CONFIGS";
    public static final String OPTION_PROCESS_TOOLS_DATA = "PROCESS_TOOLS_DATA";
    public static final String OPTION_OVERWRITE_TOOLS_DATA = "OVERWRITE_TOOLS_DATA";
    public static final String OPTION_PROCESS_ONLY_RELATED_ICONS = "PROCESS_ONLY_RELATED_ICONS";
    public static final String OPTION_PROCESS_ICONS = "PROCESS_ICONS";
    public static final String OPTION_OVERWRITE_ICONS = "OVERWRITE_ICONS";
    public static final String OPTION_PROCESS_APP_CORE = "PROCESS_APP_CORE";
    public static final String OPTION_PROCESS_ADDONS_AND_LIBS = "PROCESS_ADDONS_AND_LIBS";
    public static final String OPTION_UPDATE_ADDONS_AND_LIBS = "UPDATE_ADDONS_AND_LIBS";
    public static final String OPTION_PROCESS_ADDON_CONFIGS = "PROCESS_ADDON_CONFIGS";
    public static final String OPTION_PROCESS_IMPORT_EXPORT_CONFIGS = "PROCESS_IMPORT_EXPORT_CONFIGS";
    public static final String OPTION_OVERWRITE_IMPORT_EXPORT_CONFIGS = "OVERWRITE_IMPORT_EXPORT_CONFIGS";
    public static final String OPTION_OVERWRITE_ADDON_CONFIGS = "OVERWRITE_ADDON_CONFIGS";
    
    public static final String PROPERTY_SKIN = "SKIN";
    public static final String PROPERTY_WINDOW_COORDINATE_X = "WINDOW_COORDINATE_X";
    public static final String PROPERTY_WINDOW_COORDINATE_Y = "WINDOW_COORDINATE_Y";
    public static final String PROPERTY_WINDOW_WIDTH = "WINDOW_WIDTH";
    public static final String PROPERTY_WINDOW_HEIGHT = "WINDOW_HEIGHT";
    public static final String PROPERTY_SHOW_ALL_ONLINE_PACKS = "SHOW_ALL_ONLINE_PACKS";
    public static final String PROPERTY_LAST_SELECTED_ID = "LAST_SELECTED_ID";
    public static final String PROPERTY_LAST_UPDATE_DATE = "LAST_UPDATE_DATE";

    public static final String PROPERTY_FORCE_TEXT_ANTIALIASING_MODE = "FORCE_TEXT_ANTIALIASING_MODE";
    public static final String PROPERTY_CUSTOM_TEXT_ANTIALIASING_MODE = "CUSTOM_TEXT_ANTIALIASING_MODE";

    public static final String META_DATA_CHECKSUM = "CHECKSUM";
    public static final String META_DATA_USERNAME = "USERNAME";
    public static final String META_DATA_TIMESTAMP = "TIMESTAMP";
    public static final String META_DATA_FILESIZE = "FILESIZE";

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
    public static final String XML_ELEMENT_ATTRIBUTE_RECURSIVELY_EXPORTED = "recursively-exported";

    public static final String MESSAGES_PATH_PREFIX = PATH_SEPARATOR + I18nService.class.getPackage().getName().replaceAll("\\.", PATH_SEPARATOR) + PATH_SEPARATOR;
    public static final String LANGUAGES_LIST_FILENAME = "lang.list";
    public static final String MESSAGE_FILE_PATH = PATH_SEPARATOR + I18nService.class.getPackage().getName().replaceAll("\\.", PATH_SEPARATOR) + PATH_SEPARATOR;
    public static final String MESSAGE_FILE_ENDING = ".msg";
    public static final String MESSAGE_KEY_VALUE_SEPARATOR = "==";
    public static final String MESSAGE_SEPARATOR = "\n+\r*";

    public static final String DEFAULT_LANGUAGE = I18nService.getInstance().getAvailableLanguages().iterator().next();
    public static final String DEFAULT_LOCALE = I18nService.getInstance().getLanguageLocale(DEFAULT_LANGUAGE);

}
