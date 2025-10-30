package io.github.honhimw.jddl;

import io.github.honhimw.jddl.column.ColumnModifier;
import io.github.honhimw.jddl.column.ColumnResolver;
import io.github.honhimw.jddl.dialect.DDLDialect;
import io.github.honhimw.jddl.model.Modify0Table;
import io.github.honhimw.jddl.model.Modify1Table;
import io.github.honhimw.jddl.model.Tables;
import io.github.honhimw.jddl.model.update.NewSchemaTable;
import io.github.honhimw.test.DataSourceConnectionManager;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.ast.impl.table.TableTypeProvider;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.babyfish.jimmer.sql.runtime.SqlFormatter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public abstract class AbstractRealDBTests extends AbstractDDLTest {

    @Test
    public void run() throws Exception {
        DataSource dataSource = dataSource();
        if (dataSource == null) {
            return;
        }
        DataSourceConnectionManager connectionManager = new DataSourceConnectionManager(dataSource);
        JSqlClientImplementor sqlClient = getSqlClient(builder -> builder
            .setDialect(dialect())
            .setConnectionManager(connectionManager)
            .setSqlFormatter(SqlFormatter.PRETTY)
        );
        SchemaCreator schemaCreator = new SchemaCreator(sqlClient, DatabaseVersion.LATEST);
        schemaCreator.init();
        List<Table<?>> tables = new ArrayList<>();
        tables.add(Tables.AUTHOR_TABLE);
        tables.add(Tables.BOOK_STORE_TABLE);
        tables.add(Tables.BOOK_TABLE);
        tables.add(Tables.COUNTRY_TABLE);
        tables.add(Tables.ORGANIZATION_TABLE);
        tables.add(Tables.PLAYER_TABLE);
        tables.add(Tables.NAME_TABLE);

        List<ImmutableType> types = tables.stream().map(TableTypeProvider::getImmutableType).collect(Collectors.toList());
        DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, types);

        Assertions.assertDoesNotThrow(ddlAutoRunner::create);
        for (ImmutableType type : types) {
            Assertions.assertDoesNotThrow(() -> {
                connectionManager.execute(null, connection -> {
                    try {
                        ResultSet resultSet = connection.getMetaData().getColumns(null, null, type.getTableName(sqlClient.getMetadataStrategy()), null);
                        List<Map<String, Object>> resultMap = toMap(resultSet);
                        Map<String, Map<String, Object>> collect = resultMap.stream().collect(Collectors.toMap(map -> (String) map.get("COLUMN_NAME"), map -> map));
                        assertColumnTypes(type, collect);
                        return null;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });
            });
        }
        Assertions.assertDoesNotThrow(ddlAutoRunner::drop);
        if (testContainer != null) {
            testContainer.stop();
        }
    }

    @Test
    public void columnModifier() {
        DataSource dataSource = dataSource();
        if (dataSource == null) {
            return;
        }
        DataSourceConnectionManager connectionManager = new DataSourceConnectionManager(dataSource);
        JSqlClientImplementor sqlClient = getSqlClient(builder -> builder
            .setDialect(dialect())
            .setConnectionManager(connectionManager)
            .setSqlFormatter(SqlFormatter.PRETTY)
        );
        SchemaCreator schemaCreator = new SchemaCreator(sqlClient, DatabaseVersion.LATEST);
        schemaCreator.init();

        List<Table<?>> tables = new ArrayList<>();
        tables.add(Tables.MODIFY0_TABLE);
        List<ImmutableType> types = tables.stream().map(TableTypeProvider::getImmutableType).collect(Collectors.toList());
        DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, types);
        Assertions.assertDoesNotThrow(ddlAutoRunner::create);

        DDLDialect ddlDialect = DDLDialect.of(dialect(), DatabaseVersion.LATEST);
        String tableName = Tables.MODIFY0_TABLE.getImmutableType().getTableName(sqlClient.getMetadataStrategy());
        ImmutableProp lastName = Modify0Table.NAME0.unwrap();
        ColumnModifier columnModifier = ColumnModifier.of(ddlDialect, tableName, DDLUtils.getName(lastName, sqlClient.getMetadataStrategy()));
        List<String> alter = columnModifier.alter(new ColumnResolver(sqlClient, ddlDialect, Modify1Table.NAME1.unwrap()));
        Assertions.assertDoesNotThrow(() -> sqlClient.getConnectionManager().execute(connection -> {
            for (String s : alter) {
                System.out.println(s);
                try (PreparedStatement preparedStatement = connection.prepareStatement(s)) {
                    preparedStatement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }));
        Assertions.assertDoesNotThrow(ddlAutoRunner::drop);
    }

    @Test
    public void update() {
        DataSource dataSource = dataSource();
        if (dataSource == null) {
            return;
        }
        DataSourceConnectionManager connectionManager = new DataSourceConnectionManager(dataSource);
        JSqlClientImplementor sqlClient = getSqlClient(builder -> builder
            .setDialect(dialect())
            .setConnectionManager(connectionManager)
            .setSqlFormatter(SqlFormatter.PRETTY)
        );
        SchemaCreator schemaCreator = new SchemaCreator(sqlClient, DatabaseVersion.LATEST);
        schemaCreator.init();

        sqlClient.getConnectionManager().execute(connection -> {
            try (PreparedStatement preparedStatement = connection.prepareStatement("create table TEST_SCHEMA (\n" +
                                                                                   "    ID varchar(255) not null,\n" +
                                                                                   "    NAME0 varchar(20) default 'foo' not null,\n" +
                                                                                   "    primary key (ID)\n" +
                                                                                   ")")) {
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
        DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.UPDATE, Collections.singletonList(NewSchemaTable.$.getImmutableType()));
        ddlAutoRunner.init();
        Assertions.assertDoesNotThrow(ddlAutoRunner::create);

        List<Map<String, Object>> result = sqlClient.getConnectionManager().execute(connection -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                String tableName = NewSchemaTable.$.getImmutableType().getTableName(sqlClient.getMetadataStrategy());
                String name1Column = DDLUtils.getName(NewSchemaTable.NAME1.unwrap(), sqlClient.getMetadataStrategy());
                if (metaData.storesLowerCaseIdentifiers()) {
                    tableName = tableName.toLowerCase();
                    name1Column = name1Column.toLowerCase();
                } else if (metaData.storesUpperCaseIdentifiers()) {
                    tableName = tableName.toUpperCase();
                    name1Column = name1Column.toUpperCase();
                }
                ResultSet columns = metaData.getColumns(null, null, tableName, name1Column);
                return toMap(columns);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
        Assertions.assertFalse(result.isEmpty());

        Assertions.assertDoesNotThrow(ddlAutoRunner::drop);
    }

}
