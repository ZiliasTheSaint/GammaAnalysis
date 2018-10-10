package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import danfulea.db.DatabaseAgent;
import danfulea.db.DatabaseAgentSupport;
//import jdf.db.AdvancedSelectPanel;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.math.Convertor;
import danfulea.math.Sort;
import danfulea.phys.PhysUtilities;
import danfulea.utils.FrameUtilities;

/**
 * The graphical user interface (GUI) for creating the proper gamma library of nuclides. <br>
 * Here the coincidence correction and decay correction are performed. <br> 
 * When copying a nuclide from master library to in-use library, all daughters data, i.e. the chain data 
 * is saved.<br>
 * The coincidence correction for each nuclide in chain take care of counts lost in 
 * photo-peak due to the fact that two (or more) quantas can be 
 * simultaneously detected and generate a sum peak (counts that are lost in those 
 * quanta's photo-peaks). To proper calculate the coincidence 
 * correction the total detector efficiency for all energies must be known. 
 * This can be done by Monte-Carlo simulations and tabulated data (energy and 
 * efficiency in random energy intervals) must be saved in a file. The program loads 
 * and reads this file and perform interpolation technique to find detection 
 * efficiency at desired radiation energy.<br>
 * The decay correction is needed because there are many situations when the library 
 * selected nuclides (the PARENT nuclide) do not emit gamma radiation themselves but 
 * their daughters do! Example: parent is 232Th (or 238U) and its daughter 208 Tl (or 214Bi) emits gamma 
 * radiation. So, by choosing from library a PARENT nuclide, you want to compute its 
 * activity based on peaks (ROIs) coming from a DAUGHTER. This is fine if and only if 
 * there is a secular equilibrium in chain, i.e. we have daughter branching ratio BR and gamma 
 * yields Y for daughter (and with Y, the daughter activity is computed) so at any time (hence the word secular) we have the relation: 
 * A_parent = A_daughter /BR and all data are handled by library. 
 * But in reality the secular equilibrium 
 * (the secular equilibrium is when the above equation is satisfied) may never be reach 
 * and therefore the calculations based on library data (which by default always 
 * assumes the secular equilibrium) are not accurate. The decay correction takes care 
 * of this shortcoming in two ways: <br>
 * 1. We know the sample time. At moment ZERO we only have parent nuclide in sample. The sample 
 * is measured after a sample time and we have PARENT and all its DAUGHTERS inside sample. The decay correction 
 * is done by computing chain activities using the Bateman decay law. In most situations, the 
 * sample time is not known so we can assume the sample lived long enough such that there 
 * is established some kind of equilibrium in PARENT-DAUGHTER chain. This leads to second way. <br>   
 * 2. By assuming we have a equilibrium in the PARENT-DAUGHTER chain 
 * but this equilibrium is NOT necessarily secular. It is a real equilibrium assumed 
 * to be reach after a reasonable long time (by default 10 times parent half life) and 
 * computations are done using Bateman decay law for radioactive chains. This is the default 
 * mode.<br>
 * In short, decay correction lets you safely compute PARENT nuclide activity by working with 
 * ROIs coming from its DAUGHTERS.      
 * 
 * @author Dan Fulea, 05 May 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaLibraryFrame extends JFrame implements ActionListener,
		ItemListener, ListSelectionListener, Runnable {

	private GammaAnalysisFrame mf;
	protected Color bkgColor;
	protected Color foreColor;
	protected Color textAreaBkgColor;
	protected Color textAreaForeColor;
	private Connection conn = null;
	private ArrayList<Statement> statements = null;
	private ArrayList<ResultSet> resultsets = null;
	private boolean STOPCOMPUTATION=false;
	private boolean stopAppend = false;
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;

	private JTextField nuclideTf = new JTextField(5);
	private JTextField atomicMassTf = new JTextField(5);
	private JTextField halfLifeTf = new JTextField(5);
	@SuppressWarnings("rawtypes")
	private JComboBox halfLifeUnitsCb;
	private JTextField energyTf = new JTextField(5);
	private JTextField yieldTf = new JTextField(5);
	private JTextField notesTf = new JTextField(5);
	private JTextField decayCorrTf = new JTextField(5);

	private JTextField sampleTimeTf = new JTextField(5);
	@SuppressWarnings("rawtypes")
	private JComboBox sampleHalfLifeUnitsCb;
	private JRadioButton a_secRb, a_afterRb, a_eqRb;

	//private AdvancedSelectPanel asp = null;
	//private AdvancedSelectPanel aspDate = null;
	// private SelectPanel spDate = null;
	// private SelectPanel sp = null;
	private JPanel suportSp = new JPanel(new BorderLayout());
	private JPanel suportSpDate = new JPanel(new BorderLayout());

	private JLabel statusL = new JLabel();
	private volatile Thread computationTh = null;// computation thread!
	private volatile Thread statusTh = null;// status display thread!
	private int delay = 100;
	private int frameNumber = -1;
	private String statusRunS = "";
	private String command = "";

	private final Dimension PREFERRED_SIZE = new Dimension(970, 700);
	private final Dimension sizeCb = new Dimension(90, 21);
	private final Dimension smallsizeCb = new Dimension(50, 21);
	private final Dimension textAreaDimension = new Dimension(400, 370);
	private final Dimension tableDimension = new Dimension(300, 200);
	private final Dimension smalltableDimension = new Dimension(300, 150);

	private static final String GETNUCLIDEINFO_COMMAND = "GETNUCLIDEINFO";
	private static final String COPY_COMMAND = "COPY";
	private static final String ADDMAIN_COMMAND = "ADDMAIN";
	private static final String DELETEMAIN_COMMAND = "DELETEMAIN";
	private static final String ADDSECOND_COMMAND = "ADDSECOND";
	private static final String DELETESECOND_COMMAND = "DELETESECOND";
	private static final String VIEWDECAYCHAIN_COMMAND = "VIEWDECAYCHAIN";
	private static final String COINCORR_COMMAND = "COINCORR";
	private static final String KILL_COMMAND = "KILL";

	private String icrpDB = "";
	protected String gammaDB = "";
	private String icrpTable = "";
	private String icrpRadTable = "";
	private String gammaNuclidesTable = "";
	private String gammaNuclidesDetailsTable = "";
	private String gammaNuclidesDecayChainTable = "";
	protected String gammaNuclidesCoincidenceTable = "";

	@SuppressWarnings("rawtypes")
	private JComboBox nucCb, dbCb, nuc2Cb, radTypeCb;
	private JRadioButton thresh_allRb, thresh_1Rb, thresh_2Rb, thresh_5Rb;
	private JCheckBox thresh_energy;

	private Vector<String> nucV = new Vector<String>();
	private JTextArea textArea;

	private double yield_current = 1.0;
	private String parentNuclide = "";
	private Vector<Double> y1_old = new Vector<Double>();
	private Vector<Double> y2_next_chain = new Vector<Double>();
	private boolean colect_first_time = false;
	private boolean parent_dual = false;
	private Vector<Double> y1_fix = new Vector<Double>();
	private Vector<Integer> index = new Vector<Integer>();
	private Vector<String> nuclideS = new Vector<String>();
	private Vector<Double> atomicMassD = new Vector<Double>();
	private Vector<Double> yieldD = new Vector<Double>();
	private Vector<Integer> radLocI = new Vector<Integer>();
	private Vector<Integer> radLocN = new Vector<Integer>();
	private Vector<Double> halfLifeD = new Vector<Double>();
	private Vector<String> halfLifeUnitsS = new Vector<String>();

	private static final int IGAMMA = 1;
	private static final int IXRAY = 2;
	private static final int IANNIH = 3;
	// rows=data;columns=nuclide index
	private String[][] rcode;
	private String[][] rtype;
	private double[][] ryield;
	private double[][] renergy;
	// ------------------------
	private String[] nucInChain;
	private double DTHRESH = 0.00;// all radiation
	private static final double DTHRESH0 = 0.00;// all radiation
	private static final double DTHRESH1 = 0.01;// cut yields below 1%
	private static final double DTHRESH2 = 0.02;// cut yields below 2%
	private static final double DTHRESH5 = 0.05;// cut yields below 5%

	private String nuclideInfo = "";
	private double atomicMassInfo = 0.0;
	private double halfLifeInfo = 0.0;
	private String halfLifeUnitsInfo = "";
	private String decayCorrInfo = "";
	private Vector<Double> energyInfo;
	private Vector<Double> yieldInfo;
	private Vector<String> notesInfo;
	private Vector<String> radiationTypeInfo;
	private Vector<Double> decayCorr;
	private String sampleTimeS = "";
	private String sampleTimeUnitsS = "";

	private int ndaughter = 0;
	private Vector<Integer> nsave = new Vector<Integer>();
	private Vector<Double> yield2_readD = new Vector<Double>();
	private int ndaughterChainMax = 0;
	private String[][] nuc_chains;
	private String[][] hlu_chains;
	private double[][] hl_chains;
	private double[][] br_chains;
	private double[][] am_chains;
	private static final int nmax_chains = 100;
	private static final int nmax_daughters = 840;
	private int nchain = 0;
	// activity of each chain at time set
	private double[][] a_chains;
	// time integrated activity of each chain at time set
	private double[][] at_chains;
	// activity of each chain at time set at 10 x parent half life
	private double[][] a_chains_equilibrum;

	private static final double activityUnit = 1.0;// Bq
	private double elapsedTime = 180.0;// 30.0;//days
	private int timeSteps = 1;// used in BATEMAN DECAY..no intermediate steps!
	// overall nuclides..from nuclideS=new Vector();
	private String[] nuclides_all;
	// overall activities from chains at time set (at t=0, A parent =1.0)
	private double[] activity_chain;
	// overall yields..from yieldD=new Vector();
	private double[] yield_all;
	// formal (not happen always) forced secular equilibrum:
	// activityUnit*yield_all[i];
	private double[] activity_secular;
	// overall normalized activities from chains at time set (at t=t, A parent
	// =1.0)
	private double[] activity_chain_scaled;
	// modified...activity_chain_scaled holds now the time integrated activities
	// during elapsed time. Elapsed time==Sample time!
	// overall normalized activities from chains at 10 x parent half life (at
	// t=t, A parent =1.0)..equilibrum auto-seek!
	private double[] activity_chain_equilibrum;
	private double[] parentDaughterDecayCorr;
	private int IA_use = 3;
	private static final int IA_sec = 0;
	// if this is used, user wants secular equilibrum in sample!
	// Gamma spectrometry uses secular equilibrum by default when
	// computing activity (of parent) from any peak (of daughter).
	private static final int IA_elapsed = 1;
	// User wants to use equilibrum reached after a certain elapsed time!
	// which is the sample time. The secular equilibrum hypotesis
	// is rejected and use decay parent-daughter correction. The
	// sample time must be known!!!!!
	// Only the parent activity at time 0 with no daughters is considered.
	// Decay follows normal laws (bateman) during elapsed time.
	private static final int IA_norm = 2;
	// same as above but:
	// if this is used, user wants time integrated activities
	// for computing decay parent-daughter correction.
	// Only the parent activity at time 0 with no daughters is considered.
	// Decay follows normal laws (bateman) during elapsed time.
	private static final int IA_eq = 3;
	// similar to IA_elapsed but:
	// if this is used, user wants real equilibrum (computed at
	// 10 X parent half life) !
	private static final double timesHalfLifeForEq = 10.0;

	/**
	 * The connection
	 */
	private Connection gammadbcon = null;
	
	/**
	 * Main table primary key column name
	 */
	private String mainTablePrimaryKey = "ID";//bkgTable
	
	/**
	 * Nested table primary key column name
	 */
	private String nestedTablePrimaryKey = "NRCRT";//bkgDetailTable
	
	/**
	 * Shared column name for main table and nested table
	 */
	private String IDlink = "ID";
	
	/**
	 * The JTable component associated to main table
	 */
	private JTable mainTable;
	
	/**
	 * The column used for sorting data in main table (ORDER BY SQL syntax)
	 */
	private String orderbyS = "ID";
	
	/**
	 * The JTable component associated to nested table
	 */
	private JTable nestedTable;
	
	/**
	 * The column used for sorting data in nested table (ORDER BY SQL syntax)
	 */
	private String nestedorderbyS = "NRCRT";
	
	/**
	 * The database agent associated to main table
	 */
	private DatabaseAgentSupport dbagent;
	
	/**
	 * The database agent associated to nested table
	 */
	private DatabaseAgentSupport nesteddbagent;
	
	private JComboBox<String> orderbyCb;
	private JComboBox<String> nestedorderbyCb;
	private final Dimension sizeOrderCb = new Dimension(200, 21);
	
	/**
	 * Constructor. GammaLibrary window is connected to main window.
	 * @param mf the GammaAnalysisFrame object
	 */
	public GammaLibraryFrame(GammaAnalysisFrame mf) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("Library.NAME"));
		this.mf = mf;
		this.bkgColor = GammaAnalysisFrame.bkgColor;
		this.foreColor = GammaAnalysisFrame.foreColor;
		this.textAreaBkgColor = GammaAnalysisFrame.textAreaBkgColor;
		this.textAreaForeColor = GammaAnalysisFrame.textAreaForeColor;
		// for initDb!!!!
		// icrpDB = resources.getString("library.master.db");
		icrpDB = resources.getString("library.master.jaeri.db");
		// icrpTable = resources.getString("library.master.db.indexTable");
		icrpTable = resources.getString("library.master.jaeri.db.indexTable");
		// icrpRadTable = resources.getString("library.master.db.radTable");
		icrpRadTable = resources.getString("library.master.jaeri.db.radTable");
		// ----------------
		gammaDB = resources.getString("main.db");
		gammaNuclidesTable = resources.getString("main.db.gammaNuclidesTable");
		gammaNuclidesDetailsTable = resources
				.getString("main.db.gammaNuclidesDetailsTable");
		gammaNuclidesDecayChainTable = resources
				.getString("main.db.gammaNuclidesDecayChainTable");
		gammaNuclidesCoincidenceTable = resources
				.getString("main.db.gammaNuclidesCoincidenceTable");
		//-====================
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		mainTablePrimaryKey = "ID";
		nestedTablePrimaryKey = "NRCRT";
		IDlink = "ID";
		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = gammaDB;
		opens = opens + file_sep + dbName;
		gammadbcon = DatabaseAgent.getConnection(opens, "", "");		
		dbagent = new DatabaseAgentSupport(gammadbcon, 
				mainTablePrimaryKey, gammaNuclidesTable);
		nesteddbagent = new DatabaseAgentSupport(gammadbcon, nestedTablePrimaryKey,
				gammaNuclidesDetailsTable);	
		dbagent.setHasValidAIColumn(false);
		nesteddbagent.setHasValidAIColumn(false);
		//======================

		initDBComponents();
		performQueryDb();
		createGUI();

		mf.statusL.setText(resources.getString("status.done"));
		mf.stopThread();

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
	 * Set master nuclide library. These are ICRP38 or JAERI. From my experience, 
	 * I recommend JAERI.
	 */
	private void setMasterTables() {
		String dbName = (String) dbCb.getSelectedItem();

		if (dbName.equals(resources.getString("library.master.db"))) {
			icrpDB = resources.getString("library.master.db");
			icrpTable = resources.getString("library.master.db.indexTable");
			icrpRadTable = resources.getString("library.master.db.radTable");
		} else if (dbName
				.equals(resources.getString("library.master.jaeri.db"))) {
			icrpDB = resources.getString("library.master.jaeri.db");
			icrpTable = resources
					.getString("library.master.jaeri.db.indexTable");
			icrpRadTable = resources
					.getString("library.master.jaeri.db.radTable");
		}
	}

	/**
	 * Exit method
	 */
	private void attemptExit() {
		try{
			if (gammadbcon != null)
				gammadbcon.close();
			if (conn != null)
				conn.close();
		}catch (Exception e){
			e.printStackTrace();
		}
		stopThread();
		//cleanUpDerby();//errors due to stopThread!!
		//However..redundant since variables are auto-garbaged at dispose!!!!
		mf.setEnabled(true);
		dispose();
	}

	/**
	 * Setting up the frame size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * This method is called from within the constructor to create GUI.
	 */
	private void createGUI() {
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";
		//====================================
		orderbyCb = dbagent.getOrderByComboBox();
		orderbyCb.setMaximumRowCount(5);
		orderbyCb.setPreferredSize(sizeOrderCb);
		orderbyCb.addItemListener(this);
		JPanel orderP = new JPanel();
		orderP.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP.add(label);
		orderP.add(orderbyCb);
		orderP.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP.add(label);
		orderP.add(dbagent.getRecordsLabel());
		
		nestedorderbyCb = nesteddbagent.getOrderByComboBox();
		nestedorderbyCb.setMaximumRowCount(5);
		nestedorderbyCb.setPreferredSize(sizeOrderCb);
		nestedorderbyCb.addItemListener(this);
		JPanel orderP2 = new JPanel();
		orderP2.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP2.add(label);
		orderP2.add(nestedorderbyCb);
		orderP2.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP2.add(label);
		orderP2.add(nesteddbagent.getRecordsLabel());
		//===================================================
		nuc2Cb = new JComboBox();
		nuc2Cb.setMaximumRowCount(15);
		nuc2Cb.setPreferredSize(sizeCb);

		nucCb = new JComboBox();
		nucCb.setMaximumRowCount(15);
		nucCb.setPreferredSize(sizeCb);
		for (int i = 0; i < nucV.size(); i++) {
			nucCb.addItem((String) nucV.elementAt(i));

			nuc2Cb.addItem((String) nucV.elementAt(i));
		}

		radTypeCb = new JComboBox();
		radTypeCb.setMaximumRowCount(15);
		radTypeCb.setPreferredSize(sizeCb);
		radTypeCb.addItem(resources.getString("radiation.x"));
		radTypeCb.addItem(resources.getString("radiation.gamma"));

		String[] dbS = (String[]) resources.getObject("library.master.dbCb");
		dbCb = new JComboBox(dbS);
		String defaultS = dbS[1];// JAERI
		dbCb.setSelectedItem((Object) defaultS);
		dbCb.setMaximumRowCount(5);
		dbCb.setPreferredSize(sizeCb);
		dbCb.addItemListener(this);

		String[] hluS = (String[]) resources.getObject("library.inuse.hluCb");
		halfLifeUnitsCb = new JComboBox(hluS);
		String str = hluS[4];// seconds
		halfLifeUnitsCb.setSelectedItem((Object) str);
		halfLifeUnitsCb.setMaximumRowCount(5);
		halfLifeUnitsCb.setPreferredSize(smallsizeCb);

		sampleHalfLifeUnitsCb = new JComboBox(hluS);
		str = hluS[1];// days
		sampleHalfLifeUnitsCb.setSelectedItem((Object) str);
		sampleHalfLifeUnitsCb.setMaximumRowCount(5);
		sampleHalfLifeUnitsCb.setPreferredSize(smallsizeCb);
		a_secRb = new JRadioButton(resources.getString("library.a_secRb"));
		a_secRb.setBackground(GammaAnalysisFrame.bkgColor);
		a_secRb.setForeground(GammaAnalysisFrame.foreColor);
		a_afterRb = new JRadioButton(resources.getString("library.a_afterRb"));
		a_afterRb.setBackground(GammaAnalysisFrame.bkgColor);
		a_afterRb.setForeground(GammaAnalysisFrame.foreColor);
		a_eqRb = new JRadioButton(resources.getString("library.a_eqRb"));
		a_eqRb.setBackground(GammaAnalysisFrame.bkgColor);
		a_eqRb.setForeground(GammaAnalysisFrame.foreColor);
		a_secRb.setToolTipText(resources.getString("library.a_secRb.toolTip"));
		a_afterRb.setToolTipText(resources
				.getString("library.a_afterRb.toolTip"));
		a_eqRb.setToolTipText(resources.getString("library.a_eqRb.toolTip"));
		ButtonGroup groupA = new ButtonGroup();
		groupA.add(a_secRb);
		groupA.add(a_afterRb);
		groupA.add(a_eqRb);
		a_eqRb.setSelected(true);
		JPanel g0 = new JPanel();
		g0.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("library.sampleTimeLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		g0.add(label);
		g0.add(sampleTimeTf);
		sampleTimeTf.setText("180");
		label = new JLabel(resources.getString("library.sampleTimeUnitLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		g0.add(label);
		g0.add(sampleHalfLifeUnitsCb);
		g0.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel g5 = new JPanel();
		g5.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		g5.add(a_secRb, null);
		g5.add(a_afterRb, null);
		g5.setBackground(GammaAnalysisFrame.bkgColor);
		JPanel g51 = new JPanel();
		g51.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		g51.add(a_eqRb, null);
		g51.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p51 = new JPanel();
		BoxLayout bld51 = new BoxLayout(p51, BoxLayout.Y_AXIS);
		p51.setLayout(bld51);
		p51.add(g5, null);
		p51.add(g51, null);
		p51.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p5 = new JPanel();
		BoxLayout bld5 = new BoxLayout(p5, BoxLayout.Y_AXIS);
		p5.setLayout(bld5);
		p5.add(g0, null);
		p5.add(p51, null);
		p5.setBackground(GammaAnalysisFrame.bkgColor);
		p5.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("library.decayCorr.border"),GammaAnalysisFrame.foreColor));

		textArea = new JTextArea();
		textArea.setCaretPosition(0);
		textArea.setEditable(false);
		textArea.setText("");
		textArea.setWrapStyleWord(true);
		textArea.setBackground(GammaAnalysisFrame.textAreaBkgColor);
		textArea.setForeground(GammaAnalysisFrame.textAreaForeColor);

		thresh_allRb = new JRadioButton(
				resources.getString("library.threshold.all"));
		thresh_allRb.setBackground(GammaAnalysisFrame.bkgColor);
		thresh_allRb.setForeground(GammaAnalysisFrame.foreColor);
		thresh_1Rb = new JRadioButton(
				resources.getString("library.threshold.cut1"));
		thresh_1Rb.setBackground(GammaAnalysisFrame.bkgColor);
		thresh_1Rb.setForeground(GammaAnalysisFrame.foreColor);
		thresh_2Rb = new JRadioButton(
				resources.getString("library.threshold.cut2"));
		thresh_2Rb.setBackground(GammaAnalysisFrame.bkgColor);
		thresh_2Rb.setForeground(GammaAnalysisFrame.foreColor);
		thresh_5Rb = new JRadioButton(
				resources.getString("library.threshold.cut5"));
		thresh_5Rb.setBackground(GammaAnalysisFrame.bkgColor);
		thresh_5Rb.setForeground(GammaAnalysisFrame.foreColor);
		thresh_energy = new JCheckBox(
				resources.getString("library.threshold.energy"));
		thresh_energy.setBackground(GammaAnalysisFrame.bkgColor);
		thresh_energy.setForeground(GammaAnalysisFrame.foreColor);

		ButtonGroup groupC = new ButtonGroup();
		groupC.add(thresh_allRb);
		groupC.add(thresh_1Rb);
		groupC.add(thresh_2Rb);
		groupC.add(thresh_5Rb);
		thresh_1Rb.setSelected(true);

		JPanel p20 = new JPanel();
		p20.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p20.add(thresh_allRb);
		p20.add(thresh_1Rb);
		p20.setBackground(GammaAnalysisFrame.bkgColor);
		JPanel p21 = new JPanel();
		p21.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p21.add(thresh_2Rb);
		p21.add(thresh_5Rb);
		p21.setBackground(GammaAnalysisFrame.bkgColor);
		JPanel p22 = new JPanel();
		p22.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p22.setBackground(GammaAnalysisFrame.bkgColor);
		JPanel p23 = new JPanel();
		p23.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p23.setBackground(GammaAnalysisFrame.bkgColor);
		JPanel p233 = new JPanel();
		p233.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p233.add(thresh_energy);
		thresh_energy.setSelected(true);
		p233.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p2 = new JPanel();
		BoxLayout bld2 = new BoxLayout(p2, BoxLayout.Y_AXIS);
		p2.setLayout(bld2);
		p2.add(p20, null);
		p2.add(p21, null);
		p2.add(p233, null);
		p2.setBackground(GammaAnalysisFrame.bkgColor);
		p2.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("library.threshold.border"), GammaAnalysisFrame.foreColor));

		JPanel p1P = new JPanel();
		p1P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));

		label = new JLabel(resources.getString("library.master.dbLabel"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p1P.add(label);
		p1P.add(dbCb);

		label = new JLabel(resources.getString("library.master.nuclide.label"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p1P.add(label);
		p1P.add(nucCb);
		p1P.setBackground(GammaAnalysisFrame.bkgColor);

		buttonName = resources.getString("library.master.nuclide.button");
		buttonToolTip = resources
				.getString("library.master.nuclide.button.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName,
				GETNUCLIDEINFO_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("library.master.nuclide.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		JPanel p2P = new JPanel();
		p2P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p2P.add(button);
		
		buttonName = resources.getString("stop.button");
		buttonToolTip = resources.getString("stop.button.toolTip");
		buttonIconName = resources.getString("img.close");
		button = FrameUtilities.makeButton(buttonIconName, KILL_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("stop.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2P.add(button);
		p2P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel boxP = new JPanel();
		BoxLayout bl = new BoxLayout(boxP, BoxLayout.Y_AXIS);
		boxP.setLayout(bl);
		boxP.add(p1P);
		boxP.add(p2);
		boxP.add(p2P);
		boxP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel resultP = new JPanel(new BorderLayout());
		resultP.setPreferredSize(textAreaDimension);
		resultP.add(new JScrollPane(textArea), BorderLayout.CENTER);
		resultP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel eastP = new JPanel(new BorderLayout());
		eastP.add(boxP, BorderLayout.NORTH);
		eastP.add(resultP, BorderLayout.CENTER);
		eastP.setBackground(GammaAnalysisFrame.bkgColor);
		eastP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("library.master.border"), GammaAnalysisFrame.foreColor));
		// //////////
		JPanel p01P = new JPanel();
		p01P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.nuclideLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p01P.add(label);
		p01P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p02P = new JPanel();
		p02P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p02P.add(nuclideTf);
		p02P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p03P = new JPanel();
		p03P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.atomicMassLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p03P.add(label);
		p03P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p04P = new JPanel();
		p04P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p04P.add(atomicMassTf);
		p04P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p05P = new JPanel();
		p05P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.halfLifeLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p05P.add(label);
		p05P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p06P = new JPanel();
		p06P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p06P.add(halfLifeTf);
		p06P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p07P = new JPanel();
		p07P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.halfLifeUnitsLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p07P.add(label);
		p07P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p08P = new JPanel();
		p08P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p08P.add(halfLifeUnitsCb);
		p08P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p09P = new JPanel();
		p09P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p09P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p10P = new JPanel();
		p10P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

		buttonName = resources.getString("library.inuse.main.delete.button");
		buttonToolTip = resources
				.getString("library.inuse.main.delete.button.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName, DELETEMAIN_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("library.inuse.main.delete.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		p10P.add(button);
		p10P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p1 = new JPanel();
		BoxLayout bl01 = new BoxLayout(p1, BoxLayout.Y_AXIS);
		p1.setLayout(bl01);
		p1.add(p10P);
		p1.setBackground(GammaAnalysisFrame.bkgColor);

		// ////////////////
		JPanel p001P = new JPanel();
		p001P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.energyLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p001P.add(label);
		p001P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p002P = new JPanel();
		p002P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p002P.add(energyTf);
		p002P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p003P = new JPanel();
		p003P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.yieldLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p003P.add(label);
		p003P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p004P = new JPanel();
		p004P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p004P.add(yieldTf);
		p004P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p005P = new JPanel();
		p005P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.notesLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p005P.add(label);
		p005P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p006P = new JPanel();
		p006P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p006P.add(nuc2Cb);
		p006P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p055P = new JPanel();
		p055P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.radTypeLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p055P.add(label);
		p055P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p066P = new JPanel();
		p066P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p066P.add(radTypeCb);
		p066P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p0055P = new JPanel();
		p0055P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		label = new JLabel(resources.getString("library.inuse.decayCorrLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p0055P.add(label);
		p0055P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p0066P = new JPanel();
		p0066P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		p0066P.add(decayCorrTf);
		decayCorrTf.setText("1.0");
		p0066P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p007P = new JPanel();
		p007P.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));

		buttonName = resources.getString("library.inuse.secondary.add.button");
		buttonToolTip = resources
				.getString("library.inuse.secondary.add.button.toolTip");
		buttonIconName = resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, ADDSECOND_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("library.inuse.secondary.add.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		JButton b1Button = button;

		p007P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p008P = new JPanel();
		p008P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));

		buttonName = resources
				.getString("library.inuse.secondary.delete.button");
		buttonToolTip = resources
				.getString("library.inuse.secondary.delete.button.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName,
				DELETESECOND_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("library.inuse.secondary.delete.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		p008P.add(button);
		p008P.add(b1Button);
		p008P.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel p11 = new JPanel();
		BoxLayout bl001 = new BoxLayout(p11, BoxLayout.Y_AXIS);
		p11.setLayout(bl001);
		p11.add(p001P);
		p11.add(p002P);
		p11.add(p003P);
		p11.add(p004P);
		p11.add(p005P);
		p11.add(p006P);
		p11.add(p055P);
		p11.add(p066P);
		p11.add(p0055P);
		p11.add(p0066P);

		p11.setBackground(GammaAnalysisFrame.bkgColor);
		// ----------
		buttonName = resources.getString("library.view.button");
		buttonToolTip = resources.getString("library.view.toolTip");
		buttonIconName = resources.getString("img.view");
		button = FrameUtilities.makeButton(buttonIconName,
				VIEWDECAYCHAIN_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("library.view.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		JPanel viewP = new JPanel();
		viewP.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		viewP.add(button);

		buttonName = resources.getString("library.inuse.main.add.button");
		buttonToolTip = resources
				.getString("library.inuse.main.add.button.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, COINCORR_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("library.inuse.main.add.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		viewP.add(button);
		viewP.setBackground(GammaAnalysisFrame.bkgColor);

		JScrollPane scrollPane = new JScrollPane(mainTable);
		mainTable.setFillsViewportHeight(true);
		suportSp.add(scrollPane);
		
		JPanel northeastP = new JPanel();
		BoxLayout blnortheast = new BoxLayout(northeastP, BoxLayout.Y_AXIS);
		northeastP.setLayout(blnortheast);
		northeastP.add(suportSp);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		northeastP.add(viewP);
		northeastP.setBackground(GammaAnalysisFrame.bkgColor);
		// ----------		
		JPanel nordP = new JPanel();
		nordP.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		nordP.add(northeastP);
		nordP.add(p1);
		JPanel nnordP = new JPanel();
		BoxLayout nnordPbl = new BoxLayout(nnordP, BoxLayout.Y_AXIS);
		nnordP.setLayout(nnordPbl);
		nnordP.add(orderP);
		nnordP.add(nordP);
		nnordP.setBackground(GammaAnalysisFrame.bkgColor);
		
		suportSp.setPreferredSize(smalltableDimension);//@@@@@@@@@@
		nordP.setBackground(GammaAnalysisFrame.bkgColor);

		JScrollPane scrollPane2 = new JScrollPane(nestedTable);
		nestedTable.setFillsViewportHeight(true);
		suportSpDate.add(scrollPane2);
		
		JPanel southeastP = new JPanel();
		BoxLayout blsoutheast = new BoxLayout(southeastP, BoxLayout.Y_AXIS);
		southeastP.setLayout(blsoutheast);
		southeastP.add(suportSpDate);
		southeastP.add(p008P);
		southeastP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel sudP = new JPanel();
		sudP.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		sudP.add(southeastP);
		sudP.add(p11);
		suportSpDate.setPreferredSize(tableDimension);
		sudP.setBackground(GammaAnalysisFrame.bkgColor);
		JPanel ssudP = new JPanel();
		BoxLayout ssudPbl = new BoxLayout(ssudP, BoxLayout.Y_AXIS);
		ssudP.setLayout(ssudPbl);
		ssudP.add(orderP2);
		ssudP.add(sudP);//@@@@@@@@
		ssudP.setBackground(GammaAnalysisFrame.bkgColor);
		
		JPanel bdP = new JPanel();
		BoxLayout bl0 = new BoxLayout(bdP, BoxLayout.Y_AXIS);
		bdP.setLayout(bl0);
		bdP.add(nnordP);//bdP.add(nordP);@@@@@@@@@@@@@@
		Component rigidArea = Box.createRigidArea(new Dimension(0, 5));
		rigidArea.setBackground(GammaAnalysisFrame.bkgColor);// redundant
		bdP.add(rigidArea);// some space
		bdP.add(ssudP);//bdP.add(sudP);
		bdP.setBackground(GammaAnalysisFrame.bkgColor);

		//nordP.setBorder(FrameUtilities.getGroupBoxBorder(""));
		nnordP.setBorder(FrameUtilities.getGroupBoxBorder(""));
		//sudP.setBorder(FrameUtilities.getGroupBoxBorder(""));
		ssudP.setBorder(FrameUtilities.getGroupBoxBorder(""));
		// //////////
		JPanel westP = new JPanel(new BorderLayout());
		westP.setBackground(GammaAnalysisFrame.bkgColor);
		westP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("library.inuse.border"), GammaAnalysisFrame.foreColor));
		westP.add(bdP);

		JPanel wwP = new JPanel();
		BoxLayout bl0123 = new BoxLayout(wwP, BoxLayout.Y_AXIS);
		wwP.setLayout(bl0123);
		wwP.setBackground(GammaAnalysisFrame.bkgColor);
		wwP.add(p5);
		wwP.add(westP);

		buttonName = resources.getString("library.copy.button");
		buttonToolTip = resources.getString("library.copy.toolTip");
		buttonIconName = resources.getString("img.pan.right");
		button = FrameUtilities.makeButton(buttonIconName, COPY_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("library.copy.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		JPanel mainP = new JPanel();
		mainP.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		mainP.add(eastP);
		mainP.add(button);
		mainP.add(wwP);
		mainP.setBackground(GammaAnalysisFrame.bkgColor);

		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		initStatusBar(statusBar);

		JPanel content = new JPanel(new BorderLayout());
		content.add(statusBar, BorderLayout.PAGE_END);
		content.add(mainP, BorderLayout.CENTER);
		setContentPane(new JScrollPane(content));
		content.setOpaque(true); // content panes must be opaque
		pack();
	}

	/**
	 * Initialize status bar.
	 * 
	 * @param toolBar toolBar
	 */
	private void initStatusBar(JToolBar toolBar) {
		JPanel toolP = new JPanel();
		toolP.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));

		toolP.add(statusL);
		toolBar.add(toolP);
		statusL.setText(resources.getString("status.wait"));
	}

	/**
	 * Initialize database.
	 */
	private void performQueryDb() {
		dbagent.init();
		orderbyS = mainTablePrimaryKey;// when start-up...ID is default!!
		
		nesteddbagent.init();
		nestedorderbyS = nestedTablePrimaryKey;
		
		mainTable = dbagent.getMainTable();
		// allow single selection of rows...not multiple rows!
		ListSelectionModel rowSM = mainTable.getSelectionModel();
		rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSM.addListSelectionListener(this);// listener!	
		
		nestedTable = nesteddbagent.getMainTable();
		ListSelectionModel rowSM2 = nestedTable.getSelectionModel();
		rowSM2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//rowSM2.addListSelectionListener(this);
		
		if (mainTable.getRowCount() > 0){
			//select last row!
			mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
					mainTable.getRowCount() - 1); // last ID
		} 
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;

			String s = "select * from " + gammaNuclidesTable;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			asp = new AdvancedSelectPanel();
			suportSp.add(asp, BorderLayout.CENTER);

			JTable mainTable = asp.getTab();

			ListSelectionModel rowSM = mainTable.getSelectionModel();
			rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			rowSM.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					if (e.getValueIsAdjusting())
						return; // Don't want to handle intermediate selections

					updateDetailTable();
				}
			});

			if (mainTable.getRowCount() > 0) {
				// always display last row!
				mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
						mainTable.getRowCount() - 1);
			} else {
				// empty table..display header
				s = "select * from " + gammaNuclidesDetailsTable;
				DBOperation.select(s, con1);

				if (aspDate != null)
					suportSpDate.remove(aspDate);

				aspDate = new AdvancedSelectPanel();
				suportSpDate.add(aspDate, BorderLayout.CENTER);
			}

			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Display data from "secondary" table (nested table) according to selection made in <br>
	 * "main" table. These tables are connected to each other via ID.
	 */
	private void updateDetailTable() {

		//JTable aspTable = asp.getTab();
		int selID = 0;// NO ZERO ID
		int selRow = mainTable.getSelectedRow();//aspTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) mainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
		} else {
			// return;
		}

		// ===update nested===
		nesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		nesteddbagent.performSelection(nestedorderbyS);
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;

			String s = "select * from " + gammaNuclidesDetailsTable
					+ " where ID = " + selID + " ORDER BY NRCRT";
			// IF press header=>selRow=-1=>ID=0=>NO ZERO ID DATA so display
			// empty!
			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			if (aspDate != null)
				suportSpDate.remove(aspDate);

			aspDate = new AdvancedSelectPanel();
			suportSpDate.add(aspDate, BorderLayout.CENTER);

			JTable detailTable = aspDate.getTab();
			ListSelectionModel row1SM = detailTable.getSelectionModel();
			row1SM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			validate();

			if (con1 != null)
				con1.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}*/

	}

	/**
	 * Initialize available nuclides.
	 */
	private void initDBComponents() {
		/*
		 * Under Linux we have only one simple option: Use a java database
		 * management system not MS acces (via odbc) or mysql because errors or
		 * undesired complications may appear. Therefore, derby.jar from Apache
		 * is appropriate for this task. We no need auxiliary software installed
		 * in order to run this application. We will use a simple embedded
		 * database driver located in lib folder along with other auxiliary
		 * packages such as jfreechart or itext. And of course this application
		 * is portable on any platform Windows,Mac or Linux or whatever OS...the
		 * only need is a JVM installed on target computer where this
		 * application is deployed and executed.
		 */
		// In linux, MsAcces database is not recognized.
		// via JDBC:ODBC on LInux platform. We change DBMS (Database management
		// system)
		// using Apache derby driver.

		/*
		 * First thing we do is create a database (in fact two databases) one
		 * for ICRP38 data and one for specific gama data. In MsAcces we
		 * performed an export data in text files which will be used to
		 * construct appropriate tables in derby Apache database.
		 */

		nucV = new Vector<String>();

		// list of Statements, PreparedStatements
		conn = null;
		statements = new ArrayList<Statement>();

		// PreparedStatement psInsert = null;
		// PreparedStatement psUpdate = null;
		Statement s = null;
		ResultSet rs = null;
		try {
			String datas = resources.getString("data.load");// Data
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = icrpDB;// "ICRP38"; // the name of the database
			opens = opens + file_sep + dbName;

			/*
			 * This connection specifies create=true in the connection URL to
			 * cause the database to be created when connecting for the first
			 * time. To remove the database, remove the directory derbyDB (the
			 * same as the database name) and its contents.
			 * 
			 * The directory derbyDB will be created under the directory that
			 * the system property derby.system.home points to, or the current
			 * directory (user.dir) if derby.system.home is not set.
			 */

			// createDerbyDatabase(dbName);
			// createGammaDB();

			/*
			 * MsAccess cannot export properly tables data! Therefore the
			 * copying method of MsAcces tables via MsAcces exported file (e.g.
			 * text file) must be avoided! Only way: We must copy directly from
			 * MsAcces which means we must run this particular operation under
			 * WINDOWS!!! Once derby database is properly set, the application
			 * can run EVERYWHERE!!!
			 */

			// copyMsAccesICRP38IndexTable();
			// copyMsAccesICRP38RadTable();
			// /creating another database

			// old method of copy db AVOID THAT!!
			// oldCopyMsAccesExportedFileIntoDerbyTables();

			conn = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");
			// We want to control transactions manually. Autocommit is on by
			// default in JDBC.
			conn.setAutoCommit(false);
			/*
			 * Creating a statement object that we can use for running various
			 * SQL statements commands against the database.
			 */
			s = conn.createStatement();
			statements.add(s);

			rs = s.executeQuery("SELECT * FROM " + icrpTable);

			if (rs != null)
				while (rs.next()) {
					String ss = rs.getString(2);
					nucV.addElement(ss);
				}

			// delete the table
			// s.execute("drop table " +
			// "icrp38Rad");

			/*
			 * We commit the transaction. Any changes will be persisted to the
			 * database now.
			 */
			conn.commit();

			/*
			 * In embedded mode, an application should shut down the database.
			 * If the application fails to shut down the database, Derby will
			 * not perform a checkpoint when the JVM shuts down. This means that
			 * it will take longer to boot (connect to) the database the next
			 * time, because Derby needs to perform a recovery operation.
			 * 
			 * It is also possible to shut down the Derby system/engine, which
			 * automatically shuts down all booted databases.
			 * 
			 * Explicitly shutting down the database or the Derby engine with
			 * the connection URL is preferred. This style of shutdown will
			 * always throw an SQLException.
			 * 
			 * Not shutting down when in a client environment, see method
			 * Javadoc.
			 */

			// do not shutdown derby..it will be closed at frame exit!

			// } catch (SQLException sqle) {
			// //printSQLException(sqle);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// release all open resources to avoid unnecessary memory usage

			// ResultSet
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
			}

			// Statements and PreparedStatements
			int i = 0;
			while (!statements.isEmpty()) {
				// PreparedStatement extend Statement
				Statement st = (Statement) statements.remove(i);
				try {
					if (st != null) {
						st.close();
						st = null;
					}
				} catch (SQLException sqle) {
					printSQLException(sqle);
				}
			}

			// Connection
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
			}
		}
	}

	
	@SuppressWarnings("unused")
	/**
	 * Some variable cleaning.
	 */
	private void cleanUpDerby() {

		int i = 0;
		while (!statements.isEmpty()) {
			// PreparedStatement extend Statement
			Statement st = (Statement) statements.remove(i);
			try {
				if (st != null) {
					st.close();
					st = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
			}
		}
	}

	/**
	 * Prints details of an SQLException chain to <code>System.err</code>.
	 * Details included are SQL State, Error code, Exception message.
	 * 
	 * @param e
	 *            the SQLException from which to print details.
	 */
	public static void printSQLException(SQLException e) {
		// Unwraps the entire exception chain to unveil the real cause of the
		// Exception.
		while (e != null) {
			System.err.println("\n----- SQLException -----");
			System.err.println("  SQL State:  " + e.getSQLState());
			System.err.println("  Error Code: " + e.getErrorCode());
			System.err.println("  Message:    " + e.getMessage());
			// for stack traces, refer to derby.log or uncomment this:
			// e.printStackTrace(System.err);
			e = e.getNextException();
		}
	}

	/**
	 * Most actions are set here
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		// String command = arg0.getActionCommand();
		command = arg0.getActionCommand();
		if (command.equals(GETNUCLIDEINFO_COMMAND)) {
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(COPY_COMMAND)) {
			copy();
		} else if (command.equals(ADDMAIN_COMMAND)) {
			addMain();
		} else if (command.equals(DELETEMAIN_COMMAND)) {
			deleteMain();
		} else if (command.equals(ADDSECOND_COMMAND)) {
			addSecond();
		} else if (command.equals(DELETESECOND_COMMAND)) {
			deleteSecond();
		} else if (command.equals(VIEWDECAYCHAIN_COMMAND)) {
			viewDecayChain();
		} else if (command.equals(COINCORR_COMMAND)) {
			coinCorrInit();
		} else if (command.equals(KILL_COMMAND)) {
			kill();
		} 
	}

	/**
	 * JCombobox related actions are set here
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == dbCb) {
			fetchNuclideBaseInfo();
		} else if (ie.getSource() == orderbyCb) {
			sort();
		} else if (ie.getSource() == nestedorderbyCb) {
			sort2();
		}
	}

	/**
	 * JTable related actions are set here
	 */
	public void valueChanged(ListSelectionEvent e) {

		if (e.getSource() == mainTable.getSelectionModel()) {
			updateDetailTable();			
		}
	}
	/**
	 * Sorts data from main table
	 */
	private void sort() {
		orderbyS = (String) orderbyCb.getSelectedItem();
		// performSelection();
		dbagent.performSelection(orderbyS);
	}
	
	/**
	 * Sorts data from nested table
	 */
	private void sort2() {
		nestedorderbyS = (String) nestedorderbyCb.getSelectedItem();
		
		//JTable aspTable = asp.getTab();
		int selID = 0;// NO ZERO ID
		int selRow = mainTable.getSelectedRow();//aspTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) mainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
		} else {
			return;
		}

		// ===update nested===
		nesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
				
		nesteddbagent.performSelection(nestedorderbyS);
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Retrieve nuclide basic information
	 */
	private void fetchNuclideBaseInfo() {

		nucV = new Vector<String>();

		setMasterTables();

		Connection conn12 = null;

		Statement s = null;
		ResultSet rs = null;
		try {
			String datas = resources.getString("data.load");// Data
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = icrpDB;
			opens = opens + file_sep + dbName;

			conn12 = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");

			s = conn12.createStatement();

			rs = s.executeQuery("SELECT * FROM " + icrpTable);

			nucCb.removeAllItems();
			nuc2Cb.removeAllItems();

			if (rs != null)
				while (rs.next()) {
					String ss = rs.getString(2);
					nucV.addElement(ss);

					nucCb.addItem(ss);
					nuc2Cb.addItem(ss);
				}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// release all open resources to avoid unnecessary memory usage

			// ResultSet
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}

				if (s != null) {
					s.close();
					s = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
			}

			// Connection
			try {
				if (conn12 != null) {
					conn12.close();
					conn12 = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
			}
		}
	}

	/**
	 * Preparing coincidence correction.
	 */
	private void coinCorrInit() {
		//JTable aspTable = asp.getTab();

		// IF press header=>selRow=-1
		int linkID = 0;// NO ZERO ID
		int selRow = mainTable.getSelectedRow();//aspTable.getSelectedRow();
		if (selRow != -1) {
			linkID = (Integer) mainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
		} else {
			return;
		}
		// retrieve some useful data:

		try {
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = gammaDB;
			//opens = opens + file_sep + dbName;

			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			String s = "select * from " + gammaNuclidesDecayChainTable
					+ " where ID = " + linkID + " ORDER BY NRCRT";
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			int ndata = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
			String[] nuclidesCorrS = new String[ndata];
			double[] brNuclidesCorrD = new double[ndata];
			for (int i = 0; i < ndata; i++) {
				nuclidesCorrS[i] = (String) DatabaseAgent.getValueAt(i, 2);//DBOperation.getValueAt(i, 2);// 0=nrcrt,1=id
				brNuclidesCorrD[i] = (Double) DatabaseAgent.getValueAt(i, 3);//DBOperation.getValueAt(i, 3);//
			}

			s = "select * from " + gammaNuclidesDetailsTable + " where ID = "
					+ linkID + " AND RADTYPE = " + "'"
					+ resources.getString("radiation.x") + "'"
					+ " ORDER BY NRCRT";// order by not necessary!!
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			ndata = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
			double[] energyXRayCorrD = new double[ndata];
			double[] yieldXRayCorrD = new double[ndata];
			for (int i = 0; i < ndata; i++) {
				energyXRayCorrD[i] = (Double) DatabaseAgent.getValueAt(i, 2);//DBOperation.getValueAt(i, 2);//
				yieldXRayCorrD[i] = (Double) DatabaseAgent.getValueAt(i, 3);//DBOperation.getValueAt(i, 3);//
			}

			GammaCoincidenceCorrectionFrame.IDLink = linkID;
			new GammaCoincidenceCorrectionFrame(nuclidesCorrS, brNuclidesCorrD,
					energyXRayCorrD, yieldXRayCorrD, this);

			//if (con1 != null)
				//con1.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	/**
	 * View decay chain informations!
	 */
	private void viewDecayChain() {
		//JTable aspTable = asp.getTab();

		int linkID = 0;// NO ZERO ID
		int selRow = mainTable.getSelectedRow();//aspTable.getSelectedRow();
		if (selRow != -1) {
			linkID = (Integer) mainTable.getValueAt(selRow, 0) - 1;//aspTable.getValueAt(selRow, 0) - 1;
		} else {
			linkID = mainTable.getRowCount() - 1;//aspTable.getRowCount() - 1;
		}
		GammaViewDecayChainFrame.IDLink = linkID;//just for selections!!! not important!
		new GammaViewDecayChainFrame(this);
	}

	/**
	 * Add data to main table. This will never be used...copy from master
	 * library only! <br>
	 * Only from master library, the chain data is properly build (used in 
	 * coincidence correction) as well as decay correction factor!
	 * 
	 * 
	 */
	@Deprecated
	private void addMain() {
		// System.out.println("add main");
		/*
		 * private JTextField energyTf = new JTextField(5); private JTextField
		 * yieldTf = new JTextField(5); private JTextField notesTf = new
		 * JTextField(5);
		 */
		/*String nuc = nuclideTf.getText();
		String hlu = (String) halfLifeUnitsCb.getSelectedItem();
		double am = 0.0;
		double hl = 0.0;
		boolean nulneg = false;
		try {
			am = Convertor.stringToDouble(atomicMassTf.getText());
			if (am <= 0)
				nulneg = true;
			hl = Convertor.stringToDouble(halfLifeTf.getText());
			if (hl <= 0)
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

		try {

			// prepare db query data
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;
			// make a connection
			//Connection con1 = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");
			// first make a selection to retrieve usefull data
			String s = "select * from " + gammaNuclidesTable;
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			int id = DatabaseAgent.getRowCount() + 1;//DBOperation.getRowCount() + 1;// id where we make the
													// insertion

			PreparedStatement psInsert = null;

			psInsert = con1.prepareStatement("insert into "
					+ gammaNuclidesTable + " values " + "(?, ?, ?, ?, ?,?)");
			psInsert.setString(1, Convertor.intToString(id));
			psInsert.setString(2, nuc);
			psInsert.setString(3, Convertor.doubleToString(am));
			psInsert.setString(4, Convertor.doubleToString(hl));
			psInsert.setString(5, hlu);
			psInsert.setString(6, resources.getString("db.equilibrum.user"));
			psInsert.executeUpdate();

			performCurrentSelection();

			nuclideTf.setText("");
			atomicMassTf.setText("");
			halfLifeTf.setText("");
			// do not shutdown derby...it will be closed at frame exit!

			if (psInsert != null)
				psInsert.close();
			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Delete and update data from main table and from the connected secondary
	 * table.
	 */
	private void deleteMain() {
		int selID = 0;
		int selRow = mainTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) mainTable.getValueAt(selRow, 
					dbagent.getPrimaryKeyColumnIndex());													
																	
		} else {
			return;
		}
		// deleteRecord
		dbagent.delete(Convertor.intToString(selID));//, orderbyS);
		//dbagent.performSelection(orderbyS);
		// now delete from nested
		nesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		nesteddbagent.delete(gammaNuclidesDetailsTable, IDlink, Convertor.intToString(selID));
		//now from other nested tables having no agents
		nesteddbagent.delete(gammaNuclidesDecayChainTable, IDlink, Convertor.intToString(selID));
		nesteddbagent.delete(gammaNuclidesCoincidenceTable, IDlink, Convertor.intToString(selID));
		
		//selection is done at the end
		dbagent.performSelection(orderbyS);
		/*try {
			// prepare db query data
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;
			// make a connection
			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			// not work in derby!!
			// String s = "delete * from " + gammaNuclidesTable;
			// DBOperation.deletealter(s, con1);

			JTable aspTable = asp.getTab();
			int rowTableCount = aspTable.getRowCount();// =MAX ID!!

			int selID = 0;// NO ZERO ID
			int selRow = aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) aspTable.getValueAt(selRow, 0);
			} else {
				if (con1 != null)
					con1.close();
				return;// nothing to delete
			}

			Statement s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			ResultSet res = s.executeQuery("SELECT * FROM "
					+ gammaNuclidesTable);
			PreparedStatement psUpdate = null;
			while (res.next()) {
				int id = res.getInt("ID");
				if (id == selID) {
					res.deleteRow();
				} else if (id > selID) {
					// since in this table ID is UNIQUE we can make
					// on-the fly update
					psUpdate = con1.prepareStatement("update "
							+ gammaNuclidesTable + " set ID=? where ID=?");

					psUpdate.setInt(1, id - 1);
					psUpdate.setInt(2, id);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			}
			// now detail table
			s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM " + gammaNuclidesDetailsTable);
			while (res.next()) {
				int id = res.getInt("ID");
				if ((id == selID)) {
					res.deleteRow();
				}
			}
			// now decay table:
			s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM "
					+ gammaNuclidesDecayChainTable);
			while (res.next()) {
				int id = res.getInt("ID");
				if ((id == selID)) {
					res.deleteRow();
				}
			}
			// now coin table
			s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM "
					+ gammaNuclidesCoincidenceTable);
			while (res.next()) {
				int id = res.getInt("ID");
				if ((id == selID)) {
					res.deleteRow();
				}
			}
			// update detail table
			if (selID + 1 <= rowTableCount)
				for (int i = selID + 1; i <= rowTableCount; i++) {
					psUpdate = con1.prepareStatement("update "
							+ gammaNuclidesDetailsTable
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, i - 1);
					psUpdate.setInt(2, i);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			// update decay chain
			if (selID + 1 <= rowTableCount)
				for (int i = selID + 1; i <= rowTableCount; i++) {
					psUpdate = con1.prepareStatement("update "
							+ gammaNuclidesDecayChainTable
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, i - 1);
					psUpdate.setInt(2, i);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			// update coincidence
			if (selID + 1 <= rowTableCount)
				for (int i = selID + 1; i <= rowTableCount; i++) {
					psUpdate = con1.prepareStatement("update "
							+ gammaNuclidesCoincidenceTable
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, i - 1);
					psUpdate.setInt(2, i);

					psUpdate.executeUpdate();
					psUpdate.close();
				}

			performCurrentSelection();
			// do not shutdown derby..it will be closed at frame exit!

			if (res != null)
				res.close();
			if (s != null)
				s.close();
			if (psUpdate != null)
				psUpdate.close();
			if (con1 != null)
				con1.close();
			// local var...does not need to set them to null
			// they are automatically subject to garbage collector
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Add data to secondary table only.
	 */
	private void addSecond() {

		// String notes = notesTf.getText();
		String notes = (String) nuc2Cb.getSelectedItem();
		String radTypeS = (String) radTypeCb.getSelectedItem();
		double en = 0.0;
		double yld = 0.0;
		double decorr = 0.0;
		boolean nulneg = false;
		try {
			en = Convertor.stringToDouble(energyTf.getText());
			if (en <= 0)
				nulneg = true;
			yld = Convertor.stringToDouble(yieldTf.getText());
			if (yld <= 0)
				nulneg = true;
			decorr = Convertor.stringToDouble(decayCorrTf.getText());
			if (decorr <= 0)
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

		try {

			// prepare db query data
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = gammaDB;
			//opens = opens + file_sep + dbName;
			// make a connection
			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			// first make a selection to retrieve usefull data
			//JTable aspTable = asp.getTab();
			int mainselID = 0;// NO ZERO ID
			int mainselRow = mainTable.getSelectedRow();//aspTable.getSelectedRow();
			if (mainselRow != -1) {
				mainselID = (Integer) mainTable.getValueAt(mainselRow, 0);//aspTable.getValueAt(mainselRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to insert
			}

			//can retrieve data from table but let's do from DB..one select is not a big deal!
			//String s = "SELECT * FROM " + gammaNuclidesDetailsTable
				//	+ " where ID = " + mainselID;//
			//DatabaseAgent.select(gammadbcon, s);//DBOperation.select(s, con1);
			//orrrrrrrrrrrrrrrrrr
			nesteddbagent.setLinks(IDlink, Convertor.intToString(mainselID));
			nesteddbagent.performSelection(nestedorderbyS);
			int nrcrt = nesteddbagent.getRowsCount();//Convertor.stringToInt(nesteddbagent.getRecordsLabel().getText());
			//int nrcrt = DatabaseAgent.getRowCount() + 1;//DBOperation.getRowCount() + 1;
			nrcrt = nrcrt +1;
			// nrcrt where we make the insertion
			String[] data = new String[nesteddbagent.getAllColumnCount()];
			int kCol = 0;
			data[kCol] = Convertor.intToString(nrcrt);
			kCol++;
			data[kCol] = Convertor.intToString(mainselID);
			kCol++;
			data[kCol] = Convertor.doubleToString(en);
			kCol++;
			data[kCol] = Convertor.doubleToString(yld);
			kCol++;
			data[kCol] = notes;
			kCol++;
			data[kCol] = radTypeS;
			kCol++;
			data[kCol] = Convertor.doubleToString(decorr);
			kCol++;
			
			nesteddbagent.insertAll(data);
			
			nesteddbagent.setLinks(IDlink, Convertor.intToString(mainselID));
			nesteddbagent.performSelection(nestedorderbyS);
			//=================================
			
			//PreparedStatement psInsert = null;

			//psInsert = con1.prepareStatement("insert into "
			//		+ gammaNuclidesDetailsTable + " values "
			//		+ "(?, ?, ?, ?, ?,?,?)");

			//psInsert.setString(1, Convertor.intToString(nrcrt));
			//psInsert.setString(2, Convertor.intToString(mainselID));
			//psInsert.setString(3, Convertor.doubleToString(en));
			//psInsert.setString(4, Convertor.doubleToString(yld));
			//psInsert.setString(5, notes);
			//psInsert.setString(6, radTypeS);
			//psInsert.setString(7, Convertor.doubleToString(decorr));

			//psInsert.executeUpdate();

			// now display in this panel ONLY!:
			//String str = "select * from " + gammaNuclidesDetailsTable
			//		+ " where ID = " + mainselID + " ORDER BY NRCRT";
			//DBOperation.select(str, con1);

			//if (aspDate != null)
			//	suportSpDate.remove(aspDate);

			//aspDate = new AdvancedSelectPanel();
			//suportSpDate.add(aspDate, BorderLayout.CENTER);

			//validate();

			notesTf.setText("");
			energyTf.setText("");
			yieldTf.setText("");
			
			//if (psInsert != null)
				//psInsert.close();
			//if (con1 != null)
				//con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Delete and update data from secondary table only.
	 */
	private void deleteSecond() {		
		//no choice here but to update to be in order.
		//the reason: adding data is based on rowCount, so we always must have 1,2,..
		//the reason for adding based on RowCount: no valid PriKey column in nested table!
		//not huge data we can afford updates and also huge enough to not have a prikey nrcrt.
		//imagine data 1,345,378,...ugly!
		try {
			// prepare db query data
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = gammaDB;
			//opens = opens + file_sep + dbName;
			// make a connection
			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			//JTable aspTable = asp.getTab();
			int mainselID = 0;// NO ZERO ID
			int mainselRow = mainTable.getSelectedRow();//aspTable.getSelectedRow();
			if (mainselRow != -1) {
				mainselID = (Integer) mainTable.getValueAt(mainselRow,0);//aspTable.getValueAt(mainselRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to delete
			}

			//JTable aspDateTable = aspDate.getTab();
			int rowTableCount = nestedTable.getRowCount();//aspDateTable.getRowCount();
			// here ID means NRCRT
			int selID = 0;// NO ZERO NRCRT
			int selRow = nestedTable.getSelectedRow();//aspDateTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) nestedTable.getValueAt(selRow,0);//aspDateTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to delete
			}

			//nesteddbagent.setLinks(IDlink, Convertor.intToString(mainselID));
			//nesteddbagent.performSelection(nestedorderbyS);
			
			//nesteddbagent.delete(Convertor.intToString(selID));
			
			Statement s = gammadbcon.createStatement(ResultSet.TYPE_FORWARD_ONLY,//con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			ResultSet res = s.executeQuery("SELECT * FROM "
					+ gammaNuclidesDetailsTable + " where ID = " + mainselID);

			PreparedStatement psUpdate = null;

			while (res.next()) {
				int id = res.getInt("NRCRT");
				if (id == selID) {
					res.deleteRow();
					break;//optimization!#!
				}
			}
			if (selID + 1 <= rowTableCount)
				for (int i = selID + 1; i <= rowTableCount; i++) {
					psUpdate = gammadbcon.prepareStatement("update "//con1.prepareStatement("update "
							+ gammaNuclidesDetailsTable
							+ " set NRCRT=? where NRCRT=? AND ID = "
							+ mainselID);

					psUpdate.setInt(1, i - 1);
					psUpdate.setInt(2, i);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			// now display in this panel ONLY!:
			nesteddbagent.setLinks(IDlink, Convertor.intToString(mainselID));
			nesteddbagent.performSelection(nestedorderbyS);
			//String str = "select * from " + gammaNuclidesDetailsTable
				//	+ " where ID = " + mainselID + " ORDER BY NRCRT";
			//DBOperation.select(str, con1);

			//if (aspDate != null)
				//suportSpDate.remove(aspDate);

			//aspDate = new AdvancedSelectPanel();
			//suportSpDate.add(aspDate, BorderLayout.CENTER);

			//validate();
			// do not shutdown derby..it will be closed at frame exit!

			if (res != null)
				res.close();
			if (s != null)
				s.close();
			if (psUpdate != null)
				psUpdate.close();
			//if (con1 != null)
				//con1.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get value from displayed table. This operation can be performed <br>
	 * in a more "clean" way by getting value from DATABASE instead of its <br>
	 * displayed table. Both ways are however equivalent.
	 * 
	 * @param a a
	 * @param row row
	 * @param col col
	 * @return the result
	 */
	private Object getValueAt(Vector<Object> a, int row, int col) {
		@SuppressWarnings("unchecked")
		Vector<Object> v = (Vector<Object>) a.elementAt(row);
		return v.elementAt(col);
	}

	/**
	 * Copy data from master library to the "in-use" library.
	 */
	private void copy() {

		// System.out.println("copy");
				
		try {
			// prepare db query data
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = gammaDB;
			//opens = opens + file_sep + dbName;
			// make a connection
			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			// first make a selection to retrieve usefull data
			//String s = "select * from " + gammaNuclidesTable;
			//DBOperation.select(s, con1);

			//int id = DBOperation.getRowCount() + 1;// id where we make the
													// insertion

			PreparedStatement psInsert = null;
			if (!nuclideInfo.equals("")) {
				String[] data = new String[dbagent.getUsefullColumnCount()];
				int kCol = 0;
				data[kCol] = nuclideInfo;
				kCol++;
				data[kCol] = Convertor.doubleToString(atomicMassInfo);
				kCol++;
				data[kCol] = Convertor.doubleToString(halfLifeInfo);
				kCol++;
				data[kCol] = halfLifeUnitsInfo;
				kCol++;
				data[kCol] = decayCorrInfo;
				kCol++;
				dbagent.insert(data);
				//psInsert = con1.prepareStatement("insert into "
					//	+ gammaNuclidesTable + " values "
						//+ "(?, ?, ?, ?, ?, ?)");
				//psInsert.setString(1, Convertor.intToString(id));
				//psInsert.setString(2, nuclideInfo);
				//psInsert.setString(3, Convertor.doubleToString(atomicMassInfo));
				//psInsert.setString(4, Convertor.doubleToString(halfLifeInfo));
				//psInsert.setString(5, halfLifeUnitsInfo);
				//psInsert.setString(6, decayCorrInfo);
				//psInsert.executeUpdate();
			} else {
				return;//if here...getNucInfo has not complete!!!!
			}

			if (energyInfo != null) {
				// /////////////sorting
				int ncols = 5;// en,y,notes,rtype,decaycorr
				int nrows = energyInfo.size();
				Vector<String> rowRadInfo = new Vector<String>();
				Vector<Object> radInfo = new Vector<Object>();

				for (int i = 0; i < nrows; i++) {
					rowRadInfo = new Vector<String>();
					rowRadInfo.addElement(energyInfo.elementAt(i).toString());
					rowRadInfo.addElement(yieldInfo.elementAt(i).toString());
					rowRadInfo.addElement(notesInfo.elementAt(i));

					rowRadInfo.addElement(radiationTypeInfo.elementAt(i));
					rowRadInfo.addElement(decayCorr.elementAt(i).toString());

					radInfo.addElement(rowRadInfo);
				}
				// radInfo will be sorted!!!
				Sort.qSort(radInfo, ncols, 0);// after energy
				// /////////////////
				//psInsert = con1.prepareStatement("insert into "
					//	+ gammaNuclidesDetailsTable + " values "
						//+ "(?, ?, ?, ?, ?, ?, ?)");
				int id = dbagent.getAIPrimaryKeyValue();
				for (int i = 0; i < energyInfo.size(); i++) {
					int nrcrt = i + 1;
					String[] data = new String[nesteddbagent.getAllColumnCount()];
					int kCol = 0;
					data[kCol] = Convertor.intToString(nrcrt);
					kCol++;
					data[kCol] = Convertor.intToString(id);
					kCol++;
					data[kCol] = getValueAt(radInfo, i, 0).toString();
					kCol++;
					data[kCol] = getValueAt(radInfo, i, 1).toString();
					kCol++;
					data[kCol] = getValueAt(radInfo, i, 2).toString();
					kCol++;
					data[kCol] = getValueAt(radInfo, i, 3).toString();
					kCol++;
					data[kCol] = getValueAt(radInfo, i, 4).toString();
					kCol++;
					
					nesteddbagent.insertAll(data);
					//psInsert.setString(1, Convertor.intToString(nrcrt));
					//psInsert.setString(2, Convertor.intToString(id));
					//psInsert.setString(3, getValueAt(radInfo, i, 0).toString());
					//psInsert.setString(4, getValueAt(radInfo, i, 1).toString());
					//psInsert.setString(5, getValueAt(radInfo, i, 2).toString());

					//psInsert.setString(6, getValueAt(radInfo, i, 3).toString());
					//psInsert.setString(7, getValueAt(radInfo, i, 4).toString());
					//psInsert.executeUpdate();
				}
			}
			// now decay chain info table=>use in COIN correction!!
			//no agents here
			if (nuclides_all != null) {
				psInsert = gammadbcon.prepareStatement("insert into "//con1.prepareStatement("insert into "
						+ gammaNuclidesDecayChainTable + " values "
						+ "(?, ?, ?, ?)");
				int id = dbagent.getAIPrimaryKeyValue();
				for (int i = 0; i < nuclides_all.length; i++) {
					int nrcrt = i + 1;
					psInsert.setString(1, Convertor.intToString(nrcrt));
					psInsert.setString(2, Convertor.intToString(id));
					psInsert.setString(3, nuclides_all[i]);
					psInsert.setString(4,
							Convertor.doubleToString(yield_all[i]));
					psInsert.executeUpdate();
				}
			}

			//performCurrentSelection();
			dbagent.performSelection(orderbyS);

			// some finalisations:
			energyInfo = null;
			yieldInfo = null;
			notesInfo = null;
			radiationTypeInfo = null;
			decayCorr = null;

			nuclides_all = null;
			yield_all = null;

			if (psInsert != null)
				psInsert.close();
			//if (con1 != null)
				//con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Display data.
	 */
	/*private void performCurrentSelection() {
		// of course, this solution is BARBARIC!!
		// we remove a panel, perform a SELECT statement
		// and rebuild the panel containing the select result.
		// An adequate solution must skip the time-consuming
		// SELECT statement (on huge database)
		// and handle INSERT/delete or update statements at
		// database level!!! Anyway, only INSERT statement
		// could be improved since a DELETE statement often
		// requires an ID update therefore a whole database
		// table scan is required so this solution is quite
		// good enough!
		// However, we don't make art here, we make science!:P
		suportSp.remove(asp);
		performQueryDb();
		validate();
	}*/

	/**
	 * Retrieving nuclide data from master database.
	 */
	private void getNuclideInfo() {
		resetAll();
		textArea.selectAll();
		textArea.replaceSelection("");
		if (a_secRb.isSelected()) {
			IA_use = IA_sec;
		}
		if (a_afterRb.isSelected()) {
			IA_use = IA_elapsed;
		}
		if (a_eqRb.isSelected()) {
			IA_use = IA_eq;
		}

		double sampleTime = 0.0;

		String s = "";
		try {
			sampleTime = Convertor.stringToDouble(sampleTimeTf.getText());
		} catch (Exception e) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			stopThread();// kill all threads
			statusL.setText(resources.getString("status.done"));
			e.printStackTrace();
			return;
		}

		if (sampleTime <= 0) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			stopThread();// kill all threads
			statusL.setText(resources.getString("status.done"));

			return;
		}

		s = (String) sampleHalfLifeUnitsCb.getSelectedItem();
		elapsedTime = formatHalfLife(sampleTime, s);

		sampleTimeS = sampleTimeTf.getText();
		sampleTimeUnitsS = s;

		yield_current = 1.0;
		parentNuclide = (String) nucCb.getSelectedItem();

		colect_first_time = true;
		parent_dual = false;

		setMasterTables();

		ndaughter = 0;// init
		nuc_chains = new String[nmax_daughters][nmax_chains];
		hlu_chains = new String[nmax_daughters][nmax_chains];
		am_chains = new double[nmax_daughters][nmax_chains];
		hl_chains = new double[nmax_daughters][nmax_chains];
		br_chains = new double[nmax_daughters][nmax_chains];
		a_chains = new double[nmax_daughters][nmax_chains];
		at_chains = new double[nmax_daughters][nmax_chains];
		a_chains_equilibrum = new double[nmax_daughters][nmax_chains];
		nchain = 0;// init
		ndaughter = 0;// init
		ndaughterChainMax = 0;// init
		timeSteps = 1;

		conn = null;
		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = icrpDB;
		opens = opens + file_sep + dbName;

		statements = new ArrayList<Statement>();
		resultsets = new ArrayList<ResultSet>();
		try {

			conn = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");

			collectDecayData(parentNuclide);
			if (STOPCOMPUTATION)return;
			// put all together removing duplicates and creating chains!!
			formatDecay();
			if (STOPCOMPUTATION)return;
			decayInfo();
			saveActivitiesSecularAndPrepareChainData();

			for (int j = 0; j <= nchain; j++) {
				computeChainActivity(j);
			}
			saveActivitiesChain();

			getRadiationYields();
			if (STOPCOMPUTATION)return;
			displayRadiation_PHOTON();

			// derby will be closed at frame exit!

		} catch (Exception ex) {
			stopThread();// kill all threads
			statusL.setText(resources.getString("status.done"));
			ex.printStackTrace();
		} finally {
			// release all open resources to avoid unnecessary memory usage
			// ResultSet
			int j = 0;
			while (!resultsets.isEmpty()) {
				ResultSet rs = (ResultSet) resultsets.remove(j);
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
				} catch (SQLException sqle) {
					printSQLException(sqle);
					stopThread();// kill all threads
					statusL.setText(resources.getString("status.done"));
				}
			}
			// Statements and PreparedStatements
			int i = 0;
			while (!statements.isEmpty()) {
				// PreparedStatement extend Statement
				Statement st = (Statement) statements.remove(i);
				try {
					if (st != null) {
						st.close();
						st = null;
					}
				} catch (SQLException sqle) {
					printSQLException(sqle);
					stopThread();// kill all threads
					statusL.setText(resources.getString("status.done"));
				}
			}
			// Connection
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
				stopThread();// kill all threads
				statusL.setText(resources.getString("status.done"));
			}
		}
		// setup necessary variable and
		// remove unused objects from memory!
		performCleaningUp();
		stopThread();// kill all threads
		statusL.setText(resources.getString("status.done"));

		// System.out.println(decayCorrInfo);
	}

	/**
	 * Save chain activities computed in default equilibrium condition (after a time 
	 * equal to 10 times the parent half life). Called by saveActivitiesChain.
	 */
	private void saveActivitiesChain_Equilibrum() {
		double norm = 1.0;
		// we have nuclideS...nuclides_all AND we have
		// nuc_chains and a_chains[][]
		if (nchain > 0)// 0,1 we have at least 2 chains
		{
			for (int i = 0; i < nuclides_all.length; i++) {
				double[] a_temp = new double[nchain + 1];
				for (int k = 0; k <= nchain; k++) {
					for (int j = 0; j < ndaughterChainMax; j++) {
						if (k < nmax_chains)
							if (nuclides_all[i].equals(nuc_chains[j][k])) {
								a_temp[k] = a_chains_equilibrum[j][k];
								// each nuclides has 1 occurance in each chain
								// System.out.println(""+a_temp[k]);
								break;
							}
					}
				}
				// look for duplicates!!
				Vector<Integer> dup = new Vector<Integer>();
				boolean foundB = false;
				for (int k = 0; k < nchain; k++) {
					if (k < nmax_chains) {
						foundB = false;
						int dupN = dup.size();
						if (dupN > 0) {
							for (int n = 0; n < dupN; n++) {
								Integer itg = (Integer) dup.elementAt(n);
								int indx = itg.intValue();
								if (k == indx) {
									foundB = true;// we have a skip
									break;
								}
							}
						}
						if (!foundB) {
							for (int l = k + 1; l <= nchain; l++) {
								if (a_temp[k] == a_temp[l]) {
									dup.addElement(new Integer(l));
								}
							}
						}
					}
				}
				// //END...look for duplicates!!

				if (dup.size() > 0)
					for (int k = 0; k < dup.size(); k++) {
						Integer itg = (Integer) dup.elementAt(k);
						int indx = itg.intValue();
						a_temp[indx] = 0.0;
					}

				double sum = 0.0;
				for (int k = 0; k <= nchain; k++) {
					if (k < nmax_chains) {
						// System.out.println(""+a_temp[k]);
						sum = sum + a_temp[k];
					}
				}

				activity_chain_equilibrum[i] = sum;

				norm = activity_chain_equilibrum[0];
				// System.out.println(" N: "+nuclides_all[i]+" Y: "+formatNumber(yield_all[i])+
				// " A: "+formatNumberScientific(activity_secular[i])+
				// " Ach: "+formatNumberScientific(activity_chain[i])+
				// " Ach_scaled: "+formatNumberScientific(activity_chain_scaled[i])
				// );

			}// for
				// normalise:
			for (int i = 0; i < nuclides_all.length; i++) {
				if (norm != 0.0)
					activity_chain_equilibrum[i] = activity_chain_equilibrum[i]
							/ norm;
				else
					activity_chain_equilibrum[i] = 0.0;
			}

		} else// if (nchain=0)
		{
			for (int i = 0; i < nuclides_all.length; i++) {
				int k = nchain;// aka 0 here!
				for (int j = 0; j < ndaughterChainMax; j++) {
					if (k < nmax_chains)
						if (nuclides_all[i].equals(nuc_chains[j][k])) {
							activity_chain_equilibrum[i] = a_chains_equilibrum[j][k];
							norm = activity_chain_equilibrum[0];
							// System.out.println(" N: "+nuclides_all[i]+" Y: "+yield_all[i]+" A: "+activity_secular[i]+
							// " Ach: "+activity_chain[i]+" Ach_scaled: "+activity_chain_scaled[i]);
							break;
						}
				}

			}
			// normalise:
			for (int i = 0; i < nuclides_all.length; i++) {
				if (norm != 0.0)
					activity_chain_equilibrum[i] = activity_chain_equilibrum[i]
							/ norm;
				else
					activity_chain_equilibrum[i] = 0.0;
			}

		}
	}

	/**
	 * Save chain activities.
	 */
	private void saveActivitiesChain() {
		double norm = 1.0;
		String s = "";
		saveActivitiesChain_Equilibrum();
		s = "-------------------------------" + " \n";
		textArea.append(s);
		if (IA_use == IA_sec)// secular
		{
			s = resources.getString("text.corr.type")
					+ resources.getString("db.equilibrum.secular");
		} else if (IA_use == IA_elapsed)// known sample time
		{
			s = resources.getString("text.corr.type")
					+ resources.getString("db.equilibrum.sample") + sampleTimeS
					+ "_" + sampleTimeUnitsS;
		} else if (IA_use == IA_norm) {
			// not used here!!
		} else if (IA_use == IA_eq)// equilibrum reached at 10 x parent half
									// life
		{
			s = resources.getString("text.corr.type")
					+ resources.getString("db.equilibrum.default");
		}
		s = s + "\n";
		textArea.append(s);
		s = "\n";
		textArea.append(s);
		// we have nuclideS...nuclides_all AND
		// we have nuc_chains and a_chains[][]
		if (nchain > 0)// 0,1 we have at least 2 chains
		{
			for (int i = 0; i < nuclides_all.length; i++) {
				double[] a_temp = new double[nchain + 1];
				double[] at_temp = new double[nchain + 1];
				for (int k = 0; k <= nchain; k++) {
					for (int j = 0; j < ndaughterChainMax; j++) {
						if (k < nmax_chains)
							if (nuclides_all[i].equals(nuc_chains[j][k])) {
								a_temp[k] = a_chains[j][k];
								// each nuclides has 1 occurance in each chain
								at_temp[k] = at_chains[j][k];
								// each nuclides has 1 occurance in each chain
								// System.out.println(""+a_temp[k]);
								break;
							}
					}
				}
				// look for duplicates!!
				Vector<Integer> dup = new Vector<Integer>();
				boolean foundB = false;
				for (int k = 0; k < nchain; k++) {
					if (k < nmax_chains) {
						foundB = false;
						int dupN = dup.size();
						if (dupN > 0) {
							for (int n = 0; n < dupN; n++) {
								Integer itg = (Integer) dup.elementAt(n);
								int indx = itg.intValue();
								if (k == indx) {
									foundB = true;// we have a skip
									break;
								}
							}
						}
						if (!foundB) {
							for (int l = k + 1; l <= nchain; l++) {
								if (a_temp[k] == a_temp[l]) {
									dup.addElement(new Integer(l));
								}
							}
						}
					}
				}
				// //END...look for duplicates!!

				if (dup.size() > 0)
					for (int k = 0; k < dup.size(); k++) {
						Integer itg = (Integer) dup.elementAt(k);
						int indx = itg.intValue();
						a_temp[indx] = 0.0;
						at_temp[indx] = 0.0;
					}

				double sum = 0.0;
				double sumt = 0.0;
				for (int k = 0; k <= nchain; k++) {
					if (k < nmax_chains) {
						// System.out.println(""+a_temp[k]);
						sum = sum + a_temp[k];
						sumt = sumt + at_temp[k];
					}
				}

				activity_chain[i] = sum;
				activity_chain_scaled[i] = sumt;

				norm = activity_chain[0];

			}// for (int i=0;i<nuclides_all.length;i++)

			for (int i = 0; i < nuclides_all.length; i++) {
				if (norm != 0.0)
					activity_chain[i] = activity_chain[i] / norm;
				else
					activity_chain[i] = 0.0;
				computeCorrection(i);
				s = resources.getString("text.nuclide")
						+ // "Nuclide: "+
						nuclides_all[i]
						+ "; "
						+ resources.getString("text.branchingRatio")
						+ // "; Yield: "+
							// Convertor.formatNumber(yield_all[i],5)+"; "+
						Convertor.formatNumberScientific(yield_all[i])
						+ "; "
						+ resources.getString("text.activitySecular")
						+ // "; Activ.secular: "+
						Convertor.formatNumberScientific(activity_secular[i])
						+ "; "
						+ resources.getString("text.activitySample")
						+ // "; Activ.elapsed: "+
						Convertor.formatNumberScientific(activity_chain[i])
						+ "; "
						+
						// "; Time-integrated activ: "+
						// Convertor.formatNumberScientific(activity_chain_scaled[i])+
						resources.getString("text.activityEquilibrum")
						+ // "; Activ.equilibrum: "+
						Convertor
								.formatNumberScientific(activity_chain_equilibrum[i])
						+ "; "
						+ resources.getString("text.decayCorr")
						+ // "; DecayCorr: "+
						Convertor
								.formatNumberScientific(parentDaughterDecayCorr[i])
						+ " \n";
				textArea.append(s);
			}
		} else// if (nchain=0)
		{
			for (int i = 0; i < nuclides_all.length; i++) {
				int k = nchain;// aka 0 here!
				for (int j = 0; j < ndaughterChainMax; j++) {
					if (k < nmax_chains)
						if (nuclides_all[i].equals(nuc_chains[j][k])) {
							activity_chain[i] = a_chains[j][k];
							activity_chain_scaled[i] = at_chains[j][k];

							norm = activity_chain[0];

							break;
						}
				}

			}

			for (int i = 0; i < nuclides_all.length; i++) {
				if (norm != 0.0)
					activity_chain[i] = activity_chain[i] / norm;
				else
					activity_chain[i] = 0.0;
				computeCorrection(i);
				s = resources.getString("text.nuclide")
						+ // "Nuclide: "+
						nuclides_all[i]
						+ "; "
						+ resources.getString("text.branchingRatio")
						+ // "; Yield: "+
							// Convertor.formatNumber(yield_all[i],5)+"; "+
						Convertor.formatNumberScientific(yield_all[i])
						+ "; "
						+ resources.getString("text.activitySecular")
						+ // "; Activ.secular: "+
						Convertor.formatNumberScientific(activity_secular[i])
						+ "; "
						+ resources.getString("text.activitySample")
						+ // "; Activ.elapsed: "+
						Convertor.formatNumberScientific(activity_chain[i])
						+ "; "
						+
						// "; Time-integrated activ: "+
						// Convertor.formatNumberScientific(activity_chain_scaled[i])+
						resources.getString("text.activityEquilibrum")
						+ // "; Activ.equilibrum: "+
						Convertor
								.formatNumberScientific(activity_chain_equilibrum[i])
						+ "; "
						+ resources.getString("text.decayCorr")
						+ // "; DecayCorr: "+
						Convertor
								.formatNumberScientific(parentDaughterDecayCorr[i])
						+ " \n";
				textArea.append(s);
			}
		}
		// END
	}

	/**
	 * Compute decay correction. Called by saveActivitiesChain.<br>
	 * Gamma spectrometry involves retrieving the parent activity based on
	 * net area computation of a ROI around a peak:
	 * Aparent=NetAreaRate/(peakEff x radiationYield) where radiationYield =
	 * BR x GammaYield; BR is the branching ratio of daughter-nuclide which
	 * present the gamma line having the yield GammaYield. Therefore, in
	 * fact it is computed: Aparent=Adaughter/BR. But Adaughter=Aparent x BR
	 * only in case of secular equilibrum therefore the standard gamma
	 * activity approach suppose that secular equilibrum is reach! <br>
	 * 
	 * In reality, the secular equilibrum may never be reach and a
	 * correction should be made according to several options: 1. Secular
	 * equilibrum is admitted, therefore no correction is made 2. According to the
	 * known SAMPLE TIME, which is the time zero where in sample there is
	 * only the chain PARENT!! and no daughters. Computation are made based
	 * on the true BATEMAN decay law. 3. The sample time it is not known,
	 * but we are sure there is an equilibrum in sample but not sure if it
	 * is secular or some other. In this case, we assume that the sample-time is
	 * 10 x parent half-life, time sufficient long for any equilibrum to be
	 * reached. Computation are made based on BATEMAN decay law!<br>
	 * 
	 * Correction is made by:
	 * 
	 * If Aparent = 1 then in fact we have Adaughter=f instead of
	 * Adaughter=Aparent x BR. So:AparentNew=Adaughter/f instead of
	 * AparentOld=Adaughter/BR. Therefore:AparentNew=AparentOld x BR/f or:
	 * New=Old/(f/BR). <br>
	 * 
	 * CORR=f/BR !!!! and it is made by dividing the observed data (such as
	 * netArea) by CORR!
	 * @param index the index of nuclide
	 */
	private void computeCorrection(int index) {
		/*
		 * Gamma spectrometry involve retrieving the parent activity based on
		 * net area computation of a ROI around a peak:
		 * Aparent=NetAreaRate/(peakEff x radiationYield) where radiationYield =
		 * BR x GammaYield; BR is the branching ratio of daughter-nuclide which
		 * present the gamma line having the yield GammaYield. Therefore, in
		 * fact it is computed: Aparent=Adaughter/BR. But Adaughter=Aparent x BR
		 * only in case of secular equilibrum therefore the standard gamma
		 * activity approach suppose that secular equilibrum is reach!!!!
		 * 
		 * In reality, the secular equilibrum may never be reach and a
		 * correction should be made according to several options: 1. Secular
		 * equilibrum is admitted=>no correction is made 2. According to the
		 * known SAMPLE TIME, which is the time zero where in sample there is
		 * only the chain PARENT!! and no daughters. Computation are made based
		 * on the true BATEMAN decay law. 3. The sample time it is not known,
		 * but we are sure there is an equilibrum in sample but not sure if it
		 * is secular or some other. In this case, we assume that the sample-time is
		 * 10 x parent half-life, time sufficient long for any equilibrum to be
		 * reached. Computation are made based on BATEMAN decay law!
		 * 
		 * Correction is made by:
		 * 
		 * If Aparent = 1=> in fact we have Adaughter=f instead of
		 * Adaughter=Aparent x BR. So:AparentNew=Adaughter/f instead of
		 * AparentOld=Adaughter/BR. Therefore:AparentNew=AparentOld x BR/f or:
		 * New=Old/(f/BR);
		 * 
		 * CORR=f/BR !!!! and it is made by dividing the observed data (such as
		 * netArea) by CORR!
		 */
		// retrieve the yield:
		Double yD = (Double) yieldD.elementAt(index);
		double BR = yD.doubleValue();
		double f = 0.0;
		if (IA_use == IA_sec)// secular
		{
			f = activity_secular[index];
		} else if (IA_use == IA_elapsed)// known sample time
		{
			f = activity_chain[index];
		} else if (IA_use == IA_norm) {
			// f=activity_chain_scaled[index];
			// not really scalled to parent, not used here!!
		} else if (IA_use == IA_eq)// equilibrum reached at 10 x parent half
									// life
		{
			f = activity_chain_equilibrum[index];
		}
		if (f > 0 && BR > 0)
			parentDaughterDecayCorr[index] = f / BR;
		else
			parentDaughterDecayCorr[index] = 1.0;// no corr

	}

	/**
	 * Save activities in secular equilibrium condtion. it also initialize some 
	 * global variables.
	 */
	private void saveActivitiesSecularAndPrepareChainData()// and transient
	{
		int nnuc = nuclideS.size();
		nuclides_all = new String[nnuc];
		yield_all = new double[nnuc];
		activity_secular = new double[nnuc];
		activity_chain_equilibrum = new double[nnuc];
		activity_chain_scaled = new double[nnuc];
		activity_chain = new double[nnuc];
		parentDaughterDecayCorr = new double[nnuc];

		for (int i = 0; i < nnuc; i++) {
			String nuc = (String) nuclideS.elementAt(i);
			Double yD = (Double) yieldD.elementAt(i);
			double yi = yD.doubleValue();

			nuclides_all[i] = nuc;
			yield_all[i] = yi;
			activity_secular[i] = activityUnit * yield_all[i];
		}
	}

	/**
	 * Computes chain activities using Bateman decay law.
	 * @param ichain index of the chain
	 */
	private void computeChainActivity(int ichain) {
		if (ichain >= nmax_chains) {
			return;
		}
		// String s="";
		int nnuc = 0;
		for (int i = 0; i < ndaughterChainMax; i++) {
			nnuc++;// 1 nuclide always

			if (nuc_chains[i][ichain].equals("-1")) {
				nnuc--;
				break;
			}
		}

		String[] nucs = new String[nnuc];
		double[] branchingRatio = new double[nnuc];
		double[] halfLife = new double[nnuc];
		double[] atomicMass = new double[nnuc];
		double[] initialMass = new double[nnuc];
		double[] initialNumberAtomsFromcSource = new double[nnuc];

		for (int i = 0; i < nnuc; i++) {
			nucs[i] = nuc_chains[i][ichain];
			branchingRatio[i] = br_chains[i][ichain];
			halfLife[i] = formatHalfLife(hl_chains[i][ichain],
					hlu_chains[i][ichain]);
			atomicMass[i] = am_chains[i][ichain];
			// compute initial mass number
			double dc = Math.log(2.0) / halfLife[i];
			double navogadro = 6.02214199E26;// atoms/kmol!!
			if (i == 0)
				initialMass[i] = activityUnit * atomicMass[i]
						/ (navogadro * dc);// only parent
			else
				initialMass[i] = 0.0;
			// System.out.println(" "+nucs[i]+"  mm "+initialMass[i]+"  hl "+
			// halfLife[i]+"  BR "+branchingRatio[i]
			// +"  "+hl_chains[i][ichain]+hlu_chains[i][ichain]+"   "+atomicMass[i]);
			initialNumberAtomsFromcSource[i] = 0.0;// no external sources
		}

		double[] dc = PhysUtilities.computeDecayConstant(halfLife);
		double[] pdc = PhysUtilities.computePartialDecayConstant(halfLife,
				branchingRatio);
		double[] initialNuclideNumbers = PhysUtilities
				.computeInitialNuclideNumbers(initialMass, atomicMass);

		PhysUtilities.steps = timeSteps;
		PhysUtilities.t = elapsedTime;
		// compute===================================================================
		PhysUtilities.bateman(nnuc, dc, pdc, initialNuclideNumbers,
				initialNumberAtomsFromcSource);

		double[] at = new double[PhysUtilities.at.length];
		double[] att = new double[PhysUtilities.att.length];
		// double t=PhysUtilities.t;
		// int steps=PhysUtilities.steps;
		int j = PhysUtilities.steps;

		for (int i = 0; i < PhysUtilities.at.length; i++) {
			at[i] = PhysUtilities.at[i][j];
			att[i] = PhysUtilities.att[i][j];
			// System.out.println("Nuclide: "+nucs[i]+"; Integrated Activ: "+att[i]);
		}
		// ===============compute activities at equilibrum regarded as being at
		// T1/2 of parent!===
		PhysUtilities.steps = 1;
		PhysUtilities.t = timesHalfLifeForEq * halfLife[0];// parent
															// half-life===always
															// is any chain
		// compute ONE MORE TIME
		PhysUtilities.bateman(nnuc, dc, pdc, initialNuclideNumbers,
				initialNumberAtomsFromcSource);

		// s="-------------------------------"+" \n";
		// textArea.append(s);

		for (int i = 1; i <= nnuc; i++) {
			a_chains_equilibrum[i - 1][ichain] = PhysUtilities.at[i - 1][j];
			if (a_chains_equilibrum[i - 1][ichain] < 0.0)
				a_chains_equilibrum[i - 1][ichain] = 0.0;// residual error
		}

		for (int i = 1; i <= nnuc; i++) {
			a_chains[i - 1][ichain] = at[i - 1];// PhysUtilities.at[i-1][j];
			at_chains[i - 1][ichain] = att[i - 1];// PhysUtilities.at[i-1][j];
			if (a_chains[i - 1][ichain] < 0.0)
				a_chains[i - 1][ichain] = 0.0;// residual error
			if (at_chains[i - 1][ichain] < 0.0)
				at_chains[i - 1][ichain] = 0.0;// residual error

			// ======================================================
			// s="Nuclide: "+nucs[i-1]+"; Activ. [Bq]: "+
			// Convertor.formatNumberScientific(a_chains[i-1][ichain])+
			// "; at time [s]: "+j*t/steps+"; Activ.equilibrum [Bq]: "+
			// Convertor.formatNumberScientific(a_chains_equilibrum[i-1][ichain])+
			// " Integral [Bq x s]= "+
			// Convertor.formatNumberScientific(at_chains[i-1][ichain])+" \n";
			// textArea.append(s);

			// }

		}
	}

	/**
	 * Reset some variables.
	 */
	private void performCleaningUp() {
		y1_old = null;
		y2_next_chain = null;
		y1_fix = null;
		index = null;
		nucV = null;

		radLocI = null;
		radLocN = null;
		// --------------------------saving some data
		nuclideInfo = nuclideS.elementAt(0);
		atomicMassInfo = atomicMassD.elementAt(0);
		halfLifeInfo = halfLifeD.elementAt(0);
		halfLifeUnitsInfo = halfLifeUnitsS.elementAt(0);
		decayCorrInfo = "";
		if (IA_use == IA_sec)// secular
		{
			decayCorrInfo = resources.getString("db.equilibrum.secular");
		} else if (IA_use == IA_elapsed)// known sample time
		{
			decayCorrInfo = resources.getString("db.equilibrum.sample")
					+ sampleTimeS + "_" + sampleTimeUnitsS;
		} else if (IA_use == IA_norm) {
			// not used here!!
		} else if (IA_use == IA_eq)// equilibrum reached at 10 x parent half
									// life
		{
			decayCorrInfo = resources.getString("db.equilibrum.default");
		}
		// -------------------------------------
		nuclideS = null;
		atomicMassD = null;
		yieldD = null;
		halfLifeD = null;
		halfLifeUnitsS = null;

		rcode = null;
		rtype = null;
		ryield = null;
		renergy = null;
		nucInChain = null;

		nsave = null;
		yield2_readD = null;

		nuc_chains = null;
		hlu_chains = null;
		hl_chains = null;
		br_chains = null;
		am_chains = null;
		a_chains = null;
		at_chains = null;
		a_chains_equilibrum = null;
		// nuclides_all=null;//used in copy
		// yield_all=null;//used in copy

		activity_chain = null;
		activity_secular = null;
		activity_chain_scaled = null;
		activity_chain_equilibrum = null;
		parentDaughterDecayCorr = null;
	}

	/**
	 * Retrieve radiation intensities.
	 */
	private void getRadiationYields() {
		int nnuc = nuclideS.size();
		int ndata = 0;
		nucInChain = new String[nnuc];
		for (int i = 0; i < nnuc; i++) {
			if (STOPCOMPUTATION)return;
			nucInChain[i] = (String) nuclideS.elementAt(i);
			Integer rn = (Integer) radLocN.elementAt(i);
			int rln = rn.intValue();
			ndata = Math.max(ndata, rln);
		}

		rcode = new String[ndata][nnuc];
		rtype = new String[ndata][nnuc];
		ryield = new double[ndata][nnuc];
		renergy = new double[ndata][nnuc];

		for (int i = 0; i < nnuc; i++) {
			if (STOPCOMPUTATION)return;
			Integer rl = (Integer) radLocI.elementAt(i);
			int rli = rl.intValue();
			Integer rn = (Integer) radLocN.elementAt(i);
			int rln = rn.intValue();
			Double yD = (Double) yieldD.elementAt(i);
			double yi = yD.doubleValue();

			readRad(rli, rln, i, yi, ndata);
			if (STOPCOMPUTATION)return;
		}
	}

	/**
	 * Display all photon radiations.
	 */
	private void displayRadiation_PHOTON() {
		boolean firstB = false;
		energyInfo = new Vector<Double>();
		yieldInfo = new Vector<Double>();
		notesInfo = new Vector<String>();
		radiationTypeInfo = new Vector<String>();
		decayCorr = new Vector<Double>();

		if (thresh_allRb.isSelected()) {
			DTHRESH = DTHRESH0;
		} else if (thresh_1Rb.isSelected()) {
			DTHRESH = DTHRESH1;
		} else if (thresh_2Rb.isSelected()) {
			DTHRESH = DTHRESH2;
		} else if (thresh_5Rb.isSelected()) {
			DTHRESH = DTHRESH5;
		}

		double energyThreshold = 0.0;
		if (thresh_energy.isSelected()) {
			energyThreshold = (Double) resources
					.getObject("library.threshold.energy.value");
		}

		String s = "";
		for (int j = 0; j < nuclideS.size(); j++) {
			firstB = false;
			for (int i = 0; i < rcode.length; i++) {
				if (rcode[i][j].equals("-1")) {
					break;
				}

				if (((rcode[i][j].equals(Convertor.intToString(IXRAY)))
						|| (rcode[i][j].equals(Convertor.intToString(IGAMMA))) || (rcode[i][j]
						.equals(Convertor.intToString(IANNIH))))
						&& (ryield[i][j] > DTHRESH)
						&& (renergy[i][j] > energyThreshold / 1000.0)) {
					// renergy is in MeV!!!!
					if (!firstB) {
						s = "--------------------------------" + " \n";
						textArea.append(s);
						s = resources.getString("text.radiation.from")
								+ nucInChain[j] + " \n";
						textArea.append(s);

						firstB = true;
					}

					energyInfo.addElement(1000.0 * renergy[i][j]);
					yieldInfo.addElement(ryield[i][j]);
					notesInfo.addElement(nucInChain[j]);
					radiationTypeInfo.addElement(rtype[i][j]);
					decayCorr.addElement(parentDaughterDecayCorr[j]);

					s = resources.getString("text.radiation.type")
							+ rtype[i][j]
							+ "; "
							+ resources.getString("text.radiation.yield")
							+ Convertor.formatNumber(ryield[i][j], 5)
							+ "; "
							+ resources.getString("text.radiation.kev")
							+ Convertor.formatNumber(1000.0 * renergy[i][j], 2)
							+ "; "
							+ resources.getString("text.decayCorr")
							+ Convertor
									.formatNumberScientific(parentDaughterDecayCorr[j])
							+ "; " + " \n";
					textArea.append(s);

				}// if
			}// for i
		}// for j
	}

	/**
	 * Reset all variables.
	 */
	private void resetAll() {
		yield_current = 1.0;
		y1_old = new Vector<Double>();
		y2_next_chain = new Vector<Double>();
		y1_fix = new Vector<Double>();
		index = new Vector<Integer>();

		rcode = new String[0][0];
		rtype = new String[0][0];
		ryield = new double[0][0];
		renergy = new double[0][0];
		nucInChain = new String[0];

		colect_first_time = false;
		parent_dual = false;

		nuclideS = new Vector<String>();
		atomicMassD = new Vector<Double>();
		yieldD = new Vector<Double>();
		radLocI = new Vector<Integer>();
		radLocN = new Vector<Integer>();
		halfLifeD = new Vector<Double>();
		halfLifeUnitsS = new Vector<String>();
		nucV = new Vector<String>();
		parentNuclide = "";

		ndaughter = 0;
		nsave = new Vector<Integer>();
		yield2_readD = new Vector<Double>();

		nchain = 0;
		nuc_chains = new String[0][0];
		hlu_chains = new String[0][0];
		hl_chains = new double[0][0];
		br_chains = new double[0][0];
		am_chains = new double[0][0];
		ndaughterChainMax = 0;
		a_chains = new double[0][0];
		at_chains = new double[0][0];
		a_chains_equilibrum = new double[0][0];

		nuclides_all = new String[0];
		// overall nuclides..from nuclideS=new Vector();
		activity_chain = new double[0];
		// overall activities from chains at time set (at t=0, A parent =1.0)
		yield_all = new double[0];
		// overall yields..from yieldD=new Vector();
		activity_secular = new double[0];
		// formal (not happen always) forced secular equilibrum:
		// activityUnit*yield_all[i];
		activity_chain_scaled = new double[0];
		// overall normalized activities from chains at time set (at t=t, A
		// parent =1.0)
		activity_chain_equilibrum = new double[0];
		// overall normalized activities from chains at 10 x parent half life
		// (at t=t, A parent =1.0)..equilibrum auto-seek!
		parentDaughterDecayCorr = new double[0];
		IA_use = 3;
		elapsedTime = 180.0;// days
		timeSteps = 1;
	}

	/**
	 * Parent chain decay information.
	 */
	private void decayInfo() {
		textArea.selectAll();
		textArea.replaceSelection("");

		String s = "";
		s = resources.getString("text.chainDecayInfo") + " \n";
		textArea.append(s);
		;
		for (int i = 0; i < nuclideS.size(); i++) {
			Double yD = (Double) yieldD.elementAt(i);
			double yi = yD.doubleValue();
			Double hlD = (Double) halfLifeD.elementAt(i);
			double hl = hlD.doubleValue();
			String hlu = (String) halfLifeUnitsS.elementAt(i);
			Double amD = (Double) atomicMassD.elementAt(i);
			double am = amD.doubleValue();

			s = resources.getString("text.nuclide")
					+ (String) nuclideS.elementAt(i) + "; "
					+ resources.getString("text.halfLife")
					+ Convertor.formatNumberScientific(hl) + " " + hlu + "; "
					+ resources.getString("text.branchingRatio")
					// + Convertor.formatNumber(yi, 5) + "; "
					+ Convertor.formatNumberScientific(yi) + "; "
					+ resources.getString("text.atomicMass") + am + " \n";
			textArea.append(s);
		}
	}

	/**
	 * Retrieve the photon radiation type.
	 * 
	 * @param ircode ircode
	 * @return the result
	 */
	private String getRadiationType(int ircode) {
		String result = "";

		if (ircode == 1) {
			result = resources.getString("radiation.gamma");
		} else if (ircode == 2) {
			result = resources.getString("radiation.x");
		} else if (ircode == 3) {
			result = resources.getString("radiation.annihilationQuanta");
		}/*
		 * else if (ircode == 4) { result = "Beta+ particle"; } else if (ircode
		 * == 5) { result = "Beta- particle"; } else if (ircode == 6) { result =
		 * "Internal conversion electron"; } else if (ircode == 7) { result =
		 * "Auger electron"; } else if (ircode == 8) { result =
		 * "Alpha particle"; }
		 */

		return result;
	}

	/**
	 * Read radiation data from master library.
	 * 
	 * @param iradloc iradloc
	 * @param nrad nrad
	 * @param nucindex nucindex
	 * @param BR BR
	 * @param ndata ndata
	 */
	private void readRad(int iradloc, int nrad, int nucindex, double BR,
			int ndata) {
		Statement s = null;
		ResultSet rs = null;

		try {

			s = conn.createStatement();
			statements.add(s);

			String sttmnt = "select ID, NI, HY, LE" + " from " + icrpRadTable
					+ " where ID > " + Convertor.intToString(iradloc)
					+ " AND ID <= " + Convertor.intToString(iradloc + nrad);

			s.execute(sttmnt);

			rs = s.getResultSet();
			resultsets.add(rs);

			int index = 0;
			if (rs != null)
				while (rs.next()) {
					if (STOPCOMPUTATION)return;
					// String IDs=rs.getString(1);
					String IRs = rs.getString(2);
					String Ys = rs.getString(3);
					String Es = rs.getString(4);
					// System.out.println("IDs: "+IDs+" ;IR: "+IRs+" ;Ys: "+Ys+" ;Es: "+Es+
					// "  inx "+nucindex);

					rcode[index][nucindex] = IRs;
					rtype[index][nucindex] = getRadiationType(Convertor
							.stringToInt(IRs));
					double yd = Convertor.stringToDouble(Ys);
					ryield[index][nucindex] = yd * BR;
					renergy[index][nucindex] = Convertor.stringToDouble(Es);

					index++;
				}
			// ---fill with -1 the remaining rows!
			if (index < ndata) {
				for (int i = index; i < ndata; i++) {
					if (STOPCOMPUTATION)return;
					rcode[i][nucindex] = "-1";
					rtype[i][nucindex] = "-1";
					ryield[i][nucindex] = -1;
					renergy[i][nucindex] = -1;
				}
			}

		} catch (Exception err) {
			err.printStackTrace();
		}

	}

	/**
	 * Recursively collect nuclide data for both parent and daughters from radioactive chain decay. 
	 * @param nuclide the nuclide
	 */
	private void collectDecayData(String nuclide) {
		if (STOPCOMPUTATION)return;
		String input = nuclide;

		Statement s = null;
		ResultSet rs = null;

		try {

			s = conn.createStatement();
			statements.add(s);

			String stmnt = "select ID, Nuclide, HalfLife, Dau1, Dau2,"
					+ " Yield1, Yield2, AtomicMass, GammaConstant, HalfLifeUnits,"
					+ " RadLoc, RadNum, DecayMode1, DecayMode2" + " from "
					+ icrpTable + " where Nuclide = " + "'" + input + "'";
			s.execute(stmnt);

			rs = s.getResultSet();
			resultsets.add(rs);

			if (rs != null)
				while (rs.next()) {
					String NuclideS = rs.getString(2);
					double hl = Convertor.stringToDouble(rs.getString(3));
					double amass = Convertor.stringToDouble(rs.getString(8));
					String hl_units = rs.getString(10);

					int dau1 = Convertor.stringToInt(rs.getString(4));
					int dau2 = Convertor.stringToInt(rs.getString(5));

					double yield1 = Convertor.stringToDouble(rs.getString(6));
					double yield2 = Convertor.stringToDouble(rs.getString(7));

					int radloc = Convertor.stringToInt(rs.getString(11));
					int radnum = Convertor.stringToInt(rs.getString(12));
					// System.out.println("Nuc "+NuclideS+" y1 "+yield1+" y2 "+yield2);
					// ----------------------------------------------------
					if ((dau1 != 0) && (dau2 != 0))
					// we have two channels of decay for current nuclide
					{
						y1_old.addElement(new Double(yield_current));
						// old yield store
						y2_next_chain.addElement(new Double(yield2));// same
																		// size
						if (colect_first_time) {
							parent_dual = true;
						}
					}

					// current yield...always via 1st (high prob decay)
					if (yield1 == 0)
					// full decay to stable isotope..see ICRP table
					{
						yield1 = 1;
						yield2 = 0;
					}

					yield_current = yield_current * yield1;

					if ((dau1 != 0) && (dau2 != 0))
					// store the fix 2channel nuclides
					{
						y1_fix.addElement(new Double(yield_current));
						index.addElement(new Integer(nuclideS.size()));
						// ==========================
						nsave.addElement(ndaughter);
						yield2_readD.addElement(new Double(yield2));

					}

					// HERE WE COLLECT DATA WE NEED/////
					nuclideS.addElement(NuclideS);
					if (colect_first_time)
					// parent..always 1
					{
						yieldD.addElement(new Double(1.0));
						colect_first_time = false;
					} else {
						yieldD.addElement(new Double(yield_current));
					}

					atomicMassD.addElement(new Double(amass));
					radLocI.addElement(new Integer(radloc));
					radLocN.addElement(new Integer(radnum));
					halfLifeD.addElement(new Double(hl));
					halfLifeUnitsS.addElement(hl_units);
					// =======================================
					if ((nchain < nmax_chains) && (ndaughter < nmax_daughters)) {
						nuc_chains[ndaughter][nchain] = NuclideS;
						hlu_chains[ndaughter][nchain] = hl_units;
						hl_chains[ndaughter][nchain] = hl;
						am_chains[ndaughter][nchain] = amass;
						br_chains[ndaughter][nchain] = yield1;
						// start with yield 1
					}
					ndaughter++;
					ndaughterChainMax = Math.max(ndaughterChainMax, ndaughter);

					// END DATA COLLECTION/////

					if ((dau1 == 0) && (y2_next_chain.size() != 0))
					// we have a comeback: another chain!
					{
						Double y1old = (Double) y1_old
								.elementAt(y1_old.size() - 1);
						Double y2next = (Double) y2_next_chain
								.elementAt(y2_next_chain.size() - 1);
						yield_current = y1old.doubleValue();
						yield_current = yield_current * y2next.doubleValue();
						// ------------
						Double y1fix = (Double) y1_fix
								.elementAt(y1_fix.size() - 1);
						double y1fixd = y1fix.doubleValue();
						double yfix = y1fixd + yield_current;
						Integer indx = (Integer) index
								.elementAt(index.size() - 1);
						int indxi = indx.intValue();
						if ((y2_next_chain.size() == 1) && (parent_dual)) {
							// do nothin..it's parent comeback!!!
						} else {
							yieldD.setElementAt(new Double(yfix), indxi);
						}
						// ------------------------------
						// now we remove the current comeback in order to take
						// another!
						y1_old.removeElementAt(y1_old.size() - 1);
						y2_next_chain.removeElementAt(y2_next_chain.size() - 1);
						// ------
						y1_fix.removeElementAt(y1_fix.size() - 1);
						index.removeElementAt(index.size() - 1);
						// ============================
						Integer insaveI = (Integer) nsave.elementAt(nsave
								.size() - 1);
						int insave = insaveI.intValue();
						Double y2readD = (Double) yield2_readD
								.elementAt(yield2_readD.size() - 1);
						double y2read = y2readD.doubleValue();
						// System.out.println("!!!"+nucVecs+"   "+insave+"  "+ndaughter);
						yield2_readD.removeElementAt(yield2_readD.size() - 1);
						nsave.removeElementAt(nsave.size() - 1);
						nchain++;
						for (int i = 0; i <= insave; i++) {
							if ((i < nmax_daughters) && (nchain < nmax_chains)) {
								nuc_chains[i][nchain] = nuc_chains[i][nchain - 1];
								hlu_chains[i][nchain] = hlu_chains[i][nchain - 1];
								hl_chains[i][nchain] = hl_chains[i][nchain - 1];
								am_chains[i][nchain] = am_chains[i][nchain - 1];
								br_chains[i][nchain] = br_chains[i][nchain - 1];
							}
						}
						if ((insave < nmax_daughters) && (nchain < nmax_chains)) {
							br_chains[insave][nchain] = y2read;// fix last BR
						}
						ndaughter = insave + 1;
					}

					if (dau1 != 0) {

						String ids = Convertor.intToString(dau1 - 1);
						String ss = "select ID,Nuclide from " + icrpTable
								+ " where ID = " + ids;
						Statement s1 = conn.createStatement();
						s1.execute(ss);
						ResultSet rs1 = s1.getResultSet();
						if (rs1 != null)
							while (rs1.next()) {
								String output = rs1.getString(2);
								collectDecayData(output);
							}
					}

					// next in series but with LESS probability
					if (dau2 != 0) {

						String ids2 = Convertor.intToString(dau2 - 1);
						String ss2 = "select ID,Nuclide from " + icrpTable
								+ " where ID = " + ids2;
						Statement s2 = conn.createStatement();
						s2.execute(ss2);
						ResultSet rs2 = s2.getResultSet();
						if (rs2 != null)
							while (rs2.next())
							// can be rs.next()..NO LOOP!
							{
								String output2 = rs2.getString(2);
								collectDecayData(output2);
							}
					}

				}

		} catch (Exception err) {
			err.printStackTrace();
		}

	}

	/**
	 * Some finalization. Format decay by removing duplicates.
	 */
	private void formatDecay() {
		if (nuclideS.size() < 2) {
			return;// do nothing
		}

		int i = 0;
		int j = 0;
		while (i < nuclideS.size() - 1) {
			if (STOPCOMPUTATION)return;
			j = i + 1;
			while (j < nuclideS.size()) {
				if (STOPCOMPUTATION)return;
				String nsi = (String) nuclideS.elementAt(i);
				String nsj = (String) nuclideS.elementAt(j);

				if (nsi.equals(nsj)) {
					Double yi = (Double) yieldD.elementAt(i);
					double yid = yi.doubleValue();
					Double yj = (Double) yieldD.elementAt(j);
					double yjd = yj.doubleValue();
					double yinew = yid + yjd;
					yieldD.setElementAt(new Double(yinew), i);

					yieldD.removeElementAt(j);
					nuclideS.removeElementAt(j);

					atomicMassD.removeElementAt(j);

					radLocI.removeElementAt(j);
					radLocN.removeElementAt(j);
					halfLifeD.removeElementAt(j);
					halfLifeUnitsS.removeElementAt(j);
				}

				j++;
			}
			i++;
		}

		// creating chains
		for (j = 0; j <= nchain; j++){
			if (STOPCOMPUTATION)return;
			// nchain
			for (i = 0; i < ndaughterChainMax; i++){
				if (STOPCOMPUTATION)return;
				if (j < nmax_chains)
					if (nuc_chains[i][j] == null) {
						if ((i < nmax_daughters) && (j < nmax_chains)) {
							nuc_chains[i][j] = "-1";
							hlu_chains[i][j] = "-1";
							am_chains[i][j] = -1;
							hl_chains[i][j] = -1;
							br_chains[i][j] = -1;
							a_chains[i][j] = -1;
							a_chains_equilibrum[i][j] = -1;
							at_chains[i][j] = -1;
						}
					}
			}
		}
		// ----------------------------------------

	}

	/**
	 * Formatting half life. Return half life in seconds.
	 * 
	 * @param hl hl
	 * @param hlu hlu
	 * @return the result
	 */
	// from master library ICRP38 we must convert HalfLife in SI units [seconds]
	public static double formatHalfLife(double hl, String hlu) {
		double result = hl;// return this value if hlu=seconds!!
		if (hlu.equals("y")) {
			result = hl * 365.25 * 24.0 * 3600.0;
		} else if (hlu.equals("d")) {
			result = hl * 24.0 * 3600.0;
		} else if (hlu.equals("h")) {
			result = hl * 3600.0;
		} else if (hlu.equals("m")) {
			result = hl * 60.0;
		} else if (hlu.equals("ms")) {
			result = hl / 1000.0;
		} else if (hlu.equals("us")) {
			result = hl / 1000000.0;
		}

		return result;
	}
	
	@SuppressWarnings("unused")
	/**
	 * Copy MSAccess exported file into derby! Not used=>better copy MsAcces
	 * data to Derby directly! 
	 */
	private void oldCopyMsAccesExportedFileIntoDerbyTables() {
		String datas = "Data";
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;

		String dbName = icrpDB;
		opens = opens + file_sep + dbName;
		try {
			Connection con11 = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");
			Statement s = con11.createStatement();

			// We create a table...
			String str = "create table icrp38Rad ( ID integer, NI VARCHAR(100), HY VARCHAR(100), LE VARCHAR(100) )";
			s.execute(str);

			str = "create table ICRP38Index ( ID integer, Nuclide VARCHAR(100),"
					+ "HalfLife DOUBLE PRECISION, HalfLifeUnits VARCHAR(100), DecayMode1 VARCHAR(100),"
					+ "DecayMode2 VARCHAR(100), DecayMode3 VARCHAR(100), DecayMode4 VARCHAR(100),"
					+ "RadLoc BIGINT, RadNum BIGINT, BetaLoc BIGINT, BetaNum BIGINT,"
					+ "Dau1 BIGINT, Yield1 DOUBLE PRECISION, Dau2 BIGINT, Yield2 DOUBLE PRECISION,"
					+ "Dau3 BIGINT, Yield3 DOUBLE PRECISION, AlphaEnergy DOUBLE PRECISION,"
					+ "ElectronEnergy DOUBLE PRECISION, PhotonEnergy DOUBLE PRECISION,"
					+ "PhotonsLT10kev BIGINT, PhotonsGE10kev BIGINT, BetaParticleNum BIGINT,"
					+ "ElectronNum BIGINT, AlphaParticleNum BIGINT, SPFFlag BIGINT,"
					+ "AtomicMass DOUBLE PRECISION, ENDSFDate VARCHAR(100), GammaConstant DOUBLE PRECISION )";
			s.execute(str);

			// /prepare statement
			PreparedStatement psInsert = null;
			// PreparedStatement psUpdate = null;
			psInsert = con11
					.prepareStatement("insert into icrp38Rad values (?, ?, ?, ?)");
			statements.add(psInsert);

			psInsert = conn
					.prepareStatement("insert into ICRP38Index values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statements.add(psInsert);
			// ////////////////////////////

			// String[] value=new String[4];
			// datas="Data";
			// file_sep=System.getProperty("file.separator");
			// String fileS="icrp38Rad.txt";
			// String filename=fileS;//datas+file_sep+fileS;

			String[] value = new String[30];
			String fileS = "ICRP38Index.txt";
			String filename = fileS;

			int iread = 0;
			int lnr = 0;// data number
			int lnrr = 0;// line number
			StringBuffer desc = new StringBuffer();
			boolean haveData = false;

			char lineSep = '\n';
			char comma = ',';
			char lastChar = ' ';//

			// boolean idB=false; // boolean ircodeB=false;//radiation code
			// boolean irB=false;//intensity // boolean eB=false;//energy

			boolean v1B = false;
			boolean v2B = false;
			boolean v3B = false;
			boolean v4B = false;
			boolean v5B = false;
			boolean v6B = false;
			boolean v7B = false;
			boolean v8B = false;
			boolean v9B = false;
			boolean v10B = false;

			boolean v11B = false;
			boolean v12B = false;
			boolean v13B = false;
			boolean v14B = false;
			boolean v15B = false;
			boolean v16B = false;
			boolean v17B = false;
			boolean v18B = false;
			boolean v19B = false;
			boolean v20B = false;

			boolean v21B = false;
			boolean v22B = false;
			boolean v23B = false;
			boolean v24B = false;
			boolean v25B = false;
			boolean v26B = false;
			boolean v27B = false;
			boolean v28B = false;
			boolean v29B = false;
			boolean v30B = false;

			try {
				FileInputStream in = new FileInputStream(filename);

				while ((iread = in.read()) != -1) {
					if (!Character.isWhitespace((char) iread)
							&& ((char) iread != comma)) {
						desc.append((char) iread);
						haveData = true;
					} else {
						if (lastChar == comma && ((char) iread == comma)) {
							// we have two coma
							desc = new StringBuffer();
							desc.append((char) ' ');
							haveData = true;
						}
						if (haveData)// we have data
						{
							haveData = false;
							// reset
							lnr++;

							if (!v1B) {// if(!idB)

								String ss = desc.toString();
								// System.out.println(ss);
								value[0] = ss;
								v1B = true;
							} else if (!v2B)// (!ircodeB)
							{
								String ss = desc.toString();
								value[1] = ss;
								// System.out.println(value[0] + " " +
								// value[1]);
								v2B = true;
							} else if (!v3B)// (!irB)
							{
								String ss = desc.toString();//
								// System.out.println(ss);
								value[2] = ss;
								v3B = true;
							} else if (!v4B)// if(!eB)
							{
								String ss = desc.toString();
								value[3] = ss;
								v4B = true;
								// ircodeB=false; // irB=false; // eB=false;
							} else if (!v5B) {
								String ss = desc.toString();
								value[4] = ss;
								v5B = true;
							} else if (!v6B) {
								String ss = desc.toString();
								value[5] = ss;
								v6B = true;
							} else if (!v7B) {
								String ss = desc.toString();
								value[6] = ss;
								v7B = true;
							} else if (!v8B) {
								String ss = desc.toString();
								value[7] = ss;
								v8B = true;
							} else if (!v9B) {
								String ss = desc.toString();
								value[8] = ss;
								v9B = true;
							} else if (!v10B) {
								String ss = desc.toString();
								value[9] = ss;
								v10B = true;
								System.out.println(value[0] + " " + value[1]);
							} else if (!v11B) {
								String ss = desc.toString();
								value[10] = ss;
								System.out.println(value[0] + " " + value[1]);
								v11B = true;
							} else if (!v12B) {
								String ss = desc.toString();
								value[11] = ss;
								v12B = true;
							} else if (!v13B) {
								String ss = desc.toString();
								value[12] = ss;
								v13B = true;
							} else if (!v14B) {
								String ss = desc.toString();
								value[13] = ss;
								v14B = true;
							} else if (!v15B) {
								String ss = desc.toString();
								value[14] = ss;
								v15B = true;
							} else if (!v16B) {
								String ss = desc.toString();
								value[15] = ss;
								v16B = true;
							} else if (!v17B) {

								String ss = desc.toString();
								value[16] = ss;
								v17B = true;
							} else if (!v18B) {
								String ss = desc.toString();
								value[17] = ss;
								v18B = true;
							} else if (!v19B) {
								String ss = desc.toString();
								value[18] = ss;
								v19B = true;
							} else if (!v20B) {
								String ss = desc.toString();
								value[19] = ss;
								v20B = true;
							} else if (!v21B) {
								String ss = desc.toString();
								value[20] = ss;
								v21B = true;
							} else if (!v22B) {
								String ss = desc.toString();
								value[21] = ss;
								v22B = true;
							} else if (!v23B) {
								String ss = desc.toString();
								value[22] = ss;
								v23B = true;
							} else if (!v24B) {
								String ss = desc.toString();
								value[23] = ss;
								v24B = true;
							} else if (!v25B) {
								String ss = desc.toString();
								value[24] = ss;
								v25B = true;
							} else if (!v26B) {
								String ss = desc.toString();
								value[25] = ss;
								v26B = true;
							} else if (!v27B) {
								String ss = desc.toString();
								value[26] = ss;
								v27B = true;
							} else if (!v28B) {
								String ss = desc.toString();
								value[27] = ss;
								v28B = true;

							} else if (!v29B) {
								String ss = desc.toString();
								value[28] = ss;

								v29B = true;
							} else if (!v30B) {
								String ss = desc.toString();
								value[29] = ss;
								// reset
								v30B = true;
								v1B = false;
								v2B = false;
								v3B = false;
								v4B = false;
								v5B = false;
								v6B = false;
								v7B = false;
								v8B = false;
								v9B = false;
								v10B = false;
								v11B = false;
								v12B = false;
								v13B = false;
								v14B = false;
								v15B = false;
								v16B = false;
								v17B = false;
								v18B = false;
								v19B = false;
								v20B = false;
								v21B = false;
								v22B = false;
								v23B = false;
								v24B = false;
								v25B = false;
								v26B = false;
								v27B = false;
								v28B = false;
								v29B = false;
								v30B = false;
							}

						}// have data

						if ((char) iread == lineSep) { //
							// System.out.println(value[0]+" intra la end line "+value[1]);
							psInsert.setString(1, value[0]);
							psInsert.setString(2, value[1]);
							psInsert.setString(3, value[2]);
							psInsert.setString(4, value[3]);
							psInsert.setString(5, value[4]);
							psInsert.setString(6, value[5]);
							psInsert.setString(7, value[6]);
							psInsert.setString(8, value[7]);
							psInsert.setString(9, value[8]);
							psInsert.setString(10, value[9]);
							psInsert.setString(11, value[10]);
							psInsert.setString(12, value[11]);
							psInsert.setString(13, value[12]);
							psInsert.setString(14, value[13]);
							psInsert.setString(15, value[14]);
							psInsert.setString(16, value[15]);
							psInsert.setString(17, value[16]);
							psInsert.setString(18, value[17]);
							psInsert.setString(19, value[18]);
							psInsert.setString(20, value[19]);
							psInsert.setString(21, value[20]);
							psInsert.setString(22, value[21]);
							psInsert.setString(23, value[22]);
							psInsert.setString(24, value[23]);
							psInsert.setString(25, value[24]);
							psInsert.setString(26, value[25]);
							psInsert.setString(27, value[26]);
							psInsert.setString(28, value[27]);
							psInsert.setString(29, value[28]);
							psInsert.setString(30, value[29]);

							psInsert.executeUpdate();

							// value=new String[4];
							value = new String[30];
							lnrr++;
						}// if ((char) iread == lineSep)

						desc = new StringBuffer();
					}// else
					lastChar = (char) iread;//
					// save the read character!
				}// main while
				in.close();
			}// try
			catch (Exception exc) {

			}

			con11.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Create derby database. 
	 * 
	 * @param dbName dbName
	 * @throws Exception exception
	 */
	@SuppressWarnings("unused")
	private void createDerbyDatabase(String dbName) throws Exception {
		String datas = resources.getString("data.load");// Data
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;

		opens = opens + file_sep + dbName;

		String protocol = "jdbc:derby:";

		DriverManager.getConnection(protocol + opens + ";create=true", "", "");
	}

	/**
	 * Create gamma database and tables.
	 */
	@SuppressWarnings("unused")
	private void createGammaDB() {

		Connection conng = null;

		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = "Gamma";
		opens = opens + file_sep + dbName;
		String protocol = "jdbc:derby:";

		statements = new ArrayList<Statement>();
		resultsets = new ArrayList<ResultSet>();

		Statement s = null;

		try {
			String driver = "org.apache.derby.jdbc.EmbeddedDriver";
			// disable log file!
			System.setProperty("derby.stream.error.method",
					"jdf.db.DBConnection.disableDerbyLogFile");

			Class.forName(driver).newInstance();

			// conng = DriverManager.getConnection(protocol + opens
			// + ";create=true", "", "");
			conng = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");
			String str = "";
			// ------------------
			conng.setAutoCommit(false);
			s = conng.createStatement();
			// delete the table
			// s.execute("drop table " +
			// "GammaEnergyCalibration");

			// s.execute("drop table " +
			// "GammaStandardSourceRois");

			// s.execute("drop table " +
			// /"GammaSampleRois");*/

			// s.execute("drop table " +
			// "GammaNuclides");

			// s.execute("drop table " +
			// "GammaNuclidesDetails");

			statements.add(s);

			str = "create table GammaNuclides ( ID integer, "
					+ "Nuclide VARCHAR(100), AtomicMass DOUBLE PRECISION, "
					+ "HalfLife DOUBLE PRECISION, HalfLifeUnits VARCHAR(100), "
					+ "DecayCorrInfo VARCHAR(100) )";

			s.execute(str);

			str = "create table GammaNuclidesDetails ( Nrcrt integer, ID integer, "
					+ "Energy_kev DOUBLE PRECISION, "
					+ "Yield DOUBLE PRECISION, Notes VARCHAR(100), RadType VARCHAR(100), "
					+ "DecayCorr DOUBLE PRECISION)";

			s.execute(str);

			str = "create table GammaNuclidesDecayChain ( Nrcrt integer, ID integer, "
					+ "Nuclide VARCHAR(100), " + "BR DOUBLE PRECISION)";

			s.execute(str);

			str = "create table GammaNuclidesCoincidence ( Nrcrt integer, ID integer, "
					+ "Energy_kev DOUBLE PRECISION, Yield DOUBLE PRECISION, "
					+ "SUMMOUT DOUBLE PRECISION, SUMMIN DOUBLE PRECISION, COIN_CORR DOUBLE PRECISION)";

			s.execute(str);

			// --------------------
			str = "create table GammaGlobalEfficiencyCalibration ( ID integer, "
					+ "Nuclide VARCHAR(100), "
					+ "Efficiency DOUBLE PRECISION, Error DOUBLE PRECISION)";

			s.execute(str);

			str = "create table GammaEfficiencyCalibration ( ID integer, "
					+ "CalibrationName VARCHAR(100), Use VARCHAR(100), "
					+ "p1a4 DOUBLE PRECISION, p1a3 DOUBLE PRECISION, "
					+ "p1a2 DOUBLE PRECISION, p1a1 DOUBLE PRECISION, p1a0 DOUBLE PRECISION, "
					+ "p2a4 DOUBLE PRECISION, p2a3 DOUBLE PRECISION, "
					+ "p2a2 DOUBLE PRECISION, p2a1 DOUBLE PRECISION, p2a0 DOUBLE PRECISION, "
					+ "CrossoverEnergy DOUBLE PRECISION, OverallProcentualError DOUBLE PRECISION)";

			s.execute(str);

			str = "create table GammaEnergyCalibration ( ID integer, "
					+ "CalibrationName VARCHAR(100), Use VARCHAR(100), "
					+ "a3 DOUBLE PRECISION, "
					+ "a2 DOUBLE PRECISION, a1 DOUBLE PRECISION, a0 DOUBLE PRECISION)";// ,
																						// "
			// + "chFkev_a3 DOUBLE PRECISION, "
			// + "chFkev_a2 DOUBLE PRECISION, chFkev_a1 DOUBLE PRECISION, " +
			// "chFkev_a0 DOUBLE PRECISION)";

			s.execute(str);

			str = "create table GammaFWHMCalibration ( ID integer, "
					+ "CalibrationName VARCHAR(100), Use VARCHAR(100), "
					+ "a3 DOUBLE PRECISION, "
					+ "a2 DOUBLE PRECISION, a1 DOUBLE PRECISION, a0 DOUBLE PRECISION, "
					+ "OverallProcentualError DOUBLE PRECISION)";

			s.execute(str);

			str = "create table GammaBackground ( ID integer, "
					+ "SpectrumName VARCHAR(100), LiveTimeSec DOUBLE PRECISION, MeasurementDate VARCHAR(100), "
					+ "Quantity DOUBLE PRECISION, QuantityUnits VARCHAR(100))";
			s.execute(str);

			str = "create table GammaBackgroundSpectrum (Nrcrt integer, ID integer, "
					+ "Channel DOUBLE PRECISION, Pulses DOUBLE PRECISION)";
			s.execute(str);
			str = "create table GammaBackgroundRois (Nrcrt integer, ID integer, "
					+ "StartChannel DOUBLE PRECISION, StartEnergy DOUBLE PRECISION, "
					+ "StartBkgChannel DOUBLE PRECISION, StartBkgEnergy DOUBLE PRECISION, "
					+ "CenterChannel DOUBLE PRECISION, CenterEnergy DOUBLE PRECISION, "
					+ "CentroidChannel DOUBLE PRECISION, CentroidEnergy DOUBLE PRECISION, "
					+ "PeakChannel DOUBLE PRECISION, PeakEnergy DOUBLE PRECISION, "
					+ "PeakPulses DOUBLE PRECISION, "
					+ "EndChannel DOUBLE PRECISION, EndEnergy DOUBLE PRECISION, "
					+ "EndBkgChannel DOUBLE PRECISION, EndBkgEnergy DOUBLE PRECISION, "
					+ "FWHMChannel DOUBLE PRECISION, FWHMChannelError DOUBLE PRECISION, "
					+ "FWHMEnergy DOUBLE PRECISION, FWHMEnergyError DOUBLE PRECISION, "
					+ "FWHMEnergyCalib DOUBLE PRECISION, FWHMEnergyCalibError DOUBLE PRECISION, "
					+ "Resolution DOUBLE PRECISION, ResolutionCalib DOUBLE PRECISION, "
					+ "Significance VARCHAR(100), "
					+ "BkgCounts DOUBLE PRECISION, BkgCountsError DOUBLE PRECISION, "
					+ "BkgCountsRate DOUBLE PRECISION, BkgCountsRateError DOUBLE PRECISION, "
					+ "GrossCounts DOUBLE PRECISION, GrossCountsError DOUBLE PRECISION, "
					+ "GrossCountsRate DOUBLE PRECISION, GrossCountsRateError DOUBLE PRECISION, "
					+ "StartBkgPulses DOUBLE PRECISION, EndBkgPulses DOUBLE PRECISION, "
					+ "ComptonCounts DOUBLE PRECISION, ComptonCountsError DOUBLE PRECISION, "
					+ "ComptonCountsRate DOUBLE PRECISION, ComptonCountsRateError DOUBLE PRECISION, "
					+ "NetCounts DOUBLE PRECISION, NetCountsError DOUBLE PRECISION, "
					+ "NetCountsRate DOUBLE PRECISION, NetCountsRateError DOUBLE PRECISION, "
					+ "ConfidenceLevel DOUBLE PRECISION, "
					+ "Nuclide VARCHAR(100), "
					+ "Yield DOUBLE PRECISION, "
					+ "EfficiencyProcentual DOUBLE PRECISION, EfficiencyProcentualError DOUBLE PRECISION, "
					+ "Activity_Bq DOUBLE PRECISION, Activity_BqError DOUBLE PRECISION, "
					+ "MDA_Bq DOUBLE PRECISION, MDA_BqError DOUBLE PRECISION, "
					+ "Difference VARCHAR(100), AtomicMass DOUBLE PRECISION, HalfLife DOUBLE PRECISION, "
					+ "HalfLifeUnits VARCHAR(100), NetCalculationMethod VARCHAR(100), "
					+ "MDACalculationMethod VARCHAR(100))";
			s.execute(str);

			str = "create table GammaStandardSource ( ID integer, "
					+ "SpectrumName VARCHAR(100), LiveTimeSec DOUBLE PRECISION, MeasurementDate VARCHAR(100), "
					+ "Quantity DOUBLE PRECISION, QuantityUnits VARCHAR(100))";
			s.execute(str);

			str = "create table GammaStandardSourceSpectrum (Nrcrt integer, ID integer, "
					+ "Channel DOUBLE PRECISION, Pulses DOUBLE PRECISION)";
			s.execute(str);
			str = "create table GammaStandardSourceRois (Nrcrt integer, ID integer, "
					+ "StartChannel DOUBLE PRECISION, StartEnergy DOUBLE PRECISION, "
					+ "StartBkgChannel DOUBLE PRECISION, StartBkgEnergy DOUBLE PRECISION, "
					+ "CenterChannel DOUBLE PRECISION, CenterEnergy DOUBLE PRECISION, "
					+ "CentroidChannel DOUBLE PRECISION, CentroidEnergy DOUBLE PRECISION, "
					+ "PeakChannel DOUBLE PRECISION, PeakEnergy DOUBLE PRECISION, "
					+ "PeakPulses DOUBLE PRECISION, "
					+ "EndChannel DOUBLE PRECISION, EndEnergy DOUBLE PRECISION, "
					+ "EndBkgChannel DOUBLE PRECISION, EndBkgEnergy DOUBLE PRECISION, "
					+ "FWHMChannel DOUBLE PRECISION, FWHMChannelError DOUBLE PRECISION, "
					+ "FWHMEnergy DOUBLE PRECISION, FWHMEnergyError DOUBLE PRECISION, "
					+ "FWHMEnergyCalib DOUBLE PRECISION, FWHMEnergyCalibError DOUBLE PRECISION, "
					+ "Resolution DOUBLE PRECISION, ResolutionCalib DOUBLE PRECISION, "
					+ "Significance VARCHAR(100), "
					+ "BkgCounts DOUBLE PRECISION, BkgCountsError DOUBLE PRECISION, "
					+ "BkgCountsRate DOUBLE PRECISION, BkgCountsRateError DOUBLE PRECISION, "
					+ "GrossCounts DOUBLE PRECISION, GrossCountsError DOUBLE PRECISION, "
					+ "GrossCountsRate DOUBLE PRECISION, GrossCountsRateError DOUBLE PRECISION, "
					+ "StartBkgPulses DOUBLE PRECISION, EndBkgPulses DOUBLE PRECISION, "
					+ "ComptonCounts DOUBLE PRECISION, ComptonCountsError DOUBLE PRECISION, "
					+ "ComptonCountsRate DOUBLE PRECISION, ComptonCountsRateError DOUBLE PRECISION, "
					+ "NetCounts DOUBLE PRECISION, NetCountsError DOUBLE PRECISION, "
					+ "NetCountsRate DOUBLE PRECISION, NetCountsRateError DOUBLE PRECISION, "
					+ "ConfidenceLevel DOUBLE PRECISION, "
					+ "Nuclide VARCHAR(100), "
					+ "Yield DOUBLE PRECISION, "
					+ "EfficiencyProcentual DOUBLE PRECISION, EfficiencyProcentualError DOUBLE PRECISION, "
					+ "Activity_Bq DOUBLE PRECISION, Activity_BqError DOUBLE PRECISION, "
					+ "MDA_Bq DOUBLE PRECISION, MDA_BqError DOUBLE PRECISION, "
					+ "Difference VARCHAR(100), AtomicMass DOUBLE PRECISION, HalfLife DOUBLE PRECISION, "
					+ "HalfLifeUnits VARCHAR(100), NetCalculationMethod VARCHAR(100), "
					+ "MDACalculationMethod VARCHAR(100))";
			s.execute(str);

			str = "create table GammaSample ( ID integer, "
					+ "SpectrumName VARCHAR(100), LiveTimeSec DOUBLE PRECISION, MeasurementDate VARCHAR(100), "
					+ "Quantity DOUBLE PRECISION, QuantityUnits VARCHAR(100))";
			s.execute(str);

			str = "create table GammaSampleSpectrum (Nrcrt integer, ID integer, "
					+ "Channel DOUBLE PRECISION, Pulses DOUBLE PRECISION)";
			s.execute(str);
			str = "create table GammaSampleRois (Nrcrt integer, ID integer, "
					+ "StartChannel DOUBLE PRECISION, StartEnergy DOUBLE PRECISION, "
					+ "StartBkgChannel DOUBLE PRECISION, StartBkgEnergy DOUBLE PRECISION, "
					+ "CenterChannel DOUBLE PRECISION, CenterEnergy DOUBLE PRECISION, "
					+ "CentroidChannel DOUBLE PRECISION, CentroidEnergy DOUBLE PRECISION, "
					+ "PeakChannel DOUBLE PRECISION, PeakEnergy DOUBLE PRECISION, "
					+ "PeakPulses DOUBLE PRECISION, "
					+ "EndChannel DOUBLE PRECISION, EndEnergy DOUBLE PRECISION, "
					+ "EndBkgChannel DOUBLE PRECISION, EndBkgEnergy DOUBLE PRECISION, "
					+ "FWHMChannel DOUBLE PRECISION, FWHMChannelError DOUBLE PRECISION, "
					+ "FWHMEnergy DOUBLE PRECISION, FWHMEnergyError DOUBLE PRECISION, "
					+ "FWHMEnergyCalib DOUBLE PRECISION, FWHMEnergyCalibError DOUBLE PRECISION, "
					+ "Resolution DOUBLE PRECISION, ResolutionCalib DOUBLE PRECISION, "
					+ "Significance VARCHAR(100), "
					+ "BkgCounts DOUBLE PRECISION, BkgCountsError DOUBLE PRECISION, "
					+ "BkgCountsRate DOUBLE PRECISION, BkgCountsRateError DOUBLE PRECISION, "
					+ "GrossCounts DOUBLE PRECISION, GrossCountsError DOUBLE PRECISION, "
					+ "GrossCountsRate DOUBLE PRECISION, GrossCountsRateError DOUBLE PRECISION, "
					+ "StartBkgPulses DOUBLE PRECISION, EndBkgPulses DOUBLE PRECISION, "
					+ "ComptonCounts DOUBLE PRECISION, ComptonCountsError DOUBLE PRECISION, "
					+ "ComptonCountsRate DOUBLE PRECISION, ComptonCountsRateError DOUBLE PRECISION, "
					+ "NetCounts DOUBLE PRECISION, NetCountsError DOUBLE PRECISION, "
					+ "NetCountsRate DOUBLE PRECISION, NetCountsRateError DOUBLE PRECISION, "
					+ "ConfidenceLevel DOUBLE PRECISION, "
					+ "Nuclide VARCHAR(100), "
					+ "Yield DOUBLE PRECISION, "
					+ "EfficiencyProcentual DOUBLE PRECISION, EfficiencyProcentualError DOUBLE PRECISION, "
					+ "Activity_Bq DOUBLE PRECISION, Activity_BqError DOUBLE PRECISION, "
					+ "MDA_Bq DOUBLE PRECISION, MDA_BqError DOUBLE PRECISION, "
					+ "Difference VARCHAR(100), AtomicMass DOUBLE PRECISION, HalfLife DOUBLE PRECISION, "
					+ "HalfLifeUnits VARCHAR(100), NetCalculationMethod VARCHAR(100), "
					+ "MDACalculationMethod VARCHAR(100))";

			s.execute(str);

			conng.commit();

			// /////
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			// release all open resources to avoid unnecessary memory usage
			// ResultSet
			int j = 0;
			while (!resultsets.isEmpty()) {
				ResultSet rs = (ResultSet) resultsets.remove(j);
				try {
					if (rs != null) {
						rs.close();
						rs = null;
					}
				} catch (SQLException sqle) {
					printSQLException(sqle);
				}
			}
			// Statements and PreparedStatements
			int i = 0;
			while (!statements.isEmpty()) {
				// PreparedStatement extend Statement
				Statement st = (Statement) statements.remove(i);
				try {
					if (st != null) {
						st.close();
						st = null;
					}
				} catch (SQLException sqle) {
					printSQLException(sqle);
				}
			}
			// Connection
			try {
				if (conng != null) {
					conng.close();
					conng = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
			}
		}
	}

	/**
	 * Copy a MSAcces database table directly to derby database. 
	 * THIS WILL NOT WORK WITH LATEST JDK (8) ODBC WAS REMOVED. THERE IS WORKAROUND 
	 * SEE nuclidesExposure module.
	 */
	@SuppressWarnings("unused")
	private void copyMsAccesICRP38IndexTable() {
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			String datas = "Data";// resources.getString("data.load");//"Data";
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			// String fileS = "icrp38_index.mdb";
			String fileS = "JAERI03_index.mdb";
			String filename = opens + file_sep + fileS;
			// System.out.println(filename);
			String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
			database += filename.trim() + ";DriverID=22;READONLY=true}"; // add
																			// on
																			// to
																			// the
																			// end
			// now we can get the connection from the DriverManager
			Connection conec1 = DriverManager.getConnection(database, "", "");

			Statement s = conec1.createStatement();
			// String statement = "select *" + " from [ICRP38 Index]";
			String statement = "select *" + " from JAERI03index";
			s.execute(statement);

			ResultSet rs = s.getResultSet();

			ResultSetMetaData rsmd = rs.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();

			Vector<Object> dataVec = new Vector<Object>();
			Vector<String> randVec;

			if (rs != null)
				while (rs.next())
				// this will step through our data row-by-row.
				{
					randVec = new Vector<String>();
					for (int i = 1; i <= numberOfColumns; i++) {
						// We always operate with strings
						// even if column type is double (e.g.).
						// problem is null data..not empty String but NULL
						// object
						String rsStr = rs.getString(i);
						randVec.addElement(rsStr);
					}
					dataVec.addElement(randVec);
				}

			// -------------------
			conec1.close();
			// .............
			opens = currentDir + file_sep + datas;
			String dbName = icrpDB;
			opens = opens + file_sep + dbName;
			Connection con1 = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");
			s = con1.createStatement();
			// We create a table...
			String str = // "create table ICRP38Index (" + " ID integer,"
			"create table JAERI03index (" + " ID integer,"
					+ " Nuclide VARCHAR(100)," + "HalfLife DOUBLE PRECISION, "
					+ "HalfLifeUnits VARCHAR(100),"
					+ " DecayMode1 VARCHAR(100)," + "DecayMode2 VARCHAR(100), "
					+ "DecayMode3 VARCHAR(100)," + " DecayMode4 VARCHAR(100),"
					+ "RadLoc BIGINT," + " RadNum BIGINT, "
					+ "BetaLoc BIGINT, " + "BetaNum BIGINT," + "Dau1 BIGINT,"
					+ " Yield1 DOUBLE PRECISION," + " Dau2 BIGINT, "
					+ "Yield2 DOUBLE PRECISION," + "Dau3 BIGINT, "
					+ "Yield3 DOUBLE PRECISION,"
					+ " AlphaEnergy DOUBLE PRECISION,"
					+ "ElectronEnergy DOUBLE PRECISION, "
					+ "PhotonEnergy DOUBLE PRECISION,"
					+ "PhotonsLT10kev BIGINT, " + "PhotonsGE10kev BIGINT, "
					+ "BetaParticleNum BIGINT," + "ElectronNum BIGINT,"
					+ " AlphaParticleNum BIGINT," + " SPFFlag BIGINT,"
					+ "AtomicMass DOUBLE PRECISION, "
					+ "ENDSFDate VARCHAR(100),"
					+ " GammaConstant DOUBLE PRECISION )";
			s.execute(str);
			// Now table is created...copy!
			Vector<Object> colNumeVec = new Vector<Object>();
			rs = s.executeQuery("SELECT * FROM " + icrpTable);
			rsmd = rs.getMetaData();
			numberOfColumns = rsmd.getColumnCount();
			String[] columns = new String[numberOfColumns];
			for (int i = 1; i <= numberOfColumns; i++) {
				columns[i - 1] = rsmd.getColumnName(i);
				colNumeVec.addElement(columns[i - 1]);

			}

			PreparedStatement psInsert = null;
			// psInsert =
			// con1.prepareStatement("insert into ICRP38Index values "
			psInsert = con1.prepareStatement("insert into JAERI03index values "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
					+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,"
					+ " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

			for (int i = 0; i < dataVec.size(); i++) {
				@SuppressWarnings("unchecked")
				Vector<Object> v = (Vector<Object>) dataVec.elementAt(i);
				String[] dataRow = new String[v.size()];
				for (int j = 0; j < v.size(); j++) {
					dataRow[j] = (String) v.elementAt(j);
					// Only works with mysql:
					// DBOperation.insert(icrpTable, columns, dataRow, con1);

					// That is ALWAYS WORKING:
					psInsert.setString(j + 1, dataRow[j]);
				}
				psInsert.executeUpdate();
			}
			// /////////
			// Let's see what we got!
			rs = s.executeQuery("SELECT * FROM " + icrpTable);
			if (rs != null)
				while (rs.next()) {
					String str1 = "";
					for (int i = 1; i <= numberOfColumns; i++) {
						str1 = str1 + ", " + rs.getString(i);
					}
					System.out.println("@ " + str1);

				}

			// delete the table
			// s.execute("drop table " + icrpTable);
			con1.close();
		} catch (Exception err) {
			err.printStackTrace();
		}

	}

	/**
	 * Copy a MSAcces database table directly to derby database. 
	 * THIS WILL NOT WORK WITH LATEST JDK (8) ODBC WAS REMOVED. THERE IS WORKAROUND 
	 * SEE nuclidesExposure module.
	 */
	@SuppressWarnings("unused")
	private void copyMsAccesICRP38RadTable() {
		try {
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
			String datas = "Data";
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			// String fileS = "icrp38Rad.mdb";
			String fileS = "JAERI03Rad.mdb";
			String filename = opens + file_sep + fileS;
			String database = "jdbc:odbc:Driver={Microsoft Access Driver (*.mdb)};DBQ=";
			database += filename.trim() + ";DriverID=22;READONLY=true}";
			// now we can get the connection from the DriverManager
			Connection con1 = DriverManager.getConnection(database, "", "");
			// ----------------
			Statement s = con1.createStatement();
			// String statement = "select *" + " from icrp38Rad";
			String statement = "select *" + " from jaeri03Rad";

			s.execute(statement);

			ResultSet rs = s.getResultSet();

			ResultSetMetaData rsmd = rs.getMetaData();
			int numberOfColumns = rsmd.getColumnCount();
			Vector<Object> dataVec = new Vector<Object>();
			Vector<String> randVec;
			if (rs != null)
				while (rs.next()) {
					randVec = new Vector<String>();
					for (int i = 1; i <= numberOfColumns; i++) {
						// We always operate with strings
						// even if column type is double (e.g.).
						// problem is null data..not empty String but NULL
						// object
						String rsStr = rs.getString(i);
						randVec.addElement(rsStr);
					}
					dataVec.addElement(randVec);
				}
			con1.close();

			opens = currentDir + file_sep + datas;
			String dbName = icrpDB;
			opens = opens + file_sep + dbName;
			Connection con11 = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");
			s = con11.createStatement();
			// We create a table...
			// String str =
			// "create table icrp38Rad ( ID integer, NI VARCHAR(100), HY VARCHAR(100), LE VARCHAR(100) )";
			String str = "create table jaeri03Rad ( ID integer, NI VARCHAR(100), HY VARCHAR(100), LE VARCHAR(100) )";
			s.execute(str);
			// //////
			PreparedStatement psInsert = null;
			psInsert = con11
			// .prepareStatement("insert into icrp38Rad values (?, ?, ?, ?)");
					.prepareStatement("insert into jaeri03Rad values (?, ?, ?, ?)");

			for (int i = 0; i < dataVec.size(); i++) {
				@SuppressWarnings("unchecked")
				Vector<Object> v = (Vector<Object>) dataVec.elementAt(i);
				String[] dataRow = new String[v.size()];
				for (int j = 0; j < v.size(); j++) {
					dataRow[j] = (String) v.elementAt(j);

					// That is ALWAYS WORKING:
					psInsert.setString(j + 1, dataRow[j]);
				}
				psInsert.executeUpdate();
			}
			// /////////
			// Let's see what we got!
			rs = s.executeQuery("SELECT * FROM " + icrpRadTable);
			if (rs != null)
				while (rs.next()) {
					String str1 = "";
					for (int i = 1; i <= numberOfColumns; i++) {
						str1 = str1 + ", " + rs.getString(i);
					}
					System.out.println("@ " + str1);

				}

			// delete the table
			// s.execute("drop table " + icrpRadTable);

			con11.close();
		} catch (Exception err) {
			System.out.println("ERROR: " + err);
		}

	}

	/**
	 * Start the computation thread.
	 */
	private void startThread() {
		stopAnim=false;
		if (computationTh == null) {
			STOPCOMPUTATION=false;
			computationTh = new Thread(this);
			computationTh.start();// Allow one simulation at time!
			//setEnabled(false);
		}

		if (statusTh == null) {
			statusTh = new Thread(this);
			statusTh.start();
		}
	}

	private void kill(){
		stopAppend = true;
		stopThread();
	}
	/**
	 * Stop the computation thread.
	 */
	private void stopThread() {
		statusTh = null;
		frameNumber = 0;
		stopAnim=true;
		
		if (computationTh == null) {
			stopAppend = false;// press kill button but simulation never
								// started!
			return;
		}
		
		computationTh = null;
		
		if (stopAppend) {// kill button was pressed!
			STOPCOMPUTATION=true;
			textArea.append(resources.getString("text.simulation.stop") + "\n");			
			stopAppend = false;
			String label = resources.getString("status.done");
			statusL.setText(label);
		}
		
		//setEnabled(true);
	}
	private boolean stopAnim=true;
	/**
	 * Thread specific run method.
	 */
	public void run() {
		Thread.currentThread().setPriority(Thread.NORM_PRIORITY);// both thread
																	// same
																	// priority

		long startTime = System.currentTimeMillis();
		Thread currentThread = Thread.currentThread();
		while (!stopAnim && currentThread == statusTh) {// if thread is status display
											// Thread!!
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

		if (currentThread == computationTh) {// if thread is the main
												// computation Thread!!
			if (command.equals(GETNUCLIDEINFO_COMMAND)) {
				getNuclideInfo();
			}
		}
	}
}
