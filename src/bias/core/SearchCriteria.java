/**
 * Created on Jul 6, 2007
 */
package bias.core;

/**
 * @author kion
 */
public class SearchCriteria {

    private String searchExpression;
    private boolean isCaseSensitive;
    private boolean isRegularExpression;

    public SearchCriteria() {
        // default empty constructor
    }
    
    public SearchCriteria(
            String searchExpression,
            boolean isCaseSensitive,
            boolean isRegularExpression) {
        this.searchExpression = searchExpression;
        this.isCaseSensitive = isCaseSensitive;
        this.isRegularExpression = isRegularExpression;
    }
    
    public String getSearchExpression() {
        return searchExpression;
    }
    
    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }
    
    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }
    
    public void setCaseSensitive(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }
    
    public boolean isRegularExpression() {
        return isRegularExpression;
    }
    
    public void setRegularExpression(boolean isRegularExpression) {
        this.isRegularExpression = isRegularExpression;
    }
    
}
