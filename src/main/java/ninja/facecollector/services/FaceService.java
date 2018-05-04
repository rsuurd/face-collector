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

import org.openimaj.image.ImageUtilities;
import org.openimaj.image.processing.face.detection.DetectedFace;
import org.openimaj.image.processing.face.detection.HaarCascadeDetector;
import org.openimaj.math.geometry.shape.Rectangle;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class FaceService {
	private HaarCascadeDetector detector;

	public FaceService() {
		this(new HaarCascadeDetector(40));
	}

	FaceService(HaarCascadeDetector detector) {
		this.detector = detector;
	}

	public Optional<BufferedImage> extractFace(BufferedImage image) {
		List<DetectedFace> faces = detector.detectFaces(ImageUtilities.createFImage(image));

		return faces.stream().max(Comparator.comparing(DetectedFace::getConfidence)).map(face -> {
			Rectangle faceBounds = face.getBounds();

			return image.getSubimage((int) faceBounds.x, (int) faceBounds.y, (int) faceBounds.width, (int) faceBounds.height);
		});
	}
}
