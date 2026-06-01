package gift.order;

import gift.member.Member;
import gift.product.Product;

public interface NotificationPort {
    boolean notify(Member member, Order order, Product product);
}
