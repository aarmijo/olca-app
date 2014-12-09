package org.tecnalia.proseco.app.io.png.results;

import java.awt.Color;
import java.awt.GradientPaint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ImpactResult;
import org.openlca.core.results.SimpleResult;
import org.openlca.core.results.SimpleResultProvider;
import org.tecnalia.proseco.app.vcn.services.integration.CMISConnector;

public class ProductSystemsImpactChart {

	DefaultCategoryDataset chartDataset;
	JFreeChart barChart;
	
	public ProductSystemsImpactChart(SimpleResultProvider<SimpleResult> simpleResultProvider1,
			Set<ImpactCategoryDescriptor> impactCategoryDescriptors1,
			SimpleResultProvider<SimpleResult> simpleResultProvider2,
			Set<ImpactCategoryDescriptor> impactCategoryDescriptors2,
			int targetAmount) {
		createDataset(simpleResultProvider1, impactCategoryDescriptors1, simpleResultProvider2, impactCategoryDescriptors2);
		this.barChart = createChart(chartDataset, targetAmount);	
	}

    /**
     * Creates a bar chart.
     * 
     * @param dataset  the dataset.
     * @param targetAmount 
     * 
     * @return The chart.
     */
    private JFreeChart createChart(final CategoryDataset dataset, int targetAmount) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createBarChart(
            "Hydro vs. Wind Electricity Production: " + targetAmount + " kWh.",         // chart title
            "Impact Categories - ReCIPe Midpoint (E)",               // domain axis label
            "Value",                  // range axis label
            dataset,                  // data
            PlotOrientation.VERTICAL, // orientation
            true,                     // include legend
            true,                     // tooltips?
            false                     // URLs?
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...

        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);

        // get a reference to the plot for further customisation...
        final CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        // disable bar outlines...
        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);        
        
        renderer.setSeriesItemLabelGenerator(0, new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getNumberInstance(Locale.getDefault())));
        renderer.setSeriesItemLabelGenerator(1, new StandardCategoryItemLabelGenerator("{2}", NumberFormat.getNumberInstance(Locale.getDefault())));
        renderer.setSeriesItemLabelsVisible(0, true);
        renderer.setSeriesItemLabelsVisible(1, true);
        
        // set up gradient paints for series...
        final GradientPaint gp0 = new GradientPaint(
            0.0f, 0.0f, Color.red, 
            0.0f, 0.0f, Color.lightGray
        );
        final GradientPaint gp1 = new GradientPaint(
            0.0f, 0.0f, Color.green, 
            0.0f, 0.0f, Color.lightGray
        );
        /*
        final GradientPaint gp2 = new GradientPaint(
            0.0f, 0.0f, Color.red, 
            0.0f, 0.0f, Color.lightGray
        );*/
        renderer.setSeriesPaint(0, gp0);
        renderer.setSeriesPaint(1, gp1);
        //renderer.setSeriesPaint(2, gp2);

        final CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );
        // OPTIONAL CUSTOMISATION COMPLETED.
        
        return chart;        
    }	
    
    /**
     * Returns a sample dataset.
     * 
     * @return The dataset.
     */
    private void createDataset(SimpleResultProvider<SimpleResult> simpleResultProvider1, Set<ImpactCategoryDescriptor> impactCategoryDescriptors1, 
    		SimpleResultProvider<SimpleResult> simpleResultProvider2, Set<ImpactCategoryDescriptor> impactCategoryDescriptors2) {
        
    	// row keys...
        final String series1 = "Hydro electricity production";
        final String series2 = "Wind electricity production";

        // column keys...
        final String category1 = "Marine ecotoxicity";
        final String category2 = "Natural land transformation";
        final String category3 = "Human toxicity";
        final String category4 = "Freshwater ecotoxicity";
        final String category5 = "Freshwater eutrophication";
        final String category6 = "Terrestrial acidification";
        final String category7 = "Particulate matter formation";
        final String category8 = "Climate Change";
        final String category9 = "Fossil depletion";
        final String category10 = "Photochemical oxidant formation";
        
        final String category1Label = "Marine ecotoxicity (kg 1,4-DB eq)";
        final String category2Label = "Natural land transformation (m2)";
        final String category3Label = "Human toxicity (kg 1,4-DB eq)";
        final String category4Label = "Freshwater ecotoxicity (kg 1,4-DB eq)";
        final String category5Label = "Freshwater eutrophication (kg P eq)";
        final String category6Label = "Terrestrial acidification (kg SO2 eq)";
        final String category7Label = "Particulate matter formation (kg PM10 eq)";
        final String category8Label = "Climate Change (kg CO2 eq)";
        final String category9Label = "Fossil depletion (kg oil eq)";
        final String category10Label = "Photochemical oxidant formation (kg NMVOC)";
        
        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();        
                
		for (ImpactCategoryDescriptor impactCategoryDescriptor : impactCategoryDescriptors1) {			
			switch (impactCategoryDescriptor.getName()) {
			case category1:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category1Label);
				break;
			case category2:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category2Label);
				break;
			case category3:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category3Label);
				break;
			case category4:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category4Label);
				break;
			case category5:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category5Label);
				break;
			case category6:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category6Label);
				break;
			case category7:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category7Label);
				break;	
			case category8:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category8Label);
				break;
			case category9:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category9Label);
				break;
			case category10:
				dataset.addValue(simpleResultProvider1.getTotalImpactResult(impactCategoryDescriptor).getValue(), series1, category10Label);
				break;
			}		
		}
		
		for (ImpactCategoryDescriptor impactCategoryDescriptor : impactCategoryDescriptors2) {			
			switch (impactCategoryDescriptor.getName()) {
			case category1:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category1Label);
				break;
			case category2:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category2Label);
				break;
			case category3:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category3Label);
				break;
			case category4:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category4Label);
				break;
			case category5:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category5Label);
				break;
			case category6:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category6Label);
				break;
			case category7:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category7Label);
				break;	
			case category8:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category8Label);
				break;
			case category9:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category9Label);
				break;
			case category10:
				dataset.addValue(simpleResultProvider2.getTotalImpactResult(impactCategoryDescriptor).getValue(), series2, category10Label);
				break;
			}		
		}
     
        this.chartDataset = dataset;        
    }
    
	public void exportComparisonChartToVCN(String folderId) {		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ChartUtilities.writeChartAsPNG(baos, barChart, 1024, 768);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		byte[] chart_content = baos.toByteArray();
		
		CMISConnector.saveComparisonChartAsPNG(chart_content, folderId);
	}
	
}
