package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOccupationTransactionDto {
    private String occupationId;
    private OccupationTransaction.OccupationTransactionType type;
    private InvoiceDto invoice;
    private String receiptId;

}