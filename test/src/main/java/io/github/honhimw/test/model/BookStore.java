package io.github.honhimw.test.model;

import org.babyfish.jimmer.Formula;
import org.babyfish.jimmer.sql.*;
import org.babyfish.jimmer.sql.meta.UUIDIdGenerator;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * The BookStore entity $:)$
 */
@Entity
@KeyUniqueConstraint
public interface BookStore {

    @Id
    @GeneratedValue(generatorType = UUIDIdGenerator.class)
    UUID id();

    @Key
    String name();

    String website();

    @Version
    int version();

    @OneToMany(mappedBy = "store")
    List<Book> books();

    @Formula(dependencies = "books.price")
    default BigDecimal maxPrice() {
        BigDecimal maxPrice = BigDecimal.ZERO;
        for (Book book : books()) {
            BigDecimal price = book.price();
            if (maxPrice.compareTo(price) < 0) {
                maxPrice = price;
            }
        }
        return maxPrice;
    }
}
