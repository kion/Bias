/**
 * Created on Oct 21, 2007
 */
package bias.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import bias.Constants;
import bias.Preferences;
import bias.core.BackEnd;
import bias.utils.FSUtils;



/**
 * @author kion
 */

public abstract class Synchronizer {
    
    /**
     * Available synchronization types.
     */
    public static enum SYNC_TYPE {
        LOCAL,
        FTP
    }
    
    private static Map<SYNC_TYPE, Synchronizer> syncs = new HashMap<SYNC_TYPE, Synchronizer>();
    
    public static final char UPDATE_MARKER = '+';
    
    public static final char DELETE_MARKER = '-';
    
    private static final String syncTableFilePath = Constants.CONFIG_DIR.getName() + Constants.PATH_SEPARATOR + Constants.SYNC_TABLE_FILE;
    
    private static final String metadataFilePath = Constants.DATA_DIR.getName() + Constants.PATH_SEPARATOR + Constants.METADATA_FILE_NAME;

    private static Synchronizer syncInstance;
    
    /**
     * Returns instance of appropriate class extending this abstract class
     * depending on user-defined preferences (synchronization type).
     */
    @SuppressWarnings("unchecked")
    public static Synchronizer getInstance() throws Exception {
        syncInstance = syncs.get(Preferences.getInstance().syncType);
        if (syncInstance == null) {
            String syncClassPackageName = Synchronizer.class.getPackage().getName() + Constants.PACKAGE_PATH_SEPARATOR + Preferences.getInstance().syncType.name().toLowerCase();
            String syncClassName = syncClassPackageName + Constants.PACKAGE_PATH_SEPARATOR + Preferences.getInstance().syncType.name() + Synchronizer.class.getSimpleName();
            Class<? extends Synchronizer> syncClass = (Class<? extends Synchronizer>) Class.forName(syncClassName);
            syncInstance = syncClass.newInstance();
            syncs.put(Preferences.getInstance().syncType, syncInstance);
        }
        return syncInstance;
    }
    
    /**
     * Template method.
     * Abstract methods called inside have to be implemented by extending class.
     */
    public void sync() throws Exception {
        // TODO: locking!!!
        System.out.println("Synchronizing...");
        // read local sync table
        Properties localSyncTable = BackEnd.getInstance().getSyncTable();
        // check out remote sync table
        Properties remoteSyncTable = new Properties();
        byte[] rstData = checkOut(syncTableFilePath);
        if (rstData != null) {
            remoteSyncTable.load(new ByteArrayInputStream(rstData));
        }
        // parse local & remote sync tables
        compareSyncTables(remoteSyncTable, localSyncTable);
        // commit metadata file
        // TODO: metadata file should be merged!!!
        byte[] data = BackEnd.getInstance().getMetadata();
        commit(metadataFilePath, data);
    }
    
