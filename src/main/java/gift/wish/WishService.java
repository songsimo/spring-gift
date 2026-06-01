package gift.wish;

import gift.exception.NotFoundException;
import gift.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class WishService {
    private final WishRepository wishRepository;
    private final ProductRepository productRepository;

    public WishService(WishRepository wishRepository, ProductRepository productRepository) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
    }

    public Page<WishResponse> getWishes(Long memberId, Pageable pageable) {
        return wishRepository.findByMemberId(memberId, pageable).map(WishResponse::from);
    }

    public WishResponse addWish(Long memberId, Long productId) {
        var product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found."));
        return wishRepository.findByMemberIdAndProductId(memberId, product.getId())
            .map(WishResponse::from)
            .orElseGet(() -> WishResponse.from(wishRepository.save(new Wish(memberId, product))));
    }
}
