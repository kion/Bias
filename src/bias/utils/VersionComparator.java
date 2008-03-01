/**
 * Created on Feb 29, 2008
 */
package bias.utils;

import java.util.Comparator;

/**
 * @author kion
 */
public class VersionComparator implements Comparator<String> {
    
    public static final String VERSION_PATTERN = "^\\d+\\.\\d+\\.\\d+$";
    
    public static VersionComparator instance;
    
    public static VersionComparator getInstance() {
        if (instance == null) {
            instance = new VersionComparator();
        }
        return instance;
    }

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(String versionA, String versionB) {
        if (Validator.isNullOrBlank(versionA) || Validator.isNullOrBlank(versionB)) {
            throw new IllegalArgumentException("Invalid arguments (none of the arguments can be null/empty)!");
        } else if (!versionA.matches(VERSION_PATTERN) || !versionB.matches(VERSION_PATTERN)) {
            throw new IllegalArgumentException("Invalid arguments (both arguments should match version pattern)!");
        }
        String[] versA = versionA.split("\\.");
        String[] versB = versionB.split("\\.");
        for (int i = 0; i < 3; i++) {
            int a = Integer.valueOf(versA[i]);
            int b = Integer.valueOf(versB[i]);
            if (a < b) {
                return -1; 
            } else if (a > b) {
                return 1;
            } else if (i == 2) {
                return 0;
            }
        }
        return 0;
    }
    
}
