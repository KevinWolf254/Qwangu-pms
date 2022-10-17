package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.pojos.SignInDto;
import co.ke.proaktivio.qwanguapi.pojos.TokenDto;
import co.ke.proaktivio.qwanguapi.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;

@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CorsConfigTest {
    @Autowired
    private WebTestClient client;
    @MockBean
    private UserService userService;

    @Test
    void corsTest() {
//        // given
//        String password = "QwwsefRgvt_@er23";
//        String emailAddress = "person@gmail.com";
//
//        SignInDto dto = new SignInDto(emailAddress, password);
//        var token = new TokenDto("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");
//
//        // when
//        when(userService.signIn(dto)).thenReturn(Mono.just(token));
//
//        // then
//        client.post()
//                .uri("/v1/signIn")
////                .uri("http://look-ma-no-port/v1/signIn")
//                .header("Origin", "http://any-origin.com")
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(Mono.just(dto), SignInDto.class)
//                .exchange()
//                .expectHeader()
//                .valueEquals("Access-Control-Allow-Origin", "*")
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$").isNotEmpty()
//                .jsonPath("$.success").isEqualTo(true)
//                .jsonPath("$.message").isEqualTo("Signed in successfully.")
//                .jsonPath("$.data").isNotEmpty()
//                .consumeWith(System.out::println);;
    }
}
