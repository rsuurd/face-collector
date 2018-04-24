package ninja.facecollector.services;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
@Service
public class DiscordService {
	private RestOperations restOperations;

	private String baseUrl;
	private String clientId;
	private String token;

	@Autowired
	public DiscordService(RestOperations restOperations, @Value("${baseUrl}") String baseUrl, @Value("${discord.clientId}") String clientId, @Value("${discord.token}") String token) {
		this.restOperations = restOperations;

		this.baseUrl = baseUrl;
		this.clientId = clientId;
		this.token = token;
	}

	public void publishEmoji(String name, byte[] data, String guildId) {
		Objects.requireNonNull(name, "Name is required");
		Objects.requireNonNull(data, "Image is required");

		Map<String, Object> emoji = new LinkedHashMap<>();
		emoji.put("name", name);
		emoji.put("image", "data:image/png;base64," + Base64.getEncoder().encodeToString(data));

		findEmojiId(name, guildId).ifPresent(emojiId ->
			restOperations.exchange(EMOJI_URL + "/{emojiId}", DELETE, new HttpEntity<>(createBotHeaders()), Void.class, guildId, emojiId));

		try {
			restOperations.exchange(EMOJI_URL, POST, new HttpEntity<>(emoji, createBotHeaders()), Void.class, guildId);
		} catch (Exception exception) {
			log.error("{}'s emoji publication to {} failed", name, guildId, exception);
		}
	}

	private Optional<String> findEmojiId(String name, String guildId) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> emojis = restOperations.exchange(EMOJI_URL, GET, new HttpEntity<>(createBotHeaders()), List.class, guildId).getBody();

		return emojis.stream().filter(emoji -> name.equals(emoji.get("name"))).findFirst().map(emoji -> emoji.get("id").toString());
	}

	@Cacheable("users")
	public User getUser(String token) {
		return restOperations.exchange(BASE_URL + "/users/@me", GET, new HttpEntity<>(createHeaders("Bearer", token)), User.class).getBody();
	}

	@Cacheable("guilds")
	public List<Guild> listGuilds(String token) {
		List<Guild> guilds = restOperations.exchange(BASE_URL + "/users/@me/guilds", GET, new HttpEntity<>(createHeaders("Bearer", token)), new ParameterizedTypeReference<List<Guild>>() {}).getBody();

		return guilds.stream().filter(Guild::isOwner).collect(Collectors.toList());
	}

	public String getAddBotUri() {
		return UriComponentsBuilder.fromUriString("https://discordapp.com/api/oauth2/authorize")
				.queryParam("client_id", clientId)
				.queryParam("permissions", 1073741824)
				.queryParam("redirect_uri", baseUrl)
				.queryParam("scope", "bot")
			.build().toUriString();
	}

	private HttpHeaders createBotHeaders() {
		return createHeaders("Bot", token);
	}

	private HttpHeaders createHeaders(String bearer, String token) {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, String.join(" ", bearer, token));
		headers.add(HttpHeaders.USER_AGENT, "DiscordBot (https://github.com/rsuurd/face-collector, ∞)");

		return headers;
	}

	private static final String BASE_URL = "https://discordapp.com/api";
	private static final String EMOJI_URL = BASE_URL.concat("/guilds/{serverId}/emojis");

	@Data
	public static class User {
		private String id;
		private String username;
		private String avatar;
	}

	@Data
	public static class Guild {
		private String id;
		private String name;
		private String icon;
		private boolean owner;
	}
}
