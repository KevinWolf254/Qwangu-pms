package co.ke.proaktivio.qwanguapi.pojos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignInDto {
    @Schema(example = "johnDoe@email.com")
    private String username;
    @Schema(example = "ABc1234!")
    private String password;
}
