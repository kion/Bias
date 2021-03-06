/**
 * Created on Jan 5, 2007
 */
package bias.skin;

import bias.extension.AddOn;
import bias.i18n.I18nService;


/**
 * @author kion
 */

public abstract class Skin implements AddOn {

    /**
     * Internationalization support
     */
    protected String getMessage(String key) {
        return I18nService.getInstance().getMessages(getClass()).get(key);
    }
    protected String getMessage(String key, String...vars) {
        String modifiedMsg = getMessage(key);
        for (String var : vars) {
            modifiedMsg = modifiedMsg.replaceFirst("\\$", var);
        }
        return modifiedMsg;
    }
    
    
    /**
     * Performs needed actions to activate certain Skin
     * 
     * @param settings settings to use while activating
     */
    public abstract void activate(byte[] settings) throws Throwable;
    
    /**
     * Configures Skin.
     * Should be overridden to return settings for certain Skin.
     * By default returns null (no configuration).
     * 
     * @param settings initial settings
     * @return settings byte array containing serialized configuration settings
     */
    public byte[] configure(byte[] settings) throws Throwable {
        return null;
    }
    
    /**
     * Defines icons of controls.
     * Should be overridden to return control icons for concrete Skin.
     * By default returns null (no custom icons).
     *
     * @return ControlIcons structure
     */
    public GUIIcons getUIIcons() {
        return null;
    }
    
    
    /**
     * Defines whether default HTML Editor Kit is required by skin.
     * 
     * IMPORTANT: built-in CustomHTMLEditorKit is meant to be used 
     * by components providing rich-text editing capabilities; 
     * however, some skins might provide (and rely on) styling for default HTMLEditorKit, 
     * in which case they should indicate so by overriding this method to return true, 
     * which would disable CustomHTMLEditorKit usage (as it may break skin styling otherwise).
     *
     * @return false by default
     */
    public boolean isDefaultHTMLEditorKitRequired() {
        return false;
    }

}
