package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.EmailDto;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

public class EmailDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 6;

    @Override
    public boolean supports(Class<?> clazz) {
        return EmailDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateEmailAddress((EmailDto) target, errors);
    }

    private void validateEmailAddress(EmailDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailAddress", "field.required", "Email address is required.");
        String emailAddress = request.getEmailAddress();
        if (emailAddress != null) {
            if (emailAddress.trim().length() < MINIMUM_LENGTH)
                errors.rejectValue("emailAddress", "field.min.length", new Object[]{Integer.valueOf(MINIMUM_LENGTH)}, "Email address must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
            var emailAddressRegEx = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
            var p = Pattern.compile(emailAddressRegEx);
            if (!p.matcher(emailAddress).matches())
                errors.rejectValue("emailAddress", "field.emailAddress.invalid", "Email address is not valid.");
        }
    }
}
