package io.github.honhimw.jddl.dialect;

import org.babyfish.jimmer.sql.dialect.TiDBDialect;

/**
 * @author honhimW
 */

public class TiDBDDLDialect extends MySqlDDLDialect {

    public TiDBDDLDialect() {
        this(DDLDialectContext.of(new TiDBDialect()));
    }

    public TiDBDDLDialect(final DDLDialectContext ctx) {
        super(ctx);
    }

    @Override
    public boolean needsStartingValue() {
        return true;
    }

    @Override
    public String getCreateSequenceString(String sequenceName, int initialValue, int incrementSize) {
        return getCreateSequenceString(sequenceName)
               + " start with " + initialValue
               + " increment by " + incrementSize
               + startingValue(initialValue, incrementSize);
    }

}
