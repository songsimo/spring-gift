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
    private final NotificationPort notificationPort;

    public OrderService(
        OrderRepository orderRepository,
        OptionRepository optionRepository,
        MemberRepository memberRepository,
        NotificationPort notificationPort
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.notificationPort = notificationPort;
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
        notificationPort.notify(member, order, option.getProduct());
        return OrderResponse.from(order);
    }
}
