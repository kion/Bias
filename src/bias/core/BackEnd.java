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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bias.Bias;
import bias.Constants;
import bias.utils.Validator;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author kion
 */
public class BackEnd {
	
	private static BackEnd instance;
	
	private BackEnd() {
		// constructor without parameters is hidden for singleton
	}
	
	public static BackEnd getInstance() {
		if (instance == null) {
			instance = new BackEnd();
		}
		return instance;
	}
    
    private Map<String, byte[]> zipEntries;
    
    private Map<String, DataEntry> identifiedData;
    
    private DataCategory data;
    
    private Document metadata;
    
    private Properties settings;
    
    public void load() throws Exception {
        zipEntries = new LinkedHashMap<String, byte[]>();
        identifiedData = new LinkedHashMap<String, DataEntry>();
        settings = new Properties();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(Bias.getJarFile()));
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while ((c = zis.read()) != -1) {
                out.write(c);
            }
            String name = ze.getName();
            if (name.matches(Constants.DATA_FILE_PATTERN)) {
            	String entryIDStr = ze.getName()
                                        .replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
                                        .replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR);
                DataEntry de = new DataEntry();
                de.setData(out.toByteArray());
                identifiedData.put(entryIDStr, de);
            } else if (name.equals(Constants.METADATA_FILE_PATH)) {
                if (out.size() != 0) {
                    metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(
                            new ByteArrayInputStream(out.toByteArray()));
                }
            } else if (name.equals(Constants.GLOBAL_CONFIG_FILE_PATH)) {
                settings.load(
                        new ByteArrayInputStream(out.toByteArray()));
            } else {
                zipEntries.put(ze.getName(), out.toByteArray());
            }
        }
        zis.close();
        data = parseMetadata(metadata, identifiedData, null);
    }
    
    public DataCategory importData(File jarFile, Collection<UUID> existingIDs) throws Exception {
        Map<String,DataEntry> importedIdentifiedData = new LinkedHashMap<String, DataEntry>();
        Document metadata = null;
        ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while ((c = zis.read()) != -1) {
                out.write(c);
            }
            if (ze.getName().matches(Constants.DATA_FILE_PATTERN)) {
                String entryIDStr = ze.getName()
                                        .replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
                                        .replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR);
                DataEntry de = new DataEntry();
                de.setData(out.toByteArray());
                importedIdentifiedData.put(entryIDStr, de);
            } else if (ze.getName().matches(Constants.ICON_FILE_PATH_PATTERN)) {
            	if (!zipEntries.containsKey(ze.getName())) {
            		zipEntries.put(ze.getName(), out.toByteArray());
            	}
            } else if (ze.getName().equals(Constants.METADATA_FILE_PATH)) {
                if (out.size() != 0) {
                    metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(
                            new ByteArrayInputStream(out.toByteArray()));
                }
            }    
        }
        zis.close();
        DataCategory importedData = parseMetadata(metadata, importedIdentifiedData, existingIDs);
        data.addDataItems(importedData.getData());
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
                	byte[] imageData = zipEntries.get(Constants.ICONS_DIR + iconID + Constants.ICON_FILE_ENDING);
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
                	byte[] imageData = zipEntries.get(Constants.ICONS_DIR + iconID + Constants.ICON_FILE_ENDING);
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
        metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = metadata.createElement(Constants.XML_ELEMENT_ROOT_CONTAINER);
        rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT, data.getPlacement().toString());
        if (data.getActiveIndex() != null) {
            rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX, data.getActiveIndex().toString());
        }
        Collection<UUID> ids = buildNode(rootNode, data);
        cleanUpOrphanedStuff(ids);
        metadata.appendChild(rootNode);
        StringWriter sw = new StringWriter();
        settings.list(new PrintWriter(sw));
        zipEntries.put(Constants.GLOBAL_CONFIG_FILE_PATH, sw.getBuffer().toString().getBytes());
        sw = new StringWriter();
        OutputFormat of = new OutputFormat();
        of.setIndenting(true);
        of.setIndent(4);
        new XMLSerializer(sw, of).serialize(metadata);
        zipEntries.put(Constants.METADATA_FILE_PATH, sw.getBuffer().toString().getBytes());
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
    
    private Collection<UUID> buildNode(Node node, DataCategory data) throws Exception {
        Collection<UUID> ids = new ArrayList<UUID>();
        for (Recognizable item : data.getData()) {
            if (item instanceof DataEntry) {
                DataEntry de = (DataEntry) item;
                String dePath = Constants.DATA_DIR + de.getId().toString() + Constants.DATA_FILE_ENDING;
                byte[] value = de.getData();
                zipEntries.put(dePath, value);
                storeDataEntrySettings(de);
                Element entryNode = metadata.createElement(Constants.XML_ELEMENT_ENTRY);
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ID, de.getId().toString());
                String encodedCaption = URLEncoder.encode(de.getCaption(), Constants.UNICODE_ENCODING);
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION, encodedCaption);
                if (de.getIcon() != null) {
                	String iconPath = ((ImageIcon)de.getIcon()).getDescription();
                	if (!Validator.isNullOrBlank(iconPath)) {
                        entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ICON, iconPath);
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
                	String iconPath = ((ImageIcon)dc.getIcon()).getDescription();
                	if (!Validator.isNullOrBlank(iconPath)) {
                        catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ICON, iconPath);
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
    
    private void setDataEntrySettings(DataEntry dataEntry) {
        if (dataEntry != null) {
            byte[] settings = zipEntries.get(Constants.CONFIG_DIR + dataEntry.getId().toString() + Constants.DATA_ENTRY_CONFIG_FILE_ENDING);
            if (settings == null) {
                settings = getExtensionSettings(dataEntry.getType());
            }
            dataEntry.setSettings(settings);
        }
    }

    private void storeDataEntrySettings(DataEntry dataEntry) throws Exception {
        if (dataEntry != null && dataEntry.getSettings() != null) {
            byte[] defSettings = getExtensionSettings(dataEntry.getType());
            if (!Arrays.equals(defSettings,dataEntry.getSettings())) {
                zipEntries.put(Constants.CONFIG_DIR + dataEntry.getId().toString() + Constants.DATA_ENTRY_CONFIG_FILE_ENDING, 
                        dataEntry.getSettings());
            } else {
                zipEntries.remove(Constants.CONFIG_DIR + dataEntry.getId().toString() + Constants.DATA_ENTRY_CONFIG_FILE_ENDING);
            }
        }
    }

    public Collection<String> getExtensions() {
        Collection<String> extensions = new LinkedHashSet<String>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.EXTENSION_PATTERN)) {
                String extension = 
                    name.substring(0, name.length() - Constants.CLASS_FILE_ENDING.length())
                    .replaceAll(Constants.ZIP_PATH_SEPARATOR, Constants.PACKAGE_PATH_SEPARATOR);
                extensions.add(extension);
            }
        }
        return extensions;
    }

    public byte[] getExtensionSettings(String extension) {
        byte[] settings = null;
        if (!Validator.isNullOrBlank(extension)) {
            String extensionName = extension.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            settings = zipEntries.get(Constants.CONFIG_DIR + extensionName + Constants.EXTENSION_CONFIG_FILE_ENDING);
        }
        return settings;
    }

    public void storeExtensionSettings(String extension, byte[] settings) throws Exception {
        if (!Validator.isNullOrBlank(extension) && settings != null) {
            String extensionName = extension.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            zipEntries.put(Constants.CONFIG_DIR + extensionName + Constants.EXTENSION_CONFIG_FILE_ENDING, settings);
        }
    }

    public void installExtension(File extensionFile) throws Exception {
        Set<String> installedExtNames = new HashSet<String>();
        if (extensionFile != null && extensionFile.exists() && !extensionFile.isDirectory()) {
            String name = extensionFile.getName();
            Map<String, byte[]> classesMap = new HashMap<String, byte[]>();
            Map<String, byte[]> resoursesMap = new HashMap<String, byte[]>();
            Map<String, byte[]> libsMap = new HashMap<String, byte[]>();
            if (name.matches(Constants.ADDON_PACK_PATTERN)) {
                ZipInputStream in = new ZipInputStream(new FileInputStream(extensionFile));
                ZipEntry ze = null;
                while ((ze = in.getNextEntry()) != null) {
                    String zeName = ze.getName();
                    ByteArrayOutputStream out = null;
                    int type = 0;
                    if (zeName.matches(Constants.ADDON_CLASS_FILE_PATH_PATTERN)) {
                        type = 1;
                    } else if (zeName.matches(Constants.RESOURCE_FILE_PATH_PATTERN)) {
                        type = 2;
                    } else if (zeName.matches(Constants.LIB_FILE_PATH_PATTERN)) {
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
                            String shortName = zeName.replaceFirst("^.*/", Constants.EMPTY_STR)
                                                     .replaceFirst("\\.class$", Constants.EMPTY_STR)
                                                     .replaceFirst("\\$.*$", Constants.EMPTY_STR);
                            installedExtNames.add(shortName);
                            classesMap.put(zeName, out.toByteArray());
                        } else if (type == 2) {
                            resoursesMap.put(zeName, out.toByteArray());
                        } else if (type == 3) {
                            ZipInputStream lin = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
                            ZipEntry lze;
                            while ((lze = lin.getNextEntry()) != null) {
                                String lzeName = lze.getName();
                                if (!lzeName.startsWith(Constants.META_INF_DIR) && !lzeName.endsWith("/")) {
                                    if (zipEntries.containsKey(lzeName)) {
                                        throw new Exception(
                                                "Can not install from file '" + extensionFile + "'\n" +
                                                "Extension pack conflict! Conflicting resource: " + lzeName);
                                    } else {
                                        ByteArrayOutputStream lout = new ByteArrayOutputStream();
                                        while ((b = lin.read()) != -1) {
                                            lout.write(b);
                                        }
                                        lout.close();
                                        libsMap.put(lzeName, lout.toByteArray());
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
                throw new Exception("Wrong extension pack: nothing to install!");
            } else if (installedExtNames.size() > 1) {
                throw new Exception("Wrong extension pack: only one extension per pack allowed!");
            } else {
                String extName = installedExtNames.iterator().next();
                String fullExtName = Constants.EXTENSION_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                            + extName + Constants.PACKAGE_PATH_SEPARATOR + extName;
                if (getExtensions().contains(fullExtName)) {
                    throw new Exception("Can not install extension pack: duplicate extension name!");
                } else {
                    for (Entry<String, byte[]> entry : resoursesMap.entrySet()) {
                        zipEntries.put(Constants.RESOURCES_DIR + extName + Constants.ZIP_PATH_SEPARATOR 
                                        + entry.getKey().replaceFirst(Constants.RESOURCE_FILE_PREFIX_PATTERN, Constants.EMPTY_STR), 
                                        entry.getValue());
                    }
                    for (Entry<String, byte[]> entry : classesMap.entrySet()) {
                        zipEntries.put(Constants.EXTENSION_DIR_PATH + Constants.ZIP_PATH_SEPARATOR 
                                + extName + Constants.ZIP_PATH_SEPARATOR 
                                + entry.getKey().replaceFirst(Constants.ADDON_CLASS_FILE_PREFIX_PATTERN, Constants.EMPTY_STR), 
                                entry.getValue());
                    }
                    if (!libsMap.isEmpty()) {
                        String extensionInstLogEntryPath = Constants.CONFIG_DIR + extName + Constants.EXT_LIB_INSTALL_LOG_FILE_ENDING;
                        StringBuffer sb = new StringBuffer();
                        for (Entry<String, byte[]> entry : libsMap.entrySet()) {
                            zipEntries.put(entry.getKey(), entry.getValue());
                            sb.append(entry.getKey() + Constants.NEW_LINE);
                        }
                        zipEntries.put(extensionInstLogEntryPath, sb.toString().getBytes());
                    }
                }
            }
        } else {
            throw new Exception("Invalid extension pack!");
        }
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
        String extensionInstLogEntryPath = Constants.CONFIG_DIR + extensionName + Constants.EXT_LIB_INSTALL_LOG_FILE_ENDING;
        byte[] bytes = zipEntries.get(extensionInstLogEntryPath);
        if (bytes != null) {
            String[] installEntries = new String(bytes).split(Constants.NEW_LINE);
            for (String installEntry : installEntries) {
                removeKeys.add(installEntry);
            }
        }
        if (!removeKeys.isEmpty()) {
            for (String key : removeKeys) {
                zipEntries.remove(key);
            }
        }
        zipEntries.remove(extensionInstLogEntryPath);
        String extensionConfigPath = Constants.CONFIG_DIR + extensionName + Constants.EXTENSION_CONFIG_FILE_ENDING;
        zipEntries.remove(extensionConfigPath);
    }
        
    public Map<String, byte[]> getLAFs() {
        Map<String, byte[]> lafs = new LinkedHashMap<String, byte[]>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.LAF_PATTERN)) {
                String laf = 
                    name.substring(0, name.length() - Constants.CLASS_FILE_ENDING.length())
                    .replaceAll(Constants.ZIP_PATH_SEPARATOR, Constants.PACKAGE_PATH_SEPARATOR);
                byte[] settings = getLAFSettings(laf);
                lafs.put(laf, settings);
            }
        }
        return lafs;
    }
    
    public byte[] getLAFSettings(String laf) {
        byte[] settings = null;
        if (!Validator.isNullOrBlank(laf)) {
            String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            settings = zipEntries.get(Constants.CONFIG_DIR + lafName + Constants.LAF_CONFIG_FILE_ENDING);
        }
        return settings;
    }

    public void storeLAFSettings(String laf, byte[] settings) throws Exception {
        if (!Validator.isNullOrBlank(laf) && settings != null) {
            String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
            zipEntries.put(Constants.CONFIG_DIR + lafName + Constants.LAF_CONFIG_FILE_ENDING, settings);
        }
    }

    public void installLAF(File lafFile) throws Exception {
        Set<String> installedLAFNames = new HashSet<String>();
        if (lafFile != null && lafFile.exists() && !lafFile.isDirectory()) {
            String name = lafFile.getName();
            Map<String, byte[]> classesMap = new HashMap<String, byte[]>();
            Map<String, byte[]> libsMap = new HashMap<String, byte[]>();
            if (name.matches(Constants.ADDON_PACK_PATTERN)) {
                ZipInputStream in = new ZipInputStream(new FileInputStream(lafFile));
                ZipEntry ze = null;
                while ((ze = in.getNextEntry()) != null) {
                    String zeName = ze.getName();
                    ByteArrayOutputStream out = null;
                    int type = 0;
                    if (zeName.matches(Constants.ADDON_CLASS_FILE_PATH_PATTERN)) {
                        type = 1;
                    } else if (zeName.matches(Constants.LIB_FILE_PATH_PATTERN)) {
                        type = 2;
                    }
                    if (type != 0) {
                        out = new ByteArrayOutputStream();
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        out.close();
                        if (type == 1) {
                            String shortName = zeName.replaceFirst("^.*/", Constants.EMPTY_STR)
                                                     .replaceFirst("\\.class$", Constants.EMPTY_STR)
                                                     .replaceFirst("\\$.*$", Constants.EMPTY_STR);
                            installedLAFNames.add(shortName);
                            classesMap.put(zeName, out.toByteArray());
                        } else if (type == 2) {
                            ZipInputStream lin = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()));
                            ZipEntry lze;
                            while ((lze = lin.getNextEntry()) != null) {
                                String lzeName = lze.getName();
                                if (!lzeName.startsWith(Constants.META_INF_DIR) && !lzeName.endsWith("/")) {
                                    if (zipEntries.containsKey(lzeName)) {
                                        throw new Exception(
                                                "Can not install from file '" + lafFile + "'\n" +
                                                "LAF pack conflict! Conflicting resource: " + lzeName);
                                    } else {
                                        ByteArrayOutputStream lout = new ByteArrayOutputStream();
                                        while ((b = lin.read()) != -1) {
                                            lout.write(b);
                                        }
                                        lout.close();
                                        libsMap.put(lzeName, lout.toByteArray());
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
                throw new Exception("Wrong LAF pack: nothing to install!");
            } else if (installedLAFNames.size() > 1) {
                throw new Exception("Wrong LAF pack: only one LAF activator per pack allowed!");
            } else {
                String lafName = installedLAFNames.iterator().next();
                String fullLAFName = Constants.LAF_DIR_PACKAGE_NAME + Constants.PACKAGE_PATH_SEPARATOR 
                                            + lafName + Constants.PACKAGE_PATH_SEPARATOR + lafName;
                if (getLAFs().keySet().contains(fullLAFName)) {
                    throw new Exception("Can not install LAF pack: duplicate LAF Manager name!");
                } else {
                    for (Entry<String, byte[]> entry : classesMap.entrySet()) {
                        zipEntries.put(Constants.LAF_DIR_PATH + Constants.ZIP_PATH_SEPARATOR 
                                + lafName + Constants.ZIP_PATH_SEPARATOR 
                                + entry.getKey().replaceFirst(Constants.ADDON_CLASS_FILE_PREFIX_PATTERN, Constants.EMPTY_STR), 
                                entry.getValue());
                    }
                    if (!libsMap.isEmpty()) {
                        String lafInstLogEntryPath = Constants.CONFIG_DIR + lafName + Constants.LAF_LIB_INSTALL_LOG_FILE_ENDING;
                        StringBuffer sb = new StringBuffer();
                        for (Entry<String, byte[]> entry : libsMap.entrySet()) {
                            zipEntries.put(entry.getKey(), entry.getValue());
                            sb.append(entry.getKey() + Constants.NEW_LINE);
                        }
                        zipEntries.put(lafInstLogEntryPath, sb.toString().getBytes());
                    }
                }
            }
        } else {
            throw new Exception("Invalid LAF pack!");
        }
    }
    
    public void uninstallLAF(String laf) throws Exception {
        String lafName = laf.replaceAll(Constants.PACKAGE_PREFIX_PATTERN, Constants.EMPTY_STR);
        String lafPath = laf.replaceAll(Constants.PACKAGE_PATH_SEPARATOR_PATTERN, Constants.ZIP_PATH_SEPARATOR);
        lafPath = lafPath.substring(0, lafPath.lastIndexOf(Constants.ZIP_PATH_SEPARATOR));
        Collection<String> removeKeys = new HashSet<String>();
        for (String key : zipEntries.keySet()) {
            if (key.startsWith(lafPath)) {
                removeKeys.add(key);
            }
        }
        String lafInstLogEntryPath = Constants.CONFIG_DIR + lafName + Constants.LAF_LIB_INSTALL_LOG_FILE_ENDING;
        byte[] bytes = zipEntries.get(lafInstLogEntryPath);
        if (bytes != null) {
            String[] installEntries = new String(bytes).split(Constants.NEW_LINE);
            for (String installEntry : installEntries) {
                removeKeys.add(installEntry);
            }
        }
        if (!removeKeys.isEmpty()) {
            for (String key : removeKeys) {
                zipEntries.remove(key);
            }
        }
        zipEntries.remove(lafInstLogEntryPath);
        String lafConfigPath = Constants.CONFIG_DIR + lafName + Constants.LAF_CONFIG_FILE_ENDING;
        zipEntries.remove(lafConfigPath);
    }
        
    public Collection<ImageIcon> getIcons() {
        Collection<ImageIcon> icons = new LinkedHashSet<ImageIcon>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.ICON_FILE_PATH_PATTERN)) {
            	String id = name.replaceFirst(Constants.ICONS_DIR_PATTERN, Constants.EMPTY_STR);
            	id = id.substring(0, id.length() - Constants.ICON_FILE_ENDING.length());
                ImageIcon icon = new ImageIcon(zipEntries.get(name), id);
                icons.add(icon);
            }
        }
        return icons;
    }
    
    public Collection<ImageIcon> addIcons(File file) throws Exception {
        Collection<ImageIcon> icons = new LinkedList<ImageIcon>();
        if (file != null && file.exists() && !file.isDirectory()) {
            if (file.getName().matches(Constants.ADDON_PACK_PATTERN)) {
                ZipInputStream in = new ZipInputStream(new FileInputStream(file));
                while (in.getNextEntry() != null) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    int b;
                    while ((b = in.read()) != -1) {
                        out.write(b);
                    }
                    out.close();
                    ImageIcon icon = addIcon(new ByteArrayInputStream(out.toByteArray()));
                    if (icon != null) {
                        icons.add(icon);
                    }
                }
            } else {
                ImageIcon icon = addIcon(new FileInputStream(file));
                if (icon != null) {
                    icons.add(icon);
                }
            }
        } else {
            throw new Exception("Invalid icon file/pack!");
        }
        return icons;
    }
    
    private ImageIcon addIcon(InputStream is) throws IOException {
        ImageIcon icon = null;
        BufferedImage image = ImageIO.read(is);
        if (image != null) {
            icon = new ImageIcon(image);
            if (icon != null) {
                String id = UUID.randomUUID().toString();
                icon.setDescription(id);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, Constants.ICON_FORMAT, baos);
                zipEntries.put(Constants.ICONS_DIR + id + Constants.ICON_FILE_ENDING, baos.toByteArray());
            }
        }
        return icon;
    }

    public void removeIcon(ImageIcon icon) throws Exception {
        zipEntries.remove(Constants.ICONS_DIR + icon.getDescription() + Constants.ICON_FILE_ENDING);
    }
    
    public Collection<Attachment> getAttachments(UUID dataEntryID) {
        Collection<Attachment> atts = new LinkedList<Attachment>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.ATTACHMENT_FILE_PATH_PATTERN)
                    && name.startsWith(Constants.ATTACHMENTS_DIR + dataEntryID.toString())) {
                byte[] attData = zipEntries.get(name);
                String attName;
                int idx = name.lastIndexOf(Constants.ZIP_PATH_SEPARATOR);
            	attName = name.substring(idx+1, name.length());
                Attachment att = new Attachment(attName, attData);
                atts.add(att);
            }
        }
        return atts;
    }
    
    public void addAttachment(UUID dataEntryID, Attachment attachment) throws Exception {
        if (dataEntryID != null && attachment != null) {
            String attPath = Constants.ATTACHMENTS_DIR + dataEntryID + Constants.ZIP_PATH_SEPARATOR + attachment.getName();
            if (zipEntries.get(attPath) != null) {
            	throw new Exception("Duplicate attachment name!");
            }
            zipEntries.put(attPath, attachment.getData());
        } else {
            throw new Exception("Invalid parameters!");
        }
    }

    public void removeAttachment(UUID dataEntryID, String attachmentName) {
        String attPath = Constants.ATTACHMENTS_DIR + dataEntryID + Constants.ZIP_PATH_SEPARATOR + attachmentName;
        zipEntries.remove(attPath);
    }
    
    public void removeAttachments(UUID dataEntryID) {
        String attsPath = Constants.ATTACHMENTS_DIR + dataEntryID;
        Collection<String> removeKeys = new HashSet<String>();
        for (String path : zipEntries.keySet()) {
            if (path.startsWith(attsPath)) {
                removeKeys.add(path);
            }
        }
        if (!removeKeys.isEmpty()) {
            for (String key : removeKeys) {
                zipEntries.remove(key);
            }
        }
    }
    
    private void cleanUpOrphanedStuff(Collection<UUID> ids) {
        Collection<UUID> foundAttIds = new ArrayList<UUID>();
        Collection<UUID> foundConfIds = new ArrayList<UUID>();
        Pattern p1 = Pattern.compile(Constants.ATTACHMENT_FILE_PATH_PATTERN);
        Pattern p2 = Pattern.compile(Constants.CONFIG_FILE_PATH_PATTERN);
        for (String path : zipEntries.keySet()) {
            Matcher m = p1.matcher(path);
            if (m.find()) {
                try {
                    UUID id = UUID.fromString(m.group(1));
                    foundAttIds.add(id);
                } catch (IllegalArgumentException iae) {}
            }
            m = p2.matcher(path);
            if (m.find()) {
                try {
                    UUID id = UUID.fromString(m.group(1));
                    foundConfIds.add(id);
                } catch (IllegalArgumentException iae) {}
            }
        }
        for (UUID id : foundAttIds) {
            if (!ids.contains(id)) {
                // orphaned attachments found, remove 'em
                removeAttachments(id);
            }
        }
        for (UUID id : foundConfIds) {
            if (!ids.contains(id)) {
                // orphaned config found, remove it
                zipEntries.remove(Constants.CONFIG_DIR + id.toString() + Constants.DATA_ENTRY_CONFIG_FILE_ENDING);
            }
        }
    }
    
    public DataCategory getData() {
        return data;
    }

    public void setData(DataCategory data) {
        this.data = data;
    }

    public Properties getSettings() {
        return settings;
    }

    public void setSettings(Properties settings) {
        this.settings = settings;
    }

}
