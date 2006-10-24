/**
 * Created on Oct 15, 2006
 */
package bias.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
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
    
    private static File jarFile = null;

    private static Map<String, String> zipEntries;
    
    private static Map<Integer, DataEntry> numberedDataEntries;
    
    private static Collection<DataEntry> data;
    
    private static Document metadata;
    
    private static Properties properties;
    
    public static void load() throws Exception {
        if (jarFile == null) {
            init();
        }
        zipEntries = new LinkedHashMap<String, String>();
        numberedDataEntries = new LinkedHashMap<Integer, DataEntry>();
        properties = new Properties();
        ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry ze = null;
        while ((ze = zis.getNextEntry()) != null) {
            StringWriter sw = new StringWriter();
            int c;
            while ((c = zis.read()) != -1) {
                sw.write(c);
            }
            if (ze.getName().matches(Constants.DATA_FILE_PATTERN)) {
                Integer number = Integer.valueOf(ze.getName()
                        .replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
                        .replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR));
                DataEntry de = new DataEntry();
                de.setData(sw.getBuffer().toString());
                numberedDataEntries.put(number, de);
            } else if (ze.getName().equals(Constants.METADATA_FILE_PATH)) {
                metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(
                        new ByteArrayInputStream(sw.getBuffer().toString().getBytes()));
            } else if (ze.getName().equals(Constants.CONFIG_FILE_PATH)) {
                properties.load(new ByteArrayInputStream(sw.getBuffer().toString().getBytes()));
            } else {
                zipEntries.put(ze.getName(), sw.getBuffer().toString());
            }
        }
        zis.close();
        NodeList entries = metadata.getElementsByTagName("entry");
        for (int i = 0; i < entries.getLength(); i++){
            Node entry = entries.item(i);
            NamedNodeMap attributes = entry.getAttributes();
            Node attNumber = attributes.getNamedItem("number");
            Integer number = Integer.valueOf(attNumber.getNodeValue());
            Node attCaption = attributes.getNamedItem("caption");
            String caption = attCaption.getNodeValue();
            Node attType = attributes.getNamedItem("type");
            String type = attType.getNodeValue();
            DataEntry dataEntry = numberedDataEntries.get(number);
            dataEntry.setCaption(caption);
            dataEntry.setType(type);
        }
        data = numberedDataEntries.values();
    }
    
    public static Collection<DataEntry> importData(File jarFile) throws Exception {
        Collection<DataEntry> importedDataEntries = new LinkedList<DataEntry>();
        Document metadata = null;
        ZipInputStream zis = new ZipInputStream(new FileInputStream(jarFile));
        ZipEntry ze = null;
        int nativeNotesCnt = numberedDataEntries.size();
        while ((ze = zis.getNextEntry()) != null) {
            StringWriter sw = new StringWriter();
            int c;
            while ((c = zis.read()) != -1) {
                sw.write(c);
            }
            if (ze.getName().matches(Constants.DATA_FILE_PATTERN)) {
                Integer number = nativeNotesCnt + Integer.valueOf(ze.getName()
                        .replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
                        .replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR));
                DataEntry de = new DataEntry();
                de.setData(sw.getBuffer().toString());
                importedDataEntries.add(de);
                numberedDataEntries.put(number, de);
            } else if (ze.getName().equals(Constants.METADATA_FILE_PATH)) {
                metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(
                        new ByteArrayInputStream(sw.getBuffer().toString().getBytes()));
            }    
        }
        zis.close();
        if (metadata != null) {
            NodeList entries = metadata.getElementsByTagName("entry");
            for (int i = 0; i < entries.getLength(); i++){
                Node entry = entries.item(i);
                NamedNodeMap attributes = entry.getAttributes();
                Node attNumber = attributes.getNamedItem("number");
                Integer number = nativeNotesCnt + Integer.valueOf(attNumber.getNodeValue());
                Node attCaption = attributes.getNamedItem("caption");
                String caption = attCaption.getNodeValue();
                Node attType = attributes.getNamedItem("type");
                String type = attType.getNodeValue();
                DataEntry dataEntry = numberedDataEntries.get(number);
                dataEntry.setCaption(caption);
                dataEntry.setType(type);
            }
        }
        data = numberedDataEntries.values();
        return importedDataEntries;
    }
    
    public static void store() throws Exception {
        if (jarFile == null) {
            init();
        }
        metadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().newDocument();
        Node rootNode = metadata.createElement("metadata");
        int number = 0;
        for (DataEntry dataEntry : data) {
            String key = Constants.DATA_DIR + ++number + Constants.DATA_FILE_ENDING;
            String value = dataEntry.getData();
            zipEntries.put(key, value);
            Element entryNode = metadata.createElement("entry");
            entryNode.setAttribute("number", ""+number);
            entryNode.setAttribute("caption", dataEntry.getCaption());
            entryNode.setAttribute("type", dataEntry.getType());
            rootNode.appendChild(entryNode);
        }
        metadata.appendChild(rootNode);
        StringWriter sw = new StringWriter();
        properties.list(new PrintWriter(sw));
        zipEntries.put(Constants.CONFIG_FILE_PATH, sw.getBuffer().toString());
        sw = new StringWriter();
        new XMLSerializer(sw, new OutputFormat()).serialize(metadata);
        zipEntries.put(Constants.METADATA_FILE_PATH, sw.getBuffer().toString());
        ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(jarFile));
        for (Entry<String, String> entry : zipEntries.entrySet()) {
            String entryName = entry.getKey();
            ZipEntry zipEntry = new ZipEntry(entryName);
            String entryData = entry.getValue();
            zos.putNextEntry(zipEntry);
            StringReader sr = new StringReader(entryData);
            int c;
            while ((c = sr.read()) != -1) {
                zos.write(c);
            }
            zos.closeEntry();
        }
        zos.flush();
        zos.close();
    }
    
    private static void init() {
        URL url = BackEnd.class.getResource(BackEnd.class.getSimpleName()+".class");
        String jarFilePath = url.getFile().substring(0, url.getFile().indexOf(BackEnd.class.getName().replaceAll("\\.", Constants.PATH_SEPARATOR)) - 2);
        jarFilePath = jarFilePath.substring("file:".length(), jarFilePath.length());
        jarFile = new File(jarFilePath);
        // TODO: remove debug code
//        jarFile = new File("/mnt/stor/devel/Bias/build/bias.jar");
    }

    public static Collection<DataEntry> getData() {
        return data;
    }

    public static void setData(Collection<DataEntry> data) {
        BackEnd.data = data;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties properties) {
        BackEnd.properties = properties;
    }

}
