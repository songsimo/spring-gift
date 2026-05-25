package gift.wish.service;

import gift.exception.NotFoundException;
import gift.product.model.Product;
import gift.product.repository.ProductRepository;
import gift.wish.model.Wish;
import gift.wish.repository.WishRepository;
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

    public Page<WishResponse> getWishes(Long memberId, Pageable pageable) {
        return wishRepository.findByMemberId(memberId, pageable).map(WishResponse::from);
    }

    public WishResponse addWish(Long memberId, Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다. id=" + productId));
        return wishRepository.findByMemberIdAndProductId(memberId, productId)
            .map(WishResponse::from)
            .orElseGet(() -> WishResponse.from(wishRepository.save(new Wish(memberId, product))));
    }

    @Transactional
    public void deleteByProductId(Long productId) {
        wishRepository.deleteByProductId(productId);
    }

    @Transactional
    public void removeByMemberAndProduct(Long memberId, Long productId) {
        wishRepository.findByMemberIdAndProductId(memberId, productId)
            .ifPresent(wishRepository::delete);
    }

    public void removeWish(Long memberId, Long wishId) {
        Wish wish = wishRepository.findById(wishId)
            .orElseThrow(() -> new NotFoundException("찜을 찾을 수 없습니다. id=" + wishId));
        if (!wish.getMemberId().equals(memberId)) {
            throw new IllegalArgumentException("본인의 찜 목록만 삭제할 수 있습니다.");
        }
        wishRepository.delete(wish);
    }
}