package io.github.honhimw.jddl.manual;

import io.github.honhimw.jddl.DDLUtils;
import io.github.honhimw.jddl.anno.*;
import io.github.honhimw.jman.ManualDraftSpi;
import io.github.honhimw.jman.ManualImmutablePropImpl;
import io.github.honhimw.jman.ManualImmutableTypeImpl;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.PropId;
import org.babyfish.jimmer.sql.Entity;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author honhimW
 * @since 2025-10-23
 */

public class ManualTypeBuilder {

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

    private final ManualImmutableTypeImpl type = new ManualImmutableTypeImpl();

    private final DDLUtils.DefaultTableDef tableDef = new DDLUtils.DefaultTableDef();

    /**
     * varchar(255) id builder
     *
     * @param id id column name
     * @return new builder
     */
    public static ManualTypeBuilder string(String id) {
        return of(column -> column.name(id).type(String.class));
    }

    /**
     * int32 auto-increment id builder
     *
     * @param id id column name
     * @return new builder
     */
    public static ManualTypeBuilder u32(String id) {
        return of(column -> column.name(id).type(Integer.class).addAnnotation(new DDLUtils.DefaultGeneratedValue()));
    }

    /**
     * int64 auto-increment id builder
     *
     * @param id id column name
     * @return new builder
     */
    public static ManualTypeBuilder u64(String id) {
        return of(column -> column.name(id).type(Long.class).addAnnotation(new DDLUtils.DefaultGeneratedValue()));
    }

    /**
     * The table must have an primary-key column
     *
     * @param id id column configurer
     * @return new builder
     */
    public static ManualTypeBuilder of(Consumer<Column> id) {
        ManualTypeBuilder builder = new ManualTypeBuilder();
        ManualImmutablePropImpl prop = new ManualImmutablePropImpl();
        prop.isId = true;

        Column column = new Column(prop);
        id.accept(column);
        column.addAnnotation(column.columnDef);
        _assert(isNotBlank(prop.name), "`prop.name` should not be blank");
        _assert(prop.returnClass != null, "`prop.returnClass` should not be null");

        ManualImmutableTypeImpl type = builder.type;
        type.idProp = prop;
        type.props.put(prop.name, prop);
        prop.declaringType = type;
        type.allTypes = Collections.singleton(type);
        type.isEntity = true;
        type.immutableAnnotation = ENTITY;
        type.draftFactory = (draftContext, o) -> new ManualDraftSpi(type, draftContext, o);
        return builder;
    }

    private ManualTypeBuilder() {
        type.javaClass = Object.class;
        Map<String, ImmutableProp> props = new LinkedHashMap<>();
        type.props = props;
        type.selectableProps = props;
        type.superTypes = Collections.emptySet();
        type.annotations = new Annotation[]{tableDef};
    }

    /**
     * Set the table name.
     *
     * @param tableName table name
     * @return the current instance
     */
    public ManualTypeBuilder tableName(String tableName) {
        type.tableName = tableName;
        return this;
    }

    /**
     * Set the comment on table.
     *
     * @param comment comment
     * @return the current instance
     */
    public ManualTypeBuilder comment(String comment) {
        tableDef.comment = comment;
        return this;
    }

    /**
     * Set the table-type for MySQL e.g. InnoDB
     *
     * @param tableType MySQL table type
     * @return the current instance
     */
    public ManualTypeBuilder tableType(String tableType) {
        tableDef.tableType = tableType;
        return this;
    }

    /**
     * Add index on the table.
     *
     * @param index index definition
     * @return the current instance
     */
    public ManualTypeBuilder addIndex(Index index) {
        List<Index> list = asList(tableDef.indexes);
        list.add(index);
        tableDef.indexes = list.toArray(new Index[0]);
        return this;
    }

    /**
     * Add index on the table.
     *
     * @param kind    columns reference kind
     * @param columns index columns
     * @return the current instance
     */
    public ManualTypeBuilder addIndex(Kind kind, String... columns) {
        List<Index> list = asList(tableDef.indexes);
        DDLUtils.DefaultIndex defaultIndex = new DDLUtils.DefaultIndex(columns);
        defaultIndex.kind = kind;
        list.add(defaultIndex);
        tableDef.indexes = list.toArray(new Index[0]);
        return this;
    }

    /**
     * Add unique constraint on the table.
     *
     * @param unique unique definition
     * @return the current instance
     */
    public ManualTypeBuilder addUnique(Unique unique) {
        List<Unique> list = asList(tableDef.uniques);
        list.add(unique);
        tableDef.uniques = list.toArray(new Unique[0]);
        return this;
    }

    /**
     * Add unique constraint on the table.
     *
     * @param kind    columns reference kind
     * @param columns unique columns
     * @return the current instance
     */
    public ManualTypeBuilder addUnique(Kind kind, String... columns) {
        List<Unique> list = asList(tableDef.uniques);
        DDLUtils.DefaultUnique defaultUnique = new DDLUtils.DefaultUnique();
        defaultUnique.kind = kind;
        defaultUnique.columns = columns;
        list.add(defaultUnique);
        tableDef.uniques = list.toArray(new Unique[0]);
        return this;
    }

