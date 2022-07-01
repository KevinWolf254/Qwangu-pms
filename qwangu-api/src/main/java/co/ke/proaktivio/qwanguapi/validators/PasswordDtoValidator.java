package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.PasswordDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

public class PasswordDtoValidator implements Validator {
    private static String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";

    @Override
    public boolean supports(Class<?> clazz) {
        return PasswordDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateCurrentPassword((PasswordDto) target, errors);
        validateNewPassword((PasswordDto) target, errors);
    }

    private void validateCurrentPassword(PasswordDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currentPassword", "field.required", "Current password is required.");
        String password = request.getCurrentPassword();
        if (password != null) {
            var p = Pattern.compile(PASSWORD_REGEX);
            if (!p.matcher(PASSWORD_REGEX).matches())
                errors.rejectValue("currentPassword", "field.currentPassword.invalid", "Current password is not valid.");
        }
    }

    private void validateNewPassword(PasswordDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "newPassword", "field.required", "New password is required.");
        String password = request.getCurrentPassword();
        if (password != null) {
            var p = Pattern.compile(PASSWORD_REGEX);
            if (!p.matcher(PASSWORD_REGEX).matches())
                errors.rejectValue("newPassword", "field.newPassword.invalid", "New password is not valid.");
        }
    }
}
