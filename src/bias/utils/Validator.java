/**
 * Oct 31, 2006
 */
package bias.utils;

import bias.global.Constants;

/**
 * @author kion
 *
 */
public class Validator {
	
	public static boolean isNullOrBlank(Object obj) {
		boolean isNullOrBlank = false;
		if (obj == null) {
			isNullOrBlank = true;
		} else {
			String str = null;
			if (obj instanceof String) {
				str = (String) obj;
			} else if (obj instanceof StringBuffer) {
				str = ((StringBuffer) obj).toString();
			}
			if (str != null) {
				if (Constants.EMPTY_STR.equals(str)
						|| str.matches("(?s)\\s+")) {
					isNullOrBlank = true;
				}
			}
		}
		return isNullOrBlank;
	}

}
