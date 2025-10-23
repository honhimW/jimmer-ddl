package io.github.honhimw.jddl;

import io.github.honhimw.jddl.anno.*;
import io.github.honhimw.jddl.dialect.DDLDialect;
import org.babyfish.jimmer.meta.EmbeddedLevel;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.EnumType;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.GenerationType;
import org.babyfish.jimmer.sql.meta.MetadataStrategy;
import org.babyfish.jimmer.sql.meta.SingleColumn;
import org.babyfish.jimmer.sql.meta.Storage;
import org.babyfish.jimmer.sql.meta.UserIdGenerator;
import org.babyfish.jimmer.sql.meta.impl.Storages;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.babyfish.jimmer.sql.runtime.ScalarProvider;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.sql.DatabaseMetaData;
import java.util.*;

import static java.sql.Types.*;

/**
 * @author honhimW
 */

public class DDLUtils {

    public static String replace(String type, @Nullable Long length, @Nullable Integer precision, @Nullable Integer scale) {
        if (scale != null) {
            type = type.replace("$s", scale.toString());
        }
        if (length != null) {
            type = type.replace("$l", length.toString());
        }
        if (precision != null) {
            type = type.replace("$p", precision.toString());
        }
        return type;
    }

    public static boolean isTemporal(int jdbcType) {
        switch (jdbcType) {
            case TIME:
            case TIME_WITH_TIMEZONE:
            case DATE:
            case TIMESTAMP:
            case TIMESTAMP_WITH_TIMEZONE:
                return true;
            default:
                return false;
        }
    }

    @Nullable
    public static Integer resolveDefaultPrecision(int jdbcType, DDLDialect dialect) {
        Integer precision = null;
        if (isTemporal(jdbcType)) {
            precision = dialect.getDefaultTimestampPrecision(jdbcType);
        }
        if (jdbcType == DECIMAL) {
            precision = dialect.getDefaultDecimalPrecision(jdbcType);
        }
        if (jdbcType == FLOAT) {
            precision = dialect.getDefaultDecimalPrecision(jdbcType);
        }
        if (jdbcType == DOUBLE) {
            precision = dialect.getDefaultDecimalPrecision(jdbcType);
        }
        return precision;
    }

    public static String getName(ImmutableProp prop, MetadataStrategy metadataStrategy) {
        Storage storage = Storages.of(prop, metadataStrategy);
        if (storage instanceof SingleColumn) {
            SingleColumn singleColumn = (SingleColumn) storage;
            return singleColumn.getName();
        }
        return prop.getName();
    }

    public static Map<String, ImmutableProp> allDefinitionProps(ImmutableType immutableType) {
        Map<String, ImmutableProp> props = new LinkedHashMap<>();
        Map<String, ImmutableProp> selectableScalarProps = immutableType.getSelectableProps();
        selectableScalarProps.forEach((k, v) -> {
            if (v.isEmbedded(EmbeddedLevel.BOTH)) {
                ImmutableType targetType = v.getTargetType();
                Map<String, ImmutableProp> next = allDefinitionProps(targetType);
                next.forEach((nextKey, nextValue) -> props.put(k + '.' + nextKey, nextValue));
            } else {
                props.put(k, v);
            }
        });
        return props;
    }

    public static List<ForeignKey> getForeignKeys(MetadataStrategy metadataStrategy, ImmutableType immutableType) {
        final List<ForeignKey> foreignKeys = new ArrayList<>();
        Map<String, ImmutableProp> allDefinitionProps = DDLUtils.allDefinitionProps(immutableType);
        for (Map.Entry<String, ImmutableProp> entry : allDefinitionProps.entrySet()) {
            ImmutableProp definitionProps = entry.getValue();
            if (definitionProps.isTargetForeignKeyReal(metadataStrategy)) {
                ColumnDef columnDef = definitionProps.getAnnotation(ColumnDef.class);
                Relation relation;
                if (columnDef != null) {
                    relation = columnDef.foreignKey();
                } else {
                    relation = new DefaultRelation();
                }
                ForeignKey _foreignKey = new ForeignKey(relation, definitionProps, immutableType, definitionProps.getTargetType());
                foreignKeys.add(_foreignKey);
            }
        }
        return foreignKeys;
    }

    public static EnumType.@Nullable Strategy resolveEnum(JSqlClientImplementor client, ImmutableProp prop) {
        Class<?> returnClass = prop.getReturnClass();
        if (returnClass.isEnum()) {
            ScalarProvider<Enum<?>, ?> scalarProvider = client.getScalarProvider(prop);
            if (String.class.isAssignableFrom(scalarProvider.getSqlType())) {
                return EnumType.Strategy.NAME;
            } else {
                return EnumType.Strategy.ORDINAL;
            }
        }
        return null;
    }

    public static List<ImmutableType> sortByDependent(
        MetadataStrategy metadataStrategy,
        Collection<ImmutableType> immutableTypes
    ) {
        Map<ImmutableType, List<ImmutableType>> dependencyGraph = new HashMap<>();
        Set<ImmutableType> allTypes = new HashSet<>(immutableTypes);

        for (ImmutableType type : immutableTypes) {
            List<ForeignKey> foreignKeys = getForeignKeys(metadataStrategy, type);
            List<ImmutableType> dependencies = new ArrayList<>();
            for (ForeignKey fk : foreignKeys) {
                ImmutableType dependency = fk.referencedTable;
                if (!type.equals(dependency) && allTypes.contains(dependency)) {
                    dependencies.add(dependency);
                }
            }
            dependencyGraph.put(type, dependencies);
        }

        List<ImmutableType> sorted = new ArrayList<>();
        Set<ImmutableType> visited = new HashSet<>();
        Set<ImmutableType> visiting = new HashSet<>();

        for (ImmutableType type : immutableTypes) {
            if (!visited.contains(type)) {
                dfs(type, dependencyGraph, visited, visiting, sorted);
            }
        }

        return sorted;
    }

