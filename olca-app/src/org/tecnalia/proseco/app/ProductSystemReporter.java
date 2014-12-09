package org.tecnalia.proseco.app;

import java.util.List;
import java.util.Set;

import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openlca.app.App;
import org.openlca.app.db.Cache;
import org.openlca.core.database.ImpactMethodDao;
import org.openlca.core.database.NwSetDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.derby.DerbyDatabase;
import org.openlca.core.math.CalculationSetup;
import org.openlca.core.math.SystemCalculator;
import org.openlca.core.matrix.NwSetTable;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.NwSetDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ContributionSet;
import org.openlca.core.results.Contributions;
import org.openlca.core.results.FullResult;
import org.openlca.core.results.FullResultProvider;
import org.openlca.core.results.ImpactResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.SimpleResultProvider;
import org.tecnalia.proseco.app.io.png.results.ProductSystemsComparisonChart;
import org.tecnalia.proseco.app.io.png.results.ProductSystemsImpactChart;
import org.tecnalia.proseco.app.io.xls.results.AnalysisResultExport;

public class ProductSystemReporter {

	public static void saveAnalysisReport(DerbyDatabase db, int productSystemId, int targetAmount, String folderId) {
		// TODO Auto-generated method stub
		
		ProductSystemDao productSystem = new ProductSystemDao(db);		
		List<ProductSystem> productSystemList = productSystem.getAll();		
		ProductSystem ps = productSystemList.get(productSystemId);		
		System.out.println("ProductSystemName: " + ps.getName());	
		
		CalculationSetup setup = new CalculationSetup(ps, CalculationSetup.ANALYSIS);		
		setup.setAllocationMethod(AllocationMethod.USE_DEFAULT);
		
		List<ImpactMethodDescriptor> imds = new ImpactMethodDao(db).getDescriptors();		
		ImpactMethodDescriptor imd = imds.get(11);
		setup.setImpactMethod(imd);
		
		List<NwSetDescriptor> nsds = new NwSetDao(db).getDescriptorsForMethod(imd.getId());
		NwSetDescriptor nsd = nsds.get(0);				
		setup.setNwSet(nsd);
				
		// Externalize this parameter. It is the functional unit target amount
		//setup.setAmount(100);
		ps.setTargetAmount(targetAmount);
		
		SystemCalculator calculator = new SystemCalculator(Cache.getMatrixCache(), App.getSolver());
		
		FullResult fullResult = calculator.calculateFull(setup);		
		
		//String resultKey = Cache.getAppCache().put(fullResult);
		//String setupKey = Cache.getAppCache().put(setup);		
		FullResultProvider fullResultProvider = new FullResultProvider(fullResult, Cache.getEntityCache());
		
		//File exportFile = new File("reports/analysis_result_test.xlsx");				
		final AnalysisResultExport export = new AnalysisResultExport(
				setup.getProductSystem(), fullResultProvider);
		
		export.saveAnalysisResultToVCN(folderId);		
	}

	public static void listNormalizedImpacts(DerbyDatabase db, int productSystemId,
			int targetAmount) {
		ProductSystemDao productSystem = new ProductSystemDao(db);		
		List<ProductSystem> productSystemList = productSystem.getAll();		
		ProductSystem ps = productSystemList.get(productSystemId);		
		System.out.println("ProductSystemName: " + ps.getName());
		
		CalculationSetup setup = new CalculationSetup(ps, CalculationSetup.ANALYSIS);		
		setup.setAllocationMethod(AllocationMethod.USE_DEFAULT);
		
		List<ImpactMethodDescriptor> imds = new ImpactMethodDao(db).getDescriptors();		
		ImpactMethodDescriptor imd = imds.get(11);
		setup.setImpactMethod(imd);
		
		List<NwSetDescriptor> nsds = new NwSetDao(db).getDescriptorsForMethod(imd.getId());
		NwSetDescriptor nsd = nsds.get(2);
		setup.setNwSet(nsd);
		
		ps.setTargetAmount(targetAmount);
		
		SystemCalculator calculator = new SystemCalculator(Cache.getMatrixCache(), App.getSolver());
		
		SimpleResult simpleResult = calculator.calculateSimple(setup);
		
		SimpleResultProvider<SimpleResult> simpleResultProvider = new SimpleResultProvider<SimpleResult>(simpleResult, Cache.getEntityCache());
		
		NwSetTable nwSetTable = NwSetTable.build(db, setup.getNwSet().getId());
		
		List<ImpactResult> impactResults = nwSetTable.applyNormalisation(simpleResultProvider.getTotalImpactResults());
		
		List<ContributionItem<ImpactResult>> items = makeContributions(impactResults);
		
		for (ContributionItem<ImpactResult> contributionItem : items) {			
			System.out.println(contributionItem.getItem().getImpactCategory().getName());
			System.out.println(contributionItem.getAmount());
			//System.out.println((double)Math.round(contributionItem.getAmount() * 100000) / 100000);			
		}		
	}

