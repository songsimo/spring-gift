package gift.member;

import gift.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void deductPoint_포인트가_부족하면_BadRequestException을_던진다() {
        var member = new Member("test@test.com", "pass");
        member.chargePoint(1000);

        assertThatThrownBy(() -> member.deductPoint(2000))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void deductPoint_차감_금액이_0이하면_BadRequestException을_던진다() {
        var member = new Member("test@test.com", "pass");
        member.chargePoint(1000);

        assertThatThrownBy(() -> member.deductPoint(0))
            .isInstanceOf(BadRequestException.class);
    }

    @Test
    void chargePoint_금액이_0이하면_BadRequestException을_던진다() {
        var member = new Member("test@test.com", "pass");

        assertThatThrownBy(() -> member.chargePoint(0))
            .isInstanceOf(BadRequestException.class);
    }
}
