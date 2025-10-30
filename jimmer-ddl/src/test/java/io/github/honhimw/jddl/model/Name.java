package io.github.honhimw.jddl.model;

import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;

/**
 * @author hon_him
 * @since 2025-03-06
 */

@Entity
public interface Name {

    @Id
    String id();

    String firstName();

    String lastName();
}
