package io.github.honhimw.test.model;

import org.babyfish.jimmer.sql.Entity;
import org.babyfish.jimmer.sql.Id;

@Entity
public interface Card {

    @Id
    long id();

    String name();
}
