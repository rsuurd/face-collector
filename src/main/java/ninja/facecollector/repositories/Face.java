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

	public Face withGuildId(String guildId) {
		guildIds.add(guildId);

		return this;
	}
}
