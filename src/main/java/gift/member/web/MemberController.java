package gift.member.web;

import gift.auth.AuthService;
import gift.auth.AuthenticationResolver;
import gift.auth.TokenResponse;
import gift.member.service.MemberRequest;
import gift.member.service.MemberResponse;
import gift.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;
    private final AuthService authService;
    private final AuthenticationResolver authenticationResolver;

    public MemberController(MemberService memberService, AuthService authService, AuthenticationResolver authenticationResolver) {
        this.memberService = memberService;
        this.authService = authService;
        this.authenticationResolver = authenticationResolver;
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(
        @RequestHeader("Authorization") String authorization
    ) {
        var member = authenticationResolver.extractMember(authorization);
        return ResponseEntity.ok(memberService.getMyInfo(member.getEmail()));
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request.email(), request.password()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(authService.login(request.email(), request.password()));
    }
}
