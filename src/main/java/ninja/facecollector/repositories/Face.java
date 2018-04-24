package ninja.facecollector.repositories;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static lombok.AccessLevel.PROTECTED;

@Data
@Entity
@Table(name = "faces")
@NoArgsConstructor(access = PROTECTED)
public class Face {
	@Id
	private String streamer;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<String> guildIds;

	public Face(String streamer, String... guildIds) {
		this.streamer = streamer;

		this.guildIds = Stream.of(guildIds).collect(Collectors.toSet());
	}
}
