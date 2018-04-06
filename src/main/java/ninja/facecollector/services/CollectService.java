package ninja.facecollector.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
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
import java.util.List;
import java.util.Set;

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
			BoundListOperations<String, String> boundListOps = redisTemplate.boundListOps(streamer);

			String name = streamer.substring(STREAMER_PREFIX.length());
			List<String> guildIds = boundListOps.range(0, boundListOps.size());

			log.info("collect {}'s face and push to {}", name, guildIds);

			twitchService.getStream(name).ifPresent(stream -> {
				try {
					BufferedImage preview = ImageIO.read(getPreviewUrl(stream));

					faceService.extractFace(preview).ifPresent(faceImage -> {
						try {
							ByteArrayOutputStream out = new ByteArrayOutputStream();

							ImageIO.write(faceImage, "png", out);

							byte[] data = out.toByteArray();

							guildIds.forEach(guildId ->
								discordService.publishEmoji(name, data, guildId)
							);
						} catch (IOException exception) {
							log.error("could not create emoji from {}'s face", name, exception);
						}
					});
				} catch (IOException exception) {
					log.error("could not collect {}'s face", name, exception);
				}
			});
		});
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
