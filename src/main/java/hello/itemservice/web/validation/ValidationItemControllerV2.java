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
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder
    public void init(WebDataBinder dataBinder) {
        log.info("init binder {}", dataBinder);
        dataBinder.addValidators(itemValidator);
    }
    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

//    @PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        /* BindingResult */
        // @ModelAttribute 다음에 와야 한다. BindingResult 는 Model 에 자동으로 포함된다.
        // 스프링이 제공하는 검증 오류를 보관하는 객체이다.
        // BindingResult 가 있으면 @ModelAttribute 에 데이터 바인딩시 오류가 발생해도 컨트롤러가 호출된다.

        // (ex) @ModelAttribute 에 바인딩시 타입 오류가 발생한다면??
        // BindingResult 존재 O : 오류정보 (FieldError) 를 BindingResult 에 담아 컨트롤러 정상 호출
        // BindingResult 존재 X : 400 에러 발생, 컨트롤러 호출하지 않고 에러 페이지 이동

        // BindingResult 에 검증 오류를 적용하는 3가지 방법
        // 1. @ModelAttribute 객체에 타입 오류 등으로 바인딩이 실패하는 경우, 스프링이 FieldError 생성하여 BindingResult 에 넣어준다.
        // 2. 개발자가 직접 넣어준다.
        // 3. Validator 를 사용한다.

        /* BindingResult 와 Errors */
        // org.springframework.validation.Errors
        // org.springframework.validation.BindingResult
        // BindingResult 는 인터페이스이고, Errors 인터페이스를 상속받고있다. (public interface BindingResult extends Errors)
        // 실제 넘어오는 구현체는 BeanPropertyBindingResult 라는 것인데, 둘다 구현하고 있으므로 BindingResult 대신 Error 를 사용해도 된다.
        // Errors 인터페이스는 단순한 오류 저장과 조회 기능을 제공한다.
        // BindingResult 는 여기에 더해서 추가적인 기능을 제공한다. addError() 도 BindingResult 가 제공하는 것이다.
        // 관례상 BindingResult 를 많이 사용한다.

        // 정리
        // BindingResult, FieldError, ObjectError 를 사용하여 오류 메시지를 처리하는 방법이다.
        // 그런데, 오류가 발생하는 경우 사용자가 입력한 내용이 모두 사라진다. 이 문제는 어떻게 해결할까? V2로

        // validation check
        // 검증 로직 : 필드 에러는 FieldError 으로 던진다.
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item","itemName", "상품명은 필수입니다."));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", "가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9,999 까지 허용합니다."));
        }
        /* FieldError 생성자 */
        // public FieldError(String objectName, String field, String defaultMessage) {}
        // objectName : @ModelAttribute 이름
        // field : 오류가 발생한 필드 이름
        // defaultMessage : 오류 기본 메시지

        // 특정 필드가 아닌 복합 룰 검증 (상관관계)
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드명을 사용할수 없으므로 ObjectError 를 이용한다.
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
            /* ObjectError 생성자 */
            // public ObjectError(String objectName, String defaultMessage) {}
            // objectName : @ModelAttribute 이름
            // defaultMessage : 오류 기본 메시지
        }

        // validation check : 검증 실패시 다시 입력 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v2/addForm";
        }

        // 검증 통과했을때 실행되는 성공로직
        // 상품등록
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // V2의 목표 : 사용자가 입력한 값이 오류인 경우, 어떤 값을 입력하여 오류가 되었는지 알 수 있도록 입력한 값을 화면에 남기기

        // 스프링의 바인딩 오류 처리
        // 타입 오류로 바인딩에 실패하면 스프링은 FieldError 를 생성하면서 사용자가 입력한 값을 넣어둔다.
        // 그리고 해당 오류를 BindingResult 에 담아서 컨트롤러를 호출한다.
        // 따라서 타입 오류 같은 바인딩 실패시에도 사용자가 입력한 값과 함께 오류 메시지를 정상 출력할 수 있다.

        // validation check
        // 검증 로직 : 필드 에러는 FieldError 으로 던진다.
        if (!StringUtils.hasText(item.getItemName())) {
            bindingResult.addError(new FieldError("item","itemName", item.getItemName(), false, null, null, "상품명은 필수입니다."));
            // FieldError 의 rejectedValue 는 오류 발생시 사용자 입력 값을 저장하는 기능을 제공한다. -> item.getItemName()
            // bindingFailure 는 타입 오류 같은 바인딩이 실패했는지 여부를 적어주면 된다.
            // 여기서는 바인딩이 실패한 것이 아니므로 false 를 지정한다.
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false, null, null,"가격은 1,000 ~ 1,000,000 까지 허용합니다."));
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, null, null,"수량은 최대 9,999 까지 허용합니다."));
        }
        /* FieldError 의 두가지 생성자 */
        // 1. public FieldError(String objectName, String field, String defaultMessage) {}
        // 2. public FieldError(String objectName, String field, @Nullable Object rejectedValue, boolean bindingFailure,
        //			@Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage) {}
        // objectName : 오류가 발생한 객체 이름
        // field : 오류 필드
        // rejectedValue : 사용자가 입력한 값 (거절된 값, 오류가 된 값)
        // codes : 메시지 코드
        // arguments : 메시지에서 사용하는 인자
        // defaultMessage : 기본 오류 메시지


        // 특정 필드가 아닌 복합 룰 검증 (상관관계)
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드명을 사용할수 없으므로 ObjectError 를 이용한다.
                bindingResult.addError(new ObjectError("item", null, null, "가격 * 수량의 합은 10,000원 이상이어야 합니다. 현재 값 = " + resultPrice));
            }
            /* ObjectError 의 두가지 생성자 */
            // 1. public ObjectError(String objectName, String defaultMessage) {}
            // 2. public ObjectError(
            //			String objectName, @Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage) {}
        }

        // 사용자의 입력 데이터가 컨트롤러의 @ModelAttribute 에 바인딩 되는 시점에 오류가 발생하면 모델 객체에 사용자 입력 값을 유지하기 어렵다.
        // 예를들어 가격에 숫자가 아닌 문자가 입력되면 가격은 Integer 타입이므로 문자를 보관할 방법이 없다.
        // 그래서 오류가 발생한 경우 사용자 입력 값을 보관하는 별도의 방법이 필요하다.
        // 그리고 보관한 사용자 입력 값을 검증 오류 발생시 화면에 다시 출력하면 된다.


        // validation check : 검증 실패시 다시 입력 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v2/addForm";
        }

        // 검증 통과했을때 실행되는 성공로직
        // 상품등록
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // V3의 목표 : 오류 메시지의 체계화

        // validation check
        // 검증 로직 : 필드 에러는 FieldError 으로 던진다.
        if (!StringUtils.hasText(item.getItemName())) {
            // MessageSource 를 찾아서 메시지를 조회한다.
            bindingResult.addError(new FieldError("item","itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            // range.item.price=가격은 {0} ~ {1} 까지 허용합니다.
            // codes : required.item.itemName 를 사용해서 메시지 코드를 지정한다. 메시지 코드는 하나가 아니라 배열로 여러 값을 전달할 수 있는데, 순서대로 매칭해서 처음 매칭되는 메시지가 사용된다.
            // arguments : Object[]{1000,1000000} 를 사용해서 코드의 {0}, {1} 로 치환할 값을 전달한다.
            bindingResult.addError(new FieldError("item", "price", item.getPrice(), false,  new String[]{"range.item.price"},  new Object[]{1000, 1000000},null));
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999) {
            bindingResult.addError(new FieldError("item", "quantity", item.getQuantity(), false, new String[]{"max.item.quantity"}, new Object[]{9999},null));
        }

        // 특정 필드가 아닌 복합 룰 검증 (상관관계)
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // 특정 필드명을 사용할수 없으므로 ObjectError 를 이용한다.
                bindingResult.addError(new ObjectError("item",  new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null));
            }
        }
        /* FieldError 의 두가지 생성자 */
        // 1. public FieldError(String objectName, String field, String defaultMessage) {}
        // 2. public FieldError(String objectName, String field, @Nullable Object rejectedValue, boolean bindingFailure,
        //			@Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage) {}

        /* ObjectError 의 두가지 생성자 */
        // 1. public ObjectError(String objectName, String defaultMessage) {}
        // 2. public ObjectError(
        //			String objectName, @Nullable String[] codes, @Nullable Object[] arguments, @Nullable String defaultMessage) {}

        // FieldError , ObjectError 의 생성자는 codes, arguments 를 제공한다.
        // codes : 메시지 코드
        // arguments : 메시지에서 사용하는 인자


        // validation check : 검증 실패시 다시 입력 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v2/addForm";
        }

        // 검증 통과했을때 실행되는 성공로직
        // 상품등록
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // V4의 목표 : 오류 메시지의 체계화2
        // FieldError, ObjectError 는 다루기 번거로운 편이다. 오류코드도 공통화 처리로 자동화 할 수 있도록 해보자.

        // BindingResult 는 검증해야 할 객체인 target 바로 뒤에 온다.
        // 즉, BindingResult 는 검증해야할 객체인 target 을 알고 있다.

        log.info("objectName={}", bindingResult.getObjectName());
        // objectName=item //@ModelAttribute name
        log.info("target={}", bindingResult.getTarget());
        // target=Item(id=null, itemName=상품, price=100, quantity=1234)

        // rejectValue(), reject()
        // BindingResult 가 제공하는 rejectValue(), reject() 를 사용하면
        // FieldError, ObjectError 를 직접 생성하지 않고 검증 오류를 다룰 수 있다.

        // validation check
        /* ValidationUtils 사용 전 */
        if (!StringUtils.hasText(item.getItemName())) {
            // FieldError 대신 rejectValue() 사용
            // BindingResult 는 검증하는 대상 객체 target 을 이미 알고있다.
            // 따라서 target(item) 에 대한 정보는 없어도 된다.
            bindingResult.rejectValue("itemName", "required");
        }
        /* ValidationUtils 사용 후, 다음과 같이 한줄로도 가능하다. */
