package hello.itemservice;

import hello.itemservice.web.validation.ItemValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
//public class ItemServiceApplication implements WebMvcConfigurer {
public class ItemServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ItemServiceApplication.class, args);
	}

	/* 상품 저장 성공	*/
	// 0. GET /add : 상품등록 폼을 표시
	// 1. POST /add : 상품저장 실행
	// 2. Redirect /items/{id} : 등록된 정보 전달
	// 3. GET /items/{id} : 상품 상세 페이지 표시
	// 사용자가 상품 등록 폼에서 정상 범위의 데이터를 입력하면, 서버에서는 검증 로직이 통과하고, 저장하여 상세화면으로 redirect 한다.

	/* 상품 저장 검증 실패 */
	// 0. GET /add : 상품 등록 폼 표시
	// 1. POST /add : 상품 저장 실행
	// 상품 저장 실패 : Model 에 검증 오류 결과 포함하여 반환
	// 싱품 등록폼 표시
	// 사용자가 상품 등록 폼에서 상품명을 입력하지 않거나, 가격, 수량 등이 너무 작거나 커서 검증 범위를 넘어서면,
	// 서버 검증 로직이 실패해야한다. 검증에 실패한 경우 고객에게 다시 상품등록 폼을 보여주고,
	// 어떤 값을 잘못 입력했는지 알려줘야 한다.

	/* 모든 컨트롤러에 검증기를 적용 (글로벌 설정) */
//	@Override
//	public Validator getValidator() {
//		return new ItemValidator();
//	}

}
