package co.ke.proaktivio.qwanguapi.models;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "AUTHORITY_CONFIG")
public class Authority {
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
    private String modifiedBy;
    

    @NoArgsConstructor
    public static class AuthorityBuilder {
        private String name;

        public AuthorityBuilder name(String name) {
            this.name = name;
            return this;
        }

        public Authority build() {
        	var authority = new Authority();
        	authority.setName(name);
        	return authority;
        }
    }
}