    public static DatabaseVersion getDatabaseVersion(JSqlClientImplementor client) {
        return client.getConnectionManager().execute(connection -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                int databaseMajorVersion = metaData.getDatabaseMajorVersion();
                int databaseMinorVersion = metaData.getDatabaseMinorVersion();
                String databaseProductVersion = metaData.getDatabaseProductVersion();
                return new DatabaseVersion(databaseMajorVersion, databaseMinorVersion, databaseProductVersion);
            } catch (Exception e) {
                // cannot get database version, using latest as default
                return DatabaseVersion.LATEST;
            }
        });
    }

    private static void dfs(
        ImmutableType type,
        Map<ImmutableType, List<ImmutableType>> graph,
        Set<ImmutableType> visited,
        Set<ImmutableType> visiting,
        List<ImmutableType> sorted
    ) {
        if (visiting.contains(type)) {
            throw new IllegalStateException("Circular dependency detected involving: " + type);
        }
        if (visited.contains(type)) {
            return;
        }

        visiting.add(type);
        for (ImmutableType dep : graph.getOrDefault(type, Collections.emptyList())) {
            dfs(dep, graph, visited, visiting, sorted);
        }
        visiting.remove(type);
        visited.add(type);
        sorted.add(type);
    }

    public static class DefaultColumnDef implements ColumnDef {
        public Nullable nullable = Nullable.NONE;
        public String sqlType = "";
        public int jdbcType = OTHER;
        public long length = -1;
        public int precision = -1;
        public int scale = -1;
        public String defaultValue = "";
        public String comment = "";
        public String definition = "";
        public Relation foreignKey = new DefaultRelation();

        @Override
        public Nullable nullable() {
            return nullable;
        }

        @Override
        public String sqlType() {
            return sqlType;
        }

        @Override
        public int jdbcType() {
            return jdbcType;
        }

        @Override
        public long length() {
            return length;
        }

        @Override
        public int precision() {
            return precision;
        }

        @Override
        public int scale() {
            return scale;
        }

        @Override
        public String defaultValue() {
            return defaultValue;
        }

        @Override
        public String comment() {
            return comment;
        }

        @Override
        public String definition() {
            return definition;
        }

        @Override
        public Relation foreignKey() {
            return foreignKey;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ColumnDef.class;
        }
    }

    public static class DefaultRelation implements Relation {
        public String name = "";
        public String definition = "";
        public OnDeleteAction action = OnDeleteAction.NONE;
        public Class<? extends ConstraintNamingStrategy> naming = ConstraintNamingStrategy.class;

        @Override
        public String name() {
            return name;
        }

        @Override
        public String definition() {
            return definition;
        }

        @Override
        public OnDeleteAction action() {
            return action;
        }

        @Override
        public Class<? extends ConstraintNamingStrategy> naming() {
            return naming;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Relation.class;
        }
    }

    public static class DefaultGeneratedValue implements GeneratedValue {
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

    public static class DefaultTableDef implements TableDef {
        public Unique[] uniques = new Unique[0];
        public Index[] indexes = new Index[0];
        public String comment = "";
        public Check[] checks = new Check[0];
        public String tableType = "";

        @Override
        public Unique[] uniques() {
            return uniques;
        }

        @Override
        public Index[] indexes() {
            return indexes;
        }

        @Override
        public String comment() {
            return comment;
        }

        @Override
        public Check[] checks() {
            return checks;
        }

        @Override
        public String tableType() {
            return tableType;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return TableDef.class;
        }
    }

    public static class DefaultUnique implements Unique {
        public String name = "";
        public String[] columns;
        public Kind kind = Kind.PATH;
        public Class<? extends ConstraintNamingStrategy> naming = ConstraintNamingStrategy.class;

        public DefaultUnique(String... columns) {
            this.columns = columns;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String[] columns() {
            return columns;
        }

        @Override
        public Kind kind() {
            return kind;
        }

        @Override
        public Class<? extends ConstraintNamingStrategy> naming() {
            return naming;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Unique.class;
        }
    }

    public static class DefaultIndex implements Index {
        public String name = "";
        public String[] columns;
        public boolean unique = false;
        public Kind kind = Kind.PATH;
        public Class<? extends ConstraintNamingStrategy> naming = ConstraintNamingStrategy.class;

        public DefaultIndex(String... columns) {
            this.columns = columns;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String[] columns() {
            return columns;
        }

        @Override
        public boolean unique() {
            return unique;
        }

        @Override
        public Kind kind() {
            return kind;
        }

        @Override
        public Class<? extends ConstraintNamingStrategy> naming() {
            return naming;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Index.class;
        }
    }

    public static class DefaultCheck implements Check {
        public String name = "";
        public String value;

        public DefaultCheck(String value) {
            this.value = value;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String value() {
            return value;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Check.class;
        }
    }

}
