package co.ke.proaktivio.qwanguapi.validators;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

//public class CreateOccupationDtoValidator implements Validator {
//
//    @Override
//    public boolean supports(Class<?> clazz) {
//        return CreateOccupationDto.class.isAssignableFrom(clazz);
//    }
//
//    @Override
//    public void validate(Object target, Errors errors) {
//        validateStarted((CreateOccupationDto) target, errors);
//        validateTenantId((CreateOccupationDto) target, errors);
//        validateUnitId((CreateOccupationDto) target, errors);
//    }
//
//    private void validateStarted(CreateOccupationDto request, Errors errors) {
//            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "started", "field.required", "Started is required.");
//    }
//
//    private void validateTenantId(CreateOccupationDto request, Errors errors) {
//            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "tenantId", "field.required", "Tenant id is required.");
//    }
//
//    private void validateUnitId(CreateOccupationDto request, Errors errors) {
//        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "unitId", "field.required", "Unit id is required.");
//    }
//}
