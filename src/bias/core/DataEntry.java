/**
 * Created on Oct 23, 2006
 */
package bias.core;

import java.util.Arrays;
import java.util.UUID;

import javax.swing.Icon;

/**
 * @author kion
 */
public class DataEntry extends Recognizable {
    
    private String type;
    
    private byte[] data;
    
    private byte[] settings;
    
    public DataEntry() {
        super();
    }
    
    public DataEntry(UUID id, String caption, Icon icon, String type, byte[] data, byte[] settings) {
        super(id, caption, icon);
        this.type = type;
        this.data = data;
        this.settings = settings;
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

    public byte[] getSettings() {
        return settings;
    }

    public void setSettings(byte[] settings) {
        this.settings = settings;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj) || !(obj instanceof DataEntry)) {
            return false;
        } else {
            DataEntry de = (DataEntry) obj;
            if (de.getType() == null) {
                return false;
            } else {
                return de.getType().equals(getType()) 
                        && (de.getData() == null ? getData() == null : Arrays.equals(de.getData(), getData())) 
                        && (de.getSettings() == null ? getSettings() == null : Arrays.equals(de.getSettings(), getSettings()));
            }
        }
    }

}
