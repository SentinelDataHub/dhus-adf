/*
 * Data Hub Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015 GAEL Systems
 *
 * This file is part of DHuS software sources.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.serco.dhus.server.http.webapp.wps.controller;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import eu.serco.dhus.config.ConfigurationManager;
import eu.serco.dhus.processor.ProcessorService;

@Controller
public class WpsAdfSearchController {
	private static Logger logger = LogManager.getLogger();
	 
	@PreAuthorize("hasRole('ROLE_SEARCH')")
	@RequestMapping(value = "/auxiliaries", method = { RequestMethod.GET })
	public ResponseEntity<?> getAuxiliaries(@RequestParam(value="type", defaultValue="") String type, @RequestParam(value="filename", defaultValue="")String filename) {

		try {
			 ProcessorService processor = ProcessorService.getInstance();
		      Object result = processor.process(type, filename);
		      if(result == null)
		    	  return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
							HttpStatus.FORBIDDEN);    	  
		      else
		    	  return new ResponseEntity<>(result, HttpStatus.OK);
			
		} catch (Exception e) {

			logger.error(" Failed to retrieve ADF list");
			e.printStackTrace();
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
					HttpStatus.UNAUTHORIZED);
		}

	}
	
	@RequestMapping(value = "/auxiliaries/download", method = { RequestMethod.GET })
	public ResponseEntity<?> downloadAuxiliaries(@RequestParam(value="uuid", defaultValue="") String uuid, @RequestParam(value="filename", defaultValue="")String filename) {

		try {
			String hashedString=ConfigurationManager.getHashedConnectionString();
			
						
	    	String urlString = ConfigurationManager.getExternalDHuSHost()+"odata/v1/Products('"+uuid+"')/$value";
	    	logger.info("urlString:::: " + urlString);
			URL url = new URL(urlString);
	    	HttpURLConnection conn =  (HttpURLConnection)url.openConnection();
	    	conn.setRequestProperty("Authorization", "Basic " + hashedString);
	    	InputStream is = conn.getInputStream();
	    	InputStreamResource isr = new InputStreamResource(is);
	    	HttpHeaders httpHeaders = new HttpHeaders();
	    	httpHeaders.add("Authorization", "Basic " + hashedString);		    	
	    	httpHeaders.add("Content-disposition", "attachment; filename="+filename);
	    	httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);	    	
	    	    
	    	return new ResponseEntity<>(isr, httpHeaders,
					HttpStatus.OK);
	        
			
		} catch (Exception e) {

			logger.error(" Failed to download Auxiliary File.");
			e.printStackTrace();
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
					HttpStatus.UNAUTHORIZED);
		}

	}
}
