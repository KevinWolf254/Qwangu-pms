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
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "status", "field.required", "Status is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "notificationDate", "field.required", "Notification date required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "vacatingDate", "field.required", "Vacating date is required.");
    }
}
