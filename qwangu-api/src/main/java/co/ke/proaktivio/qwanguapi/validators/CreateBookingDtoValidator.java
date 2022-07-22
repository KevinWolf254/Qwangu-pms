package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.CreateBookingDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class CreateBookingDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateBookingDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateOccupation((CreateBookingDto) target, errors);
        validatePaymentId((CreateBookingDto) target, errors);
        validateUnitId((CreateBookingDto) target, errors);
    }

    private void validateOccupation(CreateBookingDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupation", "field.required", "Occupation date required.");
    }

    private void validatePaymentId(CreateBookingDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "paymentId", "field.required", "Payment id is required.");
    }

    private void validateUnitId(CreateBookingDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "unitId", "field.required", "Unit id is required.");
    }
}
