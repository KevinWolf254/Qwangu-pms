package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class BookingRefundDtoValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return BookingRefundDto.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        validateAmount((BookingRefundDto) target, errors);
        validateRefundDetails((BookingRefundDto) target, errors);
        validateReceivableId((BookingRefundDto) target, errors);
    }

    private void validateAmount(BookingRefundDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "amount", "field.required", "Amount is required.");
    }

    private void validateRefundDetails(BookingRefundDto request, Errors errors) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "refundDetails", "field.required", "Refund details are required.");
    }

    private void validateReceivableId(BookingRefundDto request, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "receivableId", "field.required", "Receivable id is required.");
    }
}
