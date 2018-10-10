package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import danfulea.math.Convertor;
import danfulea.math.numerical.EvalFunc;
import danfulea.math.numerical.Function;
import danfulea.math.numerical.ModelingData;
import danfulea.math.numerical.Sorting;
import danfulea.utils.FrameUtilities;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Class for Energy/FWHM calibration. <br>
 * 
 * @author Dan Fulea, 09 Jul. 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaEnergyFWHMCalibrationFrame extends JFrame implements
		ActionListener, Function {
	private String command;
	public boolean changeCalibration = false;
	private static final String CALIBRATE_COMMAND = "CALIBRATE";
	private static final String INSERT_COMMAND = "INSERT";
	private static final String USE_COMMAND = "USE";
	private static final String REMOVE_COMMAND = "REMOVE";
	private static final String TEST_COMMAND = "TEST";
	private static final String SAVE_COMMAND = "SAVE";
	protected Color bkgColor;
	protected Color foreColor;
	protected Color textAreaBkgColor;
	protected Color textAreaForeColor;
	private GammaAnalysisFrame mf;
	private final Dimension PREFERRED_SIZE = new Dimension(900, 700);
	private final Dimension tableDimension = new Dimension(500, 200);
	private final Dimension textAreaDimension = new Dimension(600, 120);
	private final Dimension chartDimension = new Dimension(400, 250);
	private final Dimension sizeCb = new Dimension(60, 21);
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;
	private JTable dataTable;
	private String[][] dataMatrix;
	private String[][] dataMatrixAccurate;
	private String[] columnNames;
	private ChartPanel cpEn = null;
	private ChartPanel cpFWHM = null;
	@SuppressWarnings("rawtypes")
	private JComboBox penCb, pfwhmCb = null;

	private JTextField energyTf = new JTextField(5);
	private JTextField testTf = new JTextField(5);
	private JTextArea textArea;

	protected double overAllEnergyCalibrationDeviations;
	protected double overAllFWHMCalibrationDeviations;
	protected double en_a3 = 0.0;
	protected double en_a2 = 0.0;
	protected double en_a1 = 0.0;
	protected double en_a0 = 0.0;
	protected double fwhm_a3 = 0.0;
	protected double fwhm_a2 = 0.0;
	protected double fwhm_a1 = 0.0;
	protected double fwhm_a0 = 0.0;

	private static double en_a3_current = 0.0;
	private static double en_a2_current = 0.0;
	private static double en_a1_current = 0.0;
	private static double en_a0_current = 0.0;
	private static double fwhm_a3_current = 0.0;
	private static double fwhm_a2_current = 0.0;
	private static double fwhm_a1_current = 0.0;
	private static double fwhm_a0_current = 0.0;

	protected double fwhm_overallProcentualError = 0.0;
	protected boolean canSaveB = false;
	
	//protected String energyCalName="";
	//protected String fwhmCalName="";

	/**
	 * Constructor. Before creating object of this class, the ROIs must be present via 
	 * insert Roi. 
	 * @param mf GammaAnalysisFrame object
	 * @param dataMatrix data matrix with 2 significant digits containing some data such as ROI id, ROI centroid, 
	 * and ROI FWHM. It is used just for display data. 
	 * @param columnNames, the column names for the calibration table.
	 * @param dataMatrixAccurate same as dataMatrix but with full precision. This is used in calculations.
	 */
	public GammaEnergyFWHMCalibrationFrame(GammaAnalysisFrame mf,
			String[][] dataMatrix, String[] columnNames,
			String[][] dataMatrixAccurate) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("Energy.FWHM.NAME"));
		changeCalibration = false;
		this.mf = mf;
		this.dataMatrix = dataMatrix;
		this.columnNames = columnNames;
		this.dataMatrixAccurate = dataMatrixAccurate;
		this.bkgColor = GammaAnalysisFrame.bkgColor;
		this.foreColor=GammaAnalysisFrame.foreColor;
		this.textAreaBkgColor=GammaAnalysisFrame.textAreaBkgColor;
		this.textAreaForeColor=GammaAnalysisFrame.textAreaForeColor;
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

		energyTf.requestFocusInWindow();
		ModelingData.func = this;// pass the function!!
		canSaveB = false;
	}

	/**
	 * Set both energy and FWHM calibration coefficients.
	 * @param e_a3 e_a3
	 * @param e_a2 e_a2
	 * @param e_a1 e_a1
	 * @param e_a0 e_a0
	 * @param f_a3 f_a3
	 * @param f_a2 f_a2
	 * @param f_a1 f_a1
	 * @param f_a0 f_a0
	 */
	public static void setCalibrations(double e_a3, double e_a2, double e_a1,
			double e_a0, double f_a3, double f_a2, double f_a1, double f_a0) {
		en_a3_current = e_a3;
		en_a2_current = e_a2;
		en_a1_current = e_a1;
		en_a0_current = e_a0;

		fwhm_a3_current = f_a3;
		fwhm_a2_current = f_a2;
		fwhm_a1_current = f_a1;
		fwhm_a0_current = f_a0;

	}

	/**
	 * Set energy calibration. This is an order 3 polynomial. 
	 * The function is Energy[keV] = F(ROI centroid channel).
	 * @param e_a3 e_a3
	 * @param e_a2 e_a2
	 * @param e_a1 e_a1
	 * @param e_a0 e_a0
	 */
	public static void setEnergyCalibration(double e_a3, double e_a2,
			double e_a1, double e_a0) {
		en_a3_current = e_a3;
		en_a2_current = e_a2;
		en_a1_current = e_a1;
		en_a0_current = e_a0;
	}

	/**
	 * Set FWHM calibration. This is an order 3 polynomial. 
	 * The function is FWHM = F(ROI centroid channel).
	 * @param f_a3 f_a3
	 * @param f_a2 f_a2
	 * @param f_a1 f_a1
	 * @param f_a0 f_a0
	 *  
	 */
	public static void setFWHMCalibration(double f_a3, double f_a2,
			double f_a1, double f_a0) {
		fwhm_a3_current = f_a3;
		fwhm_a2_current = f_a2;
		fwhm_a1_current = f_a1;
		fwhm_a0_current = f_a0;
	}

	/**
	 * Exit method
	 */
	private void attemptExit() {
		if (changeCalibration) {
			String title = resources.getString("dialog.resetROI.title");
			String message = resources.getString("dialog.resetROI.message");

			Object[] options = (Object[]) resources
					.getObject("dialog.resetROI.buttons");
			int result = JOptionPane.showOptionDialog(this, message, title,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
					null, options, options[0]);
			if (result == JOptionPane.YES_OPTION) {
				// ///
				mf.resetAllROIs();
			}
		}
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
		// build a customized DefaultTableModel:
		DefaultTableModel dfm = new DefaultTableModel(dataMatrix, columnNames) {

			// Do not allow to alter!
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		// build a customized JTable
		dataTable = new JTable(dfm) {

			// Text CENTER aligned!!
			public TableCellRenderer getCellRenderer(int row, int col) {
				TableCellRenderer renderer = super.getCellRenderer(row, col);

				((JLabel) renderer)
						.setHorizontalAlignment(SwingConstants.CENTER);
				return renderer;
			}
		};
		// some other customization:
		ListSelectionModel rowSM = dataTable.getSelectionModel();
		rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// single
		// selection
		if (mf.roiV != null && mf.roiV.size() != 0)
			dataTable.setRowSelectionInterval(0, 0);// first row!
		// ------------
		JPanel tabP = new JPanel(new BorderLayout());
		tabP.add(new JScrollPane(dataTable));
		tabP.setBackground(GammaAnalysisFrame.bkgColor);
		tabP.setPreferredSize(tableDimension);

		textArea = new JTextArea();
		textArea.setCaretPosition(0);
		textArea.setEditable(false);
		textArea.setText("");
		textArea.setWrapStyleWord(true);
		textArea.setBackground(GammaAnalysisFrame.textAreaBkgColor);
		textArea.setForeground(GammaAnalysisFrame.textAreaForeColor);

		JFreeChart energyChart = createEnergyEmptyChart();
		JFreeChart fwhmChart = createFWHMEmptyChart();
		cpEn = new ChartPanel(energyChart, false, true, true, false, true);
		cpEn.setMouseWheelEnabled(true);// mouse wheel zooming!
		cpFWHM = new ChartPanel(fwhmChart, false, true, true, false, true);
		cpFWHM.setMouseWheelEnabled(true);// mouse wheel zooming!
		cpEn.setPreferredSize(chartDimension);
		cpFWHM.setPreferredSize(chartDimension);
		JPanel p1P = new JPanel();
		p1P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p1P.add(cpEn);
		p1P.add(cpFWHM);
		p1P.setBackground(GammaAnalysisFrame.bkgColor);

		String[] sarray = (String[]) resources
				.getObject("energy.fwhm.polynomes");
		penCb = new JComboBox(sarray);
		penCb.setMaximumRowCount(5);
		penCb.setPreferredSize(sizeCb);
		String s = sarray[0];// default
		penCb.setSelectedItem((Object) s);
		pfwhmCb = new JComboBox(sarray);
		pfwhmCb.setMaximumRowCount(5);
		pfwhmCb.setPreferredSize(sizeCb);
		s = sarray[2];// default
		pfwhmCb.setSelectedItem((Object) s);

		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";

		JPanel p2P = new JPanel();
		p2P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		label = new JLabel(
				resources.getString("energy.fwhm.calib.energyPoynomeLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p2P.add(label);
		p2P.add(penCb);
		label = new JLabel(
				resources.getString("energy.fwhm.calib.fwhmPoynomeLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p2P.add(label);
		p2P.add(pfwhmCb);
		p2P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p3P = new JPanel();
		p3P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p3P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("energy.fwhm.calib.energyLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p3P.add(label);
		p3P.add(energyTf);
		energyTf.addActionListener(this);
		buttonName = resources.getString("energy.fwhm.calib.insert.button");
		buttonToolTip = resources
				.getString("energy.fwhm.calib.insert.button.toolTip");
		buttonIconName = resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, INSERT_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.calib.insert.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p3P.add(button);
		buttonName = resources.getString("energy.fwhm.calib.use.button");
		buttonToolTip = resources
				.getString("energy.fwhm.calib.use.button.toolTip");
		buttonIconName = resources.getString("img.accept");
		button = FrameUtilities.makeButton(buttonIconName, USE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.calib.use.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p3P.add(button);
		buttonName = resources.getString("energy.fwhm.calib.remove.button");
		buttonToolTip = resources
				.getString("energy.fwhm.calib.remove.button.toolTip");
		buttonIconName = resources.getString("img.close");
		button = FrameUtilities.makeButton(buttonIconName, REMOVE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.calib.remove.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p3P.add(button);
		buttonName = resources.getString("energy.fwhm.calib.calibrate.button");
		buttonToolTip = resources
				.getString("energy.fwhm.calib.calibrate.button.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, CALIBRATE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.calib.calibrate.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p3P.add(button);

		JPanel resultP = new JPanel(new BorderLayout());
		resultP.setPreferredSize(textAreaDimension);
		resultP.add(new JScrollPane(textArea), BorderLayout.CENTER);
		resultP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel t1P = new JPanel();
		t1P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		t1P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("energy.fwhm.calib.channelLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		t1P.add(label);
		t1P.add(testTf);
		JPanel t11P = new JPanel();
		t11P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		t11P.setBackground(GammaAnalysisFrame.bkgColor);
		buttonName = resources.getString("energy.fwhm.calib.test.button");
		buttonToolTip = resources
				.getString("energy.fwhm.calib.test.button.toolTip");
		buttonIconName = null;
		button = FrameUtilities.makeButton(buttonIconName, TEST_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.calib.test.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		t11P.add(button);
		buttonName = resources.getString("energy.fwhm.calib.save.button");
		buttonToolTip = resources
				.getString("energy.fwhm.calib.save.button.toolTip");
		buttonIconName = resources.getString("img.save.database");
		button = FrameUtilities.makeButton(buttonIconName, SAVE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.calib.save.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		t11P.add(button);
		JPanel tP = new JPanel();
		BoxLayout bl0 = new BoxLayout(tP, BoxLayout.Y_AXIS);
		tP.setLayout(bl0);
		tP.setBackground(GammaAnalysisFrame.bkgColor);
		tP.add(t1P);
		tP.add(t11P);
		tP.setBorder(FrameUtilities.getGroupBoxBorder(resources
				.getString("energy.fwhm.calib.test.save.border"),GammaAnalysisFrame.foreColor));

		JPanel p4P = new JPanel();
		p4P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p4P.setBackground(GammaAnalysisFrame.bkgColor);
		p4P.add(resultP);
		p4P.add(tP);

		JPanel mainP = new JPanel();
		BoxLayout bl = new BoxLayout(mainP, BoxLayout.Y_AXIS);
		mainP.setLayout(bl);
		mainP.setBackground(GammaAnalysisFrame.bkgColor);
		mainP.add(p2P);
		mainP.add(tabP);
		mainP.add(p3P);
		mainP.add(p1P);
		mainP.add(p4P);
		JPanel content = new JPanel(new BorderLayout());
		content.add(mainP);
		setContentPane(new JScrollPane(content));
		pack();
	}
	
	/**
	 * Internally used by calibrate method. 
	 * It updates the chart with point data series and fit series for energy calibration.
	 * @param x x 
	 * @param y0 y0
	 */
	private void updateEnergyCalibrationGraph(double[] x, double[] y0) {
		cpEn.removeAll();
		imode = ichannelToKevFit;
		int n = x.length;

		XYSeries series = new XYSeries(
				resources.getString("energy.fwhm.calib.chart.data.NAME"));
		XYSeries fitseries = new XYSeries(
				resources.getString("energy.fwhm.calib.chart.fit.NAME"));

		for (int j = 0; j < n; j++) {
			series.add(x[j], y0[j]);
		}
		double dlta = 1.0 / 10.0;
		int i = 0;
		while (true) {
			i++;
			double xx = x[0] + (i - 1) * dlta;
			double yy = F(xx);// function!!
			fitseries.add(xx, yy);
			if (x[0] + (i - 1) * dlta > x[x.length - 1]) {
				break;
			}
		}
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection fitdata = new XYSeriesCollection(fitseries);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("chart.x.Channel"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("energy.fwhm.calib.chart.energy.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.SHAPES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);

		plot.setDataset(1, fitdata);
		XYItemRenderer renderer0 = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		renderer0.setSeriesPaint(0, Color.RED);
		plot.setRenderer(1, renderer0);
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("energy.fwhm.calib.chart.energy.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		cpEn.setChart(chart);
	}

	// empty not null chart
	/**
	 * Initially, create an empty chart for energy calibration.
	 * @return the chart
	 */
	private JFreeChart createEnergyEmptyChart() {
		// construct an empty XY series
		XYSeries series = new XYSeries(
				resources.getString("energy.fwhm.calib.chart.data.NAME"));
		XYSeries fitseries = new XYSeries(
				resources.getString("energy.fwhm.calib.chart.fit.NAME"));
		// empty series!
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection fitdata = new XYSeriesCollection(fitseries);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("chart.x.Channel"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("energy.fwhm.calib.chart.energy.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);

		plot.setDataset(1, fitdata);
		XYItemRenderer renderer0 = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		renderer0.setSeriesPaint(0, Color.RED);
		plot.setRenderer(1, renderer0);
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("energy.fwhm.calib.chart.energy.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}

	/**
	 * Internally used by calibrate method. 
	 * It updates the chart with point data series and fit series for FWHM calibration.
	 * @param x x 
	 * @param y0 y0
	 */
	private void updateFwhmCalibrationGraph(double[] x, double[] y0) {
		cpFWHM.removeAll();
		imode = ichannelToFwhmFit;
		int n = x.length;

		XYSeries series = new XYSeries(
				resources.getString("energy.fwhm.calib.chart.data.NAME"));
		XYSeries fitseries = new XYSeries(
				resources.getString("energy.fwhm.calib.chart.fit.NAME"));

		for (int j = 0; j < n; j++) {
			series.add(x[j], y0[j]);
		}
		double dlta = 1.0 / 10.0;
		int i = 0;
		while (true) {
			i++;
			double xx = x[0] + (i - 1) * dlta;
			double yy = F(xx);// function!!
			fitseries.add(xx, yy);
			if (x[0] + (i - 1) * dlta > x[x.length - 1]) {
				break;
			}
		}
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection fitdata = new XYSeriesCollection(fitseries);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("chart.x.Channel"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("energy.fwhm.calib.chart.fwhm.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.SHAPES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);

		plot.setDataset(1, fitdata);
		XYItemRenderer renderer0 = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		renderer0.setSeriesPaint(0, Color.RED);
		plot.setRenderer(1, renderer0);
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("energy.fwhm.calib.chart.fwhm.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		cpFWHM.setChart(chart);
	}

	// empty not null chart
	/**
	 * Create an empty chart for FWHM calibration
	 * @return the empty chart
	 */
	private JFreeChart createFWHMEmptyChart() {
		// construct an empty XY series
		XYSeries series = new XYSeries(
				resources.getString("energy.fwhm.calib.chart.data.NAME"));
		XYSeries fitseries = new XYSeries(
				resources.getString("energy.fwhm.calib.chart.fit.NAME"));
		// empty series!
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection fitdata = new XYSeriesCollection(fitseries);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("chart.x.Channel"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("energy.fwhm.calib.chart.fwhm.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);

		plot.setDataset(1, fitdata);
		XYItemRenderer renderer0 = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		renderer0.setSeriesPaint(0, Color.RED);
		plot.setRenderer(1, renderer0);
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("energy.fwhm.calib.chart.fwhm.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}

	/**
	 * Setting up actions!
	 */
	public void actionPerformed(ActionEvent arg0) {
		command = arg0.getActionCommand();
		if (command.equals(CALIBRATE_COMMAND)) {
			calibrate();
		} else if (command.equals(INSERT_COMMAND)) {
			insert();
		} else if (command.equals(USE_COMMAND)) {
			use();
		} else if (command.equals(REMOVE_COMMAND)) {
			remove();
		} else if (command.equals(TEST_COMMAND)) {
			test();
		} else if (command.equals(SAVE_COMMAND)) {
			save();
		} else if (arg0.getSource() == energyTf) {// enter!
			insert();
		}
	}

	/**
	 * Calibrations are performed here.
	 */
	private void calibrate() {

		boolean canCalibrateB = false;
		int ndat = 0;
		Vector<Double> chV = new Vector<Double>();
		Vector<Double> fwhmV = new Vector<Double>();
		Vector<Double> en_setV = new Vector<Double>();
		Vector<Integer> rowIndexV = new Vector<Integer>();
		for (int i = 0; i < dataTable.getRowCount(); i++) {
			String s = (String) dataTable.getValueAt(i, 8);// diff
			if (s.equals(resources.getString("difference.yes"))) {
				canCalibrateB = true;// at least 1 YES!!!!
				chV.addElement(Convertor
						.stringToDouble(dataMatrixAccurate[i][1]));// centroid
				fwhmV.addElement(Convertor
						.stringToDouble(dataMatrixAccurate[i][5]));

				s = (String) dataTable.getValueAt(i, 2);// energy_set
				// System.out.println(s);
				double d = 0.0;
				try {
					d = Convertor.stringToDouble(s);
					en_setV.addElement(d);
				} catch (Exception ex) {
					canCalibrateB = false;
					break;
				}
				if (d <= 0.0) {
					canCalibrateB = false;
					break;
				}

				rowIndexV.addElement(i);
				ndat++;
			}
		}

		if (!canCalibrateB) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			canSaveB = false;

			return;
		}

		if (ndat < 2) {
			String title = resources.getString("number.error.title5");
			String message = resources.getString("number.error5");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			canSaveB = false;

			return;
		}

		double[] centroidCh = new double[ndat];// xxx
		double[] energyKev = new double[ndat];// yyy

		int ndat2 = ndat;
		double[] centroidCh2 = new double[ndat2];// xxx
		double[] fwhmCh = new double[ndat2];// yyy

		int[] rowIndex = new int[ndat];// rowIndex for table!
		// data stdev=centroid standard deviation=1.0 channels!
		double[] ssig = new double[ndat];
		double[] ssig2 = new double[ndat2];
		for (int i = 0; i < ndat; i++) {// ndat=ndat2!
			centroidCh[i] = chV.elementAt(i);
			energyKev[i] = en_setV.elementAt(i);

			centroidCh2[i] = chV.elementAt(i);
			fwhmCh[i] = fwhmV.elementAt(i);

			ssig[i] = 1.0;
			ssig2[i] = 1.0;// neglect errors (1, 2 channels are small!!)

			rowIndex[i] = rowIndexV.elementAt(i);
		}
		// dispose vectors
		chV = null;
		en_setV = null;
		fwhmV = null;

		String s = (String) penCb.getSelectedItem();
		int is = Convertor.stringToInt(s);

		s = (String) pfwhmCb.getSelectedItem();
		int is2 = Convertor.stringToInt(s);

		int mma = is + 1;// 4 coefficients max!!
		int mma2 = is2 + 1;// 4 coefficients max!!

		double[] acof = new double[mma];
		int[] iia = new int[mma];
		double[] acof2 = new double[mma2];
		int[] iia2 = new int[mma2];

		for (int i = 0; i < mma; i++) {
			acof[i] = 0.0;// initial guess!! NONE!
			iia[i] = 1;// ALL FIT!
		}
		for (int i = 0; i < mma2; i++) {
			acof2[i] = 0.0;// initial guess!! NONE!
			iia2[i] = 1;// ALL FIT!
		}

		double[][] ccovar = new double[mma][mma];
		double[][] ccovar2 = new double[mma2][mma2];

		ModelingData.lfit(centroidCh, energyKev, ssig, ndat, acof, iia, mma,
				ccovar);

		// set Function:=============
		channelToKeV = new double[acof.length];
		for (int i = 0; i < acof.length; i++) {
			channelToKeV[i] = acof[i];
		}

		int n = channelToKeV.length;
		if (n == 4) {// order 3
			en_a3 = channelToKeV[3];
			en_a2 = channelToKeV[2];
			en_a1 = channelToKeV[1];
			en_a0 = channelToKeV[0];
		} else if (n == 3) {// order 2
			en_a3 = 0.0;
			en_a2 = channelToKeV[2];
			en_a1 = channelToKeV[1];
			en_a0 = channelToKeV[0];
		} else if (n == 2) {// order 1
			en_a3 = 0.0;
			en_a2 = 0.0;
			en_a1 = channelToKeV[1];
			en_a0 = channelToKeV[0];
		}
		// ======================
		ModelingData.lfit(centroidCh2, fwhmCh, ssig2, ndat2, acof2, iia2, mma2,
				ccovar2);

		// -------------
		channelToFwhm = new double[acof2.length];
		for (int i = 0; i < acof2.length; i++) {
			channelToFwhm[i] = acof2[i];
		}

		n = channelToFwhm.length;
		if (n == 4) {// order 3
			fwhm_a3 = channelToFwhm[3];
			fwhm_a2 = channelToFwhm[2];
			fwhm_a1 = channelToFwhm[1];
			fwhm_a0 = channelToFwhm[0];
		} else if (n == 3) {// order 2
			fwhm_a3 = 0.0;
			fwhm_a2 = channelToFwhm[2];
			fwhm_a1 = channelToFwhm[1];
			fwhm_a0 = channelToFwhm[0];
		} else if (n == 2) {// order 1
			fwhm_a3 = 0.0;
			fwhm_a2 = 0.0;
			fwhm_a1 = channelToFwhm[1];
			fwhm_a0 = channelToFwhm[0];
		}

		// --------------
		// ========ALL
		for (int i = 0; i < dataTable.getRowCount(); i++) {
			imode = ichannelToKevFit;
			double centroid = Convertor
					.stringToDouble(dataMatrixAccurate[i][1]);
			double encalib = F(centroid);

			imode = ichannelToFwhmFit;
			double fwhmcalib = F(centroid);

			s = Convertor.formatNumber(encalib, 2);
			dataTable.setValueAt(s, i, 3);
			s = Convertor.formatNumber(fwhmcalib, 2);
			dataTable.setValueAt(s, i, 6);

			double energy=encalib;//formal!
			s = (String) dataTable.getValueAt(i, 2);// energy_set
			if (s!=null){
				double d = Convertor.stringToDouble(s);
				energy = d;
			}

			double diffD = 100.0 * Math.abs(encalib - energy) / energy;
			s = Convertor.formatNumber(diffD, 2);
			dataTable.setValueAt(s, i, 4);

			double fwhm = Convertor.stringToDouble(dataMatrixAccurate[i][5]);
			diffD = 100.0 * Math.abs(fwhmcalib - fwhm) / fwhm;
			s = Convertor.formatNumber(diffD, 2);
			dataTable.setValueAt(s, i, 7);
		}

		// =============
		double overAllDiff = 0.0;
		double overAllDiff2 = 0.0;
		for (int i = 0; i < centroidCh.length; i++) {
			imode = ichannelToKevFit;
			double encalib = F(centroidCh[i]);
			imode = ichannelToFwhmFit;
			double fwhmcalib = F(centroidCh[i]);

			double diffD = 100.0 * Math.abs(encalib - energyKev[i])
					/ energyKev[i];
			overAllDiff = overAllDiff + diffD;

			diffD = 100.0 * Math.abs(fwhmcalib - fwhmCh[i]) / fwhmCh[i];
			overAllDiff2 = overAllDiff2 + diffD;
		}
		overAllDiff = overAllDiff / centroidCh.length;
		overAllDiff2 = overAllDiff2 / centroidCh.length;

		overAllEnergyCalibrationDeviations = overAllDiff;
		overAllFWHMCalibrationDeviations = overAllDiff2;
		fwhm_overallProcentualError = overAllFWHMCalibrationDeviations;
		canSaveB = true;// we have data to save!

		// update charts!
		updateEnergyCalibrationGraph(centroidCh, energyKev);
		updateFwhmCalibrationGraph(centroidCh, fwhmCh);

		// y = sum i a_i × afunc_i(x)
		// y= a0 x y[0] (=1.0)+a1 x y[1] (-x)+ a2 x y[2] (=x*x)......

		// NOW DISPLAY RESULTS IN TEXT AREA:
		textArea.selectAll();
		textArea.replaceSelection("");

		s = resources.getString("energy.fwhm.calib.textArea.energyCal");
		textArea.append(s + "\n");
		s = en_a3 + "; " + en_a2 + "; " + en_a1 + "; " + en_a0;
		textArea.append(s + "\n");
		s = resources.getString("energy.fwhm.calib.textArea.energyDiff");
		textArea.append(s + "\n");
		s = "" + overAllEnergyCalibrationDeviations;
		textArea.append(s + "\n");

		s = resources.getString("energy.fwhm.calib.textArea.fwhmCal");
		textArea.append(s + "\n");
		s = fwhm_a3 + "; " + fwhm_a2 + "; " + fwhm_a1 + "; " + fwhm_a0;
		textArea.append(s + "\n");
		s = resources.getString("energy.fwhm.calib.textArea.fwhmDiff");
		textArea.append(s + "\n");
		s = "" + overAllFWHMCalibrationDeviations;
		textArea.append(s + "\n");
	}

	@SuppressWarnings("unused")
	/**
	 * Get channel from keV
	 * @param kev keV
	 * @return channel
	 */
	private double getChannelFromKeV(double kev) {

		double x = 0.0;
		int n = 4;// channelToKeV.length;
		if (en_a3 != 0.0) {
			n = 4;
		} else if (en_a2 != 0.0) {
			n = 3;
		} else if (en_a1 != 0.0) {
			n = 2;
		}
		n = n - 1;
		if (n == 1) {
			// kev=ax+b
			x = (kev - en_a0) / en_a1;
		} else if (n == 2) {
			// quadratic: kev=ax2+bx+c
			double[] dd = EvalFunc.quadratic(en_a2, en_a1, en_a0 - kev);
			// most likely we have 2 roots, one negative. Since channel are
			// spread from
			// channel[0]=0 to channel[n] we want THE MINIMUM POSITIVE
			// SOLUTION!!!!
			Sorting.sort(dd.length, dd);
			for (int i = 0; i < dd.length; i++) {
				if (dd[i] >= 0.0) {
					x = dd[i];
					break;
				}
			}
		} else if (n == 3) {
			// cubic: kev=ax3+bx2+cx+d
			double[] dd = EvalFunc.cubic(en_a3, en_a2, en_a1, en_a0 - kev);

			// most likely we have 3 roots, one negative. We have to choose
			// between
			// the remaining two positive roots! Well, since channel are spread
			// from
			// channel[0]=0 to channel[n] we want THE MINIMUM POSITIVE
			// SOLUTION!!!!
			Sorting.sort(dd.length, dd);
			for (int i = 0; i < dd.length; i++) {
				if (dd[i] >= 0.0) {
					x = dd[i];
					break;
				}
			}

		}

		return x;
		// "channelized!!
		// x = Math.floor(x);
		// if (x > MAXCHANNEL)
		// x = MAXCHANNEL;
		// if (x < MINCHANNEL)
		// x = MINCHANNEL;
		// return x;
	}

	/**
	 * Insert data on initialization table.
	 */
	private void insert() {
		String s = energyTf.getText();
		int rowCount = dataTable.getRowCount();
		int selRow = dataTable.getSelectedRow();
		int toBeSelected = selRow + 1;
		if (selRow == -1) {
			return;
		}
		// test............................
		boolean nulneg = false;
		try {
			double en = Convertor.stringToDouble(energyTf.getText());
			if (en <= 0)
				nulneg = true;
		} catch (Exception e) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
			return;
		}
		if (nulneg) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// ---------------------------
		dataTable.setValueAt(s, selRow, 2);
		s = resources.getString("difference.yes");
		dataTable.setValueAt(s, selRow, 8);

		if (selRow == rowCount - 1) {// the last
			toBeSelected = 0;
		}
		dataTable.setRowSelectionInterval(toBeSelected, toBeSelected);

		energyTf.setText("");
		energyTf.requestFocusInWindow();
	}

	/**
	 * Use table selected data for calibration (function fit). 
	 */
	private void use() {
		String s = resources.getString("difference.yes");
		int selRow = dataTable.getSelectedRow();
		if (selRow == -1) {
			return;
		}
		dataTable.setValueAt(s, selRow, 8);
	}

	/**
	 * Remove table selected data for calibration (function fit).
	 */
	private void remove() {
		String s = resources.getString("difference.no");
		int selRow = dataTable.getSelectedRow();
		if (selRow == -1) {
			return;
		}
		dataTable.setValueAt(s, selRow, 8);
	}

	/**
	 * Test calibrations
	 */
	private void test() {
		String s = testTf.getText();
		boolean negB = false;
		double d = 0.0;// channel
		try {
			d = Convertor.stringToDouble(s);

		} catch (Exception ex) {
			negB = true;
		}
		if (d < 0.0) {
			negB = true;
		}
		if (negB) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		textArea.selectAll();
		textArea.replaceSelection("");

		double dd = 0.0;
		dd = en_a3_current * d * d * d + en_a2_current * d * d + en_a1_current
				* d + en_a0_current;
		s = resources.getString("energy.fwhm.calib.textArea.testOld");
		textArea.append(s + dd + "\n");

		dd = en_a3 * d * d * d + en_a2 * d * d + en_a1 * d + en_a0;
		s = resources.getString("energy.fwhm.calib.textArea.testNew");
		textArea.append(s + dd + "\n");

		dd = fwhm_a3_current * d * d * d + fwhm_a2_current * d * d
				+ fwhm_a1_current * d + fwhm_a0_current;
		s = resources.getString("energy.fwhm.calib.textArea.testOld2");
		textArea.append(s + dd + "\n");

		dd = fwhm_a3 * d * d * d + fwhm_a2 * d * d + fwhm_a1 * d + fwhm_a0;
		s = resources.getString("energy.fwhm.calib.textArea.testNew2");
		textArea.append(s + dd + "\n");

	}

	/**
	 * Go to database in order to save/set calibrations. 
	 */
	private void save() {
		new GammaEnergyFWHMCalibrationSaveFrame(this);
	}

	// =========IMPLEMENTATION OF FUNCTION INTERFACE===================
	/**
	 * Interface method
	 */
	public void printSequence(String s) {// for printing
		System.out.println(s);
	}

	private int imode = 0;
	public static final int ichannelToKevFit = 0;
	// public static final int iKevToChannelFit=1;
	public static final int ichannelToFwhmFit = 2;

	private double[] channelToKeV;
	// private double[] keVToChannel;
	private double[] channelToFwhm;

	/**
	 * Calibration fitting function
	 */
	public double F(double x) {// y=f(x)
		double y = 0.0;
		if (imode == ichannelToKevFit) {
			for (int i = 0; i < channelToKeV.length; i++) {
				y = y + channelToKeV[i] * Math.pow(x, i);
			}
		}
		// else if (imode==iKevToChannelFit){
		// for(int i=0;i<keVToChannel.length;i++){
		// y=y+keVToChannel[i]*Math.pow(x, i);
		// }
		// }
		else if (imode == ichannelToFwhmFit) {
			for (int i = 0; i < channelToFwhm.length; i++) {
				y = y + channelToFwhm[i] * Math.pow(x, i);
			}
		}

		return y;
		// return 0.0;
	}

	// --------------------------------------------------------
	public double[] FD(double x) {// 0=->y=f(x) and 1-> dy=f'(x)
		return null;
	}

	public double MF(double[] x) {// y=f(x1,x2,...)
		return 0.0;
	}

	public double[] DMF(double[] x) {// y'1=df(x1,x2,...)/dx1;...the vector
										// gradient
		// df[1..n] evaluated at the input point x
		return null;
	}

	// ==============3D func====================
	public double F3D(double x, double y, double z) {
		return 0.0;
	}

	public double yy1(double x) {
		return 0.0;
	}

	public double yy2(double x) {
		return 0.0;
	}

	public double z1(double x, double y) {
		return 0.0;
	}

	public double z2(double x, double y) {
		return 0.0;
	}

	// end 3d====================================
	// non linear equation systems: root finding
	public double[] vecfunc(int n, double[] x) {
		return null;
	}

	// ==========================================
	public double[] aF(double x, int ma) {// poli fit
		double[] y = new double[ma];

		for (int i = 0; i < ma; i++) {
			y[i] = Math.pow(x, i);
		}
		// y[0]=1.0;
		// y[1]=x;
		// y[2]=x*x;
		// y[3]=x*x*x;
		return y;
	}

	// fdf used in mrqmin-mrqcof routine!!!!!!!!!!!!!!!!!!
	public double fdf(double x, double[] a, double[] dyda, int na) {
		// gauss fit etc. nonliniar
		// double y = 0.0;

		// double expt = Math.exp(-(x - a[1]) * (x - a[1]) / (2.0 * a[2] *
		// a[2]));
		// y = a[0] * expt;
		// dyda[0] = expt;
		// dyda[1] = y * ((x - a[1]) / (a[2] * a[2]));
		// dyda[2] = y * ((x - a[1]) * (x - a[1]) / (a[2] * a[2] * a[2]));
		return 0.0;// y;
	}

	// ===========================================================
	public double[] derivF(double x, double[] y) {
		return null;
	}

	// ============2point
	public void load(double x1, double[] v, double[] y) {
		return;
	}

	public void load1(double x1, double[] v, double[] y) {
		return;
	}

	public void load2(double x2, double[] v, int nn2, double[] y) {
		return;
	}

	public void score(double x2, double[] y, double[] f) {
		return;
	}

	public void difeq(int k, int k1, int k2, int jsf, int is1, int isf,
			int indexv[], int ne, double[][] s, double[][] y) {
		return;
	}

	// ================================================
	public double g(double t) {// g(t)=FREDHOLM
		return 0.0;
	}

	public double ak(double t, double s) {// KERNEL
		return 0.0;
	}

	public double g(int k, double t) {// voltera
		return 0.0;
	}

	public double ak(int k, int l, double t, double s) {// voltera
		return 0.0;
	}

	public void kermom(double[] w, double y, int m) {
		return;
	}

	// =========END IMPLEMENTATION OF FUNCTION INTERFACE===================
}
