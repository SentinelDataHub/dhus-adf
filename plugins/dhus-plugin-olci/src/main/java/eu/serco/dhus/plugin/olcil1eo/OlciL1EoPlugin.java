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
package eu.serco.dhus.plugin.olcil1eo;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.xml.sax.SAXException;

import eu.serco.dhus.config.ConfigurationManager;
import eu.serco.dhus.config.PropertiesLoader;
import eu.serco.dhus.data.Alternative;
import eu.serco.dhus.data.Input;
import eu.serco.dhus.data.InputRequest;
import eu.serco.dhus.data.OutputResponse;
import eu.serco.dhus.data.Request;
import eu.serco.dhus.data.Response;
import eu.serco.dhus.data.Task;
import eu.serco.dhus.data.TaskTable;
import eu.serco.dhus.processor.Processable;
import eu.serco.dhus.request.RequestTask;
import eu.serco.dhus.xml.parser.TaskTableParser;



public class OlciL1EoPlugin implements Processable
{
	private static Log logger = LogFactory.getLog(OlciL1EoPlugin.class);
	private static String externalDHuSUrl;   
	private String hashedString;
	private static int MAX_THREADS_NUMBER = 20;
	
    private HashMap<String,String> map;    ;
    
    private static Executor executor;
    private static HashMap<String,Properties> propFiles;
    private static HashMap<String,TaskTable> taskTables;
    
    public OlciL1EoPlugin() {
    	executor = Executors.newFixedThreadPool(MAX_THREADS_NUMBER);    	
        map = new HashMap<String,String>();
        map.put("OL_1_EFR___","OL_1_EO");
        map.put("OL_1_ERR___","OL_1_EO");
        map.put("OL_1_RAC___","OL_1_RAC");
        map.put("OL_1_SPC___","OL_1_SPC");
        map.put("OL_2_WFR___","OL_2");
        map.put("OL_2_WRR___","OL_2");
        map.put("OL_2_LFR___","OL_2");
        map.put("OL_2_LRR___","OL_2");
        
        try {
        	
        	loadTaskTables();
        	loadSelectionRulesProperties();        	
			externalDHuSUrl = ConfigurationManager.getExternalDHuSHost();			
        	hashedString = ConfigurationManager.getHashedConnectionString();
        }
        catch(ParserConfigurationException pce){
        	pce.printStackTrace();
        }
        catch(SAXException saxe){
        	saxe.printStackTrace();
        }
        catch(IOException ioe){
        	ioe.printStackTrace();        	
        }
        catch(Exception e){
        	e.printStackTrace();
        }
    }

    public Object process(Object input) {
        List<Response> result = new ArrayList<Response>();
        
        String filename = input.toString();
        if(filename == null) {
            System.out.println("[ERROR]: null filename. Process aborted.");
            return result;
        }
        try {
            String productType = filename.substring(4, 15);
            String startTime = filename.substring(16, 31);
            String stopTime = filename.substring(32, 47);
            if (map.containsKey(productType)) {
                List<Request> requestList = retrieveRequests(productType, startTime, stopTime);
                List<RequestTask> tasks = new ArrayList<RequestTask>();
                for(Request aux_request : requestList) {
                	Response response = new Response();
                	response.setMandatory(aux_request.getMandatory());
                	logger.info("requestList.size() " + requestList.size());
                	logger.info("aux_request.getRequests().size() " + aux_request.getRequests().size());
                	List<InputRequest> requestsMap = aux_request.getRequests();
	                
	                for(InputRequest inReqs : requestsMap) {
	               
	                	tasks.add(new RequestTask(inReqs.getUrl(), inReqs.getType(), hashedString, OlciL1EoPlugin.executor));
	                	logger.info("url added to tasks list " + inReqs);
	                }
	                while(!tasks.isEmpty()) {
	                	List<Object> responses = new ArrayList<Object>();
	                	logger.info("tasks size..... " + tasks.size());
	                	
	                    for(Iterator<RequestTask> it = tasks.iterator(); it.hasNext();) {
	                        RequestTask task = it.next();
	                        OutputResponse outRes = new OutputResponse();
	                        if(task.isDone()) {
	                            String req = task.getRequest();
	                            String type = task.getRequestType();
	                            logger.info("Request task.getRequest() " + req);
	                            String res = task.getResponse();
	                            logger.info("Response task.getResponse() " + res);
	                            JSONArray jsonResult = (JSONArray)new JSONParser().parse(res);	                                                        	
                            	outRes.setResponse(jsonResult);
                            	outRes.setType(type);
	                            responses.add(outRes);	                           
	                           
	                            
	                            it.remove();
	                        }
	                    }
	                    response.setFiles(responses);
	                    //avoid tight loop in "main" thread
	                    if(!tasks.isEmpty()) Thread.sleep(100);
	                }
	                result.add(response);
                }	                
	                
            }            
        }
        catch(Exception e)
        {
            logger.error("[ERROR]: Problems occurred during file processing: " + e.getMessage());
            e.printStackTrace();
        }        
        return result;
    }
    
