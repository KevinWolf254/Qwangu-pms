package co.ke.proaktivio.qwanguapi.models;


import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Data
@NoArgsConstructor
@Document(value = "ONE_TIME_TOKEN")
public class OneTimeToken {
    @Id
    private String id;
    @Indexed(unique = true)
    private String token;
    @CreatedDate
    private LocalDateTime createdOn;
    private String userId;  
    @Transient
    private static final SecureRandom secureRandom = new SecureRandom();
    @Transient
    private static final Integer TOKEN_EXPIRATION_HOURS = 24;

    @Transient
    private String generateToken() {
        byte[] bytes = new byte[16];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    @Transient
    public LocalDateTime getExpirationDate() {
    	return createdOn != null ? createdOn.plusHours(TOKEN_EXPIRATION_HOURS) : LocalDateTime.now().plusHours(TOKEN_EXPIRATION_HOURS);
    }
    
    @Transient
    public Boolean hasExpired() {
    	LocalDateTime now = LocalDateTime.now();
    	return getExpirationDate().isBefore(now);
    }
    
    public static class OneTimeTokenBuilder {
        private String userId;

		public OneTimeTokenBuilder userId(String userId) {
			this.userId = userId;
			return this;
		}
		
		public OneTimeToken build() {
			var oneTimeToken = new OneTimeToken();
			oneTimeToken.setUserId(userId);
			oneTimeToken.setToken(oneTimeToken.generateToken());
			return oneTimeToken;
		}
    }
    
}
