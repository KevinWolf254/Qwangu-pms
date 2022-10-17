package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.VacateOccupationDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class VacateOccupationDtoValidator implements Validator{

    @Override
    public boolean supports(Class<?> clazz) {
        return VacateOccupationDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateEndDate((VacateOccupationDto) target, errors);
    }

    protected void validateEndDate(VacateOccupationDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDate", "field.required", "End date is required.");
    }
}
