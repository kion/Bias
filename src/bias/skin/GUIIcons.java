/**
 * Created on Apr 15, 2007
 */
package bias.skin;

import javax.swing.ImageIcon;

/**
 * @author kion
 */
public class GUIIcons {
    
    // TODO [P2] add more icons for different controls that has no icons currently (buttons on add-ons management dialog etc)

    /**
     * Default control icons
     */
    private static final ImageIcon ICON_ABOUT = new ImageIcon(GUIIcons.class.getResource("/bias/res/about.png"));
    private static final ImageIcon ICON_IMPORT = new ImageIcon(GUIIcons.class.getResource("/bias/res/import.png"));
    private static final ImageIcon ICON_EXPORT = new ImageIcon(GUIIcons.class.getResource("/bias/res/export.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(GUIIcons.class.getResource("/bias/res/delete.png"));
    private static final ImageIcon ICON_CATEGORY = new ImageIcon(GUIIcons.class.getResource("/bias/res/add_category.png"));
    private static final ImageIcon ICON_ADJUST_CATEGORY = new ImageIcon(GUIIcons.class.getResource("/bias/res/adjust_category.png"));
    private static final ImageIcon ICON_ENTRY = new ImageIcon(GUIIcons.class.getResource("/bias/res/add_entry.png"));
    private static final ImageIcon ICON_ADJUST_ENTRY = new ImageIcon(GUIIcons.class.getResource("/bias/res/adjust_entry.png"));
    private static final ImageIcon ICON_CONFIGURE = new ImageIcon(GUIIcons.class.getResource("/bias/res/configure.png"));
    private static final ImageIcon ICON_CHANGE_PASSWORD = new ImageIcon(GUIIcons.class.getResource("/bias/res/change_password.png"));
    private static final ImageIcon ICON_SAVE = new ImageIcon(GUIIcons.class.getResource("/bias/res/save.png"));
    private static final ImageIcon ICON_EXIT = new ImageIcon(GUIIcons.class.getResource("/bias/res/exit.png"));
    private static final ImageIcon ICON_BACKTOFIRST = new ImageIcon(GUIIcons.class.getResource("/bias/res/back_to_first.png"));
    private static final ImageIcon ICON_BACK = new ImageIcon(GUIIcons.class.getResource("/bias/res/back.png"));
    private static final ImageIcon ICON_FORWARD = new ImageIcon(GUIIcons.class.getResource("/bias/res/forward.png"));
    private static final ImageIcon ICON_FORWARDTOLAST = new ImageIcon(GUIIcons.class.getResource("/bias/res/forward_to_last.png"));
    private static final ImageIcon ICON_ADDONS = new ImageIcon(GUIIcons.class.getResource("/bias/res/addons.png"));
    private static final ImageIcon ICON_PREFERENCES = new ImageIcon(GUIIcons.class.getResource("/bias/res/prefs.png"));
    private static final ImageIcon ICON_EXTENSIONS = new ImageIcon(GUIIcons.class.getResource("/bias/res/extensions.png"));
    private static final ImageIcon ICON_SKINS = new ImageIcon(GUIIcons.class.getResource("/bias/res/skins.png"));
    private static final ImageIcon ICON_ICONS = new ImageIcon(GUIIcons.class.getResource("/bias/res/icons.png"));
    private static final ImageIcon ICON_ONLINE = new ImageIcon(GUIIcons.class.getResource("/bias/res/online.png"));
    
    private ImageIcon iconSave;
    private ImageIcon iconImport;
    private ImageIcon iconExport;
    private ImageIcon iconAdjustCategory;
    private ImageIcon iconCategory;
    private ImageIcon iconAdjustEntry;
    private ImageIcon iconConfigure;
    private ImageIcon iconEntry;
    private ImageIcon iconChangePassword;
    private ImageIcon iconDelete;
    private ImageIcon iconExit;
    private ImageIcon iconBackToFirst;
    private ImageIcon iconBack;
    private ImageIcon iconForward;
    private ImageIcon iconForwardToLast;
    private ImageIcon iconPreferences;
    private ImageIcon iconAddOns;
    private ImageIcon iconExtensions;
    private ImageIcon iconSkins;
    private ImageIcon iconIcons;
    private ImageIcon iconOnline;
    private ImageIcon iconAbout;
    
    /**
     * Constructs default ControlIcons structure
     * (with default icons for controls)
     */
    public GUIIcons() {
        this.iconSave = ICON_SAVE;
        this.iconImport = ICON_IMPORT; 
        this.iconExport = ICON_EXPORT; 
        this.iconAdjustCategory = ICON_ADJUST_CATEGORY; 
        this.iconCategory = ICON_CATEGORY;
        this.iconAdjustEntry = ICON_ADJUST_ENTRY; 
        this.iconConfigure = ICON_CONFIGURE; 
        this.iconEntry = ICON_ENTRY;
        this.iconChangePassword = ICON_CHANGE_PASSWORD;
        this.iconDelete = ICON_DELETE;
        this.iconExit = ICON_EXIT; 
        this.iconBackToFirst = ICON_BACKTOFIRST; 
        this.iconBack = ICON_BACK; 
        this.iconForward = ICON_FORWARD; 
        this.iconForwardToLast = ICON_FORWARDTOLAST; 
        this.iconPreferences = ICON_PREFERENCES;
        this.iconAddOns = ICON_ADDONS;
        this.iconExtensions = ICON_EXTENSIONS; 
        this.iconSkins = ICON_SKINS;
        this.iconIcons = ICON_ICONS; 
        this.iconOnline = ICON_ONLINE; 
        this.iconAbout = ICON_ABOUT;
    }

    public GUIIcons(
            ImageIcon iconSave, 
            ImageIcon iconImport, 
            ImageIcon iconExport, 
            ImageIcon iconCategory, 
            ImageIcon iconAdjustCategory, 
            ImageIcon iconEntry, 
            ImageIcon iconAdjustEntry, 
            ImageIcon iconConfigure, 
            ImageIcon iconDelete,
            ImageIcon iconChangePassword,
            ImageIcon iconExit, 
            ImageIcon iconBackToFirst, 
            ImageIcon iconBack, 
            ImageIcon iconForward, 
            ImageIcon iconForwardToLast, 
            ImageIcon iconPreferences, 
            ImageIcon iconAddOns, 
            ImageIcon iconExtensions, 
            ImageIcon iconSkins, 
            ImageIcon iconIcons, 
            ImageIcon iconOnline, 
            ImageIcon iconAbout) {
        this.iconSave = iconSave;
        this.iconImport = iconImport;
        this.iconExport = iconExport;
        this.iconCategory = iconCategory;
        this.iconAdjustCategory = iconAdjustCategory;
        this.iconEntry = iconEntry;
        this.iconAdjustEntry = iconAdjustEntry;
        this.iconConfigure = iconConfigure;
        this.iconDelete = iconDelete;
        this.iconChangePassword = iconChangePassword;
        this.iconExit = iconExit;
        this.iconBackToFirst = iconBackToFirst;
        this.iconBack = iconBack;
        this.iconForward = iconForward;
        this.iconForwardToLast = iconForwardToLast;
        this.iconPreferences = iconPreferences;
        this.iconAddOns = iconAddOns;
        this.iconExtensions = iconExtensions;
        this.iconSkins = iconSkins;
        this.iconIcons = iconIcons;
        this.iconOnline = iconOnline;
        this.iconAbout = iconAbout;
    }

    public ImageIcon getIconAbout() {
        return iconAbout;
    }

    public void setIconAbout(ImageIcon iconAbout) {
        this.iconAbout = iconAbout;
    }

    public ImageIcon getIconAddOns() {
        return iconAddOns;
    }

    public void setIconAddOns(ImageIcon iconAddOns) {
        this.iconAddOns = iconAddOns;
    }

    public ImageIcon getIconCategory() {
        return iconCategory;
    }

    public void setIconCategory(ImageIcon iconCategory) {
        this.iconCategory = iconCategory;
    }

    public ImageIcon getIconDelete() {
        return iconDelete;
    }

    public void setIconDelete(ImageIcon iconDelete) {
        this.iconDelete = iconDelete;
    }

    public ImageIcon getIconExit() {
        return iconExit;
    }

    public void setIconExit(ImageIcon iconExit) {
        this.iconExit = iconExit;
    }

    public ImageIcon getIconBackToFirst() {
        return iconBackToFirst;
    }

    public void setIconBackToFirst(ImageIcon iconBackToFirst) {
        this.iconBackToFirst = iconBackToFirst;
    }

    public ImageIcon getIconBack() {
        return iconBack;
    }

    public void setIconBack(ImageIcon iconBack) {
        this.iconBack = iconBack;
    }

    public ImageIcon getIconForward() {
        return iconForward;
    }

    public void setIconForward(ImageIcon iconForward) {
        this.iconForward = iconForward;
    }

    public ImageIcon getIconForwardToLast() {
        return iconForwardToLast;
    }

    public void setIconForwardToLast(ImageIcon iconForwardToLast) {
        this.iconForwardToLast = iconForwardToLast;
    }

    public ImageIcon getIconEntry() {
        return iconEntry;
    }

    public void setIconEntry(ImageIcon iconEntry) {
        this.iconEntry = iconEntry;
    }

    public ImageIcon getIconExtensions() {
        return iconExtensions;
    }

    public void setIconExtensions(ImageIcon iconExtensions) {
        this.iconExtensions = iconExtensions;
    }

    public ImageIcon getIconIcons() {
        return iconIcons;
    }

    public void setIconIcons(ImageIcon iconIcons) {
        this.iconIcons = iconIcons;
    }

    public ImageIcon getIconImport() {
        return iconImport;
    }

    public void setIconImport(ImageIcon iconImport) {
        this.iconImport = iconImport;
    }

    public ImageIcon getIconExport() {
        return iconExport;
    }

    public void setIconExport(ImageIcon iconExport) {
        this.iconExport = iconExport;
    }

    public ImageIcon getIconSkins() {
        return iconSkins;
    }

    public void setIconSkins(ImageIcon iconSkins) {
        this.iconSkins = iconSkins;
    }

    public ImageIcon getIconPreferences() {
        return iconPreferences;
    }

    public void setIconPreferences(ImageIcon iconPreferences) {
        this.iconPreferences = iconPreferences;
    }

    public ImageIcon getIconSave() {
        return iconSave;
    }

    public void setIconSave(ImageIcon iconSave) {
        this.iconSave = iconSave;
    }

    public ImageIcon getIconChangePassword() {
        return iconChangePassword;
    }

    public void setIconChangePassword(ImageIcon iconChangePassword) {
        this.iconChangePassword = iconChangePassword;
    }

    public ImageIcon getIconAdjustCategory() {
        return iconAdjustCategory;
    }

    public void setIconAdjustCategory(ImageIcon iconAdjustCategory) {
        this.iconAdjustCategory = iconAdjustCategory;
    }

    public ImageIcon getIconAdjustEntry() {
        return iconAdjustEntry;
    }

    public void setIconAdjustEntry(ImageIcon iconAdjustEntry) {
        this.iconAdjustEntry = iconAdjustEntry;
    }

    public ImageIcon getIconConfigure() {
        return iconConfigure;
    }

    public void setIconConfigure(ImageIcon iconConfigure) {
        this.iconConfigure = iconConfigure;
    }

    public ImageIcon getIconOnline() {
        return iconOnline;
    }

    public void setIconOnline(ImageIcon iconOnline) {
        this.iconOnline = iconOnline;
    }

}
