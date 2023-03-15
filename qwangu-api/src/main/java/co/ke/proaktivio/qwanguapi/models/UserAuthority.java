package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "USER_AUTHORITY")
public class UserAuthority {
    @Id
    private String id;
    private String name;
    private Boolean create;
    private Boolean read;
    private Boolean update;
    private Boolean delete;
    private Boolean authorize;
    private String roleId;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    @NoArgsConstructor
    public static class UserAuthorityBuilder {
        private String name;
        private Boolean create;
        private Boolean read;
        private Boolean update;
        private Boolean delete;
        private Boolean authorize;
        private String roleId;

        public UserAuthorityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserAuthorityBuilder create(Boolean create) {
            this.create = create;
            return this;
        }

        public UserAuthorityBuilder read(Boolean read) {
            this.read = read;
            return this;
        }

        public UserAuthorityBuilder update(Boolean update) {
            this.update = update;
            return this;
        }

        public UserAuthorityBuilder delete(Boolean delete) {
            this.delete = delete;
            return this;
        }

        public UserAuthorityBuilder authorize(Boolean authorize) {
            this.authorize = authorize;
            return this;
        }

        public UserAuthorityBuilder roleId(String roleId) {
            this.roleId = roleId;
            return this;
        }

        public UserAuthority build() {
            var authority = new UserAuthority();
            authority.setName(this.name);
            authority.setCreate(this.create);
            authority.setRead(this.read);
            authority.setUpdate(this.update);
            authority.setDelete(this.delete);
            authority.setAuthorize(this.authorize);
            authority.setRoleId(this.roleId);
            return authority;
        }
    }
}