	private static List<ContributionItem<ImpactResult>> makeContributions(
			List<ImpactResult> impactResults) {
		ContributionSet<ImpactResult> set = Contributions.calculate(
				impactResults, new Contributions.Function<ImpactResult>() {
					@Override
					public double value(ImpactResult impactResult) {
						return impactResult.getValue();
					}
				});
		List<ContributionItem<ImpactResult>> items = set.getContributions();
		Contributions.sortDescending(items);
		return items;
	}
	
	public static void saveNormalizedImpactsAsPng(DerbyDatabase db,
			int productSystem1Id, int productSystem2Id, int targetAmount,
			String folderId) {
		
		ProductSystemDao productSystem1 = new ProductSystemDao(db);		
		List<ProductSystem> productSystemList1 = productSystem1.getAll();		
		ProductSystem ps1 = productSystemList1.get(productSystem1Id);		
		System.out.println("ProductSystem1Name: " + ps1.getName());
		
		ProductSystemDao productSystem2 = new ProductSystemDao(db);		
		List<ProductSystem> productSystemList2 = productSystem2.getAll();		
		ProductSystem ps2 = productSystemList2.get(productSystem2Id);		
		System.out.println("ProductSystem2Name: " + ps2.getName());
		
		CalculationSetup setup1 = new CalculationSetup(ps1, CalculationSetup.ANALYSIS);		
		setup1.setAllocationMethod(AllocationMethod.USE_DEFAULT);
		
		CalculationSetup setup2 = new CalculationSetup(ps2, CalculationSetup.ANALYSIS);		
		setup2.setAllocationMethod(AllocationMethod.USE_DEFAULT);
		
		List<ImpactMethodDescriptor> imds1 = new ImpactMethodDao(db).getDescriptors();		
		ImpactMethodDescriptor imd1 = imds1.get(11);
		setup1.setImpactMethod(imd1);
		
		List<ImpactMethodDescriptor> imds2 = new ImpactMethodDao(db).getDescriptors();		
		ImpactMethodDescriptor imd2 = imds2.get(11);
		setup2.setImpactMethod(imd2);
		
		List<NwSetDescriptor> nsds1 = new NwSetDao(db).getDescriptorsForMethod(imd1.getId());
		NwSetDescriptor nsd1 = nsds1.get(2);
		setup1.setNwSet(nsd1);
		
		List<NwSetDescriptor> nsds2 = new NwSetDao(db).getDescriptorsForMethod(imd2.getId());
		NwSetDescriptor nsd2 = nsds2.get(2);
		setup2.setNwSet(nsd2);
		
		ps1.setTargetAmount(targetAmount);
		
		ps2.setTargetAmount(targetAmount);
		
		SystemCalculator calculator1 = new SystemCalculator(Cache.getMatrixCache(), App.getSolver());
		
		SystemCalculator calculator2 = new SystemCalculator(Cache.getMatrixCache(), App.getSolver());
		
		SimpleResult simpleResult1 = calculator1.calculateSimple(setup1);
		
		SimpleResult simpleResult2 = calculator2.calculateSimple(setup2);
		
		SimpleResultProvider<SimpleResult> simpleResultProvider1 = new SimpleResultProvider<SimpleResult>(simpleResult1, Cache.getEntityCache());
		
		SimpleResultProvider<SimpleResult> simpleResultProvider2 = new SimpleResultProvider<SimpleResult>(simpleResult2, Cache.getEntityCache());
		
		NwSetTable nwSetTable1 = NwSetTable.build(db, setup1.getNwSet().getId());
		
		NwSetTable nwSetTable2 = NwSetTable.build(db, setup2.getNwSet().getId());
		
		List<ImpactResult> impactResults1 = nwSetTable1.applyNormalisation(simpleResultProvider1.getTotalImpactResults());
		
		List<ImpactResult> impactResults2 = nwSetTable2.applyNormalisation(simpleResultProvider2.getTotalImpactResults());
		
		List<ContributionItem<ImpactResult>> items1 = makeContributions(impactResults1);
		
		List<ContributionItem<ImpactResult>> items2 = makeContributions(impactResults2);
	
		ProductSystemsComparisonChart comparisonChart = new ProductSystemsComparisonChart(ps1, items1, ps2, items2, targetAmount);
		
		comparisonChart.exportComparisonChartToVCN(folderId);	
		
		System.out.println();
	}

