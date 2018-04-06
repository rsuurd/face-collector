package ninja.facecollector.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static ninja.facecollector.FaceCollector.STREAMER_PREFIX;

@Slf4j
@Service
public class RegisterService {
	private String clientId;
	private String baseUrl;

	private StringRedisTemplate redisTemplate;

	@Autowired
	public RegisterService(@Value("${discord.clientId}") String clientId, @Value("${baseUrl}") String baseUrl, StringRedisTemplate redisTemplate) {
		this.clientId = clientId;

		this.baseUrl = baseUrl;

		this.redisTemplate = redisTemplate;
	}

	// Username: face collector#5950
	// Token: NDMwNjcxMDIwODY5ODEyMjM0.DaT3Gg.X2ClAfCSHPJfyXfM-NyPPGV6sik
	public String authorizeFaceCollector(String streamer) {
		String registrationId = UUID.randomUUID().toString();

		redisTemplate.opsForValue().set(registrationId, streamer, 1, TimeUnit.DAYS);

		return UriComponentsBuilder.fromUriString("https://discordapp.com/api/oauth2/authorize")
			.queryParam("client_id", clientId)
			.queryParam("permissions", 1073741824)
			.queryParam("redirect_uri", String.join("/", baseUrl, "joined"))
			.queryParam("response_type", "code")
			.queryParam("state", registrationId)
			.queryParam("scope", "bot").build().toString();
	}

	public void registerGuildId(String registrationId, String guildId) {
		if (redisTemplate.hasKey(registrationId)) {
			String streamer = redisTemplate.opsForValue().get(registrationId);

			redisTemplate.boundSetOps(STREAMER_PREFIX.concat(streamer)).add(guildId);
			redisTemplate.delete(registrationId);

			log.info("Faces collected from {} will be sent to {}", streamer, guildId);
		} else {
			throw new RuntimeException("No registration found");
		}
	}
}
