/**
 * Created on Jan 5, 2007
 */
package bias.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author kion
 */

@Retention(RetentionPolicy.RUNTIME)
public @interface AddOnAnnotation {
    String description();
    String version();
    String author();
}
