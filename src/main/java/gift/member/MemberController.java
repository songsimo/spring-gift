package gift.member;

import gift.auth.AuthenticationResolver;
import gift.auth.TokenResponse;
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
    private final AuthenticationResolver authenticationResolver;

    public MemberController(MemberService memberService, AuthenticationResolver authenticationResolver) {
        this.memberService = memberService;
        this.authenticationResolver = authenticationResolver;
    }

    @GetMapping("/me")
    public ResponseEntity<MemberResponse> getMyInfo(
        @RequestHeader("Authorization") String authorization
    ) {
        var member = authenticationResolver.extractMember(authorization);
        if (member == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(memberService.getMyInfo(member.getEmail()));
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.register(request.email(), request.password()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody MemberRequest request) {
        return ResponseEntity.ok(memberService.login(request.email(), request.password()));
    }
}
