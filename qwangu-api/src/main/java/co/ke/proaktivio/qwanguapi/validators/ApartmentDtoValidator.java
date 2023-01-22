package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class ApartmentDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 6;

    @Override
    public boolean supports(Class<?> clazz) {
        return PropertyDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", "Name is required.");
        PropertyDto request = (PropertyDto) target;
        if (request.getName() != null && request.getName().trim().length() < MINIMUM_LENGTH) {
            errors.rejectValue("name", "field.min.length", new Object[] { Integer.valueOf(MINIMUM_LENGTH) }, "Name must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
        }
    }
}
