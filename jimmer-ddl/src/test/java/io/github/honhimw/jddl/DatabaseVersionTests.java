package io.github.honhimw.jddl;

import org.junit.jupiter.api.Test;

/**
 * @author honhimW
 * @since 2025-10-29
 */

public class DatabaseVersionTests {

    @Test
    void simple() {
        DatabaseVersion databaseVersion = new DatabaseVersion(1, 0, "");
        assert databaseVersion.isSameOrAfter(0);
        assert !databaseVersion.isSameOrAfter(1, 1);
    }

    @Test
    void productVersion() {
        DatabaseVersion databaseVersion = new DatabaseVersion(1, 0, "2.4.300");
        assert databaseVersion.isSameOrAfter(0, 9);
        assert !databaseVersion.isSameOrAfter(2);
        assert !databaseVersion.isSameOrAfter(2, 4, 400);
        assert databaseVersion.isSameOrAfter(2, 4, 299, 0, 0, 1);
        assert !databaseVersion.isSameOrAfter(2, 4, 300, 1);

        assert DatabaseVersion.LATEST.isSameOrAfter(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

}
