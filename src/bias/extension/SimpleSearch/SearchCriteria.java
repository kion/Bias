/**
 * Created on Jul 6, 2007
 */
package bias.extension.SimpleSearch;


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

    /**
     * @return the searchExpression
     */
    public String getSearchExpression() {
        return searchExpression;
    }

    /**
     * @param searchExpression the searchExpression to set
     */
    public void setSearchExpression(String searchExpression) {
        this.searchExpression = searchExpression;
    }

    /**
     * @return the isCaseSensitive
     */
    public boolean isCaseSensitive() {
        return isCaseSensitive;
    }

    /**
     * @param isCaseSensitive the isCaseSensitive to set
     */
    public void setCaseSensitive(boolean isCaseSensitive) {
        this.isCaseSensitive = isCaseSensitive;
    }

    /**
     * @return the isRegularExpression
     */
    public boolean isRegularExpression() {
        return isRegularExpression;
    }

    /**
     * @param isRegularExpression the isRegularExpression to set
     */
    public void setRegularExpression(boolean isRegularExpression) {
        this.isRegularExpression = isRegularExpression;
    }

}
