/**
 * Created on Oct 31, 2007
 */
package bias.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import bias.Constants;

/**
 * @author kion
 */
public class ArchUtils {
    
    private ArchUtils() {
        // hidden default constructor
    }
    
    public static void extract(byte[] source, File destination) throws IOException {
        if (!destination.exists()) {
            destination.mkdirs();
        } else if (!destination.isDirectory()) {
            throw new IOException("Extraction can be done into directory only!");
        }
        ZipInputStream is = new ZipInputStream(new ByteArrayInputStream(source));
        ZipEntry ze;
        while ((ze = is.getNextEntry()) != null) {
            if (ze.getName().endsWith(Constants.PATH_SEPARATOR)) {
                File dir = new File(destination, ze.getName());
                dir.mkdir();
            } else {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int b;
                while ((b = is.read()) != -1) {
                    baos.write(b);
                }
                baos.close();
                File file = new File(destination, ze.getName());
                FSUtils.writeFile(file, baos.toByteArray());
            }
        }
        is.close();
    }
    
    public static void compress(File source, File destination) throws IOException {
        // TODO: implement
        if (!destination.exists()) {
            File destinationDir = destination.getParentFile();
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }
            destination.createNewFile();
        } else if (destination.isDirectory()) {
            throw new IOException("Compression can be done into file only!");
        }
    }

}
