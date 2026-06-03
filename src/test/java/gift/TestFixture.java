package gift;

import gift.category.Category;
import gift.member.Member;
import gift.product.Product;

public class TestFixture {

    public static Category sampleCategory() {
        return new Category("식품", "#fff", "img.png", "desc");
    }

    public static Product sampleProduct() {
        return new Product("사과", 1000, "apple.png", sampleCategory());
    }

    public static Member sampleMember() {
        return new Member("test@test.com", "pass");
    }
}
