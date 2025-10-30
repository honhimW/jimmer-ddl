package io.github.honhimw.jdml;

import io.github.honhimw.jddl.DDLAuto;
import io.github.honhimw.jddl.DDLAutoRunner;
import io.github.honhimw.jddl.manual.ManualTypeBuilder;
import io.github.honhimw.jman.ManualDraftSpi;
import io.github.honhimw.jman.ManualImmutableSpi;
import io.github.honhimw.jman.ManualImmutableTypeImpl;
import io.github.honhimw.test.AbstractH2;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.impl.mutation.ISimpleEntitySaveCommandImpl;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SimpleSaveResult;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.table.spi.TableProxy;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

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
    void select() {
        JSqlClient.Builder builder = JSqlClient.newBuilder();
        applyBuilder(builder);
        JSqlClientImplementor sqlClient = DynamicJSqlClientImpl.from((JSqlClientImplementor.Builder) builder);
        ManualImmutableTypeImpl testType = (ManualImmutableTypeImpl) ManualTypeBuilder.u32("id")
            .tableName("test")
            .addColumn(column -> column
                .name("name")
                .type(String.class)
            )
            .build();
        AnyTableProxy tableProxy = new AnyTableProxy(testType);
        try (DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, Collections.singletonList(testType))) {
            ddlAutoRunner.init();
            ddlAutoRunner.create();

            // INSERT
            ManualImmutableSpi entity = new ManualImmutableSpi(testType);
//            entity.set("id", 1);
            entity.set("name", "bar");
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

            MutableRootQuery<TableProxy<Object>> query = sqlClient.createQuery(tableProxy);
            MutableRootQuery<TableProxy<Object>> id = query.where(tableProxy.get("id").eq(1));
            Object o = id.select(tableProxy).fetchOptional().orElse(null);
            Assertions.assertEquals(1, ImmutableObjects.get(o, "id"));
            Assertions.assertEquals("foo", ImmutableObjects.get(o, "name"));

            // DELETE
            Integer deleteResult = sqlClient.createDelete(tableProxy)
                .where(tableProxy.get("id").eq(1))
                .where(tableProxy.get("name").eq("foo"))
                .execute();
            Assertions.assertEquals(1, deleteResult);

        }

    }

}
