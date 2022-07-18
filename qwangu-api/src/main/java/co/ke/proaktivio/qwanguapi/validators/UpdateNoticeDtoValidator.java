package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.UpdateNoticeDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UpdateNoticeDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateNoticeDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateIsActive((UpdateNoticeDto) target, errors);
        validateNotificationDate((UpdateNoticeDto) target, errors);
        validateVacatingDate((UpdateNoticeDto) target, errors);
    }

    private void validateIsActive(UpdateNoticeDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "active", "field.required", "Active is required.");
    }

    private void validateNotificationDate(UpdateNoticeDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "notificationDate", "field.required", "Notification date required.");
    }

    private void validateVacatingDate(UpdateNoticeDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "vacatingDate", "field.required", "Vacating date is required.");
    }
}
