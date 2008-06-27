/**
 * Created on Jun 27, 2008
 */
package bias.core;

import java.util.UUID;

/**
 * @author kion
 */
public abstract class Identifiable {

    protected UUID id;
    
    public Identifiable() {
        // default empty constructor
    }
    
    protected Identifiable(UUID id) {
        this.id = id;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

}
