package gift.product.model;

public class ProductNameValidator {

    private ProductNameValidator() {
    }

    public static boolean containsKakao(String name) {
        return name != null && name.contains("카카오");
    }
}
