/**
 * Created on Oct 23, 2006
 */
package bias.extension;

import java.util.Collection;
import java.util.UUID;

import javax.swing.JComponent;

/**
 * @author kion
 */

public abstract class EntryExtension extends JComponent implements Extension {

    private UUID id;
    
    private byte[] data;
    
    private byte[] settings;
    
    @SuppressWarnings("unused")
    private EntryExtension() {
        // default constructor without parameters is not visible
    }

    /**
     * The only allowed constructor that is aware of initialization data and settings.
     * 
     * @param id id to be assigned to extension instance
     * @param data data to be encapsulated by extension instance
     * @param settings extension instance settings
     */
    public EntryExtension(UUID id, byte[] data, byte[] settings) {
        if (id == null) {
            id = UUID.randomUUID();
        }
        this.id = id;
        this.data = data;
        this.settings = settings;
    }

    /**
     * @return the id
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return the data
     */
    public byte[] getData() {
        return data;
    }

    /**
     * @return the settings
     */
    public byte[] getSettings() {
        return settings;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#configure(byte[])
     */
    public byte[] configure(byte[] settings) throws Throwable {
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeSettings()
     */
    public byte[] serializeSettings() throws Throwable {
        return null;
    }

    /* (non-Javadoc)
     * @see bias.extension.Extension#serializeData()
     */
    public byte[] serializeData() throws Throwable {
        return null;
    }

    /**
     * Returns extension's search data.
     * By default returns null (no data for search provided).
     * Should be overridden to return search data for certain extension's instance.
     * 
     * @return data for search provided by extension's instance
     */
    public Collection<String> getSearchData() throws Throwable {
        return null;
    }

}
