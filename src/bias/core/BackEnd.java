/**
 * Created on Oct 15, 2006
 */
package bias.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bias.Constants;
import bias.Preferences;
import bias.Constants.TRANSFER_TYPE;
import bias.core.pack.Dependency;
import bias.core.pack.Pack;
import bias.core.pack.PackType;
import bias.extension.Extension;
import bias.extension.ExtensionFactory;
import bias.extension.ToolExtension;
import bias.extension.TransferExtension;
import bias.utils.ArchUtils;
import bias.utils.FSUtils;
import bias.utils.PropertiesUtils;
import bias.utils.Validator;
import bias.utils.VersionComparator;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author kion
 */
public class BackEnd {
    
    private static Cipher CIPHER_ENCRYPT;
    
    private static Cipher CIPHER_DECRYPT;
    
	private static BackEnd instance;
    
    private static String password;
    
    private String appVersion;
    
    private File metadataFile = new File(Constants.DATA_DIR, Constants.METADATA_FILE_NAME);

    private static Collection<AddOnInfo> loadedAddOns;

    private static Map<PackType, Map<AddOnInfo, String>> newAddOns = new LinkedHashMap<PackType, Map<AddOnInfo, String>>();

    private static Collection<String> uninstallAddOnsList = new ArrayList<String>();
    
    private Map<UUID, byte[]> icons = new LinkedHashMap<UUID, byte[]>();
    
    private Map<String, DataEntry> identifiedData = new LinkedHashMap<String, DataEntry>();
    
    private DataCategory data;
    
    private Map<String, byte[]> toolsData = new HashMap<String, byte[]>();
    
    private Document metadata;
    
    private Document prefs;
    
    private Properties config = new Properties();
    
    private Map<String, Properties> importConfigs;
    private Map<String, String> importConfigIDs;
    
    private Map<String, Properties> exportConfigs;
    private Map<String, String> exportConfigIDs;
    
