package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.RentAdvanceDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class RentAdvanceDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return RentAdvanceDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateStatus((RentAdvanceDto) target, errors);
        validateOccupationId((RentAdvanceDto) target, errors);
        validatePaymentId((RentAdvanceDto) target, errors);
    }

    protected void validateStatus(RentAdvanceDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "status", "field.required", "Status is required.");
    }

    protected void validateOccupationId(RentAdvanceDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupationId", "field.required", "Occupation id is required.");
    }

    protected void validatePaymentId(RentAdvanceDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "paymentId", "field.required", "Payment id is required.");
    }
}
