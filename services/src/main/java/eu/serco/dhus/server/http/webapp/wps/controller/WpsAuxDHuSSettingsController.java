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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.serco.dhus.config.ConfigurationManager;
import eu.serco.dhus.data.config.ExternalDHuSData;

@Controller
public class WpsAuxDHuSSettingsController {
	private static Logger logger = LogManager.getLogger();

	@PreAuthorize("isAuthenticated () AND hasRole('ROLE_DATA_MANAGER')")
	@RequestMapping(value = "/settings/externaldhus", method = RequestMethod.PUT)
	public ResponseEntity<?> setMenu(@RequestBody ExternalDHuSData body)
			throws JSONException {

		URL configFile = ClassLoader
				.getSystemResource("../etc/conf/ExternalDHuS.properties");
		if (configFile != null && body != null) {
			logger.debug("Loading configuration file " + configFile.getPath());

			try {

				FileInputStream in = new FileInputStream(configFile.getPath());
				Properties props = new Properties();
				props.load(in);
				in.close();
				logger.info("EXTERNAL_DHUS_HOST " + body.getUrl());
				String encodedUrl = URLEncoder.encode(body.getUrl(),"UTF-8");
				logger.info("EXTERNAL_DHUS_HOST post encode " + encodedUrl);
				logger.info("EXTERNAL_DHUS_HOST post decode " + URLDecoder.decode(encodedUrl,"UTF-8"));
				FileOutputStream out = new FileOutputStream(
						configFile.getPath());
				if (body.getUrl() != null && !body.getUrl().isEmpty()) {
					props.setProperty(ConfigurationManager.EXTERNAL_DHUS_HOST,
							body.getUrl());
				}
				if (body.getUsername() != null && !body.getUsername().isEmpty()) {
					props.setProperty(
							ConfigurationManager.EXTERNAL_DHUS_USERNAME,
							body.getUsername());
				}
				if (body.getPassword() != null && !body.getPassword().isEmpty()) {
					props.setProperty(ConfigurationManager.EXTERNAL_DHUS_PWD,
							body.getPassword());
				}
				props.store(out, null);
				out.close();

				return new ResponseEntity<>("{\"code\":\"success\"}",
						HttpStatus.OK);
			} catch (IOException e) {

				logger.error(" Cannot write External DHuS properties file ");
				e.printStackTrace();
				return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
						HttpStatus.UNAUTHORIZED);
			}
		} else {
			logger.error(" Cannot get External DHuS properties file ");
			return new ResponseEntity<>("{\"code\":\"unauthorized\"}",
					HttpStatus.UNAUTHORIZED);
		}

	}
}
