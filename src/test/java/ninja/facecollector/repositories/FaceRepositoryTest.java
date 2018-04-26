package ninja.facecollector.repositories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@RunWith(SpringRunner.class)
public class FaceRepositoryTest {
	@Autowired
	private TestEntityManager entityManager;

	@Autowired
	private FaceRepository faceRepository;

	@Before
	public void saveFace() {
		entityManager.persist(new Face("streamer", "guildId"));
		entityManager.flush();
	}

	@Test
	public void shouldFindStreamer() {
		assertThat(faceRepository.findByStreamerAndGuildId("streamer", "guildId")).isPresent();
	}

	@Test
	public void shouldFindStreamerNames() {
		assertThat(faceRepository.findStreamersByGuildId("guildId")).containsExactly("streamer");
	}
}