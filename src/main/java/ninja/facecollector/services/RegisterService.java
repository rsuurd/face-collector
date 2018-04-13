package ninja.facecollector.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.net.URLEncoder.encode;
import static ninja.facecollector.FaceCollector.STREAMER_PREFIX;

@Slf4j
@Service
public class RegisterService {
	private String clientId;
	private String baseUrl;

	private StringRedisTemplate redisTemplate;

	private CollectService collectService;

	@Autowired
	public RegisterService(@Value("${discord.clientId}") String clientId, @Value("${baseUrl}") String baseUrl, StringRedisTemplate redisTemplate) {
		this.clientId = clientId;

		this.baseUrl = baseUrl;

		this.redisTemplate = redisTemplate;
	}

	public String authorizeFaceCollector(String streamer) {
		try {
			String registrationId = UUID.randomUUID().toString();

			redisTemplate.opsForValue().set(registrationId, streamer, 1, TimeUnit.DAYS);

			return UriComponentsBuilder.fromUriString("https://discordapp.com/api/oauth2/authorize")
				.queryParam("client_id", clientId)
				.queryParam("permissions", 1073741824)
				.queryParam("redirect_uri", encode(String.join("/", baseUrl, "joined"), "UTF-8"))
				.queryParam("response_type", "code")
				.queryParam("state", registrationId)
				.queryParam("scope", "bot").build().toString();
		} catch (IOException exception) {
			throw new RuntimeException("could not authorize");
		}
	}

	public void registerGuildId(String registrationId, String guildId) {
		String streamer = findStreamerByRegistrationId(registrationId).orElseThrow(() -> new RuntimeException("No registration found"));

		redisTemplate.boundSetOps(STREAMER_PREFIX.concat(streamer)).add(guildId);
		redisTemplate.delete(registrationId);

		log.info("Faces collected from {} will be sent to {}", streamer, guildId);
	}

	public Optional<String> findStreamerByRegistrationId(String registrationId) {
		return (redisTemplate.hasKey(registrationId)) ? Optional.of(redisTemplate.opsForValue().get(registrationId)) : Optional.empty();
	}
}
