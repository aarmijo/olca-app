package org.tecnalia.proseco.app.io.png.results;

import java.awt.Color;
import java.awt.GradientPaint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.results.ContributionItem;
import org.openlca.core.results.ImpactResult;
import org.tecnalia.proseco.app.vcn.services.integration.CMISConnector;

public class ProductSystemsNormalizationChart {
	
	DefaultCategoryDataset chartDataset;
	JFreeChart barChart;
	
	public ProductSystemsNormalizationChart(ProductSystem productSystem1, List<ContributionItem<ImpactResult>> contributionItems1, 
    		ProductSystem productSystem2, List<ContributionItem<ImpactResult>> contributionItems2, int targetAmount) {
		createDataset(productSystem1, contributionItems1, productSystem2, contributionItems2);
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
            "Fossil vs. Wind Electricity Production: " + targetAmount + " kWh.",         // chart title
            "Impact Categories (Europe ReCIPe E)",               // domain axis label
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
    private void createDataset(ProductSystem productSystem1, List<ContributionItem<ImpactResult>> contributionItems1, 
    		ProductSystem productSystem2, List<ContributionItem<ImpactResult>> contributionItems2) {
        
        // row keys...
        final String series1 = productSystem1.getName();
        final String series2 = productSystem2.getName();

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
        
        // create the dataset...
        final DefaultCategoryDataset dataset = new DefaultCategoryDataset();        
        
		for (ContributionItem<ImpactResult> contributionItem : contributionItems1) {			
			switch (contributionItem.getItem().getImpactCategory().getName()) {
			case category1:
				dataset.addValue(contributionItem.getAmount(), series1, category1);
				break;
			case category2:
				dataset.addValue(contributionItem.getAmount(), series1, category2);
				break;
			case category3:
				dataset.addValue(contributionItem.getAmount(), series1, category3);
				break;
			case category4:
				dataset.addValue(contributionItem.getAmount(), series1, category4);
				break;
			case category5:
				dataset.addValue(contributionItem.getAmount(), series1, category5);
				break;
			case category6:
				dataset.addValue(contributionItem.getAmount(), series1, category6);
				break;
			case category7:
				dataset.addValue(contributionItem.getAmount(), series1, category7);
				break;	
			case category8:
				dataset.addValue(contributionItem.getAmount(), series1, category8);
				break;
			case category9:
				dataset.addValue(contributionItem.getAmount(), series1, category9);
				break;
			case category10:
				dataset.addValue(contributionItem.getAmount(), series1, category10);
				break;
			}		
		}
		
		for (ContributionItem<ImpactResult> contributionItem : contributionItems2) {			
			switch (contributionItem.getItem().getImpactCategory().getName()) {
			case category1:
				dataset.addValue(contributionItem.getAmount(), series2, category1);
				break;
			case category2:
				dataset.addValue(contributionItem.getAmount(), series2, category2);
				break;
			case category3:
				dataset.addValue(contributionItem.getAmount(), series2, category3);
				break;
			case category4:
				dataset.addValue(contributionItem.getAmount(), series2, category4);
				break;
			case category5:
				dataset.addValue(contributionItem.getAmount(), series2, category5);
				break;
			case category6:
				dataset.addValue(contributionItem.getAmount(), series2, category6);
				break;
			case category7:
				dataset.addValue(contributionItem.getAmount(), series2, category7);
				break;	
			case category8:
				dataset.addValue(contributionItem.getAmount(), series2, category8);
				break;
			case category9:
				dataset.addValue(contributionItem.getAmount(), series2, category9);
				break;
			case category10:
				dataset.addValue(contributionItem.getAmount(), series2, category10);
				break;
			}		
		}        
        this.chartDataset = dataset;        
    }

	public void exportComparisonChartToVCN(String folderId, Boolean normalization) {		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ChartUtilities.writeChartAsPNG(baos, barChart, 1024, 768);
		} catch (IOException e) {			
			e.printStackTrace();
		}
		byte[] chart_content = baos.toByteArray();
		
		CMISConnector.saveComparisonChartAsPNG(chart_content, folderId, normalization);
	}
}
