package io.github.honhimw.jddl;

import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-09-05
 */

public class DDLAutoRunner {

    private final JSqlClientImplementor client;

    private final DDLAuto ddlAuto;

    private final List<ImmutableType> types;

    private DatabaseVersion databaseVersion = DatabaseVersion.LATEST;

    private SchemaValidator.Schemas schemas = SchemaValidator.Schemas.EMPTY;

    public DDLAutoRunner(JSqlClientImplementor client, DDLAuto ddlAuto, List<ImmutableType> types) {
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
                    create(schemaCreator, types);
                    break;
                case CREATE_DROP: {
                    // prefer drop manually
                    // processDrop(schemaCreator, types);
                    processCreate(schemaCreator, types);
                    break;
                }
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

    private void create(SchemaCreator schemaCreator, List<ImmutableType> types) {
        List<ImmutableType> nonExistsTypes = types.stream()
            .filter(immutableType -> !schemas.getTableMap().containsKey(immutableType.getTableName(client.getMetadataStrategy())))
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

    private void processDrop(SchemaCreator schemaCreator, List<ImmutableType> types) {
        List<ImmutableType> sortedTypes = DDLUtils.sortByDependent(client.getMetadataStrategy(), types);
        Collections.reverse(sortedTypes);
        List<String> sqlDropStrings = schemaCreator.getSqlDropStrings(sortedTypes);
        if (!sqlDropStrings.isEmpty()) {
            client.getConnectionManager().execute(connection -> {
                try {
                    for (String sqlDropString : sqlDropStrings) {
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
