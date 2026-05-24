package gift.category.service;

import gift.category.model.Category;
import gift.category.repository.CategoryRepository;
import gift.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
            .map(CategoryResponse::from)
            .toList();
    }

    public CategoryResponse create(CategoryRequest request) {
        Category saved = categoryRepository.save(request.toEntity());
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        category.update(request.name(), request.color(), request.imageUrl(), request.description());
        categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    public void delete(Long id) {
        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalArgumentException("카테고리에 속한 상품이 있어 삭제할 수 없습니다.");
        }
        categoryRepository.deleteById(id);
    }
}