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
		detector = new HaarCascadeDetector(40);
	}

	public Optional<BufferedImage> extractFace(BufferedImage image) {
		List<DetectedFace> faces = detector.detectFaces(ImageUtilities.createFImage(image));

		return faces.stream().max(Comparator.comparing(DetectedFace::getConfidence)).map(face -> {
			Rectangle faceBounds = face.getBounds();

			return image.getSubimage((int) faceBounds.x, (int) faceBounds.y, (int) faceBounds.width, (int) faceBounds.height);
		});
	}
}
