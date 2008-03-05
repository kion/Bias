/**
 * Created on Feb 29, 2008
 */
package bias.utils;

import java.util.Comparator;

/**
 * @author kion
 */
public class VersionComparator implements Comparator<String> {
    
    public static final String VERSION_PATTERN = "^\\d+\\.\\d+(\\.\\d+)*+$";
    private static final String VERSION_SEPARATOR = "\\.";
    
    private static VersionComparator instance;
    
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
        String[] versA = versionA.split(VERSION_SEPARATOR);
        String[] versB = versionB.split(VERSION_SEPARATOR);
        int len = versA.length >= versB.length ? versA.length : versB.length;
        for (int i = 0; i < len; i++) {
            if (i > versA.length - 1) {
                return -1;
            } else if (i > versB.length - 1) {
                return 1;
            }
            float a;
            float b;
            if (versA[i].startsWith("0")) {
                a = Float.valueOf("0." + versA[i]);
            } else {
                a = Float.valueOf(versA[i]);
            }
            if (versB[i].startsWith("0")) {
                b = Float.valueOf("0." + versB[i]);
            } else {
                b = Float.valueOf(versB[i]);
            }
            if (a < b) {
                return -1; 
            } else if (a > b) {
                return 1;
            }
        }
        return 0;
    }
    
}
