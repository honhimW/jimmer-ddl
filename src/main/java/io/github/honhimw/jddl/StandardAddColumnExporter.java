package io.github.honhimw.jddl;

import io.github.honhimw.jddl.anno.ColumnDef;
import io.github.honhimw.jddl.column.ColumnResolver;
import io.github.honhimw.jddl.dialect.DDLDialect;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.util.Collections;
import java.util.List;

/**
 * @author honhimW
 */

public class StandardAddColumnExporter implements Exporter<TypeProp> {

    protected final JSqlClientImplementor client;

    protected final DDLDialect dialect;

    public StandardAddColumnExporter(JSqlClientImplementor client) {
        this.client = client;
        DatabaseVersion databaseVersion = DDLUtils.getDatabaseVersion(client);
        this.dialect = DDLDialect.of(client.getDialect(), databaseVersion);
    }

    public StandardAddColumnExporter(JSqlClientImplementor client, DatabaseVersion version) {
        this.client = client;
        this.dialect = DDLDialect.of(client.getDialect(), version);
    }

    @Override
    public List<String> getSqlCreateStrings(TypeProp exportable) {
        ImmutableProp prop = exportable.prop;
        if (prop.isId() || !prop.isColumnDefinition()) {
            return Collections.emptyList();
        }
        ColumnDef colDef = prop.getAnnotation(ColumnDef.class);

        ColumnResolver columnResolver = new ColumnResolver(client, dialect, prop);

        StringBuilder buf = new StringBuilder();
        buf
            .append(dialect.getAlterTableString()).append(' ')
            .append(exportable.type.getTableName(client.getMetadataStrategy())).append(' ')
            .append(dialect.getAddColumnString()).append(' ')
            .append(DDLUtils.getName(prop, client.getMetadataStrategy())).append(' ')
        ;

        String columnDefinition = columnResolver.columnDefinition();
        if (!columnDefinition.isEmpty()) {
            buf.append(colDef.definition());
            return Collections.singletonList(buf.toString());
        }
        boolean nullable = columnResolver.nullable();
        String columnType = columnResolver.columnType();
        Object defaultValue = columnResolver.defaultValue();
        buf.append(columnType);

        if (defaultValue != null) {
            buf.append(" default ").append(defaultValue);
        }

        if (nullable) {
            buf.append(dialect.getNullColumnString());
        } else {
            buf.append(" not null");
        }
        return Collections.singletonList(buf.toString());
    }

    @Override
    public List<String> getSqlDropStrings(TypeProp exportable) {
        StringBuilder buf = new StringBuilder();
        buf
            .append(dialect.getAlterTableString()).append(' ')
            .append(exportable.type.getTableName(client.getMetadataStrategy()))
            .append(" drop column ")
            .append(DDLUtils.getName(exportable.prop, client.getMetadataStrategy()));
        return Collections.singletonList(buf.toString());
    }

}
