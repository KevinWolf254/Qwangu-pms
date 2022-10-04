package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "INVOICE")
public class Receipt {
    @Id
    private String id;
    private String occupationId;
    private String paymentId;

    public Receipt(String occupationId, String paymentId) {
        this.occupationId = occupationId;
        this.paymentId = paymentId;
    }
}
