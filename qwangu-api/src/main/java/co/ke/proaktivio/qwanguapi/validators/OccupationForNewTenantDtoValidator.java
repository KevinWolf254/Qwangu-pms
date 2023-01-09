package co.ke.proaktivio.qwanguapi.validators;

import co.ke.proaktivio.qwanguapi.pojos.OccupationForNewTenantDto;
import co.ke.proaktivio.qwanguapi.pojos.TenantDto;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.regex.Pattern;

public class OccupationForNewTenantDtoValidator implements Validator {
    private final OccupationDtoValidator occupationDtoValidator;
    private final TenantDtoValidator tenantDtoValidator;

    public OccupationForNewTenantDtoValidator() {
        this.occupationDtoValidator = new OccupationDtoValidator();
        this.tenantDtoValidator = new TenantDtoValidator();
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return Object.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
            this.occupationDtoValidator.validate(((OccupationForNewTenantDto) target).getOccupation(), errors);

        if(!StringUtils.hasText(((OccupationForNewTenantDto) target).getTenantId()) && ((OccupationForNewTenantDto) target).getTenant() == null) {
            errors.rejectValue("tenant", "field.required", "Tenant or Tenant id is required!");
        }
        if(!StringUtils.hasText(((OccupationForNewTenantDto) target).getTenantId()) && ((OccupationForNewTenantDto) target).getTenant() != null) {
            tenantDtoValidator.validate(((OccupationForNewTenantDto) target).getTenant(), errors);
        }
    }

}
