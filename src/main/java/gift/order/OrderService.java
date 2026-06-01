package gift.order;

import gift.exception.NotFoundException;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.OptionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final MemberRepository memberRepository;
    private final KakaoMessageClient kakaoMessageClient;

    public OrderService(
        OrderRepository orderRepository,
        OptionRepository optionRepository,
        MemberRepository memberRepository,
        KakaoMessageClient kakaoMessageClient
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.kakaoMessageClient = kakaoMessageClient;
    }

    public Page<OrderResponse> getOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable).map(OrderResponse::from);
    }

    public OrderResponse create(Member member, OrderRequest request) {
        var option = optionRepository.findById(request.optionId())
            .orElseThrow(() -> new NotFoundException("Option not found."));
        option.subtractQuantity(request.quantity());
        optionRepository.save(option);

        var price = option.getProduct().getPrice() * request.quantity();
        member.deductPoint(price);
        memberRepository.save(member);

        var order = orderRepository.save(new Order(option, member.getId(), request.quantity(), request.message()));
        sendKakaoMessageIfPossible(member, order, option);
        return OrderResponse.from(order);
    }

    private void sendKakaoMessageIfPossible(Member member, Order order, gift.option.Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, option.getProduct());
        } catch (Exception ignored) {
        }
    }
}
