package co.ke.proaktivio.qwanguapi.utils.validators;

import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ApartmentDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 6;

    @Override
    public boolean supports(Class<?> clazz) {
        return ApartmentDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required");
        ApartmentDto request = (ApartmentDto) target;
        if (request.getName() != null && request.getName().trim().length() < MINIMUM_LENGTH) {
            errors.rejectValue("code", "field.min.length", new Object[] { Integer.valueOf(MINIMUM_LENGTH) }, "The code must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
        }
    }
}
