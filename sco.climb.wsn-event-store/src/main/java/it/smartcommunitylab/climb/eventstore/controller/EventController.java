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
import it.smartcommunitylab.climb.eventstore.model.NodeState;
import it.smartcommunitylab.climb.eventstore.model.WsnEvent;
import it.smartcommunitylab.climb.eventstore.storage.DataSetSetup;
import it.smartcommunitylab.climb.eventstore.storage.RepositoryManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Lists;


@Controller
public class EventController {
	private static final transient Logger logger = LoggerFactory.getLogger(EventController.class);
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	@Autowired
	@Value("${log.upload.dir}")
	private String logUploadDir;
			
	@Autowired
	private RepositoryManager storage;

	@Autowired
	private DataSetSetup dataSetSetup;
	
	@RequestMapping(value = "/report/context/url", method = RequestMethod.GET)
	public @ResponseBody String getContextApiUrl() {
		return "{\"url\":\"" + storage.getContextApiUrl() + "\"}";
	}
	
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
			List<String> nodeIdList = Lists.newArrayList();
			if(nodeIdArray != null) {
				for(String nodeIdString : nodeIdArray) {
					nodeIdList.add(nodeIdString);
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
		return "{\"status\":\"OK\"}";
	}

	@RequestMapping(value = "/api/log/upload/{ownerId}", method = RequestMethod.POST)
	public @ResponseBody String uploadLog(@RequestParam("file") MultipartFile file,
			@RequestParam("name") String name, @PathVariable String ownerId,
			HttpServletRequest request) throws Exception {
		if(!Utils.validateAPIRequest(request, dataSetSetup, storage)) {
			throw new UnauthorizedException("Unauthorized Exception: token not valid");
		}
		if(logger.isInfoEnabled()) {
			logger.info("uploadLog:" + name);
		}
		if (!file.isEmpty()) {
			if(logger.isInfoEnabled()) {
				logger.info("upload stream:" + file);
			}
			BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(
					new File(logUploadDir + "/" + name)));
			FileCopyUtils.copy(file.getInputStream(), stream);
			stream.close();
		}
		return "{\"status\":\"OK\"}";
	}
	
	@RequestMapping(value = "/api/check/{ownerId}", method = RequestMethod.GET)
	public @ResponseBody List<NodeState> checkNodes(@PathVariable String ownerId, 
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		String routeId = request.getParameter("routeId");
		String dateFromString = request.getParameter("dateFrom");
		String dateToString = request.getParameter("dateTo");
		List<NodeState> result = Lists.newArrayList();
 		try {
			Date dateFrom = sdf.parse(dateFromString);
			Date dateTo = sdf.parse(dateToString);
			result = storage.checkNodes(ownerId, routeId, dateFrom, dateTo);
			if(logger.isInfoEnabled()) {
				logger.info(String.format("checkNodes[%s]:%d", ownerId, result.size()));
			}			
		} catch (Exception e) {
			throw new InvalidParametersException("Invalid query parameters:" + e.getMessage());
		}
 		return result;
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	@ResponseBody
	public Map<String,String> handleError(HttpServletRequest request, Exception exception) {
		return Utils.handleError(exception);
	}
	
}
