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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import bias.sync.Synchronizer;
import bias.utils.FSUtils;
import bias.utils.Validator;

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
    
    private File syncTableFile = new File(Constants.CONFIG_DIR, Constants.SYNC_TABLE_FILE);
    
    private File metadataFile = new File(Constants.DATA_DIR, Constants.METADATA_FILE_NAME);

    private static Collection<String> loadedExtensions;

    private static Map<String, String> newExtensions = new LinkedHashMap<String, String>();

    private static Collection<String> loadedLAFs;
    
    private static Map<String, String> newLAFs = new LinkedHashMap<String, String>();

    private static Collection<String> classPathEntries = new ArrayList<String>();
    
    private static Collection<String> outOfClasspathAddOns = new ArrayList<String>();

    private Map<UUID, byte[]> icons = new LinkedHashMap<UUID, byte[]>();
    
    private Map<String, DataEntry> identifiedData = new LinkedHashMap<String, DataEntry>();
    
    private DataCategory data;
    
    private Map<String, byte[]> toolsData = new HashMap<String, byte[]>();
    
    private Document metadata;
    
    private Document prefs;
    
    private Properties config = new Properties();
    
    private Properties syncTable = new Properties();
    
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
    
    public static void setPassword(String currentPassword, String newPassword) throws Exception {
        if ((password != null ? currentPassword != null : currentPassword == null) && newPassword != null) {
            if ((password == null && currentPassword == null) || currentPassword.equals(password)) {
                password = newPassword;
                // changing ecryption cipher
                CIPHER_ENCRYPT = initCipher(Cipher.ENCRYPT_MODE, password);
                if (currentPassword != null) {
                    // the rest of the data will be encrypted with new password automatically on save, 
                    // but attachments have to be decrypted and encrypted back with new password explicitly
                    // because they are stored on FS and not kept in memory like the rest of the data
                    reencryptAttachments();
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
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
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
    
    private byte[] useCipher(Cipher cipher, byte[] data) throws Exception {
        if (data == null) {
            return null;
        }
        return cipher.doFinal(data);
    }

    public void load() throws Exception {
        byte[] data = null;
        byte[] decryptedData = null;
        // preferences file
        File prefsFile = new File(Constants.CONFIG_DIR, Constants.PREFERENCES_FILE);
        if (prefsFile.exists()) {
            data = FSUtils.readFile(prefsFile);
            if (data.length != 0) {
                prefs = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(data));
            }
        }
        // global config file
        File configFile = new File(Constants.CONFIG_DIR, Constants.GLOBAL_CONFIG_FILE);
        if (configFile.exists()) {
            data = FSUtils.readFile(configFile);
            config.load(new ByteArrayInputStream(data));
        }
        // metadata file
        File metadataFile = new File(Constants.DATA_DIR, Constants.METADATA_FILE_NAME);
        if (metadataFile.exists()) {
            data = FSUtils.readFile(metadataFile);
            decryptedData = decrypt(data);
            metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(decryptedData));
        }
        // sync table
        if (syncTableFile.exists()) {
            syncTable.load(new FileInputStream(syncTableFile));
        }
        // synchronize
        Synchronizer.synchronize();
        // data files
        if (Constants.DATA_DIR.exists()) {
            for (File dataFile : Constants.DATA_DIR.listFiles()) {
                if (dataFile.getName().endsWith(Constants.DATA_FILE_SUFFIX)) {
                    // entries data files
                    data = FSUtils.readFile(dataFile);
                    decryptedData = decrypt(data);
                    String entryIDStr = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                    DataEntry de = new DataEntry();
                    de.setData(decryptedData);
                    identifiedData.put(entryIDStr, de);
                } else if (dataFile.getName().endsWith(Constants.TOOL_DATA_FILE_SUFFIX)) {
                    // tools data files
                    data = FSUtils.readFile(dataFile);
                    decryptedData = decrypt(data);
                    String tool = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                    tool = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                + tool + Constants.PACKAGE_PATH_SEPARATOR + tool;
                    toolsData.put(tool, decryptedData);
                }
            }
        }
        // icon files
        if (Constants.ICONS_DIR.exists()) {
            File iconsListFile = new File(Constants.CONFIG_DIR, Constants.ICONS_CONFIG_FILE);
            if (iconsListFile.exists()) {
                String[] iconsList = new String(FSUtils.readFile(iconsListFile)).split(Constants.NEW_LINE);
                for (String iconId : iconsList) {
                    if (!Validator.isNullOrBlank(iconId)) {
                        File iconFile = new File(Constants.ICONS_DIR, iconId + Constants.ICON_FILE_SUFFIX);
                        data = FSUtils.readFile(iconFile);
                        icons.put(UUID.fromString(iconId), data);
                    }
                }
            }
        }
        // classpath
        File classPathConfigFile = new File(Constants.CONFIG_DIR, Constants.CLASSPATH_CONFIG_FILE);
        if (classPathConfigFile.exists()) {
            // read classpath entries
            byte[] cpData = FSUtils.readFile(classPathConfigFile);
            if (cpData != null) {
                for (String cpEntry : new String(cpData).split(Constants.CLASSPATH_SEPARATOR)) {
                    classPathEntries.add(cpEntry);
                }
            }
        }
        // remove addon-files that are not in classpath
        for (File addonFile : Constants.ADDONS_DIR.listFiles()) {
            URI rootURI = Constants.ROOT_DIR.toURI();
            URI addonFileURI = addonFile.toURI();
            URI relativeURI = rootURI.relativize(addonFileURI);
            if (!classPathEntries.contains(relativeURI.toString())) {
                addonFile.delete();
                String addonName = addonFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                outOfClasspathAddOns.add(addonName);
            }
        }
        // remove lib-files that are not in classpath
        for (File addonLibFile : Constants.LIBS_DIR.listFiles()) {
            URI rootURI = Constants.ROOT_DIR.toURI();
            URI addonLibFileURI = addonLibFile.toURI();
            URI relativeURI = rootURI.relativize(addonLibFileURI);
            if (!classPathEntries.contains(relativeURI.toString())) {
                addonLibFile.delete();
            }
        }
        // parse metadata file
        this.data = parseMetadata(metadata, identifiedData, null);
        // get lists of loaded extensions and lafs
        loadedExtensions = getExtensions();
        loadedLAFs = getLAFs();
    }
    
    public DataCategory importData(File importDir, Collection<UUID> existingIDs, String password) throws Exception {
        Cipher cipher = initCipher(Cipher.DECRYPT_MODE, password);
        Map<String,DataEntry> importedIdentifiedData = new LinkedHashMap<String, DataEntry>();
        Document metadata = null;
        byte[] data = null;
        byte[] decryptedData = null;
        // data files
        File dataDir = new File(importDir, Constants.DATA_DIR.getName());
        for (File dataFile : dataDir.listFiles()) {
            if (dataFile.getName().endsWith(Constants.DATA_FILE_SUFFIX)) {
                data = FSUtils.readFile(dataFile);
                String entryIDStr = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                DataEntry de = new DataEntry();
                decryptedData = cipher.doFinal(data);
                de.setData(decryptedData);
                importedIdentifiedData.put(entryIDStr, de);
            } else if (dataFile.getName().endsWith(Constants.TOOL_DATA_FILE_SUFFIX)) {
                // tools data files
                String tool = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                tool = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                            + tool + Constants.PACKAGE_PATH_SEPARATOR + tool;
                if (!toolsData.containsKey(tool)) {
                    data = FSUtils.readFile(dataFile);
                    decryptedData = cipher.doFinal(data);
                    toolsData.put(tool, decryptedData);
                }
            }
        }
        // metadata file
        data = FSUtils.readFile(new File(dataDir, Constants.METADATA_FILE_NAME));
        decryptedData = cipher.doFinal(data);
        metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(decryptedData));
        // config files
        File configDir = new File(importDir, Constants.CONFIG_DIR.getName());
        if (configDir.exists()) {
            for (File configFile : configDir.listFiles()) {
                File localConfigFile = new File(Constants.CONFIG_DIR, configFile.getName());
                // if local config file does not exist, use imported one, 
                // otherwise - skip it (already existing local configuration will be used)
                if (!localConfigFile.exists()) {
                    FSUtils.copyFile(configFile, localConfigFile);
                }
            }
        }
        // icon files
        File iconsDir = new File(importDir, Constants.ICONS_DIR.getName());
        File iconsListFile = new File(configDir, Constants.ICONS_CONFIG_FILE);
        if (iconsListFile.exists()) {
            String[] iconsList = new String(FSUtils.readFile(iconsListFile)).split(Constants.NEW_LINE);
            for (String iconId : iconsList) {
                if (!Validator.isNullOrBlank(iconId)) {
                    File iconFile = new File(iconsDir, iconId + Constants.ICON_FILE_SUFFIX);
                    data = FSUtils.readFile(iconFile);
                    icons.put(UUID.fromString(iconId), data);
                }
            }
        }
        // attachements
        File attsDir = new File(importDir, Constants.ATTACHMENTS_DIR.getName());
        if (attsDir.exists()) {
            for (File entryAttsDir : attsDir.listFiles()) {
                for (File attFile : entryAttsDir.listFiles()) {
                    data = FSUtils.readFile(attFile);
                    decryptedData = cipher.doFinal(data);
                    byte[] encryptedData = encrypt(decryptedData);
                    entryAttsDir = new File(Constants.ATTACHMENTS_DIR, entryAttsDir.getName());
                    if (!entryAttsDir.exists()) {
                        entryAttsDir.mkdir();
                    }
                    attFile = new File(entryAttsDir, attFile.getName());
                    FSUtils.writeFile(attFile, encryptedData);
                }
            }
        }
        // classpath and addons/libs files
        File classPathConfigFile = new File(configDir, Constants.CLASSPATH_CONFIG_FILE);
        if (classPathConfigFile.exists()) {
            // read classpath entries
            byte[] cpData = FSUtils.readFile(classPathConfigFile);
            if (cpData != null) {
                for (String cpEntry : new String(cpData).split(Constants.CLASSPATH_SEPARATOR)) {
                    if (!classPathEntries.contains(cpEntry)) {
                        String[] addonFilePath = cpEntry.split(Constants.PATH_SEPARATOR);
                        if (addonFilePath.length == 2) {
                            File dir = new File(importDir, addonFilePath[0]);
                            File addonFile = new File(dir, addonFilePath[1]);
                            if (addonFile.exists()) {
                                File localDir = new File(Constants.ROOT_DIR, addonFilePath[0]);
                                File localAddOnFile = new File(localDir, addonFilePath[1]);
                                if (localDir.exists() && !localAddOnFile.exists()) {
                                    FSUtils.copyFile(addonFile, localAddOnFile);
                                    classPathEntries.add(cpEntry);
                                    if (!cpEntry.startsWith(Constants.LIBS_DIR.getName())) {
                                        if (cpEntry.matches(Constants.EXTENSION_JAR_FILE_PATTERN)) {
                                            String extension = cpEntry.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
                                            extension = extension.replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                                            extension = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                                        + extension + Constants.PACKAGE_PATH_SEPARATOR + extension;
                                            if (!newExtensions.keySet().contains(extension)) {
                                                newExtensions.put(extension, Constants.COMMENT_ADDON_IMPORTED);
                                            }
                                        } else if (cpEntry.matches(Constants.LAF_JAR_FILE_PATTERN)) {
                                            String laf = cpEntry.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
                                            laf = laf.replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                                            laf = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                                        + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                                            if (!newLAFs.keySet().contains(laf)) {
                                                newLAFs.put(laf, Constants.COMMENT_ADDON_IMPORTED);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            storeClassPathConfiguration();
        }
        // parse metadata file
        DataCategory importedData = parseMetadata(metadata, importedIdentifiedData, existingIDs);
        // add imported data to existing data
        this.data.addDataItems(importedData.getData());
        return importedData;
    }
    
    private DataCategory parseMetadata(Document metadata, Map<String, DataEntry> identifiedData, Collection<UUID> existingIDs) throws Exception {
        DataCategory data = new DataCategory();
        if (metadata == null) {
            metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        } else {
            buildData(data, metadata.getFirstChild(), identifiedData, existingIDs);
            data.setPlacement(Integer.valueOf(metadata.getFirstChild().getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT).getNodeValue()));
            Node activeIdxNode = metadata.getFirstChild().getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX);
            if (activeIdxNode != null) {
                data.setActiveIndex(Integer.valueOf(activeIdxNode.getNodeValue()));
            }
        }
        return data;
    }
    
    private void buildData(DataCategory data, Node node, Map<String, DataEntry> identifiedData, Collection<UUID> existingIDs) throws Exception {
        if (node.getNodeName().equals(Constants.XML_ELEMENT_ROOT_CONTAINER)) {
            NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                buildData(data, n, identifiedData, existingIDs);
            }
        } else if (node.getNodeName().equals(Constants.XML_ELEMENT_CATEGORY)) {
            NamedNodeMap attributes = node.getAttributes();
            DataCategory dc = new DataCategory();
            Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
            UUID id = UUID.fromString(attID.getNodeValue());
            if (existingIDs == null || !existingIDs.contains(id)) {
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
                    buildData(dc, n, identifiedData, existingIDs);
                }
            }
        } else if (node.getNodeName().equals(Constants.XML_ELEMENT_ENTRY)) {
            NamedNodeMap attributes = node.getAttributes();
            Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
            UUID id = UUID.fromString(attID.getNodeValue());
            DataEntry dataEntry = identifiedData.get(id.toString());
            if (existingIDs == null || !existingIDs.contains(id)) {
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
                setDataEntrySettings(dataEntry);
                data.addDataItem(dataEntry);
            }
        }
    }
    
    public void store() throws Exception {
        // metadata file and data entries
        metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = metadata.createElement(Constants.XML_ELEMENT_ROOT_CONTAINER);
        rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT, data.getPlacement().toString());
        if (data.getActiveIndex() != null) {
            rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX, data.getActiveIndex().toString());
        }
        Collection<UUID> ids = buildNode(rootNode, data);
        cleanUpOrphanedStuff(ids);
        metadata.appendChild(rootNode);
        storeMetadata();
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
        StringBuffer iconsList = new StringBuffer();
        for (Entry<UUID, byte[]> icon : icons.entrySet()) {
            File iconFile = new File(Constants.ICONS_DIR, icon.getKey().toString() + Constants.ICON_FILE_SUFFIX);
            FSUtils.writeFile(iconFile, icon.getValue());
            iconsList.append(icon.getKey().toString());
            iconsList.append(Constants.NEW_LINE);
        }
        File iconsListFile = new File(Constants.CONFIG_DIR, Constants.ICONS_CONFIG_FILE);
        FSUtils.writeFile(iconsListFile, iconsList.toString().getBytes());
        // global config file
        StringWriter sw = new StringWriter();
        config.list(new PrintWriter(sw));
        FSUtils.writeFile(new File(Constants.CONFIG_DIR, Constants.GLOBAL_CONFIG_FILE), sw.getBuffer().toString().getBytes());
        // preferences file
        storePreferences();
        // sync table
        syncTableFile.createNewFile();
        syncTable.store(new FileOutputStream(syncTableFile), null);
    }
    
    public void storePreferences() throws Exception {
        FSUtils.writeFile(new File(Constants.CONFIG_DIR, Constants.PREFERENCES_FILE), Preferences.getInstance().serialize());
    }
    
    public void storeSyncTable() throws Exception {
        syncTableFile.createNewFile();
        syncTable.store(new FileOutputStream(syncTableFile), null);
    }
    
    public void storeMetadata() throws Exception {
        OutputFormat of = new OutputFormat();
        StringWriter sw = new StringWriter();
        new XMLSerializer(sw, of).serialize(metadata);
        byte[] encryptedData = encrypt(sw.getBuffer().toString().getBytes());
        FSUtils.writeFile(metadataFile, encryptedData);
    }
    
    private Collection<UUID> buildNode(Node node, DataCategory data) throws Exception {
        Collection<UUID> ids = new ArrayList<UUID>();
        for (Recognizable item : data.getData()) {
            if (item instanceof DataEntry) {
                DataEntry de = (DataEntry) item;
                File deFile = new File(Constants.DATA_DIR, de.getId().toString() + Constants.DATA_FILE_SUFFIX);
                byte[] entryData = de.getData();
                if (entryData == null) {
                    entryData = new byte[]{};
                }
                DataEntry oldDE = identifiedData.get(de.getId().toString());
                boolean dataChanged = (oldDE == null || !Arrays.equals(entryData, oldDE.getData()));
                if (dataChanged) {
                    System.out.println(de.getCaption() + " - writing data...");
                    byte[] encryptedData = encrypt(entryData);
                    FSUtils.writeFile(deFile, encryptedData);
                    if (Preferences.getInstance().syncType != null) {
                        handleSyncTableEntry(deFile, Synchronizer.UPDATE_MARKER);
                    }
                    identifiedData.put(de.getId().toString(), de);
                } else {
                    if (Preferences.getInstance().syncType != null) {
                        handleSyncTableEntry(deFile, null);
                    }
                }
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
                ids.addAll(buildNode(catNode, dc));
                node.appendChild(catNode);
            }
        }
        return ids;
    }
    
    private void handleSyncTableEntry(File file, Character marker) throws Exception {
        String relPath = file.getAbsolutePath().substring(Constants.ROOT_DIR.getAbsolutePath().length() + 1);
        String revisionStr = syncTable.getProperty(Synchronizer.UPDATE_MARKER + relPath);
        if (revisionStr == null) {
            revisionStr = syncTable.getProperty(relPath);
            if (revisionStr == null) {
                syncTable.setProperty(relPath, "0");
            } else if (marker != null) {
                System.out.println("Marking file [" + relPath + "] with marker [" + marker + "]");
                syncTable.setProperty(marker + relPath, revisionStr);
                syncTable.remove(relPath);
            }
        }
    }
    
    private void setDataEntrySettings(DataEntry dataEntry) throws Exception {
        if (dataEntry != null) {
            byte[] settings = null;
            File dataEntryConfigFile = new File(Constants.CONFIG_DIR, dataEntry.getId().toString() + Constants.DATA_ENTRY_CONFIG_FILE_SUFFIX);
            if (dataEntryConfigFile.exists()) {
                settings = FSUtils.readFile(dataEntryConfigFile);
            }
            if (settings == null) {
                settings = getExtensionSettings(dataEntry.getType());
            }
            dataEntry.setSettings(settings);
        }
    }

    private void storeDataEntrySettings(DataEntry dataEntry) throws Exception {
        if (dataEntry != null && dataEntry.getSettings() != null) {
            byte[] defSettings = getExtensionSettings(dataEntry.getType());
            File deConfigFile = new File(Constants.CONFIG_DIR, dataEntry.getId().toString() + Constants.DATA_ENTRY_CONFIG_FILE_SUFFIX);
            if (!Arrays.equals(defSettings,dataEntry.getSettings())) {
                FSUtils.writeFile(deConfigFile, dataEntry.getSettings());
            } else {
                FSUtils.delete(deConfigFile);
            }
        }
    }

    public Map<String, String> getNewExtensions() {
        return newExtensions;
    }
    
    public Collection<String> getExtensions() {
        Collection<String> extensions = new LinkedHashSet<String>();
        for (String name : classPathEntries) {
            if (!name.startsWith(Constants.LIBS_DIR.getName())) {
                name = name.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
                if (name.matches(Constants.EXTENSION_JAR_FILE_PATTERN)) {
                    String extension = name.replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                    extension = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                + extension + Constants.PACKAGE_PATH_SEPARATOR + extension;
                    if (!newExtensions.keySet().contains(extension)) {
                        extensions.add(extension);
                    }
                }
            }
        }
        return extensions;
    }

    public byte[] getExtensionSettings(String extension) throws Exception {
        byte[] settings = null;
        if (!Validator.isNullOrBlank(extension)) {
            String extensionName = extension.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            File extConfigFile = new File(Constants.CONFIG_DIR, extensionName + Constants.EXTENSION_CONFIG_FILE_SUFFIX);
            if (extConfigFile.exists()) {
                settings = FSUtils.readFile(extConfigFile);
            }
        }
        return settings;
    }

    public void storeExtensionSettings(String extension, byte[] settings) throws Exception {
        if (!Validator.isNullOrBlank(extension) && settings != null) {
            String extensionName = extension.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            File extConfigFile = new File(Constants.CONFIG_DIR, extensionName + Constants.EXTENSION_CONFIG_FILE_SUFFIX);
            FSUtils.writeFile(extConfigFile, settings);
        }
    }

    public String installExtension(File extensionFile) throws Exception {
        String installedExtensionName = null;
        if (extensionFile != null && extensionFile.exists() && !extensionFile.isDirectory()) {
            boolean jarFound = false;
            boolean error = false;
            String name = extensionFile.getName();
            byte[] installedExtensionJAR = null;
            Map<String, byte[]> libsMap = new HashMap<String, byte[]>();
            String extName = null;
            if (name.matches(Constants.ADDON_PACK_PATTERN)) {
                JarInputStream in = new JarInputStream(new FileInputStream(extensionFile));
                Manifest manifest = in.getManifest();
                if (manifest == null) {
                    throw new Exception(
                            "Invalid extension pack:" + Constants.NEW_LINE +
                            "MANIFEST.MF file is missing!");
                }
                extName = manifest.getMainAttributes().getValue(Constants.MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE);
                if (Validator.isNullOrBlank(extName)) {
                    throw new Exception(
                            "Invalid extension pack:" + Constants.NEW_LINE +
                            Constants.MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE 
                            + " attribute in MANIFEST.MF file is missing/empty!");
                }
                JarEntry je = null;
                while ((je = in.getNextJarEntry()) != null) {
                    String jeName = je.getName();
                    ByteArrayOutputStream out = null;
                    int type = 0;
                    if (jeName.matches(Constants.EXTENSION_JAR_FILE_PATH_PATTERN)) {
                        if (!jarFound) {
                            if (extName.equals(
                                    jeName.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR)
                                          .replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR))) {
                                type = 1;
                            } else {
                                error = true;
                                break;
                            }
                            jarFound = true;
                        } else {
                            error = true;
                            break;
                        }
                    } else if (jeName.matches(Constants.LIBS_FILE_PATH_PATTERN)) {
                        type = 2;
                    }
                    if (type != 0) {
                        out = new ByteArrayOutputStream();
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        out.close();
                        String installedName = jeName.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR)
                                                     .replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                        if (type == 1) {
                            installedExtensionName = installedName;
                            installedExtensionJAR = out.toByteArray();
                        } else if (type == 2) {
                            String libName = installedName + Constants.ADDON_LIB_FILENAME_SEPARATOR + extName + Constants.EXTENSION_JAR_FILE_SUFFIX; 
                            libsMap.put(libName, out.toByteArray());
                        }
                    }
                }
                in.close();
            }
            if (Validator.isNullOrBlank(installedExtensionName)) {
                throw new Exception("Invalid extension pack: nothing to install!");
            } else if (error) {
                throw new Exception(
                        "Invalid extension pack:" + Constants.NEW_LINE +
                        "Extension add-on pack should contain only one extension JAR file with name corresponding to declared " 
                        + Constants.MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE + " attribute" + Constants.NEW_LINE +
                        "in MANIFEST.MF file and \"jar\" file extension!");
            } else {
                String fullExtName = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                            + extName + Constants.PACKAGE_PATH_SEPARATOR + extName;
                if (getExtensions().contains(fullExtName)) {
                    throw new Exception("Can not install Extension add-on pack: duplicate extension name!");
                } else if (loadedExtensions.contains(fullExtName)) {
                    throw new Exception("Can not install Extension add-on pack: Bias restart needed!");
                } else {
                    File installedExtensionFile = new File(Constants.ADDONS_DIR, installedExtensionName + Constants.EXTENSION_JAR_FILE_SUFFIX);
                    FSUtils.writeFile(installedExtensionFile, installedExtensionJAR);
                    addClassPathEntry(installedExtensionFile);
                    if (!libsMap.isEmpty()) {
                        for (Entry<String, byte[]> entry : libsMap.entrySet()) {
                            if (entry.getValue() != null) {
                                File libFile = new File(Constants.LIBS_DIR, entry.getKey());
                                FSUtils.writeFile(libFile, entry.getValue());
                                addClassPathEntry(libFile);
                            }
                        }
                    }
                    storeClassPathConfiguration();
                }
                installedExtensionName = fullExtName;
                newExtensions.put(installedExtensionName, Constants.COMMENT_ADDON_INSTALLED);
            }
        } else {
            throw new Exception("Invalid extension pack!");
        }
        return installedExtensionName;
    }
    
    public void uninstallExtension(String extension) throws Exception {
        String extensionName = extension.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
        Collection<String> currentClassPathEntries = new ArrayList<String>(classPathEntries);
        Iterator<String> it = currentClassPathEntries.iterator();
        while (it.hasNext()) {
            String cpEntry = it.next();
            String addonFileName = cpEntry.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
            if (addonFileName.matches(Constants.EXTENSION_JAR_FILE_PATTERN)
                    && addonFileName.contains(extensionName)) {
                classPathEntries.remove(cpEntry);
            }
        }
        storeClassPathConfiguration();
        newExtensions.remove(extension);
    }
    
    public Map<String, String> getNewLAFs() {
        return newLAFs;
    }
    
    public Collection<String> getLAFs() {
        Collection<String> lafs = new LinkedHashSet<String>();
        for (String name : classPathEntries) {
            if (!name.startsWith(Constants.LIBS_DIR.getName())) {
                name = name.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
                if (name.matches(Constants.LAF_JAR_FILE_PATTERN)) {
                    String laf = name.replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                    laf = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                + laf + Constants.PACKAGE_PATH_SEPARATOR + laf;
                    if (!newLAFs.keySet().contains(laf)) {
                        lafs.add(laf);
                    }
                }
            }
        }
        return lafs;
    }
    
    public byte[] getLAFSettings(String laf) throws Exception {
        byte[] settings = null;
        if (!Validator.isNullOrBlank(laf)) {
            String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            File lafConfigFile = new File(Constants.CONFIG_DIR, lafName + Constants.LAF_CONFIG_FILE_SUFFIX);
            if (lafConfigFile.exists()) {
                settings = FSUtils.readFile(lafConfigFile);
            }
        }
        return settings;
    }

    public void storeLAFSettings(String laf, byte[] settings) throws Exception {
        if (!Validator.isNullOrBlank(laf) && settings != null) {
            String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            File lafConfigFile = new File(Constants.CONFIG_DIR, lafName + Constants.LAF_CONFIG_FILE_SUFFIX);
            FSUtils.writeFile(lafConfigFile, settings);
        }
    }

    public String installLAF(File lafFile) throws Exception {
        String installedLAFName = null;
        if (lafFile != null && lafFile.exists() && !lafFile.isDirectory()) {
            boolean jarFound = false;
            boolean error = false;
            String name = lafFile.getName();
            byte[] installedLAFJAR = null;
            Map<String, byte[]> libsMap = new HashMap<String, byte[]>();
            String lafName = null;
            if (name.matches(Constants.ADDON_PACK_PATTERN)) {
                JarInputStream in = new JarInputStream(new FileInputStream(lafFile));
                Manifest manifest = in.getManifest();
                if (manifest == null) {
                    throw new Exception(
                            "Invalid LAF pack:" + Constants.NEW_LINE +
                            "MANIFEST.MF file is missing!");
                }
                lafName = manifest.getMainAttributes().getValue(Constants.MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE);
                if (Validator.isNullOrBlank(lafName)) {
                    throw new Exception(
                            "Invalid LAF pack:" + Constants.NEW_LINE +
                            Constants.MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE 
                            + " attribute in MANIFEST.MF file is missing/empty!");
                }
                JarEntry je = null;
                while ((je = in.getNextJarEntry()) != null) {
                    String jeName = je.getName();
                    ByteArrayOutputStream out = null;
                    int type = 0;
                    if (jeName.matches(Constants.LAF_JAR_FILE_PATH_PATTERN)) {
                        if (!jarFound) {
                            if (lafName.equals(
                                    jeName.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR)
                                          .replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR))) {
                                type = 1;
                            } else {
                                error = true;
                                break;
                            }
                            jarFound = true;
                        } else {
                            error = true;
                            break;
                        }
                    } else if (jeName.matches(Constants.LIBS_FILE_PATH_PATTERN)) {
                        type = 2;
                    }
                    if (type != 0) {
                        out = new ByteArrayOutputStream();
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        out.close();
                        String installedName = jeName.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR)
                                                     .replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                        if (type == 1) {
                            installedLAFName = installedName;
                            installedLAFJAR = out.toByteArray();
                        } else if (type == 2) {
                            String libName = installedName + Constants.ADDON_LIB_FILENAME_SEPARATOR + lafName + Constants.LAF_JAR_FILE_SUFFIX; 
                            libsMap.put(libName, out.toByteArray());
                        }
                    }
                }
                in.close();
            }
            if (Validator.isNullOrBlank(installedLAFName)) {
                throw new Exception("Invalid LAF pack: nothing to install!");
            } else if (error) {
                throw new Exception(
                        "Invalid LAF pack:" + Constants.NEW_LINE +
                        "LAF add-on pack should contain only one LAF JAR file with name corresponding to declared " 
                        + Constants.MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE + " attribute" + Constants.NEW_LINE +
                        "in MANIFEST.MF file and \"jar\" file extension!");
            } else {
                String fullLAFName = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                            + lafName + Constants.PACKAGE_PATH_SEPARATOR + lafName;
                if (getLAFs().contains(fullLAFName)) {
                    throw new Exception("Can not install LAF add-on pack: duplicate look-&-feel name!");
                } else if (loadedLAFs.contains(fullLAFName)) {
                    throw new Exception("Can not install LAF add-on pack: Bias restart needed!");
                } else {
                    File installedLAFFile = new File(Constants.ADDONS_DIR, installedLAFName + Constants.LAF_JAR_FILE_SUFFIX);
                    FSUtils.writeFile(installedLAFFile, installedLAFJAR);
                    addClassPathEntry(installedLAFFile);
                    if (!libsMap.isEmpty()) {
                        for (Entry<String, byte[]> entry : libsMap.entrySet()) {
                            if (entry.getValue() != null) {
                                File libFile = new File(Constants.LIBS_DIR, entry.getKey());
                                FSUtils.writeFile(libFile, entry.getValue());
                                addClassPathEntry(libFile);
                            }
                        }
                    }
                    storeClassPathConfiguration();
                }
                installedLAFName = fullLAFName;
                newLAFs.put(installedLAFName, Constants.COMMENT_ADDON_INSTALLED);
            }
        } else {
            throw new Exception("Invalid LAF pack!");
        }
        return installedLAFName;
    }
    
    public void uninstallLAF(String laf) throws Exception {
        String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
        Collection<String> currentClassPathEntries = new ArrayList<String>(classPathEntries);
        Iterator<String> it = currentClassPathEntries.iterator();
        while (it.hasNext()) {
            String cpEntry = it.next();
            String addonFileName = cpEntry.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
            if (addonFileName.matches(Constants.LAF_JAR_FILE_PATTERN)
                    && addonFileName.contains(lafName)) {
                classPathEntries.remove(cpEntry);
            }
        }
        storeClassPathConfiguration();
        newLAFs.remove(laf);
    }
    
    public void removeUnusedAddOnDataAndConfigFiles() {
        for (String addonName : outOfClasspathAddOns) {
            File extensionDataFile = new File(Constants.DATA_DIR, addonName + Constants.TOOL_DATA_FILE_SUFFIX);
            if (extensionDataFile.exists()) {
                FSUtils.delete(extensionDataFile);
            }
            File extensionConfigFile = new File(Constants.CONFIG_DIR, addonName + Constants.EXTENSION_CONFIG_FILE_SUFFIX);
            if (extensionConfigFile.exists()) {
                FSUtils.delete(extensionConfigFile);
            }
            File lafConfigFile = new File(Constants.CONFIG_DIR, addonName + Constants.LAF_CONFIG_FILE_SUFFIX);
            if (lafConfigFile.exists()) {
                FSUtils.delete(lafConfigFile);
            }
        }
    }
        
    private void addClassPathEntry(File jarFile) {
        URI rootURI = Constants.ROOT_DIR.toURI();
        URI jarFileURI = jarFile.toURI();
        URI relativeURI = rootURI.relativize(jarFileURI);
        classPathEntries.add(relativeURI.toString());
    }
    
    private void storeClassPathConfiguration() throws Exception {
        StringBuffer classpath = new StringBuffer();
        Iterator<String> it = classPathEntries.iterator();
        while (it.hasNext()) {
            String cpEntry = it.next();
            classpath.append(cpEntry);
            if (it.hasNext()) {
                classpath.append(Constants.CLASSPATH_SEPARATOR);
            }
        }
        File classPathConfigFile = new File(Constants.CONFIG_DIR, Constants.CLASSPATH_CONFIG_FILE);
        if (!classPathConfigFile.exists()) {
            classPathConfigFile.createNewFile();
        }
        if (!Validator.isNullOrBlank(classpath)) {
            FSUtils.writeFile(classPathConfigFile, classpath.toString().getBytes());
        } else {
            classPathConfigFile.delete();
        }
    }
    
    public Collection<ImageIcon> getIcons() {
        Collection<ImageIcon> icons = new LinkedHashSet<ImageIcon>();
        for (Entry<UUID, byte[]> iconEntry : this.icons.entrySet()) {
            ImageIcon icon = new ImageIcon(iconEntry.getValue(), iconEntry.getKey().toString());
            icons.add(icon);
        }
        return icons;
    }
    
    public Collection<ImageIcon> addIcons(File file) throws Exception {
        Collection<ImageIcon> icons = new LinkedList<ImageIcon>();
        if (file != null && file.exists() && !file.isDirectory()) {
            if (file.getName().matches(Constants.ADDON_PACK_PATTERN)) {
                ZipInputStream in = new ZipInputStream(new FileInputStream(file));
                ZipEntry zEntry;                     
                while ((zEntry = in.getNextEntry()) != null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int b;
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                    out.close();
                    ImageIcon icon = addIcon(zEntry.getName(), new ByteArrayInputStream(out.toByteArray()));
                    if (icon != null) {
                        icons.add(icon);
                    }
                }
            } else {
                ImageIcon icon = addIcon(file.getName(), new FileInputStream(file));
                if (icon != null) {
                    icons.add(icon);
                }
            }
        } else {
            throw new Exception("Invalid icon file/pack!");
        }
        return icons;
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
                icon.setDescription(idStr.toString());
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, Constants.ICON_FORMAT, baos);
                this.icons.put(id, baos.toByteArray());
            }
        }
        return icon;
    }

    public void removeIcon(ImageIcon icon) throws Exception {
        String id = icon.getDescription();
        File iconFile = new File(Constants.ICONS_DIR, id + Constants.ICON_FILE_SUFFIX);
        FSUtils.delete(iconFile);
        icons.remove(UUID.fromString(id));
    }
    
    private static void reencryptAttachments() throws Exception {
        for (File entryAttsDir : Constants.ATTACHMENTS_DIR.listFiles()) {
            File[] entryAtts = entryAttsDir.listFiles();
            for (File entryAtt : entryAtts) {
                byte[] data = FSUtils.readFile(entryAtt);
                byte[] decryptedData = CIPHER_DECRYPT.doFinal(data);
                byte[] encryptedData = CIPHER_ENCRYPT.doFinal(decryptedData);
                FSUtils.writeFile(entryAtt, encryptedData);
            }
        }
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
        if (dataEntryID != null && attachment != null) {
            File entryAttsDir = new File(Constants.ATTACHMENTS_DIR, dataEntryID.toString());
            if (!entryAttsDir.exists()) {
                entryAttsDir.mkdir();
            }
            File att = new File(entryAttsDir, attachment.getName());
            if (att.exists()) {
            	throw new Exception("Duplicate attachment name!");
            }
            byte[] encryptedData = encrypt(attachment.getData());
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
    
    private void cleanUpOrphanedStuff(Collection<UUID> ids) throws Exception {
        // data entries
        if (Constants.DATA_DIR.exists()) {
            Collection<UUID> foundDEIds = new ArrayList<UUID>();
            for (File deFile : Constants.DATA_DIR.listFiles()) {
                if (deFile.getName().endsWith(Constants.DATA_FILE_SUFFIX)) {
                    UUID id = UUID.fromString(deFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
                    foundDEIds.add(id);
                }
            }
            for (UUID id : foundDEIds) {
                if (!ids.contains(id)) {
                    // orphaned data entry found, remove it
                    File deFile = new File(Constants.DATA_DIR, id.toString() + Constants.DATA_FILE_SUFFIX);
                    FSUtils.delete(deFile);
                    handleSyncTableEntry(deFile, Synchronizer.DELETE_MARKER);
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
            for (File configFile : Constants.CONFIG_DIR.listFiles()) {
                if (configFile.getName().endsWith(Constants.DATA_ENTRY_CONFIG_FILE_SUFFIX)) {
                    UUID id = UUID.fromString(configFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
                    foundConfIds.add(id);
                }
            }
            for (UUID id : foundConfIds) {
                if (!ids.contains(id)) {
                    // orphaned config found, remove it
                    FSUtils.delete(new File(Constants.CONFIG_DIR, id.toString() + Constants.DATA_ENTRY_CONFIG_FILE_SUFFIX));
                }
            }
        }
    }
    
    public DataCategory getData() {
        return data;
    }

    public void setData(DataCategory data) {
        this.data = data;
    }

    public Map<String, byte[]> getToolsData() {
        return toolsData;
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

    public Properties getSyncTable() {
        return syncTable;
    }

    public void setSyncTable(Properties syncTable) {
        this.syncTable = syncTable;
    }

    public Document getMetadata() throws Exception {
        return metadata;
    }

    public void setMetadata(Document metadata) {
        this.metadata = metadata;
    }

    public Document getPrefs() {
        return prefs;
    }

}
