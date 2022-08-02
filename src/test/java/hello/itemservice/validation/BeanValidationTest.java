package hello.itemservice.validation;

import hello.itemservice.domain.item.Item;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

public class BeanValidationTest {

    /* BeanValidator */
    // Validation check 를 매번 코드로 작성하기 번거롭다. 검증 로직을 모든 프로젝트에 적용할 수 있게 공통화하고 표준화 한 것이다.
    // 먼저 Bean Validator 은 특정한 구현체가 아니라 Bean Validation 2.0(JSR-380) 이라는 기술 표준이다.
    // 즉, 검증 어노테이션과 여러 인터페이스의 모음이다. 마치 JPA 가 기술 표준이고 그 구현체로 Hibernate 가 있는 것과 같다.
    // Bean Validation 을 구현한 기술중에 일반적으로 사용하는 구현체는 Hibernate Validator 이다.
    // 참고로 이름에 Hibernate 가 붙어 있지만 ORM 과는 관련이 없다.
    // 의존관계 추가 : implementation 'org.springframework.boot:spring-boot-starter-validation'

    @Test
    void beanValidation() {
        // 검증기 생성 : 스프링과 통합하면 사용할 일은 없다
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Item item = new Item();
        item.setItemName(" "); // 공백
        item.setPrice(0);
        item.setQuantity(10000);
        // 검증 실행 : item 을 직접 검증기에 넣어서 결과를 받는다.
        // Set 에는 ConstraintViolation 이라는 검증 에러가 담긴다. 결과가 없으면 에러가 없는 것이다.
        Set<ConstraintViolation<Item>> violations = validator.validate(item);
        for (ConstraintViolation<Item> violation : violations) {
            System.out.println("violation=" + violation);
            System.out.println("violation.message=" + violation.getMessage());
        }
        // 실행결과 : 검증 오류가 발생한 객체, 필드, 메시지 정보 등 확인 가능
        // violation=ConstraintViolationImpl{interpolatedMessage='1000에서 1000000 사이여야 합니다', propertyPath=price, rootBeanClass=class hello.itemservice.domain.item.Item, messageTemplate='{org.hibernate.validator.constraints.Range.message}'}
        // violation.message=1000에서 1000000 사이여야 합니다
        // violation=ConstraintViolationImpl{interpolatedMessage='9999 이하여야 합니다', propertyPath=quantity, rootBeanClass=class hello.itemservice.domain.item.Item, messageTemplate='{javax.validation.constraints.Max.message}'}
        // violation.message=9999 이하여야 합니다
        // violation=ConstraintViolationImpl{interpolatedMessage='공백일 수 없습니다', propertyPath=itemName, rootBeanClass=class hello.itemservice.domain.item.Item, messageTemplate='{javax.validation.constraints.NotBlank.message}'}
        // violation.message=공백일 수 없습니다
    }

}
