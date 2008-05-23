/**
 * Dec 26, 2006
 */
package bias.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * @author kion
 *
 */

public class FSUtils {
	
	private FSUtils() {
        // hidden default constructor
	}
	
	public static byte[] readFile(File file) throws IOException {
		byte[] data = null;
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int br;
        while ((br = bis.read(buffer)) > 0) {
            baos.write(buffer, 0, br);
        }
        baos.close();
        bis.close();
        data = baos.toByteArray();
		return data;
	}
	
	public static void writeFile(File file, byte[] data) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        byte[] buffer = new byte[1024];
        int br;
        while ((br = bais.read(buffer)) > 0) {
            bos.write(buffer, 0, br);
        }
        bos.close();
        bais.close();
	}
	
    public static void duplicateFile(File in, File out) throws IOException {
        if (in.exists() && in != null && out != null) {
            if (!out.exists()) {
                if (in.isDirectory()) {
                    out.mkdirs();
                } else {
                    out.createNewFile();
                }
            }
            String source = in.isDirectory() ? "directory" : "file"; 
            String target = out.isDirectory() ? "directory" : "file"; 
            if (!source.equals(target)) {
                throw new IOException("Can't duplicate " + source + " as " + target);
            } else {
                if (source.equals("directory")) {
                    File[] files = in.listFiles();
                    for (File file : files) {
                        duplicateFile(file, new File(out, file.getName()));
                    }
                } else {
                    FileChannel inCh = new FileInputStream(in).getChannel();
                    FileChannel outCh = new FileOutputStream(out).getChannel();
                    inCh.transferTo(0, inCh.size(), outCh);
                }
            }
        }
    }

    public static void delete(File file) {
        if (file != null && file.exists()) {
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    delete(f);
                }
            }
            file.delete();
        }
    }

    public static void clearDirectory(File dir) {
        for (File f : dir.listFiles()) {
            delete(f);
        }
    }
    
    public static long getFileSize(File file) {
        return getFileSize(file, null);
    }
    
    public static long getFileSize(File file, List<String> excludeFileNamesList) {
        long size = 0;
        size += file.length();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (excludeFileNamesList == null || !excludeFileNamesList.contains(f.getName())) {
                    if (f.isDirectory()) {
                        size += getFileSize(f, excludeFileNamesList);
                    } else {
                        size += f.length();
                    }
                }
            }
        }
        return size;
    }

}
