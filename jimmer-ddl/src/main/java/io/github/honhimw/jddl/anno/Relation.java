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
public @interface Relation {

    String name() default "";

    String definition() default "";

    OnDeleteAction action() default OnDeleteAction.NONE;

    Class<? extends ConstraintNamingStrategy> naming() default ConstraintNamingStrategy.class;

}
