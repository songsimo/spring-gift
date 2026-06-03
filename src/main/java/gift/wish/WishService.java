package gift.wish;

import gift.exception.ForbiddenException;
import gift.exception.NotFoundException;
import gift.product.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WishService {
    private final WishRepository wishRepository;
    private final ProductRepository productRepository;

    public WishService(WishRepository wishRepository, ProductRepository productRepository) {
        this.wishRepository = wishRepository;
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<WishResponse> getWishes(Long memberId, Pageable pageable) {
        return wishRepository.findByMemberId(memberId, pageable).map(WishResponse::from);
    }

    @Transactional
    public WishResponse addWish(Long memberId, Long productId) {
        var product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("Product not found."));
        return wishRepository.findByMemberIdAndProductId(memberId, product.getId())
            .map(WishResponse::from)
            .orElseGet(() -> WishResponse.from(wishRepository.save(new Wish(memberId, product))));
    }

    @Transactional
    public void removeWish(Long memberId, Long wishId) {
        var wish = wishRepository.findById(wishId)
            .orElseThrow(() -> new NotFoundException("Wish not found."));
        if (!wish.getMemberId().equals(memberId)) {
            throw new ForbiddenException("Access denied.");
        }
        wishRepository.delete(wish);
    }
}
