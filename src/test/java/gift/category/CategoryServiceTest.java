package gift.category;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

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
}
