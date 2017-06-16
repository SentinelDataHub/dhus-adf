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
package eu.serco.dhus.request;

import java.util.concurrent.Callable;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;;

public class RequestWork implements Callable<String> {
	
	private final String url;
	private final String type;
	private final String authorization;
    public RequestWork(String url, String type, String authorization) {
        this.url = url;
        this.type = type;
        this.authorization = authorization;
    }
    public String getUrl() {
        return this.url;
    }
    public String getType() {
        return this.type;
    }
    public String call() throws Exception {  
    	HttpGet httpGet = new HttpGet(getUrl());
    	
    	 //This class doesn't exist, it's for demonstration purpose only.

    	httpGet.setHeader("Authorization", "Basic " + this.authorization); 	
        return HttpClientBuilder.create().build().execute(httpGet, new BasicResponseHandler());
    }

}
