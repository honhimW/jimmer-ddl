package io.github.honhimw.jddl.manual;

import io.github.honhimw.jddl.DDLUtils;
import io.github.honhimw.jddl.anno.*;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;

/**
 * @author honhimW
 * @since 2025-10-23
 */

public class ManualTypeBuilder {

    private final ManualImmutableTypeImpl type = new ManualImmutableTypeImpl();

    private final DDLUtils.DefaultTableDef tableDef = new DDLUtils.DefaultTableDef();

    public static ManualTypeBuilder string(String id) {
        return of(column -> column.name(id).returnClass(String.class));
    }

    public static ManualTypeBuilder u32(String id) {
        return of(column -> column.name(id).returnClass(Integer.class).addAnnotation(new DDLUtils.DefaultGeneratedValue()));
    }

    public static ManualTypeBuilder u64(String id) {
        return of(column -> column.name(id).returnClass(Long.class).addAnnotation(new DDLUtils.DefaultGeneratedValue()));
    }

    public static ManualTypeBuilder of(Consumer<Column> id) {
        ManualTypeBuilder builder = new ManualTypeBuilder();
        ManualImmutablePropImpl prop = new ManualImmutablePropImpl();
        prop.isId = true;

        Column column = new Column(prop);
        id.accept(column);
        column.addAnnotation(column.columnDef);
        _assert(!isBlank(prop.name), "`prop.name` should not be blank");
        _assert(prop.returnClass != null, "`prop.returnClass` should not be null");

        builder.type.idProp = prop;
        builder.type.props.put(prop.name, prop);
        prop.declaringType = builder.type;
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

    public ManualTypeBuilder name(String name) {
        type.tableName = name;
        return this;
    }

    public ManualTypeBuilder comment(String comment) {
        tableDef.comment = comment;
        return this;
    }

    public ManualTypeBuilder tableType(String tableType) {
        tableDef.tableType = tableType;
        return this;
    }

    public ManualTypeBuilder addIndex(Index index) {
        List<Index> list = asList(tableDef.indexes);
        list.add(index);
        tableDef.indexes = list.toArray(new Index[0]);
        return this;
    }

    public ManualTypeBuilder addIndex(Kind kind, String... columns) {
        List<Index> list = asList(tableDef.indexes);
        DDLUtils.DefaultIndex defaultIndex = new DDLUtils.DefaultIndex(columns);
        defaultIndex.kind = kind;
        list.add(defaultIndex);
        tableDef.indexes = list.toArray(new Index[0]);
        return this;
    }

    public ManualTypeBuilder addUnique(Unique unique) {
        List<Unique> list = asList(tableDef.uniques);
        list.add(unique);
        tableDef.uniques = list.toArray(new Unique[0]);
        return this;
    }

    public ManualTypeBuilder addUnique(Kind kind, String... columns) {
        List<Unique> list = asList(tableDef.uniques);
        DDLUtils.DefaultUnique defaultUnique = new DDLUtils.DefaultUnique();
        defaultUnique.kind = kind;
        defaultUnique.columns = columns;
        list.add(defaultUnique);
        tableDef.uniques = list.toArray(new Unique[0]);
        return this;
    }

    public ManualTypeBuilder addCheck(Check check) {
        List<Check> list = asList(tableDef.checks);
        list.add(check);
        tableDef.checks = list.toArray(new Check[0]);
        return this;
    }

    public ManualTypeBuilder addCheck(String check) {
        List<Check> list = asList(tableDef.checks);
        list.add(new DDLUtils.DefaultCheck(check));
        tableDef.checks = list.toArray(new Check[0]);
        return this;
    }

    public ManualTypeBuilder addColumn(String name, Class<?> returnClass) {
        return addColumn(column -> column.name(name).returnClass(returnClass));
    }

    public ManualTypeBuilder addColumn(Consumer<Column> c) {
        ManualImmutablePropImpl prop = new ManualImmutablePropImpl();
        prop.isId = false;

        Column column = new Column(prop);
        c.accept(column);
        column.addAnnotation(column.columnDef);
        _assert(!isBlank(prop.name), "`prop.name` should not be blank");
        _assert(prop.returnClass != null, "`prop.returnClass` should not be null");

        type.props.put(prop.name, prop);
        prop.declaringType = type;
        return this;
    }

    public ImmutableType build() {
        _assert(!isBlank(type.tableName), "`type.tableName` should not be blank");
        return type;
    }

    public static class Column {
        private final ManualImmutablePropImpl prop;
        private final DDLUtils.DefaultColumnDef columnDef = new DDLUtils.DefaultColumnDef();

        private Column(ManualImmutablePropImpl prop) {
            this.prop = prop;
            prop.isColumnDefinition = true;
            prop.hasStorage = true;
        }

        public Column name(String name) {
            prop.name = name;
            return this;
        }

        public Column returnClass(Class<?> returnClass) {
            prop.returnClass = returnClass;
            return this;
        }

        public Column sqlType(String sqlType) {
            columnDef.sqlType = sqlType;
            return this;
        }

        public Column jdbcType(int jdbcType) {
            columnDef.jdbcType = jdbcType;
            return this;
        }

        public Column length(int length) {
            columnDef.length = length;
            return this;
        }

        public Column precision(int precision) {
            columnDef.precision = precision;
            return this;
        }

        public Column scale(int scale) {
            columnDef.scale = scale;
            return this;
        }

        public Column nullable(boolean nullable) {
            columnDef.nullable = nullable ? ColumnDef.Nullable.TRUE : ColumnDef.Nullable.FALSE;
            return this;
        }

        public Column defaultValue(String defaultValue) {
            columnDef.defaultValue = defaultValue;
            return this;
        }

        public Column comment(String comment) {
            columnDef.comment = comment;
            return this;
        }

        public Column definition(String definition) {
            columnDef.definition = definition;
            return this;
        }

        public Column foreignKey(String definition, OnDeleteAction action) {
            DDLUtils.DefaultRelation foreignKey = new DDLUtils.DefaultRelation();
            foreignKey.definition = definition;
            foreignKey.action = action;
            columnDef.foreignKey = foreignKey;
            return this;
        }

        public Column addAnnotation(Annotation annotation) {
            List<Annotation> list = asList(prop.annotations);
            list.add(annotation);
            prop.annotations = list.toArray(new Annotation[0]);
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

    private static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    private static void _assert(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

}
