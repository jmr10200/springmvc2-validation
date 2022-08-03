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

    // ControllerV4 : From 전송 객체 분리 -> 도메인 Item 에서는 검증 사용하지 않으므로 검증코드 제거

//    @NotNull(groups = UpdateCheck.class) // 수정 요구사항 추가
    private Long id;

//    @NotBlank(groups = {SaveCheck.class, UpdateCheck.class}) // 공백 x
    // 다음과 같이 메시지 정의도 가능하다. (프로퍼티 파일이 더 우선된다.)
//    @NotBlank(message = "공백 입력 불가능!!")
    private String itemName;

//    @NotNull(groups = {SaveCheck.class, UpdateCheck.class}) // null x
//    @Range(min = 1000,max = 1000000, groups = {SaveCheck.class, UpdateCheck.class})
    private Integer price;

//    @NotNull(groups = {SaveCheck.class, UpdateCheck.class})
//    @Max(value = 9999, groups = SaveCheck.class) // 등록시에만 사용
    private Integer quantity;

    public Item() {
    }

    public Item(String itemName, Integer price, Integer quantity) {
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
    }

    /* 참고 */
    // Bean Validation 을 사용하려면 spring-boot-starter-validation 의존관계를 추가해야한다.

    // Jakarta Bean Validation
    // jakarta.validation-api : Bean Validation 인터페이스
    // hibernate-validator : 구현체

    // 자바 표준 : javax.validation.constraints.NotNull
    // 하이버네이트 validator 구현체 사용 : org.hibernate.validator.constraints.Range

    // BeanValidation : 어노테이션 추가
    /* BeanValidation 의 한계 */
    // 현실에서는 데이터를 등록할 때와 수정할 때의 요구사항이 다른경우가 많다.
    // 1. 등록시 요구사항
    // 타입검증 : 가격, 수량에 문자가 입력되면 에러
    // 필드검증 : 상품명은 필수,공백x, 가격은 1,000원 이상 1백만원 이하, 수량은 최대 9999
    // 2. 수정시 요구사항
    // 수정시에는 수량을 무제한으로 변경할 수 있다.
    // 등록시에는 id 값이 없어도 되지만 수정시에는 id 값이 필수이다.
    // -> 이때, id 에 @NotNull, quantity 에 @Max(9999) 제거하면? 수정 OK 등록 NG!
    // 등록시 'id' : rejected value [null]; 이 발생

    /* 해결방법 2가지 */
    // ・BeanValidation 의 groups 기능 사용 -> 등록용, 수정용 groups 생성
    // ・Item 을 직접 사용하지 않고, ItemSaveForm, ItemUpdateForm 과 같이 Form 전송을 위한 별도의 모델 객체 생성하여 사용

    /* 참고 */
    // 현재 예제에서는 수정시 item 의 id 값은 항상 들어있도록 로직이 구성되어있다.
    // 그래서 검증이 불필요하다고 생각될 수 있으나, HTTP 요청은 언제든지 악의적으로 변경할 여지가 있다.
    // HTTP 요청을 변경해서 item 의 id 값을 삭제하라고 요청하는 등의 위험이 있다.
    // 따라서 서버에서 항상 검증해줘야 한다.

}
