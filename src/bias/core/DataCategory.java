/**
 * Oct 31, 2006
 */
package bias.core;

import java.util.Collection;

/**
 * @author kion
 *
 */
public class DataCategory {
	
	private String caption;
	
	private Collection<DataEntry> dataEntries;
	
	public DataCategory() {
		// default constructor
	}

	public DataCategory(String caption, Collection<DataEntry> dataEntries) {
		this.caption = caption;
		this.dataEntries = dataEntries;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public Collection<DataEntry> getDataEntries() {
		return dataEntries;
	}

	public void setDataEntries(Collection<DataEntry> dataEntries) {
		this.dataEntries = dataEntries;
	}

	public boolean removeFromDataEntries(DataEntry dataEntry) {
		return dataEntries.remove(dataEntry);
	}

	public boolean addToDataEntries(DataEntry dataEntry) {
		return dataEntries.add(dataEntry);
	}

}
