package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import lombok.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UserAuthorityDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 3;
    private static final int MAX_LENGTH = 50;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return UserAuthorityDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "name", "field.required", "Name is required.");
        var request = (UserAuthorityDto) target;

        if (StringUtils.hasText(request.getName())&& request.getName().trim().length() < MINIMUM_LENGTH) {
            errors.rejectValue("name", "field.min.length", new Object[] { MINIMUM_LENGTH },
                    "Name must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
        }
        if (StringUtils.hasText(request.getName()) && request.getName().trim().length() > MAX_LENGTH) {
            errors.rejectValue("name", "field.max.length", new Object[] { MAX_LENGTH },
                    "Name must be at most %s characters in length.".formatted(MAX_LENGTH));
        }
        ValidationUtils.rejectIfEmpty(errors, "create", "field.required", "Create is required.");
        ValidationUtils.rejectIfEmpty(errors, "read", "field.required", "Read is required.");
        ValidationUtils.rejectIfEmpty(errors, "update", "field.required", "Update is required.");
        ValidationUtils.rejectIfEmpty(errors, "delete", "field.required", "Delete is required.");
        ValidationUtils.rejectIfEmpty(errors, "authorize", "field.required", "Authorize is required.");
        ValidationUtils.rejectIfEmpty(errors, "roleId", "field.required", "Role id is required.");

    }
}
