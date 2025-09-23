package io.github.honhimw.jddl.dialect;

import io.github.honhimw.jddl.DatabaseVersion;
import org.babyfish.jimmer.sql.dialect.TiDBDialect;

/**
 * @author honhimW
 */

public class TiDBDDLDialect extends MySqlDDLDialect {

    public TiDBDDLDialect() {
        this(null);
    }

    public TiDBDDLDialect(final DatabaseVersion version) {
        super(new TiDBDialect(), version);
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
