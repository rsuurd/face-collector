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

import lombok.Data;
import ninja.facecollector.repositories.Face;
import ninja.facecollector.repositories.FaceRepository;
import ninja.facecollector.services.CollectService;
import ninja.facecollector.services.DiscordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.OK;

@Controller
public class HomeController {
	private CollectService collectService;
	private DiscordService discordService;
	private OAuth2AuthorizedClientService clientService;

	private FaceRepository faceRepository;

	@Autowired
	public HomeController(CollectService collectService, DiscordService discordService, OAuth2AuthorizedClientService clientService,
						  FaceRepository faceRepository) {
		this.collectService = collectService;
		this.discordService = discordService;
		this.clientService = clientService;

		this.faceRepository = faceRepository;
	}

	@GetMapping("/")
	public String home(Model model, OAuth2AuthenticationToken token) {
		model.addAttribute("request", new CollectRequest());

		addAttributes(model, token);

		return "home";
	}

	@PostMapping("/")
	public String collect(@Valid @ModelAttribute("request") CollectRequest request, BindingResult bindingResult, Model model, OAuth2AuthenticationToken token) {
		String view;

		if (bindingResult.hasErrors()) {
			addAttributes(model, token);

			view = "home";
		} else if (faceRepository.findByStreamerAndGuildId(request.streamer, request.guildId).isPresent()) {
			bindingResult.reject("Streamer.collecting", "That streamer's face is already being collected to that Discord server");

			addAttributes(model, token);

			view = "home";
		} else {
			Face face = faceRepository.findById(request.streamer).orElse(new Face(request.streamer)).withGuildId(request.guildId);

			faceRepository.save(face);

			CompletableFuture.runAsync(() ->
				collectService.collect(request.streamer, Collections.singleton(request.guildId)));

			view = "redirect:/";
		}

		return view;
	}

	@ResponseStatus(OK)
	@DeleteMapping("/streamers/{guildId}/{streamer}")
	public String delete(@PathVariable String guildId, @PathVariable String streamer, Model model, OAuth2AuthenticationToken token) {
		faceRepository.findByStreamerAndGuildId(streamer, guildId).ifPresent(face -> {
			face.getGuildIds().remove(guildId);

			faceRepository.save(face);
			discordService.deleteEmoji(face.getStreamer(), guildId);
		});

		getToken(token).ifPresent(tokenValue -> addStreamers(model, tokenValue));

		return "home :: streamers";
	}

	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
		binder.registerCustomEditor(String.class, "streamer", new StringTrimmerEditor(true) {
			@Override
			public void setAsText(String text) {
				super.setAsText(text.toLowerCase());
			}
		});
	}

	private Optional<String> getToken(OAuth2AuthenticationToken token) {
		return Optional.ofNullable(token).filter(AbstractAuthenticationToken::isAuthenticated).map(authentication -> {
			OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(authentication.getAuthorizedClientRegistrationId(), authentication.getName());

			return client == null ? null : client.getAccessToken().getTokenValue();
		});
	}

	private void addAttributes(Model model, OAuth2AuthenticationToken token) {
		getToken(token).ifPresent(tokenValue -> {
			model.addAttribute("user", discordService.getUser(tokenValue));

			addStreamers(model, tokenValue);
		});
	}

	private void addStreamers(Model model, String token) {
		Map<DiscordService.Guild, List<String>> streamers = new LinkedHashMap<>();

		discordService.listGuilds(token).forEach(guild -> {
			streamers.put(guild, faceRepository.findStreamersByGuildId(guild.getId()));
		});

		model.addAttribute("streamers", streamers);
	}

	@Data
	private static class CollectRequest {
		@NotNull
		private String guildId;

		@NotNull
		@Streamer
		private String streamer;
	}
}
