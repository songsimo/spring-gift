package gift.order;

import gift.category.Category;
import gift.option.Option;
import gift.product.Product;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderService orderService;

    @Test
    @DisplayName("주문 목록 조회 시 해당 회원의 주문을 페이지로 반환한다")
    void getOrders_returnsPaginatedOrders() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Page<Order> page = new PageImpl<>(List.of(new Order(option, 1L, 2, "선물이에요")));
        given(orderRepository.findByMemberId(1L, Pageable.unpaged())).willReturn(page);

        Page<OrderResponse> result = orderService.getOrders(1L, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().quantity()).isEqualTo(2);
    }
}