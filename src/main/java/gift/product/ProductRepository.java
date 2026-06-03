package gift.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByCategoryId(Long categoryId);

    @Query(
        value = "SELECT p FROM Product p JOIN FETCH p.category",
        countQuery = "SELECT count(p) FROM Product p"
    )
    Page<Product> findAllWithCategory(Pageable pageable);
}
