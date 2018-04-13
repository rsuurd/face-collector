package ninja.facecollector.web;

import ninja.facecollector.services.TwitchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Streamer.Validator.class)
public @interface Streamer {
	String message() default "{Streamer.message}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	@Component
	class Validator implements ConstraintValidator<Streamer, String> {
		private TwitchService twitchService;

		@Autowired
		public Validator(TwitchService twitchService) {
			this.twitchService = twitchService;
		}

		public void initialize(Streamer constraintAnnotation) {}

		public boolean isValid(String value, ConstraintValidatorContext context) {
			return Optional.ofNullable(value).map(twitchService::userExists).orElse(true);
		}
	}
}
