package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDto extends UserDto {
    private Boolean isEnabled;
}
