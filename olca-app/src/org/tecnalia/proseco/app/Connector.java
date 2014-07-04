package org.tecnalia.proseco.app;

import java.util.List;

import org.openlca.app.App;
import org.openlca.app.db.Cache;
import org.openlca.app.db.Database;
import org.openlca.app.db.DatabaseList;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.ContributionResult;

public class Connector {
	
	public static void init () throws Exception {
		DatabaseList dbList = Database.getConfigurations();
		
		DerbyDatabase db = (DerbyDatabase) Database.activate(dbList.getLocalDatabases().get(0));
		
		ProductSystemDao productSystem = new ProductSystemDao(db);
		
		List<ProductSystem> productSystemList = productSystem.getAll();
		
		CalculationSetup setUp = new CalculationSetup(productSystemList.get(0), CalculationSetup.QUICK_RESULT);
		//setUp.setAllocationMethod(allocationViewer.getSelected());
		//setUp.setImpactMethod(methodViewer.getSelected());
		//NwSetDescriptor set = nwViewer.getSelected();
		//setUp.setNwSet(set);
		//setUp.setNumberOfRuns(iterationCount);
		//setUp.getParameterRedefs().addAll(productSystem.getParameterRedefs());
		
		SystemCalculator calculator = new SystemCalculator(Cache.getMatrixCache(), App.getSolver());
		
		ContributionResult result = calculator.calculateContributions(setUp);	
		
		System.out.println();
	}
	
}
