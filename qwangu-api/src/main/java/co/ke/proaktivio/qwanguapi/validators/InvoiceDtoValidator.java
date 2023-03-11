package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Invoice.Type;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.time.LocalDate;
import java.util.stream.Stream;

public class InvoiceDtoValidator implements Validator {

	@Override
	public boolean supports(Class<?> clazz) {
		return InvoiceDto.class.isAssignableFrom(clazz);
	}

	@Override
	public void validate(Object target, Errors errors) {
		InvoiceDto dto = (InvoiceDto) target;
		if (dto == null) {
			errors.reject("invoiceDto.null", "InvoiceDto is required!");
		}

		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "type", "field.required", "Type is required.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "occupationId", "field.required",
				"Occupation id is required.");

		var type = ((InvoiceDto) target).getType();

		if (target != null && type != null) {
			validateInvoiceType((InvoiceDto) target, errors);
			if (type.equals(Type.RENT))
				validateDates((InvoiceDto) target, errors);
			if (type.equals(Type.UTILITIES))
				validateTypeUtilities((InvoiceDto) target, errors);
		}
	}

	private void validateInvoiceType(InvoiceDto dto, Errors errors) {
		if (!EnumUtils.isValidEnum(Invoice.Type.class, dto.getType().getName())) {
			String[] arrayOfState = Stream.of(Invoice.Type.values()).map(Invoice.Type::getName).toArray(String[]::new);
			String states = String.join(" or ", arrayOfState);
			errors.rejectValue("type", "field.invalid", "Type should be " + states + "!");
		}
	}

	private void validateTypeUtilities(InvoiceDto dto, Errors errors) {
		validateDates(dto, errors);
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "currency", "field.required",
				"Currency is required.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "otherAmounts", "field.required",
				"Other amounts is required.");
		if (dto.getOtherAmounts() != null && dto.getOtherAmounts().isEmpty())
			errors.rejectValue("otherAmounts", "field.invalid", "OtherAmounts should not be empty!");
	}

	private void validateDates(InvoiceDto dto, Errors errors) {
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "startDate", "field.required", "Start date is required.");
		ValidationUtils.rejectIfEmptyOrWhitespace(errors, "endDate", "field.required", "End date is required.");

		LocalDate startDate = dto.getStartDate();
		LocalDate endDate = dto.getEndDate();
		if (startDate != null && endDate != null) {
			if (startDate.isEqual(endDate) || startDate.isAfter(endDate)) {
				errors.reject("field.invalid", null, "End date should be after Start date!");
			}

		}
	}
}
