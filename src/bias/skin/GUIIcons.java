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
    private static final ImageIcon ICON_HELP = new ImageIcon(GUIIcons.class.getResource("/bias/res/help.png"));
    private static final ImageIcon ICON_IMPORT = new ImageIcon(GUIIcons.class.getResource("/bias/res/import.png"));
    private static final ImageIcon ICON_EXPORT = new ImageIcon(GUIIcons.class.getResource("/bias/res/export.png"));
    private static final ImageIcon ICON_DELETE = new ImageIcon(GUIIcons.class.getResource("/bias/res/delete.png"));
    private static final ImageIcon ICON_CATEGORY = new ImageIcon(GUIIcons.class.getResource("/bias/res/add_category.png"));
    private static final ImageIcon ICON_CONFIGURE_CATEGORY = new ImageIcon(GUIIcons.class.getResource("/bias/res/config_category.png"));
    private static final ImageIcon ICON_ENTRY = new ImageIcon(GUIIcons.class.getResource("/bias/res/add_entry.png"));
    private static final ImageIcon ICON_CONFIGURE_ENTRY = new ImageIcon(GUIIcons.class.getResource("/bias/res/config_entry.png"));
    private static final ImageIcon ICON_RELOCATE = new ImageIcon(GUIIcons.class.getResource("/bias/res/relocate.png"));
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
    private ImageIcon iconConfigureCategory;
    private ImageIcon iconCategory;
    private ImageIcon iconConfigureEntry;
    private ImageIcon iconEntry;
    private ImageIcon iconRelocate;
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
    private ImageIcon iconHelp;
    private ImageIcon iconAbout;
    
    /**
     * Constructs default ControlIcons structure
     * (with default icons for controls)
     */
    public GUIIcons() {
        this.iconSave = ICON_SAVE;
        this.iconImport = ICON_IMPORT; 
        this.iconExport = ICON_EXPORT; 
        this.iconConfigureCategory = ICON_CONFIGURE_CATEGORY; 
        this.iconCategory = ICON_CATEGORY;
        this.iconConfigureEntry = ICON_CONFIGURE_ENTRY; 
        this.iconEntry = ICON_ENTRY;
        this.iconRelocate = ICON_RELOCATE;
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
        this.iconHelp = ICON_HELP;
        this.iconAbout = ICON_ABOUT;
    }

    public GUIIcons(
            ImageIcon iconSave, 
            ImageIcon iconImport, 
            ImageIcon iconExport, 
            ImageIcon iconCategory, 
            ImageIcon iconConfigureCategory, 
            ImageIcon iconEntry, 
            ImageIcon iconConfigureEntry, 
            ImageIcon iconRelocate, 
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
            ImageIcon iconHelp, 
            ImageIcon iconAbout) {
        this.iconSave = iconSave;
        this.iconImport = iconImport;
        this.iconExport = iconExport;
        this.iconCategory = iconCategory;
        this.iconConfigureCategory = iconConfigureCategory;
        this.iconEntry = iconEntry;
        this.iconConfigureEntry = iconConfigureEntry;
        this.iconRelocate = iconRelocate;
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
        this.iconHelp = iconHelp;
        this.iconAbout = iconAbout;
    }

    public ImageIcon getIconAbout() {
        return iconAbout;
    }

    public void setIconAbout(ImageIcon iconAbout) {
        this.iconAbout = iconAbout;
    }

    public ImageIcon getIconHelp() {
		return iconHelp;
	}

	public void setIconHelp(ImageIcon iconHelp) {
		this.iconHelp = iconHelp;
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

    public ImageIcon getIconConfigureCategory() {
        return iconConfigureCategory;
    }

    public void setIconConfigureCategory(ImageIcon iconConfigureCategory) {
        this.iconConfigureCategory = iconConfigureCategory;
    }

    public ImageIcon getIconConfigureEntry() {
        return iconConfigureEntry;
    }

    public void setIconConfigureEntry(ImageIcon iconConfigureEntry) {
        this.iconConfigureEntry = iconConfigureEntry;
    }

    public ImageIcon getIconRelocate() {
        return iconRelocate;
    }

    public void setIconRelocate(ImageIcon iconRelocate) {
        this.iconRelocate = iconRelocate;
    }

    public ImageIcon getIconOnline() {
        return iconOnline;
    }

    public void setIconOnline(ImageIcon iconOnline) {
        this.iconOnline = iconOnline;
    }

}
