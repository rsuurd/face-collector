package ninja.facecollector.security;

import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.AbstractClientHttpResponse;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;

final class NimbusUserInfoResponseClient {
	private static final String INVALID_USER_INFO_RESPONSE_ERROR_CODE = "invalid_user_info_response";
	private final GenericHttpMessageConverter genericHttpMessageConverter = new MappingJackson2HttpMessageConverter();

	<T> T getUserInfoResponse(OAuth2UserRequest userInfoRequest, Class<T> returnType) throws OAuth2AuthenticationException {
		ClientHttpResponse userInfoResponse = this.getUserInfoResponse(
			userInfoRequest.getClientRegistration(), userInfoRequest.getAccessToken());
		try {
			return (T) this.genericHttpMessageConverter.read(returnType, userInfoResponse);
		} catch (IOException | HttpMessageNotReadableException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
				"An error occurred reading the UserInfo Success response: " + ex.getMessage(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		}
	}

	<T> T getUserInfoResponse(OAuth2UserRequest userInfoRequest, ParameterizedTypeReference<T> typeReference) throws OAuth2AuthenticationException {
		ClientHttpResponse userInfoResponse = this.getUserInfoResponse(
			userInfoRequest.getClientRegistration(), userInfoRequest.getAccessToken());
		try {
			return (T) this.genericHttpMessageConverter.read(typeReference.getType(), null, userInfoResponse);
		} catch (IOException | HttpMessageNotReadableException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
				"An error occurred reading the UserInfo Success response: " + ex.getMessage(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		}
	}

	private ClientHttpResponse getUserInfoResponse(ClientRegistration clientRegistration,
												   OAuth2AccessToken oauth2AccessToken) throws OAuth2AuthenticationException {
		URI userInfoUri = URI.create(clientRegistration.getProviderDetails().getUserInfoEndpoint().getUri());
		BearerAccessToken accessToken = new BearerAccessToken(oauth2AccessToken.getTokenValue());

		UserInfoRequest userInfoRequest = new UserInfoRequest(userInfoUri, accessToken);
		HTTPRequest httpRequest = userInfoRequest.toHTTPRequest();
		httpRequest.setHeader(HttpHeaders.USER_AGENT, "DiscordBot (https://github.com/rsuurd/face-collector, ∞)");
		httpRequest.setConnectTimeout(30000);
		httpRequest.setReadTimeout(30000);
		HTTPResponse httpResponse;

		try {
			httpResponse = httpRequest.send();
		} catch (IOException ex) {
			throw new AuthenticationServiceException("An error occurred while sending the UserInfo Request: " +
				ex.getMessage(), ex);
		}

		if (httpResponse.getStatusCode() == HTTPResponse.SC_OK) {
			return new NimbusUserInfoResponseClient.NimbusClientHttpResponse(httpResponse);
		}

		UserInfoErrorResponse userInfoErrorResponse;
		try {
			userInfoErrorResponse = UserInfoErrorResponse.parse(httpResponse);
		} catch (ParseException ex) {
			OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE,
				"An error occurred parsing the UserInfo Error response: " + ex.getMessage(), null);
			throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
		}
		ErrorObject errorObject = userInfoErrorResponse.getErrorObject();

		StringBuilder errorDescription = new StringBuilder();
		errorDescription.append("An error occurred while attempting to access the UserInfo Endpoint -> ");
		errorDescription.append("Error details: [");
		errorDescription.append("UserInfo Uri: ").append(userInfoUri.toString());
		errorDescription.append(", Http Status: ").append(errorObject.getHTTPStatusCode());
		if (errorObject.getCode() != null) {
			errorDescription.append(", Error Code: ").append(errorObject.getCode());
		}
		if (errorObject.getDescription() != null) {
			errorDescription.append(", Error Description: ").append(errorObject.getDescription());
		}
		errorDescription.append("]");

		OAuth2Error oauth2Error = new OAuth2Error(INVALID_USER_INFO_RESPONSE_ERROR_CODE, errorDescription.toString(), null);
		throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
	}

	private static class NimbusClientHttpResponse extends AbstractClientHttpResponse {
		private final HTTPResponse httpResponse;
		private final HttpHeaders headers;

		private NimbusClientHttpResponse(HTTPResponse httpResponse) {
			Assert.notNull(httpResponse, "httpResponse cannot be null");
			this.httpResponse = httpResponse;
			this.headers = new HttpHeaders();
			this.headers.setAll(httpResponse.getHeaders());
		}

		@Override
		public int getRawStatusCode() throws IOException {
			return this.httpResponse.getStatusCode();
		}

		@Override
		public String getStatusText() throws IOException {
			return String.valueOf(this.getRawStatusCode());
		}

		@Override
		public void close() {
		}

		@Override
		public InputStream getBody() throws IOException {
			InputStream inputStream = new ByteArrayInputStream(
				this.httpResponse.getContent().getBytes(Charset.forName("UTF-8")));
			return inputStream;
		}

		@Override
		public HttpHeaders getHeaders() {
			return this.headers;
		}
	}
}
