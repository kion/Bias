/**
 * Created on Mar 11; 2008
 */
package bias.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

/**
 * @author kion
 */
public class ExportConfiguration extends TransferConfiguration {

    boolean exportAll;
    private Collection<UUID> selectedIds = new LinkedList<UUID>();
    private Collection<UUID> selectedRecursiveIds = new LinkedList<UUID>();
    private boolean exportPreferences;
    private boolean exportDataEntryConfigs;
    private boolean exportOnlyRelatedDataEntryConfigs;
    private boolean exportToolsData; 
    private boolean exportIcons;
    private boolean exportOnlyRelatedIcons;
    private boolean exportAppCore;
    private boolean exportAddOnsAndLibs;
    private boolean exportAddOnConfigs;
    private boolean exportImportExportConfigs;
    
    public ExportConfiguration() {}

    public ExportConfiguration(
            String transferProvider,
            boolean exportAll,
            boolean exportPreferences, 
            boolean exportDataEntryConfigs, 
            boolean exportOnlyRelatedDataEntryConfigs,
            boolean exportToolsData, 
            boolean exportIcons, 
            boolean exportOnlyRelatedIcons, 
            boolean exportAppCore, 
            boolean exportAddOnsAndLibs, 
            boolean exportAddOnConfigs,
            boolean exportImportExportConfigs, 
            String password) {
        super(transferProvider, password);
        this.exportAll = exportAll;
        this.selectedIds = new LinkedList<UUID>();
        this.selectedRecursiveIds = new LinkedList<UUID>();
        this.exportPreferences = exportPreferences;
        this.exportDataEntryConfigs = exportDataEntryConfigs;
        this.exportOnlyRelatedDataEntryConfigs = exportOnlyRelatedDataEntryConfigs;
        this.exportToolsData = exportToolsData;
        this.exportIcons = exportIcons;
        this.exportOnlyRelatedIcons = exportOnlyRelatedIcons;
        this.exportAppCore = exportAppCore;
        this.exportAddOnsAndLibs = exportAddOnsAndLibs;
        this.exportAddOnConfigs = exportAddOnConfigs;
        this.exportImportExportConfigs = exportImportExportConfigs;
    }

    public boolean isExportAll() {
        return exportAll;
    }

    public void setExportAll(boolean exportAll) {
        this.exportAll = exportAll;
    }

    public Collection<UUID> getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(Collection<UUID> selectedIds) {
        this.selectedIds = selectedIds;
    }

    public Collection<UUID> getSelectedRecursiveIds() {
        return selectedRecursiveIds;
    }

    public void setSelectedRecursiveIds(Collection<UUID> selectedRecursiveIds) {
        this.selectedRecursiveIds = selectedRecursiveIds;
    }

    public boolean isExportPreferences() {
        return exportPreferences;
    }

    public void setExportPreferences(boolean exportPreferences) {
        this.exportPreferences = exportPreferences;
    }

    public boolean isExportDataEntryConfigs() {
        return exportDataEntryConfigs;
    }

    public void setExportDataEntryConfigs(boolean exportDataEntryConfigs) {
        this.exportDataEntryConfigs = exportDataEntryConfigs;
    }

    public boolean isExportOnlyRelatedDataEntryConfigs() {
        return exportOnlyRelatedDataEntryConfigs;
    }

    public void setExportOnlyRelatedDataEntryConfigs(boolean exportOnlyRelatedDataEntryConfigs) {
        this.exportOnlyRelatedDataEntryConfigs = exportOnlyRelatedDataEntryConfigs;
    }

    public boolean isExportToolsData() {
        return exportToolsData;
    }

    public void setExportToolsData(boolean exportToolsData) {
        this.exportToolsData = exportToolsData;
    }

    public boolean isExportIcons() {
        return exportIcons;
    }

    public void setExportIcons(boolean exportIcons) {
        this.exportIcons = exportIcons;
    }

    public boolean isExportOnlyRelatedIcons() {
        return exportOnlyRelatedIcons;
    }

    public void setExportOnlyRelatedIcons(boolean exportOnlyRelatedIcons) {
        this.exportOnlyRelatedIcons = exportOnlyRelatedIcons;
    }

    public boolean isExportAppCore() {
        return exportAppCore;
    }

    public void setExportAppCore(boolean exportAppCore) {
        this.exportAppCore = exportAppCore;
    }

    public boolean isExportAddOnsAndLibs() {
        return exportAddOnsAndLibs;
    }

    public void setExportAddOnsAndLibs(boolean exportAddOnsAndLibs) {
        this.exportAddOnsAndLibs = exportAddOnsAndLibs;
    }

    public boolean isExportAddOnConfigs() {
        return exportAddOnConfigs;
    }

    public void setExportAddOnConfigs(boolean exportAddOnConfigs) {
        this.exportAddOnConfigs = exportAddOnConfigs;
    }

    public boolean isExportImportExportConfigs() {
        return exportImportExportConfigs;
    }

    public void setExportImportExportConfigs(boolean exportImportExportConfigs) {
        this.exportImportExportConfigs = exportImportExportConfigs;
    }

}
