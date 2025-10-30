package io.github.honhimw.jddl;

import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-09-05
 */

public class DDLAutoRunner implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger("jimmer.ddl.sql");

    private final JSqlClientImplementor client;

    private final DDLAuto ddlAuto;

    private final List<ImmutableType> types;

    private DatabaseVersion databaseVersion = DatabaseVersion.LATEST;

    private SchemaValidator.Schemas schemas = SchemaValidator.Schemas.EMPTY;

    public DDLAutoRunner(JSqlClientImplementor client, DDLAuto ddlAuto, List<? extends ImmutableType> types) {
        this.client = client;
        this.ddlAuto = ddlAuto;
        this.types = new ArrayList<>(types);
    }

    public void init() {
        SchemaValidator schemaValidator = new SchemaValidator(client);
        this.databaseVersion = schemaValidator.getDatabaseVersion();
        this.schemas = schemaValidator.load(types);
    }

    public void create() {
        if (!types.isEmpty()) {
            SchemaCreator schemaCreator = new SchemaCreator(client, databaseVersion);
            schemaCreator.init();
            switch (ddlAuto) {
                case CREATE:
                case CREATE_DROP:
                    create(schemaCreator, types);
                    break;
                case UPDATE:
                    update(schemaCreator, types);
                    break;
            }
        }
    }

    public void drop() {
        Collections.reverse(types);
        if (!types.isEmpty()) {
            SchemaCreator schemaCreator = new SchemaCreator(client, databaseVersion);
            schemaCreator.init();
            switch (ddlAuto) {
                case DROP:
                case CREATE_DROP:
                    processDrop(schemaCreator, types);
                    break;
            }
        }
    }

    @Override
    public void close() {
        drop();
    }

    private void create(SchemaCreator schemaCreator, List<ImmutableType> types) {
        List<ImmutableType> nonExistsTypes = types.stream()
            .filter(immutableType -> schemas.get(immutableType.getTableName(client.getMetadataStrategy())) == null)
            .collect(Collectors.toList());
        if (!nonExistsTypes.isEmpty()) {
            processCreate(schemaCreator, nonExistsTypes);
        }
    }

    private void update(SchemaCreator schemaCreator, List<ImmutableType> types) {
        List<ImmutableType> existsTypes = types.stream()
            .filter(immutableType -> schemas.get(immutableType.getTableName(client.getMetadataStrategy())) != null)
            .collect(Collectors.toList());
        if (!existsTypes.isEmpty()) {
            processUpdate(existsTypes);
        }
        List<ImmutableType> nonExistsTypes = types.stream()
            .filter(immutableType -> schemas.get(immutableType.getTableName(client.getMetadataStrategy())) == null)
            .collect(Collectors.toList());
        if (!nonExistsTypes.isEmpty()) {
            processCreate(schemaCreator, nonExistsTypes);
        }
    }

    private void processCreate(SchemaCreator schemaCreator, List<ImmutableType> types) {
        List<ImmutableType> sortedTypes = DDLUtils.sortByDependent(client.getMetadataStrategy(), types);
        List<String> sqlCreateStrings = schemaCreator.getSqlCreateStrings(sortedTypes);
        if (!sqlCreateStrings.isEmpty()) {
            client.getConnectionManager().execute(connection -> {
                try {
                    for (String sqlCreateString : sqlCreateStrings) {
                        if (log.isDebugEnabled()) {
                            log.debug(sqlCreateString);
                        }
                        PreparedStatement preparedStatement = connection.prepareStatement(sqlCreateString);
                        preparedStatement.execute();
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("schema creation error.", e);
                }
                return null;
            });
        }
    }

    private void processUpdate(List<ImmutableType> existsTypes) {
        List<String> sqlAddColumnStrings = new ArrayList<>();
        for (ImmutableType existsType : existsTypes) {
            String tableName = existsType.getTableName(client.getMetadataStrategy());
            Map<String, ImmutableProp> allDefinitionProps = DDLUtils.allDefinitionProps(existsType);
            List<ImmutableProp> nonExistsProps = new ArrayList<>();
            allDefinitionProps.forEach((s, immutableProp) -> {
                String columnName = DDLUtils.getName(immutableProp, client.getMetadataStrategy());
                if (schemas.get(tableName, columnName) != null) {
                    return;
                }
                nonExistsProps.add(immutableProp);
            });
            if (!nonExistsProps.isEmpty()) {
                StandardAddColumnExporter standardAddColumnExporter = new StandardAddColumnExporter(client, databaseVersion);
                for (ImmutableProp nonExistsProp : nonExistsProps) {
                    List<String> sqlCreateStrings = standardAddColumnExporter.getSqlCreateStrings(nonExistsProp);
                    sqlAddColumnStrings.addAll(sqlCreateStrings);
                }
            }
        }
        if (!sqlAddColumnStrings.isEmpty()) {
            client.getConnectionManager().execute(connection -> {
                try {
                    for (String sqlAddColumnString : sqlAddColumnStrings) {
                        if (log.isDebugEnabled()) {
                            log.debug(sqlAddColumnString);
                        }
                        PreparedStatement preparedStatement = connection.prepareStatement(sqlAddColumnString);
                        preparedStatement.execute();
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("schema update error.", e);
                }
                return null;
            });
        }
    }

    private void processDrop(SchemaCreator schemaCreator, List<ImmutableType> types) {
        List<ImmutableType> sortedTypes = DDLUtils.sortByDependent(client.getMetadataStrategy(), types);
        Collections.reverse(sortedTypes);
        List<String> sqlDropStrings = schemaCreator.getSqlDropStrings(sortedTypes);
        if (!sqlDropStrings.isEmpty()) {
            client.getConnectionManager().execute(connection -> {
                try {
                    for (String sqlDropString : sqlDropStrings) {
                        if (log.isDebugEnabled()) {
                            log.debug(sqlDropString);
                        }
                        PreparedStatement preparedStatement = connection.prepareStatement(sqlDropString);
                        preparedStatement.execute();
                    }
                } catch (Exception e) {
                    throw new IllegalStateException("schema deletion error.", e);
                }
                return null;
            });
        }
    }

}
