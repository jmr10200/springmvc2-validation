package hello.itemservice.web.validation;

import hello.itemservice.web.validation.form.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {

    // Bean Validation - HttpMessageConverter
    // @Valid, @Validated 는 HttpMessageConverter (@RequestBody) 에도 적용 가능

    @PostMapping("/add")
    public Object addItem(@RequestBody @Validated ItemSaveForm form, BindingResult bindingResult) {
        log.info("API Controller 호출");

        if (bindingResult.hasErrors()) {
            log.info("Validation errors = {}", bindingResult);
            return bindingResult.getAllErrors();
        }
        log.info("성공 로직 실행");
        return form;
        // 성공 요청 : 성공
        // 실패 요청 : JSON 을 객체로 생성하는 것 자체가 실패
        // Resolved [org.springframework.http.converter.HttpMessageNotReadableException: JSON parse error: Cannot deserialize value of type `java.lang.Integer` from String "s": not a valid Integer value;
        // nested exception is com.fasterxml.jackson.databind.exc.InvalidFormatException: Cannot deserialize value of type `java.lang.Integer` from String "s": not a valid Integer value at [Source: (PushbackInputStream); line: 1, column: 31] (through reference chain: hello.itemservice.web.validation.form.ItemSaveForm["price"])]
        // -> HttpMessageConverter 에서 요청 JSON 을 ItemSaveForm 객체로 생성하는 것을 실패한 것.
        // -> 객체 생성이 실패하여 Controller 자체가 호출되지 않고 Exception 이 발생한 것. Validator 도 실행되지 않음.

        // 검증 에러 : JSON 을 객체로 생성하는 것은 성공, 검증 실패
        // Validation errors = org.springframework.validation.BeanPropertyBindingResult: 1 errors
        // Field error in object 'itemSaveForm' on field 'price': rejected value [15]; codes [Range.itemSaveForm.price,Range.price,Range.java.lang.Integer,Range];
        // arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [itemSaveForm.price,price]; arguments []; default message [price],1000000,1000]; default message [must be between 1000 and 1000000]
        // -> return bindingResult.getAllErrors(); 는 ObjectError 와 FieldError 를 반환하므로 전체가 response 메시지로 클라이언트에 전달된다.

    }
    /* 참고 */
    // @ModelAttribute : HTTP 요청 파라미터 (URL 쿼리스트링, POST Form) 에 사용
    // -> 필드단위로 세밀하게 바인딩 적용. 특정 필드가 바인딩 되지 않아도 나머지 필드는 정상 바인딩, Validator 검증 적용 가능
    // @RequestBody : HTTP Body 데이터를 객체로 변환할 때 사용 (API JSON 요청)
    // -> HttpMessageConverter 단계에서 JSON 데이터를 객체로 변환하지 못하면 이후 단계 진행되지 않고 Exception 발생
    // -> 컨트롤러도 호출되지 않고, Validator 도 적용 불가능

    // HTTP 요청 파라미터를 처리하는 @ModelAttribute 는 각 필드 단위로 세밀하게 적용되므로 특정 필드타입 에러가 발생해도 나머지 필드는 정상 처리 가능하다.
    // HttpMessageConverter 는 @ModelAttribute 와 다르게 전체 객체 단위로 적용된다.
    // 즉, 메시지 컨버터의 작동이 성공해서 ItemSaveForm 객체가 만들어져야 @Valid , @Validated 가 적용된다.
    // HttpMessageConverter 단계에서 실패해서 발생하는 Exception 도 원하는 모양으로 처리 가능하다.

}
