package hello.itemservice.domain.item;

import lombok.Data;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.ScriptAssert;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

// Bean Validation 에서 특정 필드(FieldError) 가 아닌 해당 객체 관련 오류(ObjectError)의 처리
// 메시지 코드 생성 : ScriptAssert.item -> ScriptAssert
// 그런데 이 방법은 제약이 많고 복잡해서 실무적으로 권장되지는 않는다.
// 글로벌 에러는 직접 자바코드를 작성하는 것이 낫다.
//@ScriptAssert(lang = "javascript", script = "_this.price * _this.quantity >= 10000")
@Data
public class Item {

    // BeanValidation : 어노테이션 추가

    private Long id;

    @NotBlank // 공백 x
    // 다음과 같이 메시지 정의도 가능하다. (프로퍼티 파일이 더 우선된다.)
//    @NotBlank(message = "공백 입력 불가능!!")
    private String itemName;

    @NotNull // null x
    @Range(min = 1000,max = 1000000)
    private Integer price;

    @NotNull
    @Max(9999)
    private Integer quantity;

    /* 참고 */
    // Bean Validation 을 사용하려면 spring-boot-starter-validation 의존관계를 추가해야한다.

    // Jakarta Bean Validation
    // jakarta.validation-api : Bean Validation 인터페이스
    // hibernate-validator : 구현체

    // 자바 표준 : javax.validation.constraints.NotNull
    // 하이버네이트 validator 구현체 사용 : org.hibernate.validator.constraints.Range

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }
}
