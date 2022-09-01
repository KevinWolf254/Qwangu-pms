package co.ke.proaktivio.qwanguapi.utils;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomUserHandlerValidatorUtil {

    public static Function<UserDto, UserDto> validateUserDtoFunc(Validator validator) {
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

    public static Function<UpdateUserDto, UpdateUserDto> validateUpdateUserDtoFunc(Validator validator) {
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

    public static Function<PasswordDto, PasswordDto> validatePasswordDtoFunc(Validator validator) {
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

    public static Function<SignInDto, SignInDto> validateSignInDtoFunc(Validator validator) {
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

    public static Function<EmailDto, EmailDto> validateEmailDtoFunc(Validator validator) {
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

    public static Function<ResetPasswordDto, ResetPasswordDto> validateResetPasswordDtoFunc(Validator validator) {
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

}
