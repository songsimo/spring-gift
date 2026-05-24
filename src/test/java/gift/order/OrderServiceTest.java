package gift.order;

import gift.category.model.Category;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.Option;
import gift.option.OptionRepository;
import gift.product.Product;
import gift.wish.model.Wish;
import gift.wish.repository.WishRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    OptionRepository optionRepository;

    @Mock
    MemberRepository memberRepository;

    @Mock
    KakaoMessageClient kakaoMessageClient;

    @Mock
    WishRepository wishRepository;

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
        OrderResponse first = result.getContent().getFirst();
        assertThat(first.quantity()).isEqualTo(2);
        assertThat(first.productName()).isEqualTo("MacBook");
        assertThat(first.optionName()).isEqualTo("실버 256GB");
        assertThat(first.totalPrice()).isEqualTo(2000000);
    }

    @Test
    @DisplayName("유효한 요청으로 주문 생성 시 주문을 반환한다")
    void createOrder_validRequest_returnsOrder() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Member member = new Member("test@test.com", "password");
        member.chargePoint(3000000);
        OrderRequest request = new OrderRequest(1L, 2, "선물이에요");
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "선물이에요"));

        OrderResponse result = orderService.createOrder(1L, request);

        assertThat(result.quantity()).isEqualTo(2);
        assertThat(result.message()).isEqualTo("선물이에요");
        assertThat(result.productName()).isEqualTo("MacBook");
        assertThat(result.optionName()).isEqualTo("실버 256GB");
        assertThat(result.totalPrice()).isEqualTo(2000000);
    }

    @Test
    @DisplayName("존재하지 않는 옵션으로 주문 생성 시 예외가 발생한다")
    void createOrder_optionNotFound_throwsException() {
        given(optionRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(1L, new OrderRequest(999L, 1, null)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("포인트 부족 시 주문 생성에서 예외가 발생한다")
    void createOrder_insufficientPoints_throwsException() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Member member = new Member("test@test.com", "password");
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));

        assertThatThrownBy(() -> orderService.createOrder(1L, new OrderRequest(1L, 2, null)))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("주문 시 해당 상품의 찜이 있으면 삭제된다")
    void createOrder_wishExists_removesWish() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Member member = new Member("test@test.com", "password");
        member.chargePoint(3000000);
        Wish wish = new Wish(1L, product);
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "선물이에요"));
        given(wishRepository.findByMemberIdAndProductId(1L, 1L)).willReturn(Optional.of(wish));

        orderService.createOrder(1L, new OrderRequest(1L, 2, "선물이에요"));

        then(wishRepository).should().delete(wish);
    }

    @Test
    @DisplayName("주문 시 해당 상품의 찜이 없으면 삭제 없이 진행된다")
    void createOrder_noWish_proceedsNormally() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Member member = new Member("test@test.com", "password");
        member.chargePoint(3000000);
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "선물이에요"));
        given(wishRepository.findByMemberIdAndProductId(1L, 1L)).willReturn(Optional.empty());

        orderService.createOrder(1L, new OrderRequest(1L, 2, "선물이에요"));

        then(wishRepository).should(never()).delete(any(Wish.class));
    }
}