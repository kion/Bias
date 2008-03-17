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
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
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
                byte[] buffer = new byte[1024];
                int br;
                while ((br = is.read(buffer)) > 0) {
                    baos.write(buffer, 0, br);
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

    public static void compress(File source, File destination) throws Exception {
        doCompress(source, destination, null, null);
    }
    
    public static String compress(File source, File destination, MessageDigest md) throws Exception {
        return doCompress(source, destination, md, null);
    }
    
    public static String compress(File source, File destination, MessageDigest md, Set<String> fileNamesToSkipInCheckSumCalculation) throws Exception {
        return doCompress(source, destination, md, fileNamesToSkipInCheckSumCalculation);
    }
    
    private static String doCompress(File source, File destination, MessageDigest md, Set<String> fileNamesToSkipInCheckSumCalculation) throws Exception {
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
        compress(source, source, out, md, fileNamesToSkipInCheckSumCalculation);
        out.close();
        return md != null ? FormatUtils.formatBytesAsHexString(md.digest()) : null;
    }

    private static void compress(File root, File in, ZipOutputStream out, MessageDigest md, Set<String> fileNamesToSkipInCheckSumCalculation) throws IOException {
        if (in.isDirectory()) {
            List<File> files = Arrays.asList(in.listFiles());
            if (md != null) {
                Collections.sort(files, new Comparator<File>(){
                    public int compare(File o1, File o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            }
            for (File f : files) {
                compress(root, f, out, md, fileNamesToSkipInCheckSumCalculation);
            }
        } else {
            String path = root.toURI().relativize(in.toURI()).toString();
            out.putNextEntry(new ZipEntry(path));
            FileInputStream fin = new FileInputStream(in);
            byte[] buffer = new byte[1024];
            int br;
            if (md == null || (fileNamesToSkipInCheckSumCalculation != null && fileNamesToSkipInCheckSumCalculation.contains(in.getName()))) {
                while ((br = fin.read(buffer)) > 0) {
                    out.write(buffer, 0, br);
                }
            } else {
                while ((br = fin.read(buffer)) > 0) {
                    out.write(buffer, 0, br);
                    md.update(buffer, 0, br);
                }
            }
            fin.close();
            out.closeEntry();
        }
    }

}
