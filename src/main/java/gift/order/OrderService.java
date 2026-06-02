package gift.order;

import gift.exception.BadRequestException;
import gift.exception.NotFoundException;
import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.OptionRepository;
import gift.product.Product;
import gift.wish.WishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final MemberRepository memberRepository;
    private final WishRepository wishRepository;
    private final NotificationPort notificationPort;

    public OrderService(
        OrderRepository orderRepository,
        OptionRepository optionRepository,
        MemberRepository memberRepository,
        WishRepository wishRepository,
        NotificationPort notificationPort
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.wishRepository = wishRepository;
        this.notificationPort = notificationPort;
    }

    public Page<OrderResponse> getOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable).map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse create(Member member, OrderRequest request) {
        var option = optionRepository.findByIdForUpdate(request.optionId())
            .orElseThrow(() -> new NotFoundException("Option not found."));
        option.subtractQuantity(request.quantity());

        var price = option.getProduct().getPrice() * request.quantity();
        if (memberRepository.deductPointAtomic(member.getId(), price) == 0) {
            throw new BadRequestException("포인트가 부족합니다.");
        }

        var order = orderRepository.save(new Order(option, member.getId(), request.quantity(), request.message()));
        wishRepository.findByMemberIdAndProductId(member.getId(), option.getProduct().getId())
            .ifPresent(wishRepository::delete);
        return OrderResponse.from(order);
    }

    public boolean notifyOrder(Member member, Long orderId) {
        var order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found."));
        Product product = order.getOption().getProduct();
        return notificationPort.notify(member, order, product);
    }
}
