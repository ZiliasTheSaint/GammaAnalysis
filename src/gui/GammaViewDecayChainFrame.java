package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ResourceBundle;

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
import danfulea.utils.FrameUtilities;

/**
 * View decay chain informations. <br>
 * 
 * @author Dan Fulea, 26 May 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaViewDecayChainFrame extends JFrame implements
ItemListener, ListSelectionListener{
	private GammaLibraryFrame mf;
	
	//private AdvancedSelectPanel asp = null;
	//private AdvancedSelectPanel aspDate = null;
	private JPanel suportSp = new JPanel(new BorderLayout());
	private JPanel suportSpDate = new JPanel(new BorderLayout());
	
	private final Dimension PREFERRED_SIZE = new Dimension(500, 520);
	private final Dimension tableDimension = new Dimension(400, 200);
	
	private String gammaDB="";
	private String gammaNuclidesTable = "";
	private String gammaNuclidesDecayChainTable="";
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;

	static int IDLink=0;
	
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
	 * The database agent associated to nested table (nuclide details)
	 */
	private DatabaseAgentSupport nesteddbagent;
		
	private JComboBox<String> orderbyCb;
	private JComboBox<String> nestedorderbyCb;
	
	private final Dimension sizeOrderCb = new Dimension(200, 21);
	
	/**
	 * Constructor
	 * @param mf the GammaLibraryFrame object
	 */
	public GammaViewDecayChainFrame(GammaLibraryFrame mf) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("ViewChain.NAME"));
		this.mf = mf;
	
		gammaDB = resources.getString("main.db");
		gammaNuclidesTable = resources.getString("main.db.gammaNuclidesTable");
		gammaNuclidesDecayChainTable= resources
		.getString("main.db.gammaNuclidesDecayChainTable");
		//=================================
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
				gammaNuclidesDecayChainTable);	
		dbagent.setHasValidAIColumn(false);
		nesteddbagent.setHasValidAIColumn(false);	
		//========================================================
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
	
	/**
	 * Create GUI
	 */
	private void createGUI() {
		orderbyCb = dbagent.getOrderByComboBox();
		orderbyCb.setMaximumRowCount(5);
		orderbyCb.setPreferredSize(sizeOrderCb);
		orderbyCb.addItemListener(this);
		JPanel orderP = new JPanel();
		orderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		JLabel label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(mf.foreColor);
		orderP.add(label);
		orderP.add(orderbyCb);
		orderP.setBackground(mf.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(mf.foreColor);
		orderP.add(label);
		orderP.add(dbagent.getRecordsLabel());
				
		nestedorderbyCb = nesteddbagent.getOrderByComboBox();
		nestedorderbyCb.setMaximumRowCount(5);
		nestedorderbyCb.setPreferredSize(sizeOrderCb);
		nestedorderbyCb.addItemListener(this);
		JPanel orderP2 = new JPanel();
		orderP2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(mf.foreColor);
		orderP2.add(label);
		orderP2.add(nestedorderbyCb);
		orderP2.setBackground(mf.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(mf.foreColor);
		orderP2.add(label);
		orderP2.add(nesteddbagent.getRecordsLabel());
		
		suportSp.setPreferredSize(tableDimension);
		suportSpDate.setPreferredSize(tableDimension);
		JScrollPane scrollPane = new JScrollPane(mainTable);
		mainTable.setFillsViewportHeight(true);
		suportSp.add(scrollPane);
		JScrollPane scrollPane2 = new JScrollPane(nestedTable);
		nestedTable.setFillsViewportHeight(true);
		suportSpDate.add(scrollPane2);
		
		JPanel p1P = new JPanel();
		p1P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p1P.setBackground(mf.bkgColor);
		p1P.add(suportSp);
		JPanel p2P = new JPanel();
		p2P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p2P.setBackground(mf.bkgColor);
		p2P.add(suportSpDate);
		
		//-------------
		JPanel content1 = new JPanel(new BorderLayout());
		content1.add(orderP, BorderLayout.NORTH);
		content1.add(p1P, BorderLayout.SOUTH);
		content1.setBackground(mf.bkgColor);
		
		JPanel content2 = new JPanel(new BorderLayout());
		content2.add(orderP2, BorderLayout.NORTH);
		content2.add(p2P, BorderLayout.SOUTH);
		content2.setBackground(mf.bkgColor);
		//---------------
		
		JPanel content = new JPanel(new BorderLayout());
		content.add(content1, BorderLayout.NORTH);
		content.add(content2, BorderLayout.SOUTH);
		
		setContentPane(new JScrollPane(content));
		content.setOpaque(true); // content panes must be opaque
		content.setBackground(mf.bkgColor);
		pack();

	}

	/**
	 * Initialize database
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

			if (mainTable.getRowCount() > 0){
				//mainTable.setRowSelectionInterval(mainTable.getRowCount()-1,mainTable.getRowCount()-1);
				mainTable.setRowSelectionInterval(IDLink,IDLink);
			}
			else {
				// empty table..display header
				s = "select * from " + gammaNuclidesDecayChainTable;
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
	 * JCombobox related actions are set here
	 */
	public void itemStateChanged(ItemEvent ie) {
		if (ie.getSource() == orderbyCb) {
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
			return;//!!!!!!!!!!!!!!it was commented
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

			String s = "select * from " + gammaNuclidesDecayChainTable
					+ " where ID = " + selID+
					" ORDER BY NRCRT";
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

}
