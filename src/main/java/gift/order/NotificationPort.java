package gift.order;

import gift.member.Member;
import gift.product.Product;

public interface NotificationPort {
    void notify(Member member, Order order, Product product);
}
