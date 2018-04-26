package ninja.facecollector.services;

import ninja.facecollector.repositories.Face;
import ninja.facecollector.repositories.FaceRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CollectServiceTest {
	@Mock
	private FaceRepository faceRepository;

	@Mock
	private DiscordService discordService;

	@Mock
	private FaceService faceService;

	@Mock
	private TwitchService twitchService;

	@InjectMocks
	private CollectService service;

	@Test
	public void shouldCollectStreamers() throws IOException {
		URL imageUrl = new ClassPathResource("/illmatic.jpg").getURL();

		BufferedImage image = ImageIO.read(imageUrl);

		when(faceRepository.findAll()).thenReturn(Collections.singletonList(new Face("streamer", "guildId")));
		when(twitchService.getStream("streamer")).thenReturn(Optional.of(
			new TwitchService.Stream(300, new TwitchService.Preview(imageUrl.toString(), imageUrl.toString()))));
		when(faceService.extractFace(any())).thenReturn(Optional.of(image));

		service.collect();

		verify(discordService).publishEmoji(eq("streamer"), any(), eq("guildId"));
	}

	@Test
	public void shouldNotCollectStreamerWithoutGuilds() {
		when(faceRepository.findAll()).thenReturn(Collections.singletonList(new Face("streamer")));

		service.collect();

		verifyZeroInteractions(twitchService, faceService, discordService);
	}
}
