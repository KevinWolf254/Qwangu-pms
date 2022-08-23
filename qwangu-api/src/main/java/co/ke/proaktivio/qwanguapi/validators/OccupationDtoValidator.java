package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.OccupationDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class OccupationDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return OccupationDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateStatus((OccupationDto) target, errors);
        validateStarted((OccupationDto) target, errors);
        validateTenantId((OccupationDto) target, errors);
        validateUnitId((OccupationDto) target, errors);
    }

    private void validateStatus(OccupationDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "status", "field.required", "Status is required.");
    }

    private void validateStarted(OccupationDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "started", "field.required", "Started is required.");
    }

    private void validateTenantId(OccupationDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tenantId", "field.required", "Tenant id is required.");
    }

    private void validateUnitId(OccupationDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "unitId", "field.required", "Unit id is required.");
    }
}