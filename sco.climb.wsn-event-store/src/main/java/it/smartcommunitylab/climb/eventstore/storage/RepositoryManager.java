package it.smartcommunitylab.climb.eventstore.storage;

import it.smartcommunitylab.climb.eventstore.common.Const;
import it.smartcommunitylab.climb.eventstore.common.Utils;
import it.smartcommunitylab.climb.eventstore.model.NodeState;
import it.smartcommunitylab.climb.eventstore.model.WsnEvent;
import it.smartcommunitylab.climb.eventstore.security.DataSetInfo;
import it.smartcommunitylab.climb.eventstore.security.Token;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class RepositoryManager {
	private static final transient Logger logger = LoggerFactory.getLogger(RepositoryManager.class);
	
	private MongoTemplate mongoTemplate;
	private String defaultLang;
	private String contextApiUrl;
	
	public RepositoryManager(MongoTemplate template, String defaultLang, String contextApiUrl) {
		this.mongoTemplate = template;
		this.defaultLang = defaultLang;
		this.contextApiUrl = contextApiUrl;
	}
	
	public String getDefaultLang() {
		return defaultLang;
	}
	
	public String getContextApiUrl() {
		return contextApiUrl;
	}

	public Token findTokenByToken(String token) {
		Query query = new Query(new Criteria("token").is(token));
		Token result = mongoTemplate.findOne(query, Token.class);
		return result;
	}
	
	public List<DataSetInfo> getDataSetInfo() {
		List<DataSetInfo> result = mongoTemplate.findAll(DataSetInfo.class);
		return result;
	}
	
	public DataSetInfo getDataSetInfo(String ownerId) {
		Query query = new Query(new Criteria("ownerId").is(ownerId));
		DataSetInfo result = mongoTemplate.findOne(query, DataSetInfo.class);
		return result;
	}
	
	public void saveDataSetInfo(DataSetInfo dataSetInfo) {
		Query query = new Query(new Criteria("ownerId").is(dataSetInfo.getOwnerId()));
		DataSetInfo appInfoDB = mongoTemplate.findOne(query, DataSetInfo.class);
		if (appInfoDB == null) {
			mongoTemplate.save(dataSetInfo);
		} else {
			Update update = new Update();
			update.set("password", dataSetInfo.getPassword());
			update.set("token", dataSetInfo.getToken());
			mongoTemplate.updateFirst(query, update, DataSetInfo.class);
		}
	}
	
	public void saveAppToken(String name, String token) {
		Query query = new Query(new Criteria("name").is(name));
		Token tokenDB = mongoTemplate.findOne(query, Token.class);
		if(tokenDB == null) {
			Token newToken = new Token();
			newToken.setToken(token);
			newToken.setName(name);
			newToken.getPaths().add("/api");
			mongoTemplate.save(newToken);
		} else {
			Update update = new Update();
			update.set("token", token);
			mongoTemplate.updateFirst(query, update, Token.class);
		}
	}
	
	// EVENTS
	
	public void addEvent(WsnEvent event) {
		Date actualDate = new Date();
		event.setCreationDate(actualDate);
		event.setLastUpdate(actualDate);
		mongoTemplate.save(event);
	}
	
	public void removeEvents(String ownerId, String routeId, Date dateFrom, Date dateTo, int eventType) {
		Criteria criteria = new Criteria("ownerId").is(ownerId).and("routeId").is(routeId);
		if(eventType > 0) {
			criteria = criteria.and("eventType").is(eventType);
		}
		Query query = new Query(criteria);
		query.addCriteria(new Criteria().andOperator(
			Criteria.where("timestamp").lte(dateTo),
			Criteria.where("timestamp").gte(dateFrom)));
		if(logger.isDebugEnabled()) {
			logger.debug("removeEvents:" + query.toString());
		}
		mongoTemplate.findAllAndRemove(query, WsnEvent.class);
	}
	
	public List<WsnEvent> searchEvents(String ownerId, String routeId, Date dateFrom, Date dateTo, 
			List<Integer> eventTypeList, List<String> nodeIdList) {
		Criteria criteria = new Criteria("ownerId").is(ownerId);
		if(Utils.isNotEmpty(routeId)) {
			criteria = criteria.and("routeId").is(routeId);
		}
		if(eventTypeList.size() > 0) {
			criteria = criteria.and("eventType").in(eventTypeList);
		}
		if(nodeIdList.size() > 0) {
			criteria = criteria.and("wsnNodeId").in(nodeIdList);
		}
		Query query = new Query(criteria);
		query.addCriteria(new Criteria().andOperator(
			Criteria.where("timestamp").lte(dateTo),
			Criteria.where("timestamp").gte(dateFrom)));
		query.with(new Sort(Sort.Direction.ASC, "timestamp"));
		if(logger.isDebugEnabled()) {
			logger.debug("searchEvents:" + query.toString());
		}
		List<WsnEvent> result = mongoTemplate.find(query, WsnEvent.class);
		if(logger.isDebugEnabled()) {
			logger.debug("searchEvents:" + result.size());
		}
		return result;
	}
	
	public List<NodeState> checkNodes(String ownerId, String routeId, Date dateFrom, Date dateTo) {
		List<Integer> eventTypeList = new ArrayList<Integer>();
		eventTypeList.add(Const.NODE_IN_RANGE);
		eventTypeList.add(Const.NODE_AT_DESTINATION);
		eventTypeList.add(Const.BATTERY_STATE);
		Criteria criteria = new Criteria("ownerId").is(ownerId);
		if(Utils.isNotEmpty(routeId)) {
			criteria = criteria.and("routeId").is(routeId);
		}
		if(eventTypeList.size() > 0) {
			criteria = criteria.and("eventType").in(eventTypeList);
		}
		Query query = new Query(criteria);
		query.addCriteria(new Criteria().andOperator(
			Criteria.where("timestamp").lte(dateTo),
			Criteria.where("timestamp").gte(dateFrom)));
		query.with(new Sort(Sort.Direction.ASC, "timestamp"));
		if(logger.isDebugEnabled()) {
			logger.debug("checkNodes:" + query.toString());
		}
		List<WsnEvent> events = mongoTemplate.find(query, WsnEvent.class);
		Map<String, WsnEvent> nodeInRangeMap = new HashMap<String, WsnEvent>();
		Map<String, WsnEvent> nodeAtDestinationMap = new HashMap<String, WsnEvent>();
		Map<String, WsnEvent> batteryLowMap = new HashMap<String, WsnEvent>();
		List<String> passengerList = new ArrayList<String>();
		List<NodeState> nodeStateList = new ArrayList<NodeState>();
		for(WsnEvent event : events) {
			String passengerId = (String) event.getPayload().get("passengerId");
			if(event.getEventType() == Const.NODE_IN_RANGE) {
				nodeInRangeMap.put(passengerId, event);
			} else if(event.getEventType() == Const.NODE_AT_DESTINATION) {
				nodeAtDestinationMap.put(passengerId, event);
			} else if(event.getEventType() == Const.BATTERY_STATE) {
				batteryLowMap.put(passengerId, event);
			} else {
				continue;
			}
			if(!passengerList.contains(passengerId)) {
				passengerList.add(passengerId);
			}
		}
		for(String passengerId : passengerList) {
			NodeState nodeState = new NodeState();
			nodeState.setPassengerId(passengerId);
			WsnEvent event = batteryLowMap.get(passengerId);
			if(event != null) {
				nodeState.setBatteryLevel((Integer) event.getPayload().get("batteryLevel"));
				nodeState.setBatteryVoltage((Integer) event.getPayload().get("batteryVoltage"));
			} else {
				nodeState.setBatteryLevel(0);
				nodeState.setBatteryVoltage(0);
			}
			event = nodeInRangeMap.get(passengerId);
			if(event != null) {
				nodeState.setWsnNodeId(event.getWsnNodeId());
				if(nodeAtDestinationMap.get(passengerId) != null) {
					nodeState.setManualCheckIn(false);
				}
			} else {
				if(nodeAtDestinationMap.get(passengerId) != null) {
					event = nodeAtDestinationMap.get(passengerId);
					nodeState.setWsnNodeId(event.getWsnNodeId());
					nodeState.setManualCheckIn(true);
				}
			}
			nodeStateList.add(nodeState);
		}
		if(logger.isDebugEnabled()) {
			logger.debug("checkNodes:" + nodeStateList.size());
		}
		return nodeStateList;
	}

}
