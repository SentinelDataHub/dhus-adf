/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.serco.dhus.plugin.slstr;

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
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 *
 * @author dcaristo
 */
public class SlstrPlugin implements Processable {

	private static Log logger = LogFactory.getLog(SlstrPlugin.class);
	private static String externalDHuSUrl;
	private String hashedString;
	private static int MAX_THREADS_NUMBER = 20;

	private HashMap<String, String> map;;

	private static Executor executor;
	private static HashMap<String, Properties> propFiles;
	private static HashMap<String, TaskTable> taskTables;

	public SlstrPlugin() {
		executor = Executors.newFixedThreadPool(MAX_THREADS_NUMBER);
		map = new HashMap<String, String>();
		map.put("SL_1_RBT___", "SL_1");
		map.put("SL_2_WCT___", "SL_2");
		map.put("SL_2_WST___", "SL_2");
		map.put("SL_2_LST___", "SL_2");

		try {

			loadTaskTables();
			loadSelectionRulesProperties();
			externalDHuSUrl = ConfigurationManager.getExternalDHuSHost();
			hashedString = ConfigurationManager.getHashedConnectionString();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException saxe) {
			saxe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void loadTaskTables() throws ParserConfigurationException,
			SAXException, IOException {

		taskTables = new HashMap<String, TaskTable>();
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it
					.next();
			System.out.println(pair.getKey() + " = " + pair.getValue());
			URL taskTableName = ClassLoader
					.getSystemResource("../etc/task_tables/" + pair.getValue()
							+ "_TaskTable.xml");
			TaskTable taskTable;
			if (taskTableName != null) {

				taskTable = TaskTableParser.parseTaskTable(taskTableName
						.getFile());
				taskTables.put(pair.getKey() + "", taskTable);
				logger.info("Task Table parsed successfully for type "
						+ pair.getKey());
			} else {
				logger.warn(" Cannot find task table file for type "
						+ pair.getKey());
			}
			// it.remove(); // avoids a ConcurrentModificationException
		}

	}

	private void loadSelectionRulesProperties() {
		System.out.println("called loadSelectionRulesProperties map size: "
				+ map.size());

		propFiles = new HashMap<String, Properties>();
		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();

		while (it.hasNext()) {
			Map.Entry<String, String> pair = (Map.Entry<String, String>) it
					.next();
			Properties prop = PropertiesLoader.loadProperties("../etc/rules/"
					+ pair.getValue() + "_SelectionRules.properties");
			System.out.println(pair.getKey() + " = " + pair.getValue());
			if (prop != null) {

				propFiles.put(pair.getKey() + "", prop);
				logger.info("Properties file loaded successfully for type "
						+ pair.getKey());
			} else {
				logger.warn(" Cannot load Properties file for type "
						+ pair.getKey());
			}
			// it.remove(); // avoids a ConcurrentModificationException
		}

	}

	public String getClassType(String classType) {
		String type = "";
		if (map.containsKey(classType)) {
			type = classType;
		}
		return type;
	}

	public List<Request> retrieveRequests(String productType, String startTime,
			String stopTime) {

		List<Request> requests = new ArrayList<Request>();
		Date startDate;
		Date stopDate;
		long startDateInMillis;
		long stopDateInMillis;

		DateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		// 2016-07-16T00:00:00.000Z
		DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

		try {
			startDate = inputFormat.parse(startTime);
			stopDate = inputFormat.parse(stopTime);
			startDateInMillis = startDate.getTime();
			stopDateInMillis = stopDate.getTime();

			if (taskTables.get(productType) != null) {

				for (Task task : taskTables.get(productType).getTasks()) {
					logger.info("task.getName()" + task.getName());
					logger.info("task.getInputs()" + task.getInputs());
					Request request;
					for (Input in : task.getInputs()) {
						request = new Request();
						request.setMandatory(in.getMandatory());
						// TODO: Manage Mode when getting requests to perform
						request.setMode(in.getMode());
						List<InputRequest> inputRequests = new ArrayList<InputRequest>();
						for (Alternative alternative : in.getAlternatives()) {
							InputRequest inReq = new InputRequest();
							String url = propFiles
									.get(productType)
									.getProperty(alternative.getRetrievalMode());
							url = url.replace("<filename>",
									alternative.getFileType());
							long startValidity;
							long stopValidity;
							if (alternative.getT0() > 0) {
								startValidity = startDateInMillis
										- (long) Math
												.ceil(alternative.getT0() * 1000);
							} else {
								startValidity = startDateInMillis;
							}
							if (alternative.getT1() > 0) {
								stopValidity = stopDateInMillis
										+ (long) Math
												.ceil(alternative.getT1() * 1000);
							} else {
								stopValidity = stopDateInMillis;
							}
							Date startValidityDate = new Date(startValidity);
							String beginPosition = outputFormat
									.format(startValidityDate) + ".000Z";
							Date stopValidityDate = new Date(stopValidity);
							String endPosition = outputFormat
									.format(stopValidityDate) + ".000Z";
							url = url.replace("<beginPosition>", beginPosition);
							url = url.replace("<endPosition>", endPosition);
							url = externalDHuSUrl + url;
							inReq.setUrl(url);
							inReq.setType(alternative.getFileType());
							inReq.setRetrievalMode(alternative
									.getRetrievalMode());
							inputRequests.add(inReq);
							logger.info("----------");
							logger.info("Request to perform: "
									+ externalDHuSUrl + url);
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

	public Object process(Object input) {
		List<Response> result = new ArrayList<Response>();

		String filename = input.toString();
		if (filename == null) {
			System.out.println("[ERROR]: null filename. Process aborted.");
			return result;
		}
		try {
			String productType = filename.substring(4, 15);
			String startTime = filename.substring(16, 31);
			String stopTime = filename.substring(32, 47);
			if (map.containsKey(productType)) {
				List<Request> requestList = retrieveRequests(productType,
						startTime, stopTime);
				List<RequestTask> tasks = new ArrayList<RequestTask>();
				for (Request aux_request : requestList) {
					Response response = new Response();
					response.setMandatory(aux_request.getMandatory());
					logger.info("requestList.size() " + requestList.size());
					logger.info("aux_request.getRequests().size() "
							+ aux_request.getRequests().size());
					List<InputRequest> requestsMap = aux_request.getRequests();

					for (InputRequest inReqs : requestsMap) {

						tasks.add(new RequestTask(inReqs.getUrl(), inReqs
								.getType(), inReqs.getRetrievalMode(),
								hashedString, SlstrPlugin.executor));
						logger.info("url added to tasks list " + inReqs);
					}
					while (!tasks.isEmpty()) {
						List<Object> responses = new ArrayList<Object>();
						logger.info("tasks size..... " + tasks.size());

						for (Iterator<RequestTask> it = tasks.iterator(); it
								.hasNext();) {
							RequestTask task = it.next();
							OutputResponse outRes = new OutputResponse();
							if (task.isDone()) {
								String req = task.getRequest();
								String type = task.getRequestType();
								logger.info("Request task.getRequest() " + req);
								String res = task.getResponse();
								logger.info("Response task.getResponse() "
										+ res);
								JSONArray jsonResponse = (JSONArray) new JSONParser()
										.parse(res);
								JSONArray jsonResult;
								logger.info("How is the response?????" + jsonResponse);	
	                            if(jsonResponse!= null) {
	                            	logger.info("How long is the response?????" + jsonResponse.size());	
	                            }
								if (task.getRetrievalMode().equalsIgnoreCase(
										"LatestValidityClosest")
										&& jsonResponse != null
										&& jsonResponse.size() > 0) {
									jsonResult = getLatestValidityClosestInput(
											startTime, stopTime, jsonResponse);
								} else {
									jsonResult = jsonResponse;									
								}
								outRes.setResponse(jsonResult);
								outRes.setType(type);
								responses.add(outRes);

								it.remove();
							}
						}
						response.setFiles(responses);
						// avoid tight loop in "main" thread
						if (!tasks.isEmpty())
							Thread.sleep(100);
					}
					result.add(response);
				}

			}
		} catch (Exception e) {
			logger.error("[ERROR]: Problems occurred during file processing: "
					+ e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	private Date computeMiddleDate(String start, String stop) {
		DateFormat inputFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		// 2016-07-16T00:00:00.000Z
		DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		Date startDate;
		Date stopDate;
		try {
			startDate = inputFormat.parse(start);
			stopDate = inputFormat.parse(stop);
			return new Date((startDate.getTime() + stopDate.getTime()) / 2);
		} catch (Exception e) {
			logger.error("Error computing middle date " + e.getMessage());
			e.printStackTrace();
			return null;
		}

	}

	private JSONArray getLatestValidityClosestInput(String start, String stop,
			JSONArray input) {
		JSONArray response = new JSONArray();
		;
		Date middleDate;

		try {

			middleDate = computeMiddleDate(start, stop);
			Iterator<JSONObject> i = input.iterator();
			JSONObject latestValidityClosest = new JSONObject();
			long diffInMillis = -1;
			while (i.hasNext()) {
				JSONObject obj = i.next();
				String title = (String) obj.get("identifier");
				String startTime = title.substring(16, 31);
				String stopTime = title.substring(32, 47);
				Date inputMiddleDate = computeMiddleDate(startTime, stopTime);
				if (diffInMillis == -1) {
					diffInMillis = Math.abs(middleDate.getTime()
							- inputMiddleDate.getTime());
					latestValidityClosest = obj;
				} else {
					if (Math.abs(middleDate.getTime()
							- inputMiddleDate.getTime()) < diffInMillis) {
						diffInMillis = Math.abs(middleDate.getTime()
								- inputMiddleDate.getTime());
						latestValidityClosest = obj;
					}
				}

			}
			response.add(latestValidityClosest);

		} catch (Exception e) {
			logger.error("Error computing middle date " + e.getMessage());
			e.printStackTrace();
		}
		return response;

	}

}
