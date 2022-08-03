package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import hello.itemservice.domain.item.SaveCheck;
import hello.itemservice.domain.item.UpdateCheck;
import hello.itemservice.web.validation.form.ItemSaveForm;
import hello.itemservice.web.validation.form.ItemUpdateForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/validation/v4/items")
@RequiredArgsConstructor
public class ValidationItemControllerV4 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v4/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v4/addForm";
    }

    /* Form 전송 객체 분리 */
    // 실무에서는 groups 를 잘 사용하지 않는다. 등록 또는 수정시 폼에서 전달하는 데이터가 Item 도메인 객체와 딱 맞지 않기 때문이다.
    // 그래서 ItemSaveForm, ItemUpdateForm 등 폼을 전달하는 객체를 만들어서 분리하여 @ModelAttribute 로 사용한다.
    // 컨트롤러에서 폼 데이터를 전달 받고, 필요한 데이터를 사용해서 Item 을 생성한다.
    // 등록과 수정의 항목이 비슷하다고하여 하나로 합치지 말자. 유지보수가 힘들게 된다.

    // 폼 데이터 전송
    // 1. Item 객체를 사용 : HTML Form -> Item -> Controller -> Item -> Repository
    // 장점 : Item 도메인 객체를 이용하므로 Item 객체를 생성하는 과정이 필요없다.
    // 단점 : 간단한 경우에만 적용가능, 수정시 검증 중복등의 문제가 발생하여 groups 를 사용해야한다.
    // 2. 별도 객체 사용 : HTML Form -> ItemSaveForm -> Controller -> Item 생성 -> Repository
    // 장점 : 폼 데이터가 복잡해도 각각 별도의 폼 객체를 사용하므로 검증 중복 발생하지 않는다.
    // 단점 : 폼 데이터로 컨트롤러에서 Item 을 생성 및 변환해줘야 한다.

    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute("item") ItemSaveForm form, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // @ModelAttribute("item") : item 을 지정하지 않으면 객체의 이름이 디폴트 값이 된다. (itemSaveForm)
        // @ModelAttribute 로 ItemSaveForm 을 전달 받는다. @Validated 로 검증 수행하며, BindingResult 로 검증 결과도 받는다.

        // 특정 필드 에러(FieldError) 는 @Validated 를 이용, BeanValidation
        // 글로벌 에러 확인
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // validation check : 검증 실패시 다시 입력 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            return "validation/v4/addForm";
        }

        // 검증 통과했을때 실행되는 성공로직
        // item 생성
        Item item = new Item(form.getItemName(), form.getPrice(), form.getQuantity());

        // 상품등록
        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v4/items/{itemId}";
    }

    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v4/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute("item") ItemUpdateForm form, BindingResult bindingResult) {

        // 특정 필드가 아닌 전체 에러
        if (form.getPrice() != null && form.getQuantity() != null) {
            int resultPrice = form.getPrice() * form.getQuantity();
            if (resultPrice < 10000) {
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        // validation check : 검증 실패시 다시 수정 폼 표시
        if (bindingResult.hasErrors()) {
            log.info("errors={}", bindingResult);
            // 입력 폼 표시
            return "validation/v4/editForm";
        }

        Item item = new Item(form.getItemName(), form.getPrice(), form.getQuantity());

        itemRepository.update(itemId, item);
        return "redirect:/validation/v4/items/{itemId}";
    }

}

