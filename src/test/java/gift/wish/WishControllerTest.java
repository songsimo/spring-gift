package gift.wish;

import gift.auth.AuthenticationResolver;
import gift.exception.UnauthorizedException;
import gift.member.model.Member;
import gift.wish.service.WishRequest;
import gift.wish.service.WishResponse;
import gift.wish.service.WishService;
import gift.wish.web.WishController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishController.class)
class WishControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    WishService wishService;

    @MockitoBean
    AuthenticationResolver authenticationResolver;

    @Test
    @DisplayName("유효한 토큰으로 찜 목록 조회 시 200을 반환한다")
    void getWishes_validToken_returns200() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        WishResponse wish = new WishResponse(1L, 10L, "MacBook", 1000000, "https://example.com/img.png");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);
        given(wishService.getWishes(anyLong(), any(Pageable.class)))
            .willReturn(new PageImpl<>(List.of(wish)));

        mockMvc.perform(get("/api/wishes")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("MacBook"));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 찜 목록 조회 시 401을 반환한다")
    void getWishes_invalidToken_returns401() throws Exception {
        given(authenticationResolver.extractMember("Bearer bad-token")).willThrow(new UnauthorizedException("인증이 필요합니다."));

        mockMvc.perform(get("/api/wishes")
                .header("Authorization", "Bearer bad-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 토큰으로 찜 추가 시 201을 반환한다")
    void addWish_validToken_returns201() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        WishResponse wish = new WishResponse(1L, 10L, "MacBook", 1000000, "https://example.com/img.png");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);
        given(wishService.addWish(1L, 10L)).willReturn(wish);

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 10}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("MacBook"));
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 찜 추가 시 401을 반환한다")
    void addWish_invalidToken_returns401() throws Exception {
        given(authenticationResolver.extractMember("Bearer bad-token")).willThrow(new UnauthorizedException("인증이 필요합니다."));

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer bad-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": 10}"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 토큰으로 찜 삭제 시 204를 반환한다")
    void removeWish_validToken_returns204() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);

        mockMvc.perform(delete("/api/wishes/1")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("유효하지 않은 토큰으로 찜 삭제 시 401을 반환한다")
    void removeWish_invalidToken_returns401() throws Exception {
        given(authenticationResolver.extractMember("Bearer bad-token")).willThrow(new UnauthorizedException("인증이 필요합니다."));

        mockMvc.perform(delete("/api/wishes/1")
                .header("Authorization", "Bearer bad-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("타인의 찜 삭제 시도 시 401을 반환한다")
    void removeWish_notOwner_returns401() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);
        org.mockito.BDDMockito.willThrow(new UnauthorizedException("본인의 찜 목록만 삭제할 수 있습니다."))
            .given(wishService).removeWish(1L, 1L);

        mockMvc.perform(delete("/api/wishes/1")
                .header("Authorization", "Bearer valid-token"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("찜 추가 시 productId가 없으면 400을 반환한다")
    void addWish_missingProductId_returns400() throws Exception {
        Member member = new Member(1L, "test@test.com", "pw");
        given(authenticationResolver.extractMember("Bearer valid-token")).willReturn(member);

        mockMvc.perform(post("/api/wishes")
                .header("Authorization", "Bearer valid-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\": null}"))
            .andExpect(status().isBadRequest());
    }
}
