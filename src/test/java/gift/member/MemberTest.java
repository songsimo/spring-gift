package gift.member;

import gift.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void chargePoint_금액이_0이하면_BadRequestException을_던진다() {
        var member = new Member("test@test.com", "pass");

        assertThatThrownBy(() -> member.chargePoint(0))
            .isInstanceOf(BadRequestException.class);
    }
}
