package io.github.honhimw.jddl;

import io.github.honhimw.jddl.anno.*;
import io.github.honhimw.jman.ManualImmutablePropImpl;
import io.github.honhimw.jman.ManualImmutableTypeImpl;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.meta.TargetLevel;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.ManyToMany;
import org.babyfish.jimmer.sql.meta.MiddleTable;
import org.babyfish.jimmer.sql.meta.Storage;
import org.babyfish.jimmer.sql.meta.impl.Storages;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;

import java.lang.annotation.Annotation;
import java.util.*;

/**
 * @author honhimW
 */

@NullUnmarked
public class SchemaCreator implements Exporter<@NonNull Collection<ImmutableType>> {

    private final JSqlClientImplementor client;

    private DatabaseVersion version;

    private StandardTableExporter standardTableExporter;

    private StandardForeignKeyExporter standardForeignKeyExporter;

    private StandardSequenceExporter standardSequenceExporter;

    public SchemaCreator(@NonNull JSqlClientImplementor client) {
        this(client, null);
    }

    public SchemaCreator(@NonNull JSqlClientImplementor client, DatabaseVersion version) {
        this.client = client;
        this.version = version;
    }

    /**
     * do get database meta-data
     */
    public void init() {
        if (version == null) {
            version = DDLUtils.getDatabaseVersion(client);
        }
        standardTableExporter = new StandardTableExporter(client, version);
        standardForeignKeyExporter = new StandardForeignKeyExporter(client, version);
        standardSequenceExporter = new StandardSequenceExporter(client, version);
    }

    @Override
    public List<String> getSqlCreateStrings(@NonNull Collection<@NonNull ImmutableType> exportable) {
        final List<String> allSqlCreateStrings = new ArrayList<>();
        applyCreateSequences(exportable, allSqlCreateStrings);
        // Middle Table
        exportable = applyConstructMiddleTables(exportable, true);
        applyCreateTables(exportable, allSqlCreateStrings);
        applyCreateForeignKeys(exportable, allSqlCreateStrings);
        return allSqlCreateStrings;
    }

    @Override
    public List<@NonNull String> getSqlDropStrings(@NonNull Collection<@NonNull ImmutableType> exportable) {
        final List<String> allSqlCreateStrings = new ArrayList<>();
        // Middle Table
        exportable = applyConstructMiddleTables(exportable, false);
        applyDropForeignKeys(exportable, allSqlCreateStrings);
        applyDropTables(exportable, allSqlCreateStrings);
        applyDropSequences(exportable, allSqlCreateStrings);
        return allSqlCreateStrings;
    }

    private void applyCreateSequences(Collection<ImmutableType> exportable, List<String> allSqlCreateStrings) {
        for (ImmutableType immutableType : exportable) {
            Map<String, ImmutableProp> allDefinitionProps = DDLUtils.allDefinitionProps(immutableType);
            for (Map.Entry<String, ImmutableProp> entry : allDefinitionProps.entrySet()) {
                ImmutableProp definitionProp = entry.getValue();
                if (definitionProp.getAnnotation(GeneratedValue.class) != null) {
                    List<String> sqlCreateStrings = standardSequenceExporter.getSqlCreateStrings(definitionProp);
                    allSqlCreateStrings.addAll(sqlCreateStrings);
                }
            }
        }
    }

    private void applyCreateTables(Collection<ImmutableType> exportable, List<String> allSqlCreateStrings) {
        for (ImmutableType immutableType : exportable) {
            List<String> sqlCreateStrings = standardTableExporter.getSqlCreateStrings(immutableType);
            allSqlCreateStrings.addAll(sqlCreateStrings);
        }
    }

