package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import danfulea.math.Convertor;
import danfulea.utils.FrameUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Class for automatically peak search in a spectrum. <br>
 * Use this with caution, a good nuclear physicist always setup ROIs manually. 
 * @author Dan Fulea, 14 Jun. 2011
 * 
 */
@SuppressWarnings("serial")
public class PeakSearchFrame extends JFrame implements ActionListener,
ItemListener{
	private GammaAnalysisFrame mf;
	private final Dimension PREFERRED_SIZE = new Dimension(800, 700);
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;
	private final Dimension sizeCb = new Dimension(120, 21);
	private final Dimension smallsizeCb = new Dimension(50, 21);
	private final Dimension chartDimension = new Dimension(700, 140);
	@SuppressWarnings("rawtypes")
	private JComboBox filterCb,smoothCb,widthCb,integralCb,powerCb;
	private String command = "";
	private static final String PEAKSEARCH_COMMAND = "PEAKSEARCH";
	private int idataset = 0;
	private ChartPanel cp,cp_smooth,cp_d1,cp_d2;
	private int ndatderiv=0;
	private double[] yneted;
	private double[] yderiv1;
	private double[] yderiv2;
	private double[] xderiv12;
	
	/**
	 * Constructor
	 * @param mf mf, the GammaAnalysisFrame object
	 */
	public PeakSearchFrame(GammaAnalysisFrame mf){
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("PeakSearch.NAME"));
		
		this.mf=mf;
		
		createGUI();

		setDefaultLookAndFeelDecorated(true);
		FrameUtilities.createImageIcon(
				this.resources.getString("form.icon.url"), this);

		FrameUtilities.centerFrameOnScreen(this);

		setVisible(true);
		mf.setEnabled(false);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// force attemptExit to be called always!
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				attemptExit();

			}
		});
	}
	
	/**
	 * Exit method
	 */
	private void attemptExit() {
		mf.setEnabled(true);
		dispose();
	}

	/**
	 * Setting up the frame size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**
	 * Create GUI
	 */
	private void createGUI() {
		String[] sarray = (String[]) resources.getObject("peakSearch.filterCb");
		filterCb = new JComboBox(sarray);
		String defaultS = sarray[0];// 1st derivative
		filterCb.setSelectedItem((Object) defaultS);
		filterCb.setMaximumRowCount(5);
		filterCb.setPreferredSize(sizeCb);
		filterCb.addItemListener(this);
		sarray = (String[]) resources.getObject("peakSearch.smoothCb");
		smoothCb = new JComboBox(sarray);
		defaultS = sarray[4];// 25
		smoothCb.setSelectedItem((Object) defaultS);
		smoothCb.setMaximumRowCount(5);
		smoothCb.setPreferredSize(smallsizeCb);
		sarray = (String[]) resources.getObject("peakSearch.widthCb");
		widthCb = new JComboBox(sarray);
		defaultS = sarray[5];//10];// 2.0
		widthCb.setSelectedItem((Object) defaultS);
		widthCb.setMaximumRowCount(5);
		widthCb.setPreferredSize(smallsizeCb);
		sarray = (String[]) resources.getObject("peakSearch.integralCb");
		integralCb = new JComboBox(sarray);
		defaultS = sarray[10];// 80
		integralCb.setSelectedItem((Object) defaultS);
		integralCb.setMaximumRowCount(5);
		integralCb.setPreferredSize(sizeCb);
		sarray = (String[]) resources.getObject("peakSearch.powerCb");
		powerCb = new JComboBox(sarray);
		defaultS = sarray[8];//11];// 13
		powerCb.setSelectedItem((Object) defaultS);
		powerCb.setMaximumRowCount(5);
		powerCb.setPreferredSize(sizeCb);
		
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";
		
		JPanel p1=new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p1.setBackground(GammaAnalysisFrame.bkgColor);
		label=new JLabel(resources.getString("peakSearch.filterLb"));label.setForeground(GammaAnalysisFrame.foreColor);
		p1.add(label);
		p1.add(filterCb);
		label=new JLabel(resources.getString("peakSearch.smoothLb"));label.setForeground(GammaAnalysisFrame.foreColor);
		p1.add(label);
		p1.add(smoothCb);
		label=new JLabel(resources.getString("peakSearch.widthLb"));label.setForeground(GammaAnalysisFrame.foreColor);
		p1.add(label);
		p1.add(widthCb);
		
		JPanel p2=new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p2.setBackground(GammaAnalysisFrame.bkgColor);
		label=new JLabel(resources.getString("peakSearch.integralLb"));label.setForeground(GammaAnalysisFrame.foreColor);
		p2.add(label);
		p2.add(integralCb);
		label=new JLabel(resources.getString("peakSearch.powerLb"));label.setForeground(GammaAnalysisFrame.foreColor);
		p2.add(label);
		p2.add(powerCb);
		buttonName = resources.getString("peakSearch.button");
		buttonToolTip = resources
				.getString("peakSearch.button.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName,
				PEAKSEARCH_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("peakSearch.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2.add(button);
		//============GRAPHs
		JFreeChart chart = getGammaChart();//createEmptyChart();
		cp = new ChartPanel(chart, false, true, true, false, true);
		cp.setMouseWheelEnabled(true);// mouse wheel zooming!
		cp.setPreferredSize(chartDimension);

		JFreeChart chartS = createEmptyChart_smooth();
		cp_smooth = new ChartPanel(chartS, false, true, true, false, true);
		cp_smooth.setMouseWheelEnabled(true);// mouse wheel zooming!
		cp_smooth.setPreferredSize(chartDimension);

		JFreeChart chart1 = createEmptyChart_d1();
		cp_d1 = new ChartPanel(chart1, false, true, true, false, true);
		cp_d1.setMouseWheelEnabled(true);// mouse wheel zooming!
		cp_d1.setPreferredSize(chartDimension);

		JFreeChart chart2 = createEmptyChart_d2();
		cp_d2 = new ChartPanel(chart2, false, true, true, false, true);
		cp_d2.setMouseWheelEnabled(true);// mouse wheel zooming!
		cp_d2.setPreferredSize(chartDimension);

		JPanel c1=new JPanel();
		c1.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		c1.setBackground(GammaAnalysisFrame.bkgColor);
		c1.add(cp);
		JPanel c2=new JPanel();
		c2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		c2.setBackground(GammaAnalysisFrame.bkgColor);
		c2.add(cp_smooth);
		JPanel c3=new JPanel();
		c3.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		c3.setBackground(GammaAnalysisFrame.bkgColor);
		c3.add(cp_d1);
		JPanel c4=new JPanel();
		c4.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		c4.setBackground(GammaAnalysisFrame.bkgColor);
		c4.add(cp_d2);

		//=============
		JPanel mainP = new JPanel();
		BoxLayout blmainP = new BoxLayout(mainP, BoxLayout.Y_AXIS);
		mainP.setLayout(blmainP);
		mainP.setBackground(GammaAnalysisFrame.bkgColor);
		mainP.add(p1);
		mainP.add(p2);
		mainP.add(c1);
		mainP.add(c2);
		mainP.add(c3);
		mainP.add(c4);
		
		JPanel content = new JPanel();
		content.add(mainP, BorderLayout.CENTER);
		setContentPane(new JScrollPane(content));
		content.setOpaque(true); // content panes must be opaque
		content.setBackground(GammaAnalysisFrame.bkgColor);
		pack();
	}
	
	/**
	 * Create chart for original spectrum
	 * @return the result
	 */
	private JFreeChart getGammaChart() {		
		int n = mf.channelI.length;
				
		XYSeries series = new XYSeries(resources.getString("peakSearch.chart.data.NAME"));
				
		for (int j = 0; j < n; j++) {
			series.add(mf.channelI[j], mf.pulsesD[j]);			
		}
		
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("peakSearch.chart.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(resources.getString("peakSearch.chart.y"));
		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		// 1st axis
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		// //////////////////////////////////DATASET AND
		// RENDERER/////////////////
		idataset = 0;// channel,pulses main spectrum
		plot.setDataset(idataset, data);// idataset=0!
		XYItemRenderer renderer = new StandardXYItemRenderer(
		// StandardXYItemRenderer.SHAPES_AND_LINES);//LINES);
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesVisibleInLegend(0, false);
		plot.setRenderer(idataset, renderer);// idataset=0!

		idataset = 1;

		JFreeChart chart = new JFreeChart(resources.getString("peakSearch.chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		// chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000,
		// 0, Color.green));		
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}
	// empty not null chart
	@SuppressWarnings("unused")
	/**
	 * Creates an initial empty chart for original spectrum
	 * @return the result
	 */
	private JFreeChart createEmptyChart() {
		// construct an empty XY series
		XYSeries series = new XYSeries(
				resources.getString("peakSearch.chart.data.NAME"));
		// empty series!
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("peakSearch.chart.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("peakSearch.chart.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesVisibleInLegend(0, false);

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);
		
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("peakSearch.chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}
	
	// empty not null chart
	/**
	 * Creates an initial empty chart for smoothed spectrum
	 * @return the result
	 */
	private JFreeChart createEmptyChart_smooth() {
		// construct an empty XY series
		XYSeries series = new XYSeries(
				resources.getString("peakSearch.chart.data.NAME"));
		// empty series!
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("peakSearch.chart.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("peakSearch.chart.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesVisibleInLegend(0, false);

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);
		
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("peakSearch.chart.smooth.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}
	
	/**
	 * Creates an initial empty chart for 1st derivative spectrum
	 * @return the result
	 */
	private JFreeChart createEmptyChart_d1() {
		// construct an empty XY series
		XYSeries series = new XYSeries(
				resources.getString("peakSearch.chart.data.NAME"));
		// empty series!
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("peakSearch.chart.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("peakSearch.chart.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesVisibleInLegend(0, false);

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);
		
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("peakSearch.chart.d1.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}
	
	/**
	 * Creates an initial empty chart for 2nd spectrum
	 * @return the result
	 */
	private JFreeChart createEmptyChart_d2() {
		// construct an empty XY series
		XYSeries series = new XYSeries(
				resources.getString("peakSearch.chart.data.NAME"));
		// empty series!
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("peakSearch.chart.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("peakSearch.chart.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesVisibleInLegend(0, false);

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);
		
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("peakSearch.chart.d2.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}
	
	/**
	 * Create chart for smoothed spectrum
	 * @return the result
	 */
	private JFreeChart getGammaChart_smooth() {		
		int n = ndatderiv;
				
		XYSeries series = new XYSeries(resources.getString("peakSearch.chart.data.NAME"));
				
		for (int j = 0; j < n; j++) {
			series.add(xderiv12[j],yneted[j]);			
		}
		
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("peakSearch.chart.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(resources.getString("peakSearch.chart.y"));
		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		// 1st axis
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		// //////////////////////////////////DATASET AND
		// RENDERER/////////////////		
		plot.setDataset(0, data);// idataset=0!
		XYItemRenderer renderer = new StandardXYItemRenderer(
		// StandardXYItemRenderer.SHAPES_AND_LINES);//LINES);
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesVisibleInLegend(0, false);
		plot.setRenderer(0, renderer);

		JFreeChart chart = new JFreeChart(resources.getString("peakSearch.chart.smooth.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		// chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000,
		// 0, Color.green));		
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}
	
	/**
	 * Create chart for 1st derivative spectrum
	 * @return the result
	 */
	private JFreeChart getGammaChart_d1() {		
		int n = ndatderiv;
				
		XYSeries series = new XYSeries(resources.getString("peakSearch.chart.data.NAME"));
				
		for (int j = 0; j < n; j++) {
			series.add(xderiv12[j],yderiv1[j]);			
		}
		
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("peakSearch.chart.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(resources.getString("peakSearch.chart.y"));
		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		// 1st axis
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		// //////////////////////////////////DATASET AND
		// RENDERER/////////////////		
		plot.setDataset(0, data);// idataset=0!
		XYItemRenderer renderer = new StandardXYItemRenderer(
		// StandardXYItemRenderer.SHAPES_AND_LINES);//LINES);
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesVisibleInLegend(0, false);
		plot.setRenderer(0, renderer);

		JFreeChart chart = new JFreeChart(resources.getString("peakSearch.chart.d1.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		// chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000,
		// 0, Color.green));		
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}
	
	/**
	 * Create chart for 2nd derivative spectrum
	 * @return the result
	 */
	private JFreeChart getGammaChart_d2() {		
		int n = ndatderiv;
				
		XYSeries series = new XYSeries(resources.getString("peakSearch.chart.data.NAME"));
				
		for (int j = 0; j < n; j++) {
			series.add(xderiv12[j],yderiv2[j]);			
		}
		
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("peakSearch.chart.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(resources.getString("peakSearch.chart.y"));
		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		// 1st axis
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		// //////////////////////////////////DATASET AND
		// RENDERER/////////////////		
		plot.setDataset(0, data);// idataset=0!
		XYItemRenderer renderer = new StandardXYItemRenderer(
		// StandardXYItemRenderer.SHAPES_AND_LINES);//LINES);
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		renderer.setSeriesVisibleInLegend(0, false);
		plot.setRenderer(0, renderer);

		JFreeChart chart = new JFreeChart(resources.getString("peakSearch.chart.d2.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		// chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 1000,
		// 0, Color.green));		
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}
	
	/**
	 * Update chart for smoothed spectrum	
	 */
	private void updateChart_smooth(){
		cp_smooth.removeAll();
		JFreeChart gammaChart = getGammaChart_smooth();
		cp_smooth.setChart(gammaChart);
	}
	
	/**
	 * Update chart for 1st derivative spectrum	
	 */
	private void updateChart_d1(){
		cp_d1.removeAll();
		JFreeChart gammaChart = getGammaChart_d1();
		cp_d1.setChart(gammaChart);
	}
	
	/**
	 * Update chart for 2nd derivative spectrum	
	 */
	private void updateChart_d2(){
		cp_d2.removeAll();
		JFreeChart gammaChart = getGammaChart_d2();
		cp_d2.setChart(gammaChart);
	}
	
	/**
	 * Most actions are set here
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		// String command = arg0.getActionCommand();
		command = arg0.getActionCommand();
		if (command.equals(PEAKSEARCH_COMMAND)) {
			//statusRunS = resources.getString("status.computing");
			//startThread();
			peakSearch();
		}
	}

	/**
	 * Jcombobox related actions are set here
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == filterCb) {
			setDefaults();
		}
	}
	
	/**
	 * Set search default parameters
	 */
	private void setDefaults(){
		if(filterCb.getSelectedIndex()==0){
			powerCb.setSelectedIndex(8);//11);
		} else if(filterCb.getSelectedIndex()==1){
			powerCb.setSelectedIndex(3);
		}
	}
	
	/**
	 * Perform peak search
	 */
	private void peakSearch(){
		mf.deleteAllROIs();
		deleteAllROIs();
		int nneted=Convertor.stringToInt((String)smoothCb.getSelectedItem());
		double integral_min=Convertor.stringToDouble((String)integralCb.getSelectedItem());
		double power_min=Convertor.stringToDouble((String)powerCb.getSelectedItem());
		int ifilter=filterCb.getSelectedIndex();//0-1deriv
		double roifac=Convertor.stringToDouble((String)widthCb.getSelectedItem());
		//compute start and end from spectra  
		int iss=new Double(mf.channelI[0]).intValue();//0!!
		int iee=new Double(mf.channelI[mf.channelI.length-1]).intValue();
		
		ndatderiv=iee-iss+1;//2-1=>2 points 1 and 2
		double hderiv=1.0;
		
		yneted=new double[ndatderiv];
		yderiv1=new double[ndatderiv];
		yderiv2=new double[ndatderiv];
		xderiv12=new double[ndatderiv];
		
		double ymPlus=0.0;
		double xstart=0.0;
		double xend=0.0;
		boolean maxokB=false;
		boolean startB=false;
		double integral=0.0;
		double power=0.0;
		//============================smoothing===================
		for (int j=1;j<=nneted;j++){
			for (int i=1;i<=ndatderiv;i++){
				if (j==1){
					yneted[i-1]=mf.pulsesD[iss+i-1];
			        if ((i>2) && (i<ndatderiv)){
			        	yneted[i]=mf.pulsesD[iss+i];
			        }
			        if ((i>2) && (i<ndatderiv-1)){
			        	yneted[i+1]=mf.pulsesD[iss+i+1];
			        }
				}			     
			    if ((i>2) && (i<ndatderiv-1)){
			    	yneted[i-1]=(1.0/35.0)*(-3.0*yneted[i-3]+
			                12.0*yneted[i-2]+17.0*yneted[i-1]+
					12.0*yneted[i]-3.0*yneted[i+1]);
			    }
			}
		}		
		//==========end SMOOTHING=============================
		for (int i=1;i<=ndatderiv;i++){
			//deriv
			if (i==1){
				yderiv1[i-1]=0.0;
			    yderiv2[i-1]=0.0;
			} else {
				yderiv1[i-1]=(yneted[i-1]-yneted[i-2])/hderiv;//1st derivative
			    yderiv2[i-1]=(yderiv1[i-1]-yderiv1[i-2])/hderiv;//2nd derivative
			}
			xderiv12[i-1]=mf.channelI[iss+i-1];
			//end deriv
			
			if (ifilter==0){
				//============PEAK SEARCH by 1st derivate
				if(yderiv1[i-1]>0.0){
					if (yderiv1[i-1]>ymPlus){
						ymPlus=yderiv1[i-1];
						xstart=xderiv12[i-1];
						maxokB=true;
					}
				} else {
					if(maxokB){
						xend=xderiv12[i-1];//System.out.println("dd= "+(xend-xstart));
				      	xstart=xstart-roifac*(xend-xstart);//symmetry to the left				      	
				      	xend=xstart+2.0*(xend-xstart);
				      	//~a trangle area ~1 lob of 2: delta->delta/2
				      	integral=0.25*ymPlus*(xend-xstart);
				      	power=2.0*integral/(xend-xstart);
				      	
				      	if ((integral>integral_min)&&(power>power_min)){
				      		//set rois
				      		setInitialRoi(xstart,xend);
				      	}
					}
					//reset
					ymPlus=0.0;
				    maxokB=false;
				    integral=0.0;
				    power=0.0;
				}
				//============END PEAK SEARCH by 1st derivate============
			} else if (ifilter==1){
				//============PEAK SEARCH by 2st derivate
				if(yderiv2[i-1]<0.0){
					if (!startB){
						startB=true;
				      	xstart=xderiv12[i-1];//zero
					}
					if (Math.abs(yderiv2[i-1])>ymPlus){
						ymPlus=Math.abs(yderiv2[i-1]);
						xend=xderiv12[i-1];//maximum
						if (xend>xstart){
							maxokB=true;//signal to compute
						}				      
					}
					
				} else{
					if(maxokB){
						//xend-xstart here is 0.5 of base!!
					    //delta->delta * 2.0
					    integral=ymPlus*(xend-xstart);//~a trangle area
					    power=0.5*integral/(xend-xstart);
					      
					    xstart=xstart-roifac*(xend-xstart);//symmetry to the left
					    xend=xstart+2.0*(xend-xstart);

					    if ((integral>integral_min)&&(power>power_min)){
					    	//set rois
					        setInitialRoi(xstart,xend);
						}
					}
					 //reset
				    ymPlus=0.0;
				    maxokB=false;
				    startB=false;
				    integral=0.0;
				    power=0.0;
				}
				//============END PEAK SEARCH by 2st derivate
			}//else if (ifilter==1){
			
		}//for (int i=1;i<=ndatderiv;i++)
		
		updateChart_smooth();
		updateChart_d1();
		updateChart_d2();
		
	}
	
	/**
	 * Delete all ROIs
	 */
	private void deleteAllROIs() {
		
		if (idataset-1 == 0) {
			return;
		}

		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				if (idataset > 1) {
					for (int i = 1; i <= 1 + idataset-1 - 1; i++) {
						plot.setDataset(i, null);
						plot.setRenderer(i, null);
					}
					idataset = 1;					
				}
			}
		}

	}
	
	/**
	 * Setup ROI and perform basic computations
	 * @param xstart star channel
	 * @param xend end channel
	 */
	private void setInitialRoi(double xstart, double xend){
		mf.setInitialRoi(xstart, xend);
		
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				
				XYPlot plot = (XYPlot) c.getPlot();
				
				double dd1 = xstart;//channel
				double dd2 = xend;//channel
								
				// displaying the ROI
				XYSeries series2 = new XYSeries("roi");// not shown
				// firstData
				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);
				int n = series.getItemCount();
				// setting roi based on primary data!
				for (int j = 0; j < n; j++) {
					Number x = series.getX(j);
					Number y = series.getY(j);
					if ((dd1 <= x.doubleValue()) && (x.doubleValue() <= dd2)) {
						series2.add(x, y);
					}
				}
				XYSeriesCollection data2 = new XYSeriesCollection(series2);
				plot.setDataset(idataset, data2);// !
				XYItemRenderer renderer2 = new XYAreaRenderer();
				renderer2.setSeriesPaint(0, Color.red);// overlap
				renderer2.setSeriesVisibleInLegend(0, false);
				plot.setRenderer(idataset, renderer2);

				idataset++;				
			}
		}
	}
}