    /**
     * Add check constraint on the table.
     *
     * @param check check definition
     * @return the current instance
     */
    public ManualTypeBuilder addCheck(Check check) {
        List<Check> list = asList(tableDef.checks);
        list.add(check);
        tableDef.checks = list.toArray(new Check[0]);
        return this;
    }

    /**
     * Add check constraint on the table.
     *
     * @param check check constraint content
     * @return the current instance
     */
    public ManualTypeBuilder addCheck(String check) {
        List<Check> list = asList(tableDef.checks);
        list.add(new DDLUtils.DefaultCheck(check));
        tableDef.checks = list.toArray(new Check[0]);
        return this;
    }

    /**
     * Add a column on the table.
     *
     * @param name property name
     * @param type java type
     * @return the current instance
     */
    public ManualTypeBuilder addColumn(String name, Class<?> type) {
        return addColumn(column -> column.name(name).type(type));
    }

    /**
     * Add a column on the table.
     *
     * @param c column configurer
     * @return the current instance
     */
    public ManualTypeBuilder addColumn(Consumer<Column> c) {
        ManualImmutablePropImpl prop = new ManualImmutablePropImpl();
        prop.isId = false;

        Column column = new Column(prop);
        c.accept(column);
        column.addAnnotation(column.columnDef);
        _assert(isNotBlank(prop.name), "`prop.name` should not be blank");
        _assert(prop.returnClass != null, "`prop.returnClass` should not be null");

        type.props.put(prop.name, prop);
        prop.declaringType = type;
        return this;
    }

    /**
     * Add relation on the column.
     *
     * @param c foreign-key and referenced table id column configurer
     * @return the current instance
     */
    public ManualTypeBuilder addRelation(Consumer<FK> c) {
        FK fk = new FK();
        // Construct dependent type & type#id

        c.accept(fk);
        _assert(isNotBlank(fk.propName), "`referenceProp.name` should not be blank");
        _assert(fk.referenceType != null || fk.idConsumer != null, "either `fk.referenceType` or `fk.consumer` should be set");
        ManualImmutablePropImpl prop = new ManualImmutablePropImpl();
        if (fk.referenceType != null) {
            prop.isId = false;
            prop.isTargetForeignKeyReal = true;
            prop.name = fk.propName;
            prop.returnClass = Object.class;
            prop.isReference = true;
            prop.targetType = fk.referenceType;
        } else {
            // Construct dependent type & type#id
            ManualImmutableTypeImpl referencedType = new ManualImmutableTypeImpl();
            Map<String, ImmutableProp> referencedTypeProps = new LinkedHashMap<>();
            referencedType.props = referencedTypeProps;
            referencedType.selectableProps = referencedTypeProps;
            referencedType.superTypes = Collections.emptySet();

            ManualImmutablePropImpl referencedId = new ManualImmutablePropImpl();
            referencedId.isId = true;
            referencedId.declaringType = referencedType;
            referencedType.idProp = referencedId;
            Column idColumn = new Column(referencedId);
            fk.idConsumer.accept(idColumn);
            referencedType.tableName = fk.tableName;
            idColumn.addAnnotation(idColumn.columnDef);
            _assert(isNotBlank(referencedId.name), "`referencedId.name` should not be blank");
            _assert(referencedId.returnClass != null, "`referencedId.returnClass` should not be null");
            _assert(isNotBlank(fk.propName), "`referenceProp.name` should not be blank");

            referencedTypeProps.put(idColumn.prop.name, referencedId);

            // Construct reference prop
            prop.isId = false;
            prop.isTargetForeignKeyReal = true;
            prop.name = fk.propName;
            prop.returnClass = Object.class;
            prop.isReference = true;
            prop.targetType = referencedType;
        }
        Column referenceColumn = new Column(prop);
        if (fk.selfConsumer != null) {
            fk.selfConsumer.accept(referenceColumn);
        }

        DDLUtils.DefaultRelation foreignKey = new DDLUtils.DefaultRelation();
        foreignKey.action = fk.action;
        referenceColumn.columnDef.foreignKey = foreignKey;
        referenceColumn.addAnnotation(referenceColumn.columnDef);

        type.props.put(prop.name, prop);
        prop.declaringType = type;

        return this;
    }

    public ManualTypeBuilder apply(Consumer<ManualImmutableTypeImpl> editor) {
        editor.accept(type);
        return this;
    }

    /**
     * Build the manually configured ImmutableType.
     *
     * @return immutable-type
     */
    public ImmutableType build() {
        _assert(isNotBlank(type.tableName), "`type.tableName` should not be blank");
        return type;
    }

    /**
     * Property configuration
     */
    public static class Column {
        private final ManualImmutablePropImpl prop;
        private final DDLUtils.DefaultColumnDef columnDef = new DDLUtils.DefaultColumnDef();

        private Column(ManualImmutablePropImpl prop) {
            this.prop = prop;
            prop.isColumnDefinition = true;
            prop.hasStorage = true;
            prop.dependencies = Collections.emptyList();
        }

