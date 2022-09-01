package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class UpdateUserDto extends UserDto {
    private Boolean isEnabled;

    public UpdateUserDto(Person person, String emailAddress, String roleId, Boolean isEnabled) {
        super(person, emailAddress, roleId);
        this.isEnabled = isEnabled;
    }
}
