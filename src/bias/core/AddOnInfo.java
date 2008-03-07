/**
 * Created on Mar 1, 2008
 */
package bias.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import bias.core.pack.Dependency;

/**
 * @author kion
 */
public class AddOnInfo {

    private String name;

    private String version;

    private String author;

    private String description;
    
    private Collection<Dependency> dependencies;

    public AddOnInfo() {
        // default empty constructor
    }

    public AddOnInfo(String name) {
        this.name = name;
    }
    
    public AddOnInfo(String name, String version, String author, String description) {
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
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

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public Collection<Dependency> getDependencies() {
        if (dependencies != null) return Collections.unmodifiableCollection(dependencies);
        return null;
    }

    public void addDependency(Dependency dependency) {
        if (dependencies == null) {
            dependencies = new ArrayList<Dependency>();
        }
        dependencies.add(dependency);
    }

    public void addAllDependencies(Collection<Dependency> dependencies) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<Dependency>();
        }
        this.dependencies.addAll(dependencies);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof AddOnInfo) {
            AddOnInfo aoi = (AddOnInfo) obj;
            if (aoi.getName() != null && aoi.getName().equals(getName())) return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int result = 17;
        result += getName() != null ? getName().hashCode() * 11 : 11;
        return result * 13;
    }
    
}