    public String getClassType(String classType) {    	
    	String type = "";
    	if(map.containsKey(classType))
    		type = classType;
    	return type;
    }

    public List<Request> retrieveRequests( 
        String productType, String startTime, String stopTime) {
        
        List<Request> requests = new ArrayList<Request>();
        Date startDate;
        Date stopDate;
        long startDateInMillis;
        long stopDateInMillis;

		DateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		//2016-07-16T00:00:00.000Z
		DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		
		try {
			startDate = inputFormat.parse(startTime);
			stopDate = inputFormat.parse(stopTime);
			startDateInMillis = startDate.getTime();
			stopDateInMillis = stopDate.getTime();
			
			if(taskTables.get(productType) != null) {
				
				for(Task task : taskTables.get(productType).getTasks()) {
					logger.info("task.getName()" + task.getName());
					logger.info("task.getInputs()" + task.getInputs());
					Request request;
					for(Input in : task.getInputs()) {
						request = new Request();
						request.setMandatory(in.getMandatory());
						// TODO: Manage Mode when getting requests to perform
						request.setMode(in.getMode());
						List<InputRequest> inputRequests = new ArrayList<InputRequest>();
						for (Alternative alternative : in.getAlternatives()){
							InputRequest inReq = new InputRequest();
							String url = propFiles.get(productType).getProperty(alternative.getRetrievalMode());
							url = url.replace("<filename>", alternative.getFileType());
							long startValidity;
							long stopValidity;
							if(alternative.getT0() > 0) {
								startValidity = startDateInMillis - (long)Math.ceil(alternative.getT0()*1000);							
							}
							else {
								startValidity = startDateInMillis;
							}
							if(alternative.getT1() > 0) {
								stopValidity = stopDateInMillis + (long)Math.ceil(alternative.getT1()*1000);							
							}
							else {
								stopValidity = stopDateInMillis;
							}
							Date startValidityDate = new Date(startValidity);
							String beginPosition = outputFormat.format(startValidityDate)+".000Z";							
							Date stopValidityDate = new Date(stopValidity);
							String endPosition = outputFormat.format(stopValidityDate)+".000Z";	
							url = url.replace("<beginPosition>", beginPosition);
							url = url.replace("<endPosition>", endPosition);
							url = externalDHuSUrl + url;
							inReq.setUrl(url);
							inReq.setType(alternative.getFileType());
							inputRequests.add(inReq);
							logger.info("----------");
							logger.info("Request to perform: " + externalDHuSUrl + url);
						}
						logger.info("How many urls???: " + inputRequests.size());
						request.setRequests(inputRequests);
						requests.add(request);
					}					
				}				
			}
			
		} catch (ParseException pe) {
		
			pe.printStackTrace();			
		} catch (Exception e) {
			e.printStackTrace();			
		}
        logger.info("Request size is:    ????? **** " + requests.size());
        return requests;
    }
    
    
    
    private void loadTaskTables() throws ParserConfigurationException, SAXException, IOException {
    	
    	taskTables = new HashMap<String, TaskTable>();
    	Iterator<Entry<String, String>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,String> pair = (Map.Entry<String,String>)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            URL taskTableName = ClassLoader.getSystemResource ("../etc/task_tables/"+pair.getValue()+"_TaskTable.xml");    	
            TaskTable taskTable;
            if(taskTableName != null) {
        		
        		taskTable = TaskTableParser.parseTaskTable(taskTableName.getFile());
        		taskTables.put(pair.getKey()+"", taskTable);
        		logger.info("Task Table parsed successfully for type " + pair.getKey());
        	}    		
        	else
        		logger.warn(" Cannot find task table file for type " + pair.getKey()); 
           // it.remove(); // avoids a ConcurrentModificationException
        }
    	   	
    }
    
    private void loadSelectionRulesProperties() {
    	System.out.println("called loadSelectionRulesProperties map size: " + map.size());
    	
    	propFiles = new HashMap<String, Properties>();
    	Iterator<Entry<String, String>> it = map.entrySet().iterator();
        
    	while (it.hasNext()) {
            Map.Entry<String,String> pair = (Map.Entry<String,String>)it.next();
            Properties prop = PropertiesLoader.loadProperties("../etc/rules/"+pair.getValue()+"_SelectionRules.properties");              
            System.out.println(pair.getKey() + " = " + pair.getValue());
            if(prop != null) {
        		        		
        		propFiles.put(pair.getKey()+"", prop);
        		logger.info("Properties file loaded successfully for type " + pair.getKey());
        	}    		
        	else
        		logger.warn(" Cannot load Properties file for type " + pair.getKey()); 
           // it.remove(); // avoids a ConcurrentModificationException
        }
    	   	
    }
    
}