	public static void saveImpactsAsPng(DerbyDatabase db,
			int productSystem1Id, int productSystem2Id, int targetAmount,
			String folderId) {
		ProductSystemDao productSystem1 = new ProductSystemDao(db);		
		List<ProductSystem> productSystemList1 = productSystem1.getAll();		
		ProductSystem ps1 = productSystemList1.get(productSystem1Id);		
		System.out.println("ProductSystem1Name: " + ps1.getName());
		
		ProductSystemDao productSystem2 = new ProductSystemDao(db);		
		List<ProductSystem> productSystemList2 = productSystem2.getAll();		
		ProductSystem ps2 = productSystemList2.get(productSystem2Id);		
		System.out.println("ProductSystem2Name: " + ps2.getName());
		
		CalculationSetup setup1 = new CalculationSetup(ps1, CalculationSetup.ANALYSIS);		
		setup1.setAllocationMethod(AllocationMethod.USE_DEFAULT);
		
		CalculationSetup setup2 = new CalculationSetup(ps2, CalculationSetup.ANALYSIS);		
		setup2.setAllocationMethod(AllocationMethod.USE_DEFAULT);
		
		List<ImpactMethodDescriptor> imds1 = new ImpactMethodDao(db).getDescriptors();		
		ImpactMethodDescriptor imd1 = imds1.get(11);
		setup1.setImpactMethod(imd1);
		
		List<ImpactMethodDescriptor> imds2 = new ImpactMethodDao(db).getDescriptors();		
		ImpactMethodDescriptor imd2 = imds2.get(11);
		setup2.setImpactMethod(imd2);
		
		List<NwSetDescriptor> nsds1 = new NwSetDao(db).getDescriptorsForMethod(imd1.getId());
		NwSetDescriptor nsd1 = nsds1.get(2);
		setup1.setNwSet(nsd1);
		
		List<NwSetDescriptor> nsds2 = new NwSetDao(db).getDescriptorsForMethod(imd2.getId());
		NwSetDescriptor nsd2 = nsds2.get(2);
		setup2.setNwSet(nsd2);
		
		ps1.setTargetAmount(targetAmount);
		
		ps2.setTargetAmount(targetAmount);
		
		SystemCalculator calculator1 = new SystemCalculator(Cache.getMatrixCache(), App.getSolver());
		
		SystemCalculator calculator2 = new SystemCalculator(Cache.getMatrixCache(), App.getSolver());
		
		SimpleResult simpleResult1 = calculator1.calculateSimple(setup1);
		
		SimpleResult simpleResult2 = calculator2.calculateSimple(setup2);
		
		SimpleResultProvider<SimpleResult> simpleResultProvider1 = new SimpleResultProvider<SimpleResult>(simpleResult1, Cache.getEntityCache());
		
		SimpleResultProvider<SimpleResult> simpleResultProvider2 = new SimpleResultProvider<SimpleResult>(simpleResult2, Cache.getEntityCache());
		
		Set<ImpactCategoryDescriptor> impactCategoryDescriptors1 = simpleResultProvider1.getImpactDescriptors();
		Set<ImpactCategoryDescriptor> impactCategoryDescriptors2 = simpleResultProvider2.getImpactDescriptors();
		
		ProductSystemsImpactChart comparisonChart = new ProductSystemsImpactChart(simpleResultProvider1, impactCategoryDescriptors1, simpleResultProvider2, impactCategoryDescriptors2, targetAmount);
		
		comparisonChart.exportComparisonChartToVCN(folderId);		
	}

}
