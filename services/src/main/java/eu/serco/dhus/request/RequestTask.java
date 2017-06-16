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

import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

public class RequestTask {

	private String retrievalMode; 	
	private RequestWork work;
    private FutureTask<String> task;
    public RequestTask(String url, String type, String authorization, Executor executor) {
        this.work = new RequestWork(url, type, authorization);
        this.task = new FutureTask<String>(work);
        executor.execute(this.task);
    }
    
    public RequestTask(String url, String type, String retrievalMode, String authorization, Executor executor) {
        this.work = new RequestWork(url, type, authorization);
        this.task = new FutureTask<String>(work);
        this.retrievalMode = retrievalMode;
        executor.execute(this.task);
    }
    public String getRequest() {
        return this.work.getUrl();
    }
    public String getRequestType() {
        return this.work.getType();
    }
    public boolean isDone() {
        return this.task.isDone();
    }
    public String getRetrievalMode() {
		return retrievalMode;
	}

	public void setRetrievalMode(String retrievalMode) {
		this.retrievalMode = retrievalMode;
	}
    public String getResponse() {
        try {
            return this.task.get();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
	
}


 