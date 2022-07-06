package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.ResetPasswordDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

public class ResetPasswordDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ResetPasswordDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validatePassword((ResetPasswordDto) target, errors);
    }

    private void validatePassword(ResetPasswordDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required", "Password is required.");
        String password = request.getPassword();
        if (password != null) {
            String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–[{}]:;',?/*~$^+=<>]).{8,20}$";
            var p = Pattern.compile(PASSWORD_REGEX);
            if (!p.matcher(password).matches())
                errors.rejectValue("password", "field.invalid", "Password is not valid.");
        }
    }
}
