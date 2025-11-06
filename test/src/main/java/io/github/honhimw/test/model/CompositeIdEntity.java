package io.github.honhimw.test.model;

import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.Table;
import org.jspecify.annotations.Nullable;

/**
 * @author honhimW
 * @since 2025-11-06
 */

@Entity
@Table(name = CompositeIdEntity.TABLE_NAME)
public interface CompositeIdEntity {

    String TABLE_NAME = "composite_id";

    @Id
    CompositeId id();

    @Nullable
    String name();

}
