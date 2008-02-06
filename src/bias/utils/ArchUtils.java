/**
 * Created on Oct 31, 2007
 */
package bias.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
                String name = URLDecoder.decode(ze.getName(), Constants.UNICODE_ENCODING);
                File file = new File(destination, name);
                File dir = file.getParentFile();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                file.createNewFile();
                FSUtils.writeFile(file, baos.toByteArray());
            }
        }
        is.close();
    }
    
    public static void compress(File source, File destination) throws IOException {
        if (!destination.exists()) {
            File destinationDir = destination.getParentFile();
            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }
        } else if (destination.isDirectory()) {
            throw new IOException("Compression can be done into file only!");
        }
        if (destination.exists()) {
            destination.delete();
        }
        destination.createNewFile();
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destination));
        compress(source, source, out);
        out.close();
    }
    
    private static void compress(File root, File in, ZipOutputStream out) throws IOException {
        if (in.isDirectory()) {
            for (File f : in.listFiles()) {
                compress(root, f, out);
            }
        } else {
            String path = root.toURI().relativize(in.toURI()).toString();
            out.putNextEntry(new ZipEntry(path));
            FileInputStream fin = new FileInputStream(in);
            int b;
            while ((b = fin.read()) != -1) {
                out.write(b);
            }
            fin.close();
            out.closeEntry();
        }
    }

}
