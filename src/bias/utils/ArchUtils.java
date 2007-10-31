/**
 * Created on Oct 31, 2007
 */
package bias.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
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
    
    public static void extract(File source, File destination) throws Exception {
        if (!destination.exists()) {
            destination.mkdirs();
        }
        ZipInputStream is = new ZipInputStream(new FileInputStream(source));
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

}