    private Collection<ImmutableType> applyConstructMiddleTables(Collection<ImmutableType> exportable, boolean isCreate) {
        Set<String> tableNames = new HashSet<>();
        for (ImmutableType immutableType : exportable) {
            tableNames.add(immutableType.getTableName(client.getMetadataStrategy()));
        }

        List<ImmutableType> withMiddleTables = new ArrayList<>(exportable);
        for (ImmutableType immutableType : exportable) {
            for (ImmutableProp prop : immutableType.getProps().values()) {
                if (prop.isReferenceList(TargetLevel.PERSISTENT)) {
                    ManyToMany manyToMany = prop.getAnnotation(ManyToMany.class);
                    if (manyToMany != null) {
                        Storage storage = Storages.of(prop, client.getMetadataStrategy());
                        if (storage instanceof MiddleTable) {
                            MiddleTable middleTable = (MiddleTable) storage;
                            String middleTableName = middleTable.getTableName();
                            if (tableNames.contains(middleTableName)) {
                                continue;
                            }

                            ImmutableProp joinProp = immutableType.getIdProp();
                            ImmutableProp inverseJoinProp = prop.getTargetType().getIdProp();
                            String joinColumnName = client.getMetadataStrategy().getNamingStrategy().middleTableBackRefColumnName(prop);
                            String inverseJoinColumnName = client.getMetadataStrategy().getNamingStrategy().middleTableTargetRefColumnName(prop);
                            io.github.honhimw.jddl.anno.MiddleTable annotation = prop.getAnnotation(io.github.honhimw.jddl.anno.MiddleTable.class);

                            ManualImmutableTypeImpl fakeImmutableType = new ManualImmutableTypeImpl();
                            fakeImmutableType.tableName = middleTableName;
                            fakeImmutableType.javaClass = Object.class;
                            fakeImmutableType.props = new LinkedHashMap<>();

                            boolean useAutoId;
                            boolean useRealForeignKey;
                            Relation joinColumnRelation;
                            Relation inverseJoinColumnRelation;
                            TableDef tableDef;

                            if (annotation != null) {
                                tableDef = annotation.tableDef();

                                useAutoId = annotation.useAutoId();
                                if (useAutoId) {
                                    Index[] indexes = tableDef.indexes();
                                    Unique[] uniques = tableDef.uniques();
                                    Check[] checks = tableDef.checks();
                                    String comment = tableDef.comment();
                                    String tableType = tableDef.tableType();
                                    Unique[] newUniques = new Unique[uniques.length + 1];
                                    DDLUtils.DefaultUnique defaultUnique = new DDLUtils.DefaultUnique();
                                    defaultUnique.columns = new String[]{joinColumnName, inverseJoinColumnName};
                                    defaultUnique.kind = Kind.NAME;
                                    System.arraycopy(uniques, 0, newUniques, 1, uniques.length);
                                    DDLUtils.DefaultTableDef defaultTableDef = new DDLUtils.DefaultTableDef();
                                    defaultTableDef.uniques = newUniques;
                                    defaultTableDef.indexes = indexes;
                                    defaultTableDef.comment = comment;
                                    defaultTableDef.checks = checks;
                                    defaultTableDef.tableType = tableType;
                                    tableDef = defaultTableDef;
                                }
                                useRealForeignKey = annotation.useRealForeignKey();
                                joinColumnRelation = annotation.joinColumnForeignKey();
                                inverseJoinColumnRelation = annotation.inverseJoinColumnForeignKey();
                            } else {
                                tableDef = new DDLUtils.DefaultTableDef();
                                useAutoId = false;
                                useRealForeignKey = true;
                                joinColumnRelation = new DDLUtils.DefaultRelation();
                                inverseJoinColumnRelation = new DDLUtils.DefaultRelation();
                            }
                            fakeImmutableType.annotations = new Annotation[]{tableDef};

                            ManualImmutablePropImpl id = new ManualImmutablePropImpl();
                            id.name = "id";
                            Map<String, ImmutableProp> props;
                            if (useAutoId) {
                                id.returnClass = Integer.TYPE;
                                id.isId = true;
                                id.isColumnDefinition = true;
                                id.annotations = new Annotation[]{new DDLUtils.DefaultGeneratedValue()};
                                fakeImmutableType.props.put(id.name, id);
                                fakeImmutableType.idProp = id;
                                props = fakeImmutableType.props;
                            } else {
                                id.returnClass = Object.class;
                                id.isId = true;
                                id.isColumnDefinition = false;
                                id.annotations = new Annotation[]{new DDLUtils.DefaultGeneratedValue()};
                                id.isEmbedded = true;
                                ManualImmutableTypeImpl embeddedIdType = new ManualImmutableTypeImpl();
                                id.targetType = embeddedIdType;
                                embeddedIdType.props = new LinkedHashMap<>();
                                embeddedIdType.selectableProps = embeddedIdType.props;
                                props = embeddedIdType.props;
                                fakeImmutableType.props.put(id.name, id);
                                fakeImmutableType.idProp = id;
                            }

                            ManualImmutablePropImpl fakeJoinProp = new ManualImmutablePropImpl();
                            fakeJoinProp.name = joinColumnName;
                            fakeJoinProp.returnClass = joinProp.getReturnClass();
                            fakeJoinProp.isColumnDefinition = true;
                            if (useRealForeignKey) {
                                DDLUtils.DefaultColumnDef defaultColumnDef = new DDLUtils.DefaultColumnDef();
                                defaultColumnDef.foreignKey = joinColumnRelation;
                                fakeJoinProp.annotations = new Annotation[]{defaultColumnDef};
                                fakeJoinProp.isTargetForeignKeyReal = true;
                                fakeJoinProp.targetType = immutableType;
                            }
                            props.put(fakeJoinProp.name, fakeJoinProp);

                            ManualImmutablePropImpl fakeInverseJoin = new ManualImmutablePropImpl();
                            fakeInverseJoin.name = inverseJoinColumnName;
                            fakeInverseJoin.returnClass = inverseJoinProp.getReturnClass();
                            fakeInverseJoin.isColumnDefinition = true;
                            if (useRealForeignKey) {
                                DDLUtils.DefaultColumnDef defaultColumnDef = new DDLUtils.DefaultColumnDef();
                                defaultColumnDef.foreignKey = inverseJoinColumnRelation;
                                fakeInverseJoin.annotations = new Annotation[]{defaultColumnDef};
                                fakeInverseJoin.isTargetForeignKeyReal = true;
                                fakeInverseJoin.targetType = prop.getTargetType();
                            }
                            props.put(fakeInverseJoin.name, fakeInverseJoin);

                            fakeImmutableType.selectableProps = fakeImmutableType.props;
                            if (isCreate) {
                                withMiddleTables.add(fakeImmutableType);
                            } else {
                                withMiddleTables.add(0, fakeImmutableType);
                            }
                        }
                    }
                }
            }
        }
        return withMiddleTables;
    }

