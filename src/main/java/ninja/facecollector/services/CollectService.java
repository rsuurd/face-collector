package ninja.facecollector.services;

import lombok.extern.slf4j.Slf4j;
import ninja.facecollector.repositories.FaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.stream.Stream;

@Slf4j
@Service
public class CollectService {
	private DiscordService discordService;
	private FaceService faceService;
	private TwitchService twitchService;

	private FaceRepository faceRepository;

	@Autowired
	public CollectService(DiscordService discordService, FaceService faceService, TwitchService twitchService, FaceRepository faceRepository) {
		this.discordService = discordService;
		this.faceService = faceService;
		this.twitchService = twitchService;

		this.faceRepository = faceRepository;
	}
	
	@Scheduled(fixedRate = 300000L)
	public void collect() {
		faceRepository.findAll().forEach(face -> {
			String name = face.getStreamer();
			String[] guildIds = face.getGuildIds().toArray(new String[] {});

			log.info("collect {}'s face and push to {}", name, guildIds);

			twitchService.getStream(name).ifPresent(stream -> {
				try {
					BufferedImage preview = ImageIO.read(getPreviewUrl(stream));

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
