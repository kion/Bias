/**
 * Created on Oct 23, 2006
 */
package bias.core;

/**
 * @author kion
 */
public class DataEntry {
    
    private String caption;
    
    private String data;
    
    private String type;
    
    public DataEntry() {
        // default constructor
    }
    
    public DataEntry(String caption, String data, String type) {
        this.caption = caption;
        this.data = data;
        this.type = type;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
    
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
