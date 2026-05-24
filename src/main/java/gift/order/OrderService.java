package gift.order;

import gift.member.Member;
import gift.member.MemberRepository;
import gift.option.model.Option;
import gift.option.repository.OptionRepository;
import gift.wish.repository.WishRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OptionRepository optionRepository;
    private final MemberRepository memberRepository;
    private final KakaoMessageClient kakaoMessageClient;
    private final WishRepository wishRepository;

    public OrderService(
        OrderRepository orderRepository,
        OptionRepository optionRepository,
        MemberRepository memberRepository,
        KakaoMessageClient kakaoMessageClient,
        WishRepository wishRepository
    ) {
        this.orderRepository = orderRepository;
        this.optionRepository = optionRepository;
        this.memberRepository = memberRepository;
        this.kakaoMessageClient = kakaoMessageClient;
        this.wishRepository = wishRepository;
    }

    public Page<OrderResponse> getOrders(Long memberId, Pageable pageable) {
        return orderRepository.findByMemberId(memberId, pageable).map(OrderResponse::from);
    }

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderRequest request) {
        Option option = optionRepository.findById(request.optionId())
            .orElseThrow(() -> new IllegalArgumentException("Option not found: " + request.optionId()));
        option.subtractQuantity(request.quantity());
        optionRepository.save(option);

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        int price = option.getProduct().getPrice() * request.quantity();
        member.deductPoint(price);
        memberRepository.save(member);

        Order saved = orderRepository.save(new Order(option, memberId, request.quantity(), request.message()));
        wishRepository.findByMemberIdAndProductId(memberId, option.getProduct().getId())
            .ifPresent(wishRepository::delete);
        sendKakaoMessageIfPossible(member, saved, option);
        return OrderResponse.from(saved);
    }

    private void sendKakaoMessageIfPossible(Member member, Order order, Option option) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, option.getProduct());
        } catch (Exception ignored) {
        }
    }
}