package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.UpdateOccupationDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UpdateOccupationDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateOccupationDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateStatus((UpdateOccupationDto) target, errors);
        validateStarted((UpdateOccupationDto) target, errors);
    }

    private void validateStatus(UpdateOccupationDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "status", "field.required", "Status is required.");
    }

    private void validateStarted(UpdateOccupationDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "started", "field.required", "Started is required.");
    }

}
