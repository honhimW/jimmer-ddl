import io.github.honhimw.jddl.dialect.DDLDialect;
import io.github.honhimw.jddl.dialect.DDLDialectContext;
import io.github.honhimw.jddl.dialect.DDLDialectDelegate;
import org.babyfish.jimmer.sql.dialect.DefaultDialect;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author honhimW
 * @since 2025-11-13
 */

public class DialectTests {

    @Test
    void quote() {
        DDLDialect dialect = DDLDialect.of(DefaultDialect.INSTANCE);
        Assertions.assertNull(dialect.quote(null));
        Assertions.assertEquals("\"name\"", dialect.quote("\"name\""));
        Assertions.assertEquals("\"name\"", dialect.quote("`name"));
        Assertions.assertEquals("\"foo bar\"", dialect.quote("foo bar"));
        Assertions.assertEquals("name", dialect.quote("name"));

        dialect = DDLDialectContext.builder()
            .preferQuoted(true)
            .build()
            .select();
        Assertions.assertNull(dialect.quote(null));
        Assertions.assertEquals("\"name\"", dialect.quote("\"name\""));
        Assertions.assertEquals("\"name\"", dialect.quote("`name"));
        Assertions.assertEquals("\"foo bar\"", dialect.quote("foo bar"));
        Assertions.assertEquals("\"name\"", dialect.quote("name"));
    }

}
