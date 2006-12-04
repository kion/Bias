/**
 * Created on Oct 15, 2006
 */
package bias.core;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
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

import bias.global.Constants;
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
    
    private File jarFile = null;

    private Map<String, byte[]> zipEntries;
    
    private Map<String, DataEntry> numberedData;
    
    private DataCategory data;
    
    private Document metadata;
    
    private Properties properties;
    
    public void load() throws Exception {
        if (jarFile == null) {
            init();
        }
        zipEntries = new LinkedHashMap<String, byte[]>();
        numberedData = new LinkedHashMap<String, DataEntry>();
        properties = new Properties();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while ((c = zis.read()) != -1) {
                out.write(c);
            }
            String name = ze.getName();
            if (name.matches(Constants.DATA_FILE_PATTERN)) {
            	String entryNumericPath = ze.getName()
			            	.replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
			            	.replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR);
                DataEntry de = new DataEntry();
                de.setData(out.toByteArray());
                numberedData.put(entryNumericPath, de);
            } else if (name.equals(Constants.METADATA_FILE_PATH)) {
                if (out.size() != 0) {
                    metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(
                            new ByteArrayInputStream(out.toByteArray()));
                }
            } else if (name.equals(Constants.CONFIG_FILE_PATH)) {
                properties.load(
                        new ByteArrayInputStream(out.toByteArray()));
            } else {
                zipEntries.put(ze.getName(), out.toByteArray());
            }
        }
        zis.close();
        data = parseMetadata(metadata, numberedData, null);
    }
    
    public Collection<String> getExtensions() {
        Collection<String> extensions = new LinkedHashSet<String>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.VISUAL_COMPONENT_FILE_PATTERN)
                    && !name.matches(Constants.VISUAL_COMPONENT_SKIP_FILE_PATH)) {
                String extension = 
                    name.substring(0, name.length() - Constants.VISUAL_COMPONENT_FILE_ENDING.length())
                    .replaceAll(Constants.ZIP_PATH_SEPARATOR, Constants.PACKAGE_PATH_SEPARATOR);
                extensions.add(extension);
            }
        }
        return extensions;
    }

    public boolean installExtension(File extensionFile) throws Exception {
        boolean installed = false;
        if (extensionFile != null && extensionFile.exists() && !extensionFile.isDirectory()) {
            String name = extensionFile.getName();
            if (name.matches(Constants.ARCHIVE_FILE_PATTHERN)) {
                ZipInputStream in = new ZipInputStream(new FileInputStream(extensionFile));
                ZipEntry ze = null;
                while ((ze = in.getNextEntry()) != null) {
                    String zeName = ze.getName();
                    if (zeName.endsWith(Constants.VISUAL_COMPONENT_FILE_ENDING)
                            && !zeName.matches(Constants.VISUAL_COMPONENT_SKIP_FILE_NAME)) {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        int b;
                        while ((b = in.read()) != -1) {
                            out.write(b);
                        }
                        out.flush();
                        out.close();
                        zipEntries.put(Constants.VISUAL_COMPONENT_DIR_PATH + Constants.ZIP_PATH_SEPARATOR + zeName, out.toByteArray());
                        installed = true;
                    }
                }
                in.close();
            } else if (name.endsWith(Constants.VISUAL_COMPONENT_FILE_ENDING)
                    && !name.matches(Constants.VISUAL_COMPONENT_SKIP_FILE_NAME)) {
                BufferedInputStream in = new BufferedInputStream(new FileInputStream(extensionFile));
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int b;
                while ((b = in.read()) != -1) {
                    out.write(b);
                }
                out.flush();
                out.close();
                in.close();
                zipEntries.put(Constants.VISUAL_COMPONENT_DIR_PATH + Constants.ZIP_PATH_SEPARATOR + name, out.toByteArray());
                installed = true;
            }
        }
        return installed;
    }
    
    public boolean uninstallExtension(String extension) throws Exception {
    	boolean uninstalled = false;
        Collection<String> removeKeys = new HashSet<String>();
        for (String key : zipEntries.keySet()) {
            if (key.matches(extension + Constants.ANY_CHARACTERS_PATTHERN)) {
                removeKeys.add(key);
            }
        }
        if (!removeKeys.isEmpty()) {
            for (String key : removeKeys) {
                zipEntries.remove(key);
            }
            uninstalled = true;
        }
        return uninstalled;
    }
        
    public Collection<ImageIcon> getIcons() {
        Collection<ImageIcon> icons = new LinkedHashSet<ImageIcon>();
        for (String name : zipEntries.keySet()) {
            if (name.matches(Constants.ICON_FILE_PATTERN)) {
                ImageIcon icon = new ImageIcon(zipEntries.get(name), name);
                icons.add(icon);
            }
        }
        return icons;
    }
    
    public boolean addIcon(ImageIcon icon) throws Exception {
    	boolean added = false;
    	if (icon != null) {
        	if (Validator.isNullOrBlank(icon.getDescription())){
        		String fileName = "" + (getIcons().size() + 1) + Constants.ICON_FILE_ENDING;
        		BufferedImage image = (BufferedImage) icon.getImage();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, Constants.ICON_FORMAT, baos);
        		zipEntries.put(Constants.ICONS_DIR + Constants.ZIP_PATH_SEPARATOR + fileName, baos.toByteArray());
        		added = true;
        	}
    	}
    	return added;
    }

    public boolean removeIcon(ImageIcon icon) throws Exception {
    	boolean removed = false;
        Collection<ImageIcon> currentIcons = getIcons();
    	for (ImageIcon ic : currentIcons) {
    		String name = icon.getDescription();
    		if (ic.getDescription().equals(name)) {
    			zipEntries.remove(Constants.ICONS_DIR + Constants.ZIP_PATH_SEPARATOR + name);
        		removed = true;
    			break;
    		}
    	}
    	return removed;
    }
    
    public DataCategory importData(File jarFile, Collection<UUID> existingIDs) throws Exception {
        Map<String,DataEntry> importedNumberedData = new LinkedHashMap<String, DataEntry>();
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
                String entryNumericPath = ze.getName()
                            .replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
                            .replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR);
                DataEntry de = new DataEntry();
                de.setData(out.toByteArray());
                importedNumberedData.put(entryNumericPath, de);
            } else if (ze.getName().equals(Constants.METADATA_FILE_PATH)) {
                if (out.size() != 0) {
                    metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(
                            new ByteArrayInputStream(out.toByteArray()));
                }
            }    
        }
        zis.close();
        DataCategory importedData = parseMetadata(metadata, importedNumberedData, existingIDs);
        data.addDataItems(importedData.getData());
        return importedData;
    }
    
    private DataCategory parseMetadata(
    		Document metadata, 
    		Map<String, DataEntry> numberedData,
    		Collection<UUID> existingIDs) throws Exception {
        DataCategory data = new DataCategory();
        if (metadata == null) {
            metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        } else {
            buildData(Constants.EMPTY_STR, data, metadata.getFirstChild(), numberedData, existingIDs);
            data.setPlacement(Integer.valueOf(metadata.getFirstChild().getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT).getNodeValue()));
        }
        return data;
    }
    
    private void buildData(String path, DataCategory data, Node node, Map<String, DataEntry> numberedData, Collection<UUID> existingIDs) throws Exception {
        if (node.getNodeName().equals(Constants.XML_ELEMENT_ROOT_CONTAINER)) {
            NodeList nodes = node.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node n = nodes.item(i);
                buildData(path, data, n, numberedData, existingIDs);
            }
        } else if (node.getNodeName().equals(Constants.XML_ELEMENT_CATEGORY)) {
            NamedNodeMap attributes = node.getAttributes();
            Node attCatNumber = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_NUMBER);
            Integer catNumber = Integer.valueOf(attCatNumber.getNodeValue());
            DataCategory dc = new DataCategory();
            Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
            UUID id = UUID.fromString(attID.getNodeValue());
            if (existingIDs == null || !existingIDs.contains(id)) {
                dc.setId(id);
                Node attCaption = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION);
                String caption = attCaption.getNodeValue();
                caption = URLDecoder.decode(caption, Constants.UNICODE_ENCODING);
                dc.setCaption(caption);
                Node attPlacement = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT);
                Integer placement = Integer.valueOf(attPlacement.getNodeValue());
                dc.setPlacement(placement);
                Node attActiveIdx = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX);
                if (attActiveIdx != null) {
                    Integer activeIdx = Integer.valueOf(attActiveIdx.getNodeValue());
                    dc.setActiveIndex(activeIdx);
                }
                data.addDataItem(dc);
                String catPath = path + catNumber + "/";
                NodeList nodes = node.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Node n = nodes.item(i);
                    buildData(catPath, dc, n, numberedData, existingIDs);
                }
            }
            path += catNumber + "/";
        } else if (node.getNodeName().equals(Constants.XML_ELEMENT_ENTRY)) {
            NamedNodeMap attributes = node.getAttributes();
            Node attEntNumber = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_NUMBER);
            Integer entNumber = Integer.valueOf(attEntNumber.getNodeValue());
            String dePath = path + entNumber;
            DataEntry dataEntry = numberedData.get(dePath);
            Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
            UUID id = UUID.fromString(attID.getNodeValue());
            if (existingIDs == null || !existingIDs.contains(id)) {
                dataEntry.setId(id);
                Node attCaption = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION);
                String caption = attCaption.getNodeValue();
                caption = URLDecoder.decode(caption, Constants.UNICODE_ENCODING);
                dataEntry.setCaption(caption);
                Node attType = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_TYPE);
                String type = attType.getNodeValue();
                dataEntry.setType(type);
                data.addDataItem(dataEntry);
            }
        }
    }
    
    public void store() throws Exception {
        if (jarFile == null) {
            init();
        }
        metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Element rootNode = metadata.createElement(Constants.XML_ELEMENT_ROOT_CONTAINER);
        rootNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT, data.getPlacement().toString());
        buildNode(Constants.DATA_DIR_PATTERN, rootNode, data);
        metadata.appendChild(rootNode);
        StringWriter sw = new StringWriter();
        properties.list(new PrintWriter(sw));
        zipEntries.put(Constants.CONFIG_FILE_PATH, sw.getBuffer().toString().getBytes());
        sw = new StringWriter();
        OutputFormat of = new OutputFormat();
        of.setIndenting(true);
        of.setIndent(4);
        new XMLSerializer(sw, of).serialize(metadata);
        zipEntries.put(Constants.METADATA_FILE_PATH, sw.getBuffer().toString().getBytes());
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jarFile));
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
    
    private void buildNode(String path, Node node, DataCategory data) throws Exception {
        int dcNumber = 1;
        int entNumber = 1;
        for (Recognizable item : data.getData()) {
            if (item instanceof DataEntry) {
                DataEntry de = (DataEntry) item;
                String dePath = path + entNumber + Constants.DATA_FILE_ENDING;
                byte[] value = de.getData();
                zipEntries.put(dePath, value);
                Element entryNode = metadata.createElement(Constants.XML_ELEMENT_ENTRY);
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_NUMBER, "" + entNumber);
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ID, de.getId().toString());
                String encodedCaption = URLEncoder.encode(de.getCaption(), Constants.UNICODE_ENCODING);
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION, encodedCaption);
                entryNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_TYPE, de.getType());
                node.appendChild(entryNode);
                entNumber++;
            } else if (item instanceof DataCategory) {
                DataCategory dc = (DataCategory) item;
                String dataCatPath = path + dcNumber + "/";
                Element catNode = metadata.createElement(Constants.XML_ELEMENT_CATEGORY);
                catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_NUMBER, "" + dcNumber);
                catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ID, dc.getId().toString());
                String encodedCaption = URLEncoder.encode(dc.getCaption(), Constants.UNICODE_ENCODING);
                catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_CAPTION, encodedCaption);
                catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_PLACEMENT, dc.getPlacement().toString());
                if (dc.getActiveIndex() != null) {
                    catNode.setAttribute(Constants.XML_ELEMENT_ATTRIBUTE_ACTIVE_IDX, dc.getActiveIndex().toString());
                }
                buildNode(dataCatPath, catNode, dc);
                node.appendChild(catNode);
                dcNumber++;
            }
        }
        path += dcNumber + "/";
    }
    
    private void init() {
        URL url = BackEnd.class.getResource(BackEnd.class.getSimpleName()+".class");
        String jarFilePath = url.getFile().substring(0, url.getFile().indexOf(BackEnd.class.getName().replaceAll("\\.", "/")) - 2);
        jarFilePath = jarFilePath.substring("file:".length(), jarFilePath.length());
        jarFile = new File(jarFilePath);
        // TODO: remove debug code
//        jarFile = new File("/mnt/stor/devel/Bias/build/bias.jar");
    }

    public DataCategory getData() {
        return data;
    }

    public void setData(DataCategory data) {
        this.data = data;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
