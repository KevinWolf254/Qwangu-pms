package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.OccupationDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class OccupationDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return OccupationDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        if(((OccupationDto) target).getStartDate() == null)
            errors.rejectValue("startDate", "field.required", "Start date is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "unitId", "field.required", "Unit id is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "paymentId", "field.required", "Payment id is required.");
    }
}
