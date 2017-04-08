/**
 * Created on Apr 13, 2008
 */
package bias.extension.DashBoard.snippet;

import java.awt.Container;
import java.awt.Dimension;
import java.util.Collection;
import java.util.UUID;

import javax.swing.JInternalFrame;
import javax.swing.border.EtchedBorder;

/**
 * @author kion
 */
public abstract class InfoSnippet extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    
    private UUID dataEntryID;
    private UUID id;
    private byte[] content;
    private byte[] settings;
    
    /**
     * @param id snippet-class instance identifier
     * @param content bytes array representing content of the snippet-class instance
     * @param settings bytes array representing settings of the snippet-class instance
     * @param resizable Specifies whether snippet-class frames will be resizable
     * @param closable  Specifies whether snippet-class frames will be closable
     */
    public InfoSnippet(UUID dataEntryID, UUID id, byte[] content, byte[] settings, boolean resizable, boolean closable) {
        super(null, resizable, closable, false, true);
        this.dataEntryID = dataEntryID;
        this.id = id;
        this.content = content;
        this.settings = settings;
        setContentPane(getRepresentation());
        setBorder(new EtchedBorder(EtchedBorder.RAISED));
    }
    
	public UUID getDataEntryID() {
		return dataEntryID;
	}

    public UUID getId() {
        return id;
    }

    protected byte[] getContent() {
        return content;
    }
    
    protected byte[] getSettings() {
        return settings;
    }
    
    /**
     * Returns representation container of certain snippet-class.
     * This is actual content representation shown inside snippet. 
     */
    protected abstract Container getRepresentation();

    /**
     * Serializes content of the snippet.
     * Should be overridden by certain snippet-class.
     *  
     * @return array of bytes representing serialized data of snippet-class instance
     */
    public abstract byte[] serializeContent();
    
    /**
     * Serializes snippet's settings.
     * Should be overridden by certain snippet-class. 
     * 
     * @return array of bytes representing serialized settings of snippet-class instance
     */
    public byte[] serializeSettings() {
        return null;
    }
    
    /**
     * Performs configuration of certain snippet-class instance.
     * Should be overridden to perform configuration of certain snippet-class instance.
     * By default does nothing.
     */
    public void configure() throws Throwable {}
    
    /**
     * Returns search data for certain snippet-class instance.
     * Should be overridden to return search data for certain snippet-class instance.
     * By default returns null (no search data provided). 
     * 
     * @return data for search provided by snippet
     */
    public Collection<String> getSearchData() {
        return null;
    }

    /**
     * Highlights search results, if any.
     * Should be overridden to perform actual search results highlighting for certain snippet-class instance.
     * Does nothing by default.
     */
    public void highlightSearchResults(String searchExpression, boolean isCaseSensitive, boolean isRegularExpression) throws Throwable {
        // do nothing by default
    }

    /**
     * Clears search results highlights, if any.
     * Should be overridden to perform actual search results highlights clearance for certain snippet-class instance.
     * Does nothing by default.
     */
    public void clearSearchResultsHighlight() throws Throwable {
        // do nothing by default
    }
    
    /**
     * If certain snippet-class uses attachments, this method should be overridden
     * to return list of referenced attachment names. This is needed to properly 
     * clean-up unused attachments once they are not referenced anymore.  
     */
    public Collection<String> getReferencedAttachmentNames(){
    	return null;
    };
    
    /* (non-Javadoc)
     * @see javax.swing.JComponent#getMinimumSize()
     */
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(150, 150);
    }

}
