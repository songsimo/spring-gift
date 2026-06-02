package gift.order.infrastructure;

import gift.member.Member;
import gift.order.NotificationPort;
import gift.order.Order;
import gift.product.Product;
import org.springframework.stereotype.Component;

@Component
public class KakaoNotificationAdapter implements NotificationPort {
    private final KakaoMessageClient kakaoMessageClient;

    public KakaoNotificationAdapter(KakaoMessageClient kakaoMessageClient) {
        this.kakaoMessageClient = kakaoMessageClient;
    }

    @Override
    public boolean notify(Member member, Order order, Product product) {
        if (member.getKakaoAccessToken() == null) {
            return false;
        }
        try {
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, product);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
