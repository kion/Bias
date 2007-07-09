/**
 * Created on Apr 15, 2007
 */
package bias.laf;

import javax.swing.ImageIcon;

/**
 * @author kion
 */
public class ControlIcons {

    /**
     * Default control icons
     */
    public static final ImageIcon ICON_ABOUT = new ImageIcon(ControlIcons.class.getResource("/bias/res/about.png"));
    public static final ImageIcon ICON_IMPORT = new ImageIcon(ControlIcons.class.getResource("/bias/res/import_data.png"));
    public static final ImageIcon ICON_DELETE = new ImageIcon(ControlIcons.class.getResource("/bias/res/delete.png"));
    public static final ImageIcon ICON_CATEGORY = new ImageIcon(ControlIcons.class.getResource("/bias/res/add_category.png"));
    public static final ImageIcon ICON_ROOT_CATEGORY = new ImageIcon(ControlIcons.class.getResource("/bias/res/add_root_category.png"));
    public static final ImageIcon ICON_ENTRY = new ImageIcon(ControlIcons.class.getResource("/bias/res/add_entry.png"));
    public static final ImageIcon ICON_ROOT_ENTRY = new ImageIcon(ControlIcons.class.getResource("/bias/res/add_root_entry.png"));
    public static final ImageIcon ICON_CHANGE_PASSWORD = new ImageIcon(ControlIcons.class.getResource("/bias/res/change_password.png"));
    public static final ImageIcon ICON_SEARCH = new ImageIcon(ControlIcons.class.getResource("/bias/res/search.png"));
    public static final ImageIcon ICON_SAVE = new ImageIcon(ControlIcons.class.getResource("/bias/res/save.png"));
    public static final ImageIcon ICON_DISCARD = new ImageIcon(ControlIcons.class.getResource("/bias/res/discard.png"));
    public static final ImageIcon ICON_ADDONS = new ImageIcon(ControlIcons.class.getResource("/bias/res/addons.png"));
    public static final ImageIcon ICON_PREFERENCES = new ImageIcon(ControlIcons.class.getResource("/bias/res/prefs.png"));
    public static final ImageIcon ICON_EXTENSIONS = new ImageIcon(ControlIcons.class.getResource("/bias/res/extensions.png"));
    public static final ImageIcon ICON_LAFS = new ImageIcon(ControlIcons.class.getResource("/bias/res/lafs.png"));
    public static final ImageIcon ICON_ICONS = new ImageIcon(ControlIcons.class.getResource("/bias/res/icons.png"));

    private ImageIcon iconSave;
    private ImageIcon iconImport;
    private ImageIcon iconRootCategory;
    private ImageIcon iconCategory;
    private ImageIcon iconRootEntry;
    private ImageIcon iconEntry;
    private ImageIcon iconChangePassword;
    private ImageIcon iconSearch;
    private ImageIcon iconDelete;
    private ImageIcon iconDiscard;
    private ImageIcon iconPreferences;
    private ImageIcon iconAddOns;
    private ImageIcon iconExtensions;
    private ImageIcon iconLAFs;
    private ImageIcon iconIcons;
    private ImageIcon iconAbout;
    
    /**
     * Constructs default ControlIcons structure
     * (with default icons for controls)
     */
    public ControlIcons() {
        this.iconSave = ICON_SAVE;
        this.iconImport = ICON_IMPORT; 
        this.iconRootCategory = ICON_ROOT_CATEGORY; 
        this.iconCategory = ICON_CATEGORY;
        this.iconRootEntry = ICON_ROOT_ENTRY; 
        this.iconEntry = ICON_ENTRY;
        this.iconChangePassword = ICON_CHANGE_PASSWORD;
        this.iconSearch = ICON_SEARCH;
        this.iconDelete = ICON_DELETE;
        this.iconDiscard = ICON_DISCARD; 
        this.iconPreferences = ICON_PREFERENCES;
        this.iconAddOns = ICON_ADDONS;
        this.iconExtensions = ICON_EXTENSIONS; 
        this.iconLAFs = ICON_LAFS;
        this.iconIcons = ICON_ICONS; 
        this.iconAbout = ICON_ABOUT;
    }

    public ControlIcons(
            ImageIcon iconSave, 
            ImageIcon iconImport, 
            ImageIcon iconRootCategory, 
            ImageIcon iconCategory, 
            ImageIcon iconRootEntry, 
            ImageIcon iconEntry, 
            ImageIcon iconDelete,
            ImageIcon iconChangePassword,
            ImageIcon iconSearch,
            ImageIcon iconDiscard, 
            ImageIcon iconPreferences, 
            ImageIcon iconAddOns, 
            ImageIcon iconExtensions, 
            ImageIcon iconLAFs, 
            ImageIcon iconIcons, 
            ImageIcon iconAbout) {
        this.iconSave = iconSave;
        this.iconImport = iconImport;
        this.iconRootCategory = iconRootCategory;
        this.iconCategory = iconCategory;
        this.iconRootEntry = iconRootEntry;
        this.iconEntry = iconEntry;
        this.iconDelete = iconDelete;
        this.iconChangePassword = iconChangePassword;
        this.iconSearch = iconSearch;
        this.iconDiscard = iconDiscard;
        this.iconPreferences = iconPreferences;
        this.iconAddOns = iconAddOns;
        this.iconExtensions = iconExtensions;
        this.iconLAFs = iconLAFs;
        this.iconIcons = iconIcons;
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

    public ImageIcon getIconDiscard() {
        return iconDiscard;
    }

    public void setIconDiscard(ImageIcon iconDiscard) {
        this.iconDiscard = iconDiscard;
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

    public ImageIcon getIconLAFs() {
        return iconLAFs;
    }

    public void setIconLAFs(ImageIcon iconLAFs) {
        this.iconLAFs = iconLAFs;
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

    public ImageIcon getIconSearch() {
        return iconSearch;
    }

    public void setIconSearch(ImageIcon iconSearch) {
        this.iconSearch = iconSearch;
    }

}
