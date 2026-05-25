package gift.category.service;

import gift.category.model.Category;
import gift.category.repository.CategoryRepository;
import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.product.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductService productService;

    public CategoryService(CategoryRepository categoryRepository, ProductService productService) {
        this.categoryRepository = categoryRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll() {
        return categoryRepository.findAll().stream()
            .map(CategoryResponse::from)
            .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        Category saved = categoryRepository.save(request.toEntity());
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("카테고리를 찾을 수 없습니다. id=" + id));
        category.update(request.name(), request.color(), request.imageUrl(), request.description());
        categoryRepository.save(category);
        return CategoryResponse.from(category);
    }

    @Transactional
    public void delete(Long id) {
        if (productService.existsByCategoryId(id)) {
            throw new ConflictException("카테고리에 속한 상품이 있어 삭제할 수 없습니다.");
        }
        categoryRepository.deleteById(id);
    }
}