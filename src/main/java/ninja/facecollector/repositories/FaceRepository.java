package ninja.facecollector.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FaceRepository extends JpaRepository<Face, String> {
	@Query("from Face where streamer = :streamer and :guildId member of guildIds")
	Optional<Face> findByStreamerAndGuildId(@Param("streamer") String streamer, @Param("guildId") String guildId);

	@Query("select streamer from Face where :guildId member of guildIds")
	List<String> findStreamersByGuildId(@Param("guildId") String guildId);
}
