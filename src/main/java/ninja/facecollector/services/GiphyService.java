package ninja.facecollector.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Service
public class GiphyService {
	private RestOperations restOperations;

	private String token;

	@Autowired
	public GiphyService(RestOperations restOperations, @Value("${giphy.token}") String token) {
		this.restOperations = restOperations;

		this.token = token;
	}

	public String random(String tag) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://api.giphy.com/v1/gifs/random")
			.queryParam("api_key", token);

		Optional.ofNullable(tag).ifPresent(t -> uriBuilder.queryParam("tag", t));

		ResponseEntity<GiphyResponse> response = restOperations.getForEntity(uriBuilder.build().toUri(), GiphyResponse.class);

		return response.getBody().getData().getEmbedUrl();
	}

	@Getter
	private static class GiphyResponse {
		private Data data;
	}

	@Getter
	private static class Data {
		@JsonProperty("embed_url")
		private String embedUrl;
	}
}
