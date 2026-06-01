package gift.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.product.ProductRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void getAll_카테고리_목록을_반환한다() {
        given(categoryRepository.findAll())
            .willReturn(List.of(new Category("식품", "#fff", "img.png", "desc")));

        List<CategoryResponse> result = categoryService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("식품");
    }

    @Test
    void create_카테고리를_저장하고_반환한다() {
        var request = new CategoryRequest("식품", "#fff", "img.png", "desc");
        given(categoryRepository.save(any())).willReturn(new Category("식품", "#fff", "img.png", "desc"));

        CategoryResponse result = categoryService.create(request);

        assertThat(result.name()).isEqualTo("식품");
    }

    @Test
    void update_존재하는_카테고리를_수정한다() {
        var request = new CategoryRequest("신선식품", "#000", "new.png", "updated");
        given(categoryRepository.findById(1L))
            .willReturn(Optional.of(new Category("식품", "#fff", "img.png", "desc")));

        CategoryResponse result = categoryService.update(1L, request);

        assertThat(result.name()).isEqualTo("신선식품");
    }

    @Test
    void update_존재하지_않는_카테고리는_NotFoundException을_던진다() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(99L, new CategoryRequest("x", "#x", "x", "x")))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_카테고리를_삭제한다() {
        given(productRepository.existsByCategoryId(1L)).willReturn(false);

        categoryService.delete(1L);

        org.mockito.Mockito.verify(categoryRepository).deleteById(1L);
    }

    @Test
    void delete_상품이_있는_카테고리는_ConflictException을_던진다() {
        given(productRepository.existsByCategoryId(1L)).willReturn(true);

        assertThatThrownBy(() -> categoryService.delete(1L))
            .isInstanceOf(ConflictException.class);
    }
}
