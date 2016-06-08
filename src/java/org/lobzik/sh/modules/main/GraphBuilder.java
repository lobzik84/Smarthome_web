package org.lobzik.sh.modules.main;

import java.io.OutputStream;
import java.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.awt.Color;
import java.awt.Font;
import java.sql.Connection;
import java.text.SimpleDateFormat;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYSplineRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

import org.lobzik.sh.CommonData;
import org.lobzik.tools.CommonTools;
import org.lobzik.tools.db.mysql.DBSelect;
import org.lobzik.tools.db.mysql.DBTools;
@SuppressWarnings({ "rawtypes" })
public class GraphBuilder {
	
	private HashMap configMap = null;
	public GraphBuilder(HashMap config)
	{
		configMap = config;
		
	}
	
	public void service(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		response.setContentType("image/png");
		OutputStream os = response.getOutputStream(); 
        int graphId = CommonTools.parseInt(request.getParameter("graphId"), 1);
		Connection conn = null;
		try
		{
			conn = DBTools.openConnection(CommonData.dataSourceName);
			String sSQL = " SELECT * FROM GRAPHS G WHERE ID =" + graphId;
			List<HashMap> result = DBSelect.getRows(sSQL, conn);
			HashMap graphMap = result.get(0);
			String graphName = (String)graphMap.get("NAME");
			String units = (String)graphMap.get("UNITS");
			int graphType = CommonTools.parseInt(graphMap.get("TYPE"), 0);
			String period = request.getParameter("period"); 
			JFreeChart chart = null;
			if (graphType == 1)
			{
				if ("year".equalsIgnoreCase(period))
				{
					chart = getRegularChartForYear(graphId, graphName, units, conn);
				}
				else if ("month".equalsIgnoreCase(period))
				{
					chart = getRegularChartForMonth(graphId, graphName, units, conn);
				}
				else
				{
					chart = getRegularChartForDay(graphId, graphName, units, conn);
				}
			}
			else if (graphType == 2)
			{
				if ("year".equalsIgnoreCase(period))
				{
					chart = getCounterBarForYear(graphId, graphName, units, conn);
				}
				else if ("month".equalsIgnoreCase(period))
				{
					chart = getCounterBarForMonth(graphId, graphName, units, conn);
				}
				else
				{
					//chart = getCounterChartForDay(graphId, graphName, units, conn);
                                        chart = getCounterBarForDay(graphId, graphName, units, conn);
				}
			}
			DBTools.closeConnection(conn);
    		if (chart != null) 
    			ChartUtilities.writeChartAsPNG(os, chart, CommonTools.parseInt(configMap.get("GraphWidth"), 640), CommonTools.parseInt(configMap.get("GraphHeight"), 480));
    		response.flushBuffer();
    		os.flush();
    		os.close();
		}
		catch (Exception e)
		{
			DBTools.closeConnection(conn);
			throw e;
		}	
	}
	
