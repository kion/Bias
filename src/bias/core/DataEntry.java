/**
 * Created on Oct 23, 2006
 */
package bias.core;

/**
 * @author kion
 */
public class DataEntry {
    
    private String caption;
    
    private String type;
    
    private byte[] data;
    
    public DataEntry() {
        // default constructor
    }
    
    public DataEntry(String caption, String type, byte[] data) {
        this.caption = caption;
        this.type = type;
        this.data = data;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
