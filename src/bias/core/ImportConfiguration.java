/**
 * Created on Mar 11; 2008
 */
package bias.core;

/**
 * @author kion
 */
public class ImportConfiguration extends TransferConfiguration {

    private boolean importDataEntries;
    private boolean overwriteDataEntries;
    private boolean importDataEntryConfigs;
    private boolean overwriteDataEntryConfigs;
    private boolean importPrefs;
    private boolean overwritePrefs;
    private boolean importToolsData;
    private boolean overwriteToolsData;
    private boolean importIcons;
    private boolean overwriteIcons;
    private boolean importAndUpdateAppCore;
    private boolean importAddOnsAndLibs;
    private boolean updateInstalledAddOnsAndLibs;
    private boolean importAddOnConfigs;
    private boolean overwriteAddOnConfigs;
    private boolean importImportExportConfigs;
    private boolean overwriteImportExportConfigs;
    
    public ImportConfiguration() {}
    
    public ImportConfiguration(
            String transferProvider,
            boolean importDataEntries, 
            boolean overwriteDataEntries, 
            boolean importDataEntryConfigs, 
            boolean overwriteDataEntryConfigs, 
            boolean importPrefs,
            boolean overwritePrefs, 
            boolean importToolsData, 
            boolean overwriteToolsData, 
            boolean importIcons,
            boolean overwriteIcons, 
            boolean importAndUpdateAppCore, 
            boolean importAddOnsAndLibs, 
            boolean updateInstalledAddOnsAndLibs, 
            boolean importAddOnConfigs,
            boolean overwriteAddOnConfigs, 
            boolean importImportExportConfigs, 
            boolean overwriteImportExportConfigs, 
            String password) {
        super(transferProvider, password);
        this.importDataEntries = importDataEntries;
        this.overwriteDataEntries = overwriteDataEntries;
        this.importDataEntryConfigs = importDataEntryConfigs;
        this.overwriteDataEntryConfigs = overwriteDataEntryConfigs;
        this.importPrefs = importPrefs;
        this.overwritePrefs = overwritePrefs;
        this.importToolsData = importToolsData;
        this.overwriteToolsData = overwriteToolsData;
        this.importIcons = importIcons;
        this.overwriteIcons = overwriteIcons;
        this.importAndUpdateAppCore = importAndUpdateAppCore;
        this.importAddOnsAndLibs = importAddOnsAndLibs;
        this.updateInstalledAddOnsAndLibs = updateInstalledAddOnsAndLibs;
        this.importAddOnConfigs = importAddOnConfigs;
        this.overwriteAddOnConfigs = overwriteAddOnConfigs;
        this.importImportExportConfigs = importImportExportConfigs;
        this.overwriteImportExportConfigs = overwriteImportExportConfigs;
    }

    public boolean isImportDataEntries() {
        return importDataEntries;
    }

    public void setImportDataEntries(boolean importDataEntries) {
        this.importDataEntries = importDataEntries;
    }

    public boolean isOverwriteDataEntries() {
        return overwriteDataEntries;
    }

    public void setOverwriteDataEntries(boolean overwriteDataEntries) {
        this.overwriteDataEntries = overwriteDataEntries;
    }

    public boolean isImportDataEntryConfigs() {
        return importDataEntryConfigs;
    }

    public void setImportDataEntryConfigs(boolean importDataEntryConfigs) {
        this.importDataEntryConfigs = importDataEntryConfigs;
    }

    public boolean isOverwriteDataEntryConfigs() {
        return overwriteDataEntryConfigs;
    }

    public void setOverwriteDataEntryConfigs(boolean overwriteDataEntryConfigs) {
        this.overwriteDataEntryConfigs = overwriteDataEntryConfigs;
    }

    public boolean isImportPrefs() {
        return importPrefs;
    }

    public void setImportPrefs(boolean importPrefs) {
        this.importPrefs = importPrefs;
    }

    public boolean isOverwritePrefs() {
        return overwritePrefs;
    }

    public void setOverwritePrefs(boolean overwritePrefs) {
        this.overwritePrefs = overwritePrefs;
    }

    public boolean isImportToolsData() {
        return importToolsData;
    }

    public void setImportToolsData(boolean importToolsData) {
        this.importToolsData = importToolsData;
    }

    public boolean isOverwriteToolsData() {
        return overwriteToolsData;
    }

    public void setOverwriteToolsData(boolean overwriteToolsData) {
        this.overwriteToolsData = overwriteToolsData;
    }

    public boolean isImportIcons() {
        return importIcons;
    }

    public void setImportIcons(boolean importIcons) {
        this.importIcons = importIcons;
    }

    public boolean isOverwriteIcons() {
        return overwriteIcons;
    }

    public void setOverwriteIcons(boolean overwriteIcons) {
        this.overwriteIcons = overwriteIcons;
    }

    public boolean isImportAndUpdateAppCore() {
        return importAndUpdateAppCore;
    }

    public void setImportAndUpdateAppCore(boolean importAndUpdateAppCore) {
        this.importAndUpdateAppCore = importAndUpdateAppCore;
    }

    public boolean isImportAddOnsAndLibs() {
        return importAddOnsAndLibs;
    }

    public void setImportAddOnsAndLibs(boolean importAddOnsAndLibs) {
        this.importAddOnsAndLibs = importAddOnsAndLibs;
    }

    public boolean isUpdateInstalledAddOnsAndLibs() {
        return updateInstalledAddOnsAndLibs;
    }

    public void setUpdateInstalledAddOnsAndLibs(boolean updateInstalledAddOnsAndLibs) {
        this.updateInstalledAddOnsAndLibs = updateInstalledAddOnsAndLibs;
    }

    public boolean isImportAddOnConfigs() {
        return importAddOnConfigs;
    }

    public void setImportAddOnConfigs(boolean importAddOnConfigs) {
        this.importAddOnConfigs = importAddOnConfigs;
    }

    public boolean isOverwriteAddOnConfigs() {
        return overwriteAddOnConfigs;
    }

    public void setOverwriteAddOnConfigs(boolean overwriteAddOnConfigs) {
        this.overwriteAddOnConfigs = overwriteAddOnConfigs;
    }

    public boolean isImportImportExportConfigs() {
        return importImportExportConfigs;
    }

    public void setImportImportExportConfigs(boolean importImportExportConfigs) {
        this.importImportExportConfigs = importImportExportConfigs;
    }

    public boolean isOverwriteImportExportConfigs() {
        return overwriteImportExportConfigs;
    }

    public void setOverwriteImportExportConfigs(boolean overwriteImportExportConfigs) {
        this.overwriteImportExportConfigs = overwriteImportExportConfigs;
    }

}
