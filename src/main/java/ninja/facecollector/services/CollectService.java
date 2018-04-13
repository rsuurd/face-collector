package ninja.facecollector.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static ninja.facecollector.FaceCollector.STREAMER_PREFIX;

@Slf4j
@Service
public class CollectService {
	private DiscordService discordService;
	private FaceService faceService;
	private TwitchService twitchService;

	private StringRedisTemplate redisTemplate;

	@Autowired
	public CollectService(DiscordService discordService, FaceService faceService, TwitchService twitchService, StringRedisTemplate redisTemplate) {
		this.discordService = discordService;
		this.faceService = faceService;
		this.twitchService = twitchService;

		this.redisTemplate = redisTemplate;
	}
	
	@Scheduled(fixedRate = 300000L)
	public void collect() {
		Set<String> streamers = redisTemplate.keys(STREAMER_PREFIX.concat("*"));

		streamers.forEach(streamer -> {

			String name = streamer.substring(STREAMER_PREFIX.length());
			Set<String> guildIds = redisTemplate.boundSetOps(streamer).members();

			collect(name, guildIds.toArray(new String[] {}));
		});
	}

	public void collect(String name, String... guildIds) {
		log.info("collect {}'s face and push to {}", name, guildIds);

		Optional<TwitchService.Stream> maybeStream = twitchService.getStream(name);

		if (maybeStream.isPresent()) {
			try {
				BufferedImage preview = ImageIO.read(getPreviewUrl(maybeStream.get()));

				Optional<BufferedImage> maybeFace = faceService.extractFace(preview);

				if (maybeFace.isPresent()) {
					try {
						ByteArrayOutputStream out = new ByteArrayOutputStream();

						ImageIO.write(maybeFace.get(), "png", out);

						byte[] data = out.toByteArray();

						Stream.of(guildIds).forEach(guildId ->
							discordService.publishEmoji(name, data, guildId)
						);
					} catch (IOException exception) {
						log.error("could not create emoji from {}'s face", name, exception);
					}
				} else {
					log.warn("could not extract a face for {}", name);
				}
			} catch (Exception exception) {
				log.error("could not collect {}'s face", name, exception);
			}
		} else {
			log.warn("{} is offline", name);
		}
	}

	private URL getPreviewUrl(TwitchService.Stream stream) throws MalformedURLException {
		TwitchService.Preview preview = stream.getPreview();

		String url;

		if (!StringUtils.isEmpty(preview.getTemplate()) && (stream.getVideoHeight() != 0)) {
			url = preview.getTemplate()
				.replace("{width}", Integer.toString(stream.getVideoWidth()))
				.replace("{height}", Integer.toString(stream.getVideoHeight()));
		} else {
			url = preview.getLarge();
		}

		return new URL(url);
	}
}
