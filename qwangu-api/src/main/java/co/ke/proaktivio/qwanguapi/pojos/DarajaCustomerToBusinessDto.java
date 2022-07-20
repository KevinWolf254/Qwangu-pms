package co.ke.proaktivio.qwanguapi.pojos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DarajaCustomerToBusinessDto {
    @JsonProperty("TransID")
    private String transactionId;
    @JsonProperty("TransactionType")
    private String transactionType;
    @JsonProperty("TransTime")
    private String transactionTime;
    @JsonProperty("TransAmount")
    private String amount;
    @JsonProperty("BusinessShortCode")
    private String shortCode;
    @JsonProperty("BillRefNumber")
    private String referenceNumber;
    @JsonProperty("InvoiceNumber")
    private String invoiceNo;
    @JsonProperty("OrgAccountBalance")
    private String accountBalance;
    @JsonProperty("ThirdPartyTransID")
    private String thirdPartyId;
    @JsonProperty("MSISDN")
    private String mobileNumber;
    @JsonProperty("FirstName")
    private String firstName;
    @JsonProperty("MiddleName")
    private String middleName;
    @JsonProperty("LastName")
    private String lastName;
}
