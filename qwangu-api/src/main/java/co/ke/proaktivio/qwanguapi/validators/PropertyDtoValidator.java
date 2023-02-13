package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import lombok.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class PropertyDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 3;
    private static final int MAX_LENGTH = 1000;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return PropertyDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "field.required", "Property type is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", "Name is required.");
        PropertyDto request = (PropertyDto) target;


        if (StringUtils.hasText(request.getName())&& request.getName().trim().length() < MINIMUM_LENGTH) {
            errors.rejectValue("name", "field.min.length", new Object[] { MINIMUM_LENGTH },
                    "Name must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
        }
        if (StringUtils.hasText(request.getName()) && request.getName().trim().length() > MAX_LENGTH) {
            errors.rejectValue("name", "field.max.length", new Object[] { MAX_LENGTH },
                    "Name must be at most %s characters in length.".formatted(MAX_LENGTH));
        }
        if (StringUtils.hasText(request.getDescription()) && request.getDescription().trim().length() > MAX_LENGTH) {
            errors.rejectValue("description", "field.max.length", new Object[] { MAX_LENGTH },
                    "Property description must be at most %s characters in length.".formatted(MAX_LENGTH));
        }
    }
}
