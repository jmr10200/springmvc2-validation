package hello.itemservice.domain.item;

import lombok.Data;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class Item {

    // BeanValidation : 어노테이션 추가

    private Long id;

    @NotBlank // 공백 x
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
