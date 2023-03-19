package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.EmailDto;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationForNewTenantDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.PasswordDto;
import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.pojos.ResetPasswordDto;
import co.ke.proaktivio.qwanguapi.pojos.SignInDto;
import co.ke.proaktivio.qwanguapi.pojos.TenantDto;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import co.ke.proaktivio.qwanguapi.pojos.UpdateUserDto;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
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

    public static Function<UserDto, UserDto> validateUserDto(Validator validator) {
        return userDto -> {
            Errors errors = new BeanPropertyBindingResult(userDto, UserDto.class.getName());
            validator.validate(userDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return userDto;
        };
    }

    public static Function<UpdateUserDto, UpdateUserDto> validateUpdateUserDto(Validator validator) {
        return userDto -> {
            Errors errors = new BeanPropertyBindingResult(userDto, UpdateUserDto.class.getName());
            validator.validate(userDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return userDto;
        };
    }

    public static Function<PasswordDto, PasswordDto> validatePasswordDto(Validator validator) {
        return passwordDto -> {
            Errors errors = new BeanPropertyBindingResult(passwordDto, PasswordDto.class.getName());
            validator.validate(passwordDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return passwordDto;
        };
    }

    public static Function<SignInDto, SignInDto> validateSignInDto(Validator validator) {
        return signInDto -> {
            Errors errors = new BeanPropertyBindingResult(signInDto, SignInDto.class.getName());
            validator.validate(signInDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return signInDto;
        };
    }

    public static Function<EmailDto, EmailDto> validateEmailDto(Validator validator) {
        return emailDto -> {
            Errors errors = new BeanPropertyBindingResult(emailDto, EmailDto.class.getName());
            validator.validate(emailDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return emailDto;
        };
    }

    public static Function<ResetPasswordDto, ResetPasswordDto> validateResetPasswordDto(Validator validator) {
        return passwordDto -> {
            Errors errors = new BeanPropertyBindingResult(passwordDto, ResetPasswordDto.class.getName());
            validator.validate(passwordDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return passwordDto;
        };
    }
    
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


    public static Function<UnitDto, UnitDto> validateUnitDto(Validator validator) {
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
}
