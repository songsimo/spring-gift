package gift.order;

import gift.member.Member;
import gift.product.Product;
import org.springframework.stereotype.Component;

@Component
public class KakaoNotificationAdapter implements NotificationPort {
    private final KakaoMessageClient kakaoMessageClient;

    public KakaoNotificationAdapter(KakaoMessageClient kakaoMessageClient) {
        this.kakaoMessageClient = kakaoMessageClient;
    }

    @Override
    public void notify(Member member, Order order, Product product) {
        if (member.getKakaoAccessToken() == null) {
            return;
        }
        try {
            kakaoMessageClient.sendToMe(member.getKakaoAccessToken(), order, product);
        } catch (Exception ignored) {
        }
    }
}
