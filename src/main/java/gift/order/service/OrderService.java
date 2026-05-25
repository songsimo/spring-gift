package gift.order.service;

import gift.exception.NotFoundException;
import gift.member.model.Member;
import gift.member.repository.MemberRepository;
import gift.option.model.Option;
import gift.option.repository.OptionRepository;
import gift.order.model.Order;
import gift.order.repository.OrderRepository;
import gift.wish.repository.WishRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
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

    public boolean existsByOptionId(Long optionId) {
        return orderRepository.existsByOptionId(optionId);
    }

    public boolean existsByProductId(Long productId) {
        return orderRepository.existsByOptionProductId(productId);
    }

    @Transactional
    public OrderResponse createOrder(Long memberId, OrderRequest request) {
        Option option = optionRepository.findById(request.optionId())
            .orElseThrow(() -> new NotFoundException("옵션을 찾을 수 없습니다. id=" + request.optionId()));
        option.subtractQuantity(request.quantity());
        optionRepository.save(option);

        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new NotFoundException("회원을 찾을 수 없습니다. id=" + memberId));
        int price = option.getProduct().getPrice() * request.quantity();
        member.deductPoint(price);
        memberRepository.save(member);

        Order saved = orderRepository.save(new Order(option, memberId, request.quantity(), request.message()));
        wishRepository.findByMemberIdAndProductId(memberId, option.getProduct().getId())
            .ifPresent(wishRepository::delete);
        boolean notificationSent = sendKakaoMessageIfPossible(member, saved, option);
        return OrderResponse.of(saved, notificationSent);
    }

    private boolean sendKakaoMessageIfPossible(Member member, Order order, Option option) {
        if (member.getKakaoAccessToken() == null) {
            return false;
        }
        try {
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, option.getProduct());
            return true;
        } catch (Exception e) {
            log.warn("카카오 알림 전송 실패 (memberId={}): {}", order.getMemberId(), e.getMessage());
            return false;
        }
    }
}