    private void compareSyncTables(Properties remoteSyncTable, Properties localSyncTable) throws Exception {
        if (!remoteSyncTable.equals(localSyncTable)) {
            // walk through remote sync table
            for (Entry<Object, Object> entry : remoteSyncTable.entrySet()) {
                String path = (String) entry.getKey();
                String remoteRevisionStr = (String) entry.getValue();
                String localUpdatedRevisionStr = localSyncTable.getProperty(UPDATE_MARKER + path);
                if (localUpdatedRevisionStr != null) {
                    int remoteRevision = Integer.parseInt(remoteRevisionStr);
                    int localRevision = Integer.parseInt(localUpdatedRevisionStr);
                    if (localRevision < remoteRevision) {
                        // TODO
                        // handle conflicting revisions
                        System.out.println("Conflicting revisions! Local: " + localRevision + ", Remote: " + remoteRevision);
                    } else {
                        // commit local file revision
                        byte[] fileData = FSUtils.readFile(new File(Constants.ROOT_DIR, path));
                        commit(path, fileData);
                        localSyncTable.setProperty(path, "" + ++localRevision);
                        localSyncTable.remove(UPDATE_MARKER + path);
                        System.out.println("Commited [updated]: " + path);
                    }
                } else {
                    String localDeletedRevisionStr = localSyncTable.getProperty(DELETE_MARKER + path);
                    if (localDeletedRevisionStr != null) {
                        // file removed locally, remove remote one as well
                        delete(path);
                    } else {
                        String localRevisionStr = localSyncTable.getProperty(path);
                        if (localRevisionStr == null) {
                            if (path.charAt(0) != DELETE_MARKER) {
                                // remote file does not exist locally yet, check it out
                              byte[] fileData = checkOut(path);
                              FSUtils.writeFile(new File(Constants.ROOT_DIR, path), fileData);
                              // set file revision
                              localSyncTable.setProperty(path, remoteRevisionStr);
                              System.out.println("Checked out [added]: " + path);
                            } else {
                                // remote file deleted, delete local one as well
                                path = path.substring(1);
                                File file = new File(Constants.ROOT_DIR, path);
                                FSUtils.delete(file);
                                localSyncTable.remove(path);
                            }
                        } else if (!localRevisionStr.equals(remoteRevisionStr)) {
                            int remoteRevision = Integer.parseInt(remoteRevisionStr);
                            int localRevision = Integer.parseInt(localRevisionStr);
                            if (remoteRevision > localRevision) {
                                // remote file is newer, check it out
                                byte[] fileData = checkOut(path);
                                FSUtils.writeFile(new File(Constants.ROOT_DIR, path), fileData);
                                // set file revision
                                localSyncTable.setProperty(path, remoteRevisionStr);
                                System.out.println("Checked out [updated]: " + path);
                            } else {
                                // local file is newer, commit it
                                byte[] fileData = FSUtils.readFile(new File(Constants.ROOT_DIR, path));
                                commit(path, fileData);
                                System.out.println("Commited [updated]: " + path);
                            }
                        }
                    }
                }
            }
            // walk through local sync table
            for (Entry<Object, Object> entry : localSyncTable.entrySet()) {
                String path = (String) entry.getKey();
                if (path.charAt(0) == UPDATE_MARKER) {
                    path = path.substring(1);
                }
                String remoteRevisionStr = remoteSyncTable.getProperty(path);
                if (remoteRevisionStr == null) {
                    // local file does not exist remotely yet, commit it
                    byte[] fileData = FSUtils.readFile(new File(Constants.ROOT_DIR, path));
                    commit(path, fileData);
                    String localRevisionStr = localSyncTable.getProperty(path);
                    int localRevision = Integer.parseInt(localRevisionStr);
                    localSyncTable.setProperty(path, "" + ++localRevision);
                    System.out.println("Commited [added]: " + path);
                }
            }
            // save...
            BackEnd.getInstance().setSyncTable(localSyncTable);
            BackEnd.getInstance().storeSyncTable();
            // ... & commit synchronization table
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            localSyncTable.store(baos, null);
            commit(syncTableFilePath, baos.toByteArray());
        }
    }
    
    /**
     * Checks file out from synchronization repository.
     * 
     * @param filePath path of the file to check out, must be a path relative to Bias root directory
     * @return data array of bytes containing data of the checked out file
     * @throws Exception
     */
    protected abstract byte[] checkOut(String filePath) throws Exception;

    /**
     * Commits file into synchronization repository.
     * 
     * @param filePath path of the file to commit, must be a path relative to Bias root directory
     * @param data array of bytes containing data to commit
     * @throws Exception
     */
    protected abstract void commit(String filePath, byte[] data) throws Exception;
    
    /**
     * Removes file from synchronization repository.
     * 
     * @param filePath path of the file to remove, must be a path relative to Bias root directory
     * @throws Exception
     */
    protected abstract void delete(String filePath) throws Exception;

}
