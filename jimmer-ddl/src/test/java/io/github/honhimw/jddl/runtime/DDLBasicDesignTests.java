package io.github.honhimw.jddl.runtime;

import io.github.honhimw.jddl.DDLAuto;
import io.github.honhimw.jddl.DDLAutoRunner;
import io.github.honhimw.jman.ManualTypeBuilder;
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

public class DDLBasicDesignTests extends AbstractH2 {

    @Test
    void builder() {
        List<ImmutableType> types = new ArrayList<>();
        ManualTypeBuilder<?, ?, ?> table3Builder = ManualTypeBuilder.of("TEST_TABLE3")
            .addColumn(column -> column.name("id").type(UUID.class).primaryKey());
        ImmutableType table3 = table3Builder.build();
        types.add(table3);
        ManualTypeBuilder<?, ?, ?> table4Builder = ManualTypeBuilder.of("TEST_TABLE4")
            .addColumn(column -> column.name("id").type(Integer.TYPE).primaryKey().autoIncrement());
        ImmutableType table4 = table4Builder.build();
        types.add(table4);
        ImmutableType table2 = ManualTypeBuilder.of("TEST_TABLE2")
            .addColumn(column -> column.name("id").type(Long.TYPE).primaryKey().autoIncrement())
            .tableName("TEST_TABLE2")
            .addColumn(column -> column
                .name("name")
                .type(String.class)
            )
            .addColumn("uuidValue", UUID.class)
            .addRelation(fk -> fk
                .propName("table3")
                .type(table3)
            )
            .addRelation(fk -> fk
                .tableName("TEST_TABLE4")
                .propName("table4")
                .id(column -> column.name("id").type(Integer.class))
            )
            .build();
        types.add(table2);
        JSqlClientImplementor sqlClient = getSqlClient();
        DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, types);
        ddlAutoRunner.init();
        ddlAutoRunner.create();
        ddlAutoRunner.drop();
    }

}
