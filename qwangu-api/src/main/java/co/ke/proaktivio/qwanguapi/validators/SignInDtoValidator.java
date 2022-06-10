package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.SignInDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

public class SignInDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 6;

    @Override
    public boolean supports(Class<?> clazz) {
        return SignInDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateUsername((SignInDto) target, errors);
        validatePassword((SignInDto) target, errors);
    }

    private void validateUsername(SignInDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "field.required", "Username is required.");
        String emailAddress = request.getUsername();
        if (emailAddress != null) {
            if (emailAddress.trim().length() < MINIMUM_LENGTH)
                errors.rejectValue("username", "field.min.length", new Object[]{Integer.valueOf(MINIMUM_LENGTH)}, "Username must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
            var emailAddressRegEx = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
            var p = Pattern.compile(emailAddressRegEx);
            if (!p.matcher(emailAddress).matches())
                errors.rejectValue("username", "field.username.invalid", "Username is not a valid email address.");
        }
    }

    private void validatePassword(SignInDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required", "Password is required.");
        String password = request.getPassword();
        if (password != null)
            if (password.trim().length() < MINIMUM_LENGTH)
                errors.rejectValue("password", "field.min.length", new Object[]{Integer.valueOf(MINIMUM_LENGTH)}, "Password must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
    }
}
