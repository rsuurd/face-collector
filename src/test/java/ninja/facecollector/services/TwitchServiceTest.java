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
package ninja.facecollector.services;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

public class TwitchServiceTest {
	@Mock
	private RestOperations restOperations;

	private TwitchService service;

	@Before
	public void createService() {
		MockitoAnnotations.initMocks(this);

		service = new TwitchService(restOperations, "clientId");
	}

	@Test(expected = NullPointerException.class)
	public void shouldRequireStreamer() {
		service.getStream(null);
	}

	@Test
	public void shouldGetLiveStream() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Client-ID", "clientId");

		when(restOperations.exchange("https://api.twitch.tv/kraken/streams/streamer", GET, new HttpEntity<>(headers), TwitchService.StreamResponse.class))
			.thenReturn(new ResponseEntity<>(new TwitchService.StreamResponse(new TwitchService.Stream(720, new TwitchService.Preview("large", "template"))), OK));

		Optional<TwitchService.Stream> maybeStream = service.getStream("streamer");
		assertThat(maybeStream).isPresent();
		maybeStream.ifPresent(stream -> {
		 	assertThat(stream.getVideoHeight()).isEqualTo(720);
			assertThat(stream.getVideoWidth()).isEqualTo(1280);
			assertThat(stream.getPreview()).isNotNull();
		});
	}

	@Test
	public void shouldHandleOfflineStream() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Client-ID", "clientId");

		when(restOperations.exchange("https://api.twitch.tv/kraken/streams/streamer", GET, new HttpEntity<>(headers), TwitchService.StreamResponse.class))
			.thenReturn(new ResponseEntity<>(OK));

		Optional<TwitchService.Stream> maybeStream = service.getStream("streamer");
		assertThat(maybeStream).isNotPresent();
	}

	@Test
	public void shouldCheckIfUserExists() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Client-ID", "clientId");

		when(restOperations.exchange("https://api.twitch.tv/kraken/channels/streamer", GET, new HttpEntity<>(headers), Map.class))
			.thenReturn(new ResponseEntity<>(Collections.singletonMap("_id", 1), OK));

		assertThat(service.userExists("streamer")).isTrue();
	}
}