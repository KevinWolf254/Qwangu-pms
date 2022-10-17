package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.stream.Stream;

public class InvoiceDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return InvoiceDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateInvoiceType((InvoiceDto) target, errors);
        validateRentAmount((InvoiceDto) target, errors);
        validateSecurityAmount((InvoiceDto) target, errors);
        validateGarbageAmount((InvoiceDto) target, errors);
        validateOccupationId((InvoiceDto) target, errors);
    }

    private void validateInvoiceType(InvoiceDto request, Errors errors) {
        if (!EnumUtils.isValidEnum(Invoice.Type.class, request.getType().getName())) {
            String[] arrayOfState = Stream.of(Invoice.Type.values()).map(Invoice.Type::getName).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            errors.rejectValue("type", "field.invalid", "Type should be " + states + "!");
        }

    }

    private void validateRentAmount(InvoiceDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "rentAmount", "field.required",
                "Rent amount is required.");
    }

    private void validateSecurityAmount(InvoiceDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "securityAmount", "field.required",
                    "Security amount are required.");
    }

    private void validateGarbageAmount(InvoiceDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "garbageAmount", "field.required",
                "Garbage amount are required.");
    }

    private void validateOccupationId(InvoiceDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupationId", "field.required",
                "Occupation id is required.");
    }
}
