package gift.order.repository;

import gift.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByMemberId(Long memberId, Pageable pageable);

    boolean existsByOptionId(Long optionId);

    boolean existsByOptionProductId(Long productId);
}
