/**
 * Created on Aug 7, 2007
 */
package bias.extension.Reminder;

import java.util.UUID;

/**
 * @author kion
 */
public class ReminderEntry {
    
    private UUID id;

    private String date;

    private String checkmarkDate;

    private String time;

    private String title;

    private String description;

    public ReminderEntry() {
        // default empty constructor
    }
    
    public ReminderEntry(UUID id, String date, String time, String title, String description) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.title = title;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCheckmarkDate() {
		return checkmarkDate;
	}

	public void setCheckmarkDate(String checkmarkDate) {
		this.checkmarkDate = checkmarkDate;
	}

	public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    @Override
    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null 
                && obj instanceof ReminderEntry 
                && this.getId() != null 
                && ((ReminderEntry) obj).getId() != null 
                && this.getId().equals(((ReminderEntry) obj).getId())) { 
            result = true;
        }
        return result;
    }

}
