package gift.wish;

import gift.category.Category;
import gift.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class WishRepositoryTest {

    @Autowired
    WishRepository wishRepository;

    @Autowired
    TestEntityManager em;

    @Test
    @DisplayName("동일한 (member_id, product_id) 조합으로 두 번 저장하면 예외가 발생한다")
    void duplicateWish_throwsDataIntegrityViolationException() {
        Category category = em.persist(new Category("전자기기", "#1E90FF", "https://img.com/e.png", "전자제품"));
        Product product = em.persist(new Product("MacBook", 1000000, "https://img.com/mac.png", category));

        wishRepository.save(new Wish(1L, product));
        em.flush();

        assertThatThrownBy(() -> {
            wishRepository.save(new Wish(1L, product));
            em.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
