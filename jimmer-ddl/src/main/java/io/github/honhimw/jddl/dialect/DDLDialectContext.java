package io.github.honhimw.jddl.dialect;

import io.github.honhimw.jddl.DatabaseVersion;
import org.babyfish.jimmer.sql.dialect.*;
import org.jspecify.annotations.NonNull;

/**
 * @author honhimW
 * @since 2025-11-13
 */

public class DDLDialectContext {

    public final DatabaseVersion version;

    public final boolean preferQuoted;

    public final Dialect dialect;

    public static DDLDialectContext auto() {
        return new Builder().build();
    }

    public static DDLDialectContext of(Dialect dialect) {
        return new Builder()
            .dialect(dialect)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    private DDLDialectContext(
        DatabaseVersion version,
        boolean preferQuoted,
        Dialect dialect
    ) {
        this.version = version;
        this.preferQuoted = preferQuoted;
        this.dialect = dialect;
    }

    public DDLDialect select() {
        if (dialect instanceof DDLDialect) {
            return (DDLDialect) dialect;
        } else if (dialect instanceof H2Dialect) {
            return new H2DDLDialect(this);
        } else if (dialect instanceof MySqlDialect) {
            return new MySqlDDLDialect(this);
        } else if (dialect instanceof PostgresDialect) {
            return new PostgresDDLDialect(this);
        } else if (dialect instanceof OracleDialect) {
            return new OracleDDLDialect(this);
        } else if (dialect instanceof SqlServerDialect) {
            return new SqlServerDDLDialect(this);
        } else if (dialect instanceof SQLiteDialect) {
            return new SQLiteDDLDialect(this);
        } else if (dialect instanceof TiDBDialect) {
            return new TiDBDDLDialect(this);
        } else {
            return new DefaultDDLDialect(this);
        }
    }

    public static class Builder {
        private DatabaseVersion databaseVersion = DatabaseVersion.LATEST;
        private boolean preferQuoted = false;
        private Dialect dialect = DefaultDialect.INSTANCE;

        public Builder version(@NonNull DatabaseVersion databaseVersion) {
            this.databaseVersion = databaseVersion;
            return this;
        }

        public Builder preferQuoted(boolean preferQuoted) {
            this.preferQuoted = preferQuoted;
            return this;
        }

        public Builder dialect(@NonNull Dialect dialect) {
            this.dialect = dialect;
            return this;
        }

        public DDLDialectContext build() {
            return new DDLDialectContext(
                databaseVersion,
                preferQuoted,
                dialect
            );
        }
    }

}
