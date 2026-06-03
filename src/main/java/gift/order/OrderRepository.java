package gift.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByOptionProductId(Long productId);

    @Query("SELECT o FROM Order o JOIN FETCH o.option opt JOIN FETCH opt.product WHERE o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);
}
