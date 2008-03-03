/**
 * Created on Mar 3, 2008
 */
package bias.core;

import bias.Constants.ADDON_TYPE;

/**
 * @author kion
 */
public class Dependency {
    
    private ADDON_TYPE type;

    private String name;

    private String version;

    public Dependency(ADDON_TYPE type, String name, String version) {
        this.type = type;
        this.name = name;
        this.version = version;
    }

    public ADDON_TYPE getType() {
        return type;
    }

    public void setType(ADDON_TYPE type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
