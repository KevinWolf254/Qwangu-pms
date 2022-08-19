package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.models.RentAdvance;
import co.ke.proaktivio.qwanguapi.pojos.UpdateRentAdvanceDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UpdateRentAdvanceDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return UpdateRentAdvanceDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateStatus((UpdateRentAdvanceDto) target, errors);
        validateReturnDetails((UpdateRentAdvanceDto) target, errors);
        validateReturnedOn((UpdateRentAdvanceDto) target, errors);
    }

    protected void validateStatus(UpdateRentAdvanceDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "status", "field.required",
                "Status is required.");
    }

    private void validateReturnDetails(UpdateRentAdvanceDto request, Errors errors) {
        if (request.getStatus().equals(RentAdvance.Status.RELEASED))
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "returnDetails", "field.required",
                    "Return details are required.");
    }

    private void validateReturnedOn(UpdateRentAdvanceDto request, Errors errors) {
        if (request.getStatus().equals(RentAdvance.Status.RELEASED))
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "returnedOn", "field.required",
                    "Returned on date is required.");
    }
}
