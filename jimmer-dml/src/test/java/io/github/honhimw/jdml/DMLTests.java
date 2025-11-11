package io.github.honhimw.jdml;

import io.github.honhimw.jddl.DDLAuto;
import io.github.honhimw.jddl.DDLAutoRunner;
import io.github.honhimw.jddl.manual.TableBuilder;
import io.github.honhimw.jman.ManualDraftSpi;
import io.github.honhimw.jman.ManualImmutableSpi;
import io.github.honhimw.test.AbstractH2;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SimpleSaveResult;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author honhimW
 * @since 2025-10-28
 */

public class DMLTests extends AbstractH2 {

    @Override
    protected JSqlClientImplementor getSqlClient() {
        return super.getSqlClient();
    }

    @Test
    void crud() {
        JSqlClient.Builder builder = JSqlClient.newBuilder();
        applyBuilder(builder);
        JSqlClientImplementor sqlClient = DynJSqlClientImpl.from((JSqlClientImplementor.Builder) builder);
        ImmutableType referred = TableBuilder.of("REFERRED_TABLE")
            .addColumn(column -> column.name("id").type(UUID.class).primaryKey())
            .addColumn(column -> column.name("name").type(String.class))
            .build();
        ImmutableType main = TableBuilder.of("MAIN_TABLE")
            .addColumn(column -> column.name("id").type(Integer.TYPE).primaryKey().autoIncrement())
//            .u32("id").tableName("MAIN_TABLE")
            .addColumn(column -> column.name("name").type(String.class))
            .addRelation(fk -> fk
                .type(referred)
                .propName("ref")
                .self(column -> column.nullable(true))
            )
            .build();
        ImmutableType compositeId = TableBuilder.of("COMPOSITE_TABLE")
            .addColumn(column -> column.name("id0").type(String.class).primaryKey())
            .addColumn(column -> column.name("id1").type(String.class).primaryKey())
            .addColumn(column -> column.name("name").type(String.class))
            .build();
        try (DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, Arrays.asList(referred, main, compositeId))) {
            ddlAutoRunner.init();
            ddlAutoRunner.create();

            TableProxy<?> tableProxy = new DynTableProxy(main);
//            TableProxy<?> tableProxy = MainTable.$;
            // INSERT
            ManualImmutableSpi entity = new ManualDraftSpi(main)
                .set("id", 1)
                .set("name", "bar")
                .asImmutable();
            SimpleSaveResult<ManualImmutableSpi> insertResult = sqlClient.saveCommand(entity)
                .setMode(SaveMode.INSERT_ONLY)
                .execute();
            Assertions.assertEquals(1, insertResult.getModifiedEntity().__get("id"));

            // UPDATE
            Integer updateResult = sqlClient.createUpdate(tableProxy)
                .set(tableProxy.get("name"), "foo")
                .where(tableProxy.get("id").eq(1))
                .execute();

            Assertions.assertEquals(1, updateResult);

            // SELECT
            MutableRootQuery<?> query = sqlClient.createQuery(tableProxy)
                .where(tableProxy.get("id").eq(1));
            Object o = query.select(tableProxy).fetchFirst();
            Assertions.assertEquals(1, ImmutableObjects.get(o, "id"));
            Assertions.assertEquals("foo", ImmutableObjects.get(o, "name"));

            // DELETE
            Integer deleteResult = sqlClient.createDelete(tableProxy)
                .where(tableProxy.get("id").eq(1))
                .where(tableProxy.get("name").eq("foo"))
                .execute();
            Assertions.assertEquals(1, deleteResult);

            // JOIN
            Table<?> join = tableProxy.join("ref");
            Object o1 = sqlClient.createQuery(tableProxy)
                .where(join.get("id").eq(UUID.randomUUID()))
                .where(join.get("name").eq("???"))
                .select(tableProxy).fetchFirstOrNull();
            Assertions.assertNull(o1);

        }

    }

}
