package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserWithoutPasswordDto {
    private String id;
    private Person person;
    private String emailAddress;
    private String roleId;
    private Boolean isAccountExpired;
    private Boolean isCredentialsExpired;
    private Boolean isAccountLocked;
    private Boolean isEnabled;
    private LocalDateTime created;
    private LocalDateTime modified;

    public UserWithoutPasswordDto(User user) {
        this.id = user.getId();
        this.person = user.getPerson();
        this.emailAddress = user.getEmailAddress();
        this.roleId = user.getRoleId();
        this.isAccountExpired = user.getIsAccountExpired();
        this.isCredentialsExpired = user.getIsCredentialsExpired();
        this.isAccountLocked = user.getIsAccountLocked();
        this.isEnabled = user.getIsEnabled();
        this.created = user.getCreated();
        this.modified = user.getModified();
    }
}
