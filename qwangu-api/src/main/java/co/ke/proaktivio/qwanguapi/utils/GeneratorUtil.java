package co.ke.proaktivio.qwanguapi.utils;

import org.apache.commons.lang3.RandomStringUtils;

public class GeneratorUtil {

    public static String generateOccupationNo() {
        return RandomStringUtils.randomAlphanumeric(4);
    }
}
