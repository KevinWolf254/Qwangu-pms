package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.User;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    private Person person;
    private String emailAddress;
    private String roleId;

    public static UserDto generateFrom(User user) {
        return new UserDto(user.getPerson(), user.getEmailAddress(), user.getRoleId());
    }
}
