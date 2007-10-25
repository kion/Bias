/**
 * Created on Oct 21, 2007
 */
package bias.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.UUID;
import java.util.Map.Entry;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bias.Constants;
import bias.Preferences;
import bias.core.BackEnd;
import bias.utils.FSUtils;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author kion
 */

public abstract class Synchronizer {

    /**
     * Available synchronization types
     */
    public static enum SYNC_TYPE {
        LOCAL,
        FTP
    }

    public static final char UPDATE_MARKER = '+';

    public static final char DELETE_MARKER = '-';

    private static final String SYNCTABLE_FILEPATH = Constants.CONFIG_DIR.getName() + Constants.PATH_SEPARATOR + Constants.SYNC_TABLE_FILE;

    private static final String METADATA_FILEPATH = Constants.DATA_DIR.getName() + Constants.PATH_SEPARATOR + Constants.METADATA_FILE_NAME;

    private static Map<SYNC_TYPE, Synchronizer> syncs = new HashMap<SYNC_TYPE, Synchronizer>();

    private static Synchronizer syncInstance;

    /**
     * @return either instance of appropriate class extending this abstract class 
     *         depending on current active synchronization type 
     *         or null if there's no current active synchronization type defined
     */
    @SuppressWarnings("unchecked")
    private static Synchronizer getCurrentActiveSynchronizer() throws Exception {
        if (Preferences.getInstance().syncData && Preferences.getInstance().syncType != null) {
            System.out.println("Active synchronization type: " + Preferences.getInstance().syncType);
            syncInstance = syncs.get(Preferences.getInstance().syncType);
            if (syncInstance == null) {
                String syncClassPackageName = Synchronizer.class.getPackage().getName() + Constants.PACKAGE_PATH_SEPARATOR
                        + Preferences.getInstance().syncType.name().toLowerCase();
                String syncClassName = syncClassPackageName + Constants.PACKAGE_PATH_SEPARATOR
                        + Preferences.getInstance().syncType.name() + Synchronizer.class.getSimpleName();
                Class<? extends Synchronizer> syncClass = (Class<? extends Synchronizer>) Class.forName(syncClassName);
                syncInstance = syncClass.newInstance();
                syncs.put(Preferences.getInstance().syncType, syncInstance);
            }
        } else {
            System.out.println("Synchronization is currently disabled.");
            syncInstance = null;
        }
        return syncInstance;
    }

    public static void synchronize() throws Exception {
        Synchronizer synchronizer = Synchronizer.getCurrentActiveSynchronizer();
        if (synchronizer != null) {
            synchronizer.sync();
        }
    }

    /**
     * Template method.
     * Abstract methods called have to be implemented by extending class.
     */
    private void sync() throws Exception {
        // TODO: locking during synchronization
        System.out.println("Synchronizing...");
        // read local sync table
        Properties localSyncTable = new Properties();
        localSyncTable.putAll(BackEnd.getInstance().getSyncTable());
        // check out remote sync table
        Properties remoteSyncTable = new Properties();
        byte[] rstData = checkOut(SYNCTABLE_FILEPATH);
        if (rstData != null) {
            remoteSyncTable.load(new ByteArrayInputStream(rstData));
        }
        // read local metadata
        Document localMetadata = BackEnd.getInstance().getMetadata();
        // check out remote metadata
        Document remoteMetadata = null;
        byte[] remoteMetaData = checkOut(METADATA_FILEPATH);
        if (remoteMetaData != null) {
            byte[] decryptedData = BackEnd.getInstance().decrypt(remoteMetaData);
            remoteMetadata = new DocumentBuilderFactoryImpl().newDocumentBuilder().parse(new ByteArrayInputStream(decryptedData));
        }
        // parse local & remote sync tables
        Collection<UUID> diffEntriesIDs = compareSyncTables(localSyncTable, remoteSyncTable);
        if (!diffEntriesIDs.isEmpty() && remoteMetadata != null) {
            modifyMetadata(localMetadata, remoteMetadata, diffEntriesIDs);
        }
        saveAndCommitMetadata(localMetadata);
        saveAndCommitSyncTable(localSyncTable);
    }

    private void saveAndCommitMetadata(Document metadata) throws Exception {
        // save...
        BackEnd.getInstance().setMetadata(metadata);
        BackEnd.getInstance().storeMetadata();
        // ... & commit metadata file
        OutputFormat of = new OutputFormat();
        StringWriter sw = new StringWriter();
        new XMLSerializer(sw, of).serialize(metadata);
        byte[] encryptedData = BackEnd.getInstance().encrypt(sw.getBuffer().toString().getBytes());
        commit(METADATA_FILEPATH, encryptedData);

    }