        /**
         * Set the property name. This is the logical property name, not the column name,
         * even though they may look similar(the column name derive from property name).
         *
         * @param name property name
         * @return the current instance
         */
        public Column name(String name) {
            prop.name = name;
            prop.id = PropId.byName(name);
            return this;
        }

        /**
         * Set the property type, will be auto-mapped to jdbc-type for DDL generation.
         *
         * @param type java type
         * @return the current instance
         */
        public Column type(Class<?> type) {
            prop.returnClass = type;
            prop.elementClass = type;
            return this;
        }

        /**
         * SQL type definition.
         *
         * @param sqlType type definition. If not blank: overwritten the auto-mapping logic.
         * @return the current instance
         */
        public Column sqlType(String sqlType) {
            columnDef.sqlType = sqlType;
            return this;
        }

        /**
         * Set the jdbc-type.
         *
         * @param jdbcType the jdbc type code, see: {@link java.sql.Types}
         * @return the current instance
         */
        public Column jdbcType(int jdbcType) {
            columnDef.jdbcType = jdbcType;
            return this;
        }

        /**
         * Set the type length.
         *
         * @param length length
         * @return the current instance
         */
        public Column length(int length) {
            columnDef.length = length;
            return this;
        }

        /**
         * Set the type precision.
         *
         * @param precision precision
         * @return the current instance
         */
        public Column precision(int precision) {
            columnDef.precision = precision;
            return this;
        }

        /**
         * Set the type scale.
         *
         * @param scale scale
         * @return the current instance
         */
        public Column scale(int scale) {
            columnDef.scale = scale;
            return this;
        }

        /**
         * Set if the column is nullable.
         *
         * @param nullable nullability
         * @return the current instance
         */
        public Column nullable(boolean nullable) {
            columnDef.nullable = nullable ? ColumnDef.Nullable.TRUE : ColumnDef.Nullable.FALSE;
            return this;
        }

        /**
         * Set the column default value
         *
         * @param defaultValue in raw format, e.g. CURRENT_TIMESTAMP, 'foo', 0, false.
         * @return the current instance
         */
        public Column defaultValue(String defaultValue) {
            columnDef.defaultValue = defaultValue;
            return this;
        }

        /**
         * Set the column comment.
         *
         * @param comment comment on column
         * @return the current instance
         */
        public Column comment(String comment) {
            columnDef.comment = comment;
            return this;
        }

        /**
         * Column definition in plain.
         *
         * @param definition plain definition without column name, e.g. datetime default CURRENT_TIMESTAMP not null
         * @return the current instance
         */
        public Column definition(String definition) {
            columnDef.definition = definition;
            return this;
        }

        /**
         * Add annotation on the property.
         *
         * @param annotation annotation
         * @return the current instance
         */
        public Column addAnnotation(Annotation annotation) {
            List<Annotation> list = asList(prop.annotations);
            list.add(annotation);
            prop.annotations = list.toArray(new Annotation[0]);
            return this;
        }

        public Column apply(Consumer<ManualImmutablePropImpl> editor) {
            editor.accept(prop);
            return this;
        }

    }

    public static class FK {
        private String tableName;
        private String propName;
        private OnDeleteAction action = OnDeleteAction.NONE;
        private ImmutableType referenceType;
        private Consumer<Column> idConsumer;
        private Consumer<Column> selfConsumer;

        private FK() {

        }

        /**
         * Set referenced table name
         *
         * @param tableName referenced table name
         * @return the current instance
         */
        public FK tableName(String tableName) {
            this.tableName = tableName;
            return this;
        }

        /**
         * Set reference property name
         *
         * @param propName property name
         * @return the current instance
         */
        public FK propName(String propName) {
            this.propName = propName;
            return this;
        }

        /**
         * Foreign key on delete action
         *
         * @param action on delete action
         * @return the current instance
         */
        public FK action(OnDeleteAction action) {
            this.action = action;
            return this;
        }

        /**
         * Set referenced type, useful when the type is a pre-constructed.
         *
         * @see #id(Consumer) either using this or using id(Consumer)
         * @param type referenced table type
         * @return the current instance
         */
        public FK type(ImmutableType type) {
            this.referenceType = type;
            return this;
        }

        /**
         * Configure the referenced table id column.
         * Auto build a single column type for generation.
         *
         * @see #type(ImmutableType) either using this or using type(ImmutableType)
         * @param c id column configurer
         * @return the current instance
         */
        public FK id(Consumer<Column> c) {
            this.idConsumer = c;
            return this;
        }

        /**
         * Configure the column reference to the referenced type.
         *
         * @param c foreign-key column configurer
         * @return the current instance
         */
        public FK self(Consumer<Column> c) {
            this.selfConsumer = c;
            return this;
        }

    }

    private static <T> List<T> asList(T[] array) {
        if (array == null) {
            return new ArrayList<>();
        }
        List<T> list = new ArrayList<>(array.length);
        list.addAll(Arrays.asList(array));
        return list;
    }

    private static boolean isNotBlank(String string) {
        return string != null && !string.trim().isEmpty();
    }

    private static void _assert(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

}
