package io.github.honhimw.jddl.runtime;

import com.zaxxer.hikari.HikariDataSource;
import io.github.honhimw.jddl.AbstractRealDB;
import io.github.honhimw.jddl.DDLAuto;
import io.github.honhimw.jddl.DDLAutoRunner;
import io.github.honhimw.jddl.DDLUtils;
import io.github.honhimw.jddl.anno.*;
import io.github.honhimw.jddl.manual.ManualImmutablePropImpl;
import io.github.honhimw.jddl.manual.ManualImmutableTypeImpl;
import io.github.honhimw.jddl.manual.ManualTypeBuilder;
import org.babyfish.jimmer.meta.ImmutableType;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.H2Dialect;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

/**
 * @author honhimW
 * @since 2025-10-23
 */

public class DDLDesignTests extends AbstractRealDB {

    @Test
    void create() {
        ManualImmutableTypeImpl manualImmutableTypeImpl = new ManualImmutableTypeImpl();
        manualImmutableTypeImpl.tableName = "TEST_TABLE";
        manualImmutableTypeImpl.javaClass = Object.class;
        manualImmutableTypeImpl.props = new LinkedHashMap<>();

        DDLUtils.DefaultTableDef tableDef = new DDLUtils.DefaultTableDef();
        tableDef.comment = "comment on table";
        DDLUtils.DefaultIndex defaultIndex = new DDLUtils.DefaultIndex();
        defaultIndex.columns = new String[]{"name"};
        tableDef.indexes = new Index[]{defaultIndex};
        DDLUtils.DefaultUnique defaultUnique = new DDLUtils.DefaultUnique();
        defaultUnique.columns = new String[]{"name"};
        tableDef.uniques = new Unique[]{defaultUnique};
        tableDef.checks = new Check[]{new DDLUtils.DefaultCheck("#name <> ''")};
        manualImmutableTypeImpl.annotations = new Annotation[]{tableDef};

        ManualImmutablePropImpl id = new ManualImmutablePropImpl();
        id.name = "id";
        id.returnClass = String.class;
        id.isNullable = false;
        id.isId = true;
        id.isColumnDefinition = true;

        manualImmutableTypeImpl.idProp = id;
        manualImmutableTypeImpl.props.put("id", id);

        ManualImmutablePropImpl name = new ManualImmutablePropImpl();
        name.name = "name";
        name.returnClass = String.class;
        name.isNullable = false;
        name.isId = false;
        name.isColumnDefinition = true;
        DDLUtils.DefaultColumnDef defaultColumnDef = new DDLUtils.DefaultColumnDef();
        defaultColumnDef.length = 1024;
        defaultColumnDef.defaultValue = "'foo'";
        defaultColumnDef.comment = "comment on column";
        name.annotations = new Annotation[]{defaultColumnDef};
        manualImmutableTypeImpl.props.put("name", name);
        manualImmutableTypeImpl.selectableProps = manualImmutableTypeImpl.props;

        List<ManualImmutableTypeImpl> fakeImmutableTypes = Collections.singletonList(manualImmutableTypeImpl);

        JSqlClientImplementor sqlClient = getSqlClient();
        DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, fakeImmutableTypes);
        ddlAutoRunner.init();
        ddlAutoRunner.create();
        ddlAutoRunner.drop();
    }

    @Test
    void builder() {
        ManualTypeBuilder builder = ManualTypeBuilder.u64("id");
        ImmutableType build = builder
            .name("TEST_TABLE2")
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
            .addRelation((fk, column) -> {
                fk
                    .tableName("TEST_TABLE3")
                    .propName("table3")
                    .action(OnDeleteAction.CASCADE);
                column
                    .name("id")
                    .type(UUID.class);
            })
            .comment("comment on table")
            .build();
        List<ImmutableType> fakeImmutableTypes = Collections.singletonList(build);

        JSqlClientImplementor sqlClient = getSqlClient();
        DDLAutoRunner ddlAutoRunner = new DDLAutoRunner(sqlClient, DDLAuto.CREATE_DROP, fakeImmutableTypes);
        ddlAutoRunner.init();
        ddlAutoRunner.create();
        ddlAutoRunner.drop();
    }

    @Override
    protected Dialect dialect() {
        return new H2Dialect();
    }

    @Override
    protected @Nullable DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:h2:mem:test;MODE\\=Regular");
        dataSource.setDriverClassName("org.h2.Driver");
        return dataSource;
    }
}
