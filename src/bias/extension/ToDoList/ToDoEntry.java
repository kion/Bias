/**
 * Created on Aug 7, 2007
 */
package bias.extension.ToDoList;

import java.util.Date;
import java.util.UUID;

/**
 * @author kion
 */
public class ToDoEntry {
    
    private UUID id;

    private Date timestamp;

    private String title;

    private String description;

    private String priority;

    private String status;

    public ToDoEntry() {
        // default empty constructor
    }
    
    public ToDoEntry(UUID id, Date timestamp, String title, String description, String priority, String status) {
        this.id = id;
        this.timestamp = timestamp;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null 
                && obj instanceof ToDoEntry 
                && this.getId() != null 
                && ((ToDoEntry) obj).getId() != null 
                && this.getId().equals(((ToDoEntry) obj).getId())) { 
            result = true;
        }
        return result;
    }

}
