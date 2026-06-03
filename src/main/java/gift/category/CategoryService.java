package gift.category;

import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.product.ProductRepository;
import org.springframework.stereotype.Service;

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

    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Category not found."));
        category.update(request.name(), request.color(), request.imageUrl(), request.description());
        return CategoryResponse.from(category);
    }

    public void delete(Long id) {
        if (productRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category has associated products.");
        }
        categoryRepository.deleteById(id);
    }
}
