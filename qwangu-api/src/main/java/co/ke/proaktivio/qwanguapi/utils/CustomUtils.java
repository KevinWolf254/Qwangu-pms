package co.ke.proaktivio.qwanguapi.utils;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.StringUtils;

import java.util.Optional;

public class CustomUtils {
    public static Optional<String> convertToOptional(String value) {
        return StringUtils.hasText(value) ?
                Optional.of(value) :
                Optional.empty();
    }

    public static Integer convertToInteger(String value, String name) {
        Integer finalValue;
        if(NumberUtils.isParsable(value)) {
            finalValue = NumberUtils.createInteger(value);
            if(finalValue < 1)
                throw new CustomBadRequestException("%s is not valid. Should be greater than zero!".formatted(name));
        } else
            throw new CustomBadRequestException("%s is not valid!".formatted(name));
        return finalValue;
    }
}
