/**
 * Created on Jun 27, 2008
 */
package bias.core;

import java.util.UUID;

/**
 * @author kion
 */
public class ToolData extends Identifiable {

    private byte[] data;
    
    public ToolData(UUID id, byte[] data) {
        super(id);
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
    
}
