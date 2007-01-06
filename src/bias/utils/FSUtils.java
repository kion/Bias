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

/**
 * @author kion
 *
 */

public class FSUtils {
	
	private static FSUtils instance;
	
	public static FSUtils getInstance() {
		if (instance == null) {
			instance = new FSUtils();
		}
		return instance;
	}
	
	private FSUtils() {
		// singleton: default constructor is private
	}
	
	public byte[] readFile(File file) throws IOException {
		byte[] data = null;
		if (file != null && file.exists() && !file.isDirectory()) {
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int b;
			while ((b = bis.read()) != -1) {
				baos.write(b);
			}
			baos.close();
			bis.close();
			data = baos.toByteArray();
		}
		return data;
	}
	
	public void writeFile(File file, byte[] data) throws IOException {
		if (file != null && !file.isDirectory()) {
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			FileOutputStream fos = new FileOutputStream(file);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			int b;
			while ((b = bais.read()) != -1) {
				bos.write(b);
			}
			bos.close();
			bais.close();
		}
	}

}