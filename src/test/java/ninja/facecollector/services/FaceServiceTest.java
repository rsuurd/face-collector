package ninja.facecollector.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FaceServiceTest {
	@Mock
	private HaarCascadeDetector detector;

	@InjectMocks
	private FaceService service;

	@Test
	public void shouldExtractFace() {
		DetectedFace faceA = new DetectedFace(new Rectangle(0, 0, 10, 10), null, 5);
		DetectedFace faceB = new DetectedFace(new Rectangle(100, 100, 20, 20), null, 10);

		when(detector.detectFaces(any())).thenReturn(Arrays.asList(faceA, faceB));

		Optional<BufferedImage> maybeFace = service.extractFace(new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB));

		assertThat(maybeFace).isNotEmpty();
		maybeFace.ifPresent(face -> {
			assertThat(face.getWidth()).isEqualTo(20);
			assertThat(face.getHeight()).isEqualTo(20);
		});
	}
}
