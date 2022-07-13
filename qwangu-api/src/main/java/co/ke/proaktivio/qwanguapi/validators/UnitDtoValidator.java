package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UnitDtoValidator implements Validator {
    private static final int MINIMUM_LENGTH = 6;

    @Override
    public boolean supports(Class<?> clazz) {
        return UnitDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateUnitType((UnitDto) target, errors);
        validateUnitIdentifier((UnitDto) target, errors);
        validateFloorNo((UnitDto) target, errors);
        validateNoOfBedrooms((UnitDto) target, errors);
        validateNoOfBathrooms((UnitDto) target, errors);
        validateAdvanceInMonths((UnitDto) target, errors);
        validateCurrency((UnitDto) target, errors);
        validateRentPerMonth((UnitDto) target, errors);
        validateSecurityPerMonth((UnitDto) target, errors);
        validateGarbagePerMonth((UnitDto) target, errors);
        validateApartmentId((UnitDto) target, errors);
    }

    private void validateUnitType(UnitDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "field.required", "Type is required.");
    }

    private void validateUnitIdentifier(UnitDto request, Errors errors) {
        if(request.getType().equals(Unit.Type.APARTMENT_UNIT))
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "identifier", "field.required", "Type is required.");
    }

    private void validateFloorNo(UnitDto request, Errors errors) {
        if(request.getType().equals(Unit.Type.APARTMENT_UNIT))
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "floorNo", "field.required", "Floor No is required.");
    }

    private void validateNoOfBedrooms(UnitDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noOfBedrooms", "field.required", "No of bedrooms is required.");
    }

    private void validateNoOfBathrooms(UnitDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "noOfBathrooms", "field.required", "No of bathrooms is required.");
    }

    private void validateAdvanceInMonths(UnitDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "advanceInMonths", "field.required", "Advance in months is required.");
    }

    private void validateCurrency(UnitDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currency", "field.required", "Currency is required.");
    }

    private void validateRentPerMonth(UnitDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "rentPerMonth", "field.required", "Rent per month is required.");
    }

    private void validateSecurityPerMonth(UnitDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "securityPerMonth", "field.required", "Security per month is required.");
    }

    private void validateGarbagePerMonth(UnitDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "garbagePerMonth", "field.required", "Garbage per month is required.");
    }

    private void validateApartmentId(UnitDto request, Errors errors) {
        if(request.getType().equals(Unit.Type.APARTMENT_UNIT))
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "apartmentId", "field.required", "Apartment Id is required.");
    }
}