    private static final FilenameFilter FILE_FILTER_DATA_ENTRY_CONFIG = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.DATA_ENTRY_CONFIG_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_DATA = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.DATA_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_TOOL_DATA = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.TOOL_DATA_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_ADDON_INFO = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(
                    Constants.ADDON_EXTENSION_INFO_FILE_SUFFIX) 
                    || name.endsWith(Constants.ADDON_SKIN_INFO_FILE_SUFFIX) 
                    || name.endsWith(Constants.ADDON_ICONSET_INFO_FILE_SUFFIX)
                    || name.endsWith(Constants.ADDON_LIB_INFO_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_LIB_INFO = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.ADDON_LIB_INFO_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_ICONSET_INFO = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.ADDON_ICONSET_INFO_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_EXTENSION_INFO = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.ADDON_EXTENSION_INFO_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_SKIN_INFO = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.ADDON_SKIN_INFO_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_ICONSET_INFO_OR_REG = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.ADDON_ICONSET_INFO_FILE_SUFFIX) || name.endsWith(Constants.ICONSET_REGISTRY_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_IMPORT_CONFIG = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.IMPORT_CONFIG_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_EXPORT_CONFIG = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.EXPORT_CONFIG_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_IMPORT_EXPORT_CONFIG = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.IMPORT_CONFIG_FILE_SUFFIX) || name.endsWith(Constants.EXPORT_CONFIG_FILE_SUFFIX);
        }
    };
    
    private static final FilenameFilter FILE_FILTER_ADDON_CONFIG = new FilenameFilter(){
        public boolean accept(File dir, String name) {
            return name.endsWith(Constants.EXTENSION_CONFIG_FILE_SUFFIX) || name.endsWith(Constants.SKIN_CONFIG_FILE_SUFFIX);
        }
    };
    
    private BackEnd() {
        try {
            CIPHER_ENCRYPT = initCipher(Cipher.ENCRYPT_MODE, password);
            CIPHER_DECRYPT = initCipher(Cipher.DECRYPT_MODE, password);
        } catch (Exception e) {
            System.err.println(
                    "Encryption/decryption ciphers initialization failed!" + Constants.NEW_LINE +
                    "This is most likely some system environment related problem." + Constants.NEW_LINE +
                    "Bias can not proceed further... :(");
            System.exit(1);
        }
	}
	
	public static BackEnd getInstance() {
		if (instance == null) {
			instance = new BackEnd();
		}
		return instance;
	}
	
	public URL getResourceURL(Class<? extends Extension> extensionClass, String resourceName) {
	    return BackEnd.class.getResource("/bias/res/" + extensionClass.getSimpleName() + "/" + resourceName);
	}
    
    public InputStream getResourceAsStream(Class<? extends Extension> extensionClass, String resourceName) {
        return BackEnd.class.getResourceAsStream("/bias/res/" + extensionClass.getSimpleName() + "/" + resourceName);
    }
    
    public static void setPassword(String currentPassword, String newPassword) throws Exception {
        if ((password != null ? currentPassword != null : currentPassword == null) && newPassword != null) {
            if ((password == null && currentPassword == null) || currentPassword.equals(password)) {
                password = newPassword;
                // changing encryption cipher
                CIPHER_ENCRYPT = initCipher(Cipher.ENCRYPT_MODE, password);
                if (currentPassword != null) {
                    reencryptData();
                    reencryptAttachments();
                    reencryptSettings();
                }
                // now decryption cipher can be changed as well
                CIPHER_DECRYPT = initCipher(Cipher.DECRYPT_MODE, password);
            } else {
                throw new Exception("Current password is wrong!");
            }
        }
    }
    
    private static Cipher initCipher(int mode, String password) throws Exception {
        PBEParameterSpec paramSpec = new PBEParameterSpec(Constants.CIPHER_SALT, 20);
        PBEKeySpec keySpec = new PBEKeySpec(password != null ? password.toCharArray() : new char[]{});
        SecretKeyFactory kf = SecretKeyFactory.getInstance(Constants.CIPHER_ALGORITHM);
        SecretKey key = kf.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_ALGORITHM);
        cipher.init(mode, key, paramSpec);
        return cipher;
    }

    public byte[] decrypt(byte[] data) throws Exception {
        return useCipher(CIPHER_DECRYPT, data);
    }
    
    public byte[] encrypt(byte[] data) throws Exception {
        return useCipher(CIPHER_ENCRYPT, data);
    }
    
    private static byte[] useCipher(Cipher cipher, byte[] data) throws Exception {
        if (data == null) {
            return null;
        }
        return cipher.doFinal(data);
    }
    
    public void load() throws Throwable {
        byte[] data = null;
        byte[] decryptedData = null;
        // data files
        if (Constants.DATA_DIR.exists()) {
            // entries data files
            for (File dataFile : Constants.DATA_DIR.listFiles(FILE_FILTER_DATA)) {
                DataEntry de = new DataEntry();
                String entryIDStr = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                identifiedData.put(entryIDStr, de);
            }
            // tools data files
            for (File dataFile : Constants.DATA_DIR.listFiles(FILE_FILTER_TOOL_DATA)) {
                data = FSUtils.readFile(dataFile);
                decryptedData = decrypt(data);
                String tool = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                tool = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                            + tool + Constants.PACKAGE_PATH_SEPARATOR + tool;
                toolsData.put(tool, decryptedData);
            }
        }
        // preferences file
        loadPreferences();
        // global config file
        loadConfig();
        // metadata file
        File metadataFile = new File(Constants.DATA_DIR, Constants.METADATA_FILE_NAME);
        if (metadataFile.exists()) {
            data = FSUtils.readFile(metadataFile);
            decryptedData = decrypt(data);
            metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(decryptedData));
        }
        // icon files
        if (Constants.ICONS_DIR.exists()) {
            File iconsListFile = new File(Constants.CONFIG_DIR, Constants.ICONS_CONFIG_FILE);
            if (iconsListFile.exists()) {
                String[] iconsList = new String(FSUtils.readFile(iconsListFile)).split(Constants.NEW_LINE);
                for (String iconId : iconsList) {
                    if (!Validator.isNullOrBlank(iconId)) {
                        File iconFile = new File(Constants.ICONS_DIR, iconId + Constants.ICON_FILE_SUFFIX);
                        if (iconFile.exists()) {
                            data = FSUtils.readFile(iconFile);
                            icons.put(UUID.fromString(iconId), data);
                        }
                    }
                }
            }
        }
        // parse metadata file
        this.data = parseMetadata(metadata, identifiedData, null, false);
        // get lists of loaded addons
        loadedAddOns = getAddOns();
        // get application's version
        appVersion = readAppVersion();
    }
    
    private Collection<String> getDataExportExtensionsList() {
        Collection<String> extensions = new ArrayList<String>();
        try {
            for (Entry<ToolExtension, String> entry : ExtensionFactory.getAnnotatedToolExtensions().entrySet()) {
                ToolExtension extension = entry.getKey();
                if (!extension.skipDataExport()) {
                    extensions.add(extension.getClass().getName());
                }
            }
        } catch (Throwable t) {
            // ignore
            t.printStackTrace(System.err);
        }
        return extensions;
    }
    
    private Collection<String> getConfigExportExtensionsList() {
        Collection<String> extensions = new ArrayList<String>();
        try {
            for (Entry<ToolExtension, String> entry : ExtensionFactory.getAnnotatedToolExtensions().entrySet()) {
                ToolExtension extension = entry.getKey();
                if (!extension.skipConfigExport()) {
                    extensions.add(extension.getClass().getName());
                }
            }
        } catch (Throwable t) {
            // ignore
            t.printStackTrace(System.err);
        }
        return extensions;
    }
    
    public void loadDataEntryData(DataEntry de) throws Exception {
        File dataFile = new File(Constants.DATA_DIR, de.getId() + Constants.DATA_FILE_SUFFIX);
        if (dataFile.exists()) {
            byte[] data = FSUtils.readFile(dataFile);
            if (data != null) {
                de.setData(decrypt(data));
            }
        }
    }
    
    public boolean isTransferFileLocationCheckSumChanged(TRANSFER_TYPE transferType, Class<? extends TransferExtension> transferExtClass, String fileLocation, String checkSum) throws Exception {
        File checkSumsFile = new File(Constants.CONFIG_DIR, transferExtClass.getSimpleName() + Constants.CHECKSUM_CONFIG_FILE_SUFFIX);
        if (checkSumsFile.exists()) {
            byte[] encryptedData = FSUtils.readFile(checkSumsFile);
            byte[] decryptedData = decrypt(encryptedData);
            Properties p = new Properties();
            ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
            p.load(bais);
            bais.close();
            String storedCheckSum = p.getProperty(transferType.name() + Constants.VALUES_SEPARATOR + fileLocation);
            if (!Validator.isNullOrBlank(storedCheckSum)) {
                if (storedCheckSum.equals(checkSum)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public void storeTransferFileLocationCheckSum(TRANSFER_TYPE transferType, Class<? extends TransferExtension> transferExtClass, String fileLocation, String checkSum) throws Exception {
        if (checkSum != null) {
            Properties p = new Properties();
            File checkSumsFile = new File(Constants.CONFIG_DIR, transferExtClass.getSimpleName() + Constants.CHECKSUM_CONFIG_FILE_SUFFIX);
            if (checkSumsFile.exists()) {
                byte[] encryptedData = FSUtils.readFile(checkSumsFile);
                byte[] decryptedData = decrypt(encryptedData);
                ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
                p.load(bais);
                bais.close();
            }
            p.setProperty(transferType.name() + Constants.VALUES_SEPARATOR + fileLocation, checkSum);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();;
            p.store(baos, null);
            byte[] encryptedData = encrypt(baos.toByteArray());
            FSUtils.writeFile(checkSumsFile, encryptedData);
        }
    }
    
    public DataCategory importData(
            File importDir, 
            Collection<UUID> existingIDs,
            boolean importDataEntries,
            boolean overwriteDataEntries,
            boolean importDataEntryConfigs,
            boolean overwriteDataEntryConfigs,
            boolean importPrefs,
            boolean overwritePrefs,
            boolean importGlobalConfig,
            boolean overwriteGlobalConfig,
            boolean importToolsData,
            boolean overwriteToolsData,
            boolean importIcons,
            boolean overwriteIcons,
            boolean importAndUpdateAppCore,
            boolean importAddOnsAndLibs,
            boolean updateInstalledAddOnsAndLibs,
            boolean importAddOnConfigs,
            boolean overwriteAddOnConfigs,
            boolean importImportExportConfigs,
            boolean overwriteImportExportConfigs,
            String password) throws Exception {
        Cipher cipher = initCipher(Cipher.DECRYPT_MODE, password);
        Map<String,DataEntry> importedIdentifiedData = new LinkedHashMap<String, DataEntry>();
        Document metadata = null;
        byte[] data = null;
        byte[] decryptedData = null;
        byte[] encryptedData = null;
        File dataDir = new File(importDir, Constants.DATA_DIR.getName());
        if (dataDir.exists()) {
            if (importDataEntries) {
                // data entry files
                for (File dataFile : dataDir.listFiles(FILE_FILTER_DATA)) {
                    String entryIDStr = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                    File localDataFile = new File(Constants.DATA_DIR, entryIDStr + Constants.DATA_FILE_SUFFIX);
                    if (!localDataFile.exists() || overwriteDataEntries) {
                        data = FSUtils.readFile(dataFile);
                        decryptedData = useCipher(cipher, data);
                        encryptedData = encrypt(decryptedData);
                        FSUtils.writeFile(localDataFile, encryptedData);
                        UUID id = UUID.fromString(entryIDStr);
                        DataEntry de = new DataEntry();
                        de.setId(id);
                        importedIdentifiedData.put(entryIDStr, de);
                    }
                }
                // attachments
                File attsDir = new File(importDir, Constants.ATTACHMENTS_DIR.getName());
                if (attsDir.exists()) {
                    for (File entryAttsDir : attsDir.listFiles()) {
                        for (File attFile : entryAttsDir.listFiles()) {
                            data = FSUtils.readFile(attFile);
                            decryptedData = useCipher(cipher, data);
                            encryptedData = encrypt(decryptedData);
                            entryAttsDir = new File(Constants.ATTACHMENTS_DIR, entryAttsDir.getName());
                            if (!entryAttsDir.exists()) {
                                entryAttsDir.mkdir();
                            }
                            attFile = new File(entryAttsDir, attFile.getName());
                            FSUtils.writeFile(attFile, encryptedData);
                        }
                    }
                }
                // metadata file
                File metadataFile = new File(dataDir, Constants.METADATA_FILE_NAME);
                if (metadataFile.exists()) {
                    data = FSUtils.readFile(metadataFile);
                    decryptedData = useCipher(cipher, data);
                    metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(decryptedData));
                }
            }
            // tools data files
            if (importToolsData) {
                for (File dataFile : dataDir.listFiles(FILE_FILTER_TOOL_DATA)) {
                    String iDStr = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                    File localDataFile = new File(Constants.DATA_DIR, iDStr + Constants.TOOL_DATA_FILE_SUFFIX);
                    if (!localDataFile.exists() || overwriteToolsData) {
                        data = FSUtils.readFile(dataFile);
                        decryptedData = useCipher(cipher, data);
                        encryptedData = encrypt(decryptedData);
                        FSUtils.writeFile(localDataFile, encryptedData);
                        String tool = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                        tool = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR + tool + Constants.PACKAGE_PATH_SEPARATOR + tool;
                        toolsData.put(tool, decryptedData);
                    }
                }
            }
        }
        // config files
        File configDir = new File(importDir, Constants.CONFIG_DIR.getName());
        if (configDir.exists()) {
            // preferences
            if (importPrefs) {
                File prefsFile = new File(configDir, Constants.PREFERENCES_FILE);
                if (prefsFile.exists()) {
                    File localPrefsFile = new File(Constants.CONFIG_DIR, Constants.PREFERENCES_FILE);
                    if (!localPrefsFile.exists() || overwritePrefs) {
                        FSUtils.duplicateFile(prefsFile, localPrefsFile);
                        // reload preferences file
                        loadPreferences();
                    }
                }
            }
            // global config
            if (importGlobalConfig) {
                File globalConfFile = new File(configDir, Constants.GLOBAL_CONFIG_FILE);
                if (globalConfFile.exists()) {
                    File localGlobalConfFile = new File(Constants.CONFIG_DIR, Constants.GLOBAL_CONFIG_FILE);
                    if (!localGlobalConfFile.exists() || overwriteGlobalConfig) {
                        FSUtils.duplicateFile(globalConfFile, localGlobalConfFile);
                        // reload global config file
                        loadConfig();
                    }
                }
            }
            // data entry configs
            if (importDataEntryConfigs) {
                for (File configFile : configDir.listFiles(FILE_FILTER_DATA_ENTRY_CONFIG)) {
                    File localConfigFile = new File(Constants.CONFIG_DIR, configFile.getName());
                    if (!localConfigFile.exists() || overwriteDataEntryConfigs) {
                        encryptedData = FSUtils.readFile(configFile);
                        decryptedData = useCipher(cipher, encryptedData);
                        encryptedData = encrypt(decryptedData);
                        FSUtils.writeFile(localConfigFile, encryptedData);
                    }
                }
            }
            // import/export configs
            if (importImportExportConfigs) {
                for (File configFile : configDir.listFiles(FILE_FILTER_IMPORT_EXPORT_CONFIG)) {
                    File localConfigFile = new File(Constants.CONFIG_DIR, configFile.getName());
                    if (!localConfigFile.exists() || overwriteImportExportConfigs) {
                        encryptedData = FSUtils.readFile(configFile);
                        decryptedData = useCipher(cipher, encryptedData);
                        encryptedData = encrypt(decryptedData);
                        FSUtils.writeFile(localConfigFile, encryptedData);
                    }
                }
                // reload import/export configs
                loadTransferConfigurations();
            }
            // add-on configs
            if (importAddOnConfigs) {
                for (File configFile : configDir.listFiles(FILE_FILTER_ADDON_CONFIG)) {
                    File localConfigFile = new File(Constants.CONFIG_DIR, configFile.getName());
                    if (!localConfigFile.exists() || overwriteAddOnConfigs) {
                        encryptedData = FSUtils.readFile(configFile);
                        decryptedData = useCipher(cipher, encryptedData);
                        encryptedData = encrypt(decryptedData);
                        FSUtils.writeFile(localConfigFile, encryptedData);
                    }
                }
            }
        }
        // icon files
        if (importIcons) {
            File iconsDir = new File(importDir, Constants.ICONS_DIR.getName());
            File iconsListFile = new File(configDir, Constants.ICONS_CONFIG_FILE);
            if (iconsListFile.exists()) {
                String[] iconsList = new String(FSUtils.readFile(iconsListFile)).split(Constants.NEW_LINE);
                for (String iconId : iconsList) {
                    if (!Validator.isNullOrBlank(iconId)) {
                        File iconFile = new File(iconsDir, iconId + Constants.ICON_FILE_SUFFIX);
                        File localIconFile = new File(Constants.ICONS_DIR, iconId + Constants.ICON_FILE_SUFFIX);
                        if (!localIconFile.exists() || overwriteIcons) {
                            data = FSUtils.readFile(iconFile);
                            FSUtils.writeFile(localIconFile, data);
                            icons.put(UUID.fromString(iconId), data);
                        }
                    }
                }
                for (File iconSetInfoOrRegFile : iconsDir.listFiles(FILE_FILTER_ICONSET_INFO_OR_REG)) {
                    File localIconSetInfoOrRegFile = new File(Constants.ICONS_DIR, iconSetInfoOrRegFile.getName());
                    FSUtils.duplicateFile(iconSetInfoOrRegFile, localIconSetInfoOrRegFile);
                }
            }
        }
        // app core
        if (importAndUpdateAppCore) {
            File appCoreFile = new File(importDir, Constants.APP_CORE_FILE_NAME);
            if (appCoreFile.exists()) {
                File localAppCoreUpdateFile = new File(Constants.ROOT_DIR, Constants.UPDATE_FILE_PREFIX + Constants.APP_CORE_FILE_NAME);
                FSUtils.duplicateFile(appCoreFile, localAppCoreUpdateFile);
            }
        }
        // addons/libs
        if (importAddOnsAndLibs) {
            File addOnsDir = new File(importDir, Constants.ADDONS_DIR.getName());
            if (addOnsDir.exists()) {
                for (File addOnFile : addOnsDir.listFiles()) {
                    File localAddOnFile = new File(Constants.ADDONS_DIR, addOnFile.getName());
                    if (!localAddOnFile.exists()) {
                        FSUtils.duplicateFile(addOnFile, localAddOnFile);
                    } else if (updateInstalledAddOnsAndLibs) {
                        localAddOnFile = new File(Constants.ADDONS_DIR, Constants.UPDATE_FILE_PREFIX + addOnFile.getName());
                        FSUtils.duplicateFile(addOnFile, localAddOnFile);
                    }
                }
            }
            File libsDir = new File(importDir, Constants.LIBS_DIR.getName());
            if (libsDir.exists()) {
                for (File libFile : libsDir.listFiles()) {
                    File localLibFile = new File(Constants.LIBS_DIR, libFile.getName());
                    if (!localLibFile.exists()) {
                        FSUtils.duplicateFile(libFile, localLibFile);
                    } else if (updateInstalledAddOnsAndLibs) {
                        localLibFile = new File(Constants.LIBS_DIR, Constants.UPDATE_FILE_PREFIX + libFile.getName());
                        FSUtils.duplicateFile(libFile, localLibFile);
                    }
                }
            }
        }
        // parse metadata file
        DataCategory importedData = parseMetadata(metadata, importedIdentifiedData, existingIDs, overwriteDataEntries);
        // add imported data to existing data
        this.data.addDataItems(importedData.getData());
        return importedData;
    }
    
    private void loadPreferences() throws Exception {
        File prefsFile = new File(Constants.CONFIG_DIR, Constants.PREFERENCES_FILE);
        if (prefsFile.exists()) {
            byte[] data = FSUtils.readFile(prefsFile);
            if (data.length != 0) {
                prefs = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(data));
            }
        }
    }
    
    private void loadConfig() throws Exception {
        File configFile = new File(Constants.CONFIG_DIR, Constants.GLOBAL_CONFIG_FILE);
        if (configFile.exists()) {
            byte[] data = FSUtils.readFile(configFile);
            ByteArrayInputStream bais = new ByteArrayInputStream(data);
            config.load(bais);
            bais.close();
        }
    }

    private DataCategory parseMetadata(Document metadata, Map<String, DataEntry> identifiedData, Collection<UUID> existingIDs, boolean overwrite) throws Exception {
        DataCategory data = new DataCategory();
        if (metadata == null) {
            metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        } else {
            buildData(data, metadata.getFirstChild(), identifiedData, existingIDs, overwrite);
            data.setPlacement(Integer.valueOf(metadata.getFirstChild().getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT).getNodeValue()));
            Node activeIdxNode = metadata.getFirstChild().getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX);
            if (activeIdxNode != null) {
                data.setActiveIndex(Integer.valueOf(activeIdxNode.getNodeValue()));
            }
        }
        return data;
    }
    
    private void buildData(DataCategory data, Node node, Map<String, DataEntry> identifiedData, Collection<UUID> existingIDs, boolean overwrite) throws Exception {
        if (node.getNodeName().equals(Constants.XML_ELEMENT_ROOT_CONTAINER)) {
            NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                buildData(data, n, identifiedData, existingIDs, overwrite);
            }
        } else if (node.getNodeName().equals(Constants.XML_ELEMENT_CATEGORY)) {
            NamedNodeMap attributes = node.getAttributes();
            Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
            UUID id = UUID.fromString(attID.getNodeValue());
            if (overwrite || existingIDs == null || !existingIDs.contains(id)) {
                DataCategory dc = new DataCategory();
                dc.setId(id);
                Node attCaption = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION);
                String caption = attCaption.getNodeValue();
                caption = URLDecoder.decode(caption, Constants.UNICODE_ENCODING);
                dc.setCaption(caption);
                Node attIcon = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ICON);
                if (attIcon != null) {
                	String iconID = attIcon.getNodeValue();
                    byte[] imageData = icons.get(UUID.fromString(iconID));
                	if (imageData != null) {
                    	ImageIcon icon = new ImageIcon(imageData, iconID);
                    	dc.setIcon(icon);
                	}
                }
                Node attPlacement = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT);
                Integer placement = Integer.valueOf(attPlacement.getNodeValue());
                dc.setPlacement(placement);
                Node attActiveIdx = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX);
                if (attActiveIdx != null) {
                    Integer activeIdx = Integer.valueOf(attActiveIdx.getNodeValue());
                    dc.setActiveIndex(activeIdx);
                }
                data.addDataItem(dc);
                NodeList nodes = node.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node n = nodes.item(i);
                    buildData(dc, n, identifiedData, existingIDs, overwrite);
                }
            }
        } else if (node.getNodeName().equals(Constants.XML_ELEMENT_ENTRY)) {
            NamedNodeMap attributes = node.getAttributes();
            Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
            UUID id = UUID.fromString(attID.getNodeValue());
            if (overwrite || existingIDs == null || !existingIDs.contains(id)) {
                DataEntry dataEntry = identifiedData.get(id.toString());
                if (dataEntry != null) {
                    dataEntry.setId(id);
                    Node attCaption = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION);
                    String caption = attCaption.getNodeValue();
                    caption = URLDecoder.decode(caption, Constants.UNICODE_ENCODING);
                    dataEntry.setCaption(caption);
                    Node attIcon = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ICON);
                    if (attIcon != null) {
                        String iconID = attIcon.getNodeValue();
                        byte[] imageData = icons.get(UUID.fromString(iconID));
                        if (imageData != null) {
                            ImageIcon icon = new ImageIcon(imageData, iconID);
                            dataEntry.setIcon(icon);
                        }
                    }
                    Node attType = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_TYPE);
                    String type = attType.getNodeValue();
                    dataEntry.setType(type);
                    loadDataEntrySettings(dataEntry);
                    data.addDataItem(dataEntry);
                }
            }
        }
    }
    
    public FileInfo exportData(
            DataCategory data,
            boolean exportPreferences,
            boolean exportGlobalConfig,
            boolean exportDataEntryConfigs,
            boolean exportOnlyRelatedDataEntryConfigs,
            boolean exportToolsData, 
            boolean exportIcons,
            boolean exportOnlyRelatedIcons,
            boolean exportAppCore,
            boolean exportAddOnsAndLibs,
            boolean exportAddOnConfigs,
            boolean exportImportExportConfigs,
            String password) throws Exception {
        Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, password);
        File exportDir = new File(Constants.TMP_DIR, "exportDir");
        FSUtils.delete(exportDir);
        exportDir.mkdirs();
        // metadata file and data entries
        Document metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = metadata.createElement(Constants.XML_ELEMENT_ROOT_CONTAINER);
        rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT, data.getPlacement().toString());
        if (data.getActiveIndex() != null) {
            rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX, data.getActiveIndex().toString());
        }
        File dataDir = new File(exportDir, Constants.DATA_DIR.getName());
        dataDir.mkdir();
        Collection<UUID> ids = buildNode(metadata, rootNode, data, false);
        if (!ids.isEmpty()) {
            metadata.appendChild(rootNode);
            File metadataFile = new File(dataDir, Constants.METADATA_FILE_NAME);
            storeMetadata(metadata, metadataFile, cipher);
            for (File dataFile : Constants.DATA_DIR.listFiles(FILE_FILTER_DATA)) {
                UUID id = UUID.fromString(dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
                if (ids.contains(id)) {
                    reencryptFile(dataFile, dataDir, cipher);
                }
            }
            // attachments
            Map<UUID, Collection<Attachment>> atts = new HashMap<UUID, Collection<Attachment>>();
            for (UUID id : ids) {
                atts.put(id, getAttachments(id));
            }
            if (!atts.isEmpty()) {
                File attsDir = new File(exportDir, Constants.ATTACHMENTS_DIR.getName());
                attsDir.mkdir();
                for (Entry<UUID, Collection<Attachment>> entryAtts : atts.entrySet()) {
                    for (Attachment att : entryAtts.getValue()) {
                        addAttachment(entryAtts.getKey(), att, attsDir, cipher);
                    }
                }
            }
        }
        File configDir = new File(exportDir, Constants.CONFIG_DIR.getName());
        configDir.mkdir();
        // data entry configs
        if (exportDataEntryConfigs) {
            for (File dataConfig : Constants.CONFIG_DIR.listFiles(FILE_FILTER_DATA_ENTRY_CONFIG)) {
                UUID id = UUID.fromString(dataConfig.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
                if (!exportOnlyRelatedDataEntryConfigs || ids.contains(id)) {
                    reencryptFile(dataConfig, configDir, cipher);
                }
            }
        }
        // preferences file
        if (exportPreferences) {
            FSUtils.writeFile(new File(configDir, Constants.PREFERENCES_FILE), Preferences.getInstance().serialize());
        }
        // global config file
        if (exportGlobalConfig) {
            config.store(new FileOutputStream(new File(configDir, Constants.GLOBAL_CONFIG_FILE)), null);
        }
        // icons
        if (exportIcons) {
            if (!icons.isEmpty()) {
                File iconsDir = new File(exportDir, Constants.ICONS_DIR.getName());
                iconsDir.mkdir();
                StringBuffer iconsList = new StringBuffer();
                for (Entry<UUID, byte[]> icon : icons.entrySet()) {
                    if (!exportOnlyRelatedIcons || ids.contains(icon.getKey())) {
                        File iconFile = new File(iconsDir, icon.getKey().toString() + Constants.ICON_FILE_SUFFIX);
                        FSUtils.writeFile(iconFile, icon.getValue());
                        iconsList.append(icon.getKey().toString());
                        iconsList.append(Constants.NEW_LINE);
                    }
                }
                File iconsListFile = new File(configDir, Constants.ICONS_CONFIG_FILE);
                FSUtils.writeFile(iconsListFile, iconsList.toString().getBytes());
                for (File localIconSetInfoOrRegFile : Constants.ICONS_DIR.listFiles(FILE_FILTER_ICONSET_INFO_OR_REG)) {
                    File iconSetInfoOrRegFile = new File(iconsDir, localIconSetInfoOrRegFile.getName());
                    FSUtils.duplicateFile(localIconSetInfoOrRegFile, iconSetInfoOrRegFile);
                }
            }
        }
        // tools data files
        if (exportToolsData) {
            for (Entry<String, byte[]> toolEntry : toolsData.entrySet()) {
                if (getDataExportExtensionsList().contains(toolEntry.getKey()) && toolEntry.getValue() != null) {
                    String tool = toolEntry.getKey().replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                    File toolDataFile = new File(dataDir, tool + Constants.TOOL_DATA_FILE_SUFFIX);
                    FSUtils.writeFile(toolDataFile, useCipher(cipher, toolEntry.getValue()));
                }
            }
        }
        // addons/libs
        if (exportAddOnsAndLibs) {
            File addonsDir = new File(exportDir, Constants.ADDONS_DIR.getName());
            FSUtils.duplicateFile(Constants.ADDONS_DIR, addonsDir);
            File libsDir = new File(exportDir, Constants.LIBS_DIR.getName());
            FSUtils.duplicateFile(Constants.LIBS_DIR, libsDir);
        }
        // app core
        if (exportAppCore) {
            File localAppCoreFile = new File(Constants.ROOT_DIR, Constants.APP_CORE_FILE_NAME);
            File appCoreFile = new File(exportDir, Constants.APP_CORE_FILE_NAME);
            FSUtils.duplicateFile(localAppCoreFile, appCoreFile);
        }
        // addon configs
        if (exportAddOnConfigs) {
            for (File addOnConfig : Constants.CONFIG_DIR.listFiles(FILE_FILTER_ADDON_CONFIG)) {
                String addOnName = addOnConfig.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                addOnName = Constants.EXTENSION_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                + addOnName  + Constants.PACKAGE_PATH_SEPARATOR + addOnName ;
                if (getConfigExportExtensionsList().contains(addOnName)) {
                    reencryptFile(addOnConfig, configDir, cipher);
                }
            }
        }
        // import/export configs
        if (exportImportExportConfigs) {
            for (File localConfig : Constants.CONFIG_DIR.listFiles(FILE_FILTER_IMPORT_EXPORT_CONFIG)) {
                reencryptFile(localConfig, configDir, cipher);
            }
        }
        if (configDir.listFiles().length == 0) {
            configDir.delete();
        }
        if (dataDir.listFiles().length == 0) {
            dataDir.delete();
        }
        File file = new File(Constants.TMP_DIR, "data.zip");
        String checkSum = ArchUtils.compress(
                exportDir, 
                file, 
                MessageDigest.getInstance(Constants.DIGEST_ALGORITHM), 
                Collections.singleton(Constants.GLOBAL_CONFIG_FILE));
        return new FileInfo(file, checkSum);
    }
    
    public void store() throws Exception {
        // metadata file and data entries
        metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = metadata.createElement(Constants.XML_ELEMENT_ROOT_CONTAINER);
        rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT, data.getPlacement().toString());
        if (data.getActiveIndex() != null) {
            rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX, data.getActiveIndex().toString());
        }
        Collection<UUID> ids = buildNode(metadata, rootNode, data, true);
        cleanUpOrphanedStuff(ids);
        metadata.appendChild(rootNode);
        storeMetadata(metadata, metadataFile, CIPHER_ENCRYPT);
        // tools data files
        for (Entry<String, byte[]> toolEntry : toolsData.entrySet()) {
            if (toolEntry.getValue() != null) {
                String tool = toolEntry.getKey().replaceFirst(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
                File toolDataFile = new File(Constants.DATA_DIR, tool + Constants.TOOL_DATA_FILE_SUFFIX);
                byte[] encryptedData = encrypt(toolEntry.getValue());
                FSUtils.writeFile(toolDataFile, encryptedData);
            }
        }
        // icons
        storeIconsList();
        // global config file
        config.store(new FileOutputStream(new File(Constants.CONFIG_DIR, Constants.GLOBAL_CONFIG_FILE)), null);
        // preferences file
        storePreferences();
    }
    
    public void storePreferences() throws Exception {
        FSUtils.writeFile(new File(Constants.CONFIG_DIR, Constants.PREFERENCES_FILE), Preferences.getInstance().serialize());
    }
    
    private void loadTransferConfigurations() throws Exception {
        importConfigs = null;
        importConfigIDs = null;
        getTransferConfigurations(Constants.TRANSFER_TYPE.IMPORT);
        exportConfigs = null;
        exportConfigIDs = null;
        getTransferConfigurations(Constants.TRANSFER_TYPE.EXPORT);
    }
    
    public Map<String, Properties> getTransferConfigurations(Constants.TRANSFER_TYPE transferType) throws Exception {
        Map<String, Properties> configs;
        Map<String, String> configIDs;
        FilenameFilter ff;
        if (transferType == Constants.TRANSFER_TYPE.IMPORT) {
            if (importConfigs == null) {
                importConfigs = new HashMap<String, Properties>();
                importConfigIDs = new HashMap<String, String>();
            }
            configs = importConfigs;
            configIDs = importConfigIDs;
            ff = FILE_FILTER_IMPORT_CONFIG;
        } else {
            if (exportConfigs == null) {
                exportConfigs = new HashMap<String, Properties>();
                exportConfigIDs = new HashMap<String, String>();
            }
            configs = exportConfigs;
            configIDs = exportConfigIDs;
            ff = FILE_FILTER_EXPORT_CONFIG;
        }
        File[] configFiles = Constants.CONFIG_DIR.listFiles(ff);
        for (File file : configFiles) {
            Properties props = new Properties();
            byte[] encryptedData = FSUtils.readFile(file);
            byte[] decryptedData = decrypt(encryptedData);
            ByteArrayInputStream bais = new ByteArrayInputStream(decryptedData);
            props.load(bais);
            bais.close();
            String name = props.getProperty(Constants.OPTION_CONFIG_NAME);
            String id = file.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
            configs.put(name, props);
            configIDs.put(name, id);
        }
        return configs;
    }
    
    public byte[] getTransferOptions(String configName, Constants.TRANSFER_TYPE transferType) throws Exception {
        Map<String, String> configIDs = transferType == Constants.TRANSFER_TYPE.IMPORT ? importConfigIDs : exportConfigIDs; 
        String fileName = configIDs.get(configName) + Constants.TRANSFER_OPTIONS_CONFIG_FILE_SUFFIX;
        File transferOptionsFile = new File(Constants.CONFIG_DIR, fileName);
        byte[] encryptedData = FSUtils.readFile(transferOptionsFile);
        byte[] decryptedData = decrypt(encryptedData);
        return decryptedData;
    }
    
    public void storeTransferConfigurationAndOptions(String name, Properties config, byte[] transferOptions, Constants.TRANSFER_TYPE transferType) throws Exception {
        Map<String, Properties> configs = transferType == Constants.TRANSFER_TYPE.IMPORT ? importConfigs : exportConfigs;;
        Map<String, String> configIDs = transferType == Constants.TRANSFER_TYPE.IMPORT ? importConfigIDs : exportConfigIDs;;
        String configSuffix = transferType == Constants.TRANSFER_TYPE.IMPORT ? Constants.IMPORT_CONFIG_FILE_SUFFIX : Constants.EXPORT_CONFIG_FILE_SUFFIX;
        String fileName = configIDs.get(name);
        if (fileName == null) {
            UUID id = UUID.randomUUID();
            configIDs.put(name, id.toString());
            fileName = id.toString();
        }
        configs.put(name, config);
        String transferOptionsFileName = fileName + Constants.TRANSFER_OPTIONS_CONFIG_FILE_SUFFIX;
        fileName += configSuffix;
        File configFile = new File(Constants.CONFIG_DIR, fileName);
        File transferOptionsFile = new File(Constants.CONFIG_DIR, transferOptionsFileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        config.store(baos, null);
        byte[] encryptedData = encrypt(baos.toByteArray());
        FSUtils.writeFile(configFile, encryptedData);
        encryptedData = encrypt(transferOptions);
        FSUtils.writeFile(transferOptionsFile, encryptedData);
    }
    
    public void removeTransferConfiguration(String name, Constants.TRANSFER_TYPE transferType) throws Exception {
        Map<String, Properties> configs = transferType == Constants.TRANSFER_TYPE.IMPORT ? importConfigs : exportConfigs;;
        Map<String, String> configIDs = transferType == Constants.TRANSFER_TYPE.IMPORT ? importConfigIDs : exportConfigIDs;;
        String configSuffix = transferType == Constants.TRANSFER_TYPE.IMPORT ? Constants.IMPORT_CONFIG_FILE_SUFFIX : Constants.EXPORT_CONFIG_FILE_SUFFIX;
        String fileName = configIDs.get(name);
        String transferOptionsFileName = fileName + Constants.TRANSFER_OPTIONS_CONFIG_FILE_SUFFIX;
        fileName += configSuffix;
        File configFile = new File(Constants.CONFIG_DIR, fileName);
        File transferOptionsFile = new File(Constants.CONFIG_DIR, transferOptionsFileName);
        configFile.delete();
        transferOptionsFile.delete();
        configs.remove(name);
        configIDs.remove(name);
    }
    
    public void renameTransferConfiguration(String oldName, String newName, Constants.TRANSFER_TYPE transferType) throws Exception {
        Map<String, Properties> configs = transferType == Constants.TRANSFER_TYPE.IMPORT ? importConfigs : exportConfigs;;
        Map<String, String> configIDs = transferType == Constants.TRANSFER_TYPE.IMPORT ? importConfigIDs : exportConfigIDs;;
        Properties config = configs.get(oldName);
        config.setProperty(Constants.OPTION_CONFIG_NAME, newName);
        File oldTransferOptionsFile = new File(Constants.CONFIG_DIR, configIDs.get(oldName) + Constants.TRANSFER_OPTIONS_CONFIG_FILE_SUFFIX);
        byte[] transferOptions = FSUtils.readFile(oldTransferOptionsFile);
        storeTransferConfigurationAndOptions(newName, config, transferOptions, transferType);
        oldTransferOptionsFile.delete();
        removeTransferConfiguration(oldName, transferType);
    }
    
    private void storeMetadata(Document metadata, File file, Cipher cipher) throws Exception {
        OutputFormat of = new OutputFormat();
        StringWriter sw = new StringWriter();
        new XMLSerializer(sw, of).serialize(metadata);
        byte[] encryptedData = useCipher(cipher, sw.getBuffer().toString().getBytes());
        FSUtils.writeFile(file, encryptedData);
    }
    
    private Collection<UUID> buildNode(Document metadata, Node node, DataCategory data, boolean writeDataMode) throws Exception {
        Collection<UUID> ids = new ArrayList<UUID>();
        for (Recognizable item : data.getData()) {
            if (item.getIcon() != null) {
                String idStr = ((ImageIcon) item.getIcon()).getDescription();
                if (!Validator.isNullOrBlank(idStr)) {
                    ids.add(UUID.fromString(idStr));
                }
            }
            if (item instanceof DataEntry) {
                DataEntry de = (DataEntry) item;
                storeDataEntrySettings(de);
                Element entryNode = metadata.createElement(Constants.XML_ELEMENT_ENTRY);
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ID, de.getId().toString());
                String encodedCaption = URLEncoder.encode(de.getCaption(), Constants.UNICODE_ENCODING);
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION, encodedCaption);
                if (de.getIcon() != null) {
                	String iconId = ((ImageIcon)de.getIcon()).getDescription();
                	if (!Validator.isNullOrBlank(iconId)) {
                        entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ICON, iconId);
                	}
                }
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_TYPE, de.getType());
                node.appendChild(entryNode);
                ids.add(de.getId());
                if (writeDataMode) {
                    // check if data of the entry have changed;
                    // if yes - write changed data to file, otherwise - skip data writing
                    DataEntry oldDE = identifiedData.get(de.getId().toString());
                    byte[] entryData = de.getData();
                    if (entryData != null && (oldDE == null || !Arrays.equals(entryData, oldDE.getData()))) {
                        byte[] encryptedData = encrypt(entryData);
                        File deFile = new File(Constants.DATA_DIR, de.getId().toString() + Constants.DATA_FILE_SUFFIX);
                        FSUtils.writeFile(deFile, encryptedData);
                        identifiedData.put(de.getId().toString(), de);
                    }
                }
            } else if (item instanceof DataCategory) {
                DataCategory dc = (DataCategory) item;
                Element catNode = metadata.createElement(Constants.XML_ELEMENT_CATEGORY);
                catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ID, dc.getId().toString());
                String encodedCaption = URLEncoder.encode(dc.getCaption(), Constants.UNICODE_ENCODING);
                catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION, encodedCaption);
                if (dc.getIcon() != null) {
                	String iconId = ((ImageIcon)dc.getIcon()).getDescription();
                	if (!Validator.isNullOrBlank(iconId)) {
                        catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ICON, iconId);
                	}
                }
                catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT, dc.getPlacement().toString());
                if (dc.getActiveIndex() != null) {
                    catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX, dc.getActiveIndex().toString());
                }
                ids.addAll(buildNode(metadata, catNode, dc, writeDataMode));
                node.appendChild(catNode);
            }
        }
        return ids;
    }
    
    public URL getRepositoryBaseURL() throws Exception {
        Properties p = new Properties();
        File reposConfigFile = new File(Constants.CONFIG_DIR, Constants.REPOSITORY_CONFIG_FILE);
        String urlStr = null;
        if (reposConfigFile.exists()) {
            FileInputStream fis = new FileInputStream(reposConfigFile);
            p.load(fis);
            fis.close();
            urlStr = p.getProperty(Constants.MAIN_REPOSITORY_KEY);
        }
        if (Validator.isNullOrBlank(urlStr)) {
            urlStr = Constants.MAIN_REPOSITORY_VALUE;
            p.setProperty(Constants.MAIN_REPOSITORY_KEY, urlStr);
            p.store(new FileOutputStream(reposConfigFile), null);
        }
        return new URL(urlStr);
    }
    
    public void installAppCoreUpdate(File appCoreUpdateFile) throws Throwable {
        FSUtils.duplicateFile(appCoreUpdateFile, new File(Constants.ROOT_DIR, Constants.UPDATE_FILE_PREFIX + Constants.APP_CORE_FILE_NAME));
    }
    
    private void loadDataEntrySettings(DataEntry dataEntry) throws Exception {
        if (dataEntry != null) {
            byte[] settings = null;
            File dataEntryConfigFile = new File(Constants.CONFIG_DIR, dataEntry.getId().toString() + Constants.DATA_ENTRY_CONFIG_FILE_SUFFIX);
            if (dataEntryConfigFile.exists()) {
                settings = decrypt(FSUtils.readFile(dataEntryConfigFile));
            }
            if (settings == null) {
                settings = getAddOnSettings(dataEntry.getType(), PackType.EXTENSION);
            }
            dataEntry.setSettings(settings);
        }
    }

    private void storeDataEntrySettings(DataEntry dataEntry) throws Exception {
        if (dataEntry != null && dataEntry.getSettings() != null) {
            byte[] defSettings = getAddOnSettings(dataEntry.getType(), PackType.EXTENSION);
            File deConfigFile = new File(Constants.CONFIG_DIR, dataEntry.getId().toString() + Constants.DATA_ENTRY_CONFIG_FILE_SUFFIX);
            if (!Arrays.equals(defSettings, dataEntry.getSettings())) {
                FSUtils.writeFile(deConfigFile, encrypt(dataEntry.getSettings()));
            } else {
                FSUtils.delete(deConfigFile);
            }
        }
    }
    
    public Map<AddOnInfo, String> getNewAddOns(PackType addOnType) {
        return newAddOns.get(addOnType);
    }
    
    private void storeAddOnInfo(AddOnInfo addOnInfo, PackType addOnType) throws Throwable {
        String suffix = null;
        switch (addOnType) {
        case EXTENSION:
            suffix = Constants.ADDON_EXTENSION_INFO_FILE_SUFFIX;
            break;
        case SKIN:
            suffix = Constants.ADDON_SKIN_INFO_FILE_SUFFIX;
            break;
        case ICON_SET:
            suffix = Constants.ADDON_ICONSET_INFO_FILE_SUFFIX;
            break;
        case LIBRARY:
            suffix = Constants.ADDON_LIB_INFO_FILE_SUFFIX;
            break;
        }
        File addOnInfoFile = new File(Constants.CONFIG_DIR, addOnInfo.getName() + suffix);
        if (!addOnInfoFile.exists()) {
            addOnInfoFile.createNewFile();
        }
        Properties info = new Properties();
        info.setProperty(Constants.ATTRIBUTE_ADD_ON_VERSION, addOnInfo.getVersion());
        info.setProperty(Constants.ATTRIBUTE_ADD_ON_AUTHOR, addOnInfo.getAuthor());
        info.setProperty(Constants.ATTRIBUTE_ADD_ON_DESCRIPTION, addOnInfo.getDescription());
        StringBuffer deps = new StringBuffer();
        if (addOnInfo.getDependencies() != null && !addOnInfo.getDependencies().isEmpty()) {
            Iterator<Dependency> it = addOnInfo.getDependencies().iterator();
            while (it.hasNext()) {
                Dependency dep = it.next();
                deps.append(dep.getType().value());
                deps.append(Constants.VALUES_SEPARATOR);
                deps.append(dep.getName());
                if (!Validator.isNullOrBlank(dep.getVersion())) {
                    deps.append(Constants.VALUES_SEPARATOR);
                    deps.append(dep.getVersion());
                }
                if (it.hasNext()) {
                    deps.append(Constants.PROPERTY_VALUES_SEPARATOR);
                }
            }
        }
        if (!Validator.isNullOrBlank(deps)) {
            info.setProperty(Constants.ATTRIBUTE_ADD_ON_DEPENDENCIES, deps.toString());
        }
        FSUtils.writeFile(addOnInfoFile, PropertiesUtils.serializeProperties(info));
    }
    
    private AddOnInfo readAddOnInfo(File addOnInfoFile) throws Throwable {
        Properties info = new Properties();
        InputStream is = new FileInputStream(addOnInfoFile);
        info.load(is);
        is.close();
        AddOnInfo aoi = new AddOnInfo();
        aoi.setName(addOnInfoFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
        aoi.setVersion(info.getProperty(Constants.ATTRIBUTE_ADD_ON_VERSION));
        aoi.setAuthor(info.getProperty(Constants.ATTRIBUTE_ADD_ON_AUTHOR));
        aoi.setDescription(info.getProperty(Constants.ATTRIBUTE_ADD_ON_DESCRIPTION));
        String deps = info.getProperty(Constants.ATTRIBUTE_ADD_ON_DEPENDENCIES);
        if (!Validator.isNullOrBlank(deps)) {
             for (String dep : deps.split(Constants.PROPERTY_VALUES_SEPARATOR)) {
                 String[] depInfo = dep.trim().split(Constants.VALUES_SEPARATOR);
                 if (depInfo.length != 2 && depInfo.length != 3) {
                     throw new Exception(
                             "Invalid Add-On-Info: " + Constants.NEW_LINE +
                             Constants.ATTRIBUTE_ADD_ON_DEPENDENCIES 
                             + " attribute has invalid value!" + Constants.NEW_LINE
                             + "[At least dependency type and name should be specified (version is optional)]");
                 }
                 Dependency d = new Dependency();
                 d.setType(PackType.fromValue(depInfo[0]));
                 d.setName(depInfo[1]);
                 if (depInfo.length == 3) { 
                     d.setVersion(depInfo[2]);
                 }
                 aoi.addDependency(d);
             }
            
        }
        return aoi;
    }
    
    public Boolean isAddOnInstalledAndUpToDate(Pack pack) throws Throwable {
        if (pack.getType() == PackType.APP_CORE) {
            if (VersionComparator.getInstance().compare(getAppVersion(), pack.getVersion()) >= 0) {
                return true;
            } else {
                return false;
            }
        } else {
            for (AddOnInfo addOn : getAddOns(pack.getType())) {
                if (addOn.getName().equals(pack.getName())) {
                    if (VersionComparator.getInstance().compare(addOn.getVersion(), pack.getVersion()) >= 0) {
                        return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return null;
    }
    
    public String getAppVersion() {
        return appVersion;
    }
    
    private String readAppVersion() throws Exception {
        File appCoreFile = new File(Constants.ROOT_DIR, Constants.APP_CORE_FILE_NAME);
        JarInputStream in = new JarInputStream(new FileInputStream(appCoreFile));
        Manifest manifest = in.getManifest();
        String appVersion = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_APP_VERSION);
        if (Validator.isNullOrBlank(appVersion)) {
            throw new Exception(
                    "Invalid Application Core JAR File:" + Constants.NEW_LINE +
                    Constants.ATTRIBUTE_APP_VERSION
                    + " attribute in MANIFEST.MF file is missing/empty!");
        }
        return appVersion;
    }
    
    public Collection<AddOnInfo> getAddOns() throws Throwable {
        return getAddOns(null);
    }
    
    public Collection<AddOnInfo> getAddOns(PackType addOnType) throws Throwable {
        Collection<AddOnInfo> addOns = new HashSet<AddOnInfo>();
        FilenameFilter ff = FILE_FILTER_ADDON_INFO;
        if (addOnType != null) {
            switch (addOnType) {
            case EXTENSION:
                ff = FILE_FILTER_EXTENSION_INFO;
                break;
            case SKIN:
                ff = FILE_FILTER_SKIN_INFO;
                break;
            case ICON_SET:
                ff = FILE_FILTER_ICONSET_INFO;
                break;
            case LIBRARY:
                ff = FILE_FILTER_LIB_INFO;
                break;
            }
        }
        for (File addOnInfoFile : Constants.CONFIG_DIR.listFiles(ff)) {
            File addOnDir = null;
            File addOnFile = null;
            String suffix = null;
            if (addOnInfoFile.getName().endsWith(Constants.ADDON_EXTENSION_INFO_FILE_SUFFIX)) {
                addOnDir = Constants.ADDONS_DIR;
                suffix = Constants.EXTENSION_JAR_FILE_SUFFIX;
            } else if (addOnInfoFile.getName().endsWith(Constants.ADDON_SKIN_INFO_FILE_SUFFIX)) {
                addOnDir = Constants.ADDONS_DIR;
                suffix = Constants.SKIN_JAR_FILE_SUFFIX;
            } else if (addOnInfoFile.getName().endsWith(Constants.ADDON_LIB_INFO_FILE_SUFFIX)) {
                addOnDir = Constants.LIBS_DIR;
                suffix = Constants.LIB_JAR_FILE_SUFFIX;
            }
            if (addOnDir != null) {
                addOnFile = new File(addOnDir, addOnInfoFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR) + suffix);
            }
            if (addOnFile == null || addOnFile.exists()) {
                addOns.add(readAddOnInfo(addOnInfoFile));
            }
        }
        return addOns;
    }

    public byte[] getAddOnSettings(String addOnName, PackType addOnType) throws Exception {
        byte[] settings = null;
        if (!Validator.isNullOrBlank(addOnName)) {
            addOnName = addOnName.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            File addOnConfigFile = new File(
                    Constants.CONFIG_DIR, 
                    addOnName + (addOnType == PackType.EXTENSION ? Constants.EXTENSION_CONFIG_FILE_SUFFIX : Constants.SKIN_CONFIG_FILE_SUFFIX));
            if (addOnConfigFile.exists()) {
                settings = decrypt(FSUtils.readFile(addOnConfigFile));
            }
        }
        return settings;
    }

    public void storeAddOnSettings(String addOnName, PackType addOnType, byte[] settings) throws Exception {
        if (!Validator.isNullOrBlank(addOnName) && settings != null) {
            addOnName = addOnName.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            File addOnConfigFile = new File(
                    Constants.CONFIG_DIR, 
                    addOnName + (addOnType == PackType.EXTENSION ? Constants.EXTENSION_CONFIG_FILE_SUFFIX : Constants.SKIN_CONFIG_FILE_SUFFIX));
            FSUtils.writeFile(addOnConfigFile, encrypt(settings));
        }
    }
    
    private void extractAddOnInfo(JarInputStream in, String addOnName) throws Throwable {
        try {
            File destination = new File(Constants.ADDON_INFO_DIR, addOnName);
            if (destination.exists()) {
                FSUtils.delete(destination);
            }
            JarEntry je;
            while ((je = in.getNextJarEntry()) != null) {
                String name = je.getName();
                if (!name.equals(Constants.JAR_FILE_ADDON_INFO_DIR_PATH) && name.startsWith(Constants.JAR_FILE_ADDON_INFO_DIR_PATH)) {
                    if (name.endsWith(Constants.PATH_SEPARATOR)) {
                        name = name.substring(Constants.JAR_FILE_ADDON_INFO_DIR_PATH.length());
                        if (!Validator.isNullOrBlank(name)) {
                            File dir = new File(destination, name);
                            dir.mkdirs();
                        }
                    } else {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] buffer = new byte[1024];
                        int br;
                        while ((br = in.read(buffer)) > 0) {
                            baos.write(buffer, 0, br);
                        }
                        baos.close();
                        name = je.getName().substring(Constants.JAR_FILE_ADDON_INFO_DIR_PATH.length());
                        name = URLDecoder.decode(name, Constants.UNICODE_ENCODING);
                        File file = new File(destination, name);
                        File dir = file.getParentFile();
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        file.createNewFile();
                        FSUtils.writeFile(file, baos.toByteArray());
                    }
                }
            }
        } catch (Throwable t) {
            throw new Throwable("Failure while trying to extract add-on info!", t);
        }
    }
    
    public AddOnInfo getAddOnInfoAndDependencies(File addOnFile, PackType addOnType) throws Throwable {
        return getAddOnInfoAndDependencies(addOnFile, addOnType, false);
    }
    
    private AddOnInfo getAddOnInfoAndDependencies(File addOnFile, PackType addOnType, boolean extractAddOnInfo) throws Throwable {
        AddOnInfo addOnInfo = null;
        if (addOnFile != null && addOnFile.exists() && !addOnFile.isDirectory()) {
            String name = addOnFile.getName();
            if (name.matches(Constants.JAR_FILE_PATTERN)) {
                JarInputStream in = new JarInputStream(new FileInputStream(addOnFile));
                Manifest manifest = in.getManifest();
                if (manifest == null) {
                    throw new Exception(
                            "Invalid Add-On-Package:" + Constants.NEW_LINE +
                            "MANIFEST.MF file is missing!");
                }
                String addOnTypeStr = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_TYPE);
                if (Validator.isNullOrBlank(addOnTypeStr)) {
                    throw new Exception(
                            "Invalid Add-On-Package: " + Constants.NEW_LINE +
                            Constants.ATTRIBUTE_ADD_ON_TYPE 
                            + " attribute in MANIFEST.MF file is missing/empty!");
                }
                if (!addOnTypeStr.equals(addOnType.value())) {
                    throw new Exception(
                            "Invalid Add-On-Package type: " + Constants.NEW_LINE +
                            "(actual: '" + addOnTypeStr + "', expected: '" + addOnType.value() + "')!");
                }
                String addOnName = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_NAME);
                if (Validator.isNullOrBlank(addOnName)) {
                    throw new Exception(
                            "Invalid Add-On-Package: " + Constants.NEW_LINE +
                            Constants.ATTRIBUTE_ADD_ON_NAME 
                            + " attribute in MANIFEST.MF file is missing/empty!");
                }
                String addOnVersion = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_VERSION);
                if (Validator.isNullOrBlank(addOnVersion)) {
                    throw new Exception(
                            "Invalid Add-On-Package: " + Constants.NEW_LINE +
                            Constants.ATTRIBUTE_ADD_ON_VERSION 
                            + " attribute in MANIFEST.MF file is missing/empty!");
                }
                String addOnAuthor = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_AUTHOR);
                String addOnDescription = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_DESCRIPTION);
                addOnInfo = new AddOnInfo(addOnName, addOnVersion, addOnAuthor, addOnDescription);
                String addOnDependencies = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_DEPENDENCIES);
                if (!Validator.isNullOrBlank(addOnDependencies)) {
                    String[] deps = addOnDependencies.split(Constants.PROPERTY_VALUES_SEPARATOR);
                    for (String dep : deps) {
                        String[] depInfo = dep.trim().split(Constants.VALUES_SEPARATOR);
                        if (depInfo.length != 2 && depInfo.length != 3) {
                            throw new Exception(
                                    "Invalid Add-On-Package: " + Constants.NEW_LINE +
                                    Constants.ATTRIBUTE_ADD_ON_DEPENDENCIES 
                                    + " attribute in MANIFEST.MF file has invalid value!" + Constants.NEW_LINE
                                    + "[At least dependency type and name should be specified (version is optional)]");
                        }
                        Dependency d = new Dependency();
                        d.setType(PackType.fromValue(depInfo[0]));
                        d.setName(depInfo[1]);
                        if (depInfo.length == 3) { 
                            d.setVersion(depInfo[2]);
                        }
                        addOnInfo.addDependency(d);
                    }
                }
                if (extractAddOnInfo) {
                    extractAddOnInfo(in, addOnName);
                }
                in.close();
            }
        } else {
            throw new Exception("Invalid Add-On-Package!");
        }
        return addOnInfo;
    }
    
    public void installLibrary(File addOnFile, AddOnInfo libInfo) throws Throwable {
        installAddOn(addOnFile, PackType.LIBRARY, libInfo);
    }
    
    public AddOnInfo installAddOn(File addOnFile, PackType addOnType) throws Throwable {
        return installAddOn(addOnFile, addOnType, null);
    }
    
    private AddOnInfo installAddOn(File addOnFile, PackType addOnType, AddOnInfo addOnInfo) throws Throwable {
        if (addOnInfo == null) {
            addOnInfo = getAddOnInfoAndDependencies(addOnFile, addOnType, true);
        }
        boolean update = false;
        String status = Constants.ADDON_STATUS_INSTALLED;
        if (getAddOns(addOnType).contains(addOnInfo) || loadedAddOns.contains(addOnInfo)) {
            update = true;
            status = Constants.ADDON_STATUS_UPDATED;
        }
        File dir = null;
        String fileSuffix = null;
        switch (addOnType) {
        case EXTENSION:
            dir = Constants.ADDONS_DIR;
            fileSuffix = Constants.EXTENSION_JAR_FILE_SUFFIX;
            break;
        case SKIN:
            dir = Constants.ADDONS_DIR;
            fileSuffix = Constants.SKIN_JAR_FILE_SUFFIX;
            break;
        case LIBRARY:
            dir = Constants.LIBS_DIR;
            fileSuffix = Constants.LIB_JAR_FILE_SUFFIX;
            break;
        }
        File installedAddOnFile = new File(dir, addOnInfo.getName() + fileSuffix);
        if (update) {
            File updateAddOnFile = new File(dir, Constants.UPDATE_FILE_PREFIX + addOnInfo.getName() + fileSuffix);
            FSUtils.duplicateFile(addOnFile, updateAddOnFile);
        } else {
            FSUtils.duplicateFile(addOnFile, installedAddOnFile);
        }
        storeAddOnInfo(addOnInfo, addOnType);
        Map<AddOnInfo, String> addons = newAddOns.get(addOnType);
        if (addons == null) {
            addons = new LinkedHashMap<AddOnInfo, String>();
            newAddOns.put(addOnType, addons);
        }
        addons.put(addOnInfo, status);
        if (uninstallAddOnsList.contains(addOnInfo.getName() + fileSuffix)) {
            uninstallAddOnsList.remove(addOnInfo.getName() + fileSuffix);
            storeUninstallConfiguration();
        }
        return addOnInfo;
    }
    
    public void uninstallAddOn(String addOnName, PackType addOnType) throws Throwable {
        String fullName = addOnName;
        addOnName = addOnName.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
        String fileSuffix = null;
        String configFileSuffix = null;
        switch (addOnType) {
        case EXTENSION:
            fileSuffix = Constants.EXTENSION_JAR_FILE_SUFFIX;
            configFileSuffix = Constants.ADDON_EXTENSION_INFO_FILE_SUFFIX;
            break;
        case SKIN:
            fileSuffix = Constants.SKIN_JAR_FILE_SUFFIX;
            configFileSuffix = Constants.ADDON_SKIN_INFO_FILE_SUFFIX;
            break;
        case ICON_SET:
            configFileSuffix = Constants.ADDON_ICONSET_INFO_FILE_SUFFIX;
            break;
        case LIBRARY:
            fileSuffix = Constants.LIB_JAR_FILE_SUFFIX;
            configFileSuffix = Constants.ADDON_LIB_INFO_FILE_SUFFIX;
            break;
        }
        if (fileSuffix != null) {
            uninstallAddOnsList.add(addOnName + fileSuffix);
            storeUninstallConfiguration();
        }
        File addOnInfoFile = new File(Constants.CONFIG_DIR, addOnName + configFileSuffix);
        FSUtils.delete(addOnInfoFile);
        newAddOns.remove(fullName);
    }
    
    private Collection<File> unusedFiles;
    public boolean unusedAddOnDataAndConfigFilesFound() {
        if (unusedFiles == null) {
            unusedFiles = new ArrayList<File>();            
            Map<File, FilenameFilter> ffs = new HashMap<File, FilenameFilter>();
            ffs.put(Constants.DATA_DIR, FILE_FILTER_TOOL_DATA);
            ffs.put(Constants.CONFIG_DIR, FILE_FILTER_ADDON_CONFIG);
            for (Entry<File, FilenameFilter> ff : ffs.entrySet()) {
                for (File f : ff.getKey().listFiles(ff.getValue())) {
                    String name = f.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                    AddOnInfo info = new AddOnInfo();
                    info.setName(name);
                    if (!loadedAddOns.contains(info)) {
                        unusedFiles.add(f);
                    }
                }
            }
        }
        return !unusedFiles.isEmpty();
    }

    public void removeUnusedAddOnDataAndConfigFiles() {
        if (unusedAddOnDataAndConfigFilesFound()) {
            for (File f : unusedFiles) {
                FSUtils.delete(f);
            }
        }
    }
        
    private void storeUninstallConfiguration() throws Exception {
        StringBuffer removeList = new StringBuffer();
        Iterator<String> it = uninstallAddOnsList.iterator();
        while (it.hasNext()) {
            String cpEntry = it.next();
            removeList.append(cpEntry);
            if (it.hasNext()) {
                removeList.append(Constants.PROPERTY_VALUES_SEPARATOR);
            }
        }
        File removeAddOnsConfigFile = new File(Constants.CONFIG_DIR, Constants.UNINSTALL_CONFIG_FILE);
        if (!removeAddOnsConfigFile.exists()) {
            removeAddOnsConfigFile.createNewFile();
        }
        if (!Validator.isNullOrBlank(removeList)) {
            FSUtils.writeFile(removeAddOnsConfigFile, removeList.toString().getBytes());
        } else {
            removeAddOnsConfigFile.delete();
        }
    }
    
    public Collection<AddOnInfo> getIconSets() throws Throwable {
        Collection<AddOnInfo> iconSets = new LinkedList<AddOnInfo>();
        for (File iconSetInfoFile : Constants.CONFIG_DIR.listFiles(FILE_FILTER_ICONSET_INFO)) {
            String name = iconSetInfoFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
            Properties info = PropertiesUtils.deserializeProperties(FSUtils.readFile(iconSetInfoFile));
            AddOnInfo aoi = new AddOnInfo();
            aoi.setName(name);
            aoi.setVersion(info.getProperty(Constants.ATTRIBUTE_ADD_ON_VERSION));
            aoi.setAuthor(info.getProperty(Constants.ATTRIBUTE_ADD_ON_AUTHOR));
            aoi.setDescription(info.getProperty(Constants.ATTRIBUTE_ADD_ON_DESCRIPTION));
            iconSets.add(aoi);
        }
        return iconSets;
    }
    
    private void storeIconSet(AddOnInfo iconSetInfo, Collection<String> iconIds) throws Throwable {
        if (iconSetInfo.getName() != null && iconSetInfo.getVersion() != null) {
            storeAddOnInfo(iconSetInfo, PackType.ICON_SET);
            if (!iconIds.isEmpty()) {
                StringBuffer iconIdsList = new StringBuffer();
                File iconSetRegFile = new File(Constants.CONFIG_DIR, iconSetInfo.getName() + Constants.ICONSET_REGISTRY_FILE_SUFFIX);
                if (!iconSetRegFile.exists()) {
                    iconSetRegFile.createNewFile();
                } else {
                    iconIdsList.append(new String(FSUtils.readFile(iconSetRegFile)));
                    iconIdsList.append(Constants.NEW_LINE);
                }
                Iterator<String> it = iconIds.iterator();
                while (it.hasNext()) {
                    String iconId = it.next();
                    iconIdsList.append(iconId);
                    if (it.hasNext()) {
                        iconIdsList.append(Constants.NEW_LINE);
                    }
                }
                FSUtils.writeFile(iconSetRegFile, iconIdsList.toString().getBytes());
            }
        }
    }
    
    public Collection<String> removeIconSet(String iconSetName) throws Throwable {
        Collection<String> removedIds = new ArrayList<String>();
        File iconSetRegFile = new File(Constants.CONFIG_DIR, iconSetName + Constants.ICONSET_REGISTRY_FILE_SUFFIX);
        if (iconSetRegFile.exists()) {
            String[] iconIdsList = new String(FSUtils.readFile(iconSetRegFile)).split(Constants.NEW_LINE);
            for (String iconId : iconIdsList) {
                removeIcon(iconId);
                removedIds.add(iconId);
            }
            FSUtils.delete(iconSetRegFile);
        }
        File iconSetInfoFile = new File(Constants.CONFIG_DIR, iconSetName + Constants.ADDON_ICONSET_INFO_FILE_SUFFIX);
        FSUtils.delete(iconSetInfoFile);
        storeIconsList();
        return removedIds;
    }
    
    public Collection<ImageIcon> getIcons() {
        Collection<ImageIcon> icons = new LinkedHashSet<ImageIcon>();
        for (Entry<UUID, byte[]> iconEntry : this.icons.entrySet()) {
            ImageIcon icon = new ImageIcon(iconEntry.getValue(), iconEntry.getKey().toString());
            icons.add(icon);
        }
        return icons;
    }
    
    public Collection<ImageIcon> addIcons(File file) throws Throwable {
        Map<ImageIcon, String> icons = new LinkedHashMap<ImageIcon, String>();
        if (file != null && file.exists() && !file.isDirectory()) {
            try {
                if (file.getName().matches(Constants.JAR_FILE_PATTERN)) {
                    JarInputStream in = new JarInputStream(new FileInputStream(file));
                    AddOnInfo aoi = null;
                    Manifest manifest = in.getManifest();
                    if (manifest != null) {
                        String iconSetName = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_NAME);
                        String iconSetVersion = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_VERSION);
                        String iconSetAuthor = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_AUTHOR);
                        String iconSetDescription = manifest.getMainAttributes().getValue(Constants.ATTRIBUTE_ADD_ON_DESCRIPTION);
                        aoi = new AddOnInfo(iconSetName, iconSetVersion, iconSetAuthor, iconSetDescription);
                    }
                    File destination = new File(Constants.ADDON_INFO_DIR, aoi.getName());
                    if (destination.exists()) {
                        FSUtils.delete(destination);
                    }
                    JarEntry entry;                     
                    while ((entry = in.getNextJarEntry()) != null) {
                        String name = entry.getName();
                        if (!name.equals(Constants.JAR_FILE_ADDON_INFO_DIR_PATH) && name.startsWith(Constants.JAR_FILE_ADDON_INFO_DIR_PATH)) {
                            if (name.endsWith(Constants.PATH_SEPARATOR)) {
                                name = name.substring(Constants.JAR_FILE_ADDON_INFO_DIR_PATH.length());
                                if (!Validator.isNullOrBlank(name)) {
                                    File dir = new File(destination, name);
                                    dir.mkdirs();
                                }
                            } else {
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                byte[] buffer = new byte[1024];
                                int br;
                                while ((br = in.read(buffer)) > 0) {
                                    baos.write(buffer, 0, br);
                                }
                                baos.close();
                                name = entry.getName().substring(Constants.JAR_FILE_ADDON_INFO_DIR_PATH.length());
                                name = URLDecoder.decode(name, Constants.UNICODE_ENCODING);
                                File f = new File(destination, name);
                                File dir = f.getParentFile();
                                if (!dir.exists()) {
                                    dir.mkdirs();
                                }
                                f.createNewFile();
                                FSUtils.writeFile(f, baos.toByteArray());
                            }
                        } else {
                            ByteArrayOutputStream out = new ByteArrayOutputStream();
                            byte[] buffer = new byte[1024];
                            int br;
                            while ((br = in.read(buffer)) > 0) {
                                out.write(buffer, 0, br);
                            }
                            out.close();
                            byte[] bytes = out.toByteArray();
                            ImageIcon icon = addIcon(entry.getName(), new ByteArrayInputStream(bytes));
                            if (icon != null) {
                                icons.put(icon, icon.getDescription());
                            }
                        }
                    }
                    in.close();
                    if (aoi != null) {
                        storeIconSet(aoi, icons.values());
                    }
                } else {
                    ImageIcon icon = addIcon(file.getName(), new FileInputStream(file));
                    if (icon != null) {
                        icons.put(icon, icon.getDescription());
                    }
                }
            } finally {
                storeIconsList();
            }
        } else {
            throw new Exception("Invalid icon(set) file/package!");
        }
        return icons.keySet();
    }
    
    private void storeIconsList() throws IOException {
        StringBuffer iconsList = new StringBuffer();
        for (Entry<UUID, byte[]> icon : icons.entrySet()) {
            File iconFile = new File(Constants.ICONS_DIR, icon.getKey().toString() + Constants.ICON_FILE_SUFFIX);
            FSUtils.writeFile(iconFile, icon.getValue());
            iconsList.append(icon.getKey().toString());
            iconsList.append(Constants.NEW_LINE);
        }
        File iconsListFile = new File(Constants.CONFIG_DIR, Constants.ICONS_CONFIG_FILE);
        if (!Validator.isNullOrBlank(iconsList)) {
            FSUtils.writeFile(iconsListFile, iconsList.toString().getBytes());
        } else {
            FSUtils.delete(iconsListFile);
        }
    }
    
    private ImageIcon addIcon(String idStr, InputStream is) throws IOException {
        ImageIcon icon = null;
        BufferedImage image = ImageIO.read(is);
        if (image != null) {
            icon = new ImageIcon(image);
            if (icon != null) {
                UUID id = null;
                try {
                    // try to get id from entry name
                    id = UUID.fromString(idStr);
                } catch (Exception ex) {
                    // ignore, if id can't be generated from entry name, it will be autogenerated
                }
                if (id == null) {
                    id = UUID.randomUUID();
                }
                icon.setDescription(id.toString());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, Constants.ICON_FORMAT, baos);
                byte[] iconBytes = baos.toByteArray();
                File iconFile = new File(Constants.ICONS_DIR, id.toString() + Constants.ICON_FILE_SUFFIX);
                FSUtils.writeFile(iconFile, iconBytes);
                this.icons.put(id, iconBytes);
            }
        }
        return icon;
    }

    public void removeIcons(Collection<String> ids) throws IOException {
        for (String id : ids) {
            removeIcon(id);
        }
        storeIconsList();
    }

    private void removeIcon(String id) {
        File iconFile = new File(Constants.ICONS_DIR, id + Constants.ICON_FILE_SUFFIX);
        FSUtils.delete(iconFile);
        icons.remove(UUID.fromString(id));
    }
    
    private void reencryptFile(File file, File dir, Cipher cipher) throws Exception {
        byte[] reencryptedData = reencryptData(FSUtils.readFile(file), cipher);
        File newDataFile = new File(dir, file.getName());
        newDataFile.createNewFile();
        FSUtils.writeFile(newDataFile, reencryptedData);
    }
    
    private byte[] reencryptData(byte[] data, Cipher cipher) throws Exception {
        return useCipher(cipher, decrypt(data));
    }
    
    private static void reencryptData() throws Exception {
        reencryptFiles(Constants.DATA_DIR.listFiles(FILE_FILTER_DATA));
    }
    
    private static void reencryptAttachments() throws Exception {
        for (File entryAttsDir : Constants.ATTACHMENTS_DIR.listFiles()) {
            File[] entryAtts = entryAttsDir.listFiles();
            for (File entryAtt : entryAtts) {
                byte[] data = FSUtils.readFile(entryAtt);
                byte[] decryptedData = useCipher(CIPHER_DECRYPT, data);
                byte[] encryptedData = useCipher(CIPHER_ENCRYPT, decryptedData);
                FSUtils.writeFile(entryAtt, encryptedData);
            }
        }
    }
    
    private static void reencryptSettings() throws Exception {
        reencryptFiles(Constants.CONFIG_DIR.listFiles(FILE_FILTER_DATA_ENTRY_CONFIG));
        reencryptFiles(Constants.CONFIG_DIR.listFiles(FILE_FILTER_ADDON_CONFIG));
        reencryptFiles(Constants.CONFIG_DIR.listFiles(FILE_FILTER_IMPORT_EXPORT_CONFIG));
    }
    
    private static void reencryptFiles(File[] files) throws Exception {
        for (File f : files) {
            byte[] data = FSUtils.readFile(f);
            byte[] decryptedData = useCipher(CIPHER_DECRYPT, data);
            byte[] encryptedData = useCipher(CIPHER_ENCRYPT, decryptedData);
            FSUtils.writeFile(f, encryptedData);
        }
    }
    
    public File getDataEntryAttachmentsDir(UUID dataEntryID) throws Exception {
        File entryAttsDir = new File(Constants.ATTACHMENTS_DIR, dataEntryID.toString());
        if (!entryAttsDir.exists()) {
            entryAttsDir.mkdir();
        }
        return entryAttsDir;
    }
    
    public Collection<Attachment> getAttachments(UUID dataEntryID) throws Exception {
        Collection<Attachment> atts = new LinkedList<Attachment>();
        File entryAttsDir = new File(Constants.ATTACHMENTS_DIR, dataEntryID.toString());
        if (entryAttsDir.exists()) {
            File[] entryAtts = entryAttsDir.listFiles();
            for (File entryAtt : entryAtts) {
                byte[] data = FSUtils.readFile(entryAtt);
                byte[] decryptedData = decrypt(data);
                Attachment att = new Attachment(entryAtt.getName(), decryptedData);
                atts.add(att);
            }
        }
        return atts;
    }
    
    public Attachment getAttachment(UUID dataEntryID, String attName) throws Exception {
        Attachment att = null;
        File entryAttsDir = new File(Constants.ATTACHMENTS_DIR, dataEntryID.toString());
        if (entryAttsDir.exists()) {
            File[] entryAtts = entryAttsDir.listFiles();
            for (File entryAtt : entryAtts) {
                if (entryAtt.getName().equals(attName)) {
                    byte[] data = FSUtils.readFile(entryAtt);
                    byte[] decryptedData = decrypt(data);
                    att = new Attachment(entryAtt.getName(), decryptedData);
                }
            }
        }
        return att;
    }
    
    public void addAttachment(UUID dataEntryID, Attachment attachment) throws Exception {
        addAttachment(dataEntryID, attachment, Constants.ATTACHMENTS_DIR, CIPHER_ENCRYPT);
    }

    private void addAttachment(UUID dataEntryID, Attachment attachment, File attachmentsDir, Cipher cipher) throws Exception {
        if (dataEntryID != null && attachment != null) {
            File entryAttsDir = new File(attachmentsDir, dataEntryID.toString());
            if (!entryAttsDir.exists()) {
                entryAttsDir.mkdir();
            }
            File att = new File(entryAttsDir, attachment.getName());
            if (att.exists()) {
                throw new Exception("Duplicate attachment name!");
            }
            byte[] encryptedData = useCipher(cipher, attachment.getData());
            FSUtils.writeFile(att, encryptedData);
        } else {
            throw new Exception("Invalid parameters!");
        }
    }

    public void removeAttachment(UUID dataEntryID, String attachmentName) {
        File attDir = new File(Constants.ATTACHMENTS_DIR, dataEntryID.toString());
        File att = new File(attDir, attachmentName);
        FSUtils.delete(att);
        if (attDir.listFiles().length == 0) {
            attDir.delete();
        }
    }
    
    public void removeAttachments(UUID dataEntryID) {
        File entryAttsDir = new File(Constants.ATTACHMENTS_DIR, dataEntryID.toString());
        for (File attFile : entryAttsDir.listFiles()) {
            FSUtils.delete(attFile);
        }
        if (entryAttsDir.listFiles().length == 0) {
            entryAttsDir.delete();
        }
    }
    
    public void shutdown(int code) {
        try {
            cleanUpOrphanedStuff(collectDataEntryIDs(data));
            FSUtils.delete(Constants.TMP_DIR);
        } catch (Throwable t) {
            System.err.println("Error on shutdown: ");
            t.printStackTrace(System.err);
            code = -1;
        } finally {
            System.exit(code);
        }
    }
    
    private Collection<UUID> collectDataEntryIDs(DataCategory data) {
        Collection<UUID> ids = new ArrayList<UUID>();
        for (Recognizable r : data.getData()) {
            if (r instanceof DataCategory) {
                ids.addAll(collectDataEntryIDs((DataCategory) r));
            } else if (r instanceof DataEntry) {
                ids.add(r.getId());
            }
        }
        return ids;
    }
    
    private void cleanUpOrphanedStuff(Collection<UUID> ids) throws Exception {
        // data entries
        if (Constants.DATA_DIR.exists()) {
            Collection<UUID> foundDEIds = new ArrayList<UUID>();
            for (File deFile : Constants.DATA_DIR.listFiles(FILE_FILTER_DATA)) {
                UUID id = UUID.fromString(deFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
                foundDEIds.add(id);
            }
            for (UUID id : foundDEIds) {
                if (!ids.contains(id)) {
                    // orphaned data entry found, remove it
                    File deFile = new File(Constants.DATA_DIR, id.toString() + Constants.DATA_FILE_SUFFIX);
                    FSUtils.delete(deFile);
                }
            }
        }
        // attachments
        if (Constants.ATTACHMENTS_DIR.exists()) {
            Collection<UUID> foundAttIds = new ArrayList<UUID>();
            for (File attFile : Constants.ATTACHMENTS_DIR.listFiles()) {
                UUID id = UUID.fromString(attFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
                foundAttIds.add(id);
            }
            for (UUID id : foundAttIds) {
                if (!ids.contains(id)) {
                    // orphaned attachments found, remove'em
                    removeAttachments(id);
                }
            }
        }
        // configs
        if (Constants.CONFIG_DIR.exists()) {
            Collection<UUID> foundConfIds = new ArrayList<UUID>();
            for (File configFile : Constants.CONFIG_DIR.listFiles(FILE_FILTER_DATA_ENTRY_CONFIG)) {
                UUID id = UUID.fromString(configFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
                foundConfIds.add(id);
            }
            for (UUID id : foundConfIds) {
                if (!ids.contains(id)) {
                    // orphaned config found, remove it
                    FSUtils.delete(new File(Constants.CONFIG_DIR, id.toString() + Constants.DATA_ENTRY_CONFIG_FILE_SUFFIX));
                }
            }
        }
        // empty not anymore needed directories
        if (Constants.ADDON_INFO_DIR.exists() && Constants.ADDON_INFO_DIR.list().length == 0) {
            Constants.ADDON_INFO_DIR.delete();
        }
    }
    
    public DataCategory getData() {
        return data;
    }

    public void setData(DataCategory data) {
        this.data = data;
    }

    public byte[] getToolData(String tool) {
        return toolsData.get(tool);
    }

    public void setToolsData(Map<String, byte[]> toolsData) {
        this.toolsData = toolsData;
    }

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public Document getPrefs() {
        return prefs;
    }

}
