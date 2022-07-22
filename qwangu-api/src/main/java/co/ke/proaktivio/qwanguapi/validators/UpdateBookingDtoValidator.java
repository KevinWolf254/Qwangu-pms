package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.UpdateBookingDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UpdateBookingDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateBookingDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateOccupation((UpdateBookingDto) target, errors);
    }

    private void validateOccupation(UpdateBookingDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupation", "field.required", "Occupation date required.");
    }
}
