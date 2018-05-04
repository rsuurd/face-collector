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
