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

        @Override
        public Nullable nullable() {
            return Nullable.NONE;
        }

        @Override
        public String sqlType() {
            return "";
        }

        @Override
        public int jdbcType() {
            return OTHER;
        }

        @Override
        public long length() {
            return -1;
        }

        @Override
        public int precision() {
            return -1;
        }

        @Override
        public int scale() {
            return -1;
        }

        @Override
        public String defaultValue() {
            return "";
        }

        @Override
        public String comment() {
            return "";
        }

        @Override
        public String definition() {
            return "";
        }

        @Override
        public Relation foreignKey() {
            return new DefaultRelation();
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return ColumnDef.class;
        }
    }

    public static class DefaultRelation implements Relation {
        @Override
        public String name() {
            return "";
        }

        @Override
        public String definition() {
            return "";
        }

        @Override
        public OnDeleteAction action() {
            return OnDeleteAction.NONE;
        }

        @Override
        public Class<? extends ConstraintNamingStrategy> naming() {
            return ConstraintNamingStrategy.class;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Relation.class;
        }
    }

    public static class DefaultGeneratedValue implements GeneratedValue {
        @Override
        public GenerationType strategy() {
            return GenerationType.AUTO;
        }

        @Override
        public Class<? extends UserIdGenerator<?>> generatorType() {
            return UserIdGenerator.None.class;
        }

        @Override
        public String generatorRef() {
            return "";
        }

        @Override
        public String sequenceName() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return GeneratedValue.class;
        }
    }

    public static class DefaultTableDef implements TableDef {
        @Override
        public Unique[] uniques() {
            return new Unique[0];
        }

        @Override
        public Index[] indexes() {
            return new Index[0];
        }

        @Override
        public String comment() {
            return "";
        }

        @Override
        public Check[] checks() {
            return new Check[0];
        }

        @Override
        public String tableType() {
            return "";
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return TableDef.class;
        }
    }

    public static abstract class DefaultUnique implements Unique {
        @Override
        public String name() {
            return "";
        }

        @Override
        public Kind kind() {
            return Kind.PATH;
        }

        @Override
        public Class<? extends ConstraintNamingStrategy> naming() {
            return ConstraintNamingStrategy.class;
        }

        @Override
        public Class<? extends Annotation> annotationType() {
            return Unique.class;
        }
    }

}
