package io.github.honhimw.jddl;

/**
 * @author honhimW
 * @since 2025-09-05
 */

public enum DDLAuto {
    /**
     * Create the schema.
     */
    CREATE,
    /**
     * Create and then destroy the schema at the end of the session.
     */
    CREATE_DROP,
    /**
     * Drop the schema at the end of the session.
     */
    DROP,
    /**
     * Disable DDL handling.
     */
    NONE,
}
