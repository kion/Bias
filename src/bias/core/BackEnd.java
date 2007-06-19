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
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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

import bias.Bias;
import bias.Constants;
import bias.Launcher;
import bias.Preferences;
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
	
	private BackEnd() {
        try {
            CIPHER_ENCRYPT = initCipher(Cipher.ENCRYPT_MODE, Launcher.PASSWORD);
            CIPHER_DECRYPT = initCipher(Cipher.DECRYPT_MODE, Launcher.PASSWORD);
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
    
    private static Cipher initCipher(int mode, String password) throws Exception {
        PBEParameterSpec paramSpec = new PBEParameterSpec(Constants.CIPHER_SALT, 20);
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory kf = SecretKeyFactory.getInstance(Constants.CIPHER_ALGORITHM);
        SecretKey key = kf.generateSecret(keySpec);
        Cipher cipher = Cipher.getInstance(Constants.CIPHER_ALGORITHM);
        cipher.init(mode, key, paramSpec);
        return cipher;
    }
    
    private Map<String, byte[]> zipEntries = new HashMap<String, byte[]>();
    
    private Map<UUID, byte[]> icons = new LinkedHashMap<UUID, byte[]>();
    
    private Map<String, DataEntry> identifiedData;
    
    private DataCategory data;
    
    private Document metadata;
    
    private Document prefs;
    
    private Properties config;
    
    public void load() throws Exception {
        identifiedData = new LinkedHashMap<String, DataEntry>();
        config = new Properties();
        
        ZipInputStream zis = new ZipInputStream(new FileInputStream(Bias.getJarFile()));
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            String path = ze.getName();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while ((c = zis.read()) != -1) {
                out.write(c);
            }
            out.close();
            zipEntries.put(path, out.toByteArray());
        }
        zis.close();
        
        byte[] data = null;
        byte[] decryptedData = null;
        if (Constants.DATA_DIR.exists()) {
            // data files
            for (File dataFile : Constants.DATA_DIR.listFiles()) {
                if (dataFile.getName().endsWith(Constants.DATA_FILE_SUFFIX)) {
                    data = FSUtils.readFile(dataFile);
                    decryptedData = CIPHER_DECRYPT.doFinal(data);
                    String entryIDStr = dataFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                    DataEntry de = new DataEntry();
                    de.setData(decryptedData);
                    identifiedData.put(entryIDStr, de);
                }
            }
        }
        File metadataFile = new File(Constants.DATA_DIR, Constants.METADATA_FILE);
        if (metadataFile.exists()) {
            // metadata file
            data = FSUtils.readFile(metadataFile);
            decryptedData = CIPHER_DECRYPT.doFinal(data);
            metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(decryptedData));
        }
        // global config file
        File configFile = new File(Constants.CONFIG_DIR, Constants.GLOBAL_CONFIG_FILE);
        if (configFile.exists()) {
            data = FSUtils.readFile(configFile);
            config.load(new ByteArrayInputStream(data));
        }
        // preferences file
        File prefsFile = new File(Constants.CONFIG_DIR, Constants.PREFERENCES_FILE);
        if (prefsFile.exists()) {
            data = FSUtils.readFile(prefsFile);
            if (data.length != 0) {
                prefs = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(data));
            }
        }
        if (Constants.ICONS_DIR.exists()) {
            // icon files
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
        // parse metadata file
        this.data = parseMetadata(metadata, identifiedData, null);
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
            }
        }
        // metadata file
        data = FSUtils.readFile(new File(dataDir, Constants.METADATA_FILE));
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
                    localConfigFile.createNewFile();
                    byte[] configData = FSUtils.readFile(configFile);
                    FSUtils.writeFile(localConfigFile, configData);
                }
            }
        }
        // read icon files
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
                    byte[] encryptedData = CIPHER_ENCRYPT.doFinal(decryptedData);
                    entryAttsDir = new File(Constants.ATTACHMENTS_DIR, entryAttsDir.getName());
                    if (!entryAttsDir.exists()) {
                        entryAttsDir.mkdir();
                    }
                    attFile = new File(entryAttsDir, attFile.getName());
                    FSUtils.writeFile(attFile, encryptedData);
                }
            }
        }
        // parse metadata file
        DataCategory importedData = parseMetadata(metadata, importedIdentifiedData, existingIDs);
        // add imported data to existing data
        this.data.addDataItems(importedData.getData());
        return importedData;
    }
    
    private DataCategory parseMetadata(
    		Document metadata, 
    		Map<String, DataEntry> identifiedData,
    		Collection<UUID> existingIDs) throws Exception {
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
    
    public void store(boolean storeDataOnly) throws Exception {
        // clear data dir before writing updated files
        FSUtils.clearDirectory(Constants.DATA_DIR);
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
        OutputFormat of = new OutputFormat();
        of.setIndenting(true);
        of.setIndent(4);
        StringWriter sw = new StringWriter();
        new XMLSerializer(sw, of).serialize(metadata);
        byte[] encryptedData = CIPHER_ENCRYPT.doFinal(sw.getBuffer().toString().getBytes());
        FSUtils.writeFile(new File(Constants.DATA_DIR, Constants.METADATA_FILE), encryptedData);
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
        sw = new StringWriter();
        config.list(new PrintWriter(sw));
        FSUtils.writeFile(new File(Constants.CONFIG_DIR, Constants.GLOBAL_CONFIG_FILE), sw.getBuffer().toString().getBytes());
        // preferences file
        FSUtils.writeFile(new File(Constants.CONFIG_DIR, Constants.PREFERENCES_FILE), Preferences.getInstance().serialize());
        if (!storeDataOnly) {
            // jar file itself (extensions & lafs)
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(Bias.getJarFile()));
            for (Entry<String, byte[]> entry : zipEntries.entrySet()) {
                String entryName = entry.getKey();
                ZipEntry zipEntry = new ZipEntry(entryName);
                byte[] entryData = entry.getValue();
                zos.putNextEntry(zipEntry);
                if (entryData != null) {
                    for (byte b : entryData) {
                        zos.write(b);
                    }
                }
                zos.closeEntry();
            }
            zos.flush();
            zos.close();
        }
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
                byte[] encryptedData = CIPHER_ENCRYPT.doFinal(entryData);
                FSUtils.writeFile(deFile, encryptedData);
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

    public Collection<String> getExtensions() {
        Collection<String> extensions = new LinkedHashSet<String>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.EXTENSION_PATTERN)) {
                String extension = 
                    name.substring(0, name.length() - Constants.CLASS_FILE_SUFFIX.length())
                    .replaceAll(Constants.ZIP_PATH_SEPARATOR, Constants.PACKAGE_PATH_SEPARATOR);
                extensions.add(extension);
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
        Set<String> installedExtNames = new HashSet<String>();
        if (extensionFile != null && extensionFile.exists() && !extensionFile.isDirectory()) {
            String name = extensionFile.getName();
            Map<String, byte[]> classesMap = new HashMap<String, byte[]>();
            Map<String, byte[]> resoursesMap = new HashMap<String, byte[]>();
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
                    if (jeName.matches(Constants.ADDON_CLASS_FILE_PATH_PATTERN)) {
                        type = 1;
                    } else if (jeName.matches(Constants.RESOURCE_FILE_PATH_PATTERN)) {
                        type = 2;
                    } else if (jeName.matches(Constants.LIB_FILE_PATH_PATTERN)) {
                        type = 3;
                    }
                    if (type != 0) {
                        out = new ByteArrayOutputStream();
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        out.close();
                        if (type == 1) {
                            if (!jeName.matches(Constants.ZIP_ADDITIONAL_CLASS_FILE_PATTERN)) {
                                String shortName = jeName.replaceFirst(Constants.ZIP_ENTRY_PREFIX_PATTERN, Constants.EMPTY_STR)
                                                     .replaceFirst(Constants.CLASS_FILE_SUFFIX_PATTERN, Constants.EMPTY_STR)
                                                     .replaceFirst(Constants.INNER_CLASS_FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                                installedExtNames.add(shortName);
                            }
                            classesMap.put(jeName, out.toByteArray());
                        } else if (type == 2) {
                            resoursesMap.put(jeName, out.toByteArray());
                        } else if (type == 3) {
                            ZipInputStream lin = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
                            ZipEntry lze;
                            while ((lze = lin.getNextEntry()) != null) {
                                String lzeName = lze.getName();
                                if (!lzeName.startsWith(Constants.META_INF_DIR) && !lzeName.endsWith(Constants.ZIP_PATH_SEPARATOR)) {
                                    ByteArrayOutputStream lout = new ByteArrayOutputStream();
                                    while ((b = lin.read()) != -1) {
                                        lout.write(b);
                                    }
                                    lout.close();
                                    if (!zipEntries.containsKey(lzeName)) {
                                        libsMap.put(lzeName, lout.toByteArray());
                                    } else {
                                        MessageDigest md = MessageDigest.getInstance(Constants.DIGEST_ALGORITHM);
                                        md.update(lout.toByteArray());
                                        byte[] newDigest = md.digest();
                                        md.update(zipEntries.get(lzeName));
                                        byte[] existingDigest = md.digest();
                                        if (MessageDigest.isEqual(newDigest, existingDigest)) {
                                            libsMap.put(lzeName, null);
                                        } else {
                                            throw new Exception(
                                                    "Extension pack '" + extensionFile.getAbsolutePath() + "' failed to intall!" + Constants.NEW_LINE + 
                                                    "Detected conflict with following add-on: " + getConflictingAddOn(lzeName) + Constants.NEW_LINE +
                                                    "Conflicting resource: " + lzeName + Constants.NEW_LINE + 
                                                    "If you still want to install this extension, you should first uninstall add-on it conflicts with.");
                                        }
                                    }
                                }
                            }
                            lin.close();
                        }
                    }
                }
                in.close();
            }
            if (installedExtNames.isEmpty()) {
                throw new Exception("Invalid extension pack: nothing to install!");
            } else if (!installedExtNames.contains(extName)) {
                throw new Exception(
                        "Invalid extension pack:" + Constants.NEW_LINE +
                        "class corresponding to declared " 
                        + Constants.MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE + " attribute" + Constants.NEW_LINE +
                        "in MANIFEST.MF file has not been found in package!");
            } else {
                String fullExtName = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                            + extName + Constants.PACKAGE_PATH_SEPARATOR + extName;
                if (getAddOns().contains(extName)) {
                    throw new Exception("Can not install Add-On pack: duplicate Add-On name!");
                } else {
                    for (Entry<String, byte[]> entry : classesMap.entrySet()) {
                        zipEntries.put(Constants.EXTENSION_DIR_PATH + Constants.ZIP_PATH_SEPARATOR 
                                + extName + Constants.ZIP_PATH_SEPARATOR 
                                + entry.getKey().replaceFirst(Constants.ADDON_CLASS_FILE_PREFIX_PATTERN, Constants.EMPTY_STR), 
                                entry.getValue());
                    }
                    for (Entry<String, byte[]> entry : resoursesMap.entrySet()) {
                        zipEntries.put(Constants.RESOURCES_DIR + extName + Constants.ZIP_PATH_SEPARATOR 
                                        + entry.getKey().replaceFirst(Constants.RESOURCE_FILE_PREFIX_PATTERN, Constants.EMPTY_STR), 
                                        entry.getValue());
                    }
                    if (!libsMap.isEmpty()) {
                        File extensionInstLogFile = new File(Constants.CONFIG_DIR, extName + Constants.EXT_LIB_INSTALL_LOG_FILE_SUFFIX);
                        StringBuffer sb = new StringBuffer();
                        for (Entry<String, byte[]> entry : libsMap.entrySet()) {
                            if (entry.getValue() != null) {
                                zipEntries.put(entry.getKey(), entry.getValue());
                            }
                            sb.append(entry.getKey() + Constants.NEW_LINE);
                        }
                        FSUtils.writeFile(extensionInstLogFile, sb.toString().getBytes());
                    }
                }
                installedExtensionName = fullExtName;
            }
        } else {
            throw new Exception("Invalid extension pack!");
        }
        return installedExtensionName;
    }
    
    public void uninstallExtension(String extension) throws Exception {
        String extensionName = extension.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
        String extensionPath = extension.replaceAll(Constants.PACKAGE_PATH_SEPARATOR_PATTERN, Constants.ZIP_PATH_SEPARATOR);
        extensionPath = extensionPath.substring(0, extensionPath.lastIndexOf(Constants.ZIP_PATH_SEPARATOR));
        Collection<String> removeKeys = new HashSet<String>();
        for (String key : zipEntries.keySet()) {
            if (key.startsWith(extensionPath)
                    || key.matches(Constants.RESOURCES_DIR 
                            + extensionName 
                            + Constants.ANY_CHARACTERS_PATTERN)) {
                removeKeys.add(key);
            }
        }
        uninstallLibs(extensionName);
        if (!removeKeys.isEmpty()) {
            for (String key : removeKeys) {
                zipEntries.remove(key);
            }
        }
        File extensionConfigFile = new File(Constants.CONFIG_DIR, extensionName + Constants.EXTENSION_CONFIG_FILE_SUFFIX);
        FSUtils.delete(extensionConfigFile);
    }
        
    public Map<String, byte[]> getLAFs() throws Exception {
        Map<String, byte[]> lafs = new LinkedHashMap<String, byte[]>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.LAF_PATTERN)) {
                String laf = 
                    name.substring(0, name.length() - Constants.CLASS_FILE_SUFFIX.length())
                    .replaceAll(Constants.ZIP_PATH_SEPARATOR, Constants.PACKAGE_PATH_SEPARATOR);
                byte[] settings = getLAFSettings(laf);
                lafs.put(laf, settings);
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
        String installedLAF = null;
        Set<String> installedLAFNames = new HashSet<String>();
        if (lafFile != null && lafFile.exists() && !lafFile.isDirectory()) {
            String name = lafFile.getName();
            Map<String, byte[]> classesMap = new HashMap<String, byte[]>();
            Map<String, byte[]> resoursesMap = new HashMap<String, byte[]>();
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
                    if (jeName.matches(Constants.ADDON_CLASS_FILE_PATH_PATTERN)) {
                        type = 1;
                    } else if (jeName.matches(Constants.RESOURCE_FILE_PATH_PATTERN)) {
                        type = 2;
                    } else if (jeName.matches(Constants.LIB_FILE_PATH_PATTERN)) {
                        type = 3;
                    }
                    if (type != 0) {
                        out = new ByteArrayOutputStream();
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        out.close();
                        if (type == 1) {
                            String shortName = jeName.replaceFirst(Constants.ZIP_ENTRY_PREFIX_PATTERN, Constants.EMPTY_STR)
                                                     .replaceFirst(Constants.CLASS_FILE_SUFFIX_PATTERN, Constants.EMPTY_STR)
                                                     .replaceFirst(Constants.INNER_CLASS_FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                            installedLAFNames.add(shortName);
                            classesMap.put(jeName, out.toByteArray());
                        } else if (type == 2) {
                            resoursesMap.put(jeName, out.toByteArray());
                        } else if (type == 3) {
                            ZipInputStream lin = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
                            ZipEntry lze;
                            while ((lze = lin.getNextEntry()) != null) {
                                String lzeName = lze.getName();
                                if (!lzeName.startsWith(Constants.META_INF_DIR) && !lzeName.endsWith(Constants.ZIP_PATH_SEPARATOR)) {
                                    ByteArrayOutputStream lout = new ByteArrayOutputStream();
                                    while ((b = lin.read()) != -1) {
                                        lout.write(b);
                                    }
                                    lout.close();
                                    if (!zipEntries.containsKey(lzeName)) {
                                        libsMap.put(lzeName, lout.toByteArray());
                                    } else {
                                        MessageDigest md = MessageDigest.getInstance(Constants.DIGEST_ALGORITHM);
                                        md.update(lout.toByteArray());
                                        byte[] newDigest = md.digest();
                                        md.update(zipEntries.get(lzeName));
                                        byte[] existingDigest = md.digest();
                                        if (MessageDigest.isEqual(newDigest, existingDigest)) {
                                            libsMap.put(lzeName, null);
                                        } else {
                                            throw new Exception(
                                                    "LAF pack '" + lafFile.getAbsolutePath() + "' failed to intall!" + Constants.NEW_LINE + 
                                                    "Detected conflict with following add-on: " + getConflictingAddOn(lzeName) + Constants.NEW_LINE +
                                                    "Conflicting resource: " + lzeName + Constants.NEW_LINE + 
                                                    "If you still want to install this look-&-feel, you should first uninstall add-on it conflicts with.");
                                        }
                                    }
                                }
                            }
                            lin.close();
                        }
                    }
                }
                in.close();
            }
            if (installedLAFNames.isEmpty()) {
                throw new Exception("Invalid LAF pack: nothing to install!");
            } else if (!installedLAFNames.contains(lafName)) {
                throw new Exception(
                        "Invalid extension pack:" + Constants.NEW_LINE +
                        "class corresponding to declared " 
                        + Constants.MANIFEST_FILE_ADD_ON_NAME_ATTRIBUTE + " attribute" + Constants.NEW_LINE +
                        "in MANIFEST.MF file has not been found in package!");
            } else {
                String fullLAFName = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                            + lafName + Constants.PACKAGE_PATH_SEPARATOR + lafName;
                if (getAddOns().contains(lafName)) {
                    throw new Exception("Can not install Add-On pack: duplicate Add-On name!");
                } else {
                    for (Entry<String, byte[]> entry : classesMap.entrySet()) {
                        zipEntries.put(Constants.LAF_DIR_PATH + Constants.ZIP_PATH_SEPARATOR 
                                + lafName + Constants.ZIP_PATH_SEPARATOR 
                                + entry.getKey().replaceFirst(Constants.ADDON_CLASS_FILE_PREFIX_PATTERN, Constants.EMPTY_STR), 
                                entry.getValue());
                    }
                    for (Entry<String, byte[]> entry : resoursesMap.entrySet()) {
                        zipEntries.put(Constants.RESOURCES_DIR + lafName + Constants.ZIP_PATH_SEPARATOR 
                                        + entry.getKey().replaceFirst(Constants.RESOURCE_FILE_PREFIX_PATTERN, Constants.EMPTY_STR), 
                                        entry.getValue());
                    }
                    if (!libsMap.isEmpty()) {
                        File lafInstLogFile = new File(Constants.CONFIG_DIR, lafName + Constants.LAF_LIB_INSTALL_LOG_FILE_SUFFIX);
                        StringBuffer sb = new StringBuffer();
                        for (Entry<String, byte[]> entry : libsMap.entrySet()) {
                            if (entry.getValue() != null) {
                                zipEntries.put(entry.getKey(), entry.getValue());
                            }
                            sb.append(entry.getKey() + Constants.NEW_LINE);
                        }
                        FSUtils.writeFile(lafInstLogFile, sb.toString().getBytes());
                    }
                }
                installedLAF = fullLAFName;
            }
        } else {
            throw new Exception("Invalid LAF pack!");
        }
        return installedLAF;
    }
    
    public void uninstallLAF(String laf) throws Exception {
        String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
        String lafPath = laf.replaceAll(Constants.PACKAGE_PATH_SEPARATOR_PATTERN, Constants.ZIP_PATH_SEPARATOR);
        lafPath = lafPath.substring(0, lafPath.lastIndexOf(Constants.ZIP_PATH_SEPARATOR));
        Collection<String> removeKeys = new HashSet<String>();
        for (String key : zipEntries.keySet()) {
            if (key.startsWith(lafPath)
                || key.matches(Constants.RESOURCES_DIR 
                        + lafName 
                        + Constants.ANY_CHARACTERS_PATTERN)) {
                removeKeys.add(key);
            }
        }
        uninstallLibs(lafName);
        if (!removeKeys.isEmpty()) {
            for (String key : removeKeys) {
                zipEntries.remove(key);
            }
        }
        File lafConfigFile = new File(Constants.CONFIG_DIR, lafName + Constants.LAF_CONFIG_FILE_SUFFIX);
        zipEntries.remove(lafConfigFile);
    }
    
    private void uninstallLibs(String addOn) throws Exception {
        Collection<String> addOnLibEntries = getAddOnLibEntries(addOn);
        Collection<String> nonAddOnLibEntries = getNonAddOnLibEntries(addOn);
        addOnLibEntries.removeAll(nonAddOnLibEntries);
        zipEntries.keySet().removeAll(addOnLibEntries);
        FSUtils.delete(new File(Constants.CONFIG_DIR, addOn + Constants.EXT_LIB_INSTALL_LOG_FILE_SUFFIX));
        FSUtils.delete(new File(Constants.CONFIG_DIR, addOn + Constants.LAF_LIB_INSTALL_LOG_FILE_SUFFIX));
    }
    
    private Collection<String> getAddOns() {
        Collection<String> addOns = new ArrayList<String>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.EXTENSION_PATTERN) || name.matches(Constants.LAF_PATTERN)) {
                String addOn = 
                    name.substring(0, name.length() - Constants.CLASS_FILE_SUFFIX.length())
                    .replaceAll(Constants.ZIP_ENTRY_PREFIX_PATTERN, Constants.EMPTY_STR);
                addOns.add(addOn);
            }
        }
        return addOns;
    }
    
    private Collection<String> getAddOnLibEntries(String addOn) throws Exception {
        Collection<String> entries = new ArrayList<String>();
        File logFile = new File(Constants.CONFIG_DIR, addOn + Constants.EXT_LIB_INSTALL_LOG_FILE_SUFFIX);
        if (!logFile.exists()) {
            logFile = new File(Constants.CONFIG_DIR, addOn + Constants.LAF_LIB_INSTALL_LOG_FILE_SUFFIX);
        }
        byte[] bytes = FSUtils.readFile(logFile);
        if (bytes != null) {
            String[] installEntries = new String(bytes).split(Constants.NEW_LINE);
            for (String installEntry : installEntries) {
                entries.add(installEntry);
            }
        }
        return entries;
    }
        
    private Collection<String> getNonAddOnLibEntries(String addOn) throws Exception {
        Collection<String> entries = new ArrayList<String>();
        for (File f : Constants.CONFIG_DIR.listFiles()) {
            if (f.getName().endsWith(Constants.EXT_LIB_INSTALL_LOG_FILE_SUFFIX)
                    || f.getName().endsWith(Constants.LAF_LIB_INSTALL_LOG_FILE_SUFFIX)) {
                byte[] bytes = FSUtils.readFile(f);
                if (bytes != null) {
                    String[] installEntries = new String(bytes).split(Constants.NEW_LINE);
                    for (String installEntry : installEntries) {
                        entries.add(installEntry);
                    }
                }
            }
        }
        return entries;
    }
    
    private String getConflictingAddOn(String conflictingResource) throws Exception {
        String conflictingAddOn = null;
        for (File f : Constants.CONFIG_DIR.listFiles()) {
            if (f.getName().endsWith(Constants.EXT_LIB_INSTALL_LOG_FILE_SUFFIX)
                    || f.getName().endsWith(Constants.LAF_LIB_INSTALL_LOG_FILE_SUFFIX)) {
                byte[] bytes = FSUtils.readFile(f);
                if (bytes != null) {
                    String[] installEntries = new String(bytes).split(Constants.NEW_LINE);
                    if (Arrays.asList(installEntries).contains(conflictingResource)) {
                        conflictingAddOn = f.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                        break;
                    }
                }
            }
        }
        return conflictingAddOn;
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
        File iconFile = new File(Constants.ICONS_DIR, icon.getDescription() + Constants.ICON_FILE_SUFFIX);
        FSUtils.delete(iconFile);
    }
    
    public Collection<Attachment> getAttachments(UUID dataEntryID) throws Exception {
        Collection<Attachment> atts = new LinkedList<Attachment>();
        File entryAttsDir = new File(Constants.ATTACHMENTS_DIR, dataEntryID.toString());
        if (entryAttsDir.exists()) {
            File[] entryAtts = entryAttsDir.listFiles();
            for (File entryAtt : entryAtts) {
                byte[] data = FSUtils.readFile(entryAtt);
                byte[] decryptedData = CIPHER_DECRYPT.doFinal(data);
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
                    byte[] decryptedData = CIPHER_DECRYPT.doFinal(data);
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
            byte[] encryptedData = CIPHER_ENCRYPT.doFinal(attachment.getData());
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
        if (Constants.ATTACHMENTS_DIR.exists()) {
            Collection<UUID> foundAttIds = new ArrayList<UUID>();
            for (File attFile : Constants.ATTACHMENTS_DIR.listFiles()) {
                UUID id = UUID.fromString(attFile.getName().replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR));
                foundAttIds.add(id);
            }
            for (UUID id : foundAttIds) {
                if (!ids.contains(id)) {
                    // orphaned attachments found, remove 'em
                    removeAttachments(id);
                }
            }
        }
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

    public Properties getConfig() {
        return config;
    }

    public void setConfig(Properties config) {
        this.config = config;
    }

    public Document getPrefs() {
        return prefs;
    }

    public void setPrefs(Document prefs) {
        this.prefs = prefs;
    }

}
