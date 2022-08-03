package hello.itemservice.validation;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.validation.DefaultMessageCodesResolver;
import org.springframework.validation.MessageCodesResolver;

import static org.assertj.core.api.Assertions.*;

public class MessageCodesResolverTest {

    MessageCodesResolver codesResolver = new DefaultMessageCodesResolver();

    @Test
    void messageCodesResolverObject() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item");
        assertThat(messageCodes).containsExactly("required.item", "required");
    }

    @Test
    void messageCodesResolverField() {
        String[] messageCodes = codesResolver.resolveMessageCodes("required", "item", "itemName", String.class);
        // contains() : 중복여부, 순서에 관계없이 값만 일치하는가
        // containsExactly() : 순서를 포함해서 값이 정확히 일치하는가
        // containsOnly() : 중복여부, 순서에 관계없이 값과 개수가 일치하는가
        assertThat(messageCodes).containsExactly(
                "required.item.itemName",
                "required.itemName",
                "required.java.lang.String",
                "required"
        );
    }
    /* MessageCodesResolver */
    // 검증 오류 코드로 메시지 코드를 생성한다.
    // MessageCodesResolver 는 인터페이스이고 DefaultMessageCodesResolver 는 기본 구현체이다.
    // 주로 ObjectError, FieldError 과 함께 사용된다.

    /* DefaultMessageCodesResolver 의 기본 메시지 생성 규칙 */
    // 객체 오류
    // 다음과 같은 순서로 2가지 생성
    // 1.: code + "." + object name
    // 2.: code
    // 예) 오류 코드 : required, object name : item
    // 1.: required.item
    // 2.: required

    // 필드 오류
    // 다음과 같은 순서로 4가지 생성
    // 1.: code + "." + object name + "." + field
    // 2.: code + "." + field
    // 3.: code + "." + field type
    // 4.: code
    // 예) 오류 코드 : typeMismatch, object name "user", field "age", field type: int
    // "typeMismatch.user.age"
    // "typeMismatch.age"
    // "typeMismatch.int"
    // "typeMismatch"

    // 동작 방식
    // rejectValue() , reject() 는 내부에서 MessageCodesResolver 를 사용하며 여기서 메시지 코드를 생성한다.
    // FieldError, ObjectError 의 생성자를 보면, 오류 코드를 하나가 아니라 여러 오류 코드를 가질 수 있다.
    // MessageCodesResolver 를 통해서 생성된 순서대로 오류 코드를 보관한다.
    // 이 부분을 BindingResult 의 로그를 통해서 확인하면
    // codes [range.item.price, range.price, range.java.lang.Integer, range]

    // FieldError : rejectValue("itemName", "required")
    // 다음 4가지 오류 코드를 자동으로 생성
    // required.item.itemName
    // required.itemName
    // required.java.lang.String
    // required

    // ObjectError : reject("totalPriceMin")
    // 다음 2가지 오류 코드를 자동으로 생성
    // totalPriceMin.item
    // totalPriceMin

    // 에러 메시지 출력
    // 타임리프 화면을 렌더링 할 때 th:errors 가 실행된다.
    // 에러가 있는 경우, 생성된 에러 메시지 코드를 순서대로 돌아가면서 메시지를 찾고 없으면 디폴트 메시지를 출력한다.
}
