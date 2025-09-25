package io.github.honhimw.jddl;

import io.github.honhimw.jddl.anno.OnDeleteAction;
import io.github.honhimw.jddl.dialect.DDLDialect;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.sql.DatabaseMetaData;
import java.util.Collections;
import java.util.List;

/**
 * @author honhimW
 */

public class StandardForeignKeyExporter implements Exporter<ForeignKey> {

    protected final JSqlClientImplementor client;

    protected final DDLDialect dialect;

    public StandardForeignKeyExporter(JSqlClientImplementor client) {
        this.client = client;
        DatabaseVersion databaseVersion = client.getConnectionManager().execute(connection -> {
            try {
                DatabaseMetaData metaData = connection.getMetaData();
                int databaseMajorVersion = metaData.getDatabaseMajorVersion();
                int databaseMinorVersion = metaData.getDatabaseMinorVersion();
                String databaseProductVersion = metaData.getDatabaseProductVersion();
                return new DatabaseVersion(databaseMajorVersion, databaseMinorVersion, databaseProductVersion);
            } catch (Exception e) {
                // cannot get database version, using latest as default
                return DatabaseVersion.LATEST;
            }
        });
        this.dialect = DDLDialect.of(client.getDialect(), databaseVersion);
    }

    public StandardForeignKeyExporter(JSqlClientImplementor client, DatabaseVersion version) {
        this.client = client;
        this.dialect = DDLDialect.of(client.getDialect(), version);
    }

    @Override
    public List<String> getSqlCreateStrings(ForeignKey exportable) {
        if (!dialect.hasAlterTable()) {
            return Collections.emptyList();
        }
        BufferContext bufferContext = new BufferContext(this.client, exportable.table);
        String sourceTableName = exportable.table.getTableName(client.getMetadataStrategy());
        String targetTableName = exportable.referencedTable.getTableName(client.getMetadataStrategy());

        bufferContext.buf.append("alter table ");
        if (dialect.supportsIfExistsAfterAlterTable()) {
            bufferContext.buf.append("if exists ");
        }
        bufferContext.buf.append(sourceTableName);

        String joinColumnName = DDLUtils.getName(exportable.joinColumn, client.getMetadataStrategy());
        String foreignKeyName = getForeignKeyName(bufferContext, exportable);
        String definition = exportable.relation.definition();
        if (!definition.isEmpty()) {
            bufferContext.buf.append(" add constraint ")
                .append(dialect.quote(foreignKeyName))
                .append(' ')
                .append(definition);
        } else {
            bufferContext.buf.append(" add constraint ")
                .append(dialect.quote(foreignKeyName))
                .append(" foreign key (")
                .append(joinColumnName)
                .append(')')
                .append(" references ")
                .append(targetTableName)
                .append(" (")
                .append(DDLUtils.getName(exportable.referencedTable.getIdProp(), client.getMetadataStrategy()))
                .append(')');
        }
        OnDeleteAction action = exportable.relation.action();
        if (action != OnDeleteAction.NONE) {
            bufferContext.buf.append(" on delete ").append(action.sql);
        }
        return Collections.singletonList(bufferContext.buf.toString());
    }

    @Override
    public List<String> getSqlDropStrings(ForeignKey exportable) {
        if (!dialect.hasAlterTable()) {
            return Collections.emptyList();
        }
        BufferContext bufferContext = new BufferContext(this.client, exportable.table);
        bufferContext.buf.append("alter table ");
        if (dialect.supportsIfExistsAfterAlterTable()) {
            bufferContext.buf.append("if exists ");
        }
        bufferContext.buf
            .append(exportable.table.getTableName(client.getMetadataStrategy()))
            .append(' ')
            .append(dialect.getDropForeignKeyString())
            .append(' ');
        if (dialect.supportsIfExistsBeforeConstraintName()) {
            bufferContext.buf.append("if exists ");
        }
        bufferContext.buf.append(dialect.quote(getForeignKeyName(bufferContext, exportable)));
        return Collections.singletonList(bufferContext.buf.toString());
    }

    private String getForeignKeyName(BufferContext bufferContext, ForeignKey exportable) {
        String sourceTableName = exportable.table.getTableName(client.getMetadataStrategy());
        String foreignKeyName = exportable.relation.name();
        String joinColumnName = DDLUtils.getName(exportable.joinColumn, client.getMetadataStrategy());
        if (foreignKeyName.isEmpty()) {
            ConstraintNamingStrategy ns = bufferContext.getNamingStrategy(exportable.relation.naming());
            foreignKeyName = ns.determineForeignKeyName(sourceTableName, new String[]{joinColumnName});
        }
        return foreignKeyName;
    }

}
