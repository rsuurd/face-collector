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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

@RunWith(MockitoJUnitRunner.class)
public class DiscordServiceTest {
	@Mock
	private RestOperations restOperations;

	@InjectMocks
	private DiscordService service;

	@Test
	public void shouldPublishNewEmoji() {
		when(restOperations.exchange(any(), eq(GET), any(), eq(List.class), eq("guildId"))).thenReturn(new ResponseEntity<>(emptyList(), OK));

		service.publishEmoji("streamer", "emoji".getBytes(), "guildId");

		@SuppressWarnings("unchecked")
		ArgumentCaptor<HttpEntity<Map<String, Object>>> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

		verify(restOperations).exchange(any(), eq(POST), requestCaptor.capture(), eq(Void.class), eq("guildId"));

		HttpEntity<Map<String, Object>> request = requestCaptor.getValue();
		assertThat(request).isNotNull();
		Map<String, Object> emoji = request.getBody();
		assertThat(emoji).isNotNull();
		assertThat(emoji).containsEntry("name", "streamer");
		assertThat(emoji).containsEntry("image", "data:image/png;base64,ZW1vamk=");
	}

	@Test
	public void shouldDeleteEmoji() {
		Map<String, String> emoji = new LinkedHashMap<>();
		emoji.put("id", "emojiId");
		emoji.put("name", "streamer");

		when(restOperations.exchange(any(), eq(GET), any(), eq(List.class), eq("guildId"))).thenReturn(new ResponseEntity<>(singletonList(emoji), OK));

		service.deleteEmoji("streamer", "guildId");

		verify(restOperations).exchange(any(), eq(DELETE), any(), eq(Void.class), eq("guildId"), eq("emojiId"));
	}

	@Test
	public void shouldGetUser() {
		DiscordService.User user = new DiscordService.User();

		when(restOperations.exchange(anyString(), eq(GET), any(), eq(DiscordService.User.class))).thenReturn(new ResponseEntity<>(user, OK));

		assertThat(service.getUser("token")).isSameAs(user);
	}

	@Test
	public void shouldGetOwnedGuilds() {
		DiscordService.Guild joined = new DiscordService.Guild();
		DiscordService.Guild created = new DiscordService.Guild();
		created.setOwner(true);

		when(restOperations.exchange(anyString(), eq(GET), any(), any(ParameterizedTypeReference.class))).thenReturn(new ResponseEntity<>(Arrays.asList(joined, created), OK));

		assertThat(service.listGuilds("token")).hasSize(1);
	}
}