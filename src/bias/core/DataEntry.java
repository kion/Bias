/**
 * Created on Oct 23, 2006
 */
package bias.core;

/**
 * @author kion
 */
public class DataEntry {
    
    private String caption;
    
    private String category;
    
    private String parentCategory;
    
    private String type;
    
    private byte[] data;
    
    public DataEntry() {
        // default constructor
    }
    
    public DataEntry(String caption, String category, String type, byte[] data) {
        this.caption = caption;
        this.category = category;
        this.type = type;
        this.data = data;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(String parentCategory) {
        this.parentCategory = parentCategory;
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
