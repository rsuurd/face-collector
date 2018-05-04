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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StreamerValidatorTest {
	@Mock
	private TwitchService twitchService;

	@InjectMocks
	private Streamer.Validator validator;

	@Test
	public void shouldAcceptNull() {
		assertThat(validator.isValid(null, null)).isTrue();

		verifyZeroInteractions(twitchService);
	}
	@Test
	public void shouldAcceptExistingStreamer() {
		when(twitchService.userExists("streamer")).thenReturn(true);

		assertThat(validator.isValid("streamer", null)).isTrue();
	}

	@Test
	public void shouldRejectNonExistingStreamer() {
		when(twitchService.userExists("streamer")).thenReturn(false);

		assertThat(new Streamer.Validator(twitchService).isValid("streamer", null)).isFalse();
	}
}
