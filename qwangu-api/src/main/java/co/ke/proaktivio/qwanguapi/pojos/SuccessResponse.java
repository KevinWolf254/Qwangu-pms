package co.ke.proaktivio.qwanguapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class SuccessResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
