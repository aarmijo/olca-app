package org.tecnalia.proseco.app;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.Platform;
import org.openlca.app.db.Database;
import org.openlca.app.db.DatabaseList;
import org.openlca.core.database.derby.DerbyDatabase;
import org.tecnalia.proseco.app.vcn.services.integration.CMISConnector;

public class Connector {
	
	// Logger
	private static Logger logger = Logger.getLogger(Connector.class.getName());
	static String properties_file = "Connector.properties";
	static Properties prop;
	static String hydraulicEnergyFolderId = "hidraulic_energy_folder_id";
	static String windEnergyFolderId = "wind_energy_folder_id";
	static String windVsHydraulicFolderId = "wind_vs_hydraulic_folder_id";
	
	public static void init () throws Exception {
		
		// Get the argument that represents the energy generated
		String energyValueString = Platform.getApplicationArgs()[1];
		Double d = Double.parseDouble(energyValueString);
		long energyValueLong = Math.round(d);
		int energyValueInt = (int) energyValueLong;		
		
		// Set logger level.
		logger.setLevel(Level.INFO);

		// Read properties from Connector.properties file
		prop = new Properties();
		// load properties file
		InputStream input = Connector.class.getResourceAsStream(properties_file);
		// load a properties file
		prop.load(input);

		DatabaseList dbList = Database.getConfigurations();		
		DerbyDatabase db = (DerbyDatabase) Database.activate(dbList.getLocalDatabases().get(1));
		
		CMISConnector.startSession();
		
		// Hydraulic energy
		ProductSystemReporter.saveAnalysisReport(db, 0, energyValueInt, prop.getProperty(hydraulicEnergyFolderId));
		
		// Wind vs. Hydraulic Energy Comparison Chart (Normalized)
		ProductSystemReporter.saveNormalizedImpactsAsPng(db, 0, 1, energyValueInt, prop.getProperty(windVsHydraulicFolderId));
		
		// Wind vs. Hydraulic Energy Comparison Chart (Characterization)
		ProductSystemReporter.saveImpactsAsPng(db, 0, 1, energyValueInt, prop.getProperty(windVsHydraulicFolderId));
		
		// Wind energy
		ProductSystemReporter.saveAnalysisReport(db, 1, energyValueInt, prop.getProperty(windEnergyFolderId));
				
		// Wind vs. Hydraulic Energy report
		ProjectReporter.saveComparisonReport(db, 0, energyValueInt, prop.getProperty(windVsHydraulicFolderId));		
		
		System.out.println();
	}
	
}
