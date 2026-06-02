package gift.order;

import gift.category.Category;
import gift.category.CategoryRepository;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.product.Product;
import gift.product.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderServiceConcurrencyTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private OptionRepository optionRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    private static final int THREAD_COUNT = 10;
    private Long optionId;
    private Long memberId;
    private Long productId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        transactionTemplate.execute(status -> {
            var category = categoryRepository.save(
                new Category("동시성테스트", "#ffffff", "img.png", "설명"));
            var product = productRepository.save(
                new Product("테스트상품", 1000, "img.png", category));
            var option = optionRepository.save(
                new Option(product, "기본옵션", THREAD_COUNT));
            var member = memberRepository.save(new Member("concurrency@test.com", "pw"));
            member.chargePoint(THREAD_COUNT * 1000 * 10);
            memberRepository.save(member);
            categoryId = category.getId();
            productId = product.getId();
            optionId = option.getId();
            memberId = member.getId();
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        transactionTemplate.execute(status -> {
            orderRepository.findAll().stream()
                .filter(o -> memberId.equals(o.getMemberId()))
                .forEach(orderRepository::delete);
            productRepository.deleteById(productId);
            categoryRepository.deleteById(categoryId);
            memberRepository.deleteById(memberId);
            return null;
        });
    }

    @Test
    void 동시_주문_재고_차감이_정확해야_한다() throws InterruptedException {
        var executor = Executors.newFixedThreadPool(THREAD_COUNT);
        var ready = new CountDownLatch(THREAD_COUNT);
        var start = new CountDownLatch(1);
        var done = new CountDownLatch(THREAD_COUNT);
        var successCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    var member = memberRepository.findById(memberId).orElseThrow();
                    orderService.create(member, new OrderRequest(optionId, 1, null));
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        var finalOption = optionRepository.findById(optionId).orElseThrow();
        assertThat(successCount.get()).isEqualTo(THREAD_COUNT);
        assertThat(finalOption.getQuantity()).isEqualTo(0);
    }
}
