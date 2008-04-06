/**
 * Created on Oct 23, 2006
 */
package bias.extension;

import java.util.Collection;
import java.util.UUID;

import javax.swing.JComponent;

import bias.i18n.I18nService;

/**
 * @author kion
 */

public abstract class EntryExtension extends JComponent implements Extension {

    /**
     * Internationalization support
     */
    protected String getMessage(String key) {
        return I18nService.getInstance().getMessages(getClass()).get(key);
    }
    
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

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    /**
     * Serializes extension's settings to array of bytes
     * Should be overridden to return settings for certain extension's instance.
     * By default returns null (no settings).
     * 
     * @return array of bytes representing serialized settings of extension's instance
     */
    public byte[] serializeSettings() throws Throwable {
        return null;
    }

    /**
     * Serializes extension's data to array of bytes.
     * Should be overridden to return data for certain extension's instance.
     * By default returns null (no data).
     * 
     * @return array of bytes representing serialized data of extension's instance
     */
    public byte[] serializeData() throws Throwable {
        return null;
    }

    /**
     * Performs configuration of either specific extension's instance or extension in general.
     * Should be overridden to return settings for certain extension (or it's instance).
     * By default returns null (no configuration / no changes made).
     * Note: this method is called from 2 places:
     *       1) AddOns-management screen, in case user wants to perform general extension configuration
     *          (so, settings returned by this method in this case will be treated as default ones 
     *          and will be applied to each extension instance (entry) that has no it's own settings)
     *       2) Main tool-bar, in case user wants to perform specific extension instance (entry) configuration
     *          (so, settings returned by this method in this case will be simply passed to applySettings() method, 
     *          so appropriate changes can be performed in the business-layer of the entry)
     * 
     * @param settings initial settings
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure(byte[] settings) throws Throwable {
        return null;
    }
    
    /**
     * Applies settings specified.
     * Should be overridden to apply settings for certain extension instance.
     * Note: this method is called after each configure() method call, 
     * in case of specific extension instance configuration 
     * (thus, if configure() is called for general extension configuration,
     * this method won't be called).
     * 
     * @param settings settings to be applied
     */
    public void applySettings(byte[] settings) {}

    /**
     * Returns extension's search data.
     * Should be overridden to return search data for certain extension's instance.
     * By default returns null (no search data provided).
     * 
     * @return data for search provided by extension's instance
     */
    public Collection<String> getSearchData() throws Throwable {
        return null;
    }

}
