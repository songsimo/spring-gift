package gift.category;

import gift.exception.ConflictException;
import gift.exception.NotFoundException;
import gift.product.ProductRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Nested
    class 카테고리_목록_조회 {

        @Test
        void 카테고리_목록을_조회한다() {
            given(categoryRepository.findAll())
                .willReturn(List.of(new Category("식품", "#fff", "img.png", "desc")));

            List<CategoryResponse> result = categoryService.getAll();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).name()).isEqualTo("식품");
        }
    }

    @Nested
    class 카테고리_등록 {

        @Test
        void 카테고리가_등록된다() {
            var request = new CategoryRequest("식품", "#fff", "img.png", "desc");
            given(categoryRepository.save(any())).willReturn(new Category("식품", "#fff", "img.png", "desc"));

            CategoryResponse result = categoryService.create(request);

            assertThat(result.name()).isEqualTo("식품");
        }
    }

    @Nested
    class 카테고리_수정 {

        @Test
        void 카테고리_정보가_수정된다() {
            var request = new CategoryRequest("신선식품", "#000", "new.png", "updated");
            given(categoryRepository.findById(1L))
                .willReturn(Optional.of(new Category("식품", "#fff", "img.png", "desc")));

            CategoryResponse result = categoryService.update(1L, request);

            assertThat(result.name()).isEqualTo("신선식품");
        }

        @Test
        void 존재하지_않는_카테고리는_수정할_수_없다() {
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.update(99L, new CategoryRequest("x", "#x", "x", "x")))
                .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class 카테고리_삭제 {

        @Test
        void 카테고리가_삭제된다() {
            given(productRepository.existsByCategoryId(1L)).willReturn(false);

            categoryService.delete(1L);

            org.mockito.Mockito.verify(categoryRepository).deleteById(1L);
        }

        @Test
        void 상품이_있는_카테고리는_삭제할_수_없다() {
            given(productRepository.existsByCategoryId(1L)).willReturn(true);

            assertThatThrownBy(() -> categoryService.delete(1L))
                .isInstanceOf(ConflictException.class);
        }
    }
}
