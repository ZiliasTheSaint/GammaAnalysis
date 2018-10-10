package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.Vector;

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
import danfulea.phys.GammaRoi;
import danfulea.utils.FrameUtilities;
import danfulea.utils.TimeUtilities;

/**
 * The graphical user interface (GUI) for Gamma database section. <br>
 * 
 * @author Dan Fulea, 16 May 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaSpectraDatabaseFrame extends JFrame implements
		ActionListener,	ItemListener, ListSelectionListener, Runnable {
	public boolean mustResetB = false;
	private GammaAnalysisFrame mf;
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;
	private String command = "";
	private final Dimension PREFERRED_SIZE = new Dimension(950, 700);
	private final Dimension mainTableDimension = new Dimension(600, 250);
	private final Dimension tableDimension = new Dimension(300, 200);

	private JLabel statusL = new JLabel("Waiting...");
	private volatile Thread computationTh = null;// computation thread!
	private volatile Thread statusTh = null;// status display thread!
	private int delay = 100;
	private int frameNumber = -1;
	private String statusRunS = "";

	private JRadioButton bkgRb, standardSourceRb, sampleRb = null;
	private JCheckBox overwritteSpectrumCh = null;
	private JButton saveB, useBkgB, noBkgB, loadB, deleteB = null;
	//private AdvancedSelectPanel asp = null;
	//private AdvancedSelectPanel aspDate = null;
	//private AdvancedSelectPanel aspDateRois = null;
	private JPanel suportSp = new JPanel(new BorderLayout());
	private JPanel suportSpDate = new JPanel(new BorderLayout());
	private JPanel suportSpDateRois = new JPanel(new BorderLayout());

	private static final String SAVE_COMMAND = "SAVE";
	private static final String LOAD_COMMAND = "LOAD";
	private static final String USEBKG_COMMAND = "USEBKG";
	private static final String NOBKG_COMMAND = "NOBKG";
	private static final String DELETE_COMMAND = "DELETE";
	private static final String BKG_COMMAND = "BKG";
	private static final String STANDARDSOURCE_COMMAND = "STANDARDSOURCE";
	private static final String SAMPLE_COMMAND = "SAMPLE";
	private String gammaDB = "";
	private String bkgTable = "";
	private String bkgSpectrumTable = "";
	private String bkgRoisTable = "";
	private String standardSourceTable = "";
	private String standardSourceSpectrumTable = "";
	private String standardSourceRoisTable = "";
	private String sampleTable = "";
	private String sampleSpectrumTable = "";
	private String sampleRoisTable = "";
	private String mainTableS = "";
	private String dateTableS = "";
	private String roiTableS = "";

	public static final int SAVE_SET_MODE = 0;
	public static final int BKG_SET_MODE = 1;
	public static final int LOAD_SET_MODE = 2;
	private static int irunMode = SAVE_SET_MODE;

	public static final int BKG_DISPLAY_MODE = 0;
	public static final int STANDARDSOURCE_DISPLAY_MODE = 1;
	public static final int SAMPLE_DISPLAY_MODE = 2;
	private static int idisplayMode = BKG_DISPLAY_MODE;

	private boolean channelWarningB = false;

	/**
	 * The connection
	 */
	private Connection gammadbcon = null;
	
	/**
	 * Main table primary key column name
	 */
	private String mainTablePrimaryKey = "ID";//spectrum
		
	/**
	 * Nested table primary key column name
	 */
	private String nestedTablePrimaryKey = "NRCRT";//spectrumDetail
	private String roinestedTablePrimaryKey = "NRCRT";//rois (if any) for that spectrum
		
	/**
	 * Shared column name for main table and nested table
	 */
	private String IDlink = "ID";	
	
	/**
	 * The column used for sorting data in main table (ORDER BY SQL syntax)
	 */
	private String orderbyS = "ID";	
	
	/**
	 * The column used for sorting data in nested table (ORDER BY SQL syntax)
	 */
	private String nestedorderbyS = "NRCRT";
	private String roinestedorderbyS = "NRCRT";	
	
	private final Dimension sizeOrderCb = new Dimension(200, 21);
	
	private JComboBox<String> genericorderbyCb;private JLabel genericrecordLabel;
	private JComboBox<String> genericnestedorderbyCb;private JLabel genericnestedrecordLabel;
	private JComboBox<String> genericroinestedorderbyCb;private JLabel genericroinestedrecordLabel;
	private JTable genericmainTable;
	private JTable genericnestedTable;
	private JTable genericroinestedTable;
	
	private DatabaseAgentSupport genericdbagent;
	private DatabaseAgentSupport genericnesteddbagent;
	private DatabaseAgentSupport genericroinesteddbagent;
	
	//private ListSelectionModel genericLSM;
	
	/**
	 * Constructor. GammaSpectraDatabaseFrame window is connected to main
	 * window.
	 * @param mf the GammaAnalysisFrame object
	 */
	public GammaSpectraDatabaseFrame(GammaAnalysisFrame mf) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("Database.NAME"));
		this.mf = mf;
		mustResetB = false;
		gammaDB = resources.getString("main.db");
		bkgTable = resources.getString("main.db.bkgTable");
		bkgSpectrumTable = resources.getString("main.db.bkgTable.spectrum");
		bkgRoisTable = resources.getString("main.db.bkgTable.rois");
		standardSourceTable = resources
				.getString("main.db.standardSourceTable");
		standardSourceSpectrumTable = resources
				.getString("main.db.standardSourceTable.spectrum");
		standardSourceRoisTable = resources
				.getString("main.db.standardSourceTable.rois");
		sampleTable = resources.getString("main.db.sampleTable");
		sampleSpectrumTable = resources
				.getString("main.db.sampleTable.spectrum");
		sampleRoisTable = resources.getString("main.db.sampleTable.rois");
		//===========================================
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		mainTablePrimaryKey = "ID";
		nestedTablePrimaryKey = "NRCRT";
		roinestedTablePrimaryKey = "NRCRT";
		IDlink = "ID";
		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = gammaDB;
		opens = opens + file_sep + dbName;
		gammadbcon = DatabaseAgent.getConnection(opens, "", "");		
		//=========================================
		performQueryDb();
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
	 * Before GammaSpectraDatabaseFrame is called, it can be set the display
	 * mode!<br>
	 * Display mode refers to background, standard source or sample database.
	 * 
	 * @param idisplay the display index
	 */
	public static void setDisplayMode(int idisplay) {
		if (idisplay < BKG_DISPLAY_MODE || idisplay > SAMPLE_DISPLAY_MODE)
			idisplayMode = BKG_DISPLAY_MODE;
		else
			idisplayMode = idisplay;
	}

	/**
	 * Retrieve the display mode.
	 * 
	 * @return the result
	 */
	public int getDisplayMode() {
		return idisplayMode;
	}

	/**
	 * Before GammaSpectraDatabaseFrame is called, it can be set the run mode! <br>
	 * Run mode refers to save spectrum, load spectrum from database or <br>
	 * set an ambiental background spectrum for further ROI calculations.
	 * 
	 * @param irun irun
	 */
	public static void setRunMode(int irun) {
		if (irun < SAVE_SET_MODE || irun > LOAD_SET_MODE)
			irunMode = SAVE_SET_MODE;
		else
			irunMode = irun;
	}

	/**
	 * Retrieve the run mode.
	 * 
	 * @return the result
	 */
	public int getRunMode() {
		return irunMode;
	}

	/**
	 * Exit method
	 */
	private void attemptExit() {
		if (mustResetB) {
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
		try{
			if (gammadbcon != null)
				gammadbcon.close();			
		}catch (Exception e){
			e.printStackTrace();
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

	
	private JPanel orderP, orderP2, orderP3;
	/**
	 * GUI creation.
	 */
	private void createGUI() {
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";
		//====================================
				
		genericorderbyCb = genericdbagent.getOrderByComboBox();
		genericorderbyCb.addItemListener(this);
		genericorderbyCb.setMaximumRowCount(5);
		genericorderbyCb.setPreferredSize(sizeOrderCb);
		genericrecordLabel = genericdbagent.getRecordsLabel();
		orderP = new JPanel();
		orderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP.add(label);
		orderP.add(genericorderbyCb);
		orderP.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP.add(label);
		orderP.add(genericrecordLabel);
						
		genericnestedorderbyCb = genericnesteddbagent.getOrderByComboBox();
		genericnestedorderbyCb.addItemListener(this);
		genericnestedorderbyCb.setMaximumRowCount(5);
		genericnestedorderbyCb.setPreferredSize(sizeOrderCb);
		genericnestedrecordLabel = genericnesteddbagent.getRecordsLabel();
		orderP2 = new JPanel();
		orderP2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP2.add(label);
		orderP2.add(genericnestedorderbyCb);
		orderP2.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP2.add(label);
		orderP2.add(genericnestedrecordLabel);
					
		genericroinestedorderbyCb = genericroinesteddbagent.getOrderByComboBox();
		genericroinestedorderbyCb.addItemListener(this);
		genericroinestedorderbyCb.setMaximumRowCount(5);
		genericroinestedorderbyCb.setPreferredSize(sizeOrderCb);
		genericroinestedrecordLabel = genericroinesteddbagent.getRecordsLabel();
		orderP3 = new JPanel();
		orderP3.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP3.add(label);
		orderP3.add(genericroinestedorderbyCb);
		orderP3.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP3.add(label);
		orderP3.add(genericroinestedrecordLabel);	

		//===================================
		overwritteSpectrumCh = new JCheckBox(
				resources.getString("database.overwrite.checkbox"));
		overwritteSpectrumCh.setBackground(GammaAnalysisFrame.bkgColor);
		overwritteSpectrumCh.setForeground(GammaAnalysisFrame.foreColor);

		// saveB,useBkgB,noBkgB,loadB,deleteB
		buttonName = resources.getString("database.delete.button");
		buttonToolTip = resources.getString("database.delete.button.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName, DELETE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("database.delete.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		deleteB = button;

		buttonName = resources.getString("database.save.button");
		buttonToolTip = resources.getString("database.save.button.toolTip");
		buttonIconName = resources.getString("img.save.database");
		button = FrameUtilities.makeButton(buttonIconName, SAVE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("database.save.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		saveB = button;

		buttonName = resources.getString("database.load.button");
		buttonToolTip = resources.getString("database.load.button.toolTip");
		buttonIconName = resources.getString("img.open.database");
		button = FrameUtilities.makeButton(buttonIconName, LOAD_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("database.load.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		loadB = button;

		buttonName = resources.getString("database.useBkg.button");
		buttonToolTip = resources.getString("database.useBkg.button.toolTip");
		buttonIconName = resources.getString("img.substract.bkg");
		button = FrameUtilities.makeButton(buttonIconName, USEBKG_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("database.useBkg.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		useBkgB = button;

		buttonName = resources.getString("database.noBkg.button");
		buttonToolTip = resources.getString("database.noBkg.button.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName, NOBKG_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("database.noBkg.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		noBkgB = button;

		bkgRb = new JRadioButton(resources.getString("rb.bkg"));
		bkgRb.setBackground(GammaAnalysisFrame.bkgColor);
		bkgRb.setForeground(GammaAnalysisFrame.foreColor);
		standardSourceRb = new JRadioButton(
				resources.getString("rb.standardSource"));
		standardSourceRb.setBackground(GammaAnalysisFrame.bkgColor);
		standardSourceRb.setForeground(GammaAnalysisFrame.foreColor);
		sampleRb = new JRadioButton(resources.getString("rb.sample"));
		sampleRb.setBackground(GammaAnalysisFrame.bkgColor);
		sampleRb.setForeground(GammaAnalysisFrame.foreColor);
		ButtonGroup group = new ButtonGroup();
		group.add(bkgRb);
		group.add(standardSourceRb);
		group.add(sampleRb);

		if (getDisplayMode() == BKG_DISPLAY_MODE) {
			bkgRb.setSelected(true);
		} else if (getDisplayMode() == STANDARDSOURCE_DISPLAY_MODE) {
			standardSourceRb.setSelected(true);
		} else if (getDisplayMode() == SAMPLE_DISPLAY_MODE) {
			sampleRb.setSelected(true);
		}

		bkgRb.setActionCommand(BKG_COMMAND);
		bkgRb.addActionListener(this);
		standardSourceRb.setActionCommand(STANDARDSOURCE_COMMAND);
		standardSourceRb.addActionListener(this);
		sampleRb.setActionCommand(SAMPLE_COMMAND);
		sampleRb.addActionListener(this);
		// ------------------------
		JPanel p1P = new JPanel();
		p1P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p1P.add(bkgRb);
		p1P.add(standardSourceRb);
		p1P.add(sampleRb);
		p1P.setBackground(GammaAnalysisFrame.bkgColor);

		p1P.setBorder(FrameUtilities.getGroupBoxBorder(resources
				.getString("database.selection.border"),GammaAnalysisFrame.foreColor));

		// -----------

		JPanel p2P = new JPanel();
		p2P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));

		p2P.add(overwritteSpectrumCh);
		p2P.add(saveB);
		p2P.add(deleteB);
		label = new JLabel("  ");
		p2P.add(label);
		p2P.add(useBkgB);
		p2P.add(noBkgB);
		label = new JLabel("  ");
		p2P.add(label);
		p2P.add(loadB);
		p2P.setBackground(GammaAnalysisFrame.bkgColor);
		p2P.setBorder(FrameUtilities.getGroupBoxBorder(resources
				.getString("database.tasks.border"),GammaAnalysisFrame.foreColor));

		JPanel bdP = new JPanel();
		BoxLayout bl0 = new BoxLayout(bdP, BoxLayout.Y_AXIS);
		bdP.setLayout(bl0);
		bdP.add(p1P);
		bdP.add(p2P);
		bdP.setBackground(GammaAnalysisFrame.bkgColor);

		//====================
		//suportSp.setPreferredSize(mainTableDimension);
		//suportSpDate.setPreferredSize(tableDimension);
		//suportSpDateRois.setPreferredSize(tableDimension);
		genericmainTable = genericdbagent.getMainTable();//mainTable;//bkg
		genericnestedTable = genericnesteddbagent.getMainTable();//nestedTable;
		genericroinestedTable = genericroinesteddbagent.getMainTable();//roinestedTable;
		
		JScrollPane scrollPane = new JScrollPane(genericmainTable);
		//genericmainTable.setFillsViewportHeight(true);
		suportSp.add(scrollPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane2 = new JScrollPane(genericnestedTable);
		//genericnestedTable.setFillsViewportHeight(true);
		suportSpDate.add(scrollPane2,BorderLayout.CENTER);
		JScrollPane scrollPane3 = new JScrollPane(genericroinestedTable);
		//genericroinestedTable.setFillsViewportHeight(true);
		//genericroinestedTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		suportSpDateRois.add(scrollPane3,BorderLayout.CENTER);
		
		suportSp.setPreferredSize(mainTableDimension);
		suportSpDate.setPreferredSize(tableDimension);
		suportSpDateRois.setPreferredSize(tableDimension);
		
		
		JPanel testP = new JPanel();
		testP.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		testP.add(suportSp);
		testP.setBackground(GammaAnalysisFrame.bkgColor);
		//===============
		
		JPanel nordP = new JPanel();
		BoxLayout blnordP = new BoxLayout(nordP, BoxLayout.Y_AXIS);
		nordP.setLayout(blnordP);//new FlowLayout(FlowLayout.CENTER, 2, 2));
		nordP.add(orderP);//,BorderLayout.NORTH);
		nordP.add(testP);//,BorderLayout.CENTER);//(testP);//suportSp);//		
		nordP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel s1 = new JPanel();
		BoxLayout bls1 = new BoxLayout(s1, BoxLayout.Y_AXIS);
		s1.setLayout(bls1);
		s1.add(orderP2);
		s1.add(suportSpDate);
		s1.setBackground(GammaAnalysisFrame.bkgColor);
		JPanel s2 = new JPanel();
		BoxLayout bls2 = new BoxLayout(s2, BoxLayout.Y_AXIS);
		s2.setLayout(bls2);
		s2.add(orderP3);
		s2.add(suportSpDateRois);
		s2.setBackground(GammaAnalysisFrame.bkgColor);
		
		JPanel sudP = new JPanel();
		sudP.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 2));
		sudP.add(s1);//suportSpDate);		
		sudP.add(s2);//suportSpDateRois);		
		sudP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel mainP = new JPanel();
		BoxLayout bl1 = new BoxLayout(mainP, BoxLayout.Y_AXIS);
		mainP.setLayout(bl1);
		mainP.add(nordP);
		mainP.add(sudP);
		mainP.setBackground(GammaAnalysisFrame.bkgColor);

		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		initStatusBar(statusBar);

		JPanel content = new JPanel(new BorderLayout());
		content.add(statusBar, BorderLayout.PAGE_END);
		content.add(bdP, BorderLayout.NORTH);
		content.add(mainP, BorderLayout.CENTER);
		setContentPane(new JScrollPane(content));
		content.setOpaque(true);
		pack();

		customize();
	}

	/**
	 * Setting enabled/disabled GUI controls according to the run mode!
	 */
	private void customize() {
		if (irunMode == LOAD_SET_MODE) {
			loadB.setEnabled(true);
			deleteB.setEnabled(true);
			saveB.setEnabled(false);
			overwritteSpectrumCh.setEnabled(false);
			useBkgB.setEnabled(false);
			noBkgB.setEnabled(false);

			standardSourceRb.setEnabled(true);
			sampleRb.setEnabled(true);
		} else if (irunMode == SAVE_SET_MODE) {
			loadB.setEnabled(false);
			saveB.setEnabled(true);
			overwritteSpectrumCh.setEnabled(true);
			deleteB.setEnabled(true);
			useBkgB.setEnabled(false);
			noBkgB.setEnabled(false);

			standardSourceRb.setEnabled(true);
			sampleRb.setEnabled(true);
		} else if (irunMode == BKG_SET_MODE) {
			loadB.setEnabled(false);
			saveB.setEnabled(false);
			deleteB.setEnabled(false);
			overwritteSpectrumCh.setEnabled(false);
			useBkgB.setEnabled(true);
			noBkgB.setEnabled(true);

			standardSourceRb.setEnabled(false);
			sampleRb.setEnabled(false);
		}

	}

	/**
	 * Setting up the status bar.
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
	 * Set the database tables and agents according to the display mode.
	 */
	private void setTables() {
		if (getDisplayMode() == BKG_DISPLAY_MODE) {
			mainTableS = bkgTable;
			dateTableS = bkgSpectrumTable;
			roiTableS = bkgRoisTable;
			
		} else if (getDisplayMode() == STANDARDSOURCE_DISPLAY_MODE) {
			mainTableS = standardSourceTable;
			dateTableS = standardSourceSpectrumTable;
			roiTableS = standardSourceRoisTable;			
			
		} else if (getDisplayMode() == SAMPLE_DISPLAY_MODE) {
			mainTableS = sampleTable;
			dateTableS = sampleSpectrumTable;
			roiTableS = sampleRoisTable;
		}
		
		genericdbagent = new DatabaseAgentSupport(gammadbcon, 
				mainTablePrimaryKey, mainTableS);
		genericnesteddbagent = new DatabaseAgentSupport(gammadbcon,
				nestedTablePrimaryKey, dateTableS);
		genericroinesteddbagent = new DatabaseAgentSupport(gammadbcon, 
				roinestedTablePrimaryKey, roiTableS);
		genericdbagent.setHasValidAIColumn(false);
		genericnesteddbagent.setHasValidAIColumn(false);
		genericroinesteddbagent.setHasValidAIColumn(false);

		genericdbagent.init();
		orderbyS = mainTablePrimaryKey;// when start-up...ID is default!!
		genericnesteddbagent.setLinks(IDlink, "0");//dummy link to not select 10^100 records
		genericnesteddbagent.init();//not happy with this
		nestedorderbyS = nestedTablePrimaryKey;		
		genericroinesteddbagent.setLinks(IDlink, "0");//dummy link to not select 10^100 records
		genericroinesteddbagent.init();
		roinestedorderbyS = roinestedTablePrimaryKey;
		
		genericmainTable = genericdbagent.getMainTable();
		genericnestedTable = genericnesteddbagent.getMainTable();
		genericroinestedTable = genericroinesteddbagent.getMainTable();	
		
		ListSelectionModel genericLSM = genericmainTable.getSelectionModel();
		genericLSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		genericLSM.addListSelectionListener(this);
		
		if (genericmainTable.getRowCount() > 0){
			//select last row!
			genericmainTable.setRowSelectionInterval(genericmainTable.getRowCount() - 1,
					genericmainTable.getRowCount() - 1); // last ID
		}
	}

	/**
	 * Initialize database.
	 */
	private void performQueryDb() {		

		setTables();
				
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;
			// set tables
			setTables();
			// now do the job
			String s = "select * from " + mainTableS;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			asp = new AdvancedSelectPanel();
			suportSp.add(asp, BorderLayout.CENTER);

			// sp = new SelectPanel(0);
			// suportSp.add(sp, BorderLayout.CENTER);

			// s = "select * from " + gammaNuclidesDetailsTable;
			// DBOperation.select(s, con1);
			// aspDate=new AdvancedSelectPanel();
			// suportSpDate.add(aspDate, BorderLayout.CENTER);

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
				s = "select * from " + dateTableS;
				DBOperation.select(s, con1);

				if (aspDate != null)
					suportSpDate.remove(aspDate);

				aspDate = new AdvancedSelectPanel();
				suportSpDate.add(aspDate, BorderLayout.CENTER);

				// empty table..display header
				s = "select * from " + roiTableS;
				DBOperation.select(s, con1);

				if (aspDateRois != null)
					suportSpDateRois.remove(aspDateRois);

				aspDateRois = new AdvancedSelectPanel();
				suportSpDateRois.add(aspDateRois, BorderLayout.CENTER);
			}

			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Display data from "secondary" table (nested table) according to the selection made <br>
	 * in "main" table. Main table and all secondary tables are connected <br>
	 * by ID!
	 */
	private void updateDetailTable() {

		//JTable aspTable = asp.getTab();
		int selID = 0;// NO ZERO ID
		int selRow =  genericmainTable.getSelectedRow();//aspTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) genericmainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
		} else {
			 return;//!!!!!!!!!!!!!!!!!!!it was commented
		}
		// ===update nested===
		genericnesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		genericnesteddbagent.performSelection(nestedorderbyS);
		// ===update nested===
		genericroinesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		genericroinesteddbagent.performSelection(roinestedorderbyS);		
//System.out.println(genericroinesteddbagent.getDatabaseTableName()+"  "+selID+"  IDLINK "+IDlink);
		/**try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;
			// if here tables are already set!
			// but for consistency double check:
			setTables();
			// now the job:
			String s = "select * from " + dateTableS + " where ID = " + selID
					+ " ORDER BY NRCRT";
			// IF press header=>selRow=-1=>ID=0=>NO ZERO ID DATA=>
			// so display an empty table!
			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			if (aspDate != null)
				suportSpDate.remove(aspDate);

			aspDate = new AdvancedSelectPanel();
			suportSpDate.add(aspDate, BorderLayout.CENTER);

			JTable detailTable = aspDate.getTab();
			ListSelectionModel row1SM = detailTable.getSelectionModel();
			row1SM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// ------------------------------------------------
			s = "select * from " + roiTableS + " where ID = " + selID
					+ " ORDER BY NRCRT";
			con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			if (aspDateRois != null)
				suportSpDateRois.remove(aspDateRois);

			aspDateRois = new AdvancedSelectPanel();
			suportSpDateRois.add(aspDateRois, BorderLayout.CENTER);

			detailTable = aspDateRois.getTab();
			row1SM = detailTable.getSelectionModel();
			row1SM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			// ----------------------------------------------
			validate();

			if (con1 != null)
				con1.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}*/

	}	
		
	/*private void performCurrentSelection() {
		suportSp.remove(asp);
		performQueryDb();
		validate();
		statusL.setText(resources.getString("status.done"));
	}*/

	/**
	 * Most actions are set here
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		// String command = arg0.getActionCommand();
		command = arg0.getActionCommand();
		if (command.equals(SAVE_COMMAND)) {
			// save();
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(USEBKG_COMMAND)) {
			// useBKG();
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(NOBKG_COMMAND)) {
			// noBKG();
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(LOAD_COMMAND)) {
			// load();
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(DELETE_COMMAND)) {
			// delete();
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(BKG_COMMAND)) {
			//showBkgData();
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(STANDARDSOURCE_COMMAND)) {
			//showStandardSourceData();
			statusRunS = resources.getString("status.computing");
			startThread();
		} else if (command.equals(SAMPLE_COMMAND)) {
			//showSampleData();
			statusRunS = resources.getString("status.computing");
			startThread();
		}
	}

	/**
	 * JCombobox related actions are set here
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == genericorderbyCb) {
			sort();
		} else if (ie.getSource() == genericnestedorderbyCb) {
			sort2();
		} else if (ie.getSource() == genericroinestedorderbyCb) {
			sort3();
		}		
	}
	
	/**
	 * Sorts data from main table
	 */
	private void sort() {				
		orderbyS = (String) genericorderbyCb.getSelectedItem();
		genericdbagent.performSelection(orderbyS);
	}
	
	/**
	 * Sorts data from nested table
	 */
	private void sort2() {
		nestedorderbyS = (String) genericnestedorderbyCb.getSelectedItem();
		
		//JTable aspTable = asp.getTab();
		int selID = 0;// NO ZERO ID
		int selRow = genericmainTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) genericmainTable.getValueAt(selRow, 0);
		} else {
			return;
		}

		// ===update nested===
		genericnesteddbagent.setLinks(IDlink, Convertor.intToString(selID));				
		genericnesteddbagent.performSelection(nestedorderbyS);
	}
	
	/**
	 * Sorts data from roi nested table
	 */
	private void sort3() {
		roinestedorderbyS = (String) genericroinestedorderbyCb.getSelectedItem();
		
		//JTable aspTable = asp.getTab();
		int selID = 0;// NO ZERO ID
		int selRow = genericmainTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) genericmainTable.getValueAt(selRow, 0);
		} else {
			return;
		}

		// ===update nested===
		genericroinesteddbagent.setLinks(IDlink, Convertor.intToString(selID));				
		genericroinesteddbagent.performSelection(roinestedorderbyS);
	}
		
	/**
	 * JTable related actions are set here
	 */
	public void valueChanged(ListSelectionEvent e) {
		
		if (e.getSource() == genericmainTable.getSelectionModel()) {
			updateDetailTable();			
		}
	}
	
	/**
	 * Save spectrum in database.
	 */
	private void save() {
		//setTables();//redundant not needed
		
		boolean updateB = overwritteSpectrumCh.isSelected();
		if (!updateB) {
			String[] data = new String[genericdbagent.getUsefullColumnCount()];
			int kCol = 0;
			data[kCol] = mf.spectrumName;
			kCol++;
			data[kCol] = Convertor.doubleToString(mf.spectrumLiveTime);
			kCol++;
			data[kCol] = mf.measurementDate;
			kCol++;
			data[kCol] = Convertor.doubleToString(mf.quantity);
			kCol++;
			data[kCol] = mf.quantityUnit;
			kCol++;
			
			genericdbagent.insert(data);
			
			//nested tables
			int id = genericdbagent.getAIPrimaryKeyValue();
			
			int n = mf.channelI.length;
			for (int i = 0; i < n; i++) {
				int nrcrt = i + 1;
				data = new String[genericnesteddbagent.getAllColumnCount()];
				kCol = 0;
				data[kCol] = Convertor.intToString(nrcrt);//Convertor.intToString(id);
				kCol++;
				data[kCol] = Convertor.intToString(id);
				kCol++;
				data[kCol] = Convertor.doubleToString(mf.channelI[i]);
				kCol++;
				data[kCol] = Convertor.doubleToString(mf.pulsesD[i]);
				kCol++;
				
				genericnesteddbagent.insertAll(data);
			}
			
			if (mf.roiV.size() != 0) {
				// we have rois....save them:
				for (int i = 0; i < mf.roiV.size(); i++) {
					GammaRoi gr = mf.roiV.elementAt(i);
					
					int nrcrt = i + 1;
					data = new String[genericroinesteddbagent.getAllColumnCount()];
					kCol = 0;
					data[kCol] = Convertor.intToString(nrcrt);//Convertor.intToString(id);
					kCol++;
					data[kCol] = Convertor.intToString(id);
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartEdgeChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartEdgeEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getCenterChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getCenterEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getCentroidChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getCentroidEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getPeakChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getPeakEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getPeakPulses());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndEdgeChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndEdgeEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFWHMChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFWHMChannelError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFwhmEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFwhmEnergyError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFwhmEnergyCalib());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFwhmEnergyCalibError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getResolution());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getResolutionCalib());
					kCol++;
					data[kCol] = gr.getSignificance();
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getBkgCounts());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getBkgCountsError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getBkgCountsRate());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getBkgCountsRateError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getGrossCounts());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getGrossCountsError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getGrossCountsRate());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getGrossCountsRateError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartEdgePulses());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndEdgePulses());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getComptonCounts());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getComptonCountsError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getComptonCountsRate());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getComptonCountsRateError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getNetCounts());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getNetCountsError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getNetCountsRate());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getNetCountsRateError());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getConfidenceLevel());
					kCol++;
					data[kCol] =gr.getNuclide();
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getYield());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getEfficiencyProcentual());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getEfficiencyProcentualError());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getActivity_Bq());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getActivity_BqError());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getMda_Bq());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getMda_BqError());
					kCol++;
					data[kCol] =gr.getDifference();
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getAtomicMass());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getHalfLife());
					kCol++;
					data[kCol] =gr.getHalfLifeUnits();
					kCol++;
					data[kCol] =gr.getNetCalculationMethod();
					kCol++;
					data[kCol] =gr.getMdaCalculationMethod();
					kCol++;
					
					genericroinesteddbagent.insertAll(data);
				}
				
			}			
			
		} else {
			//update
			JTable mainTable = genericdbagent.getMainTable();
			int selID = 0;
			int selRow = mainTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) mainTable.getValueAt(selRow, 
						genericdbagent.getPrimaryKeyColumnIndex());													
																		
			} else {
				stopThread();// kill all threads
				statusL.setText(resources.getString("status.done"));
				return;
			}
			
			String[] data = new String[genericdbagent.getUsefullColumnCount()];
			int kCol = 0;
			data[kCol] = mf.spectrumName;
			kCol++;
			data[kCol] = Convertor.doubleToString(mf.spectrumLiveTime);
			kCol++;
			data[kCol] = mf.measurementDate;
			kCol++;
			data[kCol] = Convertor.doubleToString(mf.quantity);
			kCol++;
			data[kCol] = mf.quantityUnit;
			kCol++;
			
			genericdbagent.setSelectedRow(selRow);//no last row selection!!!
			genericdbagent.update(data, Convertor.intToString(selID));
			
			//now the nested table
			//first delete:
			genericnesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
			genericnesteddbagent.delete(genericnesteddbagent.getDatabaseTableName(), 
					IDlink, Convertor.intToString(selID));
			//now insert:
			//String[] CVALUE = new String[4];
			//Integer[] TVALUE = new Integer[4];
			//CVALUE[0]="Nrcrt";TVALUE[0]=Types.INTEGER;//Types.VARCHAR;
			//CVALUE[1]="ID";TVALUE[1]=Types.INTEGER;//Types.DOUBLE;
			//CVALUE[2]="Channel";TVALUE[2]=Types.DOUBLE;
			//CVALUE[2]="Pulses";TVALUE[3]=Types.DOUBLE;
			
			int n = mf.channelI.length;
			for (int i = 0; i < n; i++) {
				int nrcrt = i + 1;
				data = new String[genericnesteddbagent.getAllColumnCount()];
				kCol = 0;
				data[kCol] = Convertor.intToString(nrcrt);
				kCol++;
				data[kCol] = Convertor.intToString(selID);
				kCol++;
				data[kCol] = Convertor.doubleToString(mf.channelI[i]);
				kCol++;
				data[kCol] = Convertor.doubleToString(mf.pulsesD[i]);
				kCol++;
				
				genericnesteddbagent.insertAll(data);
			}
			
			if (mf.roiV.size() != 0) {
				// first delete than insert!
				genericroinesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
				genericroinesteddbagent.delete(genericroinesteddbagent.getDatabaseTableName(), 
						IDlink, Convertor.intToString(selID));
				//-------------
				for (int i = 0; i < mf.roiV.size(); i++) {
					GammaRoi gr = mf.roiV.elementAt(i);
					
					int nrcrt = i + 1;
					data = new String[genericroinesteddbagent.getAllColumnCount()];
					kCol = 0;
					data[kCol] = Convertor.intToString(nrcrt);
					kCol++;
					data[kCol] = Convertor.intToString(selID);
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartEdgeChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartEdgeEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getCenterChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getCenterEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getCentroidChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getCentroidEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getPeakChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getPeakEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getPeakPulses());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndEdgeChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndEdgeEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFWHMChannel());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFWHMChannelError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFwhmEnergy());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFwhmEnergyError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFwhmEnergyCalib());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getFwhmEnergyCalibError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getResolution());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getResolutionCalib());
					kCol++;
					data[kCol] = gr.getSignificance();
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getBkgCounts());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getBkgCountsError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getBkgCountsRate());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getBkgCountsRateError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getGrossCounts());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getGrossCountsError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getGrossCountsRate());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getGrossCountsRateError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getStartEdgePulses());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getEndEdgePulses());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getComptonCounts());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getComptonCountsError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getComptonCountsRate());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getComptonCountsRateError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getNetCounts());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getNetCountsError());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getNetCountsRate());
					kCol++;
					data[kCol] = Convertor.doubleToString(gr.getNetCountsRateError());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getConfidenceLevel());
					kCol++;
					data[kCol] =gr.getNuclide();
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getYield());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getEfficiencyProcentual());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getEfficiencyProcentualError());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getActivity_Bq());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getActivity_BqError());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getMda_Bq());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getMda_BqError());
					kCol++;
					data[kCol] =gr.getDifference();
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getAtomicMass());
					kCol++;
					data[kCol] =Convertor.doubleToString(gr.getHalfLife());
					kCol++;
					data[kCol] =gr.getHalfLifeUnits();
					kCol++;
					data[kCol] =gr.getNetCalculationMethod();
					kCol++;
					data[kCol] =gr.getMdaCalculationMethod();
					kCol++;
					
					genericroinesteddbagent.insertAll(data);
				}
			}
		}
		
		genericdbagent.performSelection(orderbyS);
		stopThread();// kill all threads
		statusL.setText(resources.getString("status.spectrum.save"));
		/*try {
			// prepare db query data
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;
			// set tables
			setTables();
			// now do the job

			// make a connection
			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			String s = "";
			boolean updateB = overwritteSpectrumCh.isSelected();
			PreparedStatement psInsert = null;
			PreparedStatement psUpdate = null;

			if (!updateB) {
				// first make a selection to retrieve usefull data
				// and point db cursor to the last row!!
				s = "select * from " + mainTableS;
				DBOperation.select(s, con1);
				// id where we make the insertion
				int id = DBOperation.getRowCount() + 1;

				// main table insert!
				psInsert = con1.prepareStatement("insert into " + mainTableS
						+ " values " + "(?, ?, ?, ?, ?, ?)");
				psInsert.setString(1, Convertor.intToString(id));
				psInsert.setString(2, mf.spectrumName);
				psInsert.setString(3,
						Convertor.doubleToString(mf.spectrumLiveTime));
				psInsert.setString(4, mf.measurementDate);
				psInsert.setString(5, Convertor.doubleToString(mf.quantity));
				psInsert.setString(6, mf.quantityUnit);
				psInsert.executeUpdate();

				// now the rest of the tables
				int n = mf.channelI.length;
				psInsert = con1.prepareStatement("insert into " + dateTableS
						+ " values " + "(?, ?, ?, ?)");
				for (int i = 0; i < n; i++) {
					int nrcrt = i + 1;
					psInsert.setString(1, Convertor.intToString(nrcrt));
					psInsert.setString(2, Convertor.intToString(id));
					psInsert.setString(3,
							Convertor.doubleToString(mf.channelI[i]));
					psInsert.setString(4,
							Convertor.doubleToString(mf.pulsesD[i]));

					psInsert.executeUpdate();
				}

				if (mf.roiV.size() != 0) {
					// we have rois....save them:
					psInsert = con1.prepareStatement("insert into " + roiTableS
							+ " values " + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?)");
					for (int i = 0; i < mf.roiV.size(); i++) {
						int nrcrt = i + 1;
						GammaRoi gr = mf.roiV.elementAt(i);
						psInsert.setString(1, Convertor.intToString(nrcrt));
						psInsert.setString(2, Convertor.intToString(id));
						psInsert.setString(3,
								Convertor.doubleToString(gr.getStartChannel()));
						psInsert.setString(4,
								Convertor.doubleToString(gr.getStartEnergy()));
						psInsert.setString(5, Convertor.doubleToString(gr
								.getStartEdgeChannel()));
						psInsert.setString(6, Convertor.doubleToString(gr
								.getStartEdgeEnergy()));
						psInsert.setString(7,
								Convertor.doubleToString(gr.getCenterChannel()));
						psInsert.setString(8,
								Convertor.doubleToString(gr.getCenterEnergy()));
						psInsert.setString(9, Convertor.doubleToString(gr
								.getCentroidChannel()));
						psInsert.setString(10, Convertor.doubleToString(gr
								.getCentroidEnergy()));
						psInsert.setString(11,
								Convertor.doubleToString(gr.getPeakChannel()));
						psInsert.setString(12,
								Convertor.doubleToString(gr.getPeakEnergy()));
						psInsert.setString(13,
								Convertor.doubleToString(gr.getPeakPulses()));
						psInsert.setString(14,
								Convertor.doubleToString(gr.getEndChannel()));
						psInsert.setString(15,
								Convertor.doubleToString(gr.getEndEnergy()));
						psInsert.setString(16, Convertor.doubleToString(gr
								.getEndEdgeChannel()));
						psInsert.setString(17,
								Convertor.doubleToString(gr.getEndEdgeEnergy()));
						psInsert.setString(18,
								Convertor.doubleToString(gr.getFWHMChannel()));
						psInsert.setString(19, Convertor.doubleToString(gr
								.getFWHMChannelError()));
						psInsert.setString(20,
								Convertor.doubleToString(gr.getFwhmEnergy()));
						psInsert.setString(21, Convertor.doubleToString(gr
								.getFwhmEnergyError()));
						psInsert.setString(22, Convertor.doubleToString(gr
								.getFwhmEnergyCalib()));
						psInsert.setString(23, Convertor.doubleToString(gr
								.getFwhmEnergyCalibError()));
						psInsert.setString(24,
								Convertor.doubleToString(gr.getResolution()));
						psInsert.setString(25, Convertor.doubleToString(gr
								.getResolutionCalib()));
						psInsert.setString(26, gr.getSignificance());
						psInsert.setString(27,
								Convertor.doubleToString(gr.getBkgCounts()));
						psInsert.setString(28, Convertor.doubleToString(gr
								.getBkgCountsError()));
						psInsert.setString(29,
								Convertor.doubleToString(gr.getBkgCountsRate()));
						psInsert.setString(30, Convertor.doubleToString(gr
								.getBkgCountsRateError()));
						psInsert.setString(31,
								Convertor.doubleToString(gr.getGrossCounts()));
						psInsert.setString(32, Convertor.doubleToString(gr
								.getGrossCountsError()));
						psInsert.setString(33, Convertor.doubleToString(gr
								.getGrossCountsRate()));
						psInsert.setString(34, Convertor.doubleToString(gr
								.getGrossCountsRateError()));
						psInsert.setString(35, Convertor.doubleToString(gr
								.getStartEdgePulses()));
						psInsert.setString(36,
								Convertor.doubleToString(gr.getEndEdgePulses()));
						psInsert.setString(37,
								Convertor.doubleToString(gr.getComptonCounts()));
						psInsert.setString(38, Convertor.doubleToString(gr
								.getComptonCountsError()));
						psInsert.setString(39, Convertor.doubleToString(gr
								.getComptonCountsRate()));
						psInsert.setString(40, Convertor.doubleToString(gr
								.getComptonCountsRateError()));
						psInsert.setString(41,
								Convertor.doubleToString(gr.getNetCounts()));
						psInsert.setString(42, Convertor.doubleToString(gr
								.getNetCountsError()));
						psInsert.setString(43,
								Convertor.doubleToString(gr.getNetCountsRate()));
						psInsert.setString(44, Convertor.doubleToString(gr
								.getNetCountsRateError()));
						psInsert.setString(45, Convertor.doubleToString(gr
								.getConfidenceLevel()));
						psInsert.setString(46, gr.getNuclide());
						psInsert.setString(47,
								Convertor.doubleToString(gr.getYield()));
						psInsert.setString(48, Convertor.doubleToString(gr
								.getEfficiencyProcentual()));
						psInsert.setString(49, Convertor.doubleToString(gr
								.getEfficiencyProcentualError()));
						psInsert.setString(50,
								Convertor.doubleToString(gr.getActivity_Bq()));
						psInsert.setString(51, Convertor.doubleToString(gr
								.getActivity_BqError()));
						psInsert.setString(52,
								Convertor.doubleToString(gr.getMda_Bq()));
						psInsert.setString(53,
								Convertor.doubleToString(gr.getMda_BqError()));
						psInsert.setString(54, gr.getDifference());
						psInsert.setString(55,
								Convertor.doubleToString(gr.getAtomicMass()));
						psInsert.setString(56,
								Convertor.doubleToString(gr.getHalfLife()));
						psInsert.setString(57, gr.getHalfLifeUnits());
						psInsert.setString(58, gr.getNetCalculationMethod());
						psInsert.setString(59, gr.getMdaCalculationMethod());

						psInsert.executeUpdate();
					}// for
				}// if we have rois

				if (psInsert != null)
					psInsert.close();

			} else {
				// update
				JTable aspTable = asp.getTab();
				int selID = 0;// NO ZERO ID
				int selRow = aspTable.getSelectedRow();
				if (selRow != -1) {
					selID = (Integer) aspTable.getValueAt(selRow, 0);
				} else {
					if (con1 != null)
						con1.close();

					stopThread();// kill all threads
					statusL.setText(resources.getString("status.done"));

					return;// nothing to save
				}

				// now we have an ID where to overwritte data!
				psUpdate = con1.prepareStatement("update " + mainTableS
						+ " set SpectrumName=?, " + "LiveTimeSec=?, "
						+ "MeasurementDate=?, " + "Quantity=?, "
						+ "QuantityUnits=? where ID = " + selID);
				psUpdate.setString(1, mf.spectrumName);
				psUpdate.setDouble(2, mf.spectrumLiveTime);
				psUpdate.setString(3, mf.measurementDate);
				psUpdate.setDouble(4, mf.quantity);
				psUpdate.setString(5, mf.quantityUnit);
				psUpdate.executeUpdate();

				// now the rest of the tables!
				Statement stmt = con1.createStatement(
						ResultSet.TYPE_SCROLL_SENSITIVE,// ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
				ResultSet res = null;
				// first delete than insert!
				s = "select * from " + dateTableS + " WHERE ID = " + selID;
				res = stmt.executeQuery(s);
				while (res.next()) {
					// first delete row
					res.deleteRow();
				}

				// dboperation=>statement scrollable and after the while
				// res.next LOOP
				// => the db position cursor is at last row where we will make
				// an insertion!
				// here, after the above select statement, the db cursor is
				// somehow lost
				// and the insert data are take place NOT in order!! e.g. the
				// displayed
				// nrcrt (id,channel,+ulses) may start with 79 to maximum and
				// then from
				// 1 to 78!!!

				// s = "select * from " + dateTableS+ " WHERE ID < " + selID;
				// res= stmt.executeQuery(s);
				// res.last();//position the insert cursor!!

				// THE ABOVE CAN BE AVOID, IF ANY SELECT STATEMENT IN LOAD DATA
				// OR DISPLAY DATA ARE DONE VIA ORDER BY NRCRT!!
				// IMPROVE COMPUTATION TIME!!!

				// now insert:..can be NOT in order!!!
				int n = mf.channelI.length;
				psInsert = con1.prepareStatement("insert into " + dateTableS
						+ " values " + "(?, ?, ?, ?)");
				for (int i = 0; i < n; i++) {
					int nrcrt = i + 1;
					psInsert.setString(1, Convertor.intToString(nrcrt));
					psInsert.setString(2, Convertor.intToString(selID));
					psInsert.setString(3,
							Convertor.doubleToString(mf.channelI[i]));
					psInsert.setString(4,
							Convertor.doubleToString(mf.pulsesD[i]));

					psInsert.executeUpdate();
				}

				if (mf.roiV.size() != 0) {
					// we have rois....save them:
					// first delete than insert!
					s = "select * from " + roiTableS + " WHERE ID = " + selID;
					res = stmt.executeQuery(s);
					while (res.next()) {
						res.deleteRow();// delete rows if any!
					}

					// position the cursor at desired insertion point!
					// s = "select * from " + roiTableS+ " WHERE ID < " + selID;
					// res= stmt.executeQuery(s);
					// res.last();//position the insert cursor!!

					// now insert:
					psInsert = con1.prepareStatement("insert into " + roiTableS
							+ " values " + "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
							+ "?, ?, ?, ?, ?, ?, ?, ?, ?)");
					for (int i = 0; i < mf.roiV.size(); i++) {
						int nrcrt = i + 1;
						GammaRoi gr = mf.roiV.elementAt(i);
						psInsert.setString(1, Convertor.intToString(nrcrt));
						psInsert.setString(2, Convertor.intToString(selID));
						psInsert.setString(3,
								Convertor.doubleToString(gr.getStartChannel()));
						psInsert.setString(4,
								Convertor.doubleToString(gr.getStartEnergy()));
						psInsert.setString(5, Convertor.doubleToString(gr
								.getStartEdgeChannel()));
						psInsert.setString(6, Convertor.doubleToString(gr
								.getStartEdgeEnergy()));
						psInsert.setString(7,
								Convertor.doubleToString(gr.getCenterChannel()));
						psInsert.setString(8,
								Convertor.doubleToString(gr.getCenterEnergy()));
						psInsert.setString(9, Convertor.doubleToString(gr
								.getCentroidChannel()));
						psInsert.setString(10, Convertor.doubleToString(gr
								.getCentroidEnergy()));
						psInsert.setString(11,
								Convertor.doubleToString(gr.getPeakChannel()));
						psInsert.setString(12,
								Convertor.doubleToString(gr.getPeakEnergy()));
						psInsert.setString(13,
								Convertor.doubleToString(gr.getPeakPulses()));
						psInsert.setString(14,
								Convertor.doubleToString(gr.getEndChannel()));
						psInsert.setString(15,
								Convertor.doubleToString(gr.getEndEnergy()));
						psInsert.setString(16, Convertor.doubleToString(gr
								.getEndEdgeChannel()));
						psInsert.setString(17,
								Convertor.doubleToString(gr.getEndEdgeEnergy()));
						psInsert.setString(18,
								Convertor.doubleToString(gr.getFWHMChannel()));
						psInsert.setString(19, Convertor.doubleToString(gr
								.getFWHMChannelError()));
						psInsert.setString(20,
								Convertor.doubleToString(gr.getFwhmEnergy()));
						psInsert.setString(21, Convertor.doubleToString(gr
								.getFwhmEnergyError()));
						psInsert.setString(22, Convertor.doubleToString(gr
								.getFwhmEnergyCalib()));
						psInsert.setString(23, Convertor.doubleToString(gr
								.getFwhmEnergyCalibError()));
						psInsert.setString(24,
								Convertor.doubleToString(gr.getResolution()));
						psInsert.setString(25, Convertor.doubleToString(gr
								.getResolutionCalib()));
						psInsert.setString(26, gr.getSignificance());
						psInsert.setString(27,
								Convertor.doubleToString(gr.getBkgCounts()));
						psInsert.setString(28, Convertor.doubleToString(gr
								.getBkgCountsError()));
						psInsert.setString(29,
								Convertor.doubleToString(gr.getBkgCountsRate()));
						psInsert.setString(30, Convertor.doubleToString(gr
								.getBkgCountsRateError()));
						psInsert.setString(31,
								Convertor.doubleToString(gr.getGrossCounts()));
						psInsert.setString(32, Convertor.doubleToString(gr
								.getGrossCountsError()));
						psInsert.setString(33, Convertor.doubleToString(gr
								.getGrossCountsRate()));
						psInsert.setString(34, Convertor.doubleToString(gr
								.getGrossCountsRateError()));
						psInsert.setString(35, Convertor.doubleToString(gr
								.getStartEdgePulses()));
						psInsert.setString(36,
								Convertor.doubleToString(gr.getEndEdgePulses()));
						psInsert.setString(37,
								Convertor.doubleToString(gr.getComptonCounts()));
						psInsert.setString(38, Convertor.doubleToString(gr
								.getComptonCountsError()));
						psInsert.setString(39, Convertor.doubleToString(gr
								.getComptonCountsRate()));
						psInsert.setString(40, Convertor.doubleToString(gr
								.getComptonCountsRateError()));
						psInsert.setString(41,
								Convertor.doubleToString(gr.getNetCounts()));
						psInsert.setString(42, Convertor.doubleToString(gr
								.getNetCountsError()));
						psInsert.setString(43,
								Convertor.doubleToString(gr.getNetCountsRate()));
						psInsert.setString(44, Convertor.doubleToString(gr
								.getNetCountsRateError()));
						psInsert.setString(45, Convertor.doubleToString(gr
								.getConfidenceLevel()));
						psInsert.setString(46, gr.getNuclide());
						psInsert.setString(47,
								Convertor.doubleToString(gr.getYield()));
						psInsert.setString(48, Convertor.doubleToString(gr
								.getEfficiencyProcentual()));
						psInsert.setString(49, Convertor.doubleToString(gr
								.getEfficiencyProcentualError()));
						psInsert.setString(50,
								Convertor.doubleToString(gr.getActivity_Bq()));
						psInsert.setString(51, Convertor.doubleToString(gr
								.getActivity_BqError()));
						psInsert.setString(52,
								Convertor.doubleToString(gr.getMda_Bq()));
						psInsert.setString(53,
								Convertor.doubleToString(gr.getMda_BqError()));
						psInsert.setString(54, gr.getDifference());
						psInsert.setString(55,
								Convertor.doubleToString(gr.getAtomicMass()));
						psInsert.setString(56,
								Convertor.doubleToString(gr.getHalfLife()));
						psInsert.setString(57, gr.getHalfLifeUnits());
						psInsert.setString(58, gr.getNetCalculationMethod());
						psInsert.setString(59, gr.getMdaCalculationMethod());

						psInsert.executeUpdate();
					}// for
				}// if we have rois!

				// ------------------
				stmt.close();
				res.close();
				if (psUpdate != null)
					psUpdate.close();
				if (psInsert != null)
					psInsert.close();
			}// else...update

			// finally, display what we've got
			performCurrentSelection();

			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			stopThread();// kill all threads
			statusL.setText(resources.getString("status.done"));

			e.printStackTrace();
		}

		stopThread();// kill all threads
		statusL.setText(resources.getString("status.spectrum.save"));*/
	}

	/**
	 * Load ambient background spectrum. The ambient background pulses are automatically 
	 * corrected by sample time (as if BKG is recorded along with the sample). 
	 */
	private void useBKG() {
		channelWarningB = false;
		try {
			// prepare db query data
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = gammaDB;
			//opens = opens + file_sep + dbName;
			// set tables
			//setTables();//NOT NEEEEEEEEEEEEEEEEEEEED
			// now do the job

			// make a connection
			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			JTable aspTable = genericdbagent.getMainTable();//asp.getTab();
			int selID = 0;// NO ZERO ID
			int selRow = aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) aspTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();

				stopThread();// kill all threads
				statusL.setText(resources.getString("status.done"));

				return;// nothing to load
			}
			// now we have an ID...load!
			String s = "select * from " + mainTableS + " WHERE ID = " + selID;
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			//===============
			mf.bkgSpectrumName = (String)DatabaseAgent.getValueAt(0, 1);
			mf.bkgSpectrumDate = (String)DatabaseAgent.getValueAt(0, 3);
			//==================
			mf.bkgSpectrumLiveTime = (Double) DatabaseAgent.getValueAt(0, 2);//DBOperation.getValueAt(0, 2);
			// spectrum

			s = "select * from " + dateTableS + " WHERE ID = " + selID
					+ " ORDER BY NRCRT";
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);
			double currentChannel = 0.0;
			mf.globalBkgCounts = 0.0;
			for (int i = 0; i < DatabaseAgent.getRowCount(); i++) {//DBOperation.getRowCount(); i++) {
				if (i > 0) {
					if (currentChannel >= (Double) DatabaseAgent.getValueAt(i, 2)) {//DBOperation.getValueAt(i, 2)) {
						channelWarningB = true;
					}
				}
				currentChannel = (Double) DatabaseAgent.getValueAt(i, 2);//DBOperation.getValueAt(i, 2);

				if (i < mf.bkgpulsesD.length) {

					mf.globalBkgCounts = mf.globalBkgCounts
							+ (Double) DatabaseAgent.getValueAt(i, 3);//DBOperation.getValueAt(i, 3);

					mf.bkgpulsesD[i] = (Double) DatabaseAgent.getValueAt(i, 3)//DBOperation.getValueAt(i, 3)
							* mf.spectrumLiveTime / mf.bkgSpectrumLiveTime;
					// System.out.println("use? "+mf.bkgpulsesD[i]);
				} else {
					break;// truncate BKG spectrum...it is user error not a
							// computational one!
				}
			}

			if (mf.bkgpulsesD.length != DatabaseAgent.getRowCount()) {//DBOperation.getRowCount()) {
				warning(true);
			}

			mf.updateBkgFromDatabase();

			// set rois BKG!!!
			if (mf.roiV.size() != 0) {
				mustResetB = true;
				for (int i = 0; i < mf.roiV.size(); i++) {
					GammaRoi gr = mf.roiV.elementAt(i);
					double sC = gr.getStartChannel();
					double eC = gr.getEndChannel();
					gr.resetBkgData();
					int i1 = (new Double(sC)).intValue();
					int i2 = (new Double(eC)).intValue();
					for (int j = i1; j <= i2; j++) {
						gr.addBkgPulses(mf.bkgpulsesD[j]);
					}
				}
			}
			// --------END!!!!!!!!!!!!!!!!!!!!!!!
			//if (con1 != null)
				//con1.close();

			status();

		} catch (Exception e) {
			stopThread();// kill all threads
			statusL.setText(resources.getString("status.done"));

			e.printStackTrace();
		}

		stopThread();// kill all threads
		statusL.setText(resources.getString("status.spectrum.load"));
	}

	/**
	 * Several warnings to display in main gamma frame.
	 */
	private void status() {
		if (channelWarningB) {
			mf.statusL.setText(resources.getString("status.spectrum.warning"));
		}// else
			// if (mf.roiV.size() != 0) {
			// we have rois
		// mf.statusL.setText(resources.getString("status.bkg.roi.warning"));
		// }
	}

	/**
	 * Several warnings to display in main gamma frame.
	 * 
	 * @param b b
	 */ 
	private void warning(boolean b) {
		if (b)
			mf.statusL.setText(resources.getString("status.bkg.warning"));
		else
			mf.statusL.setText(resources.getString("status.done"));
	}

	/**
	 * Reset to null (zero) the ambient background spectrum.
	 */
	private void noBKG() {
		for (int i = 0; i < mf.bkgpulsesD.length; i++) {
			mf.bkgpulsesD[i] = 0.0;
		}

		mf.globalBkgCounts = 0.0;

		mf.updateBkgFromDatabase();

		if (mf.roiV.size() != 0) {
			mustResetB = true;
			for (int i = 0; i < mf.roiV.size(); i++) {
				GammaRoi gr = mf.roiV.elementAt(i);
				double sC = gr.getStartChannel();
				double eC = gr.getEndChannel();
				gr.resetBkgData();
				int i1 = (new Double(sC)).intValue();
				int i2 = (new Double(eC)).intValue();
				for (int j = i1; j <= i2; j++) {
					gr.addBkgPulses(mf.bkgpulsesD[j]);
				}
			}
		}

		status();

		stopThread();// kill all threads
		statusL.setText(resources.getString("status.done"));
	}

	
	@SuppressWarnings("unchecked")
	/**
	 * Load spectrum from database.
	 */
	private void load() {
		try {
			// prepare db query data
			//String datas = resources.getString("data.load");
			//String currentDir = System.getProperty("user.dir");
			//String file_sep = System.getProperty("file.separator");
			//String opens = currentDir + file_sep + datas;
			//String dbName = gammaDB;
			//opens = opens + file_sep + dbName;
			// set tables
			//setTables();//NOT NEEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD
			// now do the job

			// make a connection
			//Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			JTable aspTable = genericdbagent.getMainTable();//asp.getTab();
			int selID = 0;// NO ZERO ID
			int selRow = aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) aspTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();

				stopThread();// kill all threads
				statusL.setText(resources.getString("status.done"));

				return;// nothing to load
			}
			// now we have an ID...load!
			String s = "select * from " + mainTableS + " WHERE ID = " + selID;
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			// int id=(Integer)DBOperation.getValueAt(0, 0);//first column id
			mf.spectrumName = (String) DatabaseAgent.getValueAt(0, 1);//DBOperation.getValueAt(0, 1);
			mf.idSpectrumTf.setText(mf.spectrumName);

			mf.spectrumLiveTime = (Double) DatabaseAgent.getValueAt(0, 2);//DBOperation.getValueAt(0, 2);
			mf.spectrumLiveTimeTf.setText(Convertor
					.doubleToString(mf.spectrumLiveTime));

			mf.measurementDate = (String) DatabaseAgent.getValueAt(0, 3);//DBOperation.getValueAt(0, 3);
			//TimeUtilities.unformatDate(mf.measurementDate);
			TimeUtilities tu = new TimeUtilities(mf.measurementDate);
			mf.dayCb.setSelectedItem((Object) tu.getDayS());//TimeUtilities.idayS);
			mf.monthCb.setSelectedItem((Object) tu.getMonthS());//TimeUtilities.imonthS);
			mf.yearTf.setText(tu.getYearS());//TimeUtilities.iyearS);

			mf.quantity = (Double) DatabaseAgent.getValueAt(0, 4);//DBOperation.getValueAt(0, 4);
			mf.quantityTf.setText(Convertor.doubleToString(mf.quantity));

			mf.quantityUnit = (String) DatabaseAgent.getValueAt(0, 5);//DBOperation.getValueAt(0, 5);
			mf.quantityUnitTf.setText(mf.quantityUnit);
			// spectrum
			mf.channelV = new Vector<String>();
			mf.pulsesV = new Vector<String>();

			// make sure data are order by nrcrt!!!
			s = "select * from " + dateTableS + " WHERE ID = " + selID
					+ " ORDER BY NRCRT";
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);
			mf.globalCounts = 0.0;
			for (int i = 0; i < DatabaseAgent.getRowCount(); i++) {//DBOperation.getRowCount(); i++) {
				mf.channelV.addElement(((Double) DatabaseAgent.getValueAt(i, 2))//DBOperation.getValueAt(i, 2))
						.toString());// channel
				mf.pulsesV.addElement(((Double) DatabaseAgent.getValueAt(i, 3))//DBOperation.getValueAt(i, 3))
						.toString());// pulses
				mf.globalCounts = mf.globalCounts
						+ (Double) DatabaseAgent.getValueAt(i, 3);//DBOperation.getValueAt(i, 3);
			}
			mf.updateSpectrumFromDatabase();
			// here via new chart channelI and pulsesD are populated
			// also the channelV and pulsesV are reset to null!!

			// ROIS
			s = "select * from " + roiTableS + " WHERE ID = " + selID
					+ " ORDER BY NRCRT";
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);
			for (int i = 0; i < DatabaseAgent.getRowCount(); i++) {//DBOperation.getRowCount(); i++) {
				double sC = (Double) DatabaseAgent.getValueAt(i, 2);////DBOperation.getValueAt(i, 2);// channel
				double eC = (Double) DatabaseAgent.getValueAt(i, 13);//DBOperation.getValueAt(i, 13);// channel
				GammaRoi gr = new GammaRoi(sC, eC);
				gr.setRoiSet(false);// no set but loaded ROI!!!
				// in constructor : setStartChannel(startChannel);
				// in constructor : setEndChannel(endChannel);
				// in constructor : setCenterChannel();
				// ---------------------------------
				int i1 = (new Double(sC)).intValue();
				int i2 = (new Double(eC)).intValue();
				for (int j = i1; j <= i2; j++) {
					gr.addChannel(mf.channelI[j]);
					gr.addPulses(mf.pulsesD[j]);
					gr.addBkgPulses(mf.bkgpulsesD[j]);
				}

				double d0 = (Double) DatabaseAgent.getValueAt(i, 34);//DBOperation.getValueAt(i, 34);
				gr.setStartEdgePulses(d0);
				d0 = (Double) DatabaseAgent.getValueAt(i, 35);//DBOperation.getValueAt(i, 35);
				gr.setEndEdgePulses(d0);
				gr.setLiveTime(mf.spectrumLiveTime);
				// ----------------------------------
				double d = 0.0;
				String str = "";
				d = (Double) DatabaseAgent.getValueAt(i, 3);// sEnergy
				gr.setStartEnergy(d);
				d = (Double) DatabaseAgent.getValueAt(i, 4);// sBkgch
				gr.setStartEdgeChannel(d);
				d = (Double) DatabaseAgent.getValueAt(i, 5);
				gr.setStartEdgeEnergy(d);// 6 is centerChannel already set!!
				d = (Double) DatabaseAgent.getValueAt(i, 7);
				gr.setCenterEnergy(d);
				d = (Double) DatabaseAgent.getValueAt(i, 8);
				gr.setCentroidChannel(d);
				d = (Double) DatabaseAgent.getValueAt(i, 9);
				gr.setCentroidEnergy(d);
				d = (Double) DatabaseAgent.getValueAt(i, 10);
				gr.setPeakChannel(d);
				d = (Double) DatabaseAgent.getValueAt(i, 11);
				gr.setPeakEnergy(d);
				d = (Double) DatabaseAgent.getValueAt(i, 12);
				gr.setPeakPulses(d);// 13 is endChannel already set!!
				d = (Double) DatabaseAgent.getValueAt(i, 14);
				gr.setEndEnergy(d);
				d = (Double) DatabaseAgent.getValueAt(i, 15);
				gr.setEndEdgeChannel(d);
				d = (Double) DatabaseAgent.getValueAt(i, 16);
				gr.setEndEdgeEnergy(d);
				d = (Double) DatabaseAgent.getValueAt(i, 17);
				gr.setFWHMChannel(d);
				d = (Double) DatabaseAgent.getValueAt(i, 18);
				gr.setFWHMChannelError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 19);
				gr.setFwhmEnergy(d);
				d = (Double) DatabaseAgent.getValueAt(i, 20);
				gr.setFwhmEnergyError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 21);
				gr.setFwhmEnergyCalib(d);
				d = (Double) DatabaseAgent.getValueAt(i, 22);
				gr.setFwhmEnergyCalibError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 23);
				gr.setResolution(d);
				d = (Double) DatabaseAgent.getValueAt(i, 24);
				gr.setResolutionCalib(d);
				str = (String) DatabaseAgent.getValueAt(i, 25);
				gr.setSignificance(str);
				d = (Double) DatabaseAgent.getValueAt(i, 26);
				gr.setBkgCounts(d);
				d = (Double) DatabaseAgent.getValueAt(i, 27);
				gr.setBkgCountsError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 28);
				gr.setBkgCountsRate(d);
				d = (Double) DatabaseAgent.getValueAt(i, 29);
				gr.setBkgCountsRateError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 30);
				gr.setGrossCounts(d);
				d = (Double) DatabaseAgent.getValueAt(i, 31);
				gr.setGrossCountsError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 32);
				gr.setGrossCountsRate(d);
				d = (Double) DatabaseAgent.getValueAt(i, 33);
				gr.setGrossCountsRateError(d);
				// 34,35=>startEdge abd endEdge pulses
				d = (Double) DatabaseAgent.getValueAt(i, 36);
				gr.setComptonCounts(d);
				d = (Double) DatabaseAgent.getValueAt(i, 37);
				gr.setComptonCountsError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 38);
				gr.setComptonCountsRate(d);
				d = (Double) DatabaseAgent.getValueAt(i, 39);
				gr.setComptonCountsRateError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 40);
				gr.setNetCounts(d);
				d = (Double) DatabaseAgent.getValueAt(i, 41);
				gr.setNetCountsError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 42);
				gr.setNetCountsRate(d);
				d = (Double) DatabaseAgent.getValueAt(i, 43);
				gr.setNetCountsRateError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 44);
				gr.setConfidenceLevel(d);
				str = (String) DatabaseAgent.getValueAt(i, 45);
				gr.setNuclide(str);
				d = (Double) DatabaseAgent.getValueAt(i, 46);
				gr.setYield(d);
				d = (Double) DatabaseAgent.getValueAt(i, 47);
				gr.setEfficiencyProcentual(d);
				d = (Double) DatabaseAgent.getValueAt(i, 48);
				gr.setEfficiencyProcentualError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 49);
				gr.setActivity_Bq(d);
				d = (Double) DatabaseAgent.getValueAt(i, 50);
				gr.setActivity_BqError(d);
				d = (Double) DatabaseAgent.getValueAt(i, 51);
				gr.setMda_Bq(d);
				d = (Double) DatabaseAgent.getValueAt(i, 52);
				gr.setMda_BqError(d);
				str = (String) DatabaseAgent.getValueAt(i, 53);
				gr.setDifference(str);
				d = (Double) DatabaseAgent.getValueAt(i, 54);
				gr.setAtomicMass(d);
				d = (Double) DatabaseAgent.getValueAt(i, 55);
				gr.setHalfLife(d);
				str = (String) DatabaseAgent.getValueAt(i, 56);
				gr.setHalfLifeUnits(str);
				str = (String) DatabaseAgent.getValueAt(i, 57);
				gr.setNetCalculationMethod(str);
				str = (String) DatabaseAgent.getValueAt(i, 58);
				gr.setMdaCalculationMethod(str);
				// -----------------------------------
				String strng = gr.getNetCalculationMethod();
				if (strng.equals("Default"))
					gr.setNetCalculationMethod(GammaRoi.NET_CALCULATION_NAI);
				else if (strng.equals("Ge_FWHM"))
					gr.setNetCalculationMethod(GammaRoi.NET_CALCULATION_GE);
				else if (strng.equals("Gaussian_Fit"))
					gr.setNetCalculationMethod(GammaRoi.NET_CALCULATION_GAUSSIAN);

				strng = gr.getMdaCalculationMethod();
				if (strng.equals("Pasternack"))
					gr.setMdaCalculationMethod(GammaRoi.MDA_CALCULATION_PASTERNACK);
				else if (strng.equals("Curie"))
					gr.setMdaCalculationMethod(GammaRoi.MDA_CALCULATION_CURIE);
				else if (strng.equals("Default"))
					gr.setMdaCalculationMethod(GammaRoi.MDA_CALCULATION_DEFAULT);
				// -------------------------------
				mf.roiV.addElement(gr);

				mf.roiCb.addItem(Convertor.intToString(mf.roiV.size()));
				mf.roiCb.setSelectedItem(Convertor.intToString(mf.roiV.size()));

				mf.updateRoiFromDatabase(gr);//(sC, eC);
			}

			//if (con1 != null)
				//con1.close();

		} catch (Exception e) {
			stopThread();// kill all threads
			statusL.setText(resources.getString("status.done"));

			e.printStackTrace();
		}

		stopThread();// kill all threads
		statusL.setText(resources.getString("status.spectrum.load"));

	}

	/**
	 * Delete spectrum data.
	 */
	private void delete() {
		JTable aspTable = genericdbagent.getMainTable();
		//int rowTableCount = aspTable.getRowCount();// =MAX ID!!

		int selID = 0;// NO ZERO ID
		int selRow = aspTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) aspTable.getValueAt(selRow, 0);
		} else {
			//if (con1 != null)
				//con1.close();

			stopThread();// kill all threads
			statusL.setText(resources.getString("status.done"));
			return;// nothing to delete
		}
		
		genericdbagent.delete(Convertor.intToString(selID));
		
		genericnesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		genericnesteddbagent.delete(genericnesteddbagent.getDatabaseTableName(),
				IDlink, Convertor.intToString(selID));
		
		genericroinesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		genericroinesteddbagent.delete(genericroinesteddbagent.getDatabaseTableName(),
				IDlink, Convertor.intToString(selID));
		
		//selection is done at the end
		genericdbagent.performSelection(orderbyS);
		stopThread();// kill all threads
		statusL.setText(resources.getString("status.spectrum.delete"));
		/*try {
			// prepare db query data
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;

			// set tables
			setTables();
			// now do the job

			// make a connection
			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			JTable aspTable = asp.getTab();
			int rowTableCount = aspTable.getRowCount();// =MAX ID!!

			int selID = 0;// NO ZERO ID
			int selRow = aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) aspTable.getValueAt(selRow, 0);
			} else {
				if (con1 != null)
					con1.close();

				stopThread();// kill all threads
				statusL.setText(resources.getString("status.done"));
				return;// nothing to delete
			}

			Statement s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			ResultSet res = s.executeQuery("SELECT * FROM " + mainTableS);
			PreparedStatement psUpdate = null;
			while (res.next()) {
				int id = res.getInt("ID");
				if (id == selID) {
					res.deleteRow();
				} else if (id > selID) {
					// since in this table ID is UNIQUE and ASCENDING, we can
					// make
					// on-the fly update
					psUpdate = con1.prepareStatement("update " + mainTableS
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, id - 1);
					psUpdate.setInt(2, id);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			}
			// now detail table
			s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM " + dateTableS);
			while (res.next()) {
				int id = res.getInt("ID");
				if ((id == selID)) {
					res.deleteRow();
				}
			}

			if (selID + 1 <= rowTableCount)
				for (int i = selID + 1; i <= rowTableCount; i++) {
					psUpdate = con1.prepareStatement("update " + dateTableS
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, i - 1);
					psUpdate.setInt(2, i);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			// /now the roi Tables
			s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM " + roiTableS);
			while (res.next()) {
				int id = res.getInt("ID");
				if ((id == selID)) {
					res.deleteRow();
				}
			}

			if (selID + 1 <= rowTableCount)
				for (int i = selID + 1; i <= rowTableCount; i++) {
					psUpdate = con1.prepareStatement("update " + roiTableS
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, i - 1);
					psUpdate.setInt(2, i);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			// ////////

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
			stopThread();// kill all threads
			statusL.setText(resources.getString("status.done"));

			e.printStackTrace();
		}
		stopThread();// kill all threads
		statusL.setText(resources.getString("status.spectrum.delete"));*/
	}
	
	/**
	 * Update GUI based on BKG, standard source or sample selection.
	 */
	private void updateGUI(){
		
		genericrecordLabel = genericdbagent.getRecordsLabel();
		genericnestedrecordLabel = genericnesteddbagent.getRecordsLabel();
		genericroinestedrecordLabel = genericroinesteddbagent.getRecordsLabel();
		
		genericorderbyCb = genericdbagent.getOrderByComboBox();
		genericnestedorderbyCb = genericnesteddbagent.getOrderByComboBox();
		genericroinestedorderbyCb = genericroinesteddbagent.getOrderByComboBox();
		genericorderbyCb.addItemListener(this);
		genericorderbyCb.setMaximumRowCount(5);
		genericorderbyCb.setPreferredSize(sizeOrderCb);
		genericnestedorderbyCb.addItemListener(this);
		genericnestedorderbyCb.setMaximumRowCount(5);
		genericnestedorderbyCb.setPreferredSize(sizeOrderCb);
		genericroinestedorderbyCb.addItemListener(this);
		genericroinestedorderbyCb.setMaximumRowCount(5);
		genericroinestedorderbyCb.setPreferredSize(sizeOrderCb);
			
		//=================================================================================================
		orderP.removeAll();//validate();//here VALIDATE WORKS....
		//seems not needed though EXCEPT for record counts label...not anymore!!!!!!
		JLabel label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP.add(label);
		orderP.add(genericorderbyCb);
		orderP.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP.add(label);
		orderP.add(genericrecordLabel);
		
		orderP2.removeAll();//validate();//here IT WORKS!!!
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP2.add(label);
		orderP2.add(genericnestedorderbyCb);
		orderP2.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP2.add(label);
		orderP2.add(genericnestedrecordLabel);
		
		orderP3.removeAll();//validate();//here IT WORKS!!!
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP3.add(label);
		orderP3.add(genericroinestedorderbyCb);
		orderP3.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP3.add(label);
		orderP3.add(genericroinestedrecordLabel);
		
		suportSp.removeAll();//validate();
		suportSpDate.removeAll();//validate();
		suportSpDateRois.removeAll();//validate();
		
		JScrollPane scrollPane = new JScrollPane(genericmainTable);
		//genericmainTable.setFillsViewportHeight(true);
		//genericmainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		suportSp.add(scrollPane, BorderLayout.CENTER);
		// BorderLayout interprets the absence of a string specification the same as the constant CENTER:
		//so suportSp.add(scrollPane) = suportSp.add(scrollPane, BorderLayout.CENTER);!!!!!!!!!!
		
		JScrollPane scrollPane2 = new JScrollPane(genericnestedTable);
		//genericnestedTable.setFillsViewportHeight(true);
		suportSpDate.add(scrollPane2, BorderLayout.CENTER);
		
		JScrollPane scrollPane3 = new JScrollPane(genericroinestedTable);
		//genericroinestedTable.setFillsViewportHeight(true);
		suportSpDateRois.add(scrollPane3, BorderLayout.CENTER);		
	}

	/**
	 * Show background database.
	 */
	private void showBkgData() {
		// System.out.println("show bkg");
		setDisplayMode(BKG_DISPLAY_MODE);
		//performCurrentSelection();
		statusL.setText(resources.getString("status.bkg.display"));
		//------------
		setTables();//here agents are set.	
		
		updateGUI();
		stopThread();// kill all threads
		statusL.setText(resources.getString("status.done"));
	}
	
	

	/**
	 * Show standard source database.
	 */
	private void showStandardSourceData() {
		// System.out.println("show standard source");
		setDisplayMode(STANDARDSOURCE_DISPLAY_MODE);
		//performCurrentSelection();
		statusL.setText(resources.getString("status.standardSource.display"));		
		//-----------
		setTables();
				
		updateGUI();
		stopThread();// kill all threads
		statusL.setText(resources.getString("status.done"));
	}

	/**
	 * Show sample database.
	 */
	private void showSampleData() {
		// System.out.println("show sample");
		setDisplayMode(SAMPLE_DISPLAY_MODE);
		//performCurrentSelection();
		statusL.setText(resources.getString("status.sample.display"));
		//-----------
		setTables();
						
		updateGUI();
		stopThread();// kill all threads
		statusL.setText(resources.getString("status.done"));
	}

	/**
	 * Start computation thread.
	 */
	private void startThread() {
		stopAnim=false;
		if (computationTh == null) {
			computationTh = new Thread(this);
			computationTh.start();// Allow one simulation at time!
			setEnabled(false);
		}

		if (statusTh == null) {
			statusTh = new Thread(this);
			statusTh.start();
		}
	}

	/**
	 * Stop computation thread.
	 */
	private void stopThread() {
		stopAnim=true;
		statusTh = null;
		frameNumber = 0;

		computationTh = null;
		setEnabled(true);//here yes..do not close frame 
		//until DB operations (load or save mainly) are completed!!
	}
	private boolean stopAnim=true;
	/**
	 * Thread specific run method
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
			if (command.equals(SAVE_COMMAND)) {
				save();
			} else if (command.equals(DELETE_COMMAND)) {
				delete();
			} else if (command.equals(LOAD_COMMAND)) {
				load();
			} else if (command.equals(USEBKG_COMMAND)) {
				useBKG();
			} else if (command.equals(NOBKG_COMMAND)) {
				noBKG();
			} else if (command.equals(BKG_COMMAND)) {
				showBkgData();
			} else if (command.equals(STANDARDSOURCE_COMMAND)) {
				showStandardSourceData();				
			} else if (command.equals(SAMPLE_COMMAND)) {
				showSampleData();				
			}

		}
	}
}