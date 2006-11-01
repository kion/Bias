/**
 * Created on Nov 1, 2006
 */
package bias.gui;

import java.util.UUID;

/**
 * @author kion
 */
public class VisualEntryDescriptor {
    
    private UUID id;
    
    private Integer categoryNumber;
    
    private Integer number;
    
    private String categoryCaption;

    private String caption;
    
    public VisualEntryDescriptor() {
        // default constructor
    }
    
    public VisualEntryDescriptor(UUID id, Integer categoryNumber, Integer number, String categoryCaption, String caption) {
        this.id = id;
        this.categoryNumber = categoryNumber;
        this.number = number;
        this.categoryCaption = categoryCaption;
        this.caption = caption;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public String getCategoryCaption() {
        return categoryCaption;
    }

    public void setCategoryCaption(String categoryCaption) {
        this.categoryCaption = categoryCaption;
    }

    public Integer getCategoryNumber() {
        return categoryNumber;
    }

    public void setCategoryNumber(Integer categoryNumber) {
        this.categoryNumber = categoryNumber;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return categoryNumber + "/" + categoryCaption + " > " + number + "/" + caption;
    }

}
