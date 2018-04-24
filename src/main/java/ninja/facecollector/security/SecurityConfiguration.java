package ninja.facecollector.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.csrf().disable()
			.oauth2Login()
			.userInfoEndpoint().userService(new DefaultOAuth2UserService())
			.and()
			.tokenEndpoint().accessTokenResponseClient(new NimbusAuthorizationCodeTokenResponseClient());
	}
}
