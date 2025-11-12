package io.github.honhimw.jddl.runtime;

import io.github.honhimw.jddl.DDLAuto;
import io.github.honhimw.jddl.DDLAutoRunner;
import io.github.honhimw.jddl.anno.Kind;
import io.github.honhimw.jddl.anno.OnDeleteAction;
import io.github.honhimw.jddl.manual.TableBuilder;
import io.github.honhimw.test.AbstractH2;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author honhimW
 * @since 2025-10-23
 */

public class DDLDesignTests extends AbstractH2 {

    @Test
    void builder() {
        List<ImmutableType> types = new ArrayList<>();
        TableBuilder table3Builder = TableBuilder.of("TEST_TABLE3")
            .addColumn(column -> column.name("id").type(UUID.class).primaryKey());
        ImmutableType table3 = table3Builder.build();
        types.add(table3);
        TableBuilder table4Builder = TableBuilder.of("TEST_TABLE4")
            .addColumn(column -> column.name("id").type(Integer.TYPE).primaryKey().autoIncrement());
        ImmutableType table4 = table4Builder.build();
        types.add(table4);
        ImmutableType table2 = TableBuilder.of("TEST_TABLE2")
            .addColumn(column -> column.name("id").type(Long.TYPE).primaryKey().autoIncrement())
            .tableName("TEST_TABLE2")
            .addIndex(Kind.PATH, "name")
            .addUnique(Kind.PATH, "name")
            .addCheck("#name <> ''")
            .addColumn(column -> column
                .name("name")
                .type(String.class)
                .nullable(false)
                .length(1024)
                .defaultValue("'foo'")
                .comment("comment on column")
            )
            .addColumn("uuidValue", UUID.class)
            .addColumn(column -> column.name("uuid2").type(UUID.class).columnName("uuid_value_2"))
            .addRelation(fk -> fk
                .propName("table3")
                .action(OnDeleteAction.CASCADE)
                .type(table3)
            )
            .addRelation(fk -> fk
                .tableName("TEST_TABLE4")
                .propName("table4")
                .action(OnDeleteAction.SET_DEFAULT)
                .self(column -> column.comment("reference to table4").defaultValue("-1"))
                .id(column -> column.name("id").type(Integer.class))
            )
            .comment("comment on table")
            .build();
        types.add(table2);
        JSqlClientImplementor sqlClient = getSqlClient();
        DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, types);
        ddlAutoRunner.init();
        ddlAutoRunner.create();
        ddlAutoRunner.drop();
    }

}
