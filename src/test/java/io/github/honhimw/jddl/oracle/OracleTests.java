package io.github.honhimw.jddl.oracle;

import io.github.honhimw.jddl.AbstractRealDBTests;
import org.babyfish.jimmer.sql.dialect.Dialect;
import org.babyfish.jimmer.sql.dialect.OracleDialect;

import java.util.Properties;

/**
 * @author honhimW
 * @since 2025-10-20
 */

public class OracleTests extends AbstractRealDBTests {

    @Override
    protected Dialect dialect() {
        return new OracleDialect();
    }

    @Override
    protected void setProperties(Properties properties) {
        properties.put("internal_logon", "sysdba");
    }
}
