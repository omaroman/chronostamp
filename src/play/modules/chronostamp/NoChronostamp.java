/**
 * Author: OMAROMAN
 * Date: 9/8/11
 * Time: 10:25 AM
 */

package play.modules.chronostamp;
 
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
 
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NoChronostamp {
 
}
