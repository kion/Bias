/**
 * Created on Apr 6, 2007
 */
package bias.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author kion
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface PreferenceAnnotation {
    String title();
    String description();
}
