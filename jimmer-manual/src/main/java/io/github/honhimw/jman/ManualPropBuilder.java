package io.github.honhimw.jman;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.PropId;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Property configuration
 */
public class ManualPropBuilder<SELF extends ManualPropBuilder<SELF>> {
    protected final ManualImmutablePropImpl prop;
    protected final List<Annotation> annotations = new ArrayList<>();
    protected Annotation generatedValue = null;
    protected final DefaultColumn column = new DefaultColumn();
    protected boolean primaryKey = false;

    public ManualPropBuilder() {
        this(new ManualImmutablePropImpl());
    }

    public ManualPropBuilder(ManualImmutablePropImpl prop) {
        this.prop = prop;
        prop.isColumnDefinition = true;
        prop.hasStorage = true;
        prop.dependencies = Collections.emptyList();
    }

    public SELF primaryKey() {
        this.primaryKey = true;
        return self();
    }

    /**
     * auto-increment on id
     *
     * @return the current instance
     */
    public SELF autoIncrement() {
        if (generatedValue == null) {
            generatedValue = new DefaultGeneratedValue();
            addAnnotation(generatedValue);
        }
        return self();
    }

    /**
     * Set the property name. This is the logical property name, not the column name,
     * even though they may look similar(the column name derive from property name).
     *
     * @param name property name
     * @return the current instance
     */
    public SELF name(String name) {
        prop.name = name;
        prop.id = PropId.byName(name);
        return self();
    }

    /**
     * set the column name without sneaking
     *
     * @param columnName the column name
     * @return the current instance
     */
    public SELF columnName(String columnName) {
        column.name = columnName;
        return self();
    }

    /**
     * Set the property type, will be auto-mapped to jdbc-type for DDL generation.
     *
     * @param type java type
     * @return the current instance
     */
    public SELF type(Class<?> type) {
        prop.returnClass = type;
        prop.elementClass = type;
        return self();
    }

    /**
     * Add annotation on the property.
     *
     * @param annotation annotation
     * @return the current instance
     */
    public SELF addAnnotation(Annotation annotation) {
        annotations.add(annotation);
        return self();
    }

    public SELF apply(Consumer<ManualImmutablePropImpl> editor) {
        editor.accept(prop);
        return self();
    }

    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    public ImmutableProp build() {
        prop.annotations = annotations.toArray(new Annotation[0]);
        Objects.requireNonNull(prop.name, "`name` must not be null");
        Objects.requireNonNull(prop.returnClass, "`type` must not be null");
        return prop;
    }

}
