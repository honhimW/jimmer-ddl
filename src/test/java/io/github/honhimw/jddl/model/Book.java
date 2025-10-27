package io.github.honhimw.jddl.model;

import org.babyfish.jimmer.Formula;
import org.babyfish.jimmer.client.TNullable;
import org.babyfish.jimmer.sql.*;
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The Book Entity $:)$
 */
@Entity
@KeyUniqueConstraint
public interface Book {

    /**
     * Id $:)$
     */
    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator.class)
    UUID id();

    @GeneratedValue(sequenceName = "another_id_generator")
    String anotherId();

    /**
     * Name $:)$
     */
    @Key
    String name();

    /**
     * Edition $:)$
     */
    @Key
    int edition();

    /**
     * Price $:)$
     */
    BigDecimal price();

    /**
     * Store $:)$
     *
     * <p>Note: This property can be null</p>
     */
    @TNullable // issue #1023
    @ManyToOne
    BookStore store();

    /**
     * Authors $:)$
     */
    @ManyToMany
    @JoinTable(
            name = "BOOK_AUTHOR_MAPPING",
            joinColumnName = "BOOK_ID",
            inverseJoinColumnName = "AUTHOR_ID",
            deletedWhenEndpointIsLogicallyDeleted = true
    )
    List<Author> authors();

    /**
     * StoreId $:)$
     *
     * <p>Note: This property can be null</p>
     */
    @IdView
    @Nullable
    UUID storeId();

    @Formula(dependencies = "store")
    default boolean alone() {
        return store() == null;
    }

    /**
     * AuthorIds $:)$
     */
    @IdView("authors")
    List<UUID> authorIds();

    @Formula(dependencies = "authors")
    default int authorCount() {
        return authors().size();
    }

    @Formula(dependencies = {"authors.firstName", "authors.lastName"})
    default List<String> authorFullNames() {
        List<String> fullNames = new ArrayList<>(authors().size());
        for (Author author : authors()) {
            fullNames.add(author.firstName() + "-" + author.lastName());
        }
        return fullNames;
    }
}
