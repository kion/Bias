/**
 * Created on Nov 1, 2006
 */
package bias.gui;

import java.util.Collection;
import java.util.Iterator;

import bias.core.Recognizable;

/**
 * @author kion
 */
public class VisualEntryDescriptor {
    
    private Recognizable entry;
    private Collection<Recognizable> entryPath;
    
    public VisualEntryDescriptor() {
        // default constructor
    }
    
    public VisualEntryDescriptor(Recognizable entry, Collection<Recognizable> entryPath) {
        this.entry = entry;
        this.entryPath = entryPath;
    }

    public Recognizable getEntry() {
        return entry;
    }

    public void setEntry(Recognizable entry) {
        this.entry = entry;
    }
    
    public Collection<Recognizable> getEntryPath() {
        return entryPath;
    }

    public void setEntryPath(Collection<Recognizable> entryPath) {
        this.entryPath = entryPath;
    }
    
    @Override
    public String toString() {
        StringBuffer entryPathStr = new StringBuffer();
        Iterator<Recognizable> it = entryPath.iterator();
        while (it.hasNext()) {
            Recognizable r = it.next();
            entryPathStr.append(r.getCaption());
            if (it.hasNext()) {
                entryPathStr.append(" > ");
            }
        }
        return entryPathStr.toString();
    }

}
