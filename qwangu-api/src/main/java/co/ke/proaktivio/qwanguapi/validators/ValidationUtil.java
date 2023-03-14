package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationForNewTenantDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.pojos.TenantDto;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import co.ke.proaktivio.qwanguapi.pojos.VacateOccupationDto;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.util.StringUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	public static Function<UserRoleDto, UserRoleDto> validateUserRoleDto(Validator validator) {
        return createUserRoleDto -> {
            Errors errors = new BeanPropertyBindingResult(createUserRoleDto, UserRoleDto.class.getName());
            validator.validate(createUserRoleDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return createUserRoleDto;
        };
    }

	public static Function<UserAuthorityDto, UserAuthorityDto> validateUserAuthorityDto(Validator validator) {
        return createUserAuthorityDto -> {
            Errors errors = new BeanPropertyBindingResult(createUserAuthorityDto, UserAuthorityDto.class.getName());
            validator.validate(createUserAuthorityDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return createUserAuthorityDto;
        };
    }

    public static Function<PropertyDto, PropertyDto> validatePropertyDto(Validator validator) {
        return apartmentDto -> {
            Errors errors = new BeanPropertyBindingResult(apartmentDto, PropertyDto.class.getName());
            validator.validate(apartmentDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return apartmentDto;
        };
    }
    
	public static void vaidateOrderType(Optional<String> orderOptional) {
		if (orderOptional.isPresent() && StringUtils.hasText(orderOptional.get())
				&& !EnumUtils.isValidEnum(OrderType.class, orderOptional.get())) {
			String[] arrayOfState = Stream.of(OrderType.values()).map(OrderType::getType).toArray(String[]::new);
			String states = String.join(" or ", arrayOfState);
			throw new CustomBadRequestException("Order should be " + states + "!");
		}
	}
}
