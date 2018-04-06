package ninja.facecollector.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;

import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
@Service
public class DiscordService {
	private RestOperations restOperations;

	private String token;

	@Autowired
	public DiscordService(RestOperations restOperations, @Value("${discord.token}") String token) {
		this.restOperations = restOperations;

		this.token = token;
	}

	public void publishEmoji(String name, byte[] data, String guildId) {
		Objects.requireNonNull(name, "Name is required");
		Objects.requireNonNull(data, "Image is required");

		Map<String, Object> emoji = new LinkedHashMap<>();
		emoji.put("name", name);
		emoji.put("image", "data:image/png;base64," + Base64.getEncoder().encodeToString(data));

		findEmojiId(name, guildId).ifPresent(emojiId ->
			restOperations.exchange(BASE_URL + "/{emojiId}", DELETE, new HttpEntity<>(createHeaders()), Void.class, guildId, emojiId));

		try {
			restOperations.exchange(BASE_URL, POST, new HttpEntity<>(emoji, createHeaders()), Void.class, guildId);
		} catch (Exception exception) {
			log.error("{}'s emoji publication to {} failed", name, guildId, exception);
		}
	}

	private Optional<String> findEmojiId(String name, String guildId) {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> emojis = restOperations.exchange(BASE_URL, GET, new HttpEntity<>(createHeaders()), List.class, guildId).getBody();

		return emojis.stream().filter(emoji -> name.equals(emoji.get("name"))).findFirst().map(emoji -> emoji.get("id").toString());
	}

	private HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.AUTHORIZATION, String.join(" ", "Bot", token));
		headers.add(HttpHeaders.USER_AGENT, "DiscordBot (https://github.com/rsuurd/face-collector, ∞)");

		return headers;
	}

	private static final String BASE_URL = "https://discordapp.com/api/guilds/{serverId}/emojis";
}
