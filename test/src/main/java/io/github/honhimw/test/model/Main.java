package io.github.honhimw.test.model;

import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;
import org.babyfish.jimmer.sql.ManyToOne;
import org.babyfish.jimmer.sql.Table;
import org.jetbrains.annotations.Nullable;

/**
 * @author honhimW
 * @since 2025-11-03
 */

@Table(name = "MAIN_TABLE")
@Entity
public interface Main {

    @Id
    int id();

    String name();

    @Nullable
    @ManyToOne
    Referred refId();

}
