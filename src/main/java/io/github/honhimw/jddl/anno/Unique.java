package io.github.honhimw.jddl.anno;

import io.github.honhimw.jddl.ConstraintNamingStrategy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author honhimW
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {

    String name() default "";

    /**
     * (Required) An array of the column names that make up the constraint.
     */
    String[] columns();

    Kind kind() default Kind.PATH;

    Class<? extends ConstraintNamingStrategy> naming() default ConstraintNamingStrategy.class;

}
