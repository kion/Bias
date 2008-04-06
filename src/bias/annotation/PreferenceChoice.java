/**
 * Created on Apr 6, 2008
 */
package bias.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import bias.Preferences;

/**
 * @author kion
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.FIELD})
public @interface PreferenceChoice {
    Class<? extends Preferences.PreferenceChoiceProvider> providerClass();
    boolean isEditable() default false;
}
