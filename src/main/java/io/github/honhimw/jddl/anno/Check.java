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

    String constraint();

}
