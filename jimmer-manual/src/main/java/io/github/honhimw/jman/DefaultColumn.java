package io.github.honhimw.jman;

import org.babyfish.jimmer.sql.Column;

import java.lang.annotation.Annotation;

/**
 * @author honhimW
 * @since 2025-11-13
 */
public class DefaultColumn implements Column {
    public String name = "";
    public String type = "";
    public String sqlElementType = "";

    @Override
    public String name() {
        return name;
    }

    @Override
    public String sqlType() {
        return type;
    }

    @Override
    public String sqlElementType() {
        return sqlElementType;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Column.class;
    }
}
