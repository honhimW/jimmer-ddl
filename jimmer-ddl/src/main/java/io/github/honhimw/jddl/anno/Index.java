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
public @interface Index {

    String name() default "";

    /**
     * (Required) The names of the columns to be included in the index,
     * in order.
     */
    String[] columns();

    /**
     * (Optional) Whether the index is unique.
     */
    boolean unique() default false;

    Kind kind() default Kind.PATH;

    Class<? extends ConstraintNamingStrategy> naming() default ConstraintNamingStrategy.class;

}
