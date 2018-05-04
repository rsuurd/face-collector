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

import ninja.facecollector.repositories.Face;
import ninja.facecollector.repositories.FaceRepository;
import ninja.facecollector.services.CollectService;
import ninja.facecollector.services.DiscordService;
import ninja.facecollector.services.GiphyService;
import ninja.facecollector.services.TwitchService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@Import(FaceCollectorDialect.class)
@WebMvcTest(controllers = HomeController.class, secure = false)
public class HomeControllerTest {
	@MockBean
	private OAuth2AuthorizedClientService clientService;

	@MockBean
	private CollectService collectService;

	@MockBean
	private DiscordService discordService;

	@MockBean
	private FaceRepository faceRepository;

	@MockBean
	private GiphyService giphyService;

	@MockBean
	private TwitchService twitchService;

	@Autowired
	private MockMvc mvc;

	@Before
	public void setUpOAuth2() {
		OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class, Answers.RETURNS_DEEP_STUBS);
		when(clientService.loadAuthorizedClient("registrationId", "name")).thenReturn(authorizedClient);
		when(authorizedClient.getAccessToken().getTokenValue()).thenReturn("token");
	}

	@Test
	public void shouldShowHome() throws Exception {
		mvc.perform(get("/"))
			.andExpect(model().attributeExists("request"))
			.andExpect(status().isOk())
			.andExpect(view().name("home"));
	}

	@Test
	public void shouldShowHomeWhenLoggedIn() throws Exception {
		when(discordService.getUser("token")).thenReturn(new DiscordService.User());

		mvc.perform(get("/").principal(oauthToken()))
			.andExpect(model().attributeExists("request", "user", "streamers"))
			.andExpect(status().isOk())
			.andExpect(view().name("home"));

		verify(discordService).getUser("token");
		verify(discordService).listGuilds("token");
	}

	@Test
	public void shouldCollect() throws Exception {
		when(twitchService.userExists("streamer")).thenReturn(true);
		when(faceRepository.findByStreamerAndGuildId("streamer", "guildId")).thenReturn(empty());

		mvc.perform(post("/")
				.param("guildId", "guildId")
				.param("streamer", "streamer")
			)
			.andExpect(status().isFound())
			.andExpect(redirectedUrl("/"));

		verify(faceRepository).save(new Face("streamer", "guildId"));
	}

	@Test
	public void shouldNotCollectNonExistingStreamer() throws Exception {
		when(twitchService.userExists("streamer")).thenReturn(false);

		mvc.perform(post("/")
				.param("guildId", "guildId")
				.param("streamer", "streamer")
			)
			.andExpect(status().isOk())
			.andExpect(model().attributeHasFieldErrorCode("request", "streamer", "Streamer"))
			.andExpect(view().name("home"));

		verifyZeroInteractions(faceRepository, collectService);
	}

	@Test
	public void shouldNotDoubleCollect() throws Exception {
		when(twitchService.userExists("streamer")).thenReturn(true);
		when(faceRepository.findByStreamerAndGuildId("streamer", "guildId")).thenReturn(of(new Face("streamer", "guildId")));

		mvc.perform(post("/")
				.param("guildId", "guildId")
				.param("streamer", "streamer")
			)
			.andExpect(status().isOk())
			.andExpect(model().hasErrors())
			.andExpect(view().name("home"));

		verify(faceRepository, never()).save(new Face("streamer", "guildId"));
		verifyZeroInteractions(collectService);
	}

	@Test
	public void shouldDeleteStreamer() throws Exception {
		when(twitchService.userExists("streamer")).thenReturn(true);
		when(faceRepository.findByStreamerAndGuildId("streamer", "guildId")).thenReturn(of(new Face("streamer", "guildId")));

		mvc.perform(delete("/streamers/{guildId}/{streamer}", "guildId", "streamer"))
			.andExpect(status().isOk())
			.andExpect(view().name("home :: streamers"));

		verify(faceRepository).save(new Face("streamer"));
		verify(discordService).deleteEmoji("streamer", "guildId");
	}

	private OAuth2AuthenticationToken oauthToken() {
		Set<GrantedAuthority> authorities = singleton(new SimpleGrantedAuthority("identity"));

		return new OAuth2AuthenticationToken(new DefaultOAuth2User(authorities, singletonMap("name", "name"), "name"), authorities, "registrationId");
	}
}