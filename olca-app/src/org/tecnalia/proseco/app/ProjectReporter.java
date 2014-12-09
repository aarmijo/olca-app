package org.tecnalia.proseco.app;

import java.io.File;
import java.util.List;

import org.openlca.app.App;
import org.openlca.app.db.Cache;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.ProjectCalculator;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.core.results.ProjectResultProvider;
import org.tecnalia.proseco.app.io.xls.results.ProjectResultExport;

public class ProjectReporter {

	public static void saveComparisonReport(DerbyDatabase db, int productSystemId, int targetAmount, String folderId) {
		// TODO Auto-generated method stub
		ProjectDao projectDao = new ProjectDao(db);		
		List<Project> projectList = projectDao.getAll();		
		Project project = projectList.get(productSystemId);		
		System.out.println("Project: " + project.getName());
		
		// Set the target amount in the project variants
		for (ProjectVariant projectVariant : project.getVariants()) {
			projectVariant.setAmount(targetAmount);
		}	
		
		ProjectCalculator calculator = new ProjectCalculator(Cache.getMatrixCache(), App.getSolver());		
		ProjectResultProvider projectResultProvider = calculator.solve(project, Cache.getEntityCache());
		
		//ProjectResultExport export = new ProjectResultExport(project, new File("reports/Wind vs. Hidro.xlsx"), Cache.getEntityCache());
		ProjectResultExport export = new ProjectResultExport(project, Cache.getEntityCache());
		
		try {
			export.saveAnalysisResultToVCN(projectResultProvider, folderId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
