package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.UpdateUserDto;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

public class UpdateUserDtoValidator extends UserDtoValidator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateUserDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateEmailAddress((UserDto) target, errors);
        validatePerson((UserDto) target, errors);
        validateRoleId((UserDto) target, errors);
        validateIsEnabled((UpdateUserDto) target, errors);
    }

    protected void validateIsEnabled(UpdateUserDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "isEnabled", "field.required",
                "IsEnabled is required.");
    }
}
