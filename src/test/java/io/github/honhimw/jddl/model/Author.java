package io.github.honhimw.jddl.model;

import org.babyfish.jimmer.Formula;
import org.babyfish.jimmer.sql.*;
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

@Entity
@KeyUniqueConstraint
public interface Author {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator.class)
    UUID id();

    @Key
    String firstName();

    @Key
    String lastName();

    @Formula(dependencies = {"firstName", "lastName"})
    default String fullName() {
        return firstName() + ' ' + lastName();
    }

    @Formula(sql = "length(%alias.FIRST_NAME) + length(%alias.LAST_NAME)")
    int fullNameLength();

    @Formula(sql = "concat(%alias.FIRST_NAME, ' ', %alias.LAST_NAME)")
    String fullName2();

    Gender gender();

    @ManyToMany(mappedBy = "authors")
    List<Book> books();

    @ManyToOne
    @Nullable
    @JoinTable(
            name = "AUTHOR_COUNTRY_MAPPING",
            joinColumnName = "AUTHOR_ID",
            inverseJoinColumnName = "COUNTRY_CODE",
            deletedWhenEndpointIsLogicallyDeleted = true
    )
    Country country();

    @Transient
    Organization organization();

    @Formula(dependencies = "books")
    default int bookCount() {
        return books().size();
    }
}
