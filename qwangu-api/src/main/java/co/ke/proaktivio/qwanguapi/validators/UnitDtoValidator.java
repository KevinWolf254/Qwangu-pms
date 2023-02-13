package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UnitDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UnitDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "field.required", "Type is required.");
        validateUnitIdentifier((UnitDto) target, errors);
        validateFloorNo((UnitDto) target, errors);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noOfBedrooms", "field.required", "No of bedrooms is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noOfBathrooms", "field.required", "No of bathrooms is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "advanceInMonths", "field.required", "Advance in months is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currency", "field.required", "Currency is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "rentPerMonth", "field.required", "Rent per month is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "securityPerMonth", "field.required", "Security per month is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "garbagePerMonth", "field.required", "Garbage per month is required.");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "propertyId", "field.required", "Property Id is required.");
    }

    private void validateUnitIdentifier(UnitDto request, Errors errors) {
        if(request.getType() != null && request.getType().equals(Unit.UnitType.APARTMENT_UNIT))
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "identifier", "field.required", "Identifier is required.");
    }

    private void validateFloorNo(UnitDto request, Errors errors) {
        if(request.getType() != null && request.getType().equals(Unit.UnitType.APARTMENT_UNIT))
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "floorNo", "field.required", "Floor No is required.");
    }
}
