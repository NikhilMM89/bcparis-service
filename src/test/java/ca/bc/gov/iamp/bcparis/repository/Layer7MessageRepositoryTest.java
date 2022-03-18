package ca.bc.gov.iamp.bcparis.repository;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.iamp.bcparis.exception.layer7.Layer7RestException;
import ca.bc.gov.iamp.bcparis.model.message.Layer7Message;

@RunWith(MockitoJUnitRunner.class)
public class Layer7MessageRepositoryTest {

	@InjectMocks
	private Layer7MessageRepository repo = new Layer7MessageRepository();
	
	@Mock
	private RestTemplate rest;
	
	@Before
    public void initMocks() throws NoSuchFieldException, SecurityException{

		ReflectionTestUtils.setField(repo,"messageEndpoint", "messageEndpoint");
		ReflectionTestUtils.setField(repo,"path", "path");
		ReflectionTestUtils.setField(repo,"username", "mock_username");
		ReflectionTestUtils.setField(repo,"password", "mock_password");
    }
	
	@Test
	public void request_success() {

        Mockito.when(rest.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.eq(String.class) ))
    			.thenReturn(new ResponseEntity<>("OK", HttpStatus.OK));
		
		final String resp = repo.sendMessage(new Layer7Message());
		
		Assert.assertNotNull(resp);
	}
	
	@Test(expected=Layer7RestException.class)
	public void request_exception() {

        Mockito.when(rest.postForEntity(Mockito.anyString(), Mockito.any(HttpEntity.class), Mockito.eq(String.class) ))
    			.thenReturn(new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND));
		
		final String resp = repo.sendMessage(new Layer7Message());
		
		Assert.assertNotNull(resp);
	}
	
}
