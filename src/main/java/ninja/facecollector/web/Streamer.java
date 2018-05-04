/*
 *     Face Collector
 *     Copyright (C) 2018 Rolf Suurd
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
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
