package gift.member;

import gift.exception.DuplicateException;
import gift.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {
    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member register(MemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new DuplicateException("Email is already registered.");
        }
        return memberRepository.save(new Member(request.email(), request.password()));
    }

    @Transactional(readOnly = true)
    public Member login(MemberRequest request) {
        Member member = memberRepository.findByEmail(request.email())
            .orElseThrow(() -> new NotFoundException("Invalid email or password."));
        if (!request.password().equals(member.getPassword())) {
            throw new NotFoundException("Invalid email or password.");
        }
        return member;
    }
}
