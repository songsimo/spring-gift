package gift.member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE Member m SET m.point = m.point - :amount WHERE m.id = :id AND m.point >= :amount")
    int deductPointAtomic(@Param("id") Long id, @Param("amount") int amount);
}
