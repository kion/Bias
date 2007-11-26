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
    
    public enum ENTRY_TYPE {
        CATEGORY,
        ENTRY
    }
    
    private Recognizable entry;
    private Collection<Recognizable> entryPath;
    private ENTRY_TYPE entryType;
    
    public VisualEntryDescriptor(Recognizable entry, Collection<Recognizable> entryPath, ENTRY_TYPE entryType) {
        this.entry = entry;
        this.entryPath = entryPath;
        this.entryType = entryType;
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
    
    public ENTRY_TYPE getEntryType() {
        return entryType;
    }

    public void setEntryType(ENTRY_TYPE entryType) {
        this.entryType = entryType;
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
