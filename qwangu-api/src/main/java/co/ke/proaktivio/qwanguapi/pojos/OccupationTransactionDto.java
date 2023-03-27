package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction.OccupationTransactionType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OccupationTransactionDto {
    private OccupationTransactionType type;
    private String occupationId;
    private String invoiceId;
    private String receiptId;
    
    public static class OccupationTransactionDtoBuilder {
        private OccupationTransactionType type;
        private String occupationId;
        private String invoiceId;
        private String receiptId;
        
		public OccupationTransactionDtoBuilder type(OccupationTransactionType type) {
			this.type = type;
			return this;
		}
		public OccupationTransactionDtoBuilder occupationId(String occupationId) {
			this.occupationId = occupationId;
			return this;
		}
		public OccupationTransactionDtoBuilder invoiceId(String invoiceId) {
			this.invoiceId = invoiceId;
			return this;
		}
		public OccupationTransactionDtoBuilder receiptId(String receiptId) {
			this.receiptId = receiptId;
			return this;
		}
        public OccupationTransactionDto build() {
        	var transaction = new OccupationTransactionDto();
    		transaction.setType(type);
    		transaction.setOccupationId(occupationId);
    		
        	if(type.equals(OccupationTransactionType.CREDIT)) 
        		transaction.setReceiptId(receiptId);
        	else
        		transaction.setInvoiceId(invoiceId);
        	return transaction;
        }
        
    }
}
