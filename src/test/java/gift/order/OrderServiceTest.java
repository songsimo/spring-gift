package gift.order;

import gift.member.MemberRepository;
import gift.option.OptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OptionRepository optionRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KakaoMessageClient kakaoMessageClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void getOrders_주문_목록을_반환한다() {
        given(orderRepository.findByMemberId(any(), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of()));

        var result = orderService.getOrders(1L, Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }
}
