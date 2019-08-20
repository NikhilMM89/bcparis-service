package ca.bc.gov.iamp.bcparis.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.iamp.bcparis.exception.icbc.ICBCRestException;
import ca.bc.gov.iamp.bcparis.repository.query.IMSRequest;
import ca.bc.gov.iamp.bcparis.repository.query.IMSResponse;

@Repository
public class ICBCRestRepository {

	private final Logger log = LoggerFactory.getLogger(ICBCRestRepository.class);
	
	@Value("${endpoint.icbc.rest}")
	private String icbcUrl;
	
	@Value("${endpoint.icbc.rest.header.imsUserId}")
	private String imsUserId;
	
	@Value("${endpoint.icbc.rest.header.imsCredential}")
	private String imsCredential;
	
	@Value("${endpoint.icbc.rest.header.auditTransactionId}")
	private String auditTransactionId;
	
	@Value("${endpoint.icbc.rest.path.transaction}")
	private String pathTransaction;
	
	@Value("${endpoint.icbc.rest.header.username}")
	private String username;
	
	@Value("${endpoint.icbc.rest.header.password}")
	private String password;
	
	@Autowired
	private RestTemplate restTemplate;
	
	@NewSpan("icbc")
	public String requestDetails(IMSRequest ims) {
		try {
			final String URL = icbcUrl + pathTransaction;
			log.info(String.format("Calling ICBC Rest Service. URL=%s, IMS=%s", URL, ims.imsRequest ));
			
			HttpEntity<?> httpEntity = new HttpEntity<IMSRequest>(ims,  getHeaders(username, password));
			
			ResponseEntity<IMSResponse> response = restTemplate.postForEntity(URL, httpEntity, IMSResponse.class);
			
			handleResponse(response);
			
			return response.getBody().getImsResponse();
		}catch (HttpServerErrorException e) {
			return e.getResponseBodyAsString();
		}
		catch (Exception e) {
			throw new ICBCRestException("Exception to call ICBC Rest Service", e);
		}
	}
	
	private HttpHeaders getHeaders(final String username, final String password) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("imsUserId", imsUserId);
		headers.add("imsCredential", imsCredential);
		headers.add("auditTransactionId", auditTransactionId);
		headers.setBasicAuth(username, password);
		return headers;
	}
	
	private void handleResponse(ResponseEntity<IMSResponse> response) throws Exception {
		if( response.getStatusCode() == HttpStatus.OK) {
			log.info(String.format("ICBC Rest service response=%s", response.getStatusCode()));
			log.debug(String.format("Body=%s", response.getBody()));
		}else {
			String message = String.format("Status code not expected during the ICBC Rest Service request. Status=%s. Body=%s", 
					response.getStatusCodeValue(), response.getBody());
			throw new ICBCRestException(message, null);			
		}
	}

}