    private void applyCreateForeignKeys(Collection<ImmutableType> exportable, List<String> allSqlCreateStrings) {
        if (!client.getDialect().isForeignKeySupported()) {
            return;
        }
        for (ImmutableType immutableType : exportable) {
            for (ForeignKey foreignKey : DDLUtils.getForeignKeys(client.getMetadataStrategy(), immutableType)) {
                List<String> sqlCreateStrings = standardForeignKeyExporter.getSqlCreateStrings(foreignKey);
                allSqlCreateStrings.addAll(sqlCreateStrings);
            }
        }
    }

    private void applyDropForeignKeys(Collection<ImmutableType> exportable, List<String> allSqlCreateStrings) {
        if (!client.getDialect().isForeignKeySupported()) {
            return;
        }
        for (ImmutableType immutableType : exportable) {
            for (ForeignKey foreignKey : DDLUtils.getForeignKeys(client.getMetadataStrategy(), immutableType)) {
                List<String> sqlCreateStrings = standardForeignKeyExporter.getSqlDropStrings(foreignKey);
                allSqlCreateStrings.addAll(sqlCreateStrings);
            }
        }
    }

    private void applyDropTables(Collection<ImmutableType> exportable, List<String> allSqlCreateStrings) {
        for (ImmutableType immutableType : exportable) {
            List<String> sqlDropStrings = standardTableExporter.getSqlDropStrings(immutableType);
            allSqlCreateStrings.addAll(sqlDropStrings);
        }
    }

    private void applyDropSequences(Collection<ImmutableType> exportable, List<String> allSqlCreateStrings) {
        for (ImmutableType immutableType : exportable) {
            Map<String, ImmutableProp> allDefinitionProps = DDLUtils.allDefinitionProps(immutableType);
            for (Map.Entry<String, ImmutableProp> entry : allDefinitionProps.entrySet()) {
                ImmutableProp definitionProp = entry.getValue();
                if (definitionProp.getAnnotation(GeneratedValue.class) != null) {
                    List<String> sqlCreateStrings = standardSequenceExporter.getSqlDropStrings(definitionProp);
                    allSqlCreateStrings.addAll(sqlCreateStrings);
                }
            }
        }
    }

}
