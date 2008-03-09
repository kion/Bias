/**
 * Created on Mar 9, 2008
 */
package bias.core;

import java.io.File;

/**
 * @author kion
 */
public class FileInfo {
    
    private File file;
    
    private String checkSum;

    public FileInfo(File file, String checkSum) {
        this.file = file;
        this.checkSum = checkSum;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(String checkSum) {
        this.checkSum = checkSum;
    }

}
