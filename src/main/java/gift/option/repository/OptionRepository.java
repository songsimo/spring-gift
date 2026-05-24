package gift.option.repository;

import gift.option.model.Option;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OptionRepository extends JpaRepository<Option, Long> {
    List<Option> findByProductId(Long productId);

    boolean existsByProductIdAndName(Long productId, String name);

    boolean existsByProductIdAndNameAndIdNot(Long productId, String name, Long excludeId);
}
