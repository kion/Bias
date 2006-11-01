/**
 * Created on Oct 15, 2006
 */
package bias.core;

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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bias.global.Constants;

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
    
    private Map<Integer, Map<Integer, DataEntry>> numberedData;
    
    private Collection<DataCategory> data;
    
    private Document metadata;
    
    private Properties properties;
    
    public void load() throws Exception {
        if (jarFile == null) {
            init();
        }
        zipEntries = new LinkedHashMap<String, byte[]>();
        numberedData = new LinkedHashMap<Integer, Map<Integer,DataEntry>>();
        properties = new Properties();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int c;
            while ((c = zis.read()) != -1) {
                out.write(c);
            }
            if (ze.getName().matches(Constants.DATA_FILE_PATTERN)) {
            	String numbers[] = ze.getName()
			            	.replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
			            	.replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR).split("/");
                Integer catNumber = Integer.valueOf(numbers[0]);
                Integer entNumber = Integer.valueOf(numbers[1]);
                DataEntry de = new DataEntry();
                de.setData(out.toByteArray());
                Map<Integer, DataEntry> numberedEntries = numberedData.get(catNumber);
                if (numberedEntries == null) {
                	numberedEntries = new LinkedHashMap<Integer, DataEntry>();
                	numberedData.put(catNumber, numberedEntries);
                }
                numberedEntries.put(entNumber, de);
            } else if (ze.getName().equals(Constants.METADATA_FILE_PATH)) {
                if (out.size() != 0) {
                    metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(
                            new ByteArrayInputStream(out.toByteArray()));
                }
            } else if (ze.getName().equals(Constants.CONFIG_FILE_PATH)) {
                properties.load(
                        new ByteArrayInputStream(out.toByteArray()));
            } else {
                zipEntries.put(ze.getName(), out.toByteArray());
            }
        }
        zis.close();
        data = parseMetadata(metadata, numberedData, true);
    }
    
    public Collection<DataCategory> importData(File jarFile) throws Exception {
        Map<Integer, Map<Integer,DataEntry>> importedNumberedData = new LinkedHashMap<Integer, Map<Integer,DataEntry>>();
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
            	String numbers[] = ze.getName()
			            	.replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
			            	.replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR).split("/");
			    Integer catNumber = Integer.valueOf(numbers[0]);
			    Integer entNumber = Integer.valueOf(numbers[1]);
                DataEntry de = new DataEntry();
                de.setData(out.toByteArray());
                Map<Integer, DataEntry> numberedEntries = importedNumberedData.get(catNumber);
                if (numberedEntries == null) {
                	numberedEntries = new LinkedHashMap<Integer, DataEntry>();
                	importedNumberedData.put(catNumber, numberedEntries);
                }
                numberedEntries.put(entNumber, de);
            } else if (ze.getName().equals(Constants.METADATA_FILE_PATH)) {
                if (out.size() != 0) {
                    metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(
                            new ByteArrayInputStream(out.toByteArray()));
                }
            }    
        }
        zis.close();
        Collection<DataCategory> importedData = parseMetadata(metadata, importedNumberedData, false);
        data.addAll(importedData);
        return importedData;
    }
    
    private Collection<DataCategory> parseMetadata(
    		Document metadata, 
    		Map<Integer, Map<Integer,DataEntry>> numberedData,
    		boolean parseIDs) throws Exception {
        Collection<DataCategory> dataCategories = new LinkedHashSet<DataCategory>();
        if (metadata == null) {
            metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        } else {
            NodeList categories = metadata.getElementsByTagName("category");
            for (int i = 0; i < categories.getLength(); i++){
            	// category
            	Node category = categories.item(i);
                NamedNodeMap attributes = category.getAttributes();
                Node attCatNumber = attributes.getNamedItem("number");
                Integer catNumber = Integer.valueOf(attCatNumber.getNodeValue());
                Node attCaption = attributes.getNamedItem("caption");
                String caption = attCaption.getNodeValue();
                caption = URLDecoder.decode(caption, Constants.UNICODE_ENCODING);
                DataCategory dataCategory = new DataCategory();
                dataCategory.setCaption(caption);
                // category's entries
                NodeList entries = category.getChildNodes();
                for (int j = 0; j < entries.getLength(); j++){
                    Node entry = entries.item(j);
                    attributes = entry.getAttributes();
                    Node attEntNumber = attributes.getNamedItem("number");
                    Integer entNumber = Integer.valueOf(attEntNumber.getNodeValue());
                    DataEntry dataEntry = numberedData.get(catNumber).get(entNumber);
                    if (parseIDs) {
                        Node attID = attributes.getNamedItem("id");
                        UUID id = UUID.fromString(attID.getNodeValue());
                        dataEntry.setId(id);
                    }
                    attCaption = attributes.getNamedItem("caption");
                    caption = attCaption.getNodeValue();
                    caption = URLDecoder.decode(caption, Constants.UNICODE_ENCODING);
                    dataEntry.setCaption(caption);
                    Node attType = attributes.getNamedItem("type");
                    String type = attType.getNodeValue();
                    dataEntry.setType(type);
                }
                Map<Integer, DataEntry> numberedEntries = numberedData.get(catNumber);
                if (numberedEntries != null) {
                    dataCategory.setDataEntries(numberedEntries.values());
                }
                dataCategories.add(dataCategory);
            }
        }
        return dataCategories;
    }
    
    public void store() throws Exception {
        if (jarFile == null) {
            init();
        }
        metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Node rootNode = metadata.createElement("metadata");
        int catNumber = 0;
        for (DataCategory dataCategory : data) {
            String catKey = Constants.DATA_DIR + ++catNumber + "/";
            Element catNode = metadata.createElement("category");
            catNode.setAttribute("number", "" + catNumber);
            String encodedCaption = URLEncoder.encode(dataCategory.getCaption(), Constants.UNICODE_ENCODING);
            catNode.setAttribute("caption", encodedCaption);
            int entNumber = 0;
            for (DataEntry dataEntry : dataCategory.getDataEntries()) {
                String entKey = catKey + ++entNumber + Constants.DATA_FILE_ENDING;
                byte[] value = dataEntry.getData();
                zipEntries.put(entKey, value);
                Element entryNode = metadata.createElement("entry");
                entryNode.setAttribute("number", "" + entNumber);
                entryNode.setAttribute("id", dataEntry.getId().toString());
                encodedCaption = URLEncoder.encode(dataEntry.getCaption(), Constants.UNICODE_ENCODING);
                entryNode.setAttribute("caption", encodedCaption);
                entryNode.setAttribute("type", dataEntry.getType());
                catNode.appendChild(entryNode);
            }
            rootNode.appendChild(catNode);
        }
        metadata.appendChild(rootNode);
        StringWriter sw = new StringWriter();
        properties.list(new PrintWriter(sw));
        zipEntries.put(Constants.CONFIG_FILE_PATH, sw.getBuffer().toString().getBytes());
        sw = new StringWriter();
        new XMLSerializer(sw, new OutputFormat()).serialize(metadata);
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
    
    private void init() {
        URL url = BackEnd.class.getResource(BackEnd.class.getSimpleName()+".class");
        String jarFilePath = url.getFile().substring(0, url.getFile().indexOf(BackEnd.class.getName().replaceAll("\\.", "/")) - 2);
        jarFilePath = jarFilePath.substring("file:".length(), jarFilePath.length());
        jarFile = new File(jarFilePath);
        // TODO: remove debug code
//        jarFile = new File("/mnt/stor/devel/Bias/build/bias.jar");
    }

    public Collection<DataCategory> getData() {
        return data;
    }

    public void setData(Collection<DataCategory> data) {
        this.data = data;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

}
