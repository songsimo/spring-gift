package gift.member;

public record MemberResponse(String email, int point) {
    public static MemberResponse from(Member member) {
        return new MemberResponse(member.getEmail(), member.getPoint());
    }
}
