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
package eu.serco.dhus.xml.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.serco.dhus.data.Alternative;
import eu.serco.dhus.data.Input;
import eu.serco.dhus.data.Task;
import eu.serco.dhus.data.TaskTable;


public class TaskTableParser {
	
	private TaskTableParser() {
		
	}
	
	private static Log logger = LogFactory.getLog(TaskTableParser.class);

	public static TaskTable parseTaskTable(String filename)
			throws ParserConfigurationException, SAXException, IOException {			

		TaskTable taskTable = new TaskTable();

		if (filename == null || filename.isEmpty())
			throw new RuntimeException("The name of the XML file is required!");

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		// Load the input XML document, parse it and return an instance of the
		// Document class.
		Document document = builder.parse(new File(filename));
		document.getDocumentElement().normalize();
		NodeList list = document.getElementsByTagName("Processor_Name");
		logger.info(" NodeList list size----: " + list.getLength());
		if(list!= null && list.item(0) != null && list.item(0).getFirstChild() != null) {					
				taskTable.setProcessorName(
				list.item(0).getFirstChild().getNodeValue());			
		}


		
		NodeList nodeList = document
				.getElementsByTagName("Task");
		Node node;
		Element elem;
		List<Task> tasks = new ArrayList<Task>();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Task task = new Task();
			node = nodeList.item(i);
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				elem = (Element) node;

				task.setName(elem.getElementsByTagName("Name").item(0)
						.getChildNodes().item(0).getNodeValue());
				logger.info("taskname:  " + task.getName());

				List<Input> task_inputs = new ArrayList<Input>();
				NodeList inputs = elem
						.getElementsByTagName("Input");

				for (int j = 0; j < inputs.getLength(); j++) {

					Input input = new Input();
					node = inputs.item(j);

					if (node.getNodeType() == Node.ELEMENT_NODE) {
						elem = (Element) node;

						input.setMode(elem.getElementsByTagName("Mode").item(0)
								.getChildNodes().item(0).getNodeValue());
						logger.info("==== mode:  " + input.getMode());
						input.setMandatory(elem
								.getElementsByTagName("Mandatory").item(0)
								.getChildNodes().item(0).getNodeValue());
						logger.info("==== mandatory:  " + input.getMandatory());

						List<Alternative> input_alternatives = new ArrayList<Alternative>();
						NodeList alternatives = elem
								.getElementsByTagName("Alternative");

						for (int k = 0; k < alternatives.getLength(); k++) {

							Alternative alternative = new Alternative();
							node = alternatives.item(k);

							if (node.getNodeType() == Node.ELEMENT_NODE) {
								elem = (Element) node;

								alternative.setOrder(Integer.parseInt(elem
										.getElementsByTagName("Order").item(0)
										.getChildNodes().item(0).getNodeValue()));
								logger.info("==== order:  " + alternative.getOrder());
								alternative.setRetrievalMode(elem
										.getElementsByTagName("Retrieval_Mode")
										.item(0).getChildNodes().item(0)
										.getNodeValue());
								logger.info("==== retrievalMode:  "
										+ alternative.getRetrievalMode());
								alternative.setT0(Double.parseDouble(elem.getElementsByTagName("T0")
										.item(0).getChildNodes().item(0)
										.getNodeValue()));
								logger.info("==== t0:  " + alternative.getT0());
								alternative.setT1(Double.parseDouble(elem.getElementsByTagName("T1")
										.item(0).getChildNodes().item(0)
										.getNodeValue()));
								logger.info("==== t1:  " + alternative.getT1());
								alternative.setFileType(elem
										.getElementsByTagName("File_Type")
										.item(0).getChildNodes().item(0)
										.getNodeValue());
								logger.info("==== fileType:  "
										+ alternative.getFileType());
								alternative.setFileNameType(elem
										.getElementsByTagName("File_Name_Type")
										.item(0).getChildNodes().item(0)
										.getNodeValue());
								logger.info("==== fileNameType:  "
										+ alternative.getFileNameType());
							}
							input_alternatives.add(alternative);
						}
						input.setAlternatives(input_alternatives);
					}
					task_inputs.add(input);
				}
				task.setInputs(task_inputs);
			}
			tasks.add(task);
		}
		taskTable.setTasks(tasks);
		return taskTable;
	}
		

}
