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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import bias.global.Constants;

/**
 * @author kion
 */
public class BackEnd {
    
    private static File jarFile = null;

    private static Map<String, String> zipEntries;
    
    private static Map<String, String> notes;
    
    private static Properties properties;
    
    public static void load() throws Exception {
        if (jarFile == null) {
            init();
        }
        zipEntries = new LinkedHashMap<String, String>();
        notes = new LinkedHashMap<String, String>();
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
                notes.put(ze.getName()
                        .replaceFirst(Constants.DATA_DIR_PATTERN, Constants.EMPTY_STR)
                        .replaceFirst(Constants.DATA_FILE_ENDING_PATTERN, Constants.EMPTY_STR), sw.getBuffer().toString());
            } else if (ze.getName().equals(Constants.CONFIG_FILE_PATH)) {
                properties.load(new ByteArrayInputStream(sw.getBuffer().toString().getBytes()));
            } else {
                zipEntries.put(ze.getName(), sw.getBuffer().toString());
            }
        }
        zis.close();
    }
    
    public static void store() throws Exception {
        if (jarFile == null) {
            init();
        }
        for (Entry<String, String> note : notes.entrySet()) {
            String key = Constants.DATA_DIR + note.getKey() + Constants.DATA_FILE_ENDING;
            String value = note.getValue();
            zipEntries.put(key, value);
        }
        StringWriter sw = new StringWriter();
        properties.list(new PrintWriter(sw));
        zipEntries.put(Constants.CONFIG_FILE_PATH, sw.getBuffer().toString());
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

    public static Map<String, String> getNotes() {
        return notes;
    }

    public static void setNotes(Map<String, String> notes) {
        BackEnd.notes = notes;
    }

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties properties) {
        BackEnd.properties = properties;
    }

}
