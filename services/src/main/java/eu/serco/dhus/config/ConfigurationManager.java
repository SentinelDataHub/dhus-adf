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

import java.util.Properties;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConfigurationManager {
	
	private static Properties configFile;
	
	private static Log logger = LogFactory.getLog(PropertiesLoader.class);
	public static String EXTERNAL_DHUS_HOST=			"external_dhus_host";
	public static String EXTERNAL_DHUS_USERNAME=		"external_dhus_username";
	public static String EXTERNAL_DHUS_PWD=			"external_dhus_password";
	
	private ConfigurationManager() {
		
	}
	
	private static synchronized Properties getPropertiesInstance() {
		if(configFile == null) 
			configFile = PropertiesLoader.loadProperties("../etc/conf/ExternalDHuS.properties");
		return configFile;
	}
	
	
	public static String getExternalDHuSHost() {
		String prop="";
		try {
			prop = getPropertiesInstance().getProperty(EXTERNAL_DHUS_HOST);
		} catch (Exception e) {
			logger.error("Error retrieving property " + EXTERNAL_DHUS_HOST);
			e.printStackTrace();
		}
		return prop;
	}
	
	private static String getExternalDHuSUsername() {
		String prop="";
		try {
			prop = getPropertiesInstance().getProperty(EXTERNAL_DHUS_USERNAME);
		} catch (Exception e) {
			logger.error("Error retrieving property " + EXTERNAL_DHUS_USERNAME);
			e.printStackTrace();
		}
		return prop;
	}
	
	private static String getExternalDHuSPassword() {
		String prop="";
		try {
			prop = getPropertiesInstance().getProperty(EXTERNAL_DHUS_PWD);
		} catch (Exception e) {
			logger.error("Error retrieving property " + EXTERNAL_DHUS_PWD);
			e.printStackTrace();
		}
		return prop;
	}
	
	public static String getHashedConnectionString() {
		String hashedString="";
		try {
			String unhashedString = getExternalDHuSUsername() + ":" + getExternalDHuSPassword();
			Base64 base64 = new Base64();
			hashedString = (String) base64.encodeToString(unhashedString.getBytes());
		} catch (Exception e) {
			logger.error("Error retrieving Hashed Connection String");
			e.printStackTrace();
		}
    	return hashedString;
	}
	
	

}
