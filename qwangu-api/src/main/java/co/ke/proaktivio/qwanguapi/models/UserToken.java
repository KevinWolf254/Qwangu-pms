package co.ke.proaktivio.qwanguapi.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(value = "USER_TOKEN")
public class UserToken {
    @Id
    private String id;
    @Indexed(unique = true)
    private String emailAddress;
    private String token;
    @CreatedDate
    private LocalDateTime firstSignIn;
    @LastModifiedDate
    private LocalDateTime lastSignIn;
    
    public static class UserTokenBuilder {
        private String emailAddress;
        private String token;
        
		public UserTokenBuilder emailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
			return this;
		}
		
		public UserTokenBuilder token(String token) {
			this.token = token;
			return this;
		}
		
		public UserToken build() {
			var userToken = new UserToken();
			userToken.setEmailAddress(emailAddress);
			userToken.setToken(token);
			return userToken;
		}
        
    }
}
