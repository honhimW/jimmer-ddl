package io.github.honhimw.jddl;

import io.github.honhimw.jddl.dialect.DDLDialect;
import org.babyfish.jimmer.meta.ImmutableProp;
import org.babyfish.jimmer.sql.GeneratedValue;
import org.babyfish.jimmer.sql.runtime.JSqlClientImplementor;

import java.sql.DatabaseMetaData;
import java.util.Collections;
import java.util.List;

/**
 * @author honhimW
 */

public class StandardSequenceExporter implements Exporter<ImmutableProp> {

    protected final JSqlClientImplementor client;

    protected final DDLDialect dialect;

    public StandardSequenceExporter(JSqlClientImplementor client) {
        this.client = client;
        DatabaseVersion databaseVersion = DDLUtils.getDatabaseVersion(client);
        this.dialect = DDLDialect.of(client.getDialect(), databaseVersion);
    }

    public StandardSequenceExporter(JSqlClientImplementor client, DatabaseVersion version) {
        this.client = client;
        this.dialect = DDLDialect.of(client.getDialect(), version);
    }

    @Override
    public List<String> getSqlCreateStrings(ImmutableProp exportable) {
        if (dialect.supportsSequence()) {
            GeneratedValue annotation = exportable.getAnnotation(GeneratedValue.class);
            if (annotation != null) {
                if (!annotation.sequenceName().isEmpty()) {
                    String createSequenceString = dialect.getCreateSequenceString(annotation.sequenceName(), 1, 1);
                    return Collections.singletonList(createSequenceString);
                }
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<String> getSqlDropStrings(ImmutableProp exportable) {
        if (dialect.supportsSequence()) {
            GeneratedValue annotation = exportable.getAnnotation(GeneratedValue.class);
            if (annotation != null) {
                if (!annotation.sequenceName().isEmpty()) {
                    String createSequenceString = dialect.getDropSequenceString(annotation.sequenceName());
                    return Collections.singletonList(createSequenceString);
                }
            }
        }
        return Collections.emptyList();
    }

}
