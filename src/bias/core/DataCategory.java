/**
 * Oct 31, 2006
 */
package bias.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

/**
 * @author kion
 *
 */
public class DataCategory extends Recognizable {
    
    private Integer placement;
	
    private Integer activeIndex;
    
    private Collection<Recognizable> data;
    
	public DataCategory() {
		super();
        this.data = new LinkedList<Recognizable>();
	}

	public DataCategory(UUID id, String caption, Collection<Recognizable> data, Integer placement) {
        super(id, caption);
        this.data = data;
        this.placement = placement;
	}

    public Integer getPlacement() {
        return placement;
    }

    public void setPlacement(Integer placement) {
        this.placement = placement;
    }

    public Integer getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(Integer activeIndex) {
        this.activeIndex = activeIndex;
    }

    public Collection<Recognizable> getData() {
        return data;
    }

    public void setData(Collection<Recognizable> data) {
        this.data = data;
    }

    public boolean removeDataItem(Recognizable dataItem) {
        return data.remove(dataItem);
    }

    public boolean addDataItem(Recognizable dataItem) {
        return data.add(dataItem);
    }

    public boolean addDataItems(Collection<Recognizable> dataItems) {
        return data.addAll(dataItems);
    }

}
