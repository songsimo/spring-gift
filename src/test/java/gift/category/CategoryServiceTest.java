package gift.category;

import org.junit.jupiter.api.DisplayName;
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
    CategoryRepository categoryRepository;

    @InjectMocks
    CategoryService categoryService;

    @Test
    @DisplayName("카테고리 전체 조회 시 모든 카테고리를 반환한다")
    void getAll_returnsAllCategories() {
        given(categoryRepository.findAll()).willReturn(List.of(
            new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품"),
            new Category(2L, "패션", "#FF6347", "https://example.com/img2.png", null)
        ));

        List<CategoryResponse> result = categoryService.getAll();

        assertThat(result).hasSize(2)
                .extracting("name")
                .containsExactly("전자기기", "패션");
    }

    @Test
    @DisplayName("카테고리 생성 시 저장된 카테고리를 반환한다")
    void create_returnsSavedCategory() {
        CategoryRequest request = new CategoryRequest("식품", "#32CD32", "https://example.com/img.png", "먹거리");
        given(categoryRepository.save(any())).willReturn(
            new Category(1L, "식품", "#32CD32", "https://example.com/img.png", "먹거리")
        );

        CategoryResponse result = categoryService.create(request);

        assertThat(result.name()).isEqualTo("식품");
        assertThat(result.color()).isEqualTo("#32CD32");
    }

    @Test
    @DisplayName("존재하는 카테고리 수정 시 변경된 카테고리를 반환한다")
    void update_existingCategory_returnsUpdated() {
        Category existing = new Category(1L, "전자기기", "#1E90FF", "https://example.com/img.png", "전자제품");
        CategoryRequest request = new CategoryRequest("가전", "#000000", "https://example.com/new.png", "가전제품");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));
        given(categoryRepository.save(any())).willReturn(existing);

        CategoryResponse result = categoryService.update(1L, request);

        assertThat(result.name()).isEqualTo("가전");
        assertThat(result.color()).isEqualTo("#000000");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 시 예외가 발생한다")
    void update_nonExistingCategory_throwsException() {
        CategoryRequest request = new CategoryRequest("가전", "#000000", "https://example.com/new.png", null);
        given(categoryRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(999L, request))
            .isInstanceOf(IllegalArgumentException.class);
    }
}