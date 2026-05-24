package gift.member;

import gift.member.model.Member;
import gift.member.service.MemberService;
import gift.member.web.AdminMemberController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(AdminMemberController.class)
class AdminMemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MemberService memberService;

    @Test
    @DisplayName("회원 목록 페이지를 반환한다")
    void list_returnsMemberListView() throws Exception {
        given(memberService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/admin/members"))
            .andExpect(status().isOk())
            .andExpect(view().name("member/list"))
            .andExpect(model().attributeExists("members"));
    }

    @Test
    @DisplayName("회원 추가 폼을 반환한다")
    void newForm_returnsNewFormView() throws Exception {
        mockMvc.perform(get("/admin/members/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("member/new"));
    }

    @Test
    @DisplayName("유효한 이메일로 회원 생성 시 목록 페이지로 리다이렉트한다")
    void create_validEmail_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/members")
                .param("email", "new@test.com")
                .param("password", "password"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/members"));
    }

    @Test
    @DisplayName("중복 이메일로 회원 생성 시 에러와 함께 new 폼을 반환한다")
    void create_duplicateEmail_returnsNewFormWithError() throws Exception {
        willThrow(new IllegalArgumentException("Email is already registered."))
            .given(memberService).adminCreate("dup@test.com", "password");

        mockMvc.perform(post("/admin/members")
                .param("email", "dup@test.com")
                .param("password", "password"))
            .andExpect(status().isOk())
            .andExpect(view().name("member/new"))
            .andExpect(model().attributeExists("error"))
            .andExpect(model().attributeExists("email"));
    }

    @Test
    @DisplayName("회원 수정 폼을 반환한다")
    void editForm_returnsMemberEditView() throws Exception {
        given(memberService.findById(1L)).willReturn(new Member(1L, "test@test.com", "password"));

        mockMvc.perform(get("/admin/members/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("member/edit"))
            .andExpect(model().attributeExists("member"));
    }

    @Test
    @DisplayName("회원 정보 수정 시 목록 페이지로 리다이렉트한다")
    void update_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/members/1/edit")
                .param("email", "updated@test.com")
                .param("password", "newpassword"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/members"));
    }

    @Test
    @DisplayName("포인트 충전 시 목록 페이지로 리다이렉트한다")
    void chargePoint_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/members/1/charge-point")
                .param("amount", "5000"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/members"));
    }

    @Test
    @DisplayName("회원 삭제 시 목록 페이지로 리다이렉트한다")
    void delete_redirectsToList() throws Exception {
        mockMvc.perform(post("/admin/members/1/delete"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/members"));
    }
}