//        ValidationUtils.rejectIfEmptyOrWhitespace(bindingResult, "itemName", "required");
        // Empty, Whitespace 같은 단순한 기능만 제공함

        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000) {
            // FieldError() 의 경우, 오류코드를 range.item.price 와 같이 전체를 입력했다.
            // rejectValue() 는 오류코드를 range 로 간단하게 입력했다.
            // 그런데도 문제없이 출력이 잘 된다. 왜? 규칙이 있는데, MessageCodesResolver 가 그 역할을 해준다.
            bindingResult.rejectValue("price", "range", new Object[]{1000, 1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() > 10000) {
            bindingResult.rejectValue("quantity", "max", new Object[]{9999}, null);
        }
        // 오류코드
        // 1. 상세한 오류코드
        // required.item.itemName : 상품 이름은 필수 입니다.
        // range.item.price : 상품의 가격 범위 오류 입니다.
        // 2. 단순한 오류코드
        // required : 필수 값 입니다.
        // range : 범위 오류 입니다.
        // 단순하게 만들면 범용적이기 때문에 여러 곳에서 사용할 수 있지만, 상세한 메시지를 전달하기 어렵다.
        // 반대로 하나하나 따로 상세하게 작성하면 너무 범용성이 떨어지고 번거롭다.
        // 즉, 범용성으로 사용하되, 필요에 따라 상세한 메시지가 전달되도록 메시지에 단계를 설정하는 것이 좋다.

        // #Level1
        // required.item.itemName: 상품 이름은 필수 입니다.
        // #Level2
        // required: 필수 값 입니다.
        // 이렇게 객체명과 필드명을 조합한 메시지가 있는지 우선 확인하고, 없으면 더 범용적인 메시지를 선택하도록 개발하는게 좋으며,
        // 범용성있게 잘 개발해두면, 메시지의 추가 만으로도 에러 메시지 관리를 할 수 있게 된다.
        // 스프링은 MessageCodesResolver 으로 이 기능을 제공한다.

        // 특정 필드가 아닌 복합 룰 검증 (상관관계)
        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                // ObjectError 대신 reject() 사용
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }
        // rejectValue()
        // void rejectValue(@Nullable String field, String errorCode, @Nullable Object[] errorArgs, @Nullable String defaultMessage);
        // field : 오류 필드명
        // errorCode : 오류 코드 (메시지에 등록된 코드가 아니다. messageResolver 를 위한 오류 코드)
        // errorArgs : 오류 메시지에서 {0} 을 치환하기 위한 값
        // defaultMessage : 오류 메시지를 찾을 수 없을 때 사용하는 기본 메시지

        // 정리
        // 1. rejectValue() 호출 하고
        // 2. MessageCodesResolver 를 사용하여 검증 에러 코드로 메시지 코드를 생성 (우선순위)
        // 3. new Field() 를 생성하면서 메시지 코드를 보관
        // 4. th:errors 에서 메시지 코드들로 메시지를 순서대로 찾아서 출력함

        /* 스프링이 직접 만든 에러 메시지 처리 */
        // case 1 : 개발자가 직접 설정한 에러 코드 -> rejectValue() 직접 호출
        // case 2 : 스프링이 직접 검증 오류에 추가하는 케이스 (주로 타입정보)
        // price 필드에 문자를 입력하는 경우,
        // 로그를 확인하면 BindingResult 에 FieldError 가 담겨있고, 다음과 같은 메시지 출력을 확인할 수 있다.
        // codes [typeMismatch.item.price,typeMismatch.price,typeMismatch.java.lang.Integer,typeMismatch]
        // 즉, 스프링은 타입 에러가 발생하면 typeMismatch 라는 에러 코드를 사용한다. MessageCodesResolver 로 4가지 코드가 생성된다.
        // 화면에서는 error.properties 에 메시지를 설정하지 않으면 스프링이 생성하는 기본 메시지가 출력된다.
        // Failed to convert property value of type java.lang.String to required type java.lang.Integer for property price; nested exception is java.lang.NumberFormatException: For input string: "ㅁㅁㅁ"
        // 프로퍼티에 typeMismatch 정보를 정의하면 해당 메시지가 출력된다.
        // 즉 소스코드를 건드리지 않고, 원하는 메시지를 우선순위에 따른 단계별로 설정하여 출력할 수 있다.


        // validation check : 검증 실패시 다시 입력 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v2/addForm";
        }

        // 검증 통과했을때 실행되는 성공로직
        // 상품등록
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

