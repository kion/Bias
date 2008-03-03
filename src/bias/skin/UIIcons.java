/**
 * Created on Apr 15, 2007
 */
package bias.skin;

import javax.swing.ImageIcon;

/**
 * @author kion
 */
public class UIIcons {

    /**
     * Default control icons
     */
    // TODO [P1] find some nice and single-styled default icons, current ones look bad, more so, they are from different icon-sets
    public static final ImageIcon ICON_ABOUT = new ImageIcon(UIIcons.class.getResource("/bias/res/about.png"));
    public static final ImageIcon ICON_IMPORT = new ImageIcon(UIIcons.class.getResource("/bias/res/import.png"));
    public static final ImageIcon ICON_EXPORT = new ImageIcon(UIIcons.class.getResource("/bias/res/export.png"));
    public static final ImageIcon ICON_DELETE = new ImageIcon(UIIcons.class.getResource("/bias/res/delete.png"));
    public static final ImageIcon ICON_CATEGORY = new ImageIcon(UIIcons.class.getResource("/bias/res/add_category.png"));
    public static final ImageIcon ICON_ROOT_CATEGORY = new ImageIcon(UIIcons.class.getResource("/bias/res/add_root_category.png"));
    public static final ImageIcon ICON_ADJUST_CATEGORY = new ImageIcon(UIIcons.class.getResource("/bias/res/adjust_category.png"));
    public static final ImageIcon ICON_ENTRY = new ImageIcon(UIIcons.class.getResource("/bias/res/add_entry.png"));
    public static final ImageIcon ICON_ROOT_ENTRY = new ImageIcon(UIIcons.class.getResource("/bias/res/add_root_entry.png"));
    public static final ImageIcon ICON_ADJUST_ENTRY = new ImageIcon(UIIcons.class.getResource("/bias/res/adjust_entry.png"));
    public static final ImageIcon ICON_CHANGE_PASSWORD = new ImageIcon(UIIcons.class.getResource("/bias/res/change_password.png"));
    public static final ImageIcon ICON_SAVE = new ImageIcon(UIIcons.class.getResource("/bias/res/save.png"));
    public static final ImageIcon ICON_EXIT = new ImageIcon(UIIcons.class.getResource("/bias/res/exit.png"));
    public static final ImageIcon ICON_BACKTOFIRST = new ImageIcon(UIIcons.class.getResource("/bias/res/back_to_first.png"));
    public static final ImageIcon ICON_BACK = new ImageIcon(UIIcons.class.getResource("/bias/res/back.png"));
    public static final ImageIcon ICON_FORWARD = new ImageIcon(UIIcons.class.getResource("/bias/res/forward.png"));
    public static final ImageIcon ICON_FORWARDTOLAST = new ImageIcon(UIIcons.class.getResource("/bias/res/forward_to_last.png"));
    public static final ImageIcon ICON_ADDONS = new ImageIcon(UIIcons.class.getResource("/bias/res/addons.png"));
    public static final ImageIcon ICON_PREFERENCES = new ImageIcon(UIIcons.class.getResource("/bias/res/prefs.png"));
    public static final ImageIcon ICON_EXTENSIONS = new ImageIcon(UIIcons.class.getResource("/bias/res/extensions.png"));
    public static final ImageIcon ICON_SKINS = new ImageIcon(UIIcons.class.getResource("/bias/res/skins.png"));
    public static final ImageIcon ICON_ICONS = new ImageIcon(UIIcons.class.getResource("/bias/res/icons.png"));
    public static final ImageIcon ICON_ONLINE = new ImageIcon(UIIcons.class.getResource("/bias/res/online.png"));

    private ImageIcon iconSave;
    private ImageIcon iconImport;
    private ImageIcon iconExport;
    private ImageIcon iconRootCategory;
    private ImageIcon iconAdjustCategory;
    private ImageIcon iconCategory;
    private ImageIcon iconRootEntry;
    private ImageIcon iconAdjustEntry;
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
    public UIIcons() {
        this.iconSave = ICON_SAVE;
        this.iconImport = ICON_IMPORT; 
        this.iconExport = ICON_EXPORT; 
        this.iconRootCategory = ICON_ROOT_CATEGORY; 
        this.iconAdjustCategory = ICON_ADJUST_CATEGORY; 
        this.iconCategory = ICON_CATEGORY;
        this.iconRootEntry = ICON_ROOT_ENTRY; 
        this.iconAdjustEntry = ICON_ADJUST_ENTRY; 
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

    public UIIcons(
            ImageIcon iconSave, 
            ImageIcon iconImport, 
            ImageIcon iconExport, 
            ImageIcon iconRootCategory, 
            ImageIcon iconCategory, 
            ImageIcon iconAdjustCategory, 
            ImageIcon iconRootEntry, 
            ImageIcon iconEntry, 
            ImageIcon iconAdjustEntry, 
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
        this.iconRootCategory = iconRootCategory;
        this.iconCategory = iconCategory;
        this.iconAdjustCategory = iconAdjustCategory;
        this.iconRootEntry = iconRootEntry;
        this.iconEntry = iconEntry;
        this.iconAdjustEntry = iconAdjustEntry;
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

    public ImageIcon getIconRootCategory() {
        return iconRootCategory;
    }

    public void setIconRootCategory(ImageIcon iconRootCategory) {
        this.iconRootCategory = iconRootCategory;
    }

    public ImageIcon getIconRootEntry() {
        return iconRootEntry;
    }

    public void setIconRootEntry(ImageIcon iconRootEntry) {
        this.iconRootEntry = iconRootEntry;
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

    public ImageIcon getIconOnline() {
        return iconOnline;
    }

    public void setIconOnline(ImageIcon iconOnline) {
        this.iconOnline = iconOnline;
    }

}
