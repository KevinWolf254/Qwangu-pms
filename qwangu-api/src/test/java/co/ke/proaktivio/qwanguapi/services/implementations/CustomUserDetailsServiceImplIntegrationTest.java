package co.ke.proaktivio.qwanguapi.services.implementations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.repositories.UserAuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRoleRepository;
import co.ke.proaktivio.qwanguapi.services.CustomUserDetailsService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class CustomUserDetailsServiceImplIntegrationTest {
	@Autowired
    private UserRepository userRepository;
	@Autowired
	private UserRoleRepository userRoleRepository;
	@Autowired
	private UserAuthorityRepository userAuthorityRepository;
	@Autowired
	private CustomUserDetailsService underTest;
    
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }
    
    private Mono<Void> reset() {
		return userRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("--- Deleted users!"))
				.then(userRoleRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted user roles!"))
				.then(userAuthorityRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted user authorities!"));
	}
    
	@Test
	void findByUsername_returnsUsernameNotFoundException_whenUsernameDoesNotExist() {
		// given
		var emailAddress = "john.doe@somecompany.com";
		// when
		Mono<UserDetails> doesNotExist = reset().then(underTest.findByUsername(emailAddress));
		// then
		StepVerifier
			.create(doesNotExist)
			.expectErrorMatches(e -> e instanceof UsernameNotFoundException 
					&& e.getMessage().equals("User %s could not be found!".formatted(emailAddress)))
			.verify();
	}
    
	@Test
	void findByUsername_returnsCustomBadRequestException_whenNoRoleIsAssignedToUser() {
		var emailAddress = "john.doe@somecompany.com";
		var person = new Person("John", "Doe", "JustDoe");
		var user = new User.UserBuilder()
				.person(person)
				.emailAddress(emailAddress)
				.build();
				
		// when
		Mono<UserDetails> doesNotExist = reset().then(userRepository.save(user))
				.doOnSuccess(u -> System.out.println("Created: " +u))
				.then(underTest.findByUsername(emailAddress));
		// then
		StepVerifier
			.create(doesNotExist)
			.expectErrorMatches(e -> e instanceof CustomBadRequestException 
					&& e.getMessage().equals("User %s is not assigned a role!".formatted(emailAddress)))
			.verify();
	}
    
	@Test
	void findByUsername_returnsCustomNotFoundException_whenRoleIdDoesNotExist() {
		// given
		var roleId = "1234";
		var emailAddress = "john.doe@somecompany.com";
		var person = new Person("John", "Doe", "JustDoe");
		var user = new User.UserBuilder()
				.person(person)
				.emailAddress(emailAddress)
				.roleId(roleId)
				.build();
				
		// when
		Mono<UserDetails> doesNotExist = reset().then(userRepository.save(user))
				.doOnSuccess(u -> System.out.println("Created: " +u))
				.then(underTest.findByUsername(emailAddress));
		// then
		StepVerifier
			.create(doesNotExist)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException 
					&& e.getMessage().equals("Role for user %s could not be found!".formatted(emailAddress)))
			.verify();
	}
    
	@Test
	void findByUsername_returnsUserDetails_whenSuccessful() {
		// given
		var roleId = "1234";
		String roleName = "ADMIN";
		var role = new UserRole.UserRoleBuilder()
				.name(roleName)
				.build();
		role.setId(roleId);
		String authorityName = "APARTMENT";
		var authority = new UserAuthority.UserAuthorityBuilder()
				.name(authorityName)
				.read(true)
				.create(true)
				.update(true)
				.authorize(true)
				.delete(false)
				.roleId(roleId)
				.build();
		var emailAddress = "john.doe@somecompany.com";
		var person = new Person("John", "Doe", "JustDoe");
		var user = new User.UserBuilder()
				.person(person)
				.emailAddress(emailAddress)
				.roleId(roleId)
				.build();
				
		// when
		Mono<UserDetails> doesNotExist = reset().then(userRoleRepository.save(role))
				.doOnSuccess(u -> System.out.println("Created: " +u))
				.then(userAuthorityRepository.save(authority))
				.doOnSuccess(u -> System.out.println("Created: " +u))
				.then(userRepository.save(user))
				.doOnSuccess(u -> System.out.println("Created: " +u))
				.then(underTest.findByUsername(emailAddress));
		// then
		StepVerifier
			.create(doesNotExist)
            .expectNextMatches(cud ->
            cud.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + roleName)) &&
            cud.getAuthorities().contains(new SimpleGrantedAuthority(authorityName + "_UPDATE")) &&
            cud.getAuthorities().contains(new SimpleGrantedAuthority(authorityName + "_AUTHORIZE")) &&
            cud.getAuthorities().contains(new SimpleGrantedAuthority(authorityName + "_CREATE")) &&
            cud.getAuthorities().contains(new SimpleGrantedAuthority(authorityName + "_READ")) &&
            cud.getPassword() == null && cud.getUsername().equalsIgnoreCase(emailAddress)
            && cud.isAccountNonExpired() && cud.isCredentialsNonExpired() && cud.isAccountNonLocked() && !cud.isEnabled())
    .verifyComplete();
	}
}
