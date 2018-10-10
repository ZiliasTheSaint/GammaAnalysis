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
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ResourceBundle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import danfulea.db.DatabaseAgent;
import danfulea.db.DatabaseAgentSupport;
//import jdf.db.AdvancedSelectPanel;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.math.Convertor;
import danfulea.phys.GammaRoi;
import danfulea.utils.FrameUtilities;

/**
 * Class for save/set the energy/FWHM calibration. <br>
 * 
 * @author Dan Fulea, 09 Jul. 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaEnergyFWHMCalibrationSaveFrame extends JFrame implements
		ActionListener, ItemListener {
	public boolean changedEnergyCal = false;
	public boolean changedFWHMCal = false;
	private String command;
	private static final String SAVEENERGYCAL_COMMAND = "SAVEENERGYCAL";
	private static final String SAVEFWHMCAL_COMMAND = "SAVEFWHMCAL";
	private static final String SETENERGYCAL_COMMAND = "SETENERGYCAL";
	private static final String SETFWHMCAL_COMMAND = "SETFWHMCAL";
	private static final String DELETEENERGYCAL_COMMAND = "DELETEENERGYCAL";
	private static final String DELETEFWHMCAL_COMMAND = "DELETEFWHMCAL";
	GammaEnergyFWHMCalibrationFrame mf;

	private final Dimension PREFERRED_SIZE = new Dimension(1000, 700);//was 950
	private final Dimension tableDimension = new Dimension(500, 200);
	private final Dimension sizeCb = new Dimension(60, 21);

	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;

	//private AdvancedSelectPanel aspE = null;
	//private AdvancedSelectPanel aspF = null;
	private JPanel suportSpE = new JPanel(new BorderLayout());
	private JPanel suportSpF = new JPanel(new BorderLayout());
	@SuppressWarnings("rawtypes")
	private JComboBox useECb, useFCb;
	private JTextField energyCalNameTf = new JTextField(28);
	private JTextField fwhmCalNameTf = new JTextField(28);
	private String gammaDB = "";
	private String gammaEnergyCalibrationTable = "";
	private String gammaFWHMCalibrationTable = "";

	/**
	 * The connection
	 */
	private Connection gammadbcon = null;
	/**
	 * Energy main table primary key column name
	 */
	private String enmainTablePrimaryKey = "ID";
	/**
	 * The JTable component associated to energy main table
	 */
	private JTable enmainTable;
	/**
	 * The column used for sorting data in energy main table (ORDER BY SQL syntax)
	 */
	private String enorderbyS = "ID";
	/**
	 * The database agent associated to energy main table
	 */
	private DatabaseAgentSupport endbagent;
	/**
	 * FWHM main table primary key column name
	 */
	private String fwhmmainTablePrimaryKey = "ID";
	/**
	 * The JTable component associated to fwhm main table
	 */
	private JTable fwhmmainTable;
	/**
	 * The column used for sorting data in fwhm main table (ORDER BY SQL syntax)
	 */
	private String fwhmorderbyS = "ID";
	/**
	 * The database agent associated to fwhm main table
	 */
	private DatabaseAgentSupport fwhmdbagent;
	
	private JComboBox<String> enorderbyCb;
	private JComboBox<String> fwhmorderbyCb;
	private final Dimension sizeOrderCb = new Dimension(200, 21);
	
	/**
	 * Constructor
	 * @param mf the GammaEnergyFWHMCalibrationFrame object
	 */
	public GammaEnergyFWHMCalibrationSaveFrame(
			GammaEnergyFWHMCalibrationFrame mf) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("Energy.FWHM.SAVE.NAME"));
		this.mf = mf;
		changedEnergyCal = false;
		changedFWHMCal = false;
		gammaDB = resources.getString("main.db");
		gammaEnergyCalibrationTable = resources.getString("main.db.enCalib");
		gammaFWHMCalibrationTable = resources.getString("main.db.fwhmCalib");
		//======================================================================
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = gammaDB;
		opens = opens + file_sep + dbName;
		gammadbcon = DatabaseAgent.getConnection(opens, "", "");
				
		enmainTablePrimaryKey = "ID";		
		endbagent = new DatabaseAgentSupport(gammadbcon, 
		enmainTablePrimaryKey, gammaEnergyCalibrationTable);
		endbagent.setHasValidAIColumn(false);
		
		fwhmmainTablePrimaryKey = "ID";		
		fwhmdbagent = new DatabaseAgentSupport(gammadbcon, 
		fwhmmainTablePrimaryKey, gammaFWHMCalibrationTable);
		fwhmdbagent.setHasValidAIColumn(false);
		//======================================================================
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

		mf.changeCalibration = changedEnergyCal | changedFWHMCal;
	}

	/**
	 * Setting up the frame size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

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
		suportSpE.remove(aspE);
		suportSpF.remove(aspF);
		performQueryDb();
		validate();
	}*/

	/**
	 * Initialize database.
	 */
	private void performQueryDb() {
		endbagent.init();
		enorderbyS = enmainTablePrimaryKey;
		
		enmainTable = endbagent.getMainTable();
		
		ListSelectionModel rowSM = enmainTable.getSelectionModel();
		rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//rowSM.addListSelectionListener(this);// listener!
		if (enmainTable.getRowCount() > 0){
			//select last row!
			enmainTable.setRowSelectionInterval(enmainTable.getRowCount() - 1,
					enmainTable.getRowCount() - 1); // last ID
		}
		
		fwhmdbagent.init();
		fwhmorderbyS = fwhmmainTablePrimaryKey;
		
		fwhmmainTable = fwhmdbagent.getMainTable();
		
		ListSelectionModel rowSM1 = fwhmmainTable.getSelectionModel();
		rowSM1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//rowSM.addListSelectionListener(this);// listener!
		if (fwhmmainTable.getRowCount() > 0){
			//select last row!
			fwhmmainTable.setRowSelectionInterval(fwhmmainTable.getRowCount() - 1,
					fwhmmainTable.getRowCount() - 1); // last ID
		}
		
		/**
		try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			String s = "select * from " + gammaEnergyCalibrationTable;
			DBOperation.select(s, con1);

			aspE = new AdvancedSelectPanel();
			suportSpE.add(aspE, BorderLayout.CENTER);

			JTable mainTable = aspE.getTab();
			if (mainTable.getRowCount() > 0) {
				mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
						mainTable.getRowCount() - 1);
			}
			ListSelectionModel rowSM = mainTable.getSelectionModel();
			rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			s = "select * from " + gammaFWHMCalibrationTable;
			DBOperation.select(s, con1);

			aspF = new AdvancedSelectPanel();
			suportSpF.add(aspF, BorderLayout.CENTER);

			mainTable = aspF.getTab();
			if (mainTable.getRowCount() > 0) {
				mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
						mainTable.getRowCount() - 1);
			}
			rowSM = mainTable.getSelectionModel();
			rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	/**
	 * Create GUI
	 */
	private void createGUI() {
		String[] sarray = new String[2];
		sarray[0] = resources.getString("difference.yes");
		sarray[1] = resources.getString("difference.no");
		useECb = new JComboBox(sarray);
		useECb.setMaximumRowCount(2);
		useECb.setPreferredSize(sizeCb);
		useFCb = new JComboBox(sarray);
		useFCb.setMaximumRowCount(2);
		useFCb.setPreferredSize(sizeCb);
		String str = sarray[0];// yes
		useECb.setSelectedItem((Object) str);
		useFCb.setSelectedItem((Object) str);

		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";
		//-------------------------
		enorderbyCb = endbagent.getOrderByComboBox();
		enorderbyCb.setMaximumRowCount(5);
		enorderbyCb.setPreferredSize(sizeOrderCb);
		enorderbyCb.addItemListener(this);
		JPanel orderP = new JPanel();
		orderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(mf.foreColor);
		orderP.add(label);
		orderP.add(enorderbyCb);
		orderP.setBackground(mf.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(mf.foreColor);
		orderP.add(label);
		orderP.add(endbagent.getRecordsLabel());// recordsCount);
		
		fwhmorderbyCb = fwhmdbagent.getOrderByComboBox();
		fwhmorderbyCb.setMaximumRowCount(5);
		fwhmorderbyCb.setPreferredSize(sizeOrderCb);
		fwhmorderbyCb.addItemListener(this);
		JPanel orderP2 = new JPanel();
		orderP2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(mf.foreColor);
		orderP2.add(label);
		orderP2.add(fwhmorderbyCb);
		orderP2.setBackground(mf.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(mf.foreColor);
		orderP2.add(label);
		orderP2.add(fwhmdbagent.getRecordsLabel());// recordsCount);
		//-----------------------------------------		
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(
				resources.getString("energy.fwhm.saveCal.energyCalNameLb"));label.setForeground(mf.foreColor);
		p1.add(label);
		p1.add(energyCalNameTf);
		label = new JLabel(resources.getString("energy.fwhm.saveCal.setLb"));label.setForeground(mf.foreColor);
		p1.add(label);
		p1.add(useECb);
		buttonName = resources
				.getString("energy.fwhm.saveCal.saveEnergy.button");
		buttonToolTip = resources
				.getString("energy.fwhm.saveCal.saveEnergy.button.toolTip");
		buttonIconName = resources.getString("img.save.database");
		button = FrameUtilities.makeButton(buttonIconName,
				SAVEENERGYCAL_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.saveCal.saveEnergy.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p1.add(button);
		p1.setBackground(mf.bkgColor);

		suportSpE.setPreferredSize(tableDimension);
		JScrollPane scrollPane = new JScrollPane(enmainTable);
		enmainTable.setFillsViewportHeight(true);
		suportSpE.add(scrollPane);

		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		buttonName = resources
				.getString("energy.fwhm.saveCal.setEnergy.button");
		buttonToolTip = resources
				.getString("energy.fwhm.saveCal.setEnergy.button.toolTip");
		buttonIconName = resources.getString("img.accept");
		button = FrameUtilities.makeButton(buttonIconName,
				SETENERGYCAL_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.saveCal.setEnergy.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2.add(button);
		buttonName = resources
				.getString("energy.fwhm.saveCal.deleteEnergy.button");
		buttonToolTip = resources
				.getString("energy.fwhm.saveCal.deleteEnergy.button.toolTip");
		buttonIconName = resources.getString("img.close");
		button = FrameUtilities.makeButton(buttonIconName,
				DELETEENERGYCAL_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.saveCal.deleteEnergy.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2.add(button);
		p2.add(orderP);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		p2.setBackground(mf.bkgColor);

		JPanel north = new JPanel();
		BoxLayout blnorth = new BoxLayout(north, BoxLayout.Y_AXIS);
		north.setLayout(blnorth);
		north.add(p1);
		north.add(suportSpE);
		north.add(p2);
		north.setBackground(mf.bkgColor);
		north.setBorder(FrameUtilities.getGroupBoxBorder(""));

		JPanel p11 = new JPanel();
		p11.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(
				resources.getString("energy.fwhm.saveCal.fwhmCalNameLb"));label.setForeground(mf.foreColor);
		p11.add(label);
		p11.add(fwhmCalNameTf);
		label = new JLabel(resources.getString("energy.fwhm.saveCal.setLb"));label.setForeground(mf.foreColor);
		p11.add(label);
		p11.add(useFCb);
		buttonName = resources.getString("energy.fwhm.saveCal.saveFWHM.button");
		buttonToolTip = resources
				.getString("energy.fwhm.saveCal.saveFWHM.button.toolTip");
		buttonIconName = resources.getString("img.save.database");
		button = FrameUtilities.makeButton(buttonIconName, SAVEFWHMCAL_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.saveCal.saveFWHM.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p11.add(button);
		p11.setBackground(mf.bkgColor);

		suportSpF.setPreferredSize(tableDimension);
		JScrollPane scrollPane2 = new JScrollPane(fwhmmainTable);
		fwhmmainTable.setFillsViewportHeight(true);
		suportSpF.add(scrollPane2);

		JPanel p22 = new JPanel();
		p22.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		buttonName = resources.getString("energy.fwhm.saveCal.setFWHM.button");
		buttonToolTip = resources
				.getString("energy.fwhm.saveCal.setFWHM.button.toolTip");
		buttonIconName = resources.getString("img.accept");
		button = FrameUtilities.makeButton(buttonIconName, SETFWHMCAL_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.saveCal.setFWHM.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p22.add(button);
		buttonName = resources
				.getString("energy.fwhm.saveCal.deleteFWHM.button");
		buttonToolTip = resources
				.getString("energy.fwhm.saveCal.deleteFWHM.button.toolTip");
		buttonIconName = resources.getString("img.close");
		button = FrameUtilities.makeButton(buttonIconName,
				DELETEFWHMCAL_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("energy.fwhm.saveCal.deleteFWHM.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p22.add(button);
		p22.add(orderP2);//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
		p22.setBackground(mf.bkgColor);

		JPanel south = new JPanel();
		BoxLayout blsouth = new BoxLayout(south, BoxLayout.Y_AXIS);
		south.setLayout(blsouth);
		south.add(p11);
		south.add(suportSpF);
		south.add(p22);
		south.setBackground(mf.bkgColor);
		south.setBorder(FrameUtilities.getGroupBoxBorder(""));

		JPanel mainP = new JPanel();
		BoxLayout blmainP = new BoxLayout(mainP, BoxLayout.Y_AXIS);
		mainP.setLayout(blmainP);
		mainP.add(north);
		mainP.add(south);
		mainP.setBackground(mf.bkgColor);

		JPanel content = new JPanel(new BorderLayout());
		content.add(mainP);
		setContentPane(new JScrollPane(content));
		pack();
	}

	/**
	 * Most actions are set here
	 */
	public void actionPerformed(ActionEvent arg0) {
		command = arg0.getActionCommand();
		if (command.equals(SAVEENERGYCAL_COMMAND)) {
			SAVEENERGYCAL();
		} else if (command.equals(SAVEFWHMCAL_COMMAND)) {
			SAVEFWHMCAL();
		} else if (command.equals(SETENERGYCAL_COMMAND)) {
			SETENERGYCAL();
		} else if (command.equals(SETFWHMCAL_COMMAND)) {
			SETFWHMCAL();
		} else if (command.equals(DELETEENERGYCAL_COMMAND)) {
			DELETEENERGYCAL();
		} else if (command.equals(DELETEFWHMCAL_COMMAND)) {
			DELETEFWHMCAL();
		}
	}

	/**
	 * JCombobox actions are set here
	 */
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == enorderbyCb) {
			sort();
		} else if (e.getSource() == fwhmorderbyCb) {
			sort2();
		}
	}
	
	/**
	 * Sorts data from energy calibration table
	 */
	private void sort() {
		enorderbyS = (String) enorderbyCb.getSelectedItem();
		endbagent.performSelection(enorderbyS);
	}
	
	/**
	 * Sorts data from fwhm calibration table
	 */
	private void sort2() {
		fwhmorderbyS = (String) fwhmorderbyCb.getSelectedItem();
		fwhmdbagent.performSelection(fwhmorderbyS);
	}
	
	/**
	 * Save energy calibration
	 */
	private void SAVEENERGYCAL() {
		if (!mf.canSaveB) {
			return;
		}

		// boolean changedEnergyCal=false;
		changedEnergyCal = false;
		//=====================================
		String calibInfo = energyCalNameTf.getText();
		String useS = (String) useECb.getSelectedItem();
		// if yes=>update where is YES to NO!!!	
		if (useS.equals(resources.getString("difference.yes"))) {
			changedEnergyCal = true;
				
			String[] CVALUE = new String[1];
			CVALUE[0]="USE";
			Integer[] TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			String[] VALUE = new String[1];
			VALUE[0]=resources.getString("difference.no");
			String IDLINK = "USE";
			String IDVALUE="'"+resources.getString("difference.yes")+"'";
					
			endbagent.update(endbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
		}
		//now insert
		String[] data = new String[endbagent.getUsefullColumnCount()];
		int kCol = 0;
		data[kCol] = calibInfo;
		kCol++;
		data[kCol] = useS;
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.en_a3);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.en_a2);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.en_a1);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.en_a0);
		kCol++;
				
		endbagent.insert(data);
		endbagent.performSelection(enorderbyS);
		
		if (changedEnergyCal) {
			GammaRoi.setEnergyCalibration(mf.en_a3, mf.en_a2, mf.en_a1,
					mf.en_a0);
			GammaAnalysisFrame.setEnergyCalibration(mf.en_a3, mf.en_a2,
					mf.en_a1, mf.en_a0);
			GammaEnergyFWHMCalibrationFrame.setEnergyCalibration(mf.en_a3,
					mf.en_a2, mf.en_a1, mf.en_a0);
		}
		//=====================================
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

			String calibInfo = energyCalNameTf.getText();
			String useS = (String) useECb.getSelectedItem();
			// if yes=>update where is YES to NO!!!
			if (useS.equals(resources.getString("difference.yes"))) {
				changedEnergyCal = true;

				PreparedStatement psUpdate = null;

				psUpdate = con1.prepareStatement("update "
						+ gammaEnergyCalibrationTable
						+ " set USE=? where USE=?");

				psUpdate.setString(1, resources.getString("difference.no"));
				psUpdate.setString(2, resources.getString("difference.yes"));

				psUpdate.executeUpdate();
				psUpdate.close();

			}

			// first make a selection to retrieve usefull data
			String s = "select * from " + gammaEnergyCalibrationTable;
			DBOperation.select(s, con1);

			int id = DBOperation.getRowCount() + 1;// id where we make the
													// insertion
			PreparedStatement psInsert = null;

			psInsert = con1.prepareStatement("insert into "
					+ gammaEnergyCalibrationTable + " values "
					+ "(?, ?, ?, ?, ?, ?, ?)");
			psInsert.setString(1, Convertor.intToString(id));
			psInsert.setString(2, calibInfo);
			psInsert.setString(3, useS);
			psInsert.setString(4, Convertor.doubleToString(mf.en_a3));
			psInsert.setString(5, Convertor.doubleToString(mf.en_a2));
			psInsert.setString(6, Convertor.doubleToString(mf.en_a1));
			psInsert.setString(7, Convertor.doubleToString(mf.en_a0));
			psInsert.executeUpdate();

			// ------------
			performCurrentSelection();
			// some finalisations:

			if (changedEnergyCal) {
				GammaRoi.setEnergyCalibration(mf.en_a3, mf.en_a2, mf.en_a1,
						mf.en_a0);
				GammaAnalysisFrame.setEnergyCalibration(mf.en_a3, mf.en_a2,
						mf.en_a1, mf.en_a0);
				GammaEnergyFWHMCalibrationFrame.setEnergyCalibration(mf.en_a3,
						mf.en_a2, mf.en_a1, mf.en_a0);
			}

			if (psInsert != null)
				psInsert.close();
			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/

		if (changedEnergyCal) {
			String title = resources.getString("dialog.cal.changed.title");
			String message = resources.getString("dialog.cal.changed.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Save FWHM calibration
	 */
	private void SAVEFWHMCAL() {
		if (!mf.canSaveB) {
			return;
		}

		changedFWHMCal = false;
		//=====================================
		String calibInfo = fwhmCalNameTf.getText();
		String useS = (String) useFCb.getSelectedItem();
		// if yes=>update where is YES to NO!!!	
		if (useS.equals(resources.getString("difference.yes"))) {
			changedFWHMCal = true;
					
			String[] CVALUE = new String[1];
			CVALUE[0]="USE";
			Integer[] TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			String[] VALUE = new String[1];
			VALUE[0]=resources.getString("difference.no");
			String IDLINK = "USE";
			String IDVALUE="'"+resources.getString("difference.yes")+"'";
					
			fwhmdbagent.update(fwhmdbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
		}
		//now insert
		String[] data = new String[fwhmdbagent.getUsefullColumnCount()];
		int kCol = 0;
		data[kCol] = calibInfo;
		kCol++;
		data[kCol] = useS;
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.fwhm_a3);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.fwhm_a2);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.fwhm_a1);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.fwhm_a0);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.fwhm_overallProcentualError);
		kCol++;
				
		fwhmdbagent.insert(data);
		fwhmdbagent.performSelection(fwhmorderbyS);
		
		if (changedFWHMCal) {
			GammaRoi.setFWHMCalibration(mf.fwhm_a3, mf.fwhm_a2, mf.fwhm_a1,
					mf.fwhm_a0, mf.fwhm_overallProcentualError);
			GammaAnalysisFrame.setFWHMCalibration(mf.fwhm_a3, mf.fwhm_a2,
					mf.fwhm_a1, mf.fwhm_a0, mf.fwhm_overallProcentualError);
			GammaEnergyFWHMCalibrationFrame.setFWHMCalibration(mf.fwhm_a3,
					mf.fwhm_a2, mf.fwhm_a1, mf.fwhm_a0);
		}
		//=====================================		
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

			String calibInfo = fwhmCalNameTf.getText();
			String useS = (String) useFCb.getSelectedItem();
			// if yes=>update where is YES to NO!!!
			if (useS.equals(resources.getString("difference.yes"))) {
				changedFWHMCal = true;

				PreparedStatement psUpdate = null;

				psUpdate = con1.prepareStatement("update "
						+ gammaFWHMCalibrationTable + " set USE=? where USE=?");

				psUpdate.setString(1, resources.getString("difference.no"));
				psUpdate.setString(2, resources.getString("difference.yes"));

				psUpdate.executeUpdate();
				psUpdate.close();

			}

			// first make a selection to retrieve usefull data
			String s = "select * from " + gammaFWHMCalibrationTable;
			DBOperation.select(s, con1);

			int id = DBOperation.getRowCount() + 1;// id where we make the
													// insertion
			PreparedStatement psInsert = null;

			psInsert = con1.prepareStatement("insert into "
					+ gammaFWHMCalibrationTable + " values "
					+ "(?, ?, ?, ?, ?, ?, ?, ?)");
			psInsert.setString(1, Convertor.intToString(id));
			psInsert.setString(2, calibInfo);
			psInsert.setString(3, useS);
			psInsert.setString(4, Convertor.doubleToString(mf.fwhm_a3));
			psInsert.setString(5, Convertor.doubleToString(mf.fwhm_a2));
			psInsert.setString(6, Convertor.doubleToString(mf.fwhm_a1));
			psInsert.setString(7, Convertor.doubleToString(mf.fwhm_a0));
			psInsert.setString(8,
					Convertor.doubleToString(mf.fwhm_overallProcentualError));
			psInsert.executeUpdate();

			// ------------
			performCurrentSelection();
			// some finalisations:

			if (changedFWHMCal) {
				GammaRoi.setFWHMCalibration(mf.fwhm_a3, mf.fwhm_a2, mf.fwhm_a1,
						mf.fwhm_a0, mf.fwhm_overallProcentualError);
				GammaAnalysisFrame.setFWHMCalibration(mf.fwhm_a3, mf.fwhm_a2,
						mf.fwhm_a1, mf.fwhm_a0, mf.fwhm_overallProcentualError);
				GammaEnergyFWHMCalibrationFrame.setFWHMCalibration(mf.fwhm_a3,
						mf.fwhm_a2, mf.fwhm_a1, mf.fwhm_a0);
			}

			if (psInsert != null)
				psInsert.close();
			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/

		if (changedFWHMCal) {
			String title = resources.getString("dialog.cal.changed.title");
			String message = resources.getString("dialog.cal.changed.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Set energy calibration
	 */
	private void SETENERGYCAL() {
		double en_a3 = 0.0;
		double en_a2 = 0.0;
		double en_a1 = 0.0;
		double en_a0 = 0.0;
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

			//JTable aspTable = aspE.getTab();

			int selID = 0;// NO ZERO ID
			int selRow = enmainTable.getSelectedRow();//aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) enmainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to set
			}
			// check first if is in-use!NOTICE WE CAN EXTRACT DATA FROM JTABLE..LETS EXTRACT THIS FROM DB FOR SAFETY AND FOR PRACTICE!	
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str = "select * from " + gammaEnergyCalibrationTable
					+ " where ID = " + selID;
			ResultSet res = s.executeQuery(str);
			res.next();// 1 single row!!no while needed!
			String useS = res.getString(3);// column index..start from 1 is
											// 3="Use"
			en_a3 = res.getDouble(4);
			en_a2 = res.getDouble(5);
			en_a1 = res.getDouble(6);
			en_a0 = res.getDouble(7);
			if (useS.equals(resources.getString("difference.yes"))) {
				//if (con1 != null)
					//con1.close();
				return;// already in-use!
			}
			changedEnergyCal = true;

			// first set NO where is Yes:
			String[] CVALUE = new String[1];
			CVALUE[0]="USE";
			Integer[] TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			String[] VALUE = new String[1];
			VALUE[0]=resources.getString("difference.no");
			String IDLINK = "USE";
			String IDVALUE="'"+resources.getString("difference.yes")+"'";
			
			endbagent.update(endbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
			
			/*PreparedStatement psUpdate0 = null;

			psUpdate0 = con1.prepareStatement("update "
					+ gammaEnergyCalibrationTable + " set USE=? where USE=?");

			psUpdate0.setString(1, resources.getString("difference.no"));
			psUpdate0.setString(2, resources.getString("difference.yes"));

			psUpdate0.executeUpdate();
			psUpdate0.close();*/
			// ---------------------------
			CVALUE = new String[1];
			CVALUE[0]="USE";
			TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			VALUE = new String[1];
			VALUE[0]=resources.getString("difference.yes");
			IDLINK = "ID";
			IDVALUE=Convertor.intToString(selID);
			
			endbagent.setSelectedRow(selRow);//no last row selection!!!
			endbagent.update(endbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
			endbagent.performSelection(enorderbyS);
			
			/*PreparedStatement psUpdate = null;

			psUpdate = con1.prepareStatement("update "
					+ gammaEnergyCalibrationTable + " set USE=? where ID=?");

			psUpdate.setString(1, resources.getString("difference.yes"));
			psUpdate.setString(2, Convertor.intToString(selID));

			psUpdate.executeUpdate();
			psUpdate.close();*/

			//performCurrentSelection();

			if (res != null)
				res.close();
			if (s != null)
				s.close();
			//if (psUpdate != null)
				//psUpdate.close();
			//if (con1 != null)
				//con1.close();
			// local var...does not need to set them to null
			// they are automatically subject to garbage collector
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (changedEnergyCal) {

			GammaRoi.setEnergyCalibration(en_a3, en_a2, en_a1, en_a0);
			GammaAnalysisFrame.setEnergyCalibration(en_a3, en_a2, en_a1, en_a0);
			GammaEnergyFWHMCalibrationFrame.setEnergyCalibration(en_a3, en_a2,
					en_a1, en_a0);

			String title = resources.getString("dialog.cal.changed.title");
			String message = resources.getString("dialog.cal.changed.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Set FWHM calibration
	 */
	private void SETFWHMCAL() {

		double fwhm_a3 = 0.0;
		double fwhm_a2 = 0.0;
		double fwhm_a1 = 0.0;
		double fwhm_a0 = 0.0;
		double fwhm_overallProcentualError = 0.0;
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

			//JTable aspTable = aspF.getTab();

			int selID = 0;// NO ZERO ID
			int selRow = fwhmmainTable.getSelectedRow();//aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) fwhmmainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to set
			}
			// check first if is in-use!NOTICE WE CAN EXTRACT DATA FROM JTABLE..LETS EXTRACT THIS FROM DB FOR SAFETY AND FOR PRACTICE!
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str = "select * from " + gammaFWHMCalibrationTable
					+ " where ID = " + selID;
			ResultSet res = s.executeQuery(str);
			res.next();// 1 single row!!no while needed!
			String useS = res.getString(3);// column index..start from 1 is
											// 3="Use"
			fwhm_a3 = res.getDouble(4);
			fwhm_a2 = res.getDouble(5);
			fwhm_a1 = res.getDouble(6);
			fwhm_a0 = res.getDouble(7);
			fwhm_overallProcentualError = res.getDouble(8);
			if (useS.equals(resources.getString("difference.yes"))) {
				//if (con1 != null)
					//con1.close();
				return;// already in-use!
			}
			changedFWHMCal = true;
			// first set NO where is Yes:
			String[] CVALUE = new String[1];
			CVALUE[0]="USE";
			Integer[] TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			String[] VALUE = new String[1];
			VALUE[0]=resources.getString("difference.no");
			String IDLINK = "USE";
			String IDVALUE="'"+resources.getString("difference.yes")+"'";
			
			fwhmdbagent.update(fwhmdbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
			
			//PreparedStatement psUpdate0 = null;

			//psUpdate0 = con1.prepareStatement("update "
				//	+ gammaFWHMCalibrationTable + " set USE=? where USE=?");

			//psUpdate0.setString(1, resources.getString("difference.no"));
			//psUpdate0.setString(2, resources.getString("difference.yes"));

			//psUpdate0.executeUpdate();
			//psUpdate0.close();
			// ---------------------------
			CVALUE = new String[1];
			CVALUE[0]="USE";
			TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			VALUE = new String[1];
			VALUE[0]=resources.getString("difference.yes");
			IDLINK = "ID";
			IDVALUE=Convertor.intToString(selID);
			
			fwhmdbagent.setSelectedRow(selRow);//no last row selection!!!
			fwhmdbagent.update(fwhmdbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
			fwhmdbagent.performSelection(fwhmorderbyS);
			
			//PreparedStatement psUpdate = null;

			//psUpdate = con1.prepareStatement("update "
				//	+ gammaFWHMCalibrationTable + " set USE=? where ID=?");

			//psUpdate.setString(1, resources.getString("difference.yes"));
			//psUpdate.setString(2, Convertor.intToString(selID));

			//psUpdate.executeUpdate();
			//psUpdate.close();

			//performCurrentSelection();

			if (res != null)
				res.close();
			if (s != null)
				s.close();
			//if (psUpdate != null)
				//psUpdate.close();
			//if (con1 != null)
				//con1.close();
			// local var...does not need to set them to null
			// they are automatically subject to garbage collector
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (changedFWHMCal) {

			GammaRoi.setFWHMCalibration(fwhm_a3, fwhm_a2, fwhm_a1, fwhm_a0,
					fwhm_overallProcentualError);
			GammaAnalysisFrame.setFWHMCalibration(fwhm_a3, fwhm_a2, fwhm_a1,
					fwhm_a0, fwhm_overallProcentualError);
			GammaEnergyFWHMCalibrationFrame.setFWHMCalibration(fwhm_a3,
					fwhm_a2, fwhm_a1, fwhm_a0);

			String title = resources.getString("dialog.cal.changed.title");
			String message = resources.getString("dialog.cal.changed.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Delete energy calibration
	 */
	private void DELETEENERGYCAL() {
		boolean changedEnergyCal = false;
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

			//JTable aspTable = aspE.getTab();

			int selID = 0;// NO ZERO ID
			int selRow = enmainTable.getSelectedRow();//aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) enmainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to delete
			}
			// check first if is in-use!WE can check from JTable but for safety lets check from DB instead!!!	
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str = "select * from " + gammaEnergyCalibrationTable
					+ " where ID = " + selID;
			ResultSet res = s.executeQuery(str);
			res.next();// 1 single row!!no while needed!
			String useS = res.getString(3);// column index..start from 1 is
											// 3="Use"
			if (useS.equals(resources.getString("difference.yes"))) {
				changedEnergyCal = true;
			}
			//now the delete
			endbagent.delete(Convertor.intToString(selID));
			
			/*s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM " + gammaEnergyCalibrationTable);
			PreparedStatement psUpdate = null;
			while (res.next()) {
				int id = res.getInt("ID");
				if (id == selID) {
					res.deleteRow();
				} else if (id > selID) {
					// since in this table ID is UNIQUE we can make
					// on-the fly update
					psUpdate = con1.prepareStatement("update "
							+ gammaEnergyCalibrationTable
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, id - 1);
					psUpdate.setInt(2, id);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			}*/
			endbagent.performSelection(enorderbyS);
			//performCurrentSelection();

			if (res != null)
				res.close();
			if (s != null)
				s.close();
			//if (psUpdate != null)
				//psUpdate.close();
			//if (con1 != null)
				//con1.close();
			// local var...does not need to set them to null
			// they are automatically subject to garbage collector
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (changedEnergyCal) {
			String title = resources.getString("dialog.cal.deleted.title");
			String message = resources.getString("dialog.cal.deleted.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Delete FWHM clibration
	 */
	private void DELETEFWHMCAL() {
		boolean changedFWHMCal = false;
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

			//JTable aspTable = aspF.getTab();

			int selID = 0;// NO ZERO ID
			int selRow = fwhmmainTable.getSelectedRow();//aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) fwhmmainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to delete
			}
			// check first if is in-use!WE can check from JTable but for safety lets check from DB instead!!!
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str = "select * from " + gammaFWHMCalibrationTable
					+ " where ID = " + selID;
			ResultSet res = s.executeQuery(str);
			res.next();// 1 single row!!no while needed!
			String useS = res.getString(3);// column index..start from 1 is
											// 3="Use"
			if (useS.equals(resources.getString("difference.yes"))) {
				changedFWHMCal = true;
			}
			//now the delete
			fwhmdbagent.delete(Convertor.intToString(selID));
			
			/*s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM " + gammaFWHMCalibrationTable);
			PreparedStatement psUpdate = null;
			while (res.next()) {
				int id = res.getInt("ID");
				if (id == selID) {
					res.deleteRow();
				} else if (id > selID) {
					// since in this table ID is UNIQUE we can make
					// on-the fly update
					psUpdate = con1.prepareStatement("update "
							+ gammaFWHMCalibrationTable
							+ " set ID=? where ID=?");

					psUpdate.setInt(1, id - 1);
					psUpdate.setInt(2, id);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			}*/
			fwhmdbagent.performSelection(fwhmorderbyS);
			//performCurrentSelection();

			if (res != null)
				res.close();
			if (s != null)
				s.close();
			//if (psUpdate != null)
				//psUpdate.close();
			//if (con1 != null)
				//con1.close();
			// local var...does not need to set them to null
			// they are automatically subject to garbage collector
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (changedFWHMCal) {
			String title = resources.getString("dialog.cal.deleted.title");
			String message = resources.getString("dialog.cal.deleted.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
