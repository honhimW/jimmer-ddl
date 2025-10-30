package io.github.honhimw.jddl.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author honhimW
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Check {

    String name() default "";

    /**
     * Support using pattern for path reference like:
     * <pre>@Check("#sbd.benchPress > 100")</pre>
     * Pattern: {@code Pattern.compile("#(?<column>[\\w.]+)")}
     */
    String value();

}
