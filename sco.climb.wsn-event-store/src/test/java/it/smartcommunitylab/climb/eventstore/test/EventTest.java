package it.smartcommunitylab.climb.eventstore.test;

import it.smartcommunitylab.climb.eventstore.model.WsnEvent;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

public class EventTest {
	private static final transient Logger logger = LoggerFactory.getLogger(EventTest.class);
	
	public static final String networkBaseUrl = "http://localhost:8080/wsn-event-store/api/event/";
	public static final String ownerId = "TEST";
	
	public ObjectMapper objectMapper;
	public HttpClient httpClient;
	
	@Before
	public void setUp() throws Exception {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		httpClient = new HttpClient();
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	private void postEvents(List<WsnEvent> events) throws Exception {
		StringWriter json = new StringWriter();
		objectMapper.writeValue(json, events);
		StringRequestEntity requestEntity = new StringRequestEntity(json.toString(),
				"application/json", "UTF-8");
		String url = networkBaseUrl + ownerId;
		PostMethod postMethod = new PostMethod(url);
		postMethod.addRequestHeader("Content-Type", "application/json;charset=UTF-8");
		postMethod.addRequestHeader("X-ACCESS-TOKEN", "L2MEq8WPbTAIT134");
		postMethod.setRequestEntity(requestEntity);
		int statusCode = httpClient.executeMethod(postMethod);
		if(logger.isInfoEnabled()) {
			logger.info("addEvents:" + statusCode);
		}
	}
	
	@Test
	public void addEvents() throws Exception {
		for(int i=0; i<1000; i++) {
			List<WsnEvent> events = Lists.newArrayList();
			for(int j=0; j<1000; j++) {
				WsnEvent event = new WsnEvent();
				event.setOwnerId(ownerId);
				event.setRouteId("ROUTE66");
				event.setWsnNodeId(1);
				if ( j % 2 == 0 ) {
					event.setEventType(101);
				} else {
					event.setEventType(201);
				}
				event.getPayload().put("state", new Integer(1));
				event.setTimestamp(new Date());
				events.add(event);
			}
			postEvents(events);
		}
	}
}
