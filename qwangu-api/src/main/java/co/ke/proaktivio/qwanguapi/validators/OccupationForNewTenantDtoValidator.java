package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.OccupationDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationForNewTenantDto;
import co.ke.proaktivio.qwanguapi.pojos.TenantDto;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

public class OccupationForNewTenantDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 6;

    @Override
    public boolean supports(Class<?> clazz) {
        return Object.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        OccupationDto occupation = ((OccupationForNewTenantDto) target).getOccupation();
        if (occupation != null) {
            if(occupation.getStartDate() == null)
                errors.rejectValue("occupation.startDate", "field.required", "Start date is required.");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupation.unitId", "field.required", "Unit id is required.");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupation.paymentId", "field.required", "Payment id is required.");
        } else {
            errors.rejectValue("occupation", "field.required", "Occupation is required.");
        }

        String tenantId = ((OccupationForNewTenantDto) target).getTenantId();
        TenantDto tenant = ((OccupationForNewTenantDto) target).getTenant();

        if (!StringUtils.hasText(tenantId) && tenant == null) {
            errors.rejectValue("tenant", "field.required", "Tenant or Tenant id is required.");
        }
        if (!StringUtils.hasText(tenantId) && tenant != null) {
//            tenantDtoValidator.validate(tenant, errors);
            validatePerson(tenant, errors);
            validateMobileNumber(tenant, errors);
            validateEmailAddress(tenant, errors);
        }
    }

    private void validatePerson(TenantDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tenant.firstName", "field.required", "First name is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tenant.surname", "field.required", "Surname is required.");
        String firstName = request.getFirstName();
        if (firstName != null) {
            if (firstName.trim().length() < 3)
                errors.rejectValue("tenant.firstName", "field.min.length", new Object[]{3}, "First name must be at least %s characters in length.".formatted(3));
            if (firstName.trim().length() > 25)
                errors.rejectValue("tenant.firstName", "field.max.length", new Object[]{25}, "First name must be at most %s characters in length.".formatted(25));
        }
        String otherNames = request.getMiddleName();
        if (StringUtils.hasText(otherNames)) {
            if (otherNames.trim().length() < 3)
                errors.rejectValue("tenant.middleName", "field.min.length", new Object[]{3}, "Middle name must be at least %s characters in length.".formatted(3));
            if (otherNames.trim().length() > 40)
                errors.rejectValue("tenant.middleName", "field.max.length", new Object[]{40}, "Middle name must be at most %s characters in length.".formatted(40));
        }
        String surname = request.getSurname();
        if (surname != null) {
            if (surname.trim().length() < 3)
                errors.rejectValue("tenant.surname", "field.min.length", new Object[]{3}, "Surname must be at least %s characters in length.".formatted(3));
            if (surname.trim().length() > 25)
                errors.rejectValue("tenant.surname", "field.max.length", new Object[]{25}, "Surname must be at most %s characters in length.".formatted(25));
        }
    }

    private void validateMobileNumber(TenantDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tenant.mobileNumber", "field.required", "Mobile number is required.");
        String mobileNumber = request.getMobileNumber();
        if (mobileNumber != null) {
            if (mobileNumber.trim().length() < MINIMUM_LENGTH)
                errors.rejectValue("tenant.mobileNumber", "field.min.length", new Object[]{MINIMUM_LENGTH}, "Email address must be at least %s characters in length.".formatted(10));
            var mobileNumberRegEx = "^(([+](254))|[0])(([7][0-9])|([1][0|1]))[0-9]{7}$";
            var p = Pattern.compile(mobileNumberRegEx);
            if (!p.matcher(mobileNumber).matches())
                errors.rejectValue("tenant.mobileNumber", "field.mobileNumber.invalid", "Mobile number is not valid.");
        }
    }

    private void validateEmailAddress(TenantDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tenant.emailAddress", "field.required", "Email address is required.");
        String emailAddress = request.getEmailAddress();
        if (emailAddress != null) {
            if (emailAddress.trim().length() < MINIMUM_LENGTH)
                errors.rejectValue("tenant.emailAddress", "field.min.length", new Object[]{MINIMUM_LENGTH}, "Email address must be at least %s characters in length.".formatted(MINIMUM_LENGTH));
            var emailAddressRegEx = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
            var p = Pattern.compile(emailAddressRegEx);
            if (!p.matcher(emailAddress).matches())
                errors.rejectValue("tenant.emailAddress", "field.email.invalid", "Email address is not valid.");
        }
    }
}
