package io.github.honhimw.jman;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.PropId;
import org.babyfish.jimmer.sql.Embeddable;
import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.ManyToOne;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author honhimW
 * @since 2025-10-23
 */

public class ManualTypeBuilder<SELF extends ManualTypeBuilder<SELF, PROP_BUILDER, FK_BUILDER>, PROP_BUILDER extends ManualPropBuilder<PROP_BUILDER>, FK_BUILDER extends ManualTypeBuilder.FK<FK_BUILDER, PROP_BUILDER>> {

    public static final Entity ENTITY = new Entity() {
        @Override
        public String microServiceName() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Entity.class;
        }
    };

    public static final Embeddable EMBEDDABLE = new Embeddable() {
        @Override
        public Class<? extends Annotation> annotationType() {
            return Embeddable.class;
        }
    };
    public static final ManyToOne MANY_TO_ONE = new ManyToOne() {
        @Override
        public boolean inputNotNull() {
            return false;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ManyToOne.class;
        }
    };

    protected final ManualImmutableTypeImpl type = new ManualImmutableTypeImpl();

    protected final List<PROP_BUILDER> id = new ArrayList<>();
    protected final List<PROP_BUILDER> other = new ArrayList<>();

    public static ManualTypeBuilder<?, ?, ?> of(String tableName) {
        ManualTypeBuilder<?, ?, ?> builder = new ManualTypeBuilder<>();
        return builder.tableName(tableName);
    }

    protected ManualTypeBuilder() {
        initType(type);
    }

    protected void initType(ManualImmutableTypeImpl type) {
        type.javaClass = Object.class;
        Map<String, ImmutableProp> props = new LinkedHashMap<>();
        type.props = props;
        type.selectableProps = props;
        type.superTypes = Collections.emptySet();
    }

    /**
     * Set the table name.
     *
     * @param tableName table name
     * @return the current instance
     */
    public SELF tableName(String tableName) {
        type.tableName = tableName;
        return self();
    }

    /**
     * Add a column on the table.
     *
     * @param name property name
     * @param type java type
     * @return the current instance
     */
    public SELF addColumn(String name, Class<?> type) {
        return addColumn(column -> column.name(name).type(type));
    }

    /**
     * Add a column on the table.
     *
     * @param c column configurer
     * @return the current instance
     */
    public SELF addColumn(Consumer<PROP_BUILDER> c) {
        PROP_BUILDER column = propBuilder();
        c.accept(column);
        if (column.primaryKey) {
            id.add(column);
        } else {
            other.add(column);
        }

        return self();
    }

    /**
     * Add relation on the column.
     *
     * @param c foreign-key and referenced table id column configurer
     * @return the current instance
     */
    public SELF addRelation(Consumer<FK_BUILDER> c) {
        FK_BUILDER fk = fkBuilder();
        // Construct dependent type & type#id

        c.accept(fk);
        _assert(isNotBlank(fk.propName), "`referenceProp.name` should not be blank");
        _assert(fk.referenceType != null || fk.idConsumer != null, "either `fk.referenceType` or `fk.consumer` should be set");
        ManualImmutablePropImpl prop = new ManualImmutablePropImpl();
        prop.name = fk.propName;
        prop.isId = false;
        prop.isTargetForeignKeyReal = true;
        prop.returnClass = Object.class;
        prop.isReference = true;
        if (fk.referenceType != null) {
            prop.targetType = fk.referenceType;
        } else {
            // Construct dependent type & type#id
            ManualImmutableTypeImpl referencedType = new ManualImmutableTypeImpl();
            initType(referencedType);

            ManualImmutablePropImpl idProp = new ManualImmutablePropImpl();
            idProp.isId = true;
            idProp.declaringType = referencedType;
            referencedType.idProp = idProp;
            PROP_BUILDER idColumn = propBuilder(idProp);
            fk.idConsumer.accept(idColumn);
            referencedType.tableName = fk.tableName;
            _assert(isNotBlank(idProp.name), "`referencedId.name` should not be blank");
            _assert(idProp.returnClass != null, "`referencedId.returnClass` should not be null");

            referencedType.props.put(idProp.name, idProp);

            // Construct reference prop
            prop.targetType = referencedType;
        }
        PROP_BUILDER referenceColumn = propBuilder(prop);
        if (fk.selfConsumer != null) {
            fk.selfConsumer.accept(referenceColumn);
        }

        prop.declaringType = type;
        prop.isAssociation = true;
        prop.associationAnnotation = MANY_TO_ONE;
        prop.id = PropId.byName(prop.name);

        type.isAssignableFrom = true;
        other.add(referenceColumn);

        return self();
    }

    public SELF apply(Consumer<ManualImmutableTypeImpl> editor) {
        editor.accept(type);
        return self();
    }

    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    @SuppressWarnings("unchecked")
    protected PROP_BUILDER propBuilder() {
        return (PROP_BUILDER) new ManualPropBuilder<>();
    }

    @SuppressWarnings("unchecked")
    protected PROP_BUILDER propBuilder(ManualImmutablePropImpl prop) {
        return (PROP_BUILDER) new ManualPropBuilder<>(prop);
    }

    @SuppressWarnings("unchecked")
    protected FK_BUILDER fkBuilder() {
        return (FK_BUILDER) new FK<>();
    }

    /**
     * Build the manually configured ImmutableType.
     *
     * @return immutable-type
     */
    public ImmutableType build() {
        _assert(!id.isEmpty(), "`id` should not be empty");
        _assert(isNotBlank(type.tableName), "`type.tableName` should not be blank");

        ManualImmutablePropImpl idProp;
        if (id.size() == 1) {
            PROP_BUILDER idColumn = id.get(0);
            idProp = (ManualImmutablePropImpl) idColumn.build();
        } else {
            idProp = new ManualImmutablePropImpl();
            idProp.name = "id";
            idProp.isEmbedded = true;
            ManualImmutableTypeImpl embeddedIdType = new ManualImmutableTypeImpl();
            initType(embeddedIdType);
            embeddedIdType.immutableAnnotation = EMBEDDABLE;
            idProp.targetType = embeddedIdType;
            for (PROP_BUILDER column : id) {
                ManualImmutablePropImpl prop = (ManualImmutablePropImpl) column.build();
                embeddedIdType.selectableProps.put(prop.name, prop);
                prop.declaringType = embeddedIdType;
            }
        }

        idProp.isId = true;
        idProp.declaringType = type;
        type.idProp = idProp;
        type.props.put(idProp.name, idProp);
        type.allTypes = Collections.singleton(type);
        type.isEntity = true;
        type.immutableAnnotation = ENTITY;
        type.draftFactory = (draftContext, o) -> new ManualDraftSpi(type, draftContext, o);

        for (PROP_BUILDER column : this.other) {
            ManualImmutablePropImpl prop = (ManualImmutablePropImpl) column.build();
            type.props.put(prop.name, prop);
            prop.declaringType = type;
        }

        return type;
    }

    public static class FK<SELF extends FK<SELF, PROP_BUILDER>, PROP_BUILDER extends ManualPropBuilder<PROP_BUILDER>> {
        protected String tableName;
        protected String propName;
        protected ImmutableType referenceType;
        protected Consumer<PROP_BUILDER> idConsumer;
        protected Consumer<PROP_BUILDER> selfConsumer;

        protected FK() {

        }

        /**
         * Set referenced table name
         *
         * @param tableName referenced table name
         * @return the current instance
         */
        public SELF tableName(String tableName) {
            this.tableName = tableName;
            return self();
        }

        /**
         * Set reference property name
         *
         * @param propName property name
         * @return the current instance
         */
        public SELF propName(String propName) {
            this.propName = propName;
            return self();
        }

        /**
         * Set referenced type, useful when the type is a pre-constructed.
         *
         * @param type referenced table type
         * @return the current instance
         * @see #id(Consumer) either using this or using id(Consumer)
         */
        public SELF type(ImmutableType type) {
            this.referenceType = type;
            return self();
        }

        /**
         * Configure the referenced table id column.
         * Auto build a single column type for generation.
         *
         * @param c id column configurer
         * @return the current instance
         * @see #type(ImmutableType) either using this or using type(ImmutableType)
         */
        public SELF id(Consumer<PROP_BUILDER> c) {
            if (this.idConsumer != null) {
                this.idConsumer = this.idConsumer.andThen(c);
            } else {
                this.idConsumer = c;
            }
            return self();
        }

        /**
         * Configure the column reference to the referenced type.
         *
         * @param c foreign-key column configurer
         * @return the current instance
         */
        public SELF self(Consumer<PROP_BUILDER> c) {
            if (this.selfConsumer != null) {
                this.selfConsumer = this.selfConsumer.andThen(c);
            } else {
                this.selfConsumer = c;
            }
            return self();
        }

        @SuppressWarnings("unchecked")
        protected SELF self() {
            return (SELF) this;
        }

    }

    protected static <T> List<T> asList(T[] array) {
        if (array == null) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>(array.length);
        list.addAll(Arrays.asList(array));
        return list;
    }

    protected static boolean isNotBlank(String string) {
        return string != null && !string.trim().isEmpty();
    }

    protected static void _assert(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

}
