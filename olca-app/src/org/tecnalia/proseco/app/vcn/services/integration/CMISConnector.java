package org.tecnalia.proseco.app.vcn.services.integration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.log4j.*;

/**
 * This class acts as a connector to alfresco based platform.
 * 
 * @author alberto
 * 
 */
public class CMISConnector {

	private static Session session;

	static String properties_file = "CMISconfig.properties";
	static String host = "host";
	static String port = "port";
	static String serviceUrl = "serviceUrl";
	static String user = "user";
	static String password = "password";
	static Properties prop;
	static String analysisResultsExcelFileName = "analysis_results_excel_file_name";
	static String productComparisonExcelFileName = "product_comparison_excel_file_name";
	static String productComparisonNormalizationChartFileName = "product_comparison_normalization_chart_file_name";
	static String productComparisonCharacterizationChartFileName = "product_comparison_characterization_chart_file_name";
	static String productComparisonChartFileName = "product_comparison_chart_file_name";	
	
	// Logger
	private static Logger logger = Logger.getLogger(CMISConnector.class
			.getName());
	
	/**
	 * Class constructor.
	 */
	public CMISConnector() {
	}

	public static void startSession() {
		
		if (session != null) return;
		
		// Set logger level.
		logger.setLevel(Level.INFO);
		
		prop = new Properties();
		try {
			// load properties file
			InputStream input = CMISConnector.class.getResourceAsStream(properties_file);
			// load a properties file
			prop.load(input);
			// default factory implementation
			SessionFactory factory = SessionFactoryImpl.newInstance();
			Map<String, String> parameter = new HashMap<String, String>();
			// user credentials
			parameter.put(SessionParameter.USER, prop.getProperty(user));
			parameter.put(SessionParameter.PASSWORD, prop.getProperty(password));
			// connection settings
			parameter.put(
					SessionParameter.ATOMPUB_URL,
					"http://" + prop.getProperty(host) + ":"
							+ prop.getProperty(port)
							+ prop.getProperty(serviceUrl));
			parameter.put(SessionParameter.BINDING_TYPE,
					BindingType.ATOMPUB.value());
			// Set the alfresco object factory
			parameter.put(SessionParameter.OBJECT_FACTORY_CLASS,
					"org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
			List<Repository> repositories = factory.getRepositories(parameter);
			for (Repository repository : repositories) {
				logger.info("Found VCN repository: " + repository.getName()
						+ ", id: " + repository.getId());
			}
			// get the first repository
			Repository repository = repositories.get(0);
			parameter.put(SessionParameter.REPOSITORY_ID, repository.getId());
			// create session
			session = factory.createSession(parameter);
			logger.info("CMIS Connection was stablished!");			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Save LCI/LCA Excel file with updated configuration into the VCN.
	 * 
	 * @param excel_content
	 * @param folderId 
	 * @throws Exception 
	 */
	public static void saveExcelProductSystemInVCN(byte[] excel_content, String folderId) {
		
		// get the LCA folder
		Folder lcaFolder = (Folder) session.getObject(session.createObjectId(folderId));
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		
		// Set the name for the file
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();		
		String fileName = (String) prop.get(analysisResultsExcelFileName);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		fileName = fileName + "_week_" + calendar.get(Calendar.WEEK_OF_YEAR) + "_" + dateFormat.format(date) + ".xlsx";
		properties.put(PropertyIds.NAME, fileName);
		try {			
			setExcelContent(lcaFolder, excel_content, properties);	
		} catch (Exception ex) {
			ex.printStackTrace();			
		}
	}
	
	/**
	 * Save LCI/LCA Excel file with updated configuration into the VCN.
	 * 
	 * @param excel_content
	 * @param folderId 
	 * @throws Exception 
	 */
	public static void saveExcelProjectInVCN(byte[] excel_content, String folderId) {
		
		// get the LCA folder
		Folder lcaFolder = (Folder) session.getObject(session.createObjectId(folderId));
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		
		// Set the name for the file
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();		
		String fileName = (String) prop.get(productComparisonExcelFileName);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		fileName = fileName + "_week_" + calendar.get(Calendar.WEEK_OF_YEAR) + "_" + dateFormat.format(date) + ".xlsx";
		properties.put(PropertyIds.NAME, fileName);
		try {			
			setExcelContent(lcaFolder, excel_content, properties);	
		} catch (Exception ex) {
			ex.printStackTrace();			
		}
	}

	/**
	 * Upload excel file to VCN.
	 * 
	 * @param folder
	 * @param excel_content
	 * @param properties
	 */
	private static void setExcelContent(Folder folder, byte[] excel_content,
			Map<String, Object> properties) {
		try {
			// set content
			String mimetype = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;  charset=UTF-8";
			ByteArrayInputStream input = new ByteArrayInputStream(excel_content);
			ContentStream contentStream = new ContentStreamImpl(
					PropertyIds.NAME, BigInteger.valueOf(excel_content.length),
					mimetype, input);			
			// create a major version
			@SuppressWarnings("unused")
			Document newDoc = folder.createDocument(properties, contentStream,
					VersioningState.MAJOR);		
			logger.info("The LCI/LCA file was uploaded into the VCN LCA directory!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void saveComparisonChartAsPNG(byte[] chart_content, String folderId) {
		// get the LCA folder
		Folder lcaFolder = (Folder) session.getObject(session.createObjectId(folderId));
		
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		
		// Set the name for the file
		Date date = new Date();
		Calendar calendar = Calendar.getInstance();		
		String fileName = (String) prop.get(productComparisonChartFileName);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
		fileName = fileName + "_week_" + calendar.get(Calendar.WEEK_OF_YEAR) + "_" + dateFormat.format(date) + ".png";
		properties.put(PropertyIds.NAME, fileName);
		try {			
			setPNGContent(lcaFolder, chart_content, properties);	
		} catch (Exception ex) {
			ex.printStackTrace();			
		}
		
	}
	
	private static void setPNGContent(Folder folder, byte[] chart_content,
			Map<String, Object> properties) {		
		try {
			// set content
			String mimetype = "image/png";
			ByteArrayInputStream input = new ByteArrayInputStream(chart_content);
			ContentStream contentStream = new ContentStreamImpl(
					PropertyIds.NAME, BigInteger.valueOf(chart_content.length),
					mimetype, input);			
			// create a major version
			@SuppressWarnings("unused")
			Document newDoc = folder.createDocument(properties, contentStream,
					VersioningState.MAJOR);		
			logger.info("The LCI/LCA PNG file was uploaded into the VCN LCA directory!");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main (String[] args) {
		CMISConnector.startSession();
	}
}
