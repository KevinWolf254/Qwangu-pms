package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DarajaErrorResponse {
    private String requestId;
    private String errorCode;
    private String errorMessage;
}
