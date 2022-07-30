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

    @PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // V2의 목표 : 오류 메시지의 체계화

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

