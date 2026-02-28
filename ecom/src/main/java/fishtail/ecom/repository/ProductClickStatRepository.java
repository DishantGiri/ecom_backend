package fishtail.ecom.repository;

import fishtail.ecom.entity.ProductClickStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductClickStatRepository extends JpaRepository<ProductClickStat, Long> {
    Optional<ProductClickStat> findByProductIdAndCountry(Long productId, String country);
}
