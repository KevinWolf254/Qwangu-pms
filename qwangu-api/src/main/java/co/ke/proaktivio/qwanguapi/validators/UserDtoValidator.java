package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

public class UserDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 6;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateEmailAddress((UserDto) target, errors);
        validatePerson((UserDto) target, errors);
        validateRoleId((UserDto) target, errors);
    }

    private void validateEmailAddress(UserDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "emailAddress", "field.required", "Email address is required.");
        String emailAddress = request.getEmailAddress();
        if (emailAddress != null) {
            if (emailAddress.trim().length() < MINIMUM_LENGTH)
                errors.rejectValue("emailAddress", "field.min.length", new Object[]{Integer.valueOf(MINIMUM_LENGTH)}, "Email address must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
            var emailAddressRegEx = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
            var p = Pattern.compile(emailAddressRegEx);
            if (!p.matcher(emailAddress).matches())
                errors.rejectValue("emailAddress", "field.email.invalid", "Email address is not valid.");
        }
    }

    private void validatePerson(UserDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "person.firstName", "field.required", "First name is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "person.surname", "field.required", "Surname is required.");
        String firstName = request.getPerson().getFirstName();
        if (firstName != null) {
            if (firstName.trim().length() < 3)
                errors.rejectValue("person.firstName", "field.min.length", new Object[]{Integer.valueOf(3)}, "First name must be at least %s characters in length.".formatted(3));
            if (firstName.trim().length() > 25)
                errors.rejectValue("person.firstName", "field.max.length", new Object[]{Integer.valueOf(25)}, "First name must be at most %s characters in length.".formatted(25));
        }
        String otherNames = request.getPerson().getOtherNames();
        if (StringUtils.hasText(otherNames)) {
            if (otherNames.trim().length() < 3)
                errors.rejectValue("person.otherNames", "field.min.length", new Object[]{Integer.valueOf(3)}, "Surname must be at least %s characters in length.".formatted(3));
            if (otherNames.trim().length() > 40)
                errors.rejectValue("person.otherNames", "field.max.length", new Object[]{Integer.valueOf(40)}, "First name must be at most %s characters in length.".formatted(40));
        }
        String surname = request.getPerson().getSurname();
        if (surname != null) {
            if (surname.trim().length() < 3)
                errors.rejectValue("person.surname", "field.min.length", new Object[]{Integer.valueOf(3)}, "Surname must be at least %s characters in length.".formatted(3));
            if (surname.trim().length() > 25)
                errors.rejectValue("person.surname", "field.max.length", new Object[]{Integer.valueOf(25)}, "Surname must be at most %s characters in length.".formatted(25));
        }
    }

    private void validateRoleId(UserDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "roleId", "field.required", "Role id is required.");
        String roleId = request.getRoleId();
        if (roleId != null) {
            if (roleId.trim().length() < 1)
                errors.rejectValue("roleId", "field.min.length", new Object[]{Integer.valueOf(1)}, "Role id must be at least %s characters in length.".formatted(1));
         }
    }

}
