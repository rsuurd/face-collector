package ninja.facecollector.repositories;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Table;

import static lombok.AccessLevel.PROTECTED;

@Data
@Entity
@Table(name = "authorized_clients")
@NoArgsConstructor(access = PROTECTED)
public class AuthorizedClient {
}
