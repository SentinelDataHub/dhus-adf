/*
 * Data HUb Service (DHuS) - For Space data distribution.
 * Copyright (C) 2013,2014,2015,2016 European Space Agency (ESA)
 * Copyright (C) 2013,2014,2015,2016 GAEL Systems
 * Copyright (C) 2013,2014,2015,2016 Serco Spa
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
package eu.serco.dhus.config;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertiesLoader {

	private static Log logger = LogFactory.getLog(PropertiesLoader.class);
 
	private static Properties rulesProps;
	
	private PropertiesLoader() {
		
	}
	
	public static Properties loadProperties(String propFileName) {
		InputStream inputStream = null;
		try {
			rulesProps = new Properties();			
 
			inputStream = PropertiesLoader.class.getClassLoader().getResourceAsStream(propFileName);
 
			if (inputStream != null) {
				rulesProps.load(inputStream);
			} else {
				logger.error("property file '" + propFileName + "' not found in the classpath");
				throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
			}
 			
		} catch (Exception e) {
			logger.error("Exception: " + e);
		} finally {
			if(inputStream != null)
				try {
					inputStream.close();
				} catch (IOException e) {
					logger.error("Error closing InputStream for properties file: " + e);
					e.printStackTrace();
				}
		}
		return rulesProps;
		
	}
	
	public static String getProperty(String key) throws IOException {
 
		String value="";
		try {
			
			value = rulesProps.getProperty(key);
			logger.info("Property[ " + key + "] = " + value );
		} catch (Exception e) {
			logger.error("Exception: " + e);
		}
		return value;
	}
	

}
