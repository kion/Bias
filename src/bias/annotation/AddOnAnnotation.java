/**
 * Created on Jan 5, 2007
 */
package bias.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author kion
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE})
public @interface AddOnAnnotation {
    String description();
    String version();
    String author();
}
