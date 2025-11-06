package io.github.honhimw.jdml;

import io.github.honhimw.jddl.DDLAuto;
import io.github.honhimw.jddl.DDLAutoRunner;
import io.github.honhimw.jddl.manual.ManualTypeBuilder;
import io.github.honhimw.jman.ManualImmutableSpi;
import io.github.honhimw.test.AbstractH2;
import io.github.honhimw.test.model.*;
import org.babyfish.jimmer.ImmutableObjects;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.JSqlClient;
import org.babyfish.jimmer.sql.ast.mutation.SaveMode;
import org.babyfish.jimmer.sql.ast.mutation.SimpleSaveResult;
import org.babyfish.jimmer.sql.ast.query.MutableRootQuery;
import org.babyfish.jimmer.sql.ast.table.Table;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

/**
 * @author honhimW
 * @since 2025-11-03
 */

public class NativeTests extends AbstractH2 {

    @Test
    void crud() {
        JSqlClientImplementor sqlClient = getSqlClient();

        try (DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, Arrays.asList(
            MainTable.$.getImmutableType(),
            ReferredTable.$.getImmutableType(),
            BookStoreTable.$.getImmutableType()
        ))) {
            ddlAutoRunner.init();
            ddlAutoRunner.create();

            // INSERT
            Main entity = MainDraft.$.produce(draft -> draft
                .setId(1)
                .setName("bar")
            );
            SimpleSaveResult<Main> insertResult = sqlClient.saveCommand(entity)
                .setMode(SaveMode.INSERT_ONLY)
                .execute();
            Assertions.assertEquals(1, insertResult.getModifiedEntity().id());

            // UPDATE
            Integer updateResult = sqlClient.createUpdate(MainTable.$)
                .set(MainTable.$.get("name"), "foo")
                .where(MainTable.$.get("id").eq(1))
                .execute();

            Assertions.assertEquals(1, updateResult);

            // SELECT
            MutableRootQuery<MainTable> query = sqlClient.createQuery(MainTable.$)
                .where(MainTable.$.get("id").eq(1));
            Object o = query.select(MainTable.$).fetchFirst();
            Assertions.assertEquals(1, ImmutableObjects.get(o, "id"));
            Assertions.assertEquals("foo", ImmutableObjects.get(o, "name"));

            // DELETE
            Integer deleteResult = sqlClient.createDelete(MainTable.$)
                .where(MainTable.$.get("id").eq(1))
                .where(MainTable.$.get("name").eq("foo"))
                .execute();
            Assertions.assertEquals(1, deleteResult);

            // JOIN
//            AnyDelayedOperation anyDelayedOperation = new AnyDelayedOperation(tableProxy, testType.getProp("ref"), JoinType.INNER, null);
//            tableProxy = new AnyTableProxy(testType, anyDelayedOperation);
            Table<?> join = MainTable.$.join("ref");
            Object o1 = sqlClient.createQuery(MainTable.$)
                .where(join.get("id").eq(UUID.randomUUID()))
                .where(join.get("name").eq("???"))
                .select(MainTable.$).fetchFirstOrNull();
            System.out.println(o1);

        }

    }

}