	private JFreeChart getCounterChartForDay(int graphId, String graphName, String units, Connection conn) throws Exception
	{
		String sSQL = 	" SELECT S.* FROM STATS S INNER JOIN PARAMETERS P ON P.ID = S.PARAMETER_ID " + 
		" WHERE P.GRAPH_ID = " + graphId + " AND DATEDIFF(CURDATE(), DATE) <= 1 ORDER BY DATE ";
		List<HashMap> result = DBSelect.getRows(sSQL, conn);
		HashMap<Integer, List<HashMap>> parMap = new  HashMap<Integer, List<HashMap>>();
		for (HashMap h: result)
		{
			Integer paramId = (Integer)h.get("PARAMETER_ID");
			if (parMap.get(paramId) == null)
			{
				ArrayList<HashMap> parList = new ArrayList<HashMap>();
				parList.add(h);
				parMap.put(paramId, parList);
			}
			else
				parMap.get(paramId).add(h);
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		for (Integer paramId: parMap.keySet())
		{
			List<HashMap> res = parMap.get(paramId);
			long c = ((Date)res.get(0).get("DATE")).getTime();
			TimeSeries series = new TimeSeries(CommonData.parameters.getDescription(paramId));
			double prev = 0;
			for (HashMap h: res)
			{
				double value = CommonTools.parseDouble(h.get("VALUE"), 0);
				if (prev == 0) prev = value;
				Date date = (Date)h.get("DATE");
				if (date.getTime() - c >= 0)
				{
					while (date.getTime() - c >= 0)
					{
						c = c + 3600 * 1000;
						series.addOrUpdate(new FixedMillisecond(c), value - prev);
						prev = value;
					}				
				}
			}
			dataset.addSeries(series);
		}
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
        		graphName,  // title
                "Время",             // x-axis label
                units + " в час",   // y-axis label
                dataset,            // data
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
            );
        
        chart.setBackgroundPaint(Color.white);
        
        XYSplineRenderer renderer = new XYSplineRenderer();
        
        //XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        renderer.setDrawSeriesLineAsPath(true);

        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRenderer(renderer);
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));
        return chart;
	}
	
        private JFreeChart getCounterBarForDay(int graphId, String graphName, String units, Connection conn) throws Exception
	{
		String sSQL = 	" SELECT S.* FROM STATS S INNER JOIN PARAMETERS P ON P.ID = S.PARAMETER_ID " + 
		" WHERE P.GRAPH_ID = " + graphId + " AND DATEDIFF(CURDATE(), DATE) <= 1 ORDER BY DATE ";
		List<HashMap> result = DBSelect.getRows(sSQL, conn);
		HashMap<Integer, List<HashMap>> parMap = new  HashMap<Integer, List<HashMap>>();
		for (HashMap h: result)
		{
			Integer paramId = (Integer)h.get("PARAMETER_ID");
			if (parMap.get(paramId) == null)
			{
				ArrayList<HashMap> parList = new ArrayList<HashMap>();
				parList.add(h);
				parMap.put(paramId, parList);
			}
			else
				parMap.get(paramId).add(h);
		}
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		for (Integer paramId: parMap.keySet())
		{
			List<HashMap> res = parMap.get(paramId);
			double prev =  CommonTools.parseDouble(res.get(0).get("VALUE"), 0);
                        double value = 0;
			Calendar c = Calendar.getInstance();
                        c.setTime(new Date());
                        c.add(Calendar.DAY_OF_MONTH, -1);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
                        int i = 0;
                        do {
                            String time = sdf.format(c.getTime());
                            c.add(Calendar.MINUTE, 30);
                            for (; i < res.size(); i++) {
                                HashMap h = res.get(i);
                                Date date = (Date)h.get("DATE");
                                if (i >= res.size() - 1) {
                                    value = CommonTools.parseDouble(res.get(i).get("VALUE"), 0);
                                    break;
                                }
                                if ( date.getTime() > c.getTime().getTime()) {
                                    value = CommonTools.parseDouble(res.get(i>0?i-1:i).get("VALUE"), 0);
                                    break;
                                }
                            }
                            
                            if (System.currentTimeMillis() - c.getTime().getTime() < 23.5 * 60 * 60 * 1000) { //limit graph range, otherwise bars gets overlapped as they having same label
                                 dataset.addValue(value - prev, CommonData.parameters.getDescription(paramId), time);
                            }
                            prev = value;

                        } while (c.getTimeInMillis() < System.currentTimeMillis());
                        
			
		}
		
		JFreeChart chart = ChartFactory.createBarChart(
        		graphName,  // title
                "Время",             // x-axis label
                units + " за полчаса",   // y-axis label
                dataset,            // data
                PlotOrientation.VERTICAL, 
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
            );
        
        chart.setBackgroundPaint(Color.white);
        
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT); 

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        
        final CategoryAxis domainAxis = plot.getDomainAxis(); 
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        
        Font font = new Font("Dialog", Font.PLAIN, 12);
        domainAxis.setTickLabelFont(font);
        
        domainAxis.setLowerMargin(0.01);
        domainAxis.setUpperMargin(0.01);
        
        return chart;
	}
        
	private JFreeChart getCounterBarForMonth(int graphId, String graphName, String units, Connection conn) throws Exception
	{
		String sSQL = 	" SELECT S.* FROM STATS S INNER JOIN PARAMETERS P ON P.ID = S.PARAMETER_ID " + 
		" WHERE P.GRAPH_ID = " + graphId + " AND DATEDIFF(CURDATE(), DATE) <= 32 ORDER BY DATE ";
		List<HashMap> result = DBSelect.getRows(sSQL, conn);
		HashMap<Integer, List<HashMap>> parMap = new  HashMap<Integer, List<HashMap>>();
		for (HashMap h: result)
		{
			Integer paramId = (Integer)h.get("PARAMETER_ID");
			if (parMap.get(paramId) == null)
			{
				ArrayList<HashMap> parList = new ArrayList<HashMap>();
				parList.add(h);
				parMap.put(paramId, parList);
			}
			else
				parMap.get(paramId).add(h);
		}
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM");
		for (Integer paramId: parMap.keySet())
		{
			List<HashMap> res = parMap.get(paramId);
			Calendar c = Calendar.getInstance();
			c.add(Calendar.MONTH, -1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.add(Calendar.SECOND, -1);
			double prev = 0;
			int i = 0;
			for (HashMap h: res)
			{
				double value = CommonTools.parseDouble(h.get("VALUE"), 0);
				if (prev == 0) prev = value;
				Date date = (Date)h.get("DATE");
				if (date.getTime() - c.getTimeInMillis() >= 0 || i == res.size()-1)
				{
					do 
					{
						dataset.addValue(value - prev, CommonData.parameters.getDescription(paramId), sdf.format(c.getTime()));
						prev = value;
						c.add(Calendar.DAY_OF_MONTH, 1);
					}	
					while (date.getTime() - c.getTimeInMillis() >= 0);
				}
				i++;
			}
                    
		}
		
		JFreeChart chart = ChartFactory.createBarChart(
        		graphName,  // title
                "Дата",             // x-axis label
                units + " за день",   // y-axis label
                dataset,            // data
                PlotOrientation.VERTICAL, 
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
            );
        
        chart.setBackgroundPaint(Color.white);
        
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT); 

        
        final CategoryAxis domainAxis = plot.getDomainAxis(); 
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        Font font = new Font("Dialog", Font.PLAIN, 12);
        domainAxis.setTickLabelFont(font);
        domainAxis.setLowerMargin(0.01);
        domainAxis.setUpperMargin(0.01);
        
        return chart;

	}
	
	private JFreeChart getCounterBarForYear(int graphId, String graphName, String units, Connection conn) throws Exception
	{
		String sSQL = 	" SELECT S.* FROM STATS S INNER JOIN PARAMETERS P ON P.ID = S.PARAMETER_ID " + 
		" WHERE P.GRAPH_ID = " + graphId + " AND DATEDIFF(CURDATE(), DATE) <= 365 ORDER BY DATE ";
		List<HashMap>result = DBSelect.getRows(sSQL, conn);
		HashMap<Integer, List<HashMap>> parMap = new  HashMap<Integer, List<HashMap>>();
		for (HashMap h: result)
		{
			Integer paramId = (Integer)h.get("PARAMETER_ID");
			if (parMap.get(paramId) == null)
			{
				ArrayList<HashMap> parList = new ArrayList<HashMap>();
				parList.add(h);
				parMap.put(paramId, parList);
			}
			else
				parMap.get(paramId).add(h);
		}
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		for (Integer paramId: parMap.keySet())
		{
			List<HashMap> res = parMap.get(paramId);
			Calendar c = Calendar.getInstance();
			c.add(Calendar.YEAR, -1);
			c.add(Calendar.MONTH, 1);
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR_OF_DAY, 0);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			double prev = 0;
			SimpleDateFormat sdf = new SimpleDateFormat("MMM yy");
			int i = 0;
			for (HashMap h: res)
			{
				i++;
				double value = CommonTools.parseDouble(h.get("VALUE"), 0);
				if (prev == 0) prev = value;
				Date date = (Date)h.get("DATE");
				if (date.getTime() - c.getTimeInMillis() >= 0 || i == res.size()-1)
				{
					do 
					{
						c.add(Calendar.MONTH, -1);
						//System.out.println(c.getTime()+ " - " + (value-prev));
						dataset.addValue(value - prev, CommonData.parameters.getDescription(paramId), sdf.format(c.getTime()));
						prev = value;
						c.add(Calendar.MONTH, 2);
					}	
					while (date.getTime() - c.getTimeInMillis() >= 0);
				}
			}
		}
		
		JFreeChart chart = ChartFactory.createBarChart(
        		graphName,  // title
                "Дата",             // x-axis label
                units + " за месяц",   // y-axis label
                dataset,            // data
                PlotOrientation.VERTICAL, 
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
            );
        
        chart.setBackgroundPaint(Color.white);
        
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setDomainAxisLocation(AxisLocation.BOTTOM_OR_RIGHT); 

        
        final CategoryAxis domainAxis = plot.getDomainAxis(); 
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);

        Font font = new Font("Dialog", Font.PLAIN, 12);
        domainAxis.setTickLabelFont(font);
        return chart;
	}
	
	private JFreeChart getRegularChartForDay(int graphId, String graphName, String units, Connection conn) throws Exception
	{
		String sSQL = 	" SELECT S.* FROM STATS S INNER JOIN PARAMETERS P ON P.ID = S.PARAMETER_ID " + 
		" WHERE P.GRAPH_ID = " + graphId + " AND DATEDIFF(CURDATE(), DATE) <= 1 ORDER BY DATE ";
		List<HashMap>result = DBSelect.getRows(sSQL, conn);
        HashMap <Integer, TimeSeries> seriesMap = new HashMap<Integer, TimeSeries>();
		for (HashMap h: result)
		{
			double value = CommonTools.parseDouble(h.get("VALUE"), 0);
			Date date = (Date)h.get("DATE");
			int paramId = CommonTools.parseInt(h.get("PARAMETER_ID"), 0);
			TimeSeries series = seriesMap.get(paramId);
			if (series == null)
				series = new TimeSeries(CommonData.parameters.getDescription(paramId));
			series.addOrUpdate(new FixedMillisecond(date), value);
			seriesMap.put(paramId, series);
		}
		
		HashMap<Integer, List<HashMap>> parMap = new  HashMap<Integer, List<HashMap>>();
		for (HashMap h: result)
		{
			Integer paramId = (Integer)h.get("PARAMETER_ID");
			if (parMap.get(paramId) == null)
			{
				ArrayList<HashMap> parList = new ArrayList<HashMap>();
				parList.add(h);
				parMap.put(paramId, parList);
			}
			else
				parMap.get(paramId).add(h);
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		
		for (Integer paramId: parMap.keySet())
		{
			List<HashMap> res = parMap.get(paramId);
			TimeSeries series = new TimeSeries(CommonData.parameters.getDescription(paramId));
			int i = 0;
			double value = 0;
			for (HashMap h: res)
			{
				value = CommonTools.parseDouble(h.get("VALUE"), 0);
				Date date = (Date)h.get("DATE");
				series.addOrUpdate(new FixedMillisecond(date.getTime()), value);
				if (i == res.size()-1)
					series.addOrUpdate(new FixedMillisecond(System.currentTimeMillis()), value);
				i++;
			}
			dataset.addSeries(series);
		}
		
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
				graphName,  // title
		        "Время",             // x-axis label
		        units,   // y-axis label
		        dataset,            // data
		        true,               // create legend?
		        true,               // generate tooltips?
		        false               // generate URLs?
		    );
		
		chart.setBackgroundPaint(Color.white);
		
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		renderer.setBaseShapesVisible(false);
		renderer.setDrawSeriesLineAsPath(true);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.white);
		plot.setDomainGridlinePaint(Color.gray);
		plot.setRangeGridlinePaint(Color.gray);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);
		plot.setRenderer(renderer);
		
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setDateFormatOverride(new SimpleDateFormat("HH:mm"));

		return chart;
	}
	
	private JFreeChart getRegularChartForMonth(int graphId, String graphName, String units, Connection conn) throws Exception
	{
		String sSQL = 	" SELECT S.* FROM STATS S INNER JOIN PARAMETERS P ON P.ID = S.PARAMETER_ID " + 
		" WHERE P.GRAPH_ID = " + graphId + " AND DATEDIFF(CURDATE(), DATE) <= 31 ORDER BY DATE ";
		List<HashMap> result = DBSelect.getRows(sSQL, conn);
	
		HashMap<Integer, List<HashMap>> parMap = new  HashMap<Integer, List<HashMap>>();
		for (HashMap h: result)
		{
			Integer paramId = (Integer)h.get("PARAMETER_ID");
			if (parMap.get(paramId) == null)
			{
				ArrayList<HashMap> parList = new ArrayList<HashMap>();
				parList.add(h);
				parMap.put(paramId, parList);
			}
			else
				parMap.get(paramId).add(h);
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		long period = 12 * 3600 * 1000;
		
		for (Integer paramId: parMap.keySet())
		{
			List<HashMap> res = parMap.get(paramId);
			long c = ((Date)res.get(0).get("DATE")).getTime();
			TimeSeries series = new TimeSeries(CommonData.parameters.getDescription(paramId));
			double sum = 0;
			int counts = 0;
			int i = 0;
			for (HashMap h: res)
			{
				
				double value = CommonTools.parseDouble(h.get("VALUE"), 0);
				Date date = (Date)h.get("DATE");
				if (date.getTime() - c >= period || i == res.size()-1) 
				{
					if (counts > 0)
					{
						series.addOrUpdate(new FixedMillisecond(c + period/2), sum/counts);
						//System.out.println(new Date(c - period/2) + " - " + sum/counts);
					}
					while (date.getTime() - c >= period)
						c = c + period;
					sum = value;
					counts = 1;
				}
				else
				{
					sum += value;
					counts++;
				}
				i++;
			}
			dataset.addSeries(series);
		}
		
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
        		graphName,  // title
                "Дата",             // x-axis label
                units+" (ср. за день)",   // y-axis label
                dataset,            // data
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
            );
        
        chart.setBackgroundPaint(Color.white);
        
        XYSplineRenderer renderer = new XYSplineRenderer();
        //XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRenderer(renderer);

        renderer.setBaseShapesVisible(false);
        renderer.setDrawSeriesLineAsPath(true);
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        axis.setVerticalTickLabels(true);
        Font font = new Font("Dialog", Font.PLAIN, 12);
        axis.setTickLabelFont(font);
        axis.setDateFormatOverride(new SimpleDateFormat("dd MMM"));


		return chart;
	}
	
	private JFreeChart getRegularChartForYear(int graphId, String graphName, String units, Connection conn) throws Exception
	{
		String sSQL = 	" SELECT S.* FROM STATS S INNER JOIN PARAMETERS P ON P.ID = S.PARAMETER_ID " + 
		" WHERE P.GRAPH_ID = " + graphId + " AND DATEDIFF(CURDATE(), DATE) <= 366 ORDER BY DATE ";
		List<HashMap> result = DBSelect.getRows(sSQL, conn);
		
		HashMap<Integer, List<HashMap>> parMap = new  HashMap<Integer, List<HashMap>>();
		for (HashMap h: result)
		{
			Integer paramId = (Integer)h.get("PARAMETER_ID");
			if (parMap.get(paramId) == null)
			{
				ArrayList<HashMap> parList = new ArrayList<HashMap>();
				parList.add(h);
				parMap.put(paramId, parList);
			}
			else
				parMap.get(paramId).add(h);
		}
		TimeSeriesCollection dataset = new TimeSeriesCollection();
		long period = 1 * 24 * 3600 * 1000;
		
		for (Integer paramId: parMap.keySet())
		{
			List<HashMap> res = parMap.get(paramId);
			long c = ((Date)res.get(0).get("DATE")).getTime();
			TimeSeries series = new TimeSeries(CommonData.parameters.getDescription(paramId));
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.YEAR, -1);
			series.addOrUpdate(new FixedMillisecond(cal.getTimeInMillis()), null);
			double sum = 0;
			int counts = 0;
			int i = 0;
			for (HashMap h: res)
			{
				
				double value = CommonTools.parseDouble(h.get("VALUE"), 0);
				Date date = (Date)h.get("DATE");
				if (date.getTime() - c >= period || i == res.size()-1) 
				{
					if (counts > 0)
					{
						series.addOrUpdate(new FixedMillisecond(c + period/2), sum/counts);
						//System.out.println(new Date(c - period/2) + " - " + sum/counts);
					}
					while (date.getTime() - c >= period)
						c = c + period;
					sum = value;
					counts = 1;
				}
				else
				{
					sum += value;
					counts++;
				}
				i++;
			}
			dataset.addSeries(series);
		}
		
        JFreeChart chart = ChartFactory.createTimeSeriesChart(
        		graphName,  // title
                "Дата",             // x-axis label
                units + " (ср. за сутки)",   // y-axis label
                dataset,            // data
                true,               // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
            );
        
        chart.setBackgroundPaint(Color.white);
        
        XYSplineRenderer renderer = new XYSplineRenderer();
        //XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.setRenderer(renderer);

        renderer.setBaseShapesVisible(false);
        renderer.setDrawSeriesLineAsPath(true);
        
        DateAxis axis = (DateAxis) plot.getDomainAxis();
        DateTickUnit unit = new DateTickUnit(DateTickUnitType.MONTH, 1);
        axis.setDateFormatOverride(new SimpleDateFormat("MMM yy"));
        axis.setVerticalTickLabels(true);
        axis.setTickUnit(unit);
        Font font = new Font("Dialog", Font.PLAIN, 12);
        axis.setTickLabelFont(font);
        

		return chart;
	}
}
