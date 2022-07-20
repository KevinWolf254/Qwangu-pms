package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class CreateNoticeDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return CreateNoticeDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateNotificationDate((CreateNoticeDto) target, errors);
        validateVacatingDate((CreateNoticeDto) target, errors);
        validateOccupationId((CreateNoticeDto) target, errors);
    }

    private void validateNotificationDate(CreateNoticeDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "notificationDate", "field.required", "Notification date required.");
    }

    private void validateVacatingDate(CreateNoticeDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "vacatingDate", "field.required", "Vacating date is required.");
    }

    private void validateOccupationId(CreateNoticeDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupationId", "field.required", "Occupation id is required.");
    }
}
