/**
 * Dec 26, 2006
 */
package bias.core;

import java.io.File;
import java.io.IOException;

import bias.utils.FSUtils;

/**
 * @author kion
 *
 */

public class Attachment {

	private String name;
    
    private byte[] data;
    
    public Attachment() {}
    
    public Attachment(String name, byte[] data) {
    	this.name = name;
    	this.data = data;
    }

    public Attachment(File file) throws IOException {
    	this.data = FSUtils.getInstance().readFile(file);
    	this.name = file.getName();
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

}
