package gift.order;

import gift.category.model.Category;
import gift.member.model.Member;
import gift.member.repository.MemberRepository;
import gift.option.model.Option;
import gift.option.repository.OptionRepository;
import gift.order.model.Order;
import gift.order.repository.OrderRepository;
import gift.order.service.KakaoMessageClient;
import gift.order.service.OrderRequest;
import gift.order.service.OrderResponse;
import gift.order.service.OrderService;
import gift.product.model.Product;
import gift.exception.NotFoundException;
import gift.wish.service.WishService;
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

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
    WishService wishService;

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
            .isInstanceOf(NotFoundException.class);
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
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "선물이에요"));

        orderService.createOrder(1L, new OrderRequest(1L, 2, "선물이에요"));

        then(wishService).should().removeByMemberAndProduct(1L, 1L);
    }

    @Test
    @DisplayName("주문 시 찜 삭제 처리를 WishService에 위임한다")
    void createOrder_delegatesWishRemovalToWishService() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Member member = new Member("test@test.com", "password");
        member.chargePoint(3000000);
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "선물이에요"));

        orderService.createOrder(1L, new OrderRequest(1L, 2, "선물이에요"));

        then(wishService).should().removeByMemberAndProduct(1L, 1L);
    }

    @Test
    @DisplayName("카카오 토큰이 있고 알림 전송 성공 시 notificationSent가 true다")
    void createOrder_kakaoTokenExists_sendSucceeds_notificationSentTrue() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Member member = new Member("test@test.com", "password");
        member.chargePoint(3000000);
        member.updateKakaoAccessToken("kakao-token");
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "선물이에요"));

        OrderResponse result = orderService.createOrder(1L, new OrderRequest(1L, 2, "선물이에요"));

        assertThat(result.notificationSent()).isTrue();
    }

    @Test
    @DisplayName("카카오 토큰이 없으면 notificationSent가 false다")
    void createOrder_noKakaoToken_notificationSentFalse() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Member member = new Member("test@test.com", "password");
        member.chargePoint(3000000);
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "선물이에요"));

        OrderResponse result = orderService.createOrder(1L, new OrderRequest(1L, 2, "선물이에요"));

        assertThat(result.notificationSent()).isFalse();
    }

    @Test
    @DisplayName("카카오 알림 전송 실패 시 notificationSent가 false이고 주문은 성공한다")
    void createOrder_kakaoSendFails_notificationSentFalse_orderSucceeds() {
        Category category = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        Product product = new Product(1L, "MacBook", 1000000, "https://example.com/mac.png", category);
        Option option = new Option(1L, product, "실버 256GB", 10);
        Member member = new Member("test@test.com", "password");
        member.chargePoint(3000000);
        member.updateKakaoAccessToken("kakao-token");
        given(optionRepository.findById(1L)).willReturn(Optional.of(option));
        given(memberRepository.findById(1L)).willReturn(Optional.of(member));
        given(orderRepository.save(any())).willReturn(new Order(option, 1L, 2, "선물이에요"));
        org.mockito.Mockito.doThrow(new RuntimeException("카카오 서버 오류"))
            .when(kakaoMessageClient).sendToMe(any(), any(), any());

        OrderResponse result = orderService.createOrder(1L, new OrderRequest(1L, 2, "선물이에요"));

        assertThat(result.notificationSent()).isFalse();
        assertThat(result.quantity()).isEqualTo(2);
    }

    @Test
    @DisplayName("getOrders는 @Transactional(readOnly = true)이 선언되어 있다")
    void getOrders_hasReadOnlyTransactionalAnnotation() throws NoSuchMethodException {
        Method method = OrderService.class.getDeclaredMethod("getOrders", Long.class, Pageable.class);
        Transactional annotation = method.getAnnotation(Transactional.class);
        assertThat(annotation).isNotNull();
        assertThat(annotation.readOnly()).isTrue();
    }
}