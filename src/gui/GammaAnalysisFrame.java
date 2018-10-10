package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import danfulea.utils.ScanDiskLFGui;
import danfulea.utils.TimeUtilities;
import danfulea.db.DatabaseAgent;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.math.Convertor;
import danfulea.math.Sort;
import danfulea.math.numerical.EvalFunc;
import danfulea.math.numerical.Sorting;
import danfulea.phys.AmbientalBkgRetriever;
import danfulea.phys.GammaRoi;
import danfulea.utils.ExampleFileFilter;
import danfulea.utils.FrameUtilities;

import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.Layer;
import org.jfree.ui.LengthAdjustmentType;
import org.jfree.ui.TextAnchor;

/**
 * The graphical user interface (GUI) for gamma analysis tasks. <br>
 * It is designed to compute sample activity using user selected ROIs (Region of Interest) and for each ROI the program computes 
 * its corresponding activity. If more ROIs are assigned to a single nuclide then a final (weighted based on ROI yields) activity is 
 * computed. Of course, all uncertainties are properly handled. <br>
 * As usual, first thing to do is to register a background spectrum. The spectrum must be saved to a file which contains either 
 * two columns data for channel and pulses separated by whitespace characters (SPACE or TAB) or a single column for pulses.<br>
 * Second, set-up a gamma nuclide library suitable for your needs. Decay correction and coincidence correction are based on library.<br>
 * Third, use a standard source (e.g. 152Eu) for energy, FWHM and efficiency calibration. The calibrations are based on ROIs and on the known 
 * quantities such as energy for each ROI centroid and activity for each nuclide in standard source. <br>
 * Last, load the sample spectrum, use a proper BKG spectrum select ROIs and perform analysis. If no peaks in ROI, use MDA as final result.<br>
 * <p>
 * Be advise that for accurate results, the sample-detector geometry must be the same as the standard source-detector geometry. Often, one laboratory 
 * has few standard sources, say 152Eu water equivalent composition, and samples can differ both in size and composition. You 
 * need standard sources for each sample size and composition for accurate results which can involve a lot of money and effort. To overcome 
 * this, use Monte Carlo simulation technique to compute the detection efficiency for all kind of sample-detector geometry. The downside 
 * of Monte Carlo method is that all dimensions and composition for detector and source must be known.   
 *    
 * @author Dan Fulea, 17 Apr. 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaAnalysisFrame extends JFrame implements ActionListener,
		ItemListener, Runnable, ChartMouseListener, AmbientalBkgRetriever {

	private final Dimension PREFERRED_SIZE = new Dimension(990, 720);
	private final Dimension sizeCb = new Dimension(60, 21);

	// protected Color bkgColor = new Color(112, 178, 136, 255);//green default
	// protected Color bkgColor = new Color(130, 0, 0, 255);//brown-red..VAMPIRE
	// protected Color bkgColor = new Color(245, 255, 250, 255);//white -cream
	public static Color bkgColor = new Color(230, 255, 210, 255);//Linux mint green alike
	public static Color foreColor = Color.black;//Color.white;
	public static Color textAreaBkgColor = Color.white;//Color.black;
	public static Color textAreaForeColor = Color.black;//Color.yellow;
	public static boolean showLAF=true;

	private final Dimension chartDimension = new Dimension(700, 250);
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources = ResourceBundle
			.getBundle(BASE_RESOURCE_CLASS);
	protected JLabel statusL = new JLabel("Waiting...");
	private JLabel roilabel = new JLabel();
	protected JLabel roiNamelabel = new JLabel();
	private static final String EXIT_COMMAND = "EXIT";
	private static final String ABOUT_COMMAND = "ABOUT";
	private static final String HOWTO_COMMAND = "HOWTO";
	private static final String LOOKANDFEEL_COMMAND = "LOOKANDFEEL";
	private static final String OPENFILE_COMMAND = "OPENFILE";
	private static final String INSERTROI_COMMAND = "INSERTROI";
	private static final String DELETEROI_COMMAND = "DELETEROI";
	private static final String ZOOMIN_COMMAND = "ZOOMIN";
	private static final String ZOOMOUT_COMMAND = "ZOOMOUT";
	private static final String PANLEFT_COMMAND = "PANLEFT";
	private static final String PANRIGHT_COMMAND = "PANRIGHT";
	private static final String PANUP_COMMAND = "PANUP";
	private static final String PANDOWN_COMMAND = "PANDOWN";
	private static final String REFRESH_COMMAND = "REFRESH";
	private static final String MOVEMARKERLEFT_COMMAND = "MOVEMARKERLEFT";
	private static final String MOVEMARKERRIGHT_COMMAND = "MOVEMARKERRIGHT";
	private static final String MOVEMARKERLEFT2_COMMAND = "MOVEMARKERLEFT2";
	private static final String MOVEMARKERRIGHT2_COMMAND = "MOVEMARKERRIGHT2";
	private static final String TODAY_COMMAND = "TODAY";
	private static final String SAVE_COMMAND = "SAVE";
	private static final String OPENDB_COMMAND = "OPENDB";
	private static final String OPENBKG_COMMAND = "OPENBKG";
	private static final String VIEWROI_COMMAND = "VIEWROI";
	private static final String DELETEALLROI_COMMAND = "DELETEALLROI";
	private static final String SETROI_COMMAND = "SETROI";
	private static final String REPORT_COMMAND = "REPORT";
	private static final String LIN_COMMAND = "LIN";
	private static final String LN_COMMAND = "LN";
	private static final String SQRT_COMMAND = "SQRT";
	private static final String KEV_COMMAND = "KEV";
	private static final String CHANNEL_COMMAND = "CHANNEL";
	private static final String UPDATEROIEDGE_COMMAND = "UPDATEROIEDGE";
	private static final String ENFWHM_COMMAND = "ENFWHM";
	private static final String EFF_COMMAND = "EFF";
	private static final String SAMPLE_COMMAND = "SAMPLE";
	private static final String PEAKSEARCH_COMMAND = "PEAKSEARCH";
	private static final String PEAKIDENTIFY_COMMAND = "PEAKIDENTIFY";
	private static final String LIBRARY_COMMAND = "LIBRARY";
	private static final String SHOWBKG_COMMAND = "SHOWGKG";
	private static final String SAVEROI_COMMAND = "SAVEROI";
	private static final String LOADROI_COMMAND = "LOADROI";
	private String command = null;

	@SuppressWarnings("rawtypes")
	protected JComboBox dayCb, monthCb, roiCb = null;
	protected JTextField yearTf = new JTextField(5);
	protected JTextField spectrumLiveTimeTf = new JTextField(8);
	protected JTextField idSpectrumTf = new JTextField(25);
	protected JTextField quantityTf = new JTextField(3);
	protected JTextField quantityUnitTf = new JTextField(3);

	private JCheckBoxMenuItem sqrtItem, lnItem, linItem, bkgItem;
	private JRadioButton keVRb, channelRb = null;

	private volatile Thread appTh = null;// application thread!
	private volatile Thread statusTh = null;
	private int delay = 100;
	private int frameNumber = -1;

	private String statusRunS = "";
	private boolean channelWarningB = false;
	private String spectrumFile = "";

	private ChartPanel cp = null;
	//--------------------------------------------------------------------------------
	//chart = pulses vs channel; charten = pulses vs kev; 
	//chartln = lnpulses vs chanel; chartlnen=lnpulses vs kev
	//chartsqrt = sqrtpulses vs chanel; chartsqrten=sqrtpulses vs kev
	private JFreeChart chart; private JFreeChart charten;
	private JFreeChart chartln;private JFreeChart chartlnen;
	private JFreeChart chartsqrt;private JFreeChart chartsqrten;
	//----------------------------------------------------------------------------------
	private boolean emptyChartB = false;
	private double chartX, chartY = 0.0;
	private int imarker = 0;
	private boolean markerReset = false;
	private final int maxMarkerCount = 2;
	private int idataset = 0;
	private double MINCHANNEL = 0;
	private double MAXCHANNEL = 0;
	private double MINBOUND = 0;// could be minchannel or minkev!!!
	private double MAXBOUND = 0;
	private ValueMarker[] markers = new ValueMarker[maxMarkerCount];
	private XYTextAnnotation[] textAnnots = new XYTextAnnotation[maxMarkerCount];

	private static StringBuffer stringBuffer = new StringBuffer();
	// all instances use this object!=>optimization!

	protected Vector<String> channelV = null;
	protected Vector<String> pulsesV = null;
	protected double[] channelI = null;
	protected double[] pulsesD = null;
	// always an int but make it double for precision!
	protected double globalCounts = 0.0;
	protected double globalBkgCounts = 0.0;
	protected double spectrumLiveTime = 0.0;
	protected double bkgSpectrumLiveTime = 0.0;
	protected double quantity = 1.0;
	protected String spectrumName = "";
	protected String quantityUnit = "kg";
	protected String measurementDate = "";

	private boolean isLinB = true;
	private boolean isLnB = false;
	private boolean isSqrtB = false;
	private boolean isChannelDisplay = true;

	private double[] keVD = null;
	protected double[] bkgpulsesD = null;

	protected Vector<GammaRoi> roiV = new Vector<GammaRoi>();
	private static double en_a3 = 0.0;
	private static double en_a2 = 0.0;
	private static double en_a1 = 0.0;
	private static double en_a0 = 0.0;
	// private double ch_a3=0.0;
	// private double ch_a2=0.0;
	// private double ch_a1=0.0;
	// private double ch_a0=0.0;
	private static double fwhm_a3 = 0.0;
	private static double fwhm_a2 = 0.0;
	private static double fwhm_a1 = 0.0;
	private static double fwhm_a0 = 0.0;
	private static double fwhm_overallProcentualError = 0.0;
	private static double eff_p1_a4 = 0.0;
	private static double eff_p1_a3 = 0.0;
	private static double eff_p1_a2 = 0.0;
	private static double eff_p1_a1 = 0.0;
	private static double eff_p1_a0 = 0.0;
	private static double eff_p2_a4 = 0.0;
	private static double eff_p2_a3 = 0.0;
	private static double eff_p2_a2 = 0.0;
	private static double eff_p2_a1 = 0.0;
	private static double eff_p2_a0 = 0.0;
	private static double eff_crossoverEnergy = 0.0;
	private static double eff_overallProcentualError = 0.0;

	protected int iMDA = 2;
	public static final int MDA_CALCULATION_PASTERNACK = 0;
	public static final int MDA_CALCULATION_CURIE = 1;
	public static final int MDA_CALCULATION_DEFAULT = 2;

	protected int iNet = 0;
	public static final int NET_CALCULATION_NAI = 0;
	public static final int NET_CALCULATION_GE = 1;
	public static final int NET_CALCULATION_GAUSSIAN = 2;

	private JRadioButton naiRb, geRb, gaussRb;
	private double roiConfidenceLevel = 95.0;// 95%..default!
	
	protected static String warningsPI="";
	protected static boolean failPI=false;
	
	private Window parent = null;
	private boolean standalone=true;
	
	protected int iROI_EDGE_PULSES_AVERAGE_BY;
	public static final int ROI_EDGE_PULSES_AVERAGE_BY_3=3;
	public static final int ROI_EDGE_PULSES_AVERAGE_BY_5=5;
	public static final int ROI_EDGE_PULSES_AVERAGE_BY_7=7;
	public static final int ROI_EDGE_PULSES_AVERAGE_BY_9=9;
	
	//for the header=============
	protected String bkgSpectrumName;//="N/A or main spectrum is loaded from DB";
	protected String bkgSpectrumDate;//="N/A";
	protected String energyCalName="";
	protected String fwhmCalName="";
	protected String effCalName="";
	//
	
	private String gammaDB = "";
	/**
	 * The connection
	 */
	private Connection gammadbcon = null;

	/**
	 * Constructor... setting up the application GUI!
	 */
	public GammaAnalysisFrame() {
		//================
		bkgSpectrumName = resources.getString("results.sample.NAOR");
		bkgSpectrumDate = resources.getString("results.sample.NA");
		//==============
		//DBConnection.startDerby();
		this.setTitle(resources.getString("Application.NAME"));
		spectrumLiveTime = 0.0;// reset
		markerReset = false;
		imarker = 0;
		
		//==================
		gammaDB = resources.getString("main.db");		
		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = gammaDB;
		opens = opens + file_sep + dbName;
		gammadbcon = DatabaseAgent.getConnection(opens, "", "");
		//================
		
		// the key to force decision made by attemptExit() method on close!!
		// otherwise...regardless the above decision, the application exit!
		// notes: solved this minor glitch in latest sun java!!
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				attemptExit();
			}
		});

		JMenuBar menuBar = createMenuBar(resources);
		setJMenuBar(menuBar);
		
		createGUI();
		setDefaultLookAndFeelDecorated(true);
		FrameUtilities.createImageIcon(
				this.resources.getString("form.icon.url"), this);

		FrameUtilities.centerFrameOnScreen(this);

		GammaRoi.abg = this;
		retrieveSettingsFromDatabase();

		idSpectrumTf.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
			};

			public void focusLost(FocusEvent e) {
				// if (!e.isTemporary() && isEnabled() ) {
				if (isEnabled()) {
					String content = idSpectrumTf.getText();
					setChartName(content);
					spectrumName = content;
				}
			}
		});
		
		//some defaults
		iROI_EDGE_PULSES_AVERAGE_BY = ROI_EDGE_PULSES_AVERAGE_BY_3;

		setVisible(true);
	}
	
	/**
	 * Constructor when a calling program is involved.
	 * @param frame the frame
	 */
	public GammaAnalysisFrame(Window frame) {
		this();
		this.parent = frame;
		standalone=false;		
	}

	/**
	 * Set the chart name
	 * @param text text
	 */
	private void setChartName(String text) {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				c.setTitle(text);
			}
		}
	}

	/**
	 * Setting up the frame size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

	/**
	 * GUI creation.
	 */
	private void createGUI() {
		// Create the statusbar.
		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		initStatusBar(statusBar);

		JPanel content = new JPanel(new BorderLayout());
		JPanel mainPanel = createMainPanel();
		content.add(mainPanel, BorderLayout.CENTER);
		content.add(statusBar, BorderLayout.PAGE_END);

		setContentPane(new JScrollPane(content));
		content.setOpaque(true); // content panes must be opaque
		pack();
	}

	/**
	 * Initiates the tool bar-like panel.
	 * @return the result
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JPanel initToolBar() {

		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";

		String[] sarray = new String[31];
		for (int i = 1; i <= 31; i++) {
			if (i < 10)
				sarray[i - 1] = "0" + i;
			else
				sarray[i - 1] = Convertor.intToString(i);
		}
		dayCb = new JComboBox(sarray);
		dayCb.setMaximumRowCount(5);
		dayCb.setPreferredSize(sizeCb);

		sarray = new String[12];
		for (int i = 1; i <= 12; i++) {
			if (i < 10)
				sarray[i - 1] = "0" + i;
			else
				sarray[i - 1] = Convertor.intToString(i);
		}
		monthCb = new JComboBox(sarray);
		monthCb.setMaximumRowCount(5);
		monthCb.setPreferredSize(sizeCb);
		// ...
		today();
		// ...
		keVRb = new JRadioButton(resources.getString("rb.kev"));
		keVRb.setForeground(foreColor);
		keVRb.setBackground(bkgColor);
		channelRb = new JRadioButton(resources.getString("rb.channel"));
		channelRb.setBackground(bkgColor);
		channelRb.setForeground(foreColor);
		ButtonGroup group = new ButtonGroup();
		group.add(keVRb);
		group.add(channelRb);
		channelRb.setSelected(true);

		JPanel rbPan = new JPanel();
		rbPan.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		rbPan.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("rb.border"), foreColor));
		rbPan.add(channelRb);
		rbPan.add(keVRb);
		rbPan.setBackground(bkgColor);
		channelRb.setActionCommand(CHANNEL_COMMAND);
		keVRb.setActionCommand(KEV_COMMAND);
		channelRb.addActionListener(this);
		keVRb.addActionListener(this);

		buttonName = resources.getString("toolbar.today");
		buttonToolTip = resources.getString("toolbar.today.toolTip");
		buttonIconName = resources.getString("img.today");
		button = FrameUtilities.makeButton(buttonIconName, TODAY_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("toolbar.today.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		JPanel dateP = new JPanel();
		dateP.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
		dateP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("date.border"), foreColor));
		label = new JLabel(resources.getString("toolbar.day"));
		label.setForeground(foreColor);
		dateP.add(label);
		dateP.add(dayCb);
		label = new JLabel(resources.getString("toolbar.month"));
		label.setForeground(foreColor);
		dateP.add(label);
		dateP.add(monthCb);
		label = new JLabel(resources.getString("toolbar.year"));
		label.setForeground(foreColor);
		dateP.add(label);
		dateP.add(yearTf);
		dateP.add(button);
		dateP.setBackground(bkgColor);
		label.setForeground(foreColor);

		JPanel toolP = new JPanel();
		toolP.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 1));

		JPanel zoomP = new JPanel();
		zoomP.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 4));
		zoomP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("chart.zoom.border"), foreColor));

		JPanel panP = new JPanel();
		panP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 4));
		panP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("chart.navigation.border"), foreColor));

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.zoom.in");
		button = FrameUtilities.makeButton(buttonIconName, ZOOMIN_COMMAND,
				buttonToolTip, buttonName, this, this);
		zoomP.add(button);

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.zoom.out");
		button = FrameUtilities.makeButton(buttonIconName, ZOOMOUT_COMMAND,
				buttonToolTip, buttonName, this, this);
		zoomP.add(button);

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.pan.left");
		button = FrameUtilities.makeButton(buttonIconName, PANLEFT_COMMAND,
				buttonToolTip, buttonName, this, this);
		panP.add(button);

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.pan.right");
		button = FrameUtilities.makeButton(buttonIconName, PANRIGHT_COMMAND,
				buttonToolTip, buttonName, this, this);
		panP.add(button);

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.pan.up");
		button = FrameUtilities.makeButton(buttonIconName, PANUP_COMMAND,
				buttonToolTip, buttonName, this, this);
		panP.add(button);

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.pan.down");
		button = FrameUtilities.makeButton(buttonIconName, PANDOWN_COMMAND,
				buttonToolTip, buttonName, this, this);
		panP.add(button);

		zoomP.setBackground(bkgColor);
		panP.setBackground(bkgColor);

		toolP.add(zoomP);
		toolP.add(panP);

		buttonName = resources.getString("toolbar.refresh");
		buttonToolTip = resources.getString("toolbar.refresh.toolTip");
		buttonIconName = resources.getString("img.pan.refresh");
		button = FrameUtilities.makeButton(buttonIconName, REFRESH_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("toolbar.refresh.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		toolP.add(button);

		toolP.add(rbPan);
		toolP.setBackground(bkgColor);

		JPanel infoP = new JPanel();
		infoP.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 1));
		label = new JLabel(resources.getString("toolbar.spectrum.time"));
		label.setForeground(foreColor);
		infoP.add(label);
		infoP.add(spectrumLiveTimeTf);
		infoP.add(dateP);
		infoP.setBackground(bkgColor);

		JPanel baseinfoP = new JPanel();
		baseinfoP.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 1));
		label = new JLabel(resources.getString("toolbar.spectrum.id"));
		label.setForeground(foreColor);
		baseinfoP.add(label);
		baseinfoP.add(idSpectrumTf);
		idSpectrumTf.setText(resources.getString("chart.NAME"));
		label = new JLabel(resources.getString("toolbar.spectrum.quantity"));
		label.setForeground(foreColor);
		baseinfoP.add(label);
		baseinfoP.add(quantityTf);
		label = new JLabel(resources.getString("toolbar.spectrum.quantityUnit"));
		label.setForeground(foreColor);
		baseinfoP.add(label);
		baseinfoP.add(quantityUnitTf);
		quantityTf.setText("1");
		quantityUnitTf.setText("kg");
		baseinfoP.setBackground(bkgColor);

		JPanel infoBoxP = new JPanel();
		BoxLayout bl03 = new BoxLayout(infoBoxP, BoxLayout.Y_AXIS);
		infoBoxP.setLayout(bl03);
		infoBoxP.add(baseinfoP);
		infoBoxP.add(infoP);
		infoBoxP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("info.border"), foreColor));
		infoBoxP.setBackground(bkgColor);

		JPanel toolBarBoxP = new JPanel();
		BoxLayout bl = new BoxLayout(toolBarBoxP, BoxLayout.Y_AXIS);
		toolBarBoxP.setLayout(bl);
		toolBarBoxP.add(toolP);
		toolBarBoxP.add(infoBoxP);
		toolBarBoxP.setBackground(bkgColor);

		JPanel toolBar = new JPanel(new BorderLayout());
		toolBar.add(toolBarBoxP);

		toolBar.setBackground(bkgColor);

		return toolBar;
	}
	
	@SuppressWarnings("rawtypes")
	/**
	 * Initialize the main panel
	 * @return the result
	 */
	private JPanel createMainPanel() {
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";

		naiRb = new JRadioButton(resources.getString("nai.rb"));
		naiRb.setToolTipText(resources.getString("nai.rb.toolTip"));
		naiRb.setBackground(bkgColor);
		naiRb.setForeground(foreColor);
		geRb = new JRadioButton(resources.getString("ge.rb"));
		geRb.setToolTipText(resources.getString("ge.rb.toolTip"));
		geRb.setBackground(bkgColor);
		geRb.setForeground(foreColor);
		gaussRb = new JRadioButton(resources.getString("gauss.rb"));
		gaussRb.setBackground(bkgColor);
		gaussRb.setForeground(foreColor);
		gaussRb.setToolTipText(resources.getString("gauss.rb.toolTip"));
		ButtonGroup group = new ButtonGroup();
		group.add(naiRb);
		group.add(geRb);
		group.add(gaussRb);
		naiRb.setSelected(true);
		JPanel rbPan = new JPanel();
		BoxLayout blrbPan = new BoxLayout(rbPan, BoxLayout.Y_AXIS);
		rbPan.setLayout(blrbPan);
		rbPan.add(naiRb, null);rbPan.add(Box.createRigidArea(new Dimension(0, 3)));// some space
		rbPan.add(geRb, null);rbPan.add(Box.createRigidArea(new Dimension(0, 3)));// some space
		rbPan.add(gaussRb, null);
		rbPan.setBackground(bkgColor);
		rbPan.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("rb.net.border"), foreColor));

		roiCb = new JComboBox();
		roiCb.setMaximumRowCount(5);
		roiCb.setPreferredSize(sizeCb);
		roiCb.addItemListener(this);

		JPanel markerLP = new JPanel();
		markerLP.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 4));
		markerLP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("marker.left.border"), foreColor));

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.pan.left");
		button = FrameUtilities.makeButton(buttonIconName,
				MOVEMARKERLEFT_COMMAND, buttonToolTip, buttonName, this, this);

		markerLP.add(button);

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.pan.right");
		button = FrameUtilities.makeButton(buttonIconName,
				MOVEMARKERRIGHT_COMMAND, buttonToolTip, buttonName, this, this);
		markerLP.add(button);
		markerLP.setBackground(bkgColor);

		JPanel markerRP = new JPanel();
		markerRP.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 4));
		markerRP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("marker.right.border"), foreColor));

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.pan.left");
		button = FrameUtilities.makeButton(buttonIconName,
				MOVEMARKERLEFT2_COMMAND, buttonToolTip, buttonName, this, this);

		markerRP.add(button);

		buttonName = "";
		buttonToolTip = "";
		buttonIconName = resources.getString("img.pan.right");
		button = FrameUtilities
				.makeButton(buttonIconName, MOVEMARKERRIGHT2_COMMAND,
						buttonToolTip, buttonName, this, this);
		markerRP.add(button);
		markerRP.setBackground(bkgColor);

		JPanel roiCreateP = new JPanel();
		roiCreateP.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
		roiCreateP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("roi.creation.border"), foreColor));
		roiCreateP.add(markerLP);
		roiCreateP.add(markerRP);
		roiCreateP.add(rbPan);
		
		BorderLayout borL = new BorderLayout();
		borL.setVgap(5);
		JPanel butroiP = new JPanel(borL);
		butroiP.setBackground(bkgColor);

		buttonName = resources.getString("roi.insert");
		buttonToolTip = resources.getString("roi.insert.toolTip");
		buttonIconName = resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, INSERTROI_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("roi.insert.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		butroiP.add(button, BorderLayout.NORTH);

		buttonName = resources.getString("roi.edge");
		buttonToolTip = resources.getString("roi.edge.toolTip");
		buttonIconName = resources.getString("img.pan.refresh");
		button = FrameUtilities.makeButton(buttonIconName,
				UPDATEROIEDGE_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("roi.edge.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		butroiP.add(button, BorderLayout.SOUTH);
		roiCreateP.add(butroiP);
		roiCreateP.setBackground(bkgColor);

		JPanel rP = new JPanel();
		rP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("roi.label"));
		label.setForeground(foreColor);
		rP.add(label);
		rP.add(roiCb);
		roilabel.setText(resources.getString("roi.count")
				+ Convertor.intToString(roiV.size()));
		roilabel.setForeground(foreColor);
		rP.setBackground(bkgColor);

		JPanel r00P = new JPanel();
		r00P.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 2));
		roiNamelabel.setText(resources.getString("roi.name.label") + "NoName");
		r00P.add(roiNamelabel);
		roiNamelabel.setForeground(foreColor);
		r00P.setBackground(bkgColor);

		JPanel r01P = new JPanel();
		r01P.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 2));
		r01P.add(roilabel);
		r01P.setBackground(bkgColor);

		JPanel rrP = new JPanel();
		BoxLayout blrrP = new BoxLayout(rrP, BoxLayout.Y_AXIS);
		rrP.setLayout(blrrP);
		rrP.add(rP, null);

		rrP.add(Box.createRigidArea(new Dimension(0, 8)));// some space

		rrP.add(r00P, null);
		rrP.add(r01P, null);
		rrP.setBackground(bkgColor);
		
		JPanel roi1P = new JPanel();
		//roi1P.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		roi1P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		roi1P.add(roiCreateP);
		roi1P.add(rrP);
		//roi1P.add(rbPan);
		roi1P.setBackground(bkgColor);

		JPanel roi2P = new JPanel();
		roi2P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		roi2P.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("roi.operation.border"), foreColor));

		buttonName = resources.getString("roi.view");
		buttonToolTip = resources.getString("roi.view.toolTip");
		buttonIconName = resources.getString("img.view");
		button = FrameUtilities.makeButton(buttonIconName, VIEWROI_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("roi.view.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		roi2P.add(button);

		buttonName = resources.getString("roi.delete");
		buttonToolTip = resources.getString("roi.delete.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName, DELETEROI_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("roi.delete.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		roi2P.add(button);

		buttonName = resources.getString("roi.deleteAll");
		buttonToolTip = resources.getString("roi.deleteAll.toolTip");
		buttonIconName = resources.getString("img.delete.all");
		button = FrameUtilities.makeButton(buttonIconName,
				DELETEALLROI_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("roi.deleteAll.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		roi2P.add(button);

		buttonName = resources.getString("roi.set");
		buttonToolTip = resources.getString("roi.set.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, SETROI_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("roi.set.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		roi2P.add(button);

		buttonName = resources.getString("roi.report");
		buttonToolTip = resources.getString("roi.report.toolTip");
		buttonIconName = resources.getString("img.report");
		button = FrameUtilities.makeButton(buttonIconName, REPORT_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("roi.report.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		roi2P.add(button);

		roi2P.setBackground(bkgColor);
		
		JPanel sampleP = new JPanel();
		sampleP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		buttonName = resources.getString("menu.gammaAnalysis.sample");
		buttonToolTip = resources.getString("menu.gammaAnalysis.sample.toolTip");
		buttonIconName = resources.getString("img.report");
		button = FrameUtilities.makeButton(buttonIconName, SAMPLE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("menu.gammaAnalysis.sample.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		sampleP.add(button);
		sampleP.setBackground(bkgColor);
		
		//
		JPanel sampleroiP = new JPanel();
		sampleroiP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		sampleroiP.add(roi2P);
		sampleroiP.add(sampleP);
		sampleroiP.setBackground(bkgColor);

		JPanel boxP = new JPanel();
		BoxLayout bl = new BoxLayout(boxP, BoxLayout.Y_AXIS);
		boxP.setLayout(bl);
		boxP.add(roi1P);
		boxP.add(sampleroiP);//roi2P);//
		boxP.setBackground(bkgColor);

		JFreeChart gammaChart = createEmptyChart();
		emptyChartB = true;
		// cp = new ChartPanel(gammaChart);//standard,
		// showing a standard pop-up!
		cp = new ChartPanel(gammaChart, false, true, true, false, true);
		cp.addChartMouseListener(this);
		cp.setMouseWheelEnabled(true);// mouse wheel zooming!
		cp.requestFocusInWindow();
		cp.setPreferredSize(chartDimension);
		cp.setBorder(FrameUtilities.getGroupBoxBorder(
				"", foreColor));
		cp.setBackground(bkgColor);
		
		JPanel northPanel = initToolBar();
		JPanel mainP = new JPanel(new BorderLayout());
		mainP.add(northPanel, BorderLayout.NORTH);
		mainP.add(cp, BorderLayout.CENTER);
		mainP.add(boxP, BorderLayout.SOUTH);
		return mainP;
	}

	/**
	 * Setting up the menu bar.
	 * 
	 * @param resources the resources
	 * @return the menu bar
	 */
	private JMenuBar createMenuBar(ResourceBundle resources) {
		// create the menus
		JMenuBar menuBar = new JMenuBar();

		String label;
		Character mnemonic;
		ImageIcon img;
		String imageName = "";

		// the file menu
		label = resources.getString("menu.file");
		mnemonic = (Character) resources.getObject("menu.file.mnemonic");
		JMenu fileMenu = new JMenu(label, true);
		fileMenu.setMnemonic(mnemonic.charValue());

		imageName = resources.getString("img.open.file");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("openFile");
		mnemonic = (Character) resources.getObject("openFile.mnemonic");
		JMenuItem openFileItem = new JMenuItem(label, mnemonic.charValue());
		openFileItem.setActionCommand(OPENFILE_COMMAND);
		openFileItem.addActionListener(this);
		openFileItem.setIcon(img);
		openFileItem.setToolTipText(resources.getString("openFile.toolTip"));
		fileMenu.add(openFileItem);

		imageName = resources.getString("img.open.database");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("openDB");
		mnemonic = (Character) resources.getObject("openDB.mnemonic");
		JMenuItem openDBItem = new JMenuItem(label, mnemonic.charValue());
		openDBItem.setActionCommand(OPENDB_COMMAND);
		openDBItem.addActionListener(this);
		openDBItem.setIcon(img);
		openDBItem.setToolTipText(resources.getString("openDB.toolTip"));
		fileMenu.add(openDBItem);

		imageName = resources.getString("img.substract.bkg");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("openBKG");
		mnemonic = (Character) resources.getObject("openBKG.mnemonic");
		JMenuItem openBKGItem = new JMenuItem(label, mnemonic.charValue());
		openBKGItem.setActionCommand(OPENBKG_COMMAND);
		openBKGItem.addActionListener(this);
		openBKGItem.setIcon(img);
		openBKGItem.setToolTipText(resources.getString("openBKG.toolTip"));
		fileMenu.add(openBKGItem);

		fileMenu.addSeparator();

		imageName = resources.getString("img.save.database");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("saveFile");
		mnemonic = (Character) resources.getObject("saveFile.mnemonic");
		JMenuItem saveItem = new JMenuItem(label, mnemonic.charValue());
		saveItem.setActionCommand(SAVE_COMMAND);
		saveItem.addActionListener(this);
		saveItem.setIcon(img);
		saveItem.setToolTipText(resources.getString("saveFile.toolTip"));
		fileMenu.add(saveItem);

		fileMenu.addSeparator();
		
		//---------------
		imageName = resources.getString("img.pan.up");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("loadROI");
		mnemonic = (Character) resources.getObject("loadROI.mnemonic");
		JMenuItem loadROIItem = new JMenuItem(label, mnemonic.charValue());
		loadROIItem.setActionCommand(LOADROI_COMMAND);
		loadROIItem.addActionListener(this);
		loadROIItem.setIcon(img);
		loadROIItem.setToolTipText(resources.getString("loadROI.toolTip"));
		fileMenu.add(loadROIItem);

		imageName = resources.getString("img.pan.down");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("saveROI");
		mnemonic = (Character) resources.getObject("saveROI.mnemonic");
		JMenuItem saveROIItem = new JMenuItem(label, mnemonic.charValue());
		saveROIItem.setActionCommand(SAVEROI_COMMAND);
		saveROIItem.addActionListener(this);
		saveROIItem.setIcon(img);
		saveROIItem.setToolTipText(resources.getString("saveROI.toolTip"));
		fileMenu.add(saveROIItem);
		
		fileMenu.addSeparator();
		//---------------

		imageName = resources.getString("img.close");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("menu.file.exit");
		mnemonic = (Character) resources.getObject("menu.file.exit.mnemonic");
		JMenuItem exitItem = new JMenuItem(label, mnemonic.charValue());
		exitItem.setActionCommand(EXIT_COMMAND);
		exitItem.addActionListener(this);
		exitItem.setIcon(img);
		exitItem.setToolTipText(resources.getString("menu.file.exit.toolTip"));
		fileMenu.add(exitItem);

		// the help menu
		label = resources.getString("menu.help");
		mnemonic = (Character) resources.getObject("menu.help.mnemonic");
		JMenu helpMenu = new JMenu(label);
		helpMenu.setMnemonic(mnemonic.charValue());

		imageName = resources.getString("img.about");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("menu.help.about");
		mnemonic = (Character) resources.getObject("menu.help.about.mnemonic");
		JMenuItem aboutItem = new JMenuItem(label, mnemonic.charValue());
		aboutItem.setActionCommand(ABOUT_COMMAND);
		aboutItem.addActionListener(this);
		aboutItem.setIcon(img);
		aboutItem
				.setToolTipText(resources.getString("menu.help.about.toolTip"));

		imageName = resources.getString("img.about");
		img = FrameUtilities.getImageIcon(imageName, this);

		label = resources.getString("menu.help.howTo");
		mnemonic = (Character) resources.getObject("menu.help.howTo.mnemonic");
		JMenuItem howToItem = new JMenuItem(label, mnemonic.charValue());
		howToItem.setActionCommand(HOWTO_COMMAND);
		howToItem.addActionListener(this);
		howToItem.setIcon(img);
		howToItem
				.setToolTipText(resources.getString("menu.help.howTo.toolTip"));

		label = resources.getString("menu.help.LF");
		mnemonic = (Character) resources.getObject("menu.help.LF.mnemonic");
		JMenuItem lfItem = new JMenuItem(label, mnemonic.charValue());
		lfItem.setActionCommand(LOOKANDFEEL_COMMAND);
		lfItem.addActionListener(this);
		lfItem.setToolTipText(resources.getString("menu.help.LF.toolTip"));
		
		if(showLAF){
			helpMenu.add(lfItem);
			helpMenu.addSeparator();
		}

		helpMenu.add(howToItem);
		helpMenu.addSeparator();

		helpMenu.add(aboutItem);

		// view menu
		label = resources.getString("menu.view");
		mnemonic = (Character) resources.getObject("menu.view.mnemonic");
		JMenu viewMenu = new JMenu(label, true);
		viewMenu.setMnemonic(mnemonic.charValue());

		label = resources.getString("menu.view.lin");
		mnemonic = (Character) resources.getObject("menu.view.lin.mnemonic");
		linItem = new JCheckBoxMenuItem(label, true);
		linItem.setMnemonic(mnemonic);
		linItem.setActionCommand(LIN_COMMAND);
		linItem.addActionListener(this);
		linItem.setToolTipText(resources.getString("menu.view.lin.toolTip"));
		viewMenu.add(linItem);

		label = resources.getString("menu.view.ln");
		mnemonic = (Character) resources.getObject("menu.view.ln.mnemonic");
		lnItem = new JCheckBoxMenuItem(label, false);
		lnItem.setMnemonic(mnemonic);
		lnItem.setActionCommand(LN_COMMAND);
		lnItem.addActionListener(this);
		lnItem.setToolTipText(resources.getString("menu.view.ln.toolTip"));
		viewMenu.add(lnItem);

		label = resources.getString("menu.view.sqrt");
		mnemonic = (Character) resources.getObject("menu.view.sqrt.mnemonic");
		sqrtItem = new JCheckBoxMenuItem(label, false);
		sqrtItem.setMnemonic(mnemonic);
		sqrtItem.setActionCommand(SQRT_COMMAND);
		sqrtItem.addActionListener(this);
		sqrtItem.setToolTipText(resources.getString("menu.view.sqrt.toolTip"));
		viewMenu.add(sqrtItem);

		label = resources.getString("menu.show.bkg");
		mnemonic = (Character) resources.getObject("menu.show.bkg.mnemonic");
		bkgItem = new JCheckBoxMenuItem(label, true);
		bkgItem.setMnemonic(mnemonic);
		bkgItem.setActionCommand(SHOWBKG_COMMAND);
		bkgItem.addActionListener(this);
		bkgItem.setToolTipText(resources.getString("menu.show.bkg.toolTip"));
		viewMenu.add(bkgItem);

		// calibration menu
		label = resources.getString("menu.calibration");
		mnemonic = (Character) resources.getObject("menu.calibration.mnemonic");
		JMenu calibrationMenu = new JMenu(label, true);
		calibrationMenu.setMnemonic(mnemonic.charValue());

		label = resources.getString("menu.calibration.EnFWHM");
		mnemonic = (Character) resources
				.getObject("menu.calibration.EnFWHM.mnemonic");
		JMenuItem enFwhmItem = new JMenuItem(label, mnemonic.charValue());
		enFwhmItem.setActionCommand(ENFWHM_COMMAND);
		enFwhmItem.addActionListener(this);
		enFwhmItem.setToolTipText(resources
				.getString("menu.calibration.EnFWHM.toolTip"));
		calibrationMenu.add(enFwhmItem);

		label = resources.getString("menu.calibration.Eff");
		mnemonic = (Character) resources
				.getObject("menu.calibration.Eff.mnemonic");
		JMenuItem effItem = new JMenuItem(label, mnemonic.charValue());
		effItem.setActionCommand(EFF_COMMAND);
		effItem.addActionListener(this);
		effItem.setToolTipText(resources
				.getString("menu.calibration.Eff.toolTip"));
		calibrationMenu.add(effItem);

		// gammaAnalysis menu
		label = resources.getString("menu.gammaAnalysis");
		mnemonic = (Character) resources
				.getObject("menu.gammaAnalysis.mnemonic");
		JMenu gammaAnalysisMenu = new JMenu(label, true);
		gammaAnalysisMenu.setMnemonic(mnemonic.charValue());

		imageName = resources.getString("img.report");
		img = FrameUtilities.getImageIcon(imageName, this);
		label = resources.getString("roi.report");
		mnemonic = (Character) resources.getObject("roi.report.mnemonic");
		JMenuItem roiReportItem = new JMenuItem(label, mnemonic.charValue());
		roiReportItem.setActionCommand(REPORT_COMMAND);
		roiReportItem.addActionListener(this);
		roiReportItem.setIcon(img);
		roiReportItem.setToolTipText(resources.getString("roi.report.toolTip"));
		gammaAnalysisMenu.add(roiReportItem);

		label = resources.getString("menu.gammaAnalysis.sample");
		mnemonic = (Character) resources
				.getObject("menu.gammaAnalysis.sample.mnemonic");
		JMenuItem sampleItem = new JMenuItem(label, mnemonic.charValue());
		sampleItem.setActionCommand(SAMPLE_COMMAND);
		sampleItem.addActionListener(this);
		sampleItem.setIcon(img);
		sampleItem.setToolTipText(resources
				.getString("menu.gammaAnalysis.sample.toolTip"));
		gammaAnalysisMenu.add(sampleItem);

		// tools menu
		label = resources.getString("menu.tools");
		mnemonic = (Character) resources.getObject("menu.tools.mnemonic");
		JMenu toolsMenu = new JMenu(label, true);
		toolsMenu.setMnemonic(mnemonic.charValue());

		label = resources.getString("menu.tools.library");
		mnemonic = (Character) resources
				.getObject("menu.tools.library.mnemonic");
		JMenuItem libraryItem = new JMenuItem(label, mnemonic.charValue());
		libraryItem.setActionCommand(LIBRARY_COMMAND);
		libraryItem.addActionListener(this);
		libraryItem.setToolTipText(resources
				.getString("menu.tools.library.toolTip"));
		toolsMenu.add(libraryItem);

		toolsMenu.addSeparator();

		label = resources.getString("menu.tools.peakSearch");
		mnemonic = (Character) resources
				.getObject("menu.tools.peakSearch.mnemonic");
		JMenuItem peakSearchItem = new JMenuItem(label, mnemonic.charValue());
		peakSearchItem.setActionCommand(PEAKSEARCH_COMMAND);
		peakSearchItem.addActionListener(this);
		peakSearchItem.setToolTipText(resources
				.getString("menu.tools.peakSearch.toolTip"));
		toolsMenu.add(peakSearchItem);

		label = resources.getString("menu.tools.peakIdentify");
		mnemonic = (Character) resources
				.getObject("menu.tools.peakIdentify.mnemonic");
		JMenuItem peakIdentifyItem = new JMenuItem(label, mnemonic.charValue());
		peakIdentifyItem.setActionCommand(PEAKIDENTIFY_COMMAND);
		peakIdentifyItem.addActionListener(this);
		peakIdentifyItem.setToolTipText(resources
				.getString("menu.tools.peakIdentify.toolTip"));
		toolsMenu.add(peakIdentifyItem);

		// finally, glue together the menu and return it
		menuBar.add(fileMenu);
		menuBar.add(viewMenu);
		menuBar.add(calibrationMenu);
		menuBar.add(gammaAnalysisMenu);
		menuBar.add(toolsMenu);
		menuBar.add(helpMenu);

		return menuBar;
	}

	/**
	 * Setting up the status bar.
	 * 
	 * @param toolBar the toolbar
	 */
	private void initStatusBar(JToolBar toolBar) {
		JPanel toolP = new JPanel();
		toolP.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));

		toolP.add(statusL);
		toolBar.add(toolP);
		statusL.setText(resources.getString("status.wait"));
	}

	/**
	 * Most actions are defined here.
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		command = arg0.getActionCommand();
		if (command.equals(ABOUT_COMMAND)) {
			about();
		} else if (command.equals(EXIT_COMMAND)) {
			attemptExit();
		} else if (command.equals(HOWTO_COMMAND)) {
			howTo();
		} else if (command.equals(LOOKANDFEEL_COMMAND)) {
			lookAndFeel();
		} else if (command.equals(OPENFILE_COMMAND)) {
			openFile();
		} else if (command.equals(ZOOMIN_COMMAND)) {
			zoomIn();
		} else if (command.equals(ZOOMOUT_COMMAND)) {
			zoomOut();
		} else if (command.equals(PANLEFT_COMMAND)) {
			panLeft();
		} else if (command.equals(PANRIGHT_COMMAND)) {
			panRight();
		} else if (command.equals(PANUP_COMMAND)) {
			panUp();
		} else if (command.equals(PANDOWN_COMMAND)) {
			panDown();
		} else if (command.equals(REFRESH_COMMAND)) {
			refresh();
		} else if (command.equals(MOVEMARKERLEFT_COMMAND)) {
			movemarkerleft();
		} else if (command.equals(MOVEMARKERRIGHT_COMMAND)) {
			movemarkerright();
		} else if (command.equals(MOVEMARKERLEFT2_COMMAND)) {
			movemarkerleft2();
		} else if (command.equals(MOVEMARKERRIGHT2_COMMAND)) {
			movemarkerright2();
		} else if (command.equals(INSERTROI_COMMAND)) {
			insertRoi();
		} else if (command.equals(DELETEROI_COMMAND)) {
			deleteRoi();
		} else if (command.equals(TODAY_COMMAND)) {
			today();
		} else if (command.equals(SAVE_COMMAND)) {
			saveInDB();
		} else if (command.equals(OPENDB_COMMAND)) {
			openDB();
		} else if (command.equals(OPENBKG_COMMAND)) {
			openBKG();
		} else if (command.equals(VIEWROI_COMMAND)) {
			viewROI();
		} else if (command.equals(DELETEALLROI_COMMAND)) {
			deleteAllROI();
		} else if (command.equals(SETROI_COMMAND)) {
			setROI();
		} else if (command.equals(REPORT_COMMAND)) {
			report();
		} else if (command.equals(LIN_COMMAND)) {
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(LN_COMMAND)) {
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(SQRT_COMMAND)) {
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(KEV_COMMAND)) {
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(CHANNEL_COMMAND)) {
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(UPDATEROIEDGE_COMMAND)) {
			updateRoiEdge();
		} else if (command.equals(ENFWHM_COMMAND)) {
			enFWHMCalibration();
		} else if (command.equals(EFF_COMMAND)) {
			effCalibration();
		} else if (command.equals(SAMPLE_COMMAND)) {
			sampleCalculation();
		} else if (command.equals(PEAKSEARCH_COMMAND)) {
			peakSearch();
		} else if (command.equals(PEAKIDENTIFY_COMMAND)) {
			peakIdentify();
		} else if (command.equals(LIBRARY_COMMAND)) {
			statusRunS = resources.getString("status.db.init");
			startThread();
		} else if (command.equals(SHOWBKG_COMMAND)) {
			showBKG();
		} else if (command.equals(LOADROI_COMMAND)) {
			loadROI();
		} else if (command.equals(SAVEROI_COMMAND)) {
			saveROI();
		}

	}

	/**
	 * JCombobox specific actions are defined here.
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == roiCb) {
			selectRoi();
		}
	}

	/**
	 * Changing the look and feel can be done here. Also display some gadgets.
	 */
	private void lookAndFeel() {
		setVisible(false);// setEnabled(false);
		new ScanDiskLFGui(this);
	}

	/**
	 * Display some useful tips and hints.
	 */
	private void howTo() {
		new HowToFrame(this);
	}

	/**
	 * Show the background spectrum on chart.
	 */
	private void showBKG() {
		if (emptyChartB) {
			bkgItem.setState(true);
			return;
		}
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				//XYPlot plot = (XYPlot) c.getPlot();
				XYPlot plot = (XYPlot) chart.getPlot();
				XYItemRenderer renderer = (XYItemRenderer) plot.getRenderer(1);
				// 1=database index for BKG!
				renderer.setSeriesVisible(0, bkgItem.isSelected());
				//-----------------------
				plot = (XYPlot) charten.getPlot();
				renderer = (XYItemRenderer) plot.getRenderer(1);
				// 1=database index for BKG!
				renderer.setSeriesVisible(0, bkgItem.isSelected());
				
				plot = (XYPlot) chartln.getPlot();
				renderer = (XYItemRenderer) plot.getRenderer(1);
				// 1=database index for BKG!
				renderer.setSeriesVisible(0, bkgItem.isSelected());
				
				plot = (XYPlot) chartlnen.getPlot();
				renderer = (XYItemRenderer) plot.getRenderer(1);
				// 1=database index for BKG!
				renderer.setSeriesVisible(0, bkgItem.isSelected());
				
				plot = (XYPlot) chartsqrt.getPlot();
				renderer = (XYItemRenderer) plot.getRenderer(1);
				// 1=database index for BKG!
				renderer.setSeriesVisible(0, bkgItem.isSelected());
				
				plot = (XYPlot) chartsqrten.getPlot();
				renderer = (XYItemRenderer) plot.getRenderer(1);
				// 1=database index for BKG!
				renderer.setSeriesVisible(0, bkgItem.isSelected());
			}
		}
	}

	/**
	 * Go to nuclide library
	 */
	private void library() {
		new GammaLibraryFrame(this);
	}

	/**
	 * Updates ROI edge. If peaks are convoluted, you may want to fit the ROI (around the desired peak) with a Gaussian since the ROI cannot be 
	 * properly set, usually only a narrow region around the peak. Therefore, the ROI must have proper edges as if no other peaks are around (as if the 
	 * peak was a singleton). Select edges far enough to have valid Compton continuum background. 
	 */
	private void updateRoiEdge() {
		if (emptyChartB)
			return;

		if (roiV.size() == 0) {
			return;
		}

		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				double d1 = -1.0;
				double d2 = 0.0;
				Iterator<?> i = null;

				Collection<?> col = plot.getDomainMarkers(Layer.BACKGROUND);
				if (col != null) {
					i = col.iterator();
					int colSize = col.size();
					if (colSize < 2)
						return;// two markers only!
				} else
					return;

				if (i != null)
					while (i.hasNext()) {
						ValueMarker vm = (ValueMarker) i.next();
						if (d1 != -1.0)// was used
							d2 = vm.getValue();
						else
							d1 = vm.getValue();
					}
				if (d1 < 0)
					d1 = 0.0;// channel or energy are positive!
				if (d2 < 0)
					d2 = 0.0;// channel or energy are positive!
				if (d1 == d2)
					return;// no roi!!!!
				double tmp = 0.0;
				if (d1 > d2) {// swap...ascend order!
					tmp = d1;
					d1 = d2;
					d2 = tmp;
				}

				double dd1 = d1;
				double dd2 = d2;
				if (!isChannelDisplay) {
					dd1 = getChannelFromKeV(d1);
					dd2 = getChannelFromKeV(d2);
				} else {
					dd1 = adjustChannel(d1);
					dd2 = adjustChannel(d2);
				}
				if (dd1 == dd2)
					return;// no edge!!!!

				String idRoiS = (String) roiCb.getSelectedItem();
				int idRoi = Convertor.stringToInt(idRoiS) - 1;
				GammaRoi gr = roiV.elementAt(idRoi);

				double sta = gr.getStartChannel();
				double end = gr.getEndChannel();

				if ((dd1 <= sta) && (dd2 >= end)) {
					gr.setStartEdgeChannel(dd1);
					gr.setEndEdgeChannel(dd2);

					int i1 = (new Double(dd1)).intValue();
					int i2 = (new Double(dd2)).intValue();
					gr.setStartEdgePulses(getAverageContinuumEdges(i1));//pulsesD[i1]);
					gr.setEndEdgePulses(getAverageContinuumEdges(i2));//pulsesD[i2]);

					if (naiRb.isSelected()) {
						iNet = NET_CALCULATION_NAI;
					} else if (geRb.isSelected()) {
						iNet = NET_CALCULATION_GE;
					} else if (gaussRb.isSelected()) {
						iNet = NET_CALCULATION_GAUSSIAN;
					}
					gr.setNetCalculationMethod(iNet);
					gr.setConfidenceLevel(roiConfidenceLevel);
					gr.performBasicComputation();

					// gr.printCalib();
				}

				clearMarkersAndSetRoiCenter(gr);
				showRoiEdge(gr);
			}
		}
	}
	
	/**
	 * Internally used. It perform an average over counts in continuum BKG edges. The reason
	 * for this operation is the relatively large deviation in computation (mainly for 
	 * continuum Compton bkg) when we select Rois slightly different (few channels to the 
	 * left or to the right). 
	 * @param ich the central channel for the ROI edge
	 * @return the average counts (gross) on the edge.
	 */
	private double getAverageContinuumEdges(int ich){
		//now, perform average:
		double result=0.0;//reset
		int ioff = (iROI_EDGE_PULSES_AVERAGE_BY-1)/2;//default (3-1)/2=1=>
		//meaning we go up and down the center by ioff.
		//e.g if 3=> i-1,i,i+1; if 7=>(7-1)/2 =3 so i-3,i-2,i-1,i,i+1,i+2,i+3.
		int divideBy=0;//to not go out of scale;
		for (int i = ich-ioff; i<=ich+ioff;i++){
			if (i>=MINCHANNEL && i<=MAXCHANNEL){//to be sure we are within.
				result = result + pulsesD[i];
				divideBy = divideBy +1;				
			}
		}
		//System.out.println("divide By= "+divideBy);
		//result = result/iROI_EDGE_PULSES_AVERAGE_BY;
		result = result/divideBy;
		return result;
	}

	/**
	 * Select ROI to be viewed on the chart.
	 */
	private void selectRoi() {
		if (emptyChartB)
			return;

		if (roiV.size() == 0) {
			return;
		}

		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {

				String idRoiS = (String) roiCb.getSelectedItem();
				int idRoi = Convertor.stringToInt(idRoiS) - 1;
				GammaRoi gr = roiV.elementAt(idRoi);

				clearMarkersAndSetRoiCenter(gr);
				showRoiEdge(gr);

				roiNamelabel.setText(resources.getString("roi.name.label")
						+ gr.getNuclide());
			}
		}

	}

	/**
	 * Shows channels on x-axis.
	 */
	private void toChannel() {
		if (emptyChartB) {
			statusL.setText(resources.getString("status.done"));
			stopThread();
			return;
		}
		if (this.cp != null) {
			//this.cp.setChart(chart);//default chart
			
			//MINBOUND = MINCHANNEL;
			//MAXBOUND = MAXCHANNEL;
			
			//isChannelDisplay = true;
			//selectRoi();
			//statusL.setText(resources.getString("status.done"));
			//stopThread();
			//========================
			
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				if (isChannelDisplay) {
					stopThread();
					statusL.setText(resources.getString("status.done"));
					return;
				}
				//==================
				chart.setTitle(spectrumName);chartln.setTitle(spectrumName);
				chartsqrt.setTitle(spectrumName);
				if (isLinB)
					this.cp.setChart(chart);//default chart
				else if (isLnB)
					this.cp.setChart(chartln);
				else if (isSqrtB)
					this.cp.setChart(chartsqrt);
				//===================
				/*XYPlot plot = (XYPlot) c.getPlot();

				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);
				XYSeriesCollection bkgdata = (XYSeriesCollection) plot
						.getDataset(1);
				XYSeries bkgseries = bkgdata.getSeries(0);

				int n = series.getItemCount();
				series.clear();
				bkgseries.clear();

				if (roiV.size() > 0) {
					for (int j = 0; j < roiV.size(); j++) {
						XYSeriesCollection dataj = (XYSeriesCollection) plot
								.getDataset(2 + j);
						XYSeries seriesj = dataj.getSeries(0);
						seriesj.clear();
					}
				}

				for (int i = 0; i < n; i++) {
					series.add(channelI[i], pulsesD[i]);
					bkgseries.add(channelI[i], bkgpulsesD[i]);

					if (roiV.size() > 0) {
						for (int j = 0; j < roiV.size(); j++) {
							XYSeriesCollection dataj = (XYSeriesCollection) plot
									.getDataset(2 + j);
							XYSeries seriesj = dataj.getSeries(0);
							GammaRoi gr = roiV.elementAt(j);
							double start = gr.getStartChannel();
							double end = gr.getEndChannel();
							if ((start <= channelI[i]) && (channelI[i] <= end))
								seriesj.add(channelI[i], pulsesD[i]);
						}
					}

				}

				((NumberAxis) plot.getDomainAxis()).setLabel(resources
						.getString("chart.x.Channel"));
				*/
				MINBOUND = MINCHANNEL;
				MAXBOUND = MAXCHANNEL;

				isChannelDisplay = true;

				selectRoi();
				/*
				isLinB = true;
				isLnB = false;
				isSqrtB = false;

				lnItem.setState(isLnB);
				sqrtItem.setState(isSqrtB);
				linItem.setState(isLinB);
				*/
				statusL.setText(resources.getString("status.done"));
				stopThread();

			} else {
				statusL.setText(resources.getString("status.done"));
				stopThread();

			}
		} else {
			statusL.setText(resources.getString("status.done"));
			stopThread();

		}

	}

	/**
	 * Show keV on x-axis.
	 */
	private void toKev() {
		if (emptyChartB) {
			channelRb.setSelected(isChannelDisplay);
			statusL.setText(resources.getString("status.done"));
			stopThread();
			return;
		}
		if (this.cp != null) {
			//=============
			//this.cp.setChart(charten);
			//String sname = spectrumName;
			//charten.setTitle(sname);
			
			//MINBOUND = getKevFromChannel(MINCHANNEL);
			//MAXBOUND = getKevFromChannel(MAXCHANNEL);
			
			////isChannelDisplay = false;
			//selectRoi();
			//statusL.setText(resources.getString("status.done"));
			//stopThread();
			
			//=================
			
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				if (!isChannelDisplay) {
					stopThread();
					statusL.setText(resources.getString("status.done"));
					return;
				}
				//=====================================
				if (isLinB){
					this.cp.setChart(charten);
					String sname = spectrumName;
					charten.setTitle(sname);
				} else if (isLnB){
					this.cp.setChart(chartlnen);
					String sname = spectrumName;
					chartlnen.setTitle(sname);
				} else if (isSqrtB){
					this.cp.setChart(chartsqrten);
					String sname = spectrumName;
					chartsqrten.setTitle(sname);
				}
				//=====================================
				/*
				XYPlot plot = (XYPlot) c.getPlot();

				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);
				XYSeriesCollection bkgdata = (XYSeriesCollection) plot
						.getDataset(1);
				XYSeries bkgseries = bkgdata.getSeries(0);

				int n = series.getItemCount();
				series.clear();
				bkgseries.clear();

				if (roiV.size() > 0) {
					for (int j = 0; j < roiV.size(); j++) {
						XYSeriesCollection dataj = (XYSeriesCollection) plot
								.getDataset(2 + j);
						XYSeries seriesj = dataj.getSeries(0);
						seriesj.clear();
					}
				}

				for (int i = 0; i < n; i++) {
					keVD[i] = getKevFromChannel(channelI[i]);

					series.add(keVD[i], pulsesD[i]);
					bkgseries.add(keVD[i], bkgpulsesD[i]);

					if (roiV.size() > 0) {
						for (int j = 0; j < roiV.size(); j++) {
							XYSeriesCollection dataj = (XYSeriesCollection) plot
									.getDataset(2 + j);
							XYSeries seriesj = dataj.getSeries(0);
							GammaRoi gr = roiV.elementAt(j);
							double start = gr.getStartChannel();
							double end = gr.getEndChannel();
							if ((start <= channelI[i]) && (channelI[i] <= end))
								seriesj.add(keVD[i], pulsesD[i]);
						}
					}

				}

				((NumberAxis) plot.getDomainAxis()).setLabel(resources
						.getString("chart.x.keV"));
				*/
				MINBOUND = getKevFromChannel(MINCHANNEL);
				MAXBOUND = getKevFromChannel(MAXCHANNEL);

				isChannelDisplay = false;

				selectRoi();
				// always perform this operation for lin view...it is easier!
				// alternative: use buffers before series.clear() which results
				// in increasing computing time=>we want functionality and ease
				// of use!
				/*
				isLinB = true;
				isLnB = false;
				isSqrtB = false;

				lnItem.setState(isLnB);
				sqrtItem.setState(isSqrtB);
				linItem.setState(isLinB);
				*/

				statusL.setText(resources.getString("status.done"));
				stopThread();
			} else {
				statusL.setText(resources.getString("status.done"));
				stopThread();
			}
		} else {
			statusL.setText(resources.getString("status.done"));
			stopThread();
		}
	}

	/**
	 * Display linear function of pulses, that is y = pulses
	 */
	private void linDisplay() {
		if (emptyChartB) {
			statusL.setText(resources.getString("status.done"));
			stopThread();

			isLinB = true;
			isLnB = false;
			isSqrtB = false;

			lnItem.setState(isLnB);
			sqrtItem.setState(isSqrtB);
			linItem.setState(isLinB);

			return;
		}
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				if (isLinB)
					return;
				
				//=====================================
				//this.cp.setChart(chart);
				String sname = spectrumName;
				chart.setTitle(sname);charten.setTitle(sname);
				if (isChannelDisplay)
					this.cp.setChart(chart);
				else
					this.cp.setChart(charten);
				//=====================================
				
				/*XYPlot plot = (XYPlot) c.getPlot();

				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);

				XYSeriesCollection bkgdata = (XYSeriesCollection) plot
						.getDataset(1);
				XYSeries bkgseries = bkgdata.getSeries(0);

				int n = series.getItemCount();
				double yy = 0.0;

				for (int i = 0; i < n; i++) {

					yy = pulsesD[i];

					if (isChannelDisplay)
						series.update((Number) channelI[i], (Number) yy);
					else
						series.update((Number) keVD[i], (Number) yy);

					yy = bkgpulsesD[i];

					if (isChannelDisplay)
						bkgseries.update((Number) channelI[i], (Number) yy);
					else
						bkgseries.update((Number) keVD[i], (Number) yy);

					if (roiV.size() > 0) {
						for (int j = 0; j < roiV.size(); j++) {
							XYSeriesCollection dataj = (XYSeriesCollection) plot
									.getDataset(2 + j);
							XYSeries seriesj = dataj.getSeries(0);
							GammaRoi gr = roiV.elementAt(j);
							double start = gr.getStartChannel();
							double end = gr.getEndChannel();
							if ((start <= channelI[i]) && (channelI[i] <= end)) {
								yy = pulsesD[i];
								if (isChannelDisplay)
									seriesj.update((Number) channelI[i],
											(Number) yy);
								else
									seriesj.update((Number) keVD[i],
											(Number) yy);
							}
						}
					}

				}*/
				isLinB = true;
				isLnB = false;
				isSqrtB = false;

				lnItem.setState(isLnB);
				sqrtItem.setState(isSqrtB);
				linItem.setState(isLinB);
				
				selectRoi();

				statusL.setText(resources.getString("status.done"));
				stopThread();

			} else {
				statusL.setText(resources.getString("status.done"));
				stopThread();

			}
		} else {
			statusL.setText(resources.getString("status.done"));
			stopThread();

		}
	}

	/**
	 * Display logarithmic function of pulses, that is y = Ln(pulses)
	 */
	private void lnDisplay() {
		if (emptyChartB) {
			statusL.setText(resources.getString("status.done"));
			stopThread();

			isLinB = true;
			isLnB = false;
			isSqrtB = false;

			lnItem.setState(isLnB);
			sqrtItem.setState(isSqrtB);
			linItem.setState(isLinB);

			return;
		}
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				if (isLnB)
					return;
				
				String sname = spectrumName;
				chartln.setTitle(sname);chartlnen.setTitle(sname);
				if (isChannelDisplay)
					this.cp.setChart(chartln);
				else
					this.cp.setChart(chartlnen);
				/*
				XYPlot plot = (XYPlot) c.getPlot();

				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);

				XYSeriesCollection bkgdata = (XYSeriesCollection) plot
						.getDataset(1);
				XYSeries bkgseries = bkgdata.getSeries(0);

				int n = series.getItemCount();
				double yy = 0.0;

				for (int i = 0; i < n; i++) {
					if (pulsesD[i] != 0.0)
						yy = Math.log(pulsesD[i]);
					else
						yy = 0;
					if (isChannelDisplay)
						series.update((Number) channelI[i], (Number) yy);
					else
						series.update((Number) keVD[i], (Number) yy);

					if (bkgpulsesD[i] != 0.0)
						yy = Math.log(bkgpulsesD[i]);
					else
						yy = 0;
					if (isChannelDisplay)
						bkgseries.update((Number) channelI[i], (Number) yy);
					else
						bkgseries.update((Number) keVD[i], (Number) yy);

					if (roiV.size() > 0) {
						for (int j = 0; j < roiV.size(); j++) {
							XYSeriesCollection dataj = (XYSeriesCollection) plot
									.getDataset(2 + j);
							XYSeries seriesj = dataj.getSeries(0);
							GammaRoi gr = roiV.elementAt(j);
							double start = gr.getStartChannel();
							double end = gr.getEndChannel();
							if ((start <= channelI[i]) && (channelI[i] <= end)) {
								if (pulsesD[i] != 0.0)
									yy = Math.log(pulsesD[i]);
								else
									yy = 0;
								if (isChannelDisplay)
									seriesj.update((Number) channelI[i],
											(Number) yy);
								else
									seriesj.update((Number) keVD[i],
											(Number) yy);
							}
						}
					}
				}*/
				isLnB = true;
				isSqrtB = false;
				isLinB = false;

				lnItem.setState(isLnB);
				sqrtItem.setState(isSqrtB);
				linItem.setState(isLinB);
				
				selectRoi();

				statusL.setText(resources.getString("status.done"));
				stopThread();

			} else {
				statusL.setText(resources.getString("status.done"));
				stopThread();
			}
		} else {
			statusL.setText(resources.getString("status.done"));
			stopThread();
		}

	}

	/**
	 * Display square root function of pulses, that is y = Sqrt(pulses)
	 */
	private void sqrtDisplay() {
		if (emptyChartB) {
			statusL.setText(resources.getString("status.done"));
			stopThread();

			isLinB = true;
			isLnB = false;
			isSqrtB = false;

			lnItem.setState(isLnB);
			sqrtItem.setState(isSqrtB);
			linItem.setState(isLinB);

			return;
		}
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				if (isSqrtB)
					return;
				
				String sname = spectrumName;
				chartsqrt.setTitle(sname);chartsqrten.setTitle(sname);
				if (isChannelDisplay)
					this.cp.setChart(chartsqrt);
				else
					this.cp.setChart(chartsqrten);
				/*
				XYPlot plot = (XYPlot) c.getPlot();

				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);

				XYSeriesCollection bkgdata = (XYSeriesCollection) plot
						.getDataset(1);
				XYSeries bkgseries = bkgdata.getSeries(0);

				int n = series.getItemCount();
				double yy = 0.0;

				for (int i = 0; i < n; i++) {
					if (pulsesD[i] != 0.0)
						yy = Math.sqrt(pulsesD[i]);
					else
						yy = 0;
					if (isChannelDisplay)
						series.update((Number) channelI[i], (Number) yy);
					else
						series.update((Number) keVD[i], (Number) yy);

					if (bkgpulsesD[i] != 0.0)
						yy = Math.sqrt(bkgpulsesD[i]);
					else
						yy = 0;
					if (isChannelDisplay)
						bkgseries.update((Number) channelI[i], (Number) yy);
					else
						bkgseries.update((Number) keVD[i], (Number) yy);

					if (roiV.size() > 0) {
						for (int j = 0; j < roiV.size(); j++) {
							XYSeriesCollection dataj = (XYSeriesCollection) plot
									.getDataset(2 + j);
							XYSeries seriesj = dataj.getSeries(0);
							GammaRoi gr = roiV.elementAt(j);
							double start = gr.getStartChannel();
							double end = gr.getEndChannel();
							if ((start <= channelI[i]) && (channelI[i] <= end)) {
								if (pulsesD[i] != 0.0)
									yy = Math.sqrt(pulsesD[i]);
								else
									yy = 0;
								if (isChannelDisplay)
									seriesj.update((Number) channelI[i],
											(Number) yy);
								else
									seriesj.update((Number) keVD[i],
											(Number) yy);
							}
						}
					}
				}*/
				isSqrtB = true;
				isLnB = false;
				isLinB = false;

				lnItem.setState(isLnB);
				sqrtItem.setState(isSqrtB);
				linItem.setState(isLinB);
				
				selectRoi();

				statusL.setText(resources.getString("status.done"));
				stopThread();

			} else {
				statusL.setText(resources.getString("status.done"));
				stopThread();
			}
		} else {
			statusL.setText(resources.getString("status.done"));
			stopThread();
		}

	}

	/**
	 * Delete all ROIs
	 */
	private void deleteAllROI() {
		if (emptyChartB)
			return;

		if (roiV.size() == 0) {
			return;
		}

		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {//we have to remove from all charts!!!!
				XYPlot plot = (XYPlot) chart.getPlot();//(XYPlot) c.getPlot();
				XYPlot ploten = (XYPlot) charten.getPlot();//@@@@@@@@
				
				XYPlot plotln = (XYPlot) chartln.getPlot();//(XYPlot) c.getPlot();
				XYPlot plotlnen = (XYPlot) chartlnen.getPlot();//@@@@@@@@
				
				XYPlot plotsqrt = (XYPlot) chartsqrt.getPlot();//(XYPlot) c.getPlot();
				XYPlot plotsqrten = (XYPlot) chartsqrten.getPlot();//@@@@@@@@
				if (idataset > 2) {
					for (int i = 2; i <= 2 + roiV.size() - 1; i++) {
						plot.setDataset(i, null);
						plot.setRenderer(i, null);
						
						ploten.setDataset(i, null);
						ploten.setRenderer(i, null);
						//-----------------
						plotln.setDataset(i, null);
						plotln.setRenderer(i, null);
						
						plotlnen.setDataset(i, null);
						plotlnen.setRenderer(i, null);
						
						plotsqrt.setDataset(i, null);
						plotsqrt.setRenderer(i, null);
						
						plotsqrten.setDataset(i, null);
						plotsqrten.setRenderer(i, null);
					}
					idataset = 2;
					roiV.removeAllElements();

					roiCb.removeItemListener(this);
					roiCb.removeAllItems();
					roiCb.addItemListener(this);

					plot.clearDomainMarkers();
					plot.clearAnnotations();
					
					ploten.clearDomainMarkers();
					ploten.clearAnnotations();
					//-------------------------------
					plotln.clearDomainMarkers();
					plotln.clearAnnotations();
					
					plotlnen.clearDomainMarkers();
					plotlnen.clearAnnotations();
					
					plotsqrt.clearDomainMarkers();
					plotsqrt.clearAnnotations();
					
					plotsqrten.clearDomainMarkers();
					plotsqrten.clearAnnotations();
					
					imarker = 0;// reset
					markers[1] = null;// reset
					markers[0] = null;// reset

					roilabel.setText(resources.getString("roi.count")
							+ Convertor.intToString(roiV.size()));

					roiNamelabel.setText(resources.getString("roi.name.label")
							+ "NoName");

					spectrumLiveTime = 0.0;// reset
				}
			}
		}
	}

	/**
	 * Set measurement date as today
	 */
	private void today() {

		String s = null;
		//TimeUtilities.today();
		TimeUtilities todayTu = new TimeUtilities();
		s = Convertor.intToString(todayTu.getDay());//TimeUtilities.iday);
		if (todayTu.getDay() < 10)//TimeUtilities.iday < 10)
			s = "0" + s;
		dayCb.setSelectedItem((Object) s);
		s = Convertor.intToString(todayTu.getMonth());//TimeUtilities.imonth);
		if (todayTu.getMonth() < 10)//TimeUtilities.imonth < 10)
			s = "0" + s;
		monthCb.setSelectedItem((Object) s);
		s = Convertor.intToString(todayTu.getYear());//TimeUtilities.iyear);
		yearTf.setText(s);
	}

	/**
	 * Initiate computation threads
	 */
	private void startThread() {
		stopAnim=false;
		if (appTh == null) {
			appTh = new Thread(this);
			appTh.start();// Allow one simulation at time!
			//setEnabled(false);
		}

		if (statusTh == null) {
			statusTh = new Thread(this);
			statusTh.start();
		}

	}
	private boolean stopAnim=true;
	/**
	 * Thread specific run method
	 */
	public void run() {
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);

		long startTime = System.currentTimeMillis();
		Thread currentThread = Thread.currentThread();

		while (!stopAnim && currentThread == statusTh) {
			frameNumber++;
			if (frameNumber % 2 == 0)
				statusL.setText(statusRunS + ".....");
			else
				statusL.setText(statusRunS);

			// Delay
			try {
				startTime += delay;
				Thread.sleep(Math.max(0, startTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				break;
			}
		}

		if (currentThread == appTh) {
			if (command.equals(OPENFILE_COMMAND)) {
				updateSpectrum();
			} else if (command.equals(CHANNEL_COMMAND)) {
				toChannel();
			} else if (command.equals(KEV_COMMAND)) {
				toKev();
			} else if (command.equals(LIN_COMMAND)) {
				linDisplay();
			} else if (command.equals(LN_COMMAND)) {
				lnDisplay();
			} else if (command.equals(SQRT_COMMAND)) {
				sqrtDisplay();
			} else if (command.equals(LIBRARY_COMMAND)) {
				library();
			}
		}
	}

	/**
	 * Update background spectrum from database.
	 */
	protected void updateBkgFromDatabase() {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {//now for all series:
				/*XYPlot plot = (XYPlot) c.getPlot();
				//XYPlot plot = (XYPlot)chart.getPlot();//(XYPlot) c.getPlot();//
				//XYPlot ploten = (XYPlot)charten.getPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@
				
				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);

				XYSeriesCollection bkgdata = (XYSeriesCollection) plot
						.getDataset(1);
				XYSeries bkgseries = bkgdata.getSeries(0);

				int n = series.getItemCount();//common
				double yy = 0.0;

				for (int i = 0; i < n; i++) {
					if (isLnB) {
						if (bkgpulsesD[i] != 0.0)
							yy = Math.log(bkgpulsesD[i]);
						else
							yy = 0;
					} else if (isSqrtB) {
						if (bkgpulsesD[i] != 0.0)
							yy = Math.sqrt(bkgpulsesD[i]);
						else
							yy = 0;
					} else if (isLinB) {
						yy = bkgpulsesD[i];
					}

					if (isChannelDisplay)
						bkgseries.update((Number) channelI[i], (Number) yy);
					else
						bkgseries.update((Number) keVD[i], (Number) yy);

				}// for
				*/
				
				//XYPlot plot = (XYPlot) c.getPlot();
				XYPlot plot = (XYPlot)chart.getPlot();//(XYPlot) c.getPlot();//
				XYPlot ploten = (XYPlot)charten.getPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@
				
				XYPlot plotln = (XYPlot)chartln.getPlot();
				XYPlot plotlnen = (XYPlot)chartlnen.getPlot();
				XYPlot plotsqrt = (XYPlot)chartsqrt.getPlot();
				XYPlot plotsqrten = (XYPlot)chartsqrten.getPlot();
				
				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);

				XYSeriesCollection bkgdata = (XYSeriesCollection) plot
						.getDataset(1);
				XYSeries bkgseries = bkgdata.getSeries(0);
				
				//-----------
				XYSeriesCollection bkgdataen = (XYSeriesCollection) ploten
						.getDataset(1);
				XYSeries bkgseriesen = bkgdataen.getSeries(0);
				
				XYSeriesCollection bkgdataln = (XYSeriesCollection) plotln
						.getDataset(1);
				XYSeries bkgseriesln = bkgdataln.getSeries(0);
				XYSeriesCollection bkgdatalnen = (XYSeriesCollection) plotlnen
						.getDataset(1);
				XYSeries bkgserieslnen = bkgdatalnen.getSeries(0);
				
				XYSeriesCollection bkgdatasqrt = (XYSeriesCollection) plotsqrt
						.getDataset(1);
				XYSeries bkgseriessqrt = bkgdatasqrt.getSeries(0);
				XYSeriesCollection bkgdatasqrten = (XYSeriesCollection) plotsqrten
						.getDataset(1);
				XYSeries bkgseriessqrten = bkgdatasqrten.getSeries(0);
				//-------------

				int n = series.getItemCount();//common
				double yy = 0.0;

				for (int i = 0; i < n; i++) {
					
					//------------
					double lnd =0.0; double sqrtd = 0.0;
					if(bkgpulsesD[i]!=0.0){
						lnd = Math.log(bkgpulsesD[i]);
						sqrtd = Math.sqrt(bkgpulsesD[i]);
					} else{
						lnd = 0.0;
						sqrtd = 0.0;
					}
					
					double xx = channelI[i];
					double xxen = keVD[i];
					yy = bkgpulsesD[i];
					
					bkgseries.update((Number) xx, (Number) yy);
					bkgseriesen.update((Number) xxen, (Number) yy);
					
					bkgseriesln.update((Number) xx, (Number) lnd);
					bkgserieslnen.update((Number) xxen, (Number) lnd);
					
					bkgseriessqrt.update((Number) xx, (Number) sqrtd);
					bkgseriessqrten.update((Number) xxen, (Number) sqrtd);

				}// for
				
			}// c
		}// cp
	}

	/**
	 * Update ROI from database. 
	 * @param gr the ROI object
	 */
	protected void updateRoiFromDatabase(GammaRoi gr){//double dd1, double dd2) {
		JFreeChart c = this.cp.getChart();
		if (c != null) {//this is redundant if here we always have channels vs pulses!!!
			
			double dd1=gr.getStartChannel();
			double dd2=gr.getEndChannel();
			
			XYPlot plot = (XYPlot)chart.getPlot();//(XYPlot) c.getPlot();//
			XYPlot ploten = (XYPlot)charten.getPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@
			
			XYPlot plotln = (XYPlot)chartln.getPlot();//(XYPlot) c.getPlot();//
			XYPlot plotlnen = (XYPlot)chartlnen.getPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@
			
			XYPlot plotsqrt = (XYPlot)chartsqrt.getPlot();//(XYPlot) c.getPlot();//
			XYPlot plotsqrten = (XYPlot)chartsqrten.getPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@
			// displaying the ROI
			XYSeries series2 = new XYSeries("roi");// not shown
			XYSeries series2en = new XYSeries("roi");// @@@@@@@@@@@@@@@@@@@@@@@@@@@
			
			XYSeries series2ln = new XYSeries("roi");
			XYSeries series2lnen = new XYSeries("roi");
			XYSeries series2sqrt = new XYSeries("roi");
			XYSeries series2sqrten = new XYSeries("roi");

			XYSeriesCollection data = (XYSeriesCollection) plot.getDataset(0);			
			XYSeries series = data.getSeries(0);
			
			int n = series.getItemCount();//common!!!!!!!!!!!!!
			// setting roi based on primary data!
			for (int j = 0; j < n; j++) {
				Number x = series.getX(j);
				Number y = series.getY(j);
				double lnd = 0.0;double sqrtd = 0.0;
				if (y.doubleValue()!=0){
					lnd = Math.log(y.doubleValue());
					sqrtd=Math.sqrt(y.doubleValue());
				}else{
					lnd = 0.0;
					sqrtd=0.0;
				}
				
				if ((dd1 <= x.doubleValue()) && (x.doubleValue() <= dd2)) {
					series2.add(x, y);					
					series2en.add(getKevFromChannel(x.doubleValue()), y);//@@@@@@@@@@@@@@
					
					series2ln.add(x, lnd);
					series2lnen.add(getKevFromChannel(x.doubleValue()), lnd);
					
					series2sqrt.add(x, sqrtd);
					series2sqrten.add(getKevFromChannel(x.doubleValue()), sqrtd);
				}
			}
			
			XYSeriesCollection data2 = new XYSeriesCollection(series2);
			plot.setDataset(idataset, data2);
			
			XYSeriesCollection data2en = new XYSeriesCollection(series2en);//@@@@@@@@@
			ploten.setDataset(idataset, data2en);//@@@@@@@@@@
			
			XYSeriesCollection data2ln = new XYSeriesCollection(series2ln);
			plotln.setDataset(idataset, data2ln);
			XYSeriesCollection data2lnen = new XYSeriesCollection(series2lnen);
			plotlnen.setDataset(idataset, data2lnen);
			
			XYSeriesCollection data2sqrt = new XYSeriesCollection(series2sqrt);
			plotsqrt.setDataset(idataset, data2sqrt);
			XYSeriesCollection data2sqrten = new XYSeriesCollection(series2sqrten);
			plotsqrten.setDataset(idataset, data2sqrten);
			
			XYItemRenderer renderer2 = new XYAreaRenderer();
			
			if (!gr.getNuclide().equals("NoName")){
				renderer2.setSeriesPaint(0, Color.green);
			} else			
				renderer2.setSeriesPaint(0, Color.red);
			renderer2.setSeriesVisibleInLegend(0, false);
			plot.setRenderer(idataset, renderer2);
			
			ploten.setRenderer(idataset, renderer2);//@@@@@@@@@@
			
			plotln.setRenderer(idataset, renderer2);//@@@@@@@@@@
			plotlnen.setRenderer(idataset, renderer2);//@@@@@@@@@@
			plotsqrt.setRenderer(idataset, renderer2);//@@@@@@@@@@
			plotsqrten.setRenderer(idataset, renderer2);//@@@@@@@@@@

			idataset++;
			// finally set a center marker and perform some cleaning
			// already set by combobox item state change=>select roi!!!

			roilabel.setText(resources.getString("roi.count")
					+ Convertor.intToString(roiV.size()));
		}
	}
	
	/**
	 * Update information about ROI. This is called after ROI set.
	 * @param gr the ROI object
	 * @param idRoi the ROI id
	 */
	protected void updateRoiInfo(GammaRoi gr, int idRoi){//double dd1, double dd2) {
		JFreeChart c = this.cp.getChart();
		if (c != null) {
			
			double dd1=gr.getStartChannel();
			double dd2=gr.getEndChannel();
			//----------
			double dd1en=getKevFromChannel(dd1);//gr.getStartChannel();
			double dd2en=getKevFromChannel(dd2);//gr.getEndChannel();
			//=======
			/*if (!isChannelDisplay) {
				dd1 = getKevFromChannel(dd1);
				dd2 = getKevFromChannel(dd2);
			}*/
			
			//XYPlot plot = (XYPlot) c.getPlot();//always
			XYPlot plot = (XYPlot)chart.getPlot();//(XYPlot) c.getPlot();//
			XYPlot ploten = (XYPlot)charten.getPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@
			XYPlot plotln = (XYPlot)chartln.getPlot();
			XYPlot plotlnen = (XYPlot)chartlnen.getPlot();
			XYPlot plotsqrt = (XYPlot)chartsqrt.getPlot();
			XYPlot plotsqrten = (XYPlot)chartsqrten.getPlot();
			// displaying the ROI
			XYSeries series2 = new XYSeries("roi");// not shown
			XYSeries series2en = new XYSeries("roi");// not shown
			XYSeries series2ln = new XYSeries("roi");// not shown
			XYSeries series2lnen = new XYSeries("roi");// not shown
			XYSeries series2sqrt = new XYSeries("roi");// not shown
			XYSeries series2sqrten = new XYSeries("roi");// not shown

			XYSeriesCollection data = (XYSeriesCollection) plot.getDataset(0);
			XYSeries series = data.getSeries(0);
			
			//XYSeriesCollection dataen = (XYSeriesCollection) ploten
			//		.getDataset(0);
			//XYSeries seriesen = dataen.getSeries(0);
			//============
			//XYSeriesCollection dataln = (XYSeriesCollection) plotln.getDataset(0);
			//XYSeries seriesln = dataln.getSeries(0);
			//XYSeriesCollection datalnen = (XYSeriesCollection) plotlnen.getDataset(0);
			//XYSeries serieslnen = datalnen.getSeries(0);
			//XYSeriesCollection datasqrt = (XYSeriesCollection) plotsqrt.getDataset(0);
			//XYSeries seriessqrt = datasqrt.getSeries(0);
			//XYSeriesCollection datasqrten = (XYSeriesCollection) plotsqrten.getDataset(0);
			//XYSeries seriessqrten = datasqrten.getSeries(0);
			
			int n = series.getItemCount();
			// setting roi based on primary data!
			for (int j = 0; j < n; j++) {
				Number x = series.getX(j);
				Number y = series.getY(j);
				/*if(!isChannelDisplay){//no point, redundant x above comes from chart
					x = seriesen.getX(j);
					y = seriesen.getY(j);
				}*/
				//------------
				double lnd =0.0; double sqrtd = 0.0;
				if(y.doubleValue()!=0.0){
					lnd = Math.log(y.doubleValue());
					sqrtd = Math.sqrt(y.doubleValue());
				} else{
					lnd = 0.0;
					sqrtd = 0.0;
				}
				//-----------
				double xx =0.0; ///double yy=0.0;
				double xxen =0.0; //double yyen=0.0;
				/*if(isChannelDisplay){//no point, redundant x above comes from chart
					xx = x.doubleValue();
					xxen = getKevFromChannel(xx);
				} else{
					//it is kev
					xxen=x.doubleValue();
					xx=getChannelFromKeV(xxen);
				}*/
				xx = x.doubleValue();
				xxen = getKevFromChannel(xx);
				
				/*if ((dd1 <= x.doubleValue()) && (x.doubleValue() <= dd2)) {
					series2.add(x, y);
				}
				if ((dd1en <= x.doubleValue()) && (x.doubleValue() <= dd2en)) {
					series2en.add(x, y);
				}*/
				if ((dd1 <= xx) && (xx <= dd2)) {
					series2.add(xx, y);
					series2ln.add(xx, lnd);
					series2sqrt.add(xx, sqrtd);
				}
				if ((dd1en <= xxen) && (xxen <= dd2en)) {//not need separate if...if
					//fall between channels then it also fall between energies!!!
					//see updateRoiFromDatabase
					series2en.add(xxen, y);
					series2lnen.add(xxen, lnd);
					series2sqrten.add(xxen, sqrtd);
				}
			}
			int idata=idRoi+2;//0,1 reserved.so roi 0 is 2!!!
			XYSeriesCollection data2 = new XYSeriesCollection(series2);
			plot.setDataset(idata, data2);
			
			XYSeriesCollection data2en = new XYSeriesCollection(series2en);
			ploten.setDataset(idata, data2en);
			//--------------------
			XYSeriesCollection data2ln = new XYSeriesCollection(series2ln);
			plotln.setDataset(idata, data2ln);
			XYSeriesCollection data2lnen = new XYSeriesCollection(series2lnen);
			plotlnen.setDataset(idata, data2lnen);
			XYSeriesCollection data2sqrt = new XYSeriesCollection(series2sqrt);
			plotsqrt.setDataset(idata, data2sqrt);
			XYSeriesCollection data2sqrten = new XYSeriesCollection(series2sqrten);
			plotsqrten.setDataset(idata, data2sqrten);
			
			XYItemRenderer renderer2 = new XYAreaRenderer();
			
			if (!gr.getNuclide().equals("NoName")){
				renderer2.setSeriesPaint(0, Color.green);
			} else			
				renderer2.setSeriesPaint(0, Color.red);
			renderer2.setSeriesVisibleInLegend(0, false);
			plot.setRenderer(idata, renderer2);
			
			ploten.setRenderer(idata, renderer2);
			
			plotln.setRenderer(idata, renderer2);
			plotlnen.setRenderer(idata, renderer2);
			plotsqrt.setRenderer(idata, renderer2);
			plotsqrten.setRenderer(idata, renderer2);
			
			clearMarkersAndSetRoiCenter(gr);
		}
	}

	/**
	 * Update (load) spectrum from database
	 */
	protected void updateSpectrumFromDatabase() {

		// first remove the existing chart
		cp.removeAll();
		// and ROIs, if any
		roiV.removeAllElements();
		roiCb.removeAllItems();
		roilabel.setText(resources.getString("roi.count")
				+ Convertor.intToString(roiV.size()));
		roiNamelabel.setText(resources.getString("roi.name.label") + "NoName");
		// setting the chart and chart panel
		JFreeChart gammaChart = getGammaChart();
		cp.setChart(gammaChart);

		String sname = spectrumName;
		gammaChart.setTitle(sname);

		emptyChartB = false;// we have a valid chart
		isChannelDisplay = true;// we have channel display!
		channelRb.setSelected(true);

		isLinB = true;
		isLnB = false;
		isSqrtB = false;

		lnItem.setState(isLnB);
		sqrtItem.setState(isSqrtB);
		linItem.setState(isLinB);

	}

	/**
	 * Update (load) spectrum from a file.
	 */
	private void updateSpectrum() {
		// read spectrum and construct vectors holding the data!
		readSpectrum(spectrumFile);
		// if some errors occured after reading!
		if (channelV == null) {
			statusL.setText(resources.getString("status.error"));
			stopThread();
			return;
		}

		// first remove the existing chart
		cp.removeAll();
		// and ROIs, if any
		roiV.removeAllElements();
		roiCb.removeAllItems();
		roilabel.setText(resources.getString("roi.count")
				+ Convertor.intToString(roiV.size()));
		roiNamelabel.setText(resources.getString("roi.name.label") + "NoName");
		// setting the chart and chart panel
		JFreeChart gammaChart = getGammaChart();
		cp.setChart(gammaChart);
		emptyChartB = false;// we have a valid chart
		isChannelDisplay = true;// we have channel display!
		channelRb.setSelected(true);

		isLinB = true;
		isLnB = false;
		isSqrtB = false;

		lnItem.setState(isLnB);
		sqrtItem.setState(isSqrtB);
		linItem.setState(isLinB);

		// performing final tasks!
		idSpectrumTf.setText(resources.getString("chart.NAME"));
		quantityTf.setText("1");
		quantityUnitTf.setText("kg");
		spectrumLiveTimeTf.setText("");
		today();
		spectrumLiveTime = 0.0;// reset

		if (!channelWarningB)
			statusL.setText(resources.getString("status.openSpectrum")+" "+spectrumFile);

		stopThread();
	}

	/**
	 * Stop application thread
	 */
	protected void stopThread() {	
		statusTh = null;
		frameNumber = 0;
		stopAnim=true;
		if (appTh == null) {
			return;
		}
		appTh = null;
		//setEnabled(true);
		channelRb.setSelected(isChannelDisplay);
		keVRb.setSelected(!isChannelDisplay);
	}

	@SuppressWarnings("unchecked")
	/**
	 * Delete ROI
	 */
	private void deleteRoi() {
		if (emptyChartB)
			return;

		if (roiV.size() == 0) {
			return;
		}

		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				//XYPlot plot = (XYPlot) c.getPlot();
				XYPlot plot = (XYPlot) chart.getPlot();
				XYPlot ploten = (XYPlot) charten.getPlot();
				
				XYPlot plotln = (XYPlot) chartln.getPlot();
				XYPlot plotlnen = (XYPlot) chartlnen.getPlot();
				XYPlot plotsqrt = (XYPlot) chartsqrt.getPlot();
				XYPlot plotsqrten = (XYPlot) chartsqrten.getPlot();
				
				if (idataset > 2) {
					String idRoiS = (String) roiCb.getSelectedItem();
					int idRoi = Convertor.stringToInt(idRoiS) - 1;
					int idata = 2 + idRoi;// 0,1 reserved..
					// start from 2 where roiSize=1!
					// end, via insertRoi, at 2+roiV.size()-1, which is
					// 1(reserved)+roiVSize!!
					idataset--;

					plot.setDataset(idata, null);
					plot.setRenderer(idata, null);
					
					ploten.setDataset(idata, null);
					ploten.setRenderer(idata, null);
					
					plotln.setDataset(idata, null);
					plotln.setRenderer(idata, null);
					plotlnen.setDataset(idata, null);
					plotlnen.setRenderer(idata, null);
					plotsqrt.setDataset(idata, null);
					plotsqrt.setRenderer(idata, null);
					plotsqrten.setDataset(idata, null);
					plotsqrten.setRenderer(idata, null);
					// ROIs
					roiV.removeElementAt(idata - 2);
					// here roiV size is already decreased!!!!!!
					for (int i = idata; i < 2 + roiV.size(); i++) {
						// roiV is decreased earlier!
						XYSeriesCollection data = (XYSeriesCollection) plot
								.getDataset(i + 1);
						XYItemRenderer renderer = (XYItemRenderer) plot
								.getRenderer(i + 1);
						plot.setDataset(i, data);
						plot.setRenderer(i, renderer);
						//------------
						XYSeriesCollection dataen = (XYSeriesCollection) ploten
								.getDataset(i + 1);
						XYItemRenderer rendereren = (XYItemRenderer) ploten
								.getRenderer(i + 1);
						ploten.setDataset(i, dataen);
						ploten.setRenderer(i, rendereren);
						//====================
						XYSeriesCollection dataln = (XYSeriesCollection) plotln
								.getDataset(i + 1);
						XYItemRenderer rendererln = (XYItemRenderer) plotln
								.getRenderer(i + 1);
						plotln.setDataset(i, dataln);
						plotln.setRenderer(i, rendererln);
						XYSeriesCollection datalnen = (XYSeriesCollection) plotlnen
								.getDataset(i + 1);
						XYItemRenderer rendererlnen = (XYItemRenderer) plotlnen
								.getRenderer(i + 1);
						plotlnen.setDataset(i, datalnen);
						plotlnen.setRenderer(i, rendererlnen);
						
						XYSeriesCollection datasqrt = (XYSeriesCollection) plotsqrt
								.getDataset(i + 1);
						XYItemRenderer renderersqrt = (XYItemRenderer) plotsqrt
								.getRenderer(i + 1);
						plotsqrt.setDataset(i, datasqrt);
						plotsqrt.setRenderer(i, renderersqrt);
						XYSeriesCollection datasqrten = (XYSeriesCollection) plotsqrten
								.getDataset(i + 1);
						XYItemRenderer renderersqrten = (XYItemRenderer) plotsqrten
								.getRenderer(i + 1);
						plotsqrten.setDataset(i, datasqrten);
						plotsqrten.setRenderer(i, renderersqrten);
					}
					// finally setting last dataset to null!
					plot.setDataset(roiV.size() + 2, null);
					plot.setRenderer(roiV.size() + 2, null);
					
					ploten.setDataset(roiV.size() + 2, null);
					ploten.setRenderer(roiV.size() + 2, null);
					
					plotln.setDataset(roiV.size() + 2, null);
					plotln.setRenderer(roiV.size() + 2, null);
					plotlnen.setDataset(roiV.size() + 2, null);
					plotlnen.setRenderer(roiV.size() + 2, null);
					
					plotsqrt.setDataset(roiV.size() + 2, null);
					plotsqrt.setRenderer(roiV.size() + 2, null);
					plotsqrten.setDataset(roiV.size() + 2, null);
					plotsqrten.setRenderer(roiV.size() + 2, null);

					// in order to reset combobox we must remove listener!
					roiCb.removeItemListener(this);
					roiCb.removeAllItems();
					if (roiV.size() != 0)
						for (int i = 1; i <= roiV.size(); i++) {
							roiCb.addItem(Convertor.intToString(i));
						}
					roiCb.setSelectedItem(Convertor.intToString(roiV.size()));
					roiCb.addItemListener(this);

					selectRoi();

					roilabel.setText(resources.getString("roi.count")
							+ Convertor.intToString(roiV.size()));

					if (roiV.size() == 0) {
						spectrumLiveTime = 0.0;// reset
					}
				}
			}
		}
	}

	/**
	 * Move first marker to the left.
	 */
	private void movemarkerleft() {
		double one = 1.0;
		// ALWAYS MOVE BY 1=data interval!!regardless display!
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				ValueMarker vm1 = markers[0];
				ValueMarker vm2 = markers[1];
				if (vm1 != null) {
					double d1 = vm1.getValue();
					if (vm2 != null) {
						double d2 = vm2.getValue();
						if (d1 < d2) {
							if (d1 - one >= MINBOUND) {
								markers[0].setValue(d1 - one);
								textAnnots[0].setX(d1 - one);
							}
						} else {
							if (d2 - one >= MINBOUND) {
								markers[1].setValue(d2 - one);
								textAnnots[1].setX(d2 - one);
							}
						}
					} else {
						if (d1 - one >= MINBOUND) {
							markers[0].setValue(d1 - one);
							textAnnots[0].setX(d1 - one);
						}
					}
				}
			}
		}

	}

	/**
	 * Move second marker to the left.
	 */
	private void movemarkerleft2() {
		double one = 1.0;
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				ValueMarker vm1 = markers[0];
				ValueMarker vm2 = markers[1];
				if (vm1 != null) {
					double d1 = vm1.getValue();
					if (vm2 != null) {
						double d2 = vm2.getValue();
						if (d1 < d2) {
							if (d2 - one >= MINBOUND) {
								markers[1].setValue(d2 - one);
								textAnnots[1].setX(d2 - one);
							}
						} else {
							if (d1 - one >= MINBOUND) {//
								markers[0].setValue(d1 - one);
								textAnnots[0].setX(d1 - one);
							}
						}
					} else {
						// do nothing...for a single marker set!
					}
				}
			}
		}

	}

	/**
	 * Move first marker to the right.
	 */
	private void movemarkerright() {
		double one = 1.0;
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				ValueMarker vm1 = markers[0];
				ValueMarker vm2 = markers[1];
				if (vm1 != null) {
					double d1 = vm1.getValue();
					if (vm2 != null) {
						double d2 = vm2.getValue();
						if (d1 < d2) {
							if (d1 + one <= MAXBOUND) {
								markers[0].setValue(d1 + one);
								textAnnots[0].setX(d1 + one);
							}
						} else {
							if (d2 + one <= MAXBOUND) {
								markers[1].setValue(d2 + one);
								textAnnots[1].setX(d2 + one);
							}
						}
					} else {
						if (d1 + one <= MAXBOUND) {
							markers[0].setValue(d1 + one);
							textAnnots[0].setX(d1 + one);
						}
					}
				}
			}
		}

	}

	/**
	 * Move second marker to the right.
	 */
	private void movemarkerright2() {
		double one = 1.0;
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				ValueMarker vm1 = markers[0];
				ValueMarker vm2 = markers[1];
				if (vm1 != null) {
					double d1 = vm1.getValue();
					if (vm2 != null) {
						double d2 = vm2.getValue();
						if (d1 < d2) {
							if (d2 + one <= MAXBOUND) {
								markers[1].setValue(d2 + one);
								textAnnots[1].setX(d2 + one);
							}
						} else {
							if (d1 + one <= MAXBOUND) {
								markers[0].setValue(d1 + one);
								textAnnots[0].setX(d1 + one);
							}
						}
					} else {
						// do nothing...for a single marker set!
					}
				}
			}
		}

	}

	/**
	 * Refresh chart. Restore to default view ("un-zoomed" and "un-moved"). It also updates chart title.
	 */
	private void refresh() {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				cp.restoreAutoBounds();
				String sname = idSpectrumTf.getText();
				c.setTitle(sname);
				spectrumName=sname;
			}
		}
	}

	/**
	 * Navigate the chart to the left
	 */
	private void panLeft() {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				ChartRenderingInfo cri = cp.getChartRenderingInfo();
				PlotRenderingInfo pri = cri.getPlotInfo();
				Point2D p = plot.getQuadrantOrigin();

				plot.panDomainAxes(-0.05, pri, p);
			}

		}
	}

	/**
	 * Navigate the chart to the right
	 */
	private void panRight() {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				ChartRenderingInfo cri = cp.getChartRenderingInfo();
				PlotRenderingInfo pri = cri.getPlotInfo();
				Point2D p = plot.getQuadrantOrigin();

				plot.panDomainAxes(0.05, pri, p);
			}

		}
	}

	/**
	 * Navigate the chart upward
	 */
	private void panUp() {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				ChartRenderingInfo cri = cp.getChartRenderingInfo();
				PlotRenderingInfo pri = cri.getPlotInfo();
				Point2D p = plot.getQuadrantOrigin();

				plot.panRangeAxes(0.05, pri, p);
			}

		}
	}

	/**
	 * Navigate the chart downward
	 */
	private void panDown() {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				ChartRenderingInfo cri = cp.getChartRenderingInfo();
				PlotRenderingInfo pri = cri.getPlotInfo();
				Point2D p = plot.getQuadrantOrigin();

				plot.panRangeAxes(-0.05, pri, p);
			}

		}
	}

	/**
	 * Chart zoom in
	 */
	private void zoomIn() {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				ChartRenderingInfo cri = cp.getChartRenderingInfo();
				PlotRenderingInfo pri = cri.getPlotInfo();
				Point2D p = plot.getQuadrantOrigin();

				plot.zoomDomainAxes(0.95, pri, p);// zoom in
				plot.zoomRangeAxes(0.95, pri, p);
			}

		}
	}

	/**
	 * Chart zoom out
	 */
	private void zoomOut() {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				ChartRenderingInfo cri = cp.getChartRenderingInfo();
				PlotRenderingInfo pri = cri.getPlotInfo();
				Point2D p = plot.getQuadrantOrigin();

				plot.zoomDomainAxes(1.05, pri, p);// zoom out
				plot.zoomRangeAxes(1.05, pri, p);
			}

		}
	}

	/**
	 * Handles the mouse moved over the chart.
	 * 
	 * @param event
	 *            the event.
	 */
	public void chartMouseMoved(ChartMouseEvent event) {
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				Point2D p = event.getTrigger().getPoint();
				// it seems this is slightly better than:
				// cp.translateScreenToJava2D(event.getTrigger().getPoint());
				Rectangle2D plotArea = cp.getScreenDataArea();
				XYPlot plot = (XYPlot) c.getPlot();
				chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea,
						plot.getDomainAxisEdge());
				chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea,
						plot.getRangeAxisEdge());

				statusL.setText("x: " + chartX + " ; y: " + chartY);
			}
		}
	}

	/**
	 * Handles the mouse clicked on the chart.
	 * 
	 * @param event
	 *            the event.
	 */
	public void chartMouseClicked(ChartMouseEvent event) {
		if (emptyChartB)
			return;
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {

				Point2D p = event.getTrigger().getPoint();
				// it seems this is slightly better than:
				// cp.translateScreenToJava2D(event.getTrigger().getPoint());
				Rectangle2D plotArea = cp.getScreenDataArea();
				XYPlot plot = (XYPlot) c.getPlot();
				chartX = plot.getDomainAxis().java2DToValue(p.getX(), plotArea,
						plot.getDomainAxisEdge());
				chartY = plot.getRangeAxis().java2DToValue(p.getY(), plotArea,
						plot.getRangeAxisEdge());

				if (imarker < maxMarkerCount) {

					if (markerReset) {
						plot.removeDomainMarker(markers[imarker],
								Layer.BACKGROUND);
						plot.removeAnnotation(textAnnots[imarker]);
					}

					markers[imarker] = this.getMarker();
					textAnnots[imarker] = this.getMarkerLabel(plot);
					plot.addDomainMarker(markers[imarker], Layer.BACKGROUND);
					plot.addAnnotation(textAnnots[imarker]);

					imarker++;
				} else {
					imarker = 0;// reinitialize
					markerReset = true;// mark reinitialization

					plot.removeDomainMarker(markers[imarker], Layer.BACKGROUND);
					plot.removeAnnotation(textAnnots[imarker]);

					markers[imarker] = this.getMarker();
					textAnnots[imarker] = this.getMarkerLabel(plot);
					plot.addDomainMarker(markers[imarker], Layer.BACKGROUND);
					plot.addAnnotation(textAnnots[imarker]);

					imarker++;
				}
				// ROI auto-select
				if (roiV.size() > 0) {
					for (int i = 0; i < roiV.size(); i++) {
						GammaRoi gr = roiV.elementAt(i);
						double start = gr.getStartChannel();
						double end = gr.getEndChannel();
						if (!isChannelDisplay) {
							start = getKevFromChannel(start);
							end = getKevFromChannel(end);
						}
						if ((start <= chartX) && (chartX <= end)) {
							// roi auto-select via combobox itemStateChanged!
							roiCb.setSelectedItem(Convertor.intToString(i + 1));
							break;
						}
					}
				}
				// end ROI auto-select
			}
		}
	}

	/**
	 * Internally used. Retrieve the marker.
	 * @return the result
	 */
	private ValueMarker getMarker() {
		ValueMarker valuemarker = null;
		if (chartX < MINBOUND) {
			valuemarker = new ValueMarker(MINBOUND);
		} else if (chartX > MAXBOUND) {
			valuemarker = new ValueMarker(MAXBOUND);
		} else {
			valuemarker = new ValueMarker(chartX);
		}
		valuemarker.setLabelOffsetType(LengthAdjustmentType.EXPAND);
		valuemarker.setPaint(Color.blue);
		valuemarker.setStroke(new BasicStroke(2.0F));

		return valuemarker;
	}

	/**
	 * Internally used. Retrieve the marker label.
	 * @param plot the plot
	 * @return the result
	 */
	private XYTextAnnotation getMarkerLabel(XYPlot plot) {
		ValueAxis axis = plot.getRangeAxis();
		double max = axis.getUpperBound();// maximum axis value
		double min = axis.getLowerBound();// minimum axis value

		XYTextAnnotation textAnnontation = null;
		if (chartX < MINBOUND) {
			textAnnontation = new XYTextAnnotation(
					resources.getString("chart.marker") + (imarker + 1),
					MINBOUND, (max + min) / 2.0);
		} else if (chartX > MAXBOUND) {
			textAnnontation = new XYTextAnnotation(
					resources.getString("chart.marker") + (imarker + 1),
					MAXBOUND, (max + min) / 2.0);
		} else {
			textAnnontation = new XYTextAnnotation(
					resources.getString("chart.marker") + (imarker + 1),
					chartX, (max + min) / 2.0);
		}
		textAnnontation.setRotationAngle(-Math.PI / 2);
		textAnnontation.setRotationAnchor(TextAnchor.BASELINE_CENTER);
		textAnnontation.setTextAnchor(TextAnchor.BASELINE_CENTER);
		textAnnontation.setPaint(Color.BLUE);

		return textAnnontation;
	}

	/**
	 * Checks if spectrum live time (provided by user) is valid.
	 * @return the result
	 */
	private boolean checkLiveTime() {
		boolean nulneg = false;
		boolean b = false;

		double newspectrumLiveTime = 0.0;
		// 2. Spectrum data: liveTime...
		try {
			newspectrumLiveTime = Convertor.stringToDouble(spectrumLiveTimeTf
					.getText());
			if (newspectrumLiveTime <= 0)
				nulneg = true;
		} catch (Exception e) {
			String title = resources.getString("number.error.roiInsert.title");
			String message = resources.getString("number.error.roiInsert");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
			return false;
		}
		if (nulneg) {
			String title = resources.getString("number.error.roiInsert.title");
			String message = resources.getString("number.error.roiInsert");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return false;
		}

		if (spectrumLiveTime != 0.0) {
			// database loaded spectrum or use BKG was set or new roi is set
			if (spectrumLiveTime != newspectrumLiveTime) {

				String title = resources
						.getString("number.error.roiInsert.title");
				String message = resources
						.getString("number.error.roiInsert.check");
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.ERROR_MESSAGE);
				b = false;
			} else
				b = true;

		} else {
			// it is NOT a database load and it is the first Roi Insert attempt!
			spectrumLiveTime = newspectrumLiveTime;
			b = true;
		}
		return b;
	}

	@SuppressWarnings("unchecked")
	/**
	 * Insert ROI.
	 */
	private void insertRoi() {
		if (emptyChartB)
			return;
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {

				boolean checkb = checkLiveTime();
				if (!checkb) {
					return;
				}
				//XYPlot plot = (XYPlot) c.getPlot();
				XYPlot plot = (XYPlot)chart.getPlot();//(XYPlot) c.getPlot();//
				XYPlot ploten = (XYPlot)charten.getPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@
				
				XYPlot plotln = (XYPlot)chartln.getPlot();
				XYPlot plotlnen = (XYPlot)chartlnen.getPlot();
				XYPlot plotsqrt = (XYPlot)chartsqrt.getPlot();
				XYPlot plotsqrten = (XYPlot)chartsqrten.getPlot();
				
				double d1 = -1.0;
				double d2 = 0.0;
				Iterator<?> i = null;

				Collection<?> col = plot.getDomainMarkers(Layer.BACKGROUND);
				if (isLnB)
					col = plotln.getDomainMarkers(Layer.BACKGROUND);
				else if (isSqrtB)
					col = plotsqrt.getDomainMarkers(Layer.BACKGROUND);
				if (!isChannelDisplay){
					col = ploten.getDomainMarkers(Layer.BACKGROUND);
					
					if (isLnB)
						col = plotlnen.getDomainMarkers(Layer.BACKGROUND);
					else if (isSqrtB)
						col = plotsqrten.getDomainMarkers(Layer.BACKGROUND);
				}
				
				if (col != null) {
					i = col.iterator();
					int colSize = col.size();
					if (colSize < 2)
						return;// two markers only!
				} else
					return;

				if (i != null)
					while (i.hasNext()) {
						ValueMarker vm = (ValueMarker) i.next();
						if (d1 != -1.0)// was used
							d2 = vm.getValue();
						else
							d1 = vm.getValue();
					}
				if (d1 < 0)
					d1 = 0.0;// channel or energy are positive!
				if (d2 < 0)
					d2 = 0.0;// channel or energy are positive!

				if (d1 == d2)
					return;// no roi!!!!
				double tmp = 0.0;
				if (d1 > d2) {// swap...ascend order!
					tmp = d1;
					d1 = d2;
					d2 = tmp;
				}

				double dd1 = d1;
				double dd2 = d2;
				if (!isChannelDisplay) {
					dd1 = getChannelFromKeV(d1);
					dd2 = getChannelFromKeV(d2);
				} else {
					dd1 = adjustChannel(d1);
					dd2 = adjustChannel(d2);
				}
				if (dd1 == dd2)
					return;// no roi!!!!

				// adding ROI...in channel domain only!
				GammaRoi gr = new GammaRoi(dd1, dd2);

				int i1 = (new Double(dd1)).intValue();
				int i2 = (new Double(dd2)).intValue();
				for (int j = i1; j <= i2; j++) {
					gr.addChannel(channelI[j]);
					gr.addPulses(pulsesD[j]);
					gr.addBkgPulses(bkgpulsesD[j]);
				}
				gr.setStartEdgePulses(getAverageContinuumEdges(i1));//pulsesD[i1]);
				gr.setEndEdgePulses(getAverageContinuumEdges(i2));//pulsesD[i2]);
				gr.setLiveTime(spectrumLiveTime);

				if (naiRb.isSelected()) {
					iNet = NET_CALCULATION_NAI;
				} else if (geRb.isSelected()) {
					iNet = NET_CALCULATION_GE;
				} else if (gaussRb.isSelected()) {
					iNet = NET_CALCULATION_GAUSSIAN;
				}
				gr.setNetCalculationMethod(iNet);
				gr.setConfidenceLevel(roiConfidenceLevel);
				gr.performBasicComputation();

				// gr.printCalib();

				roiV.addElement(gr);

				roiCb.addItem(Convertor.intToString(roiV.size()));
				roiCb.setSelectedItem(Convertor.intToString(roiV.size()));

				//================
				double dd1en=getKevFromChannel(dd1);//gr.getStartChannel();
				double dd2en=getKevFromChannel(dd2);
				/*if (!isChannelDisplay) {
					// restore the kev domain for proper display!
					dd1 = getKevFromChannel(dd1);
					dd2 = getKevFromChannel(dd2);
				}*/

				// displaying the ROI
				XYSeries series2 = new XYSeries("roi");// not shown
				XYSeries series2en = new XYSeries("roi");// not shown
				
				XYSeries series2ln = new XYSeries("roi");// not shown
				XYSeries series2lnen = new XYSeries("roi");// not shown
				XYSeries series2sqrt = new XYSeries("roi");// not shown
				XYSeries series2sqrten = new XYSeries("roi");// not shown

				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);
				
				//XYSeriesCollection dataen = (XYSeriesCollection) ploten
				//		.getDataset(0);
				//XYSeries seriesen = dataen.getSeries(0);
				
				//XYSeriesCollection dataln = (XYSeriesCollection) plotln
				//		.getDataset(0);
				//XYSeries seriesln = dataln.getSeries(0);
				//XYSeriesCollection datalnen = (XYSeriesCollection) plotlnen
				//		.getDataset(0);
				//XYSeries serieslnen = datalnen.getSeries(0);
				
				//XYSeriesCollection datasqrt = (XYSeriesCollection) plotsqrt
				//		.getDataset(0);
				//XYSeries seriessqrt = datasqrt.getSeries(0);
				//XYSeriesCollection datasqrten = (XYSeriesCollection) plotsqrten
				//		.getDataset(0);
				//XYSeries seriessqrten = datasqrten.getSeries(0);
				
				int n = series.getItemCount();
				for (int j = 0; j < n; j++) {
					Number x = series.getX(j);//series =>channels
					Number y = series.getY(j);//series =>pulses
					//if(!isChannelDisplay){//redundant
					//	x = seriesen.getX(j);
					//	y = seriesen.getY(j);
					//}
					
					//------------
					double lnd =0.0; double sqrtd = 0.0;
					if(y.doubleValue()!=0.0){
						lnd = Math.log(y.doubleValue());
						sqrtd = Math.sqrt(y.doubleValue());
					} else{
						lnd = 0.0;
						sqrtd = 0.0;
					}
					//-----------
					double xx =0.0; ///double yy=0.0;
					double xxen =0.0; //double yyen=0.0;
					//if(isChannelDisplay){
					//	xx = x.doubleValue();
					//	xxen = getKevFromChannel(xx);
					//} else{
						//it is kev
					//	xxen=x.doubleValue();
					//	xx=getChannelFromKeV(xxen);
					//}
					xx = x.doubleValue();
					xxen = getKevFromChannel(xx);
					/*if ((dd1 <= x.doubleValue()) && (x.doubleValue() <= dd2)) {
						series2.add(x, y);
					}*/
					if ((dd1 <= xx) && (xx <= dd2)) {
						series2.add(xx, y);
						series2ln.add(xx, lnd);
						series2sqrt.add(xx, sqrtd);
					}
					if ((dd1en <= xxen) && (xxen <= dd2en)) {
						series2en.add(xxen, y);
						series2lnen.add(xxen, lnd);
						series2sqrten.add(xxen, sqrtd);
					}
				}
				XYSeriesCollection data2 = new XYSeriesCollection(series2);
				plot.setDataset(idataset, data2);
				
				XYSeriesCollection data2en = new XYSeriesCollection(series2en);
				ploten.setDataset(idataset, data2en);
				
				XYSeriesCollection data2ln = new XYSeriesCollection(series2ln);
				plotln.setDataset(idataset, data2ln);
				XYSeriesCollection data2lnen = new XYSeriesCollection(series2lnen);
				plotlnen.setDataset(idataset, data2lnen);
				XYSeriesCollection data2sqrt = new XYSeriesCollection(series2sqrt);
				plotsqrt.setDataset(idataset, data2sqrt);
				XYSeriesCollection data2sqrten = new XYSeriesCollection(series2sqrten);
				plotsqrten.setDataset(idataset, data2sqrten);
				
				XYItemRenderer renderer2 = new XYAreaRenderer();
				renderer2.setSeriesPaint(0, Color.red);
				renderer2.setSeriesVisibleInLegend(0, false);
				plot.setRenderer(idataset, renderer2);
				
				ploten.setRenderer(idataset, renderer2);
				
				plotln.setRenderer(idataset, renderer2);
				plotlnen.setRenderer(idataset, renderer2);
				plotsqrt.setRenderer(idataset, renderer2);
				plotsqrten.setRenderer(idataset, renderer2);

				idataset++;
				// finally set a center marker and perform some cleaning
				// already set by combobox item state change=>select roi!!!
				roilabel.setText(resources.getString("roi.count")
						+ Convertor.intToString(roiV.size()));
			}
		}
	}

	/**
	 * Internally used. When ROI was set, clear markers and set roi center axis with 
	 * information about ROI.
	 * @param gr the ROI
	 */
	private void clearMarkersAndSetRoiCenter(GammaRoi gr) {
		if (emptyChartB)
			return;
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				// first clear markers
				plot.clearDomainMarkers();
				plot.clearAnnotations();
				imarker = 0;// reset
				markers[1] = null;// reset
				// setting up center markers
				double d = gr.getCenterChannel();
				if (!isChannelDisplay) {
					// restore the kev domain for proper display!
					d = getKevFromChannel(d);
				}

				double markerVal = d;
				markers[imarker] = new ValueMarker(markerVal);
				markers[imarker]
						.setLabelOffsetType(LengthAdjustmentType.EXPAND);
				markers[imarker].setPaint(Color.blue);
				markers[imarker].setStroke(new BasicStroke(2.0F));

				ValueAxis axis = plot.getRangeAxis();
				double max = axis.getUpperBound();// maximum axis value
				double min = axis.getLowerBound();// minimum axis value
				//===============
				if (!gr.getNuclide().equals("NoName")){
					textAnnots[imarker] = new XYTextAnnotation(
							gr.getNuclide()+" @:"+Convertor.formatNumber(gr.getCentroidEnergy(), 2),
							markerVal, (max + min) / 2.0);
				}else
				//===========
				textAnnots[imarker] = new XYTextAnnotation(
						resources.getString("chart.marker") + (imarker + 1),
						markerVal, (max + min) / 2.0);
				textAnnots[imarker].setRotationAngle(-Math.PI / 2);
				textAnnots[imarker]
						.setRotationAnchor(TextAnchor.BASELINE_CENTER);
				textAnnots[imarker].setTextAnchor(TextAnchor.BASELINE_CENTER);
				textAnnots[imarker].setPaint(Color.BLUE);

				plot.addDomainMarker(markers[imarker], Layer.BACKGROUND);
				plot.addAnnotation(textAnnots[imarker]);
				imarker++;
			}
		}

	}

	/**
	 * Display the ROI edges on chart.
	 * @param gr the ROI
	 */
	private void showRoiEdge(GammaRoi gr) {
		if (emptyChartB)
			return;
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) c.getPlot();
				// /setting up the edge markers
				double sd = gr.getStartEdgeChannel();
				if (!isChannelDisplay) {
					// restore the kev domain for proper display!
					sd = getKevFromChannel(sd);
				}

				ValueMarker sm = new ValueMarker(sd);
				sm.setLabelOffsetType(LengthAdjustmentType.EXPAND);
				sm.setPaint(Color.black);
				sm.setStroke(new BasicStroke(2.0F));
				ValueAxis yaxis = plot.getRangeAxis();
				double ymax = yaxis.getUpperBound();// maximum axis value
				double ymin = yaxis.getLowerBound();// minimum axis value
				XYTextAnnotation st = new XYTextAnnotation(
						resources.getString("chart.edge.start"), sd,
						(ymax + ymin) / 2.0);
				st.setRotationAngle(-Math.PI / 2);
				st.setRotationAnchor(TextAnchor.BASELINE_CENTER);
				st.setTextAnchor(TextAnchor.BASELINE_CENTER);
				st.setPaint(Color.black);
				plot.addDomainMarker(sm, Layer.FOREGROUND);
				// separate from other markers!
				plot.addAnnotation(st);

				double ed = gr.getEndEdgeChannel();
				if (!isChannelDisplay) {
					// restore the kev domain for proper display!
					ed = getKevFromChannel(ed);
				}

				ValueMarker em = new ValueMarker(ed);
				em.setLabelOffsetType(LengthAdjustmentType.EXPAND);
				em.setPaint(Color.black);
				em.setStroke(new BasicStroke(2.0F));

				XYTextAnnotation et = new XYTextAnnotation(
						resources.getString("chart.edge.end"), ed,
						(ymax + ymin) / 2.0);
				et.setRotationAngle(-Math.PI / 2);
				et.setRotationAnchor(TextAnchor.BASELINE_CENTER);
				et.setTextAnchor(TextAnchor.BASELINE_CENTER);
				et.setPaint(Color.black);
				plot.addDomainMarker(em, Layer.FOREGROUND);
				// separate from other markers!
				plot.addAnnotation(et);
			}
		}
	}

	/**
	 * Shows the about window
	 */
	private void about() {
		new AboutFrame(this);
	}

	/**
	 * Close program
	 */
	private void attemptExit() {
		//exit without any warning!!
		
		//String title = resources.getString("dialog.exit.title");
		//String message = resources.getString("dialog.exit.message");

		//Object[] options = (Object[]) resources
		//		.getObject("dialog.exit.buttons");
		//int result = JOptionPane.showOptionDialog(this, message, title,
		//		JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
		//		options, options[0]);
		//if (result == JOptionPane.YES_OPTION) {
		if(standalone){
			//DBConnection.shutdownDerby();
			try{
				if (gammadbcon != null)
					gammadbcon.close();
			}catch (Exception e){
				e.printStackTrace();
			}
			
			DatabaseAgent.shutdownDerby();
			dispose();
			System.exit(0);
		}
		else{
			try{
				if (gammadbcon != null)
					gammadbcon.close();
			}catch (Exception e){
				e.printStackTrace();
			}
			parent.setVisible(true);
			dispose();
		}
		//}
	}

	/**
	 * Open file
	 */
	private void openFile() {
		String FILESEPARATOR = System.getProperty("file.separator");
		String currentDir = System.getProperty("user.dir");
		String myDir = currentDir + FILESEPARATOR;//

		// File select
		JFileChooser chooser = new JFileChooser(myDir);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = chooser.showOpenDialog(this);// parent=this frame
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			spectrumFile = chooser.getSelectedFile().toString();

			statusRunS = resources.getString("status.openingSpectrum");
			startThread();
		}
	}

	/**
	 * Read the spectrum from a file
	 * @param fileS the file
	 */
	private void readSpectrum(String fileS) {
		// first test file..one or two columns!!
		String filename = fileS;

		int iread = 0;
		int lnr = 0;// data number
		@SuppressWarnings("unused")
		int lnrr = 0;// line number
		char lineSep = '\n';
		boolean oneColumnB = false;

		boolean haveData = false;
		try {
			FileInputStream in = new FileInputStream(filename);

			while ((iread = in.read()) != -1) {
				if (!Character.isWhitespace((char) iread)) {
					stringBuffer.append((char) iread);
					haveData = true;
				} else {
					if (haveData)// we have data
					{
						haveData = false;// reset
						lnr++;
					}// have data

					if ((char) iread == lineSep) {
						if (lnr > 1) {
							// we have two columns file
							oneColumnB = false;
						} else {
							// we have one column file
							oneColumnB = true;
						}
						lnrr++;

						break;// exit the loop
					}

					// Finally empty the buffer!
					int nn = stringBuffer.capacity();
					stringBuffer.delete(0, nn);// cleanUp
					stringBuffer.trimToSize();// 0 size!
				}// else
			}// main while
			in.close();
		}// try
		catch (Exception exc) {
			exc.printStackTrace();
		}
		// just in case Finally empty the buffer!
		int nn = stringBuffer.capacity();
		stringBuffer.delete(0, nn);// cleanUp
		stringBuffer.trimToSize();// 0 size!

		// based on first line read, call the appropriate read file method!
		if (oneColumnB)
			readPulses(fileS);
		else
			readChannelPulses(fileS);
	}

	// read one column file:pulses...channel is given by data index!
	/**
	 * Read pulses from a file (when 1 column file)
	 * @param fileS the file
	 */
	private void readPulses(String fileS) {
		// initialize the pair vectors
		if (channelV == null) {
			channelV = new Vector<String>();
			pulsesV = new Vector<String>();
		}

		String filename = fileS;

		int iread = 0;
		int lnr = 0;// data number
		@SuppressWarnings("unused")
		int lnrr = 0;// line number
		// here lnr and lnrr are the same!!!..1 column data file!!!
		char lineSep = '\n';

		boolean haveData = false;
		
		FileInputStream in = null;
		try {
			//FileInputStream in = new FileInputStream(filename);
			in = new FileInputStream(filename);

			while ((iread = in.read()) != -1) {
				if (!Character.isWhitespace((char) iread)) {
					stringBuffer.append((char) iread);
					haveData = true;
				} else {
					if (haveData)// we have data
					{
						haveData = false;// reset

						String s = stringBuffer.toString();
						channelV.addElement(Convertor.intToString(lnr));
						// testing data:
						try {
							Convertor.stringToDouble(s);
						} catch (Exception ee) {
							ee.printStackTrace();
							String title = resources
									.getString("dialog.spectrum.title");
							String message = resources
									.getString("dialog.spectrum.message") + s;
							JOptionPane.showMessageDialog(null, message, title,
									JOptionPane.ERROR_MESSAGE);
							channelV = null;
							pulsesV = null;
							return;
						}
						// end testing data
						pulsesV.addElement(s);

						lnr++;
					}// have data

					if ((char) iread == lineSep)
						lnrr++;

					// Finally empty the buffer!
					int nn = stringBuffer.capacity();
					stringBuffer.delete(0, nn);// cleanUp
					stringBuffer.trimToSize();// 0 size!
				}// else
			}// main while
			//in.close();
		}// try
		catch (Exception exc) {
			exc.printStackTrace();
		}finally {
	        if( null != in ) {
	            try 
	            {
	                in.close();
	            } catch(Exception ex) {
	                // log or fail if you like
	            }
	        }
	    }
	}

	// read two columns file:channel vs pulses
	/**
	 * Read channel and pulses from a file (when 2 columns file)
	 * @param fileS the file
	 */
	private void readChannelPulses(String fileS) {
		if (channelV == null) {
			channelV = new Vector<String>();
			pulsesV = new Vector<String>();
		}
//System.out.println("CETESTE BRE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		String filename = fileS;

		int iread = 0;
		@SuppressWarnings("unused")
		int lnr = 0;// data number
		@SuppressWarnings("unused")
		int lnrr = 0;// line number
		char lineSep = '\n';
		boolean channelB = true;// read channel

		boolean haveData = false;
		
		FileInputStream in = null;
		try {
			//FileInputStream in = new FileInputStream(filename);
			in = new FileInputStream(filename);

			while ((iread = in.read()) != -1) {
				if (!Character.isWhitespace((char) iread)) {
					stringBuffer.append((char) iread);
					haveData = true;
				} else {
					if (haveData)// we have data
					{
						haveData = false;// reset
						String s = stringBuffer.toString();
						// testing data:
						try {
							Convertor.stringToDouble(s);
						} catch (Exception ee) {
							ee.printStackTrace();
							String title = resources
									.getString("dialog.spectrum.title");
							String message = resources
									.getString("dialog.spectrum.message") + s;
							JOptionPane.showMessageDialog(null, message, title,
									JOptionPane.ERROR_MESSAGE);
							channelV = null;
							pulsesV = null;
							return;
						}
						// end testing data
						if (channelB) {
							channelV.addElement(s);
							channelB = false;// next read pulses
						} else {
							pulsesV.addElement(s);
							channelB = true;// next read channel
						}

						lnr++;
					}// have data

					if ((char) iread == lineSep)
						lnrr++;

					// Finally empty the buffer!
					int nn = stringBuffer.capacity();
					stringBuffer.delete(0, nn);// cleanUp
					stringBuffer.trimToSize();// 0 size!
				}// else
			}// main while
			//in.close();
		}// try
		catch (Exception exc) {
			exc.printStackTrace();
		}finally {
	        if( null != in ) {
	            try 
	            {
	                in.close();
	            } catch(Exception ex) {
	                // log or fail if you like
	            }
	        }
	    }
	}

	// empty not null chart
	/**
	 * Create an empty chart
	 * @return the chart
	 */
	private JFreeChart createEmptyChart() {
		// construct an empty XY series
		XYSeries series = new XYSeries(resources.getString("chart.sample.NAME"));
		XYSeries bkgseries = new XYSeries(resources.getString("chart.bkg.NAME"));
		// empty series!
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection bkgdata = new XYSeriesCollection(bkgseries);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("chart.x.Channel"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(resources.getString("chart.y.Pulses"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);

		plot.setDataset(1, bkgdata);
		XYItemRenderer renderer0 = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		renderer0.setSeriesPaint(0, Color.BLUE);
		plot.setRenderer(1, renderer0);

		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(resources.getString("chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(bkgColor);
		return chart;
	}

	// chart
	/**
	 * Charts are properly set here.
	 * @return the lin-view and channels on x-axis chart
	 */
	private JFreeChart getGammaChart() {

		if (channelV == null)
			return null;

		int n = channelV.size();
		channelWarningB = false;
		// constructing the NEW inner variable along with the graph
		channelI = new double[n];
		pulsesD = new double[n];
		keVD = new double[n];
		bkgpulsesD = new double[n];
		globalCounts = 0.0;
		globalBkgCounts = 0.0;
		// construct XY series for sample and bkg in channel domain!
		// Note: we may want to add a new series for direct display the keV
		// domain
		// as new axis (on top..multiple axis) but:
		// 1. It is redundant..we want simplicity
		// 2. Many bugs! new series and new axis are independent..it is hard to
		// assure an exact overlap
		// with series in channel Domain!! Basicly we have ONLY TWO series:
		// sample and BKG (of course
		// ROI but ROIs are handled later!
		XYSeries series = new XYSeries(resources.getString("chart.sample.NAME"));
		XYSeries bkgseries = new XYSeries(resources.getString("chart.bkg.NAME"));
		
		XYSeries seriesen = new XYSeries(resources.getString("chart.sample.NAME"));
		XYSeries bkgseriesen = new XYSeries(resources.getString("chart.bkg.NAME"));
		
		XYSeries seriesln = new XYSeries(resources.getString("chart.sample.NAME"));
		XYSeries bkgseriesln = new XYSeries(resources.getString("chart.bkg.NAME"));
		
		XYSeries serieslnen = new XYSeries(resources.getString("chart.sample.NAME"));
		XYSeries bkgserieslnen = new XYSeries(resources.getString("chart.bkg.NAME"));
		
		XYSeries seriessqrt = new XYSeries(resources.getString("chart.sample.NAME"));
		XYSeries bkgseriessqrt = new XYSeries(resources.getString("chart.bkg.NAME"));
		
		XYSeries seriessqrten = new XYSeries(resources.getString("chart.sample.NAME"));
		XYSeries bkgseriessqrten = new XYSeries(resources.getString("chart.bkg.NAME"));

		double currentChannel = 0.0;
		double lnd = 0.0;
		double sqrtd = 0.0;
		for (int j = 0; j < n; j++) {
			channelI[j] = Math.floor(Convertor.stringToDouble((String) channelV
					.elementAt(j)));
			pulsesD[j] = Convertor
					.stringToDouble((String) pulsesV.elementAt(j));

			globalCounts = globalCounts
					+ Convertor.stringToDouble((String) pulsesV.elementAt(j));

			bkgpulsesD[j] = 0.0;// /0 initialize

			if (j > 0) {
				if (currentChannel >= channelI[j]) {
					channelWarningB = true;
				}
			}
			currentChannel = channelI[j];

			series.add(channelI[j], pulsesD[j]);
			bkgseries.add(channelI[j], bkgpulsesD[j]);// 0 initialize!!!
			
			if (pulsesD[j]!=0){
				lnd = Math.log(pulsesD[j]);
				sqrtd = Math.sqrt(pulsesD[j]);
			} else {
				lnd = 0.0;
				sqrtd = 0.0;
			}
			
			seriesln.add(channelI[j], lnd);
			bkgseriesln.add(channelI[j], bkgpulsesD[j]);// 0 initialize!!!
			
			seriessqrt.add(channelI[j], sqrtd);
			bkgseriessqrt.add(channelI[j], bkgpulsesD[j]);// 0 initialize!!!
			
			//----------------
			keVD[j] = getKevFromChannel(channelI[j]);
			seriesen.add(keVD[j], pulsesD[j]);
			bkgseriesen.add(keVD[j], bkgpulsesD[j]);// 0 initialize!!!
			
			serieslnen.add(keVD[j], lnd);
			bkgserieslnen.add(keVD[j], bkgpulsesD[j]);// 0 initialize!!!
			
			seriessqrten.add(keVD[j], sqrtd);
			bkgseriessqrten.add(keVD[j], bkgpulsesD[j]);// 0 initialize!!!
		}
		// finnally get min and max channel and dispose vectors
		MINCHANNEL = channelI[0];
		MAXCHANNEL = channelI[n - 1];
		MINBOUND = MINCHANNEL;
		MAXBOUND = MAXCHANNEL;

		channelV = null;
		pulsesV = null;
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection bkgdata = new XYSeriesCollection(bkgseries);
		//-------
		XYSeriesCollection dataen = new XYSeriesCollection(seriesen);
		XYSeriesCollection bkgdataen = new XYSeriesCollection(bkgseriesen);
		
		XYSeriesCollection dataln = new XYSeriesCollection(seriesln);
		XYSeriesCollection bkgdataln = new XYSeriesCollection(bkgseriesln);
		XYSeriesCollection datalnen = new XYSeriesCollection(serieslnen);
		XYSeriesCollection bkgdatalnen = new XYSeriesCollection(bkgserieslnen);
		
		XYSeriesCollection datasqrt = new XYSeriesCollection(seriessqrt);
		XYSeriesCollection bkgdatasqrt = new XYSeriesCollection(bkgseriessqrt);
		XYSeriesCollection datasqrten = new XYSeriesCollection(seriessqrten);
		XYSeriesCollection bkgdatasqrten = new XYSeriesCollection(bkgseriessqrten);
		/*
		 * ((NumberAxis) plot.getDomainAxis()).setLabel(resources
						.getString("chart.x.keV"));
				MINBOUND = getKevFromChannel(MINCHANNEL);
				MAXBOUND = getKevFromChannel(MAXCHANNEL);
		 */
		//-------
		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("chart.x.Channel"));// "Channel");
		//------------
		NumberAxis xAxisen = new NumberAxis(
				resources.getString("chart.x.keV"));// "Channel");
		xAxis.setAutoRangeIncludesZero(false);
		//--------------
		xAxisen.setAutoRangeIncludesZero(false);
		
		NumberAxis xAxisln = new NumberAxis(
				resources.getString("chart.x.Channel"));// "Channel");
		xAxisln.setAutoRangeIncludesZero(false);
		NumberAxis xAxissqrt = new NumberAxis(
				resources.getString("chart.x.Channel"));// "Channel");
		xAxissqrt.setAutoRangeIncludesZero(false);
		
		NumberAxis xAxislnen = new NumberAxis(
				resources.getString("chart.x.keV"));// "Channel");
		xAxislnen.setAutoRangeIncludesZero(false);
		NumberAxis xAxissqrten = new NumberAxis(
				resources.getString("chart.x.keV"));// "Channel");
		xAxissqrten.setAutoRangeIncludesZero(false);
		
		NumberAxis yAxis = new NumberAxis(resources.getString("chart.y.Pulses"));// "Pulses");
		//even it seems pointles, we must construct new axis object for each plot:
		NumberAxis yAxisen = new NumberAxis(resources.getString("chart.y.Pulses"));// "Pulses");
		
		NumberAxis yAxisln = new NumberAxis(resources.getString("chart.y.Pulses.ln"));
		NumberAxis yAxislnen = new NumberAxis(resources.getString("chart.y.Pulses.ln"));
		
		NumberAxis yAxissqrt = new NumberAxis(resources.getString("chart.y.Pulses.sqrt"));
		NumberAxis yAxissqrten = new NumberAxis(resources.getString("chart.y.Pulses.sqrt"));
		
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
		//-----------
		XYPlot ploten = new XYPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		ploten.setOrientation(PlotOrientation.VERTICAL);
		ploten.setBackgroundPaint(Color.lightGray);
		ploten.setDomainGridlinePaint(Color.white);
		ploten.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		ploten.setDomainPannable(true);
		ploten.setRangePannable(true);
		// 1st axis
		ploten.setDomainAxis(0, xAxisen);// the axis index;axis
		ploten.setRangeAxis(0, yAxisen);
		
		XYPlot plotln = new XYPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		plotln.setOrientation(PlotOrientation.VERTICAL);
		plotln.setBackgroundPaint(Color.lightGray);
		plotln.setDomainGridlinePaint(Color.white);
		plotln.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plotln.setDomainPannable(true);
		plotln.setRangePannable(true);
		// 1st axis
		plotln.setDomainAxis(0, xAxisln);// the axis index;axis
		plotln.setRangeAxis(0, yAxisln);
		XYPlot plotlnen = new XYPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		plotlnen.setOrientation(PlotOrientation.VERTICAL);
		plotlnen.setBackgroundPaint(Color.lightGray);
		plotlnen.setDomainGridlinePaint(Color.white);
		plotlnen.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plotlnen.setDomainPannable(true);
		plotlnen.setRangePannable(true);
		// 1st axis
		plotlnen.setDomainAxis(0, xAxislnen);// the axis index;axis
		plotlnen.setRangeAxis(0, yAxislnen);
		
		XYPlot plotsqrt = new XYPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		plotsqrt.setOrientation(PlotOrientation.VERTICAL);
		plotsqrt.setBackgroundPaint(Color.lightGray);
		plotsqrt.setDomainGridlinePaint(Color.white);
		plotsqrt.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plotsqrt.setDomainPannable(true);
		plotsqrt.setRangePannable(true);
		// 1st axis
		plotsqrt.setDomainAxis(0, xAxissqrt);// the axis index;axis
		plotsqrt.setRangeAxis(0, yAxissqrt);
		XYPlot plotsqrten = new XYPlot();//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		plotsqrten.setOrientation(PlotOrientation.VERTICAL);
		plotsqrten.setBackgroundPaint(Color.lightGray);
		plotsqrten.setDomainGridlinePaint(Color.white);
		plotsqrten.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plotsqrten.setDomainPannable(true);
		plotsqrten.setRangePannable(true);
		// 1st axis
		plotsqrten.setDomainAxis(0, xAxissqrten);// the axis index;axis
		plotsqrten.setRangeAxis(0, yAxissqrten);
		
		
		// DATASET AND RENDERER:
		idataset = 0;// channel,pulses main spectrum
		plot.setDataset(idataset, data);// idataset=0!
		//---------------------
		ploten.setDataset(idataset, dataen);// idataset=0!//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		
		plotln.setDataset(idataset, dataln);plotlnen.setDataset(idataset, datalnen);
		plotsqrt.setDataset(idataset, datasqrt);plotsqrten.setDataset(idataset, datasqrten);
		
		
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		plot.setRenderer(idataset, renderer);
		//----------
		ploten.setRenderer(idataset, renderer);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		plotln.setRenderer(idataset, renderer);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		plotlnen.setRenderer(idataset, renderer);
		plotsqrt.setRenderer(idataset, renderer);
		plotsqrten.setRenderer(idataset, renderer);

		idataset = 1;// channel,pulses BKG
		plot.setDataset(idataset, bkgdata);
		//-------------------
		ploten.setDataset(idataset, bkgdataen);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		
		plotln.setDataset(idataset, bkgdataln);plotlnen.setDataset(idataset, bkgdatalnen);
		plotsqrt.setDataset(idataset, bkgdatasqrt);plotsqrten.setDataset(idataset, bkgdatasqrten);
		
		XYItemRenderer renderer0 = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		renderer0.setSeriesPaint(0, Color.BLUE);
		plot.setRenderer(idataset, renderer0);
		//------------
		ploten.setRenderer(idataset, renderer0);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		plotln.setRenderer(idataset, renderer0);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		plotlnen.setRenderer(idataset, renderer0);
		plotsqrt.setRenderer(idataset, renderer0);
		plotsqrten.setRenderer(idataset, renderer0);
		
		idataset = 2;

		//JFreeChart chart = new JFreeChart(resources.getString("chart.NAME"),
		chart = new JFreeChart(resources.getString("chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(bkgColor);
		//==================
		charten = new JFreeChart(resources.getString("chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, ploten, true);
		charten.setBackgroundPaint(bkgColor);
		
		chartln = new JFreeChart(resources.getString("chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plotln, true);
		chartln.setBackgroundPaint(bkgColor);
		chartlnen = new JFreeChart(resources.getString("chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plotlnen, true);
		chartlnen.setBackgroundPaint(bkgColor);
		
		chartsqrt = new JFreeChart(resources.getString("chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plotsqrt, true);
		chartsqrt.setBackgroundPaint(bkgColor);
		chartsqrten = new JFreeChart(resources.getString("chart.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plotsqrten, true);
		chartsqrten.setBackgroundPaint(bkgColor);

		if (channelWarningB) {
			statusL.setText(resources.getString("status.spectrum.warning"));
		}
		
		//----------
		//this.chart = chart;

		return this.chart;//chart;
	}

	/**
	 * Internally used. Adjust channel (in reality, the channels are integers)
	 * @param x x
	 * @return the result
	 */
	private double adjustChannel(double x) {

		double y = Math.floor(x);
		if (y > MAXCHANNEL)
			y = MAXCHANNEL;
		if (y < MINCHANNEL)
			y = MINCHANNEL;
		return y;
	}

	/**
	 * Go to peak search
	 */
	private void peakSearch() {
		if (channelI == null) {
			String title = resources.getString("number.error.save.title2");
			String message = resources.getString("number.error.noSpectrum2");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		deleteAllROI();

		boolean nulneg = false;
		int day, month, year = 0;

		spectrumLiveTime = 0.0;
		quantity = 1.0;
		spectrumName = idSpectrumTf.getText();
		quantityUnit = quantityUnitTf.getText();
		// 2. Spectrum data: name, date, liveTime...
		try {
			spectrumLiveTime = Convertor.stringToDouble(spectrumLiveTimeTf
					.getText());
			if (spectrumLiveTime <= 0)
				nulneg = true;

			day = Convertor.stringToInt((String) dayCb.getSelectedItem());
			month = Convertor.stringToInt((String) monthCb.getSelectedItem());
			year = Convertor.stringToInt(yearTf.getText());
			if (year < 0 || month < 0 || day < 0)
				nulneg = true;

			String str = quantityTf.getText();
			if (str != "")
				quantity = Convertor.stringToDouble(str);
			if (quantity <= 0)
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
		//TimeUtilities.setDate(day, month, year);
		//measurementDate = TimeUtilities.formatDate();
		TimeUtilities tu = new TimeUtilities(day, month, year);
		measurementDate = tu.formatDate();
		
		new PeakSearchFrame(this);
	}

	/**
	 * If we have ROI, go try to automatically identify them!
	 */
	private void peakIdentify() {
		if (roiV == null)
			return;
		if (roiV.size() == 0) {
			return;
		}

		new PeakIdentifyFrame(this);
	}

	/**
	 * Perform sample calculation
	 */
	private void sampleCalculation() {
		//if (roiV.size() != 0)
		//quantity must be known:
				boolean nulneg = false;
				quantity = 1.0;
				//spectrumName = idSpectrumTf.getText();
				quantityUnit = quantityUnitTf.getText();
				// 2. Spectrum data: name, date, liveTime...
				try {
					String str = quantityTf.getText();
					if (str != "")
						quantity = Convertor.stringToDouble(str);
					if (quantity <= 0)
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
				//=================
		if(!emptyChartB && spectrumLiveTime!=0.0){
			retrieveHeaderInfo();
			new GammaResultsFrame(this, GammaResultsFrame.IMODE_SAMPLEDATA);
		}
	}
	
	/**
	 * Collect data for header information when sample calculations are performed.
	 */
	private void retrieveHeaderInfo(){
		String gammaEnergyCalibrationTable = resources.getString("main.db.enCalib");
		String gammaFWHMCalibrationTable = resources.getString("main.db.fwhmCalib");
		String gammaEfficiencyCalibrationTable = resources.getString("main.db.effCalib");
		try {
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str = "select * from " + gammaEnergyCalibrationTable
				+ " where USE = 'Yes'";
			ResultSet res = s.executeQuery(str);
			res.next();// 1 single row!!no while needed!
			energyCalName = res.getString(2);//res.getString(3);// column index..start from 1 is
			// 3="Use"
		
			if (res != null)
				res.close();
			if (s != null)
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		try {
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str = "select * from " + gammaFWHMCalibrationTable
					+ " where USE = 'Yes'";
			ResultSet res = s.executeQuery(str);
			res.next();// 1 single row!!no while needed!
			fwhmCalName = res.getString(2);//res.getString(3);
			
			if (res != null)
				res.close();
			if (s != null)
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		try {
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str = "select * from " + gammaEfficiencyCalibrationTable
					+ " where USE = 'Yes'";
			ResultSet res = s.executeQuery(str);
			res.next();// 1 single row!!no while needed!
			effCalName = res.getString(2);//res.getString(3);
			
			if (res != null)
				res.close();
			if (s != null)
				s.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	/**
	 * Internally used. Get data from a matrix object.
	 * @param a the matrix object
	 * @param row data located at this row
	 * @param col data located at this col
	 * @return the result
	 */
	private Object getValueAt(Vector<Object> a, int row, int col) {
		@SuppressWarnings("unchecked")
		Vector<Object> v = (Vector<Object>) a.elementAt(row);
		return v.elementAt(col);
	}

	/**
	 * Go to energy-FWHM calibration.
	 */
	private void enFWHMCalibration() {
		// ..Allow in order to set calibrations!
		// Sort data
		int ncols = 2;// centroid, fwhm
		int nrows = roiV.size();
		Vector<String> rowV = new Vector<String>();
		Vector<Object> dataV = new Vector<Object>();

		for (int i = 0; i < nrows; i++) {
			rowV = new Vector<String>();
			rowV.addElement(Convertor.doubleToString(roiV.elementAt(i)
					.getCentroidChannel()));
			rowV.addElement(Convertor.doubleToString(roiV.elementAt(i)
					.getFWHMChannel()));
			dataV.addElement(rowV);
		}

		Sort.qSort(dataV, ncols, 0);// after 1 st element which is centroid!!
		// end sort
		String[] columnNames = (String[]) resources
				.getObject("energy.fwhm.calibration.columns");
		int n = roiV.size();
		int m = columnNames.length;
		String[][] dataMatrix = new String[n][m];
		String[][] dataMatrixAccurate = new String[n][m];
		for (int i = 0; i < n; i++) {
			dataMatrix[i][0] = Convertor.intToString(i + 1);// id
			dataMatrixAccurate[i][0] = Convertor.intToString(i + 1);// id

			String ss = getValueAt(dataV, i, 0).toString();// centroid
			double d = Convertor.stringToDouble(ss);
			dataMatrix[i][1] = Convertor.formatNumber(d, 2);
			dataMatrixAccurate[i][1] = Convertor.doubleToString(d);

			ss = getValueAt(dataV, i, 1).toString();// fwhm
			d = Convertor.stringToDouble(ss);
			dataMatrix[i][5] = Convertor.formatNumber(d, 2);//
			dataMatrixAccurate[i][5] = Convertor.doubleToString(d);

			dataMatrix[i][8] = resources.getString("difference.no");
			dataMatrixAccurate[i][8] = resources.getString("difference.no");
		}

		new GammaEnergyFWHMCalibrationFrame(this, dataMatrix, columnNames,
				dataMatrixAccurate);
	}

	/**
	 * Go to efficiency calibration
	 */
	private void effCalibration() {
		// ...allow in order to set calibrations!
		// A valid ROI: if setRoi is true and getNuclide!=NoName!!
		String[] columnNames = (String[]) resources
				.getObject("eff.calibration.nuclides.columns");
		int n = roiV.size();
		int m = columnNames.length;
		Vector<String> nucV = new Vector<String>();

		boolean foundB = false;
		for (int i = 0; i < n; i++) {

			String ss = roiV.elementAt(i).getNuclide();
			for (int j = 0; j < nucV.size(); j++) {
				if (ss.equals(nucV.elementAt(j))) {
					foundB = true;
					break;
				} else {
					foundB = false;
				}
			}
			if (!foundB) {
				nucV.addElement(ss);
			}
		}

		String[][] dataMatrix = new String[nucV.size()][m];
		for (int i = 0; i < nucV.size(); i++) {
			dataMatrix[i][0] = Convertor.intToString(i + 1);// id
			dataMatrix[i][1] = nucV.elementAt(i);
		}

		GammaEfficiencyCalibrationFrame.sday = (String) dayCb.getSelectedItem();
		GammaEfficiencyCalibrationFrame.smonth = (String) monthCb
				.getSelectedItem();
		GammaEfficiencyCalibrationFrame.syear = yearTf.getText();
		new GammaEfficiencyCalibrationFrame(this, dataMatrix, columnNames);
	}

	/**
	 * Report results
	 */
	private void report() {
		//quantity must be known:
		/*boolean nulneg = false;
		quantity = 1.0;
		//spectrumName = idSpectrumTf.getText();
		//quantityUnit = quantityUnitTf.getText();
		// 2. Spectrum data: name, date, liveTime...
		try {
			String str = quantityTf.getText();
			if (str != "")
				quantity = Convertor.stringToDouble(str);
			if (quantity <= 0)
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
		}*/
		//=================not needed for roi!!!
		if (roiV.size() != 0)
			new GammaResultsFrame(this, GammaResultsFrame.IMODE_ROISDATA);
	}

	/**
	 * Go to set ROI.
	 */
	private void setROI() {
		if (roiV == null)
			return;
		if (roiV.size() == 0) {
			return;
		}

		String idRoiS = (String) roiCb.getSelectedItem();
		int idRoi = Convertor.stringToInt(idRoiS) - 1;
		GammaRoi gr = roiV.elementAt(idRoi);

		if (naiRb.isSelected()) {
			iNet = NET_CALCULATION_NAI;
		} else if (geRb.isSelected()) {
			iNet = NET_CALCULATION_GE;
		} else if (gaussRb.isSelected()) {
			iNet = NET_CALCULATION_GAUSSIAN;
		}
		gr.setNetCalculationMethod(iNet);

		new GammaRoiSetFrame(this, gr, idRoi);
		// ======================TEST=======================
		// first perform some basic calculation
		// StatsUtil.confidenceLevel=0.99;
		// double degreesOfFreedom=0.0;
		// double t=StatsUtil.getStudentFactor(degreesOfFreedom);
		// System.out.println("t= "+t+" fail? "+StatsUtil.failB);

		// StatsUtil.confidenceLevel=0.95;
		// degreesOfFreedom=10.0;
		// t=StatsUtil.getStudentFactor(degreesOfFreedom);
		// System.out.println("t= "+t+" fail? "+StatsUtil.failB);

		// StatsUtil.confidenceLevel=0.90;
		// degreesOfFreedom=10.0;
		// t=StatsUtil.getStudentFactor(degreesOfFreedom);
		// System.out.println("t= "+t+" fail? "+StatsUtil.failB);
		// ----------OK!!
		// StatsUtil.confidenceLevel=0.99;
		// degreesOfFreedom=5.0;
		// t=StatsUtil.getStudentFactorForMeanComparison(degreesOfFreedom);
		// System.out.println("t for mean comparison= "+t+" fail? "+StatsUtil.failB);

		// StatsUtil.confidenceLevel=0.95;
		// degreesOfFreedom=5.0;
		// t=StatsUtil.getStudentFactorForMeanComparison(degreesOfFreedom);
		// System.out.println("t for mean comparison= "+t+" fail? "+StatsUtil.failB);

		// StatsUtil.confidenceLevel=0.90;
		// degreesOfFreedom=5.0;
		// t=StatsUtil.getStudentFactorForMeanComparison(degreesOfFreedom);
		// System.out.println("t for mean comparison= "+t+" fail? "+StatsUtil.failB);
		// -----------------
		// StatsUtil.confidenceLevel=0.99;
		// t=StatsUtil.getFisherFactor(12,12);
		// System.out.println("f= "+t+" fail? "+StatsUtil.failB);

		// StatsUtil.confidenceLevel=0.95;
		// t=StatsUtil.getFisherFactor(12,12);
		// System.out.println("f= "+t+" fail? "+StatsUtil.failB);

		// StatsUtil.confidenceLevel=0.90;
		// t=StatsUtil.getFisherFactor(12,12);
		// System.out.println("f= "+t+" fail? "+StatsUtil.failB);
		// =============OK==================
		// StatsUtil.confidenceLevel=0.95;
		// StatsUtil.ttest(12, 13, 0.15, 0.15);
		// System.out.println("different= "+StatsUtil.differentB+" fail? "+StatsUtil.failB);
		// StatsUtil.confidenceLevel=0.95;
		// StatsUtil.ttest_default_unc(12, 13, 0.95, 0.0,1000.8,1000.8);
		// System.out.println("different= "+StatsUtil.differentB+" fail? "+StatsUtil.failB);
		// ok with unc=0!!!!!!!

	}

	/**
	 * View ROI in detail.
	 */
	private void viewROI() {
		if (roiV == null)
			return;
		if (roiV.size() == 0) {
			return;
		}

		String idRoiS = (String) roiCb.getSelectedItem();
		int idRoi = Convertor.stringToInt(idRoiS) - 1;
		GammaRoi gr = roiV.elementAt(idRoi);

		if (!gr.isRoiSet()) {
			gr.performBasicComputation();
		}
		new GammaViewRoiFrame(this, gr);
	}

	/**
	 * Go to database and select a spectrum as background.
	 */
	private void openBKG() {
		if (channelI == null) {
			String title = resources
					.getString("number.error.useBKG.noSpectrum.title");
			String message = resources
					.getString("number.error.useBKG.noSpectrum");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		boolean nulneg = false;

		spectrumLiveTime = 0.0;
		// 2. Spectrum data: liveTime...
		try {
			spectrumLiveTime = Convertor.stringToDouble(spectrumLiveTimeTf
					.getText());
			if (spectrumLiveTime <= 0)
				nulneg = true;
		} catch (Exception e) {
			String title = resources.getString("number.error.useBKG.title");
			String message = resources.getString("number.error.useBKG");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
			return;
		}
		if (nulneg) {
			String title = resources.getString("number.error.useBKG.title");
			String message = resources.getString("number.error.useBKG");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		GammaSpectraDatabaseFrame
				.setDisplayMode(GammaSpectraDatabaseFrame.BKG_DISPLAY_MODE);
		GammaSpectraDatabaseFrame
				.setRunMode(GammaSpectraDatabaseFrame.BKG_SET_MODE);

		new GammaSpectraDatabaseFrame(this);
	}

	/**
	 * Open database in order to load a spectrum
	 */
	private void openDB() {
		GammaSpectraDatabaseFrame
				.setDisplayMode(GammaSpectraDatabaseFrame.BKG_DISPLAY_MODE);
		GammaSpectraDatabaseFrame
				.setRunMode(GammaSpectraDatabaseFrame.LOAD_SET_MODE);

		new GammaSpectraDatabaseFrame(this);
	}

	/**
	 * Go to database in order to save current spectrum
	 */
	private void saveInDB() {
		// some check first
		// 1. Spectrum channels and pulses
		if (channelI == null) {
			String title = resources.getString("number.error.save.title");
			String message = resources.getString("number.error.noSpectrum");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		boolean nulneg = false;
		int day, month, year = 0;

		spectrumLiveTime = 0.0;
		quantity = 1.0;
		spectrumName = idSpectrumTf.getText();
		quantityUnit = quantityUnitTf.getText();
		// 2. Spectrum data: name, date, liveTime...
		try {
			spectrumLiveTime = Convertor.stringToDouble(spectrumLiveTimeTf
					.getText());
			if (spectrumLiveTime <= 0)
				nulneg = true;

			day = Convertor.stringToInt((String) dayCb.getSelectedItem());
			month = Convertor.stringToInt((String) monthCb.getSelectedItem());
			year = Convertor.stringToInt(yearTf.getText());
			if (year < 0 || month < 0 || day < 0)
				nulneg = true;

			String str = quantityTf.getText();
			if (str != "")
				quantity = Convertor.stringToDouble(str);
			if (quantity <= 0)
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
		//TimeUtilities.setDate(day, month, year);
		//measurementDate = TimeUtilities.formatDate();
		
		TimeUtilities tu = new TimeUtilities(day, month, year);
		measurementDate = tu.formatDate();
		// 3. Setting the save mode!
		GammaSpectraDatabaseFrame
				.setDisplayMode(GammaSpectraDatabaseFrame.BKG_DISPLAY_MODE);
		GammaSpectraDatabaseFrame
				.setRunMode(GammaSpectraDatabaseFrame.SAVE_SET_MODE);
		// 4. now we are ready to save:
		new GammaSpectraDatabaseFrame(this);
	}

	/**
	 * Retrieve calibration data from database.
	 */
	private void retrieveSettingsFromDatabase() {

		try {
			//String gammaDB = resources.getString("main.db");
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = gammaDB;
			//opens = opens + file_sep + dbName;

			// 1.Energy calibration!

			String tableS = resources.getString("main.db.enCalib");

			String s = "select * from " + tableS + " where USE = " + "'"
					+ resources.getString("difference.yes") + "'";// "YES"

			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			if (DatabaseAgent.getRowCount() > 0) {
				// we have data
				// row = 0 due to the statement WHERE!!
				en_a3 = (Double) DatabaseAgent.getValueAt(0, 3);// a3!!
				en_a2 = (Double) DatabaseAgent.getValueAt(0, 4);// a2!!
				en_a1 = (Double) DatabaseAgent.getValueAt(0, 5);// a1!!
				en_a0 = (Double) DatabaseAgent.getValueAt(0, 6);// a0!!

			} else {
				en_a3 = 0.0;// a3!!
				en_a2 = 0.0;// a2!!
				en_a1 = 2.0;// a1!!
				en_a0 = 30.0;// a0!!
			}

			// 2.FWHM calibration!

			tableS = resources.getString("main.db.fwhmCalib");
			s = "select * from " + tableS + " where USE = " + "'"
					+ resources.getString("difference.yes") + "'";// "YES"

			//con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			if (DatabaseAgent.getRowCount() > 0) {
				// we have data
				fwhm_a3 = (Double) DatabaseAgent.getValueAt(0, 3);// a3!!
				fwhm_a2 = (Double) DatabaseAgent.getValueAt(0, 4);// a2!!
				fwhm_a1 = (Double) DatabaseAgent.getValueAt(0, 5);// a1!!
				fwhm_a0 = (Double) DatabaseAgent.getValueAt(0, 6);// a0!!
				fwhm_overallProcentualError = (Double) DatabaseAgent.getValueAt(
						0, 7);
			} else {
				fwhm_a3 = 0.0;// a3!!
				fwhm_a2 = 0.0;// a2!!
				fwhm_a1 = 0.0;// a1!!
				fwhm_a0 = 1.0;// a0!!
				fwhm_overallProcentualError = 0.0;
			}

			// 3.Efficiency calibration!

			tableS = resources.getString("main.db.effCalib");
			s = "select * from " + tableS + " where USE = " + "'"
					+ resources.getString("difference.yes") + "'";// "YES"

			//con1 = DBConnection.getDerbyConnection(opens, "", "");
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			if (DatabaseAgent.getRowCount() > 0) {
				// we have data
				eff_p1_a4 = (Double) DatabaseAgent.getValueAt(0, 3);// a4!!
				eff_p1_a3 = (Double) DatabaseAgent.getValueAt(0, 4);
				eff_p1_a2 = (Double) DatabaseAgent.getValueAt(0, 5);
				eff_p1_a1 = (Double) DatabaseAgent.getValueAt(0, 6);
				eff_p1_a0 = (Double) DatabaseAgent.getValueAt(0, 7);
				eff_p2_a4 = (Double) DatabaseAgent.getValueAt(0, 8);// a4!!
				eff_p2_a3 = (Double) DatabaseAgent.getValueAt(0, 9);
				eff_p2_a2 = (Double) DatabaseAgent.getValueAt(0, 10);
				eff_p2_a1 = (Double) DatabaseAgent.getValueAt(0, 11);
				eff_p2_a0 = (Double) DatabaseAgent.getValueAt(0, 12);
				eff_crossoverEnergy = (Double) DatabaseAgent.getValueAt(0, 13);
				eff_overallProcentualError = (Double) DatabaseAgent.getValueAt(0,
						14);
			} else {
				eff_p1_a4 = 0.0;
				eff_p1_a3 = 0.0;
				eff_p1_a2 = 0.0;
				eff_p1_a1 = 0.0;
				eff_p1_a0 = 0.0;// exp(0)=1
				eff_p2_a4 = 0.0;
				eff_p2_a3 = 0.0;
				eff_p2_a2 = 0.0;
				eff_p2_a1 = 0.0;
				eff_p2_a0 = 0.0;
				eff_crossoverEnergy = 0.0;
				eff_overallProcentualError = 0.0;
			}
			// transfer calib
			GammaRoi.setCalibrations(
					en_a3,
					en_a2,
					en_a1,
					en_a0,
					// ch_a3,
					// ch_a2,
					// ch_a1,
					// ch_a0,
					fwhm_a3, fwhm_a2, fwhm_a1, fwhm_a0,
					fwhm_overallProcentualError, eff_p1_a4, eff_p1_a3,
					eff_p1_a2, eff_p1_a1, eff_p1_a0, eff_p2_a4, eff_p2_a3,
					eff_p2_a2, eff_p2_a1, eff_p2_a0, eff_crossoverEnergy,
					eff_overallProcentualError);

			GammaEnergyFWHMCalibrationFrame.setCalibrations(en_a3, en_a2,
					en_a1, en_a0,

					fwhm_a3, fwhm_a2, fwhm_a1, fwhm_a0);

			GammaEfficiencyCalibrationFrame.setCalibrations(eff_p1_a4,
					eff_p1_a3, eff_p1_a2, eff_p1_a1, eff_p1_a0, eff_p2_a4,
					eff_p2_a3, eff_p2_a2, eff_p2_a1, eff_p2_a0,
					eff_crossoverEnergy);

			//con1.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
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
		en_a3 = e_a3;
		en_a2 = e_a2;
		en_a1 = e_a1;
		en_a0 = e_a0;
	}

	/**
	 * Set FWHM calibration. This is an order 3 polynomial. 
	 * The function is FWHM = F(ROI centroid channel).
	 * @param f_a3 f_a3
	 * @param f_a2 f_a2
	 * @param f_a1 f_a1
	 * @param f_a0 f_a0
	 * @param f_overallProcentualError f_overallProcentualError
	 */
	public static void setFWHMCalibration(double f_a3, double f_a2,
			double f_a1, double f_a0, double f_overallProcentualError) {
		fwhm_a3 = f_a3;
		fwhm_a2 = f_a2;
		fwhm_a1 = f_a1;
		fwhm_a0 = f_a0;
		fwhm_overallProcentualError = f_overallProcentualError;
	}

	/**
	 * Set efficiency calibration. This is two order 3 polynomials intersected at a 
	 * cross-over energy. The function is Ln(Eff) = F(Ln(En)).
	 * @param e_p1_a4 e_p1_a4
	 * @param e_p1_a3 e_p1_a3
	 * @param e_p1_a2 e_p1_a2
	 * @param e_p1_a1 e_p1_a1
	 * @param e_p1_a0 e_p1_a0
	 * @param e_p2_a4 e_p2_a4
	 * @param e_p2_a3 e_p2_a3
	 * @param e_p2_a2 e_p2_a2
	 * @param e_p2_a1 e_p2_a1
	 * @param e_p2_a0 e_p2_a0
	 * @param e_crossoverEnergy e_crossoverEnergy
	 * @param e_overallProcentualError e_overallProcentualError
	 */
	public static void setEfficiencyCalibration(double e_p1_a4, double e_p1_a3,
			double e_p1_a2, double e_p1_a1, double e_p1_a0, double e_p2_a4,
			double e_p2_a3, double e_p2_a2, double e_p2_a1, double e_p2_a0,
			double e_crossoverEnergy, double e_overallProcentualError) {
		eff_p1_a4 = e_p1_a4;
		eff_p1_a3 = e_p1_a3;
		eff_p1_a2 = e_p1_a2;
		eff_p1_a1 = e_p1_a1;
		eff_p1_a0 = e_p1_a0;
		eff_p2_a4 = e_p2_a4;
		eff_p2_a3 = e_p2_a3;
		eff_p2_a2 = e_p2_a2;
		eff_p2_a1 = e_p2_a1;
		eff_p2_a0 = e_p2_a0;
		eff_crossoverEnergy = e_crossoverEnergy;
		eff_overallProcentualError = e_overallProcentualError;
	}

	/**
	 * Interface method. Get average ambiental background pulses at a channel. 
	 * @param channel channel
	 * @return the result
	 */
	public double getAmbientalBKGPulsesAtChannel(double channel) {
		double result = 0.0;
		double chn = adjustChannel(channel);
		int ich = (new Double(chn)).intValue();// integer
		result = bkgpulsesD[ich];
		
		//now, perform average:
		result=0.0;//reset
		int ioff = (iROI_EDGE_PULSES_AVERAGE_BY-1)/2;//default (3-1)/2=1=>
		//meaning we go up and down the center by ioff.
		//e.g if 3=> i-1,i,i+1; if 7=>(7-1)/2 =3 so i-3,i-2,i-1,i,i+1,i+2,i+3.
		int divideBy=0;
		for (int i = ich-ioff; i<=ich+ioff;i++){
			if (i>=MINCHANNEL && i<=MAXCHANNEL){//to be sure we are within.
				result = result + bkgpulsesD[i];
				divideBy = divideBy +1;
			}
		}
		//result = result/iROI_EDGE_PULSES_AVERAGE_BY;
		result = result/divideBy;
		//System.out.println("ambioent divide: "+divideBy);
		return result;
	}

	/**
	 * Necessary as interface method but this implementation is wrong. 
	 * This is not use by GammaROI.
	 */
	@Deprecated
	public double[] getAmbientalNetAreaAndUnc(double startChannel,
			double endChannel) {
		double[] result = new double[2];
		// ----------------------
		double netBkg = 0.0;
		double errnetBkg = 0.0;
		double errp2 = 0.0;
		double start = adjustChannel(startChannel);
		double end = adjustChannel(endChannel);
		int istart = (new Double(start)).intValue();// integer
		int iend = (new Double(end)).intValue();
		double low = bkgpulsesD[istart];
		double high = bkgpulsesD[iend];
		// always end>start!!
		double p = 0.0;
		for (int i = istart; i <= iend; i++) {
			// note: i and channelI[i] are the same!
			p = (low * end - high * start) / (end - start) + channelI[i]
					* (high - low) / (end - start);// double not int!!

			if (bkgpulsesD[i] > p) {
				// we have ambiental net area pulses=>store it!!
				netBkg = netBkg + bkgpulsesD[i] - p;
				errp2 = ((end - channelI[i]) / (end - start))
						* ((end - channelI[i]) / (end - start)) * low;
				errp2 = errp2 + ((channelI[i] - start) / (end - start))
						* ((channelI[i] - start) / (end - start)) * high;

				errnetBkg = errnetBkg + bkgpulsesD[i] + errp2;
				// c=c1+c2=>errc2=errc12+errc22!! OK!
			}

		}
		// Adjust ambiental BKG error:
		errnetBkg = Math.sqrt(errnetBkg);// sqrt!
		// ----------------
		result[0] = netBkg;
		result[1] = errnetBkg;
		return result;
	}

	/**
	 * Get energy in keV from channel.
	 * @param x the channel
	 * @return the energy
	 */
	private double getKevFromChannel(double x) {
		// in fact, here, x is an integer!
		double y = en_a3 * Math.pow(x, 3) + en_a2 * Math.pow(x, 2) + en_a1
				* Math.pow(x, 1) + en_a0;
		return y;
	}

	/**
	 * Get channel from keV. The inverse of getKevFromChannel.
	 * @param kev kev
	 * @return the result
	 */
	private double getChannelFromKeV(double kev) {

		double x = 0.0;
		int n = 4;
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
			// spread from channel[0]=0 to channel[n] we want:
			// THE MINIMUM POSITIVE SOLUTION!!!!
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
			// between the remaining two positive roots! Well, since
			// channels are spread from channel[0]=0 to channel[n] we want:
			// THE MINIMUM POSITIVE SOLUTION!!!!
			Sorting.sort(dd.length, dd);
			for (int i = 0; i < dd.length; i++) {
				if (dd[i] >= 0.0) {
					x = dd[i];
					break;
				}
			}

		}

		// "channelized!!
		x = Math.floor(x);
		if (x > MAXCHANNEL)
			x = MAXCHANNEL;
		if (x < MINCHANNEL)
			x = MINCHANNEL;
		return x;
	}

	/**
	 * After sensitive tasks are performed, automatically reset all ROIs. Such tasks are 
	 * changing the background or calibration.
	 */
	protected void resetAllROIs() {
		
		//===============the view
		//if (emptyChartB) {
			//channelRb.setSelected(isChannelDisplay);
			//statusL.setText(resources.getString("status.done"));
			//stopThread();
		//	return;
		//}
		if (this.cp != null) {
								
			JFreeChart c = this.cp.getChart();
			if (c != null) {
			//=====================================
				this.cp.setChart(chart);//en);//reset to default view 
				String sname = spectrumName;
				chart.setTitle(sname);
				charten.setTitle(sname);
				chartln.setTitle(sname);
				chartlnen.setTitle(sname);
				chartsqrt.setTitle(sname);
				chartsqrten.setTitle(sname);
			//=============adjust the other charts
						
				XYPlot plot = (XYPlot) charten.getPlot();
				
				XYPlot plotlnen = (XYPlot) chartlnen.getPlot();
				XYPlot plotsqrten = (XYPlot) chartsqrten.getPlot();

				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);
				XYSeriesCollection bkgdata = (XYSeriesCollection) plot
						.getDataset(1);
				XYSeries bkgseries = bkgdata.getSeries(0);
				
				//==========
				XYSeriesCollection datalnen = (XYSeriesCollection) plotlnen
						.getDataset(0);
				XYSeries serieslnen = datalnen.getSeries(0);
				XYSeriesCollection bkgdatalnen = (XYSeriesCollection) plotlnen
						.getDataset(1);
				XYSeries bkgserieslnen = bkgdatalnen.getSeries(0);
				
				XYSeriesCollection datasqrten = (XYSeriesCollection) plotsqrten
						.getDataset(0);
				XYSeries seriessqrten = datasqrten.getSeries(0);
				XYSeriesCollection bkgdatasqrten = (XYSeriesCollection) plotsqrten
						.getDataset(1);
				XYSeries bkgseriessqrten = bkgdatasqrten.getSeries(0);
				//=============

				int n = series.getItemCount();//the same for all charts
				//first clear
				series.clear();
				bkgseries.clear();
				
				serieslnen.clear();
				bkgserieslnen.clear();
				seriessqrten.clear();
				bkgseriessqrten.clear();

				if (roiV.size() > 0) {
					for (int j = 0; j < roiV.size(); j++) {
						XYSeriesCollection dataj = (XYSeriesCollection) plot
								.getDataset(2 + j);
						XYSeries seriesj = dataj.getSeries(0);
						seriesj.clear();
						//==============
						XYSeriesCollection datajlnen = (XYSeriesCollection) plotlnen
								.getDataset(2 + j);
						XYSeries seriesjlnen = datajlnen.getSeries(0);
						seriesjlnen.clear();
						
						XYSeriesCollection datajsqrten = (XYSeriesCollection) plotsqrten
								.getDataset(2 + j);
						XYSeries seriesjsqrten = datajsqrten.getSeries(0);
						seriesjsqrten.clear();
					}
				}
				//-----------------now redo data:
				for (int i = 0; i < n; i++) {
					//for charten
					keVD[i] = getKevFromChannel(channelI[i]);//channelI always available

					series.add(keVD[i], pulsesD[i]);
					bkgseries.add(keVD[i], bkgpulsesD[i]);
					//------------------------
					double lnd =0.0; double bkglnd =0.0;
					double sqrtd =0.0; double bkgsqrtd =0.0;
					if (pulsesD[i]!=0.0){//always >0
						lnd = Math.log(pulsesD[i]);
						sqrtd = Math.sqrt(pulsesD[i]);
					} else {
						lnd=0.0;
						sqrtd=0.0;
					}
					if (bkgpulsesD[i]!=0.0){//always >0
						bkglnd = Math.log(bkgpulsesD[i]);
						bkgsqrtd = Math.sqrt(bkgpulsesD[i]);
					} else {
						bkglnd=0.0;
						bkgsqrtd=0.0;
					}
					
					serieslnen.add(keVD[i], lnd);
					bkgserieslnen.add(keVD[i], bkglnd);
					
					seriessqrten.add(keVD[i], sqrtd);
					bkgseriessqrten.add(keVD[i], bkgsqrtd);
					//-------------------------------------
					if (roiV.size() > 0) {
						for (int j = 0; j < roiV.size(); j++) {
							XYSeriesCollection dataj = (XYSeriesCollection) plot
									.getDataset(2 + j);
							XYSeries seriesj = dataj.getSeries(0);
							
							//------------
							XYSeriesCollection datajlnen = (XYSeriesCollection) plotlnen
									.getDataset(2 + j);
							XYSeries seriesjlnen = datajlnen.getSeries(0);
							
							XYSeriesCollection datajsqrten = (XYSeriesCollection) plotsqrten
									.getDataset(2 + j);
							XYSeries seriesjsqrten = datajsqrten.getSeries(0);
							//---------
							GammaRoi gr = roiV.elementAt(j);
							double start = gr.getStartChannel();
							double end = gr.getEndChannel();
							if ((start <= channelI[i]) && (channelI[i] <= end)){
									seriesj.add(keVD[i], pulsesD[i]);
									
									double lnd1 =0.0; 
									double sqrtd1 =0.0; 
									if (pulsesD[i]!=0.0){//always >0
										lnd1 = Math.log(pulsesD[i]);
										sqrtd1 = Math.sqrt(pulsesD[i]);
									} else {
										lnd1=0.0;
										sqrtd1=0.0;
									}
									seriesjlnen.add(keVD[i], lnd1);
									seriesjsqrten.add(keVD[i], sqrtd1);
									
							}
						}
					}

				}
						//-------------------------------------
				MINBOUND = MINCHANNEL;
				MAXBOUND = MAXCHANNEL;

				isChannelDisplay = true;
				channelRb.setSelected(true);

				//selectRoi();no need..done via channelRb true selected
			
				isLinB = true;
				isLnB = false;
				isSqrtB = false;

				lnItem.setState(isLnB);
				sqrtItem.setState(isSqrtB);
				linItem.setState(isLinB);

				statusL.setText(resources.getString("status.done"));
				//stopThread();
			} else {
				statusL.setText(resources.getString("status.done"));
				//stopThread();
			}
		} else {
			statusL.setText(resources.getString("status.done"));
			//stopThread();
		}
				//===============end view=========================
				
		if (roiV.size() < 1) {
			return;// no ROIs
		}
		//String gammaDB = resources.getString("main.db");
		String gammaNuclidesTable = resources
				.getString("main.db.gammaNuclidesTable");
		String gammaNuclidesDetailsTable = resources
				.getString("main.db.gammaNuclidesDetailsTable");
		String gammaNuclidesCoincidenceTable = resources
				.getString("main.db.gammaNuclidesCoincidenceTable");

		// first perform basic computation
		for (int j = 0; j < roiV.size(); j++) {
			GammaRoi gr = roiV.elementAt(j);
			gr.performBasicComputation();// initialize!
		}

		// try update ROI if possible!
		for (int j = 0; j < roiV.size(); j++) {
			GammaRoi gr = roiV.elementAt(j);
			String nuc = gr.getNuclide();
			// if is not NoName..we have updated ROIs!!!
			try {
				//String datas = resources.getString("data.load");
				//String currentDir = System.getProperty("user.dir");
				//String file_sep = System.getProperty("file.separator");
				//String opens = currentDir + file_sep + datas;
				//String dbName = gammaDB;
				//opens = opens + file_sep + dbName;

				//Connection con1 = DBConnection
				//		.getDerbyConnection(opens, "", "");
				String s = "select * from " + gammaNuclidesTable
						+ " where NUCLIDE = " + "'" + nuc + "'";
				//DBOperation.select(s, con1);
				DatabaseAgent.select(gammadbcon, s);
				
				int ndata = DatabaseAgent.getRowCount();
				if (ndata > 1) {
					// warningS multiple nuclides with same name!!
					// ...must user select!
					String warnings = resources
							.getString("duplicates.database.nuclides");
					statusL.setText(warnings);
					return;
				}
				if (ndata == 0) {
					return;// no updated ROIs!!
				}
				int linkID = (Integer) DatabaseAgent.getValueAt(0, 0);
				// now we have an solo ID..use it!
				s = "select * from " + gammaNuclidesDetailsTable
						+ " where ID = " + linkID + " ORDER BY NRCRT";
				//DBOperation.select(s, con1);
				DatabaseAgent.select(gammadbcon, s);
				
				ndata = DatabaseAgent.getRowCount();
				double[] energies = new double[ndata];
				double[] yields = new double[ndata];
				double[] decayCorr = new double[ndata];
				for (int i = 0; i < ndata; i++) {
					energies[i] = (Double) DatabaseAgent.getValueAt(i, 2);
					yields[i] = (Double) DatabaseAgent.getValueAt(i, 3);
					decayCorr[i] = (Double) DatabaseAgent.getValueAt(i, 6);
				}

				s = "select * from " + gammaNuclidesCoincidenceTable
						+ " where ID = " + linkID + " ORDER BY NRCRT";
				//DBOperation.select(s, con1);
				DatabaseAgent.select(gammadbcon, s);

				ndata = DatabaseAgent.getRowCount();
				double[] energiesCorr = new double[ndata];// coincidence table!
				double[] yieldsCorr = new double[ndata];
				double[] coinCorr = new double[ndata];
				for (int i = 0; i < ndata; i++) {
					energiesCorr[i] = (Double) DatabaseAgent.getValueAt(i, 2);
					yieldsCorr[i] = (Double) DatabaseAgent.getValueAt(i, 3);
					coinCorr[i] = (Double) DatabaseAgent.getValueAt(i, 6);
				}

				gr.updateRoi(energies, yields, decayCorr, energiesCorr,
						yieldsCorr, coinCorr);

				//if (con1 != null)
					//con1.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
	}

	/**
	 * Delete all ROIs
	 */
	protected void deleteAllROIs() {
		if (emptyChartB)
			return;

		if (roiV.size() == 0) {
			return;
		}

		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {
				XYPlot plot = (XYPlot) chart.getPlot();//(XYPlot) c.getPlot();
				XYPlot ploten = (XYPlot) charten.getPlot();//@@@@@@@@
				//XYPlot plot = (XYPlot) c.getPlot();
				XYPlot plotln = (XYPlot) chartln.getPlot();
				XYPlot plotlnen = (XYPlot) chartlnen.getPlot();
				XYPlot plotsqrt = (XYPlot) chartsqrt.getPlot();
				XYPlot plotsqrten = (XYPlot) chartsqrten.getPlot();
				
				if (idataset > 2) {
					for (int i = 2; i <= 2 + roiV.size() - 1; i++) {
						plot.setDataset(i, null);
						plot.setRenderer(i, null);
						
						ploten.setDataset(i, null);
						ploten.setRenderer(i, null);
						
						plotln.setDataset(i, null);
						plotln.setRenderer(i, null);
						plotlnen.setDataset(i, null);
						plotlnen.setRenderer(i, null);
						
						plotsqrt.setDataset(i, null);
						plotsqrt.setRenderer(i, null);
						plotsqrten.setDataset(i, null);
						plotsqrten.setRenderer(i, null);
						
					}
					idataset = 2;
					roiV.removeAllElements();

					roiCb.removeItemListener(this);
					roiCb.removeAllItems();
					roiCb.addItemListener(this);

					plot.clearDomainMarkers();
					plot.clearAnnotations();
					
					ploten.clearDomainMarkers();
					ploten.clearAnnotations();
					
					plotln.clearDomainMarkers();
					plotln.clearAnnotations();
					plotlnen.clearDomainMarkers();
					plotlnen.clearAnnotations();
					
					plotsqrt.clearDomainMarkers();
					plotsqrt.clearAnnotations();
					plotsqrten.clearDomainMarkers();
					plotsqrten.clearAnnotations();
					
					imarker = 0;// reset
					markers[1] = null;// reset
					markers[0] = null;// reset

					roilabel.setText(resources.getString("roi.count")
							+ Convertor.intToString(roiV.size()));

					roiNamelabel.setText(resources.getString("roi.name.label")
							+ "NoName");

				}
			}
		}

	}

	@SuppressWarnings("unchecked")
	/**
	 * Set the ROI and perform basic computation.
	 * @param xstart ROI start channel
	 * @param xend ROI end channel
	 */
	protected void setInitialRoi(double xstart, double xend) {
		if (emptyChartB)
			return;
		if (this.cp != null) {
			JFreeChart c = this.cp.getChart();
			if (c != null) {

				//XYPlot plot = (XYPlot) c.getPlot();
				XYPlot plot = (XYPlot) chart.getPlot();//(XYPlot) c.getPlot();
				XYPlot ploten = (XYPlot) charten.getPlot();//@@@@@@@@
				
				XYPlot plotln = (XYPlot) chartln.getPlot();
				XYPlot plotlnen = (XYPlot) chartlnen.getPlot();
				XYPlot plotsqrt = (XYPlot) chartsqrt.getPlot();
				XYPlot plotsqrten = (XYPlot) chartsqrten.getPlot();

				double dd1 = xstart;// channel
				double dd2 = xend;// channel
				dd1 = adjustChannel(dd1);
				dd2 = adjustChannel(dd2);

				GammaRoi gr = new GammaRoi(dd1, dd2);

				int i1 = (new Double(dd1)).intValue();
				int i2 = (new Double(dd2)).intValue();
				for (int j = i1; j <= i2; j++) {
					gr.addChannel(channelI[j]);
					gr.addPulses(pulsesD[j]);
					gr.addBkgPulses(bkgpulsesD[j]);
				}
				gr.setStartEdgePulses(getAverageContinuumEdges(i1));//pulsesD[i1]);
				gr.setEndEdgePulses(getAverageContinuumEdges(i2));//pulsesD[i2]);
				gr.setLiveTime(spectrumLiveTime);

				if (naiRb.isSelected()) {
					iNet = NET_CALCULATION_NAI;
				} else if (geRb.isSelected()) {
					iNet = NET_CALCULATION_GE;
				} else if (gaussRb.isSelected()) {
					iNet = NET_CALCULATION_GAUSSIAN;
				}
				gr.setNetCalculationMethod(iNet);
				gr.setConfidenceLevel(roiConfidenceLevel);
				gr.performBasicComputation();

				roiV.addElement(gr);

				roiCb.addItem(Convertor.intToString(roiV.size()));
				roiCb.setSelectedItem(Convertor.intToString(roiV.size()));

				double dd1en=getKevFromChannel(dd1);//gr.getStartChannel();
				double dd2en=getKevFromChannel(dd2);
				/*if (!isChannelDisplay) {
					dd1 = getKevFromChannel(dd1);
					dd2 = getKevFromChannel(dd2);
				}*/
				// displaying the ROI
				XYSeries series2 = new XYSeries("roi");// not shown
				XYSeries series2en = new XYSeries("roi");// not shown
				
				XYSeries series2ln = new XYSeries("roi");// not shown
				XYSeries series2lnen = new XYSeries("roi");// not shown
				XYSeries series2sqrt = new XYSeries("roi");// not shown
				XYSeries series2sqrten = new XYSeries("roi");// not shown
				// firstData
				XYSeriesCollection data = (XYSeriesCollection) plot
						.getDataset(0);
				XYSeries series = data.getSeries(0);
				
				//XYSeriesCollection dataen = (XYSeriesCollection) ploten
				//		.getDataset(0);
				//XYSeries seriesen = dataen.getSeries(0);
				
				int n = series.getItemCount();

				for (int j = 0; j < n; j++) {
					Number x = series.getX(j);//comes from channel-pulses chart
					Number y = series.getY(j);
					
					double lnd =0.0; double sqrtd = 0.0;
					if(y.doubleValue()!=0.0){
						lnd = Math.log(y.doubleValue());
						sqrtd = Math.sqrt(y.doubleValue());
					} else{
						lnd = 0.0;
						sqrtd = 0.0;
					}
					/*if(!isChannelDisplay){
						x = seriesen.getX(j);
						y = seriesen.getY(j);
					}*/
					double xx =0.0; ///double yy=0.0;
					double xxen =0.0; //double yyen=0.0;
					/*if(isChannelDisplay){
						xx = x.doubleValue();
						xxen = getKevFromChannel(xx);
					} else{
						//it is kev
						xxen=x.doubleValue();
						xx=getChannelFromKeV(xxen);
					}*/
					xx = x.doubleValue();
					xxen = getKevFromChannel(xx);
					/*
					if ((dd1 <= x.doubleValue()) && (x.doubleValue() <= dd2)) {
						series2.add(x, y);
					}*/
					
					if ((dd1 <= xx) && (xx <= dd2)) {
						series2.add(xx, y);
						series2ln.add(xx, lnd);
						series2sqrt.add(xx, sqrtd);
					}
					if ((dd1en <= xxen) && (xxen <= dd2en)) {
						series2en.add(xxen, y);
						series2lnen.add(xxen, lnd);
						series2sqrten.add(xxen, sqrtd);
					}
				}
				XYSeriesCollection data2 = new XYSeriesCollection(series2);
				plot.setDataset(idataset, data2);// !
				
				XYSeriesCollection data2en = new XYSeriesCollection(series2en);
				ploten.setDataset(idataset, data2en);
				
				//----------
				XYSeriesCollection data2ln = new XYSeriesCollection(series2ln);
				plotln.setDataset(idataset, data2ln);
				XYSeriesCollection data2lnen = new XYSeriesCollection(series2lnen);
				plotlnen.setDataset(idataset, data2lnen);
				
				XYSeriesCollection data2sqrt = new XYSeriesCollection(series2sqrt);
				plotsqrt.setDataset(idataset, data2sqrt);
				XYSeriesCollection data2sqrten = new XYSeriesCollection(series2sqrten);
				plotsqrten.setDataset(idataset, data2sqrten);
				//-------------
				
				XYItemRenderer renderer2 = new XYAreaRenderer();
				renderer2.setSeriesPaint(0, Color.red);
				renderer2.setSeriesVisibleInLegend(0, false);
				plot.setRenderer(idataset, renderer2);

				ploten.setRenderer(idataset, renderer2);
				
				plotln.setRenderer(idataset, renderer2);
				plotlnen.setRenderer(idataset, renderer2);
				plotsqrt.setRenderer(idataset, renderer2);
				plotsqrten.setRenderer(idataset, renderer2);
				
				idataset++;
				// finally set a center marker and perform some cleaning
				// already set by combobox item state change=>select roi!!!

				roilabel.setText(resources.getString("roi.count")
						+ Convertor.intToString(roiV.size()));
			}
		}
	}
	
	/**
	 * Set ROIs by assigning them to a nuclide and perform activity computation. 
	 * @param roisID array of ROIs ID
	 * @param nuclide the nuclide
	 */
	protected void setROIs(int[] roisID, String nuclide ) {
		failPI=false;
		//String gammaDB = resources.getString("main.db");
		String gammaNuclidesTable = resources
				.getString("main.db.gammaNuclidesTable");
		String gammaNuclidesDetailsTable = resources
				.getString("main.db.gammaNuclidesDetailsTable");
		String gammaNuclidesCoincidenceTable = resources
				.getString("main.db.gammaNuclidesCoincidenceTable");
		
		int nrois=roisID.length;
		if (nrois<1) return;//nothing to set!
		// first perform basic computation
		for (int j = 0; j < nrois; j++){
			int idRoiInVector=roisID[j]-1;
			GammaRoi gr = roiV.elementAt(idRoiInVector);
			gr.performBasicComputation();// initialize!
		}

		// try update ROI if possible!
		for (int j = 0; j < nrois; j++){
			int idRoiInVector=roisID[j]-1;
			GammaRoi gr = roiV.elementAt(idRoiInVector);
			String nuc = nuclide;
			// if is not NoName..we have updated ROIs!!!
			try {
				//String datas = resources.getString("data.load");
				//String currentDir = System.getProperty("user.dir");
				//String file_sep = System.getProperty("file.separator");
				//String opens = currentDir + file_sep + datas;
				//String dbName = gammaDB;
				//opens = opens + file_sep + dbName;

				//Connection con1 = DBConnection
						//.getDerbyConnection(opens, "", "");
				String s = "select * from " + gammaNuclidesTable
						+ " where NUCLIDE = " + "'" + nuc + "'";
				//DBOperation.select(s, con1);
				DatabaseAgent.select(gammadbcon, s);
				
				int ndata = DatabaseAgent.getRowCount();
				if (ndata > 1) {
					// warningS multiple nuclides with same name!!
					// ...must user select!
					String warnings = resources
							.getString("duplicates.database.nuclides");
					statusL.setText(warnings);
					failPI=true;
					warningsPI=warnings;
					return;
				}
				if (ndata == 0) {
					return;// no updated ROIs!!
				}
				double atomicMass = (Double) DatabaseAgent.getValueAt(0, 2);
				double halfLife = (Double) DatabaseAgent.getValueAt(0, 3);
				String halfLifeUnits = (String) DatabaseAgent.getValueAt(0, 4);
				gr.setNuclide(nuc);
				gr.setAtomicMass(atomicMass);
				gr.setHalfLife(halfLife);
				gr.setHalfLifeUnits(halfLifeUnits);
				gr.setMdaCalculationMethod(GammaRoi.MDA_CALCULATION_DEFAULT);//default!
				int linkID = (Integer) DatabaseAgent.getValueAt(0, 0);				
				// now we have an solo ID..use it!
				s = "select * from " + gammaNuclidesDetailsTable
						+ " where ID = " + linkID + " ORDER BY NRCRT";
				//DBOperation.select(s, con1);
				DatabaseAgent.select(gammadbcon, s);
				
				ndata = DatabaseAgent.getRowCount();
				double[] energies = new double[ndata];
				double[] yields = new double[ndata];
				double[] decayCorr = new double[ndata];
				for (int i = 0; i < ndata; i++) {
					energies[i] = (Double) DatabaseAgent.getValueAt(i, 2);
					yields[i] = (Double) DatabaseAgent.getValueAt(i, 3);
					decayCorr[i] = (Double) DatabaseAgent.getValueAt(i, 6);
				}

				s = "select * from " + gammaNuclidesCoincidenceTable
						+ " where ID = " + linkID + " ORDER BY NRCRT";
				//DBOperation.select(s, con1);
				DatabaseAgent.select(gammadbcon, s);

				ndata = DatabaseAgent.getRowCount();
				double[] energiesCorr = new double[ndata];// coincidence table!
				double[] yieldsCorr = new double[ndata];
				double[] coinCorr = new double[ndata];
				for (int i = 0; i < ndata; i++) {
					energiesCorr[i] = (Double) DatabaseAgent.getValueAt(i, 2);
					yieldsCorr[i] = (Double) DatabaseAgent.getValueAt(i, 3);
					coinCorr[i] = (Double) DatabaseAgent.getValueAt(i, 6);
				}

				gr.updateRoi(energies, yields, decayCorr, energiesCorr,
						yieldsCorr, coinCorr);
				roiNamelabel.setText(resources.getString("roi.name.label")
						+ gr.getNuclide());
				//if (con1 != null)
					//con1.close();

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}// for after ROIs
	}
	
	/**
	 * Load ROIs from a ROI file.
	 */
	@SuppressWarnings("unchecked")
	private void loadROI(){
		//System.out.println("Load roi...");
		if (emptyChartB)
			return;
		//=====================
		//boolean checkb = checkLiveTime();
		//if (!checkb) {
		//	return;
		//}
		boolean nulneg = false;
		int day, month, year = 0;

		spectrumLiveTime = 0.0;
		quantity = 1.0;
		spectrumName = idSpectrumTf.getText();
		quantityUnit = quantityUnitTf.getText();
		// 2. Spectrum data: name, date, liveTime...
		try {
			spectrumLiveTime = Convertor.stringToDouble(spectrumLiveTimeTf
					.getText());
			if (spectrumLiveTime <= 0)
				nulneg = true;

			day = Convertor.stringToInt((String) dayCb.getSelectedItem());
			month = Convertor.stringToInt((String) monthCb.getSelectedItem());
			year = Convertor.stringToInt(yearTf.getText());
			if (year < 0 || month < 0 || day < 0)
				nulneg = true;

			String str = quantityTf.getText();
			if (str != "")
				quantity = Convertor.stringToDouble(str);
			if (quantity <= 0)
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
		//TimeUtilities.setDate(day, month, year);
		//measurementDate = TimeUtilities.formatDate();
		TimeUtilities tu = new TimeUtilities(day, month, year);
		measurementDate = tu.formatDate();
		//=======================
		
		String ext = resources.getString("roi.load");
		String pct = ".";
		String description = resources.getString("roi.load.description");
		ExampleFileFilter jpgFilter = new ExampleFileFilter(ext, description);
		String datas = resources.getString("data.load");// "Data";
		String filename = "";
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas + file_sep;// + "egs";
		JFileChooser chooser = new JFileChooser(opens);
		chooser.addChoosableFileFilter(jpgFilter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		// -----------------
		char lineSep = '\n';// System.getProperty("line.separator").charAt(0);
		int i = 0;
		int lnr = 0;// line number
		StringBuffer desc = new StringBuffer();
		String line = "";
		// --------------

		int returnVal = chooser.showOpenDialog(this);// parent=this frame
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// filename= chooser.getSelectedFile().toString()+pct+ext;
			filename = chooser.getSelectedFile().toString();
			int fl = filename.length();
			String test = filename.substring(fl - 4);// exstension lookup!!
			String ctest = pct + ext;
			if (test.compareTo(ctest) != 0)
				filename = chooser.getSelectedFile().toString() + pct + ext;
			
			//deleteAllExistingRois
			deleteAllROIs();//roiV set to 0!!!!!
			//---------------------
			try {
				FileInputStream in = new FileInputStream(filename);
				while ((i = in.read()) != -1) {
					if ((char) i != lineSep) {
						desc.append((char) i);
					} else {
						lnr++;
						line = desc.toString();
						String[] result = line.split(" ");
						int inetmethod = Convertor.stringToInt(result[0]);
						double starten = Convertor.stringToDouble(result[1]);
						double startedgeen = Convertor.stringToDouble(result[2]);
						double enden = Convertor.stringToDouble(result[3]);
						double endedgeen = Convertor.stringToDouble(result[4]);
						String nuclide = result[5];
						
						double dd1en=starten;
						double dd2en=enden;
						
						starten = getChannelFromKeV(starten);//CHANNEL
						startedgeen = getChannelFromKeV(startedgeen);//CHANNEL
						enden = getChannelFromKeV(enden);//CHANNEL
						endedgeen = getChannelFromKeV(endedgeen);//CHANNEL
						
						//test
						//String s = Convertor.intToString(inetmethod)+" " +
						//		Convertor.doubleToString(starten)+" " +
						//		Convertor.doubleToString(startedgeen)+" " +
						//		Convertor.doubleToString(enden)+" " +
						//		Convertor.doubleToString(endedgeen)+" "+
						//		nuclide;
						//System.out.println(s);
						//test ok
						
						//The ROI
						GammaRoi gr = new GammaRoi(starten, enden);
						int i1 = (new Double(starten)).intValue();
						int i2 = (new Double(enden)).intValue();
						for (int j = i1; j <= i2; j++) {
							gr.addChannel(channelI[j]);
							gr.addPulses(pulsesD[j]);
							gr.addBkgPulses(bkgpulsesD[j]);
						}
						if ((startedgeen <= starten) && (endedgeen >= enden)) {
							gr.setStartEdgeChannel(startedgeen);
							gr.setEndEdgeChannel(endedgeen);

							i1 = (new Double(startedgeen)).intValue();
							i2 = (new Double(endedgeen)).intValue();
							gr.setStartEdgePulses(getAverageContinuumEdges(i1));//pulsesD[i1]);
							gr.setEndEdgePulses(getAverageContinuumEdges(i2));//pulsesD[i2]);
						}
						gr.setLiveTime(spectrumLiveTime);
						gr.setNetCalculationMethod(inetmethod);
						gr.setConfidenceLevel(roiConfidenceLevel);
						gr.performBasicComputation();
						
						//Now the plot
						XYPlot plot = (XYPlot) chart.getPlot();//(XYPlot) c.getPlot();
						XYPlot ploten = (XYPlot) charten.getPlot();//@@@@@@@@
						
						XYPlot plotln = (XYPlot) chartln.getPlot();
						XYPlot plotlnen = (XYPlot) chartlnen.getPlot();
						XYPlot plotsqrt = (XYPlot) chartsqrt.getPlot();
						XYPlot plotsqrten = (XYPlot) chartsqrten.getPlot();
						
						XYSeries series2 = new XYSeries("roi");// not shown
						XYSeries series2en = new XYSeries("roi");// not shown
						
						XYSeries series2ln = new XYSeries("roi");// not shown
						XYSeries series2lnen = new XYSeries("roi");// not shown
						XYSeries series2sqrt = new XYSeries("roi");// not shown
						XYSeries series2sqrten = new XYSeries("roi");// not shown
						// firstData
						XYSeriesCollection data = (XYSeriesCollection) plot
								.getDataset(0);
						XYSeries series = data.getSeries(0);
						
						int n = series.getItemCount();

						for (int j = 0; j < n; j++) {
							Number x = series.getX(j);//comes from channel-pulses chart
							Number y = series.getY(j);
							
							double lnd =0.0; double sqrtd = 0.0;
							if(y.doubleValue()!=0.0){
								lnd = Math.log(y.doubleValue());
								sqrtd = Math.sqrt(y.doubleValue());
							} else{
								lnd = 0.0;
								sqrtd = 0.0;
							}
							
							double xx =0.0; 
							double xxen =0.0; 
							
							xx = x.doubleValue();
							xxen = getKevFromChannel(xx);
														
							if ((starten <= xx) && (xx <= enden)) {
								series2.add(xx, y);
								series2ln.add(xx, lnd);
								series2sqrt.add(xx, sqrtd);
							}
							if ((dd1en <= xxen) && (xxen <= dd2en)) {
								series2en.add(xxen, y);
								series2lnen.add(xxen, lnd);
								series2sqrten.add(xxen, sqrtd);
							}
						}
						XYSeriesCollection data2 = new XYSeriesCollection(series2);
						plot.setDataset(idataset, data2);// !
						
						XYSeriesCollection data2en = new XYSeriesCollection(series2en);
						ploten.setDataset(idataset, data2en);
						
						//----------
						XYSeriesCollection data2ln = new XYSeriesCollection(series2ln);
						plotln.setDataset(idataset, data2ln);
						XYSeriesCollection data2lnen = new XYSeriesCollection(series2lnen);
						plotlnen.setDataset(idataset, data2lnen);
						
						XYSeriesCollection data2sqrt = new XYSeriesCollection(series2sqrt);
						plotsqrt.setDataset(idataset, data2sqrt);
						XYSeriesCollection data2sqrten = new XYSeriesCollection(series2sqrten);
						plotsqrten.setDataset(idataset, data2sqrten);
						//-------------
						
						XYItemRenderer renderer2 = new XYAreaRenderer();
						renderer2.setSeriesPaint(0, Color.red);
						renderer2.setSeriesVisibleInLegend(0, false);
						plot.setRenderer(idataset, renderer2);

						ploten.setRenderer(idataset, renderer2);
						
						plotln.setRenderer(idataset, renderer2);
						plotlnen.setRenderer(idataset, renderer2);
						plotsqrt.setRenderer(idataset, renderer2);
						plotsqrten.setRenderer(idataset, renderer2);
						
						idataset++;
						//====================================
						
						//=========try to set rois automatically
						trySettingRoi(gr,nuclide);
						//=================
						
						roiV.addElement(gr);

						roiCb.addItem(Convertor.intToString(roiV.size()));

						if (lnr == 1) {
							//adetTf.setText(line);
						}
						
						desc = new StringBuffer();

					}
				}
				//============
				roiCb.setSelectedItem(Convertor.intToString(roiV.size()));
				roilabel.setText(resources.getString("roi.count")
						+ Convertor.intToString(roiV.size()));
				//-----------------
				in.close();
				statusL.setText(resources.getString("status.load") + filename);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}
	
	/**
	 * Try automatically set the ROI.
	 * @param gr the ROI
	 * @param nuclide the assigned nuclide
	 */
	//gr is object, so it is changed here
	private void trySettingRoi(GammaRoi gr, String nuclide){
		failPI=false;//not useful here
		String gammaNuclidesTable = resources
				.getString("main.db.gammaNuclidesTable");
		String gammaNuclidesDetailsTable = resources
				.getString("main.db.gammaNuclidesDetailsTable");
		String gammaNuclidesCoincidenceTable = resources
				.getString("main.db.gammaNuclidesCoincidenceTable");
		String nuc = nuclide;
		try {
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = gammaDB;
			//opens = opens + file_sep + dbName;

			//Connection con1 = DBConnection
					//.getDerbyConnection(opens, "", "");
			String s = "select * from " + gammaNuclidesTable
					+ " where NUCLIDE = " + "'" + nuc + "'";
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);
			
			int ndata = DatabaseAgent.getRowCount();
			if (ndata > 1) {
				// warningS multiple nuclides with same name!!
				// ...must user select!
				String warnings = resources
						.getString("duplicates.database.nuclides");
				statusL.setText(warnings);
				failPI=true;//not useful here
				warningsPI=warnings;//not useful here
				return;
			}
			if (ndata == 0) {
				return;// no updated ROIs!!
			}
			double atomicMass = (Double) DatabaseAgent.getValueAt(0, 2);
			double halfLife = (Double) DatabaseAgent.getValueAt(0, 3);
			String halfLifeUnits = (String) DatabaseAgent.getValueAt(0, 4);
			gr.setNuclide(nuc);
			gr.setAtomicMass(atomicMass);
			gr.setHalfLife(halfLife);
			gr.setHalfLifeUnits(halfLifeUnits);
			gr.setMdaCalculationMethod(GammaRoi.MDA_CALCULATION_DEFAULT);//default!
			int linkID = (Integer) DatabaseAgent.getValueAt(0, 0);				
			// now we have an solo ID..use it!
			s = "select * from " + gammaNuclidesDetailsTable
					+ " where ID = " + linkID + " ORDER BY NRCRT";
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);
			
			ndata = DatabaseAgent.getRowCount();
			double[] energies = new double[ndata];
			double[] yields = new double[ndata];
			double[] decayCorr = new double[ndata];
			for (int i = 0; i < ndata; i++) {
				energies[i] = (Double) DatabaseAgent.getValueAt(i, 2);
				yields[i] = (Double) DatabaseAgent.getValueAt(i, 3);
				decayCorr[i] = (Double) DatabaseAgent.getValueAt(i, 6);
			}

			s = "select * from " + gammaNuclidesCoincidenceTable
					+ " where ID = " + linkID + " ORDER BY NRCRT";
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			ndata = DatabaseAgent.getRowCount();
			double[] energiesCorr = new double[ndata];// coincidence table!
			double[] yieldsCorr = new double[ndata];
			double[] coinCorr = new double[ndata];
			for (int i = 0; i < ndata; i++) {
				energiesCorr[i] = (Double) DatabaseAgent.getValueAt(i, 2);
				yieldsCorr[i] = (Double) DatabaseAgent.getValueAt(i, 3);
				coinCorr[i] = (Double) DatabaseAgent.getValueAt(i, 6);
			}

			gr.updateRoi(energies, yields, decayCorr, energiesCorr,
					yieldsCorr, coinCorr);
			//roiNamelabel.setText(resources.getString("roi.name.label")
					//+ gr.getNuclide());
			//if (con1 != null)
				//con1.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/**
	 * Save the ROIs to a ROI file
	 */
	private void saveROI(){
		//System.out.println("save roi...");
		if (emptyChartB)
			return;

		if (roiV.size() == 0) {
			return;
		}
		
		String ext = resources.getString("roi.load");
		String pct = ".";
		String description = resources.getString("roi.load.description");
		ExampleFileFilter jpgFilter = new ExampleFileFilter(ext, description);
		String datas = resources.getString("data.load");// "Data";
		String filename = "";
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas + file_sep;// + "egs";
		JFileChooser chooser = new JFileChooser(opens);
		chooser.addChoosableFileFilter(jpgFilter);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		int returnVal = chooser.showSaveDialog(this);// parent=this frame
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// filename= chooser.getSelectedFile().toString()+pct+ext;
			filename = chooser.getSelectedFile().toString();
			int fl = filename.length();
			String test = filename.substring(fl - 4);// exstension lookup!!
			String ctest = pct + ext;
			if (test.compareTo(ctest) != 0)
				filename = chooser.getSelectedFile().toString() + pct + ext;

			String s = "";
			for (int j = 0; j < roiV.size(); j++) {
				GammaRoi gr = roiV.elementAt(j);
				int inetmethod = gr.getNetCalculationMethod_internal();
				double starten = gr.getStartEnergy();
				double startedgeen = gr.getStartEdgeEnergy();
				double enden = gr.getEndEnergy();
				double endedgeen = gr.getEndEdgeEnergy();
				String nuclide = gr.getNuclide();
				
				s = s + Convertor.intToString(inetmethod)+" " +
				Convertor.doubleToString(starten)+" " +
				Convertor.doubleToString(startedgeen)+" " +
				Convertor.doubleToString(enden)+" " +
				Convertor.doubleToString(endedgeen)+" "+
				nuclide+"\n";
			}
			
			try {
				FileWriter sigfos = new FileWriter(filename);
				sigfos.write(s);
				sigfos.close();
				
				statusL.setText(resources.getString("status.save") + filename);
			} catch (Exception ex) {

			}
		}
		
		
		
	}
}
