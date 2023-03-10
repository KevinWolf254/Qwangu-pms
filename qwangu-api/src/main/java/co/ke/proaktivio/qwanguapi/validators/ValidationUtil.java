package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationForNewTenantDto;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.pojos.TenantDto;
import co.ke.proaktivio.qwanguapi.pojos.VacateOccupationDto;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.function.Function;
import java.util.stream.Collectors;

public class ValidationUtil {
	
	public static Function<ReceiptDto, ReceiptDto> validateReceiptDto(Validator validator) {
        return createReceiptDto -> {
            Errors errors = new BeanPropertyBindingResult(createReceiptDto, ReceiptDto.class.getName());
            validator.validate(createReceiptDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return createReceiptDto;
        };
    }


    public static Function<InvoiceDto, InvoiceDto> validateInvoiceDto(Validator validator) {
        return invoiceDto -> {
            Errors errors = new BeanPropertyBindingResult(invoiceDto, InvoiceDto.class.getName());
            validator.validate(invoiceDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return invoiceDto;
        };
    }
    
    public static Function<OccupationDto, OccupationDto> validateOccupationDto(Validator validator) {
        return createOccupationDto -> {
            Errors errors = new BeanPropertyBindingResult(createOccupationDto, OccupationDto.class.getName());
            validator.validate(createOccupationDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return createOccupationDto;
        };
    }

    public static Function<VacateOccupationDto, VacateOccupationDto> validateVacateOccupationDto(Validator validator) {
        return vacateOccupationDto -> {
            Errors errors = new BeanPropertyBindingResult(vacateOccupationDto, VacateOccupationDto.class.getName());
            validator.validate(vacateOccupationDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return vacateOccupationDto;
        };
    }


    public static Function<OccupationForNewTenantDto, OccupationForNewTenantDto> validateOccupationForNewTenantDto(Validator validator) {
        return occupationForNewTenantDto -> {
            Errors errors = new BeanPropertyBindingResult(occupationForNewTenantDto, OccupationForNewTenantDto.class.getName());
            validator.validate(occupationForNewTenantDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return occupationForNewTenantDto;
        };
    }


    public static Function<TenantDto, TenantDto> validateTenantDtoFunc(Validator validator) {
        return tenantDto -> {
            Errors errors = new BeanPropertyBindingResult(tenantDto, TenantDto.class.getName());
            validator.validate(tenantDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return tenantDto;
        };
    }
}
