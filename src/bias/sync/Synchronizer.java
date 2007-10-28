/**
 * Created on Oct 21, 2007
 */
package bias.sync;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
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
        Properties initialRemoteSyncTable = new Properties();
        initialRemoteSyncTable.putAll(remoteSyncTable);
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
        if (localMetadata == null) {
            localMetadata = remoteMetadata;
        }
        Map<UUID, Character> diffEntries = compareSyncTables(localSyncTable, remoteSyncTable);
        if (!diffEntries.isEmpty() && remoteMetadata != null) {
            modifyMetadata(localMetadata, remoteMetadata, diffEntries);
        }
        if (localMetadata != null) {
            BackEnd.getInstance().setMetadata(localMetadata);
            BackEnd.getInstance().storeMetadata();
        }
        BackEnd.getInstance().setSyncTable(localSyncTable);
        BackEnd.getInstance().storeSyncTable();
        if (!initialRemoteSyncTable.equals(remoteSyncTable)) {
            commitMetadata(localMetadata, remoteMetadata);
            commitSyncTable(remoteSyncTable);
        }
    }

    private void commitSyncTable(Properties remoteSyncTable) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        remoteSyncTable.store(baos, null);
        commit(SYNCTABLE_FILEPATH, baos.toByteArray());
        System.out.println("Commited [sync-table]");
    }

    private void commitMetadata(Document localMetadata, Document remoteMetadata) throws Exception {
        OutputFormat of = new OutputFormat();
        StringWriter sw = new StringWriter();
        new XMLSerializer(sw, of).serialize(localMetadata);
        byte[] encryptedData = BackEnd.getInstance().encrypt(sw.getBuffer().toString().getBytes());
        commit(METADATA_FILEPATH, encryptedData);
        System.out.println("Commited [meta-data]");
    }

    private Map<UUID, Character> compareSyncTables(Properties localSyncTable, Properties remoteSyncTable) throws Exception {
        Map<UUID, Character> diffEntries = new HashMap<UUID, Character>();
        if (!remoteSyncTable.equals(localSyncTable)) {
            // walk through remote sync table
            Properties initialRemoteSynctTable = new Properties();
            initialRemoteSynctTable.putAll(remoteSyncTable);
            for (Entry<Object, Object> entry : initialRemoteSynctTable.entrySet()) {
                String path = (String) entry.getKey();
                String remoteRevisionStr = (String) entry.getValue();
                if (path.charAt(0) == DELETE_MARKER) {
                    // remote file deleted, delete local one as well
                    localSyncTable.remove(path);
                    path = path.substring(1);
                    if (localSyncTable.containsKey(path)) {
                        File file = new File(Constants.ROOT_DIR, path);
                        FSUtils.delete(file);
                        localSyncTable.remove(path);
                        diffEntries.put(getIdFromPath(path), DELETE_MARKER);
                        System.out.println("Checked out [deleted]: " + path);
                    }
                } else {
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
                            localSyncTable.remove(UPDATE_MARKER + path);
                            localRevision++;
                            localSyncTable.setProperty(path, "" + localRevision);
                            remoteSyncTable.setProperty(path, "" + localRevision);
                            System.out.println("Commited [updated]: " + path);
                        }
                    } else {
                        String localDeletedRevisionStr = localSyncTable.getProperty(DELETE_MARKER + path);
                        if (localDeletedRevisionStr != null) {
                            // file removed locally, remove remote one as well
                            delete(path);
                            localSyncTable.remove(DELETE_MARKER + path);
                            remoteSyncTable.remove(path);
                            remoteSyncTable.setProperty(DELETE_MARKER + path, remoteRevisionStr);
                            System.out.println("Commited [deleted]: " + path);
                        } else {
                            String localRevisionStr = localSyncTable.getProperty(path);
                            if (localRevisionStr == null) {
                                // remote file does not exist locally yet, check it out
                                byte[] fileData = checkOut(path);
                                FSUtils.writeFile(new File(Constants.ROOT_DIR, path), fileData);
                                diffEntries.put(getIdFromPath(path), UPDATE_MARKER);
                                // set file revision
                                localSyncTable.setProperty(path, remoteRevisionStr);
                                System.out.println("Checked out [added]: " + path);
                            } else if (!localRevisionStr.equals(remoteRevisionStr)) {
                                int remoteRevision = Integer.parseInt(remoteRevisionStr);
                                int localRevision = Integer.parseInt(localRevisionStr);
                                if (remoteRevision > localRevision) {
                                    // remote file is newer, check it out
                                    byte[] fileData = checkOut(path);
                                    FSUtils.writeFile(new File(Constants.ROOT_DIR, path), fileData);
                                    diffEntries.put(getIdFromPath(path), UPDATE_MARKER);
                                    // set file revision
                                    localSyncTable.setProperty(path, remoteRevisionStr);
                                    System.out.println("Checked out [updated]: " + path);
                                } else {
                                    // local file is newer, commit it
                                    byte[] fileData = FSUtils.readFile(new File(Constants.ROOT_DIR, path));
                                    commit(path, fileData);
                                    remoteSyncTable.setProperty(path, localRevisionStr);
                                    System.out.println("Commited [updated]: " + path);
                                }
                            }
                        }
                    }
                }
            }
            // walk through local sync table
            Properties initialLocalSynctTable = new Properties();
            initialLocalSynctTable.putAll(localSyncTable);
            for (Entry<Object, Object> entry : initialLocalSynctTable.entrySet()) {
                String path = (String) entry.getKey();
                if (path.charAt(0) != DELETE_MARKER) {
                    String localRevisionStr = localSyncTable.getProperty(path);
                    int localRevision = Integer.parseInt(localRevisionStr);
                    if (path.charAt(0) == UPDATE_MARKER) {
                        path = path.substring(1);
                    }
                    String remoteRevisionStr = remoteSyncTable.getProperty(path);
                    if (remoteRevisionStr == null) {
                        // local file does not exist remotely yet, commit it
                        byte[] fileData = FSUtils.readFile(new File(Constants.ROOT_DIR, path));
                        commit(path, fileData);
                        localRevision++;
                        localSyncTable.setProperty(path, "" + localRevision);
                        remoteSyncTable.setProperty(path, "" + localRevision);
                        System.out.println("Commited [added]: " + path);
                    }
                }
            }
        }
        return diffEntries;
    }
    
    private UUID getIdFromPath(String path) {
        path = path.replaceFirst(Constants.PATH_PREFIX_PATTERN, Constants.EMPTY_STR);
        path = path.replaceFirst(Constants.FILE_SUFFIX_PATTERN, Constants.EMPTY_STR);
        return UUID.fromString(path);
    }

    // TODO: root node attributes must be synchronized, tabs order should be preserved
    private void modifyMetadata(Document localMetadata, Document remoteMetadata, Map<UUID, Character> diffEntries) {
        for (Entry<UUID, Character> diffEntry : diffEntries.entrySet()) {
            if (diffEntry.getValue().equals(DELETE_MARKER)) {
                Node root = localMetadata.getFirstChild();
                modifyMetadata(localMetadata, remoteMetadata, diffEntry, root);
            } else if (diffEntry.getValue().equals(UPDATE_MARKER)) {
                Node root = remoteMetadata.getFirstChild();
                modifyMetadata(localMetadata, remoteMetadata, diffEntry, root);
            }
        }
    }

    private void modifyMetadata(Document localMetadata, Document remoteMetadata, Entry<UUID, Character> diffEntry, Node node) {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node n = nodes.item(i);
            if (n.getNodeName().equals(Constants.XML_ELEMENT_ENTRY)) {
                NamedNodeMap attributes = n.getAttributes();
                Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
                UUID id = UUID.fromString(attID.getNodeValue());
                if (diffEntry.getKey().equals(id)) {
                    if (diffEntry.getValue().equals(DELETE_MARKER)) {
                        removeFromMetadata(localMetadata, diffEntry.getKey());
                    } else if (diffEntry.getValue().equals(UPDATE_MARKER)) {
                        Stack<UUID> path = new Stack<UUID>();
                        while (true) {
                            Node pn = n.getParentNode();
                            if (pn.equals(remoteMetadata.getFirstChild())) {
                                break;
                            }
                            attributes = pn.getAttributes();
                            attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
                            id = UUID.fromString(attID.getNodeValue());
                            path.push(id);
                        }
                        addToMetadata(localMetadata, path, n.cloneNode(false));
                    }
                }
            } else if (n.getNodeName().equals(Constants.XML_ELEMENT_CATEGORY)) {
                modifyMetadata(localMetadata, remoteMetadata, diffEntry, n);
            }
        }
    }

    // TODO: added categories must be added
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
        String id = node.getAttributes().getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID).getNodeValue();
        NodeList nodes = n.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node ni = nodes.item(i);
            if (ni.getNodeName().equals(Constants.XML_ELEMENT_ENTRY)) {
                NamedNodeMap attributes = ni.getAttributes();
                Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
                if (id.equals(attID.getNodeValue())) {
                    n.removeChild(ni);
                    break;
                }
            }
        }
        node = metadata.adoptNode(node);
        n.appendChild(node);
    }
    
    // TODO: removed categories must be removed
    private void removeFromMetadata(Document metadata, UUID entryId) {
        Node root = metadata.getFirstChild();
        removeFromMetadata(root, entryId);
    }
    
    private boolean removeFromMetadata(Node node, UUID entryId) {
        NodeList nodes = node.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node ni = nodes.item(i);
            if (ni.getNodeName().equals(Constants.XML_ELEMENT_ENTRY)) {
                NamedNodeMap attributes = ni.getAttributes();
                Node attID = attributes.getNamedItem(Constants.XML_ELEMENT_ATTRIBUTE_ID);
                if (entryId.toString().equals(attID.getNodeValue())) {
                    ni.getParentNode().removeChild(ni);
                    return true;
                }
            } else if (ni.getNodeName().equals(Constants.XML_ELEMENT_CATEGORY)) {
                if (removeFromMetadata(ni, entryId)) {
                    return true;
                }
            }
        }
        return false;
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
