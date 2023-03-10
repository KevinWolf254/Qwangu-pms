package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ReceiptDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ReceiptDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupationId", "field.required",
                "Occupation id is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "paymentId", "field.required",
                "Payment id is required.");
    }
}
