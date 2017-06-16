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
package eu.serco.dhus.processor;

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.serco.dhus.processor.Processable;

public class ProcessorService
{
	private static Logger logger = LogManager.getLogger();
	// singleton
    private static ProcessorService service;
    // service loader
    private ServiceLoader<Processable> loader;       

    private ProcessorService() {
        loader = ServiceLoader.load(Processable.class);
    }

    public static synchronized ProcessorService getInstance() {
        if (service == null) {
        	logger.debug("Getting new  ProcessorService instance... ");
        	service = new ProcessorService();
        }
        logger.debug("Getting already created ProcessorService instance... ");
        return service;
    }

    public Object process(String classType, Object input) {
        Object output = null;
        try {            
        	Iterator<Processable> processors = loader.iterator();
            while (processors.hasNext()) {
                Processable p = processors.next();
                logger.debug("p.getClassType(classType): " + p.getClassType(classType));
                logger.debug("classType parameter: " + classType);
                if (p.getClassType(classType).equals(classType)) {
                	output = p.process(input);       
                	break;
                }
                
            }
        } catch (ServiceConfigurationError serviceError) {
        	output = null;
            serviceError.printStackTrace();
        }
        return output;
    }
    
}

