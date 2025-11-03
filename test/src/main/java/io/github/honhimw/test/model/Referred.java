package io.github.honhimw.test.model;

import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Table;

import java.util.UUID;

/**
 * @author honhimW
 * @since 2025-11-03
 */

@Table(name = "REFERRED_TABLE")
@Entity
public interface Referred {

    @Id
    UUID id();

    String name();

}
