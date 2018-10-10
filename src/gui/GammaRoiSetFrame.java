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

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import danfulea.db.DatabaseAgent;
import danfulea.db.DatabaseAgentSupport;
import danfulea.math.Convertor;
//import jdf.db.AdvancedSelectPanel;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.phys.GammaRoi;
import danfulea.utils.FrameUtilities;

/**
 * Class for assigning ROI to a nuclide from library. <br>
 * 
 * @author Dan Fulea, 08 Jul. 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaRoiSetFrame extends JFrame implements ActionListener,
ItemListener, ListSelectionListener {

	private String command;
	private static final String ROISET_COMMAND = "ROISET";
	private GammaAnalysisFrame mf;
	private GammaRoi gr;
	private final Dimension PREFERRED_SIZE = new Dimension(800, 700);
	private final Dimension tableDimension = new Dimension(700, 200);
	private final Dimension sizeCb = new Dimension(110, 21);
	private final Dimension labelDimension = new Dimension(200, 30);
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;
	//private AdvancedSelectPanel asp = null;
	private JPanel suportSp = new JPanel(new BorderLayout());
	//private AdvancedSelectPanel aspDate = null;
	private JPanel suportSpDate = new JPanel(new BorderLayout());
	//private AdvancedSelectPanel aspCorr = null;// coin corr
	private JPanel suportSpCorr = new JPanel(new BorderLayout());
	private String gammaDB = "";
	private String gammaNuclidesTable = "";
	private String gammaNuclidesDetailsTable = "";
	private String gammaNuclidesCoincidenceTable = "";
	private JLabel roiLb = new JLabel();
	@SuppressWarnings("rawtypes")
	private JComboBox mdaCb;
	private int idRoi=0;

	/**
	 * The connection
	 */
	private Connection gammadbcon = null;
	
	/**
	 * Main table primary key column name
	 */
	private String mainTablePrimaryKey = "ID";//nuclide
	
	/**
	 * Nested table primary key column name
	 */
	private String nestedTablePrimaryKey = "NRCRT";//nuclideDetail
	private String coinnestedTablePrimaryKey = "NRCRT";//coin correction for that nuclide
	
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
	private JTable coinnestedTable;
	
	/**
	 * The column used for sorting data in nested table (ORDER BY SQL syntax)
	 */
	private String nestedorderbyS = "NRCRT";
	private String coinnestedorderbyS = "NRCRT";
	/**
	 * The database agent associated to main table
	 */
	private DatabaseAgentSupport dbagent;
	
	/**
	 * The database agent associated to nested table (nuclide details)
	 */
	private DatabaseAgentSupport nesteddbagent;
	/**
	 * The database agent associated to coincidence nested table
	 */
	private DatabaseAgentSupport coinnesteddbagent;
	
	private JComboBox<String> orderbyCb;
	private JComboBox<String> nestedorderbyCb;
	private JComboBox<String> coinnestedorderbyCb;
	private final Dimension sizeOrderCb = new Dimension(200, 21);
	
	/**
	 * Constructor.
	 * @param mf the GammaAnalysisFrame object
	 * @param gr the GammaRoi object
	 * @param idRoi the ROI ID
	 */
	public GammaRoiSetFrame(GammaAnalysisFrame mf, GammaRoi gr, int idRoi) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("RoiSet.NAME"));

		this.mf = mf;
		this.gr = gr;
		this.idRoi=idRoi;

		gammaDB = resources.getString("main.db");
		gammaNuclidesTable = resources.getString("main.db.gammaNuclidesTable");
		gammaNuclidesDetailsTable = resources
				.getString("main.db.gammaNuclidesDetailsTable");
		gammaNuclidesCoincidenceTable = resources
				.getString("main.db.gammaNuclidesCoincidenceTable");
		//-====================
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		mainTablePrimaryKey = "ID";
		nestedTablePrimaryKey = "NRCRT";coinnestedTablePrimaryKey = "NRCRT";
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
		coinnesteddbagent = new DatabaseAgentSupport(gammadbcon, coinnestedTablePrimaryKey,
				gammaNuclidesCoincidenceTable);	
		dbagent.setHasValidAIColumn(false);
		nesteddbagent.setHasValidAIColumn(false);
		coinnesteddbagent.setHasValidAIColumn(false);
		//======================
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
	 * Exit method
	 */
	private void attemptExit() {
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

	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * Create GUI
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
		orderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
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
		orderP2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP2.add(label);
		orderP2.add(nestedorderbyCb);
		orderP2.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP2.add(label);
		orderP2.add(nesteddbagent.getRecordsLabel());
		
		coinnestedorderbyCb = coinnesteddbagent.getOrderByComboBox();
		coinnestedorderbyCb.setMaximumRowCount(5);
		coinnestedorderbyCb.setPreferredSize(sizeOrderCb);
		coinnestedorderbyCb.addItemListener(this);
		JPanel orderP3 = new JPanel();
		orderP3.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP3.add(label);
		orderP3.add(coinnestedorderbyCb);
		orderP3.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(GammaAnalysisFrame.foreColor);
		orderP3.add(label);
		orderP3.add(coinnesteddbagent.getRecordsLabel());
		//===================================================
		String[] sarray = (String[]) resources.getObject("mdaCb");
		mdaCb = new JComboBox(sarray);
		mdaCb.setMaximumRowCount(5);
		mdaCb.setPreferredSize(sizeCb);
		String s = sarray[2];// default
		mdaCb.setSelectedItem((Object) s);

		roiLb.setText(resources.getString("roiSet.frame.label")+"NoName");// +gr.getNuclide());
		roiLb.setForeground(GammaAnalysisFrame.foreColor);

		buttonName = resources.getString("roiSet.frame.set");
		buttonToolTip = resources.getString("roiSet.frame.set.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, ROISET_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("roiSet.frame.set.mnemonic");
		button.setMnemonic(mnemonic.charValue());
	
		JPanel r1=new JPanel();
		r1.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 2));
		r1.setBackground(GammaAnalysisFrame.bkgColor);
		r1.add(roiLb);
		r1.setPreferredSize(labelDimension);
		
		label = new JLabel(resources.getString("rb.mda.border"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		JPanel p1P = new JPanel();
		p1P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p1P.setBackground(GammaAnalysisFrame.bkgColor);
		p1P.add(label);
		p1P.add(mdaCb);
		p1P.add(button);
		//p1P.add(roiLb);
		p1P.add(r1);

		suportSp.setPreferredSize(tableDimension);
		suportSpDate.setPreferredSize(tableDimension);
		suportSpCorr.setPreferredSize(tableDimension);
		suportSpCorr.setBackground(GammaAnalysisFrame.bkgColor);
		suportSpDate.setBackground(GammaAnalysisFrame.bkgColor);
		suportSp.setBackground(GammaAnalysisFrame.bkgColor);
		
		JScrollPane scrollPane = new JScrollPane(mainTable);
		mainTable.setFillsViewportHeight(true);
		suportSp.add(scrollPane);
		JScrollPane scrollPane2 = new JScrollPane(nestedTable);
		nestedTable.setFillsViewportHeight(true);
		suportSpDate.add(scrollPane2);
		JScrollPane scrollPane3 = new JScrollPane(coinnestedTable);
		coinnestedTable.setFillsViewportHeight(true);
		suportSpCorr.add(scrollPane3);
		
		JPanel s1=new JPanel();
		BoxLayout bls1 = new BoxLayout(s1, BoxLayout.Y_AXIS);
		s1.setLayout(bls1);//new FlowLayout(FlowLayout.CENTER, 20, 2));
		s1.setBackground(GammaAnalysisFrame.bkgColor);
		s1.add(orderP);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		s1.add(suportSp);
		JPanel s2=new JPanel();
		BoxLayout bls2 = new BoxLayout(s2, BoxLayout.Y_AXIS);
		s2.setLayout(bls2);//new FlowLayout(FlowLayout.CENTER, 20, 2));
		s2.setBackground(GammaAnalysisFrame.bkgColor);
		s2.add(orderP2);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		s2.add(suportSpDate);
		JPanel s3=new JPanel();
		BoxLayout bls3 = new BoxLayout(s3, BoxLayout.Y_AXIS);
		s3.setLayout(bls3);//new FlowLayout(FlowLayout.CENTER, 20, 2));
		s3.setBackground(GammaAnalysisFrame.bkgColor);
		s3.add(orderP3);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		s3.add(suportSpCorr);
		
		//JPanel r1=new JPanel();
		//BoxLayout blr1 = new BoxLayout(r1, BoxLayout.Y_AXIS);
		//r1.setLayout(blr1);
		//r1.setBackground(mf.bkgColor);
		//r1.add(s1);
		//r1.add(p1P);

		
		JPanel mainP = new JPanel();
		BoxLayout bl = new BoxLayout(mainP, BoxLayout.Y_AXIS);
		mainP.setLayout(bl);
		mainP.setBackground(GammaAnalysisFrame.bkgColor);
		mainP.add(s1);//suportSp);
		mainP.add(p1P);
		//mainP.add(r1);
		mainP.add(s2);//suportSpDate);
		mainP.add(s3);//suportSpCorr);

		JPanel content = new JPanel(new BorderLayout());
		content.add(mainP,BorderLayout.CENTER);//content.setBackground(mf.bkgColor);
		setContentPane(new JScrollPane(content));
		pack();
	}

	/**
	 * Initialize database.
	 */
	private void performQueryDb() {
		dbagent.init();
		orderbyS = mainTablePrimaryKey;// when start-up...ID is default!!
		
		nesteddbagent.init();
		nestedorderbyS = nestedTablePrimaryKey;
		
		coinnesteddbagent.init();
		coinnestedorderbyS = coinnestedTablePrimaryKey;
		
		mainTable = dbagent.getMainTable();
		// allow single selection of rows...not multiple rows!
		ListSelectionModel rowSM = mainTable.getSelectionModel();
		rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		rowSM.addListSelectionListener(this);// listener!	
		
		nestedTable = nesteddbagent.getMainTable();
		ListSelectionModel rowSM2 = nestedTable.getSelectionModel();
		rowSM2.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//rowSM2.addListSelectionListener(this);
		
		coinnestedTable = coinnesteddbagent.getMainTable();
		ListSelectionModel rowSM3 = coinnestedTable.getSelectionModel();
		rowSM3.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
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

				// ---------------------------------
				s = "select * from " + gammaNuclidesCoincidenceTable;
				DBOperation.select(s, con1);

				if (aspCorr != null)
					suportSpCorr.remove(aspCorr);

				aspCorr = new AdvancedSelectPanel();
				suportSpCorr.add(aspCorr, BorderLayout.CENTER);

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
			return;//!!!!!!!!!!!!!!
		}
		// ===update nested===
		nesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		nesteddbagent.performSelection(nestedorderbyS);
		// ===update nested===
		coinnesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
		coinnesteddbagent.performSelection(coinnestedorderbyS);				
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

			// =======================
			s = "select * from " + gammaNuclidesCoincidenceTable
					+ " where ID = " + selID + " ORDER BY NRCRT";
			// IF press header=>selRow=-1=>ID=0=>NO ZERO ID DATA so display
			// empty!
			con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			if (aspCorr != null)
				suportSpCorr.remove(aspCorr);

			aspCorr = new AdvancedSelectPanel();
			suportSpCorr.add(aspCorr, BorderLayout.CENTER);

			JTable corrTable = aspCorr.getTab();
			ListSelectionModel row2SM = corrTable.getSelectionModel();
			row2SM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			validate();

			if (con1 != null)
				con1.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}*/

	}

	/**
	 * Most actions are set here
	 */
	public void actionPerformed(ActionEvent arg0) {
		command = arg0.getActionCommand();
		if (command.equals(ROISET_COMMAND)) {
			roiSet();
		}
	}
	
	/**
	 * JCombobox related actions are set here
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == orderbyCb) {
			sort();
		} else if (ie.getSource() == nestedorderbyCb) {
			sort2();
		} else if (ie.getSource() == coinnestedorderbyCb) {
			sort3();
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
	
	/**
	 * Sorts data from coincidence nested table
	 */
	private void sort3() {
		coinnestedorderbyS = (String) coinnestedorderbyCb.getSelectedItem();
		
		//JTable aspTable = asp.getTab();
		int selID = 0;// NO ZERO ID
		int selRow = mainTable.getSelectedRow();//aspTable.getSelectedRow();
		if (selRow != -1) {
			selID = (Integer) mainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
		} else {
			return;
		}

		// ===update nested===
		coinnesteddbagent.setLinks(IDlink, Convertor.intToString(selID));
				
		coinnesteddbagent.performSelection(coinnestedorderbyS);
	}
	
	/**
	 * Perform ROI assignment and compute relevant quantities (update ROI).
	 */
	private void roiSet() {
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

			String s = "select * from " + gammaNuclidesTable + " where ID = "
					+ linkID;
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);
			// one row!
			String nuclide = (String) DatabaseAgent.getValueAt(0, 1);//DBOperation.getValueAt(0, 1);
			double atomicMass = (Double) DatabaseAgent.getValueAt(0, 2);//DBOperation.getValueAt(0, 2);
			double halfLife = (Double) DatabaseAgent.getValueAt(0, 3);//DBOperation.getValueAt(0, 3);
			String halfLifeUnits = (String) DatabaseAgent.getValueAt(0, 4);//DBOperation.getValueAt(0, 4);
			gr.setNuclide(nuclide);
			gr.setAtomicMass(atomicMass);
			gr.setHalfLife(halfLife);
			gr.setHalfLifeUnits(halfLifeUnits);

			gr.setMdaCalculationMethod(mdaCb.getSelectedIndex());
			// 0-Pasternack;1-Curie;2-Default

			s = "select * from " + gammaNuclidesDetailsTable + " where ID = "
					+ linkID + " ORDER BY NRCRT";
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			int ndata = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
			double[] energies = new double[ndata];
			double[] yields = new double[ndata];
			double[] decayCorr = new double[ndata];
			for (int i = 0; i < ndata; i++) {
				energies[i] = (Double) DatabaseAgent.getValueAt(i, 2);//DBOperation.getValueAt(i, 2);
				yields[i] = (Double) DatabaseAgent.getValueAt(i, 3);//DBOperation.getValueAt(i, 3);
				decayCorr[i] = (Double) DatabaseAgent.getValueAt(i, 6);//DBOperation.getValueAt(i, 6);
			}

			s = "select * from " + gammaNuclidesCoincidenceTable
					+ " where ID = " + linkID + " ORDER BY NRCRT";// order by
																	// not
																	// necessary!!
			//DBOperation.select(s, con1);
			DatabaseAgent.select(gammadbcon, s);

			ndata = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
			double[] energiesCorr = new double[ndata];// coincidence table!
			double[] yieldsCorr = new double[ndata];
			double[] coinCorr = new double[ndata];
			for (int i = 0; i < ndata; i++) {
				energiesCorr[i] = (Double) DatabaseAgent.getValueAt(i, 2);//DBOperation.getValueAt(i, 2);
				yieldsCorr[i] = (Double) DatabaseAgent.getValueAt(i, 3);//DBOperation.getValueAt(i, 3);
				coinCorr[i] = (Double) DatabaseAgent.getValueAt(i, 6);//DBOperation.getValueAt(i, 6);
			}

			gr.updateRoi(energies, yields, decayCorr, energiesCorr, yieldsCorr,
					coinCorr);
			roiLb.setText(resources.getString("roiSet.frame.label")
					+ gr.getNuclide());
			mf.roiNamelabel.setText(resources.getString("roi.name.label")
					+ gr.getNuclide());
			mf.updateRoiInfo(gr, idRoi);
			//if (con1 != null)
				//con1.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}
