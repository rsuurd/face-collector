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
