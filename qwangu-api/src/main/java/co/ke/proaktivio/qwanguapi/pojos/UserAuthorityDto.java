package co.ke.proaktivio.qwanguapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthorityDto {
    private String id;
    private String name;
    private Boolean create;
    private Boolean read;
    private Boolean update;
    private Boolean delete;
    private Boolean authorize;
    private String roleId;
}
