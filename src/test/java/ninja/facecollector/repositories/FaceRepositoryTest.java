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