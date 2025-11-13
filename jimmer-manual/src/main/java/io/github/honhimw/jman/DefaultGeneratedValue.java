package io.github.honhimw.jman;

import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.GenerationType;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;

import java.lang.annotation.Annotation;

/**
 * @author honhimW
 * @since 2025-11-13
 */
public class DefaultGeneratedValue implements GeneratedValue {
    public GenerationType strategy = GenerationType.AUTO;
    public Class<? extends UserIdGenerator<?>> generatorType = UserIdGenerator.None.class;
    public String generatorRef = "";
    public String sequenceName = "";

    @Override
    public GenerationType strategy() {
        return strategy;
    }

    @Override
    public Class<? extends UserIdGenerator<?>> generatorType() {
        return generatorType;
    }

    @Override
    public String generatorRef() {
        return generatorRef;
    }

    @Override
    public String sequenceName() {
        return sequenceName;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return GeneratedValue.class;
    }
}
