package fishtail.ecom.repository;

import fishtail.ecom.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

        /** Case-insensitive search by category */
        List<Product> findByCategoryIgnoreCase(String category);

        /** Case-insensitive partial match on title */
        List<Product> findByTitleContainingIgnoreCase(String keyword);

        /** Fuzzy search using MySQL SOUNDS LIKE to handle typos */
        @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM products WHERE title SOUNDS LIKE :keyword OR title LIKE %:keyword%", nativeQuery = true)
        List<Product> findByTitleFuzzy(String keyword);

        /** Find products in the same category, excluding the product itself */
        @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE p.category = :category AND p.id != :id")
        List<Product> findByCategoryAndIdNot(String category, Long id);

        /**
         * Find products in the same category within a price range, excluding the
         * product itself
         */
        @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p WHERE p.category = :category AND p.id != :id "
                        +
                        "AND p.discountedPrice BETWEEN :minPrice AND :maxPrice")
        List<Product> findSimilarProducts(String category, Long id, java.math.BigDecimal minPrice,
                        java.math.BigDecimal maxPrice);

        /** Get products ordered by total clicks descending (most popular first) */
        @org.springframework.data.jpa.repository.Query("SELECT p FROM Product p " +
                        "LEFT JOIN p.clickStats cs " +
                        "GROUP BY p.id " +
                        "ORDER BY COALESCE(SUM(cs.clickCount), 0) DESC")
        List<Product> findAllOrderByTotalClicksDesc();
}