    private void saveAndCommitSyncTable(Properties syncTable) throws Exception {
        // save...
        BackEnd.getInstance().setSyncTable(syncTable);
        BackEnd.getInstance().storeSyncTable();
        // ... & commit synchronization table
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        syncTable.store(baos, null);
        commit(SYNCTABLE_FILEPATH, baos.toByteArray());
    }

    private Collection<UUID> compareSyncTables(Properties localSyncTable, Properties remoteSyncTable) throws Exception {
        Collection<UUID> diffEntriesIDs = new ArrayList<UUID>();
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
                        // TODO: handle conflicting revisions
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
                            if (path.charAt(0) == DELETE_MARKER) {
                                // remote file deleted, delete local one as well
                                localSyncTable.remove(path);
                                path = path.substring(1);
                                File file = new File(Constants.ROOT_DIR, path);
                                FSUtils.delete(file);
                                localSyncTable.remove(path);
                            } else {
                                // remote file does not exist locally yet, check it out
                                byte[] fileData = checkOut(path);
                                FSUtils.writeFile(new File(Constants.ROOT_DIR, path), fileData);
                                String id = path.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
                                id = path.replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                                diffEntriesIDs.add(UUID.fromString(id));
                                // set file revision
                                localSyncTable.setProperty(path, remoteRevisionStr);
                                System.out.println("Checked out [added]: " + path);
                            }
                        } else if (!localRevisionStr.equals(remoteRevisionStr)) {
                            int remoteRevision = Integer.parseInt(remoteRevisionStr);
                            int localRevision = Integer.parseInt(localRevisionStr);
                            if (remoteRevision > localRevision) {
                                // remote file is newer, check it out
                                byte[] fileData = checkOut(path);
                                FSUtils.writeFile(new File(Constants.ROOT_DIR, path), fileData);
                                String id = path.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
                                id = id.replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
                                diffEntriesIDs.add(UUID.fromString(id));
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
                if (path.charAt(0) != DELETE_MARKER) {
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
            }
        }
        return diffEntriesIDs;
    }

    private void modifyMetadata(Document localMetadata, Document remoteMetadata, Collection<UUID> diffEntriesIDs) {
        Node root = remoteMetadata.getFirstChild();
        modifyMetadata(localMetadata, remoteMetadata, diffEntriesIDs, root);
    }

    private void modifyMetadata(Document localMetadata, Document remoteMetadata, Collection<UUID> diffEntriesIDs, Node node) {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeName().equals(Constants.XML_ELEMENT_ENTRY)) {
                NamedNodeMap attributes = n.getAttributes();
                Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
                UUID id = UUID.fromString(attID.getNodeValue());
                for (UUID diffId : diffEntriesIDs) {
                    if (diffId.equals(id)) {
                        Stack<UUID> path = new Stack<UUID>();
                        while (true) {
                            n = n.getParentNode();
                            if (n.equals(remoteMetadata.getFirstChild())) {
                                break;
                            }
                            attributes = n.getAttributes();
                            attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
                            id = UUID.fromString(attID.getNodeValue());
                            path.push(id);
                        }
                        ;
                        addToMetadata(localMetadata, path, n.cloneNode(false));
                        break;
                    }
                }
            } else if (n.getNodeName().equals(Constants.XML_ELEMENT_CATEGORY)) {
                modifyMetadata(localMetadata, remoteMetadata, diffEntriesIDs, n);
            }
        }
    }

    private void addToMetadata(Document metadata, Stack<UUID> path, Node node) {
        Node n = metadata.getFirstChild();
        while (!path.isEmpty()) {
            String nodeId = path.pop().toString();
            NodeList nodes = n.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Node ni = nodes.item(i);
                if (ni.getNodeName().equals(Constants.XML_ELEMENT_CATEGORY)) {
                    NamedNodeMap attributes = n.getAttributes();
                    Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
                    if (nodeId.equals(attID.getNodeValue())) {
                        n = ni;
                        break;
                    }
                }
            }
        }
        String id = n.getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID).getNodeValue();
        NodeList nodes = n.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node ni = nodes.item(i);
            if (ni.getNodeName().equals(Constants.XML_ELEMENT_ENTRY)) {
                NamedNodeMap attributes = n.getAttributes();
                Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
                if (id.equals(attID.getNodeValue())) {
                    n.removeChild(ni);
                    break;
                }
            }
        }
        n.appendChild(node);
    }

    /**
     * Checks file out from synchronization repository.
     * 
     * @param filePath ath of the file to check out, must be a path relative to Bias root directory
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
