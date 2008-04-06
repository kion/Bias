/**
 * Created on Apr 6, 2007
 */
package bias.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import bias.Constants;

/**
 * @author kion
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface Preference {
    String title();
    String description() default Constants.EMPTY_STR;
}
