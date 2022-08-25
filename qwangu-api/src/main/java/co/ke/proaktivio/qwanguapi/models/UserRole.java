package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "USER_ROLE")
public class UserRole {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String updatedBy;

    public UserRole(String id) {
        this.id = id;
    }

    @NoArgsConstructor
    public static class RoleBuilder {
        private String name;

        public RoleBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public UserRole build() {
            var role = new UserRole();
            role.setName(this.name);
            return role;
        }
    }
}
