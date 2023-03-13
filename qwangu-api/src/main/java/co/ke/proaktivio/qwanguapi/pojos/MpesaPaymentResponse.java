package co.ke.proaktivio.qwanguapi.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpesaPaymentResponse {
    @JsonProperty("ResultCode")
    private Object code;
    @JsonProperty("ResultDesc")
    private String description;
}
