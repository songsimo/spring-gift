package gift.order;

import gift.category.Category;
import gift.exception.NotFoundException;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.product.Product;
import gift.wish.WishRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import gift.wish.Wish;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private WishRepository wishRepository;

    @Mock
    private NotificationPort notificationPort;

    @InjectMocks
    private OrderService orderService;

    private Product sampleProduct() {
        return new Product("사과", 1000, "apple.png",
            new Category("식품", "#fff", "img.png", "desc"));
    }

    @Test
    void create_주문을_저장하고_반환한다() {
        var product = sampleProduct();
        var option = new Option(product, "대", 100);
        var member = new Member("test@test.com", "pass");
        member.chargePoint(10000);
        var request = new OrderRequest(1L, 2, "감사합니다");
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.save(any())).willReturn(member);
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "감사합니다"));

        OrderResponse result = orderService.create(member, request);

        assertThat(result.quantity()).isEqualTo(2);
    }

    @Test
    void create_존재하지_않는_옵션은_NotFoundException을_던진다() {
        var member = new Member("test@test.com", "pass");
        given(optionRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(member, new OrderRequest(99L, 1, null)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_위시리스트에_있는_상품이면_주문_후_위시를_삭제한다() {
        var product = sampleProduct();
        var option = new Option(product, "대", 100);
        var member = new Member("test@test.com", "pass");
        member.chargePoint(10000);
        var wish = mock(Wish.class);
        var request = new OrderRequest(1L, 2, "감사합니다");
        given(optionRepository.findById(1L)).willReturn(java.util.Optional.of(option));
        given(memberRepository.save(any())).willReturn(member);
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "감사합니다"));
        given(wishRepository.findByMemberIdAndProductId(any(), any()))
            .willReturn(java.util.Optional.of(wish));

        orderService.create(member, request);

        verify(wishRepository).delete(wish);
    }

    @Test
    void notifyOrder_알림을_전송한다() {
        var product = sampleProduct();
        var option = new Option(product, "대", 100);
        var member = new Member("test@test.com", "pass");
        var order = new Order(option, 1L, 2, "감사합니다");
        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        orderService.notifyOrder(member, 1L);

        verify(notificationPort).notify(member, order, product);
    }

    @Test
    void getOrders_주문_목록을_반환한다() {
        given(orderRepository.findByMemberId(any(), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of()));

        var result = orderService.getOrders(1L, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }
}
