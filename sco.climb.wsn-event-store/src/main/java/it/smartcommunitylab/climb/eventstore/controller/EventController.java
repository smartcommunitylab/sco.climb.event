/**
 *    Copyright 2015 Fondazione Bruno Kessler - Trento RISE
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package it.smartcommunitylab.climb.eventstore.controller;

import it.smartcommunitylab.climb.eventstore.common.Utils;
import it.smartcommunitylab.climb.eventstore.exception.InvalidParametersException;
import it.smartcommunitylab.climb.eventstore.exception.UnauthorizedException;
import it.smartcommunitylab.climb.eventstore.model.WsnEvent;
import it.smartcommunitylab.climb.eventstore.storage.DataSetSetup;
import it.smartcommunitylab.climb.eventstore.storage.RepositoryManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.google.common.collect.Lists;


@Controller
public class EventController {
	private static final transient Logger logger = LoggerFactory.getLogger(EventController.class);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
			
	@Autowired
	private RepositoryManager storage;

	@Autowired
	private DataSetSetup dataSetSetup;
	
	@RequestMapping(value = "/api/event/{ownerId}", method = RequestMethod.GET)
	public @ResponseBody List<WsnEvent> searchEvents(@PathVariable String ownerId, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		if(!Utils.validateAPIRequest(request, dataSetSetup, storage)) {
			throw new UnauthorizedException("Unauthorized Exception: token not valid");
		}
		List<WsnEvent> result = Lists.newArrayList();
		String routeId = request.getParameter("routeId");
		String dateFromString = request.getParameter("dateFrom");
		String dateToString = request.getParameter("dateTo");
		String[] eventTypeArray = request.getParameterValues("eventType[]");
		String[] nodeIdArray = request.getParameterValues("nodeId[]");
 		try {
			List<Integer> eventTypeList = Lists.newArrayList();
			if(eventTypeArray != null) {
				for(String eventTypeString : eventTypeArray) {
					Integer eventType = Integer.valueOf(eventTypeString);
					eventTypeList.add(eventType);
				}
			}
			List<Integer> nodeIdList = Lists.newArrayList();
			if(nodeIdArray != null) {
				for(String nodeIdString : nodeIdArray) {
					Integer nodeId = Integer.valueOf(nodeIdString);
					nodeIdList.add(nodeId);
				}
			}
			Date dateFrom = sdf.parse(dateFromString);
			Date dateTo = sdf.parse(dateToString);
			result = storage.searchEvents(ownerId, routeId, dateFrom, dateTo, eventTypeList, nodeIdList);
			if(logger.isInfoEnabled()) {
				logger.info(String.format("searchEvents[%s]:%d", ownerId, result.size()));
			}			
		} catch (Exception e) {
			throw new InvalidParametersException("Invalid query parameters:" + e.getMessage());
		}
		return result;
	}
	
	@RequestMapping(value = "/api/event/{ownerId}", method = RequestMethod.POST)
	public @ResponseBody String addEvents(@RequestBody List<WsnEvent> events, @PathVariable String ownerId, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		if(!Utils.validateAPIRequest(request, dataSetSetup, storage)) {
			throw new UnauthorizedException("Unauthorized Exception: token not valid");
		}
		for(WsnEvent event : events) {
			event.setOwnerId(ownerId);
			storage.addEvent(event);
		}
		if(logger.isInfoEnabled()) {
			logger.info(String.format("addEvents[%s]:%d", ownerId, events.size()));
		}
		return "OK";
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Map<String,String> handleError(HttpServletRequest request, Exception exception) {
		return Utils.handleError(exception);
	}
	
}