//    @PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // V5의 목표 : Validator 사용

        // validation check
        // Validator 인터페이스 사용방법 1 : ItemValidator 를 스프링 빈으로 직접 주입받아 validate() 호출
        itemValidator.validate(item, bindingResult);

        // validation check : 검증 실패시 다시 입력 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v2/addForm";
        }

        // 검증 통과했을때 실행되는 성공로직
        // 상품등록
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // V5의 목표 : Validator 사용

        /* 스프링이 제공하는 Validator 인터페이스 */
        // Validator 인터페이스를 사용해서 검증하면 스프링이 제공하는 추가적인 도움을 받을 수 있다.

        // validation check
        // Validator 인터페이스 사용방법 2 : @InitBinder 를 이용하여 WebDataBinder 에 검증기를 등록 후 사용하는 방법 (선언한 해당 컨트롤러에만 영향)
        // 검증하고자 하는 Model 에 @Validated 를 정의해준다.

        /* @Validated 어노테이션 */
        // @Validated 이 붙으면 WebDataBinder 에 등록한 검증기를 찾아서 실행
        // 검증기는 여러개를 등록할 수 있는데, 이때 어떤 검증기가 실행되어야 할지 구분이 필요하다.
        // 구분을 해주는 메소드가 supports() 이다.
        // supports(Item.class) 가 호출되고, 결과가 true 이면 ItemValidator 의 validate() 가 호출된다.

        // 해당 컨트롤러에만 적용하는 방법 : @InitBinder 를 이용하여 WebDataBinder 에 검증기를 등록
        // 모든 컨트롤러에 적용(글로벌 설정) : ItemServiceApplication 에 WebMvcConfigurer 를 구현한다.
        // public class ItemServiceApplication implements WebMvcConfigurer {..Override..}

        // @Validated : 스프링이 제공
        // @Valid : 자바 표준 javax.validation.@Valid

        // validation check : 검증 실패시 다시 입력 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v2/addForm";
        }

        // 검증 통과했을때 실행되는 성공로직
        // 상품등록
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

