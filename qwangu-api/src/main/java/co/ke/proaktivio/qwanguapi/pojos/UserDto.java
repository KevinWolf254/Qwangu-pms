package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@ToString
@Getter
@Setter
public class UserDto {
    private Person person;
    private String emailAddress;
    private String roleId;
}
