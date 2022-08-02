package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }

    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        // 특정 필드 에러(FieldError) 는 @Validated 를 이용, BeanValidation
        // 글로벌 에러 확인
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // validation check : 검증 실패시 다시 입력 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v3/addForm";
        }

        // 검증 통과했을때 실행되는 성공로직
        // 상품등록
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }

    // 스프링 MVC 는 어떻게 Bean Validator 를 사용할까?
    // 스프링부트가 spring-boot-start-validation 라이브러리를 넣으면 자동으로 Bean Validator 를 스프링에 통합한다.

    // 스프링 부트는 자동으로 글로벌 Validator 로 등록한다.
    // LocalValidatorFactoryBean 을 글로벌 Validator 로 등록한다.
    // 이 Validator 는 @NotNull 같은 어노테이션을 보고 검증을 수행한다.
    // 글로벌 Validator 가 적용되어 있기 때문에, @Valid, @Validated 만 적용하면 된다.
    // 검증 오류가 발생하면, FieldError, ObjectError 를 생성해서 BindingResult 에 담아준다.

    // 주의!!
    // @SpringBootApplication 을 붙이는 메인 클래스에 WebMvcConfigurer 을 구현하여
    // getValidator() 을 Override 해서 직접 검증기를 등록하면 스프링 부트는 글로벌 Validator 로 등록하지 않는다.
    // 즉, 어노테이션 기반의 Bean Validator 가 동작하지 않는다.

    /* 참고 : @Valid 와 @Validated */
    // @Valid : 자바표준, javax.validation.@Valid 를 사용하려면 build.gradle 의존관계 추가가 필요하다.
    // implementation 'org.springframework.boot:spring-boot-starter-validation'
    // @Validated : 는 스프링 전용 검증 어노테이션이다.
    // 둘다 동작하지만 @Validated 는 내부에 groups 라는 기능을 포함하고 있다.

    // 검증 순서
    // 1. @ModelAttribute 각각의 필드에 타입 변환을 시도, 실패시 typeMisMatch 로 FieldError 추가
    // 2. Validator 적용

    // 바인딩에 성공한 필드만 Bean Validator 적용
    // Bean Validator 는 바인딩에 실패하면 BeanValidation 을 적용하지 않는다.
    // 타입변환에 성공해서 바인딩에 성공해야 BeanValidation 의 효과가 있으므로.
    // 즉, @ModelAttribute -> 각 필드 타입 변환 시도 -> 변환 성공 필드만 BeanValidation 적용

    /* Bean Validation 에러코드 */
    // Bean Validation 이 기본으로 제공하는 에러 메시지의 변경
    // Bean Validation 을 적용하고 BindingResult 에 등록된 검증 에러 코드를 보면 어노테이션 명으로 등록된다.
    // @NotBlank 의 경우
    // NotBlank.item.itemName -> NotBlank.itemName -> NotBlank.java.lang.String -> NotBlank
    // @Range 의 경우
    // Range.item.price -> Range.price -> Range.java.lang.Integer -> Range

    // BeanValidation 의 메시지 찾는 순서
    // 1. 생성된 메시지 코드 순서대로 messageSource 에서 메시지 찾기
    // 2. 어노테이션의 message 속성 사용 -> @NotBlank(message = "공백! {0}")
    // 3. 라이브러리가 제공하는 기본 값 사용 -> 공백일 수 없습니다.

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item, BindingResult bindingResult) {

        // 특정 필드가 아닌 전체 에러
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // validation check : 검증 실패시 다시 수정 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v3/editForm";
        }

        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

