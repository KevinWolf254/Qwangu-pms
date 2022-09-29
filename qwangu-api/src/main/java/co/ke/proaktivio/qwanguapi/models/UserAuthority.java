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
    public static class AuthorityBuilder {
        private String name;
        private Boolean create;
        private Boolean read;
        private Boolean update;
        private Boolean delete;
        private Boolean authorize;
        private String roleId;

        public AuthorityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public AuthorityBuilder create(Boolean create) {
            this.create = create;
            return this;
        }

        public AuthorityBuilder read(Boolean read) {
            this.read = read;
            return this;
        }

        public AuthorityBuilder update(Boolean update) {
            this.update = update;
            return this;
        }

        public AuthorityBuilder delete(Boolean delete) {
            this.delete = delete;
            return this;
        }

        public AuthorityBuilder authorize(Boolean authorize) {
            this.authorize = authorize;
            return this;
        }

        public AuthorityBuilder roleId(String roleId) {
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
