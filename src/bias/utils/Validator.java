/*
 * Created on Oct 16, 2005
 */

package bias.utils;

import bias.global.Constants;


/**
 * @author kion
 */

public class Validator {
    
    public static boolean isNullOrBlank(Object obj) {
        if (obj == null) {
            return true;
        } else {
            if (obj instanceof String) {
                if (((String)obj).matches(Constants.EMPTY_STR_PATTERN) 
                        || Constants.NULL_STR.equalsIgnoreCase((String)obj)) {
                    return true;
                }
            }
        }
        return false;
    }

}
