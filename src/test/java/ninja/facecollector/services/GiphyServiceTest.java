package ninja.facecollector.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GiphyServiceTest {
	@Mock
	private RestOperations restOperations;

	private GiphyService service;

	@Before
	public void createService() {
		service = new GiphyService(restOperations, "token");
	}

	@Test
	public void shouldRetrieveRandomImage() {
		when(restOperations.getForEntity(any(), any()))
			.thenReturn(new ResponseEntity<>(new GiphyService.GiphyResponse(new GiphyService.Data("http://example.com")), HttpStatus.OK));

		assertThat(service.random("test")).isEqualTo("http://example.com");
	}
}