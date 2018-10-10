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
import java.sql.PreparedStatement;
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
import javax.swing.JToolBar;
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
 * Class for saving or set the efficiency calibration. <br>
 * 
 * @author Dan Fulea, 09 Jul. 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaEfficiencyCalibrationSaveFrame extends JFrame implements
ActionListener, ItemListener {

	private String command;
	private static final String SAVE_COMMAND = "SAVE";
	private static final String SET_COMMAND = "SET";
	private static final String DELETE_COMMAND = "DELETE";
	private GammaEfficiencyCalibrationFrame mf;
	
	private final Dimension PREFERRED_SIZE = new Dimension(1000, 400);//it was 950
	private final Dimension tableDimension = new Dimension(500, 200);
	private final Dimension sizeCb = new Dimension(60, 21);

	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;

	//private AdvancedSelectPanel aspE = null;
	private JPanel suportSpE = new JPanel(new BorderLayout());
	@SuppressWarnings("rawtypes")
	private JComboBox useECb;
	private JTextField effCalNameTf = new JTextField(28);
	private String gammaDB = "";
	private String gammaEfficiencyCalibrationTable = "";
	private String gammaEfficiencyGlobalCalibrationTable = "";
	public boolean changedEnergyCal=false;//in fact must be changedEffCal...but we know...!
	protected JLabel statusL = new JLabel("Waiting..");
	
	/**
	 * The connection
	 */
	private Connection gammadbcon = null;
	/**
	 * Main table primary key column name
	 */
	private String effmainTablePrimaryKey = "ID";
	/**
	 * The JTable component associated to main table
	 */
	private JTable effmainTable;
	/**
	 * The column used for sorting data in main table (ORDER BY SQL syntax)
	 */
	private String efforderbyS = "ID";
	/**
	 * The database agent associated to main table
	 */
	private DatabaseAgentSupport effdbagent;
	
	private JComboBox<String> efforderbyCb;
	private final Dimension sizeOrderCb = new Dimension(200, 21);
	
	/**
	 * Constructor.
	 * @param mf the GammaEfficiencyCalibrationFrame object
	 */
	public GammaEfficiencyCalibrationSaveFrame(GammaEfficiencyCalibrationFrame mf){
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("Efficiency.SAVE.NAME"));
		this.mf=mf;
		changedEnergyCal=false;
		gammaDB = resources.getString("main.db");
		gammaEfficiencyCalibrationTable = resources.getString("main.db.effCalib");
		gammaEfficiencyGlobalCalibrationTable =
			resources.getString("main.db.gammaGlobalEfficiencyTable");
		//======================================================================
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = gammaDB;
		opens = opens + file_sep + dbName;
		gammadbcon = DatabaseAgent.getConnection(opens, "", "");		
		effmainTablePrimaryKey = "ID";		
		effdbagent = new DatabaseAgentSupport(gammadbcon, 
				effmainTablePrimaryKey, gammaEfficiencyCalibrationTable);
		effdbagent.setHasValidAIColumn(false);
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
		mf.changeCalibration= changedEnergyCal;
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
		performQueryDb();
		validate();
	}*/
	
	/**
	 * Initialize database.
	 */
	private void performQueryDb() {
		effdbagent.init();
		efforderbyS = effmainTablePrimaryKey;
		
		effmainTable = effdbagent.getMainTable();
		
		ListSelectionModel rowSM = effmainTable.getSelectionModel();
		rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//rowSM.addListSelectionListener(this);// listener!
		if (effmainTable.getRowCount() > 0){
			//select last row!
			effmainTable.setRowSelectionInterval(effmainTable.getRowCount() - 1,
					effmainTable.getRowCount() - 1); // last ID
		}
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			String s = "select * from " + gammaEfficiencyCalibrationTable;
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
		
		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";
		
		//-------------------------
		efforderbyCb = effdbagent.getOrderByComboBox();
		efforderbyCb.setMaximumRowCount(5);
		efforderbyCb.setPreferredSize(sizeOrderCb);
		efforderbyCb.addItemListener(this);
		JPanel orderP = new JPanel();
		orderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(mf.foreColor);
		orderP.add(label);
		orderP.add(efforderbyCb);
		orderP.setBackground(mf.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(mf.foreColor);
		orderP.add(label);
		orderP.add(effdbagent.getRecordsLabel());// recordsCount);
		//-----------------------------------------				
		
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		label = new JLabel(
				resources.getString("eff.saveCal.effCalNameLb"));label.setForeground(mf.foreColor);
		p1.add(label);
		p1.add(effCalNameTf);
		label = new JLabel(resources.getString("eff.saveCal.setLb"));label.setForeground(mf.foreColor);
		p1.add(label);
		p1.add(useECb);
		buttonName = resources
				.getString("eff.saveCal.save.button");
		buttonToolTip = resources
				.getString("eff.saveCal.save.button.toolTip");
		buttonIconName = resources.getString("img.save.database");
		button = FrameUtilities.makeButton(buttonIconName,
				SAVE_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.saveCal.save.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p1.add(button);
		p1.setBackground(mf.bkgColor);
		
		suportSpE.setPreferredSize(tableDimension);
		JScrollPane scrollPane = new JScrollPane(effmainTable);
		effmainTable.setFillsViewportHeight(true);
		suportSpE.add(scrollPane);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		buttonName = resources
				.getString("eff.saveCal.set.button");
		buttonToolTip = resources
				.getString("eff.saveCal.set.button.toolTip");
		buttonIconName = resources.getString("img.accept");
		button = FrameUtilities.makeButton(buttonIconName,
				SET_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.saveCal.set.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2.add(button);
		buttonName = resources
				.getString("eff.saveCal.delete.button");
		buttonToolTip = resources
				.getString("eff.saveCal.delete.button.toolTip");
		buttonIconName = resources.getString("img.close");
		button = FrameUtilities.makeButton(buttonIconName,
				DELETE_COMMAND, buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.saveCal.delete.button.mnemonic");
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
		north.setBorder(FrameUtilities.getGroupBoxBorder("",mf.foreColor));
		
		JPanel mainP = new JPanel();
		BoxLayout blmainP = new BoxLayout(mainP, BoxLayout.Y_AXIS);
		mainP.setLayout(blmainP);
		mainP.add(north);		
		mainP.setBackground(mf.bkgColor);
		
		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		initStatusBar(statusBar);		
		
		JPanel content = new JPanel(new BorderLayout());
		content.add(mainP);
		content.add(statusBar, BorderLayout.PAGE_END);
		setContentPane(new JScrollPane(content));
		pack();
	}
	/**
	 * Most actions are set here
	 */
	public void actionPerformed(ActionEvent arg0) {
		command = arg0.getActionCommand();
		if (command.equals(SAVE_COMMAND)) {
			SAVE();
		} else if (command.equals(SET_COMMAND)) {
			SET();
		} else if (command.equals(DELETE_COMMAND)) {
			DELETE();
		}
	}
	
	/**
	 * JCombobox actions are set here
	 */
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == efforderbyCb) {
			sort();
		}
	}
	
	/**
	 * Sorts data from main table
	 */
	private void sort() {
		efforderbyS = (String) efforderbyCb.getSelectedItem();
		effdbagent.performSelection(efforderbyS);
	}
	
	/**
	 * Save efficiency calibration
	 */
	private void SAVE(){
		if (mf.canSaveGlobalB){//this is similar to dead-time table in alphabeta. One entry per nuclide!!!
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
				
				String calibInfo = mf.globalNuclide;
				String s = "select * from " + gammaEfficiencyGlobalCalibrationTable+
				" WHERE NUCLIDE = "+"'"+calibInfo+"'";
				DatabaseAgent.select(gammadbcon, s);//DBOperation.select(s, con1);
				int id = DatabaseAgent.getRowCount();// DBOperation.getRowCount();
				if(id>0){//in fact is 1{
					String ids =DatabaseAgent.getValueAt(0, 0).toString();//DBOperation.getValueAt(0, 0).toString();
					PreparedStatement psUpdate = null;
					
					psUpdate = gammadbcon.prepareStatement("update "//con1.prepareStatement("update "
							+ gammaEfficiencyGlobalCalibrationTable
							+ " set EFFICIENCY=?, ERROR=? where ID=?");

					psUpdate.setString(1, Convertor.doubleToString(mf.efficiencyGlobal));
					psUpdate.setString(2, Convertor.doubleToString(mf.efficiencyGlobalError));
					psUpdate.setString(3, ids);

					psUpdate.executeUpdate();
					psUpdate.close();
				} else {
					//insert
					s = "select * from " + gammaEfficiencyGlobalCalibrationTable;
					DatabaseAgent.select(gammadbcon, s);//DBOperation.select(s, con1);

					id = DatabaseAgent.getRowCount()+ 1;//DBOperation.getRowCount() + 1;// id where we make the
															// insertion
					PreparedStatement psInsert = null;

					psInsert = gammadbcon.prepareStatement("insert into "//con1.prepareStatement("insert into "
							+ gammaEfficiencyGlobalCalibrationTable + " values "
							+ "(?, ?, ?, ?)");
					psInsert.setString(1, Convertor.intToString(id));
					psInsert.setString(2, calibInfo);
					psInsert.setString(3, Convertor.doubleToString(mf.efficiencyGlobal));
					psInsert.setString(4, Convertor.doubleToString(mf.efficiencyGlobalError));				

					psInsert.executeUpdate();
					
					if (psInsert != null)
						psInsert.close();
					//if (con1 != null)
						//con1.close();	
				}
					
				statusL.setText(resources.getString("eff.saveCal.global.saved"));
								
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (!mf.canSaveB) {
			return;
		}
		
		changedEnergyCal=false;
		
		//=====================================
		String calibInfo = effCalNameTf.getText();
		String useS = (String) useECb.getSelectedItem();
		// if yes=>update where is YES to NO!!!	
		if (useS.equals(resources.getString("difference.yes"))) {
			changedEnergyCal=true;
			
			String[] CVALUE = new String[1];
			CVALUE[0]="USE";
			Integer[] TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			String[] VALUE = new String[1];
			VALUE[0]=resources.getString("difference.no");
			String IDLINK = "USE";
			String IDVALUE="'"+resources.getString("difference.yes")+"'";
			
			effdbagent.update(effdbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
		}
		//now insert
		String[] data = new String[effdbagent.getUsefullColumnCount()];
		int kCol = 0;
		data[kCol] = calibInfo;
		kCol++;
		data[kCol] = useS;
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p1_a4);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p1_a3);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p1_a2);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p1_a1);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p1_a0);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p2_a4);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p2_a3);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p2_a2);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p2_a1);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_p2_a0);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_crossoverEnergy);
		kCol++;
		data[kCol] = Convertor.doubleToString(mf.eff_overallProcentualError);
		kCol++;
		
		effdbagent.insert(data);
		effdbagent.performSelection(efforderbyS);
		//=====================================
		
		if(changedEnergyCal){
			GammaRoi.setEfficiencyCalibration(mf.eff_p1_a4,mf.eff_p1_a3,mf.eff_p1_a2,
					mf.eff_p1_a1, mf.eff_p1_a0,mf.eff_p2_a4,mf.eff_p2_a3,mf.eff_p2_a2,
					mf.eff_p2_a1, mf.eff_p2_a0,mf.eff_crossoverEnergy,mf.eff_overallProcentualError);
			GammaAnalysisFrame.setEfficiencyCalibration(mf.eff_p1_a4,mf.eff_p1_a3,mf.eff_p1_a2,
					mf.eff_p1_a1, mf.eff_p1_a0,mf.eff_p2_a4,mf.eff_p2_a3,mf.eff_p2_a2,
					mf.eff_p2_a1, mf.eff_p2_a0,mf.eff_crossoverEnergy,mf.eff_overallProcentualError);
			GammaEfficiencyCalibrationFrame.setCalibrations(mf.eff_p1_a4,mf.eff_p1_a3,mf.eff_p1_a2,
					mf.eff_p1_a1, mf.eff_p1_a0,mf.eff_p2_a4,mf.eff_p2_a3,mf.eff_p2_a2,
					mf.eff_p2_a1, mf.eff_p2_a0,mf.eff_crossoverEnergy);
		}
		//also
		if(changedEnergyCal){
			String title = resources.getString("dialog.cal.changed.title");
			String message = resources.getString("dialog.cal.changed.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
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
			
			String calibInfo = effCalNameTf.getText();
			String useS = (String) useECb.getSelectedItem();
			// if yes=>update where is YES to NO!!!			
			if (useS.equals(resources.getString("difference.yes"))) {
				changedEnergyCal=true;

				PreparedStatement psUpdate = null;

				psUpdate = con1.prepareStatement("update "
						+ gammaEfficiencyCalibrationTable
						+ " set USE=? where USE=?");

				psUpdate.setString(1, resources.getString("difference.no"));
				psUpdate.setString(2, resources.getString("difference.yes"));

				psUpdate.executeUpdate();
				psUpdate.close();

			}
			
			// first make a selection to retrieve usefull data
			String s = "select * from " + gammaEfficiencyCalibrationTable;
			DBOperation.select(s, con1);

			int id = DBOperation.getRowCount() + 1;// id where we make the
													// insertion
			PreparedStatement psInsert = null;

			psInsert = con1.prepareStatement("insert into "
					+ gammaEfficiencyCalibrationTable + " values "
					+ "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			psInsert.setString(1, Convertor.intToString(id));
			psInsert.setString(2, calibInfo);
			psInsert.setString(3, useS);
			psInsert.setString(4, Convertor.doubleToString(mf.eff_p1_a4));
			psInsert.setString(5, Convertor.doubleToString(mf.eff_p1_a3));
			psInsert.setString(6, Convertor.doubleToString(mf.eff_p1_a2));
			psInsert.setString(7, Convertor.doubleToString(mf.eff_p1_a1));
			psInsert.setString(8, Convertor.doubleToString(mf.eff_p1_a0));
			psInsert.setString(9, Convertor.doubleToString(mf.eff_p2_a4));
			psInsert.setString(10, Convertor.doubleToString(mf.eff_p2_a3));
			psInsert.setString(11, Convertor.doubleToString(mf.eff_p2_a2));
			psInsert.setString(12, Convertor.doubleToString(mf.eff_p2_a1));
			psInsert.setString(13, Convertor.doubleToString(mf.eff_p2_a0));
			psInsert.setString(14, Convertor.doubleToString(mf.eff_crossoverEnergy));
			psInsert.setString(15, Convertor.doubleToString(mf.eff_overallProcentualError));

			psInsert.executeUpdate();

			// ------------
			performCurrentSelection();
			// some finalisations:
			
			if(changedEnergyCal){
				GammaRoi.setEfficiencyCalibration(mf.eff_p1_a4,mf.eff_p1_a3,mf.eff_p1_a2,
						mf.eff_p1_a1, mf.eff_p1_a0,mf.eff_p2_a4,mf.eff_p2_a3,mf.eff_p2_a2,
						mf.eff_p2_a1, mf.eff_p2_a0,mf.eff_crossoverEnergy,mf.eff_overallProcentualError);
				GammaAnalysisFrame.setEfficiencyCalibration(mf.eff_p1_a4,mf.eff_p1_a3,mf.eff_p1_a2,
						mf.eff_p1_a1, mf.eff_p1_a0,mf.eff_p2_a4,mf.eff_p2_a3,mf.eff_p2_a2,
						mf.eff_p2_a1, mf.eff_p2_a0,mf.eff_crossoverEnergy,mf.eff_overallProcentualError);
				GammaEfficiencyCalibrationFrame.setCalibrations(mf.eff_p1_a4,mf.eff_p1_a3,mf.eff_p1_a2,
						mf.eff_p1_a1, mf.eff_p1_a0,mf.eff_p2_a4,mf.eff_p2_a3,mf.eff_p2_a2,
						mf.eff_p2_a1, mf.eff_p2_a0,mf.eff_crossoverEnergy);
			}
			
			if (psInsert != null)
				psInsert.close();
			if (con1 != null)
				con1.close();			

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(changedEnergyCal){
			String title = resources.getString("dialog.cal.changed.title");
			String message = resources.getString("dialog.cal.changed.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}*/
	}
	
	/**
	 * Set efficiency calibration
	 */
	private void SET(){

		double eff_p1_a4=0.0;
		double eff_p1_a3=0.0;
		double eff_p1_a2=0.0;
		double eff_p1_a1=0.0;
		double eff_p1_a0=0.0;
		double eff_p2_a4=0.0;
		double eff_p2_a3=0.0;
		double eff_p2_a2=0.0;
		double eff_p2_a1=0.0;
		double eff_p2_a0=0.0;
		double eff_crosoverEnergy=0.0;
		double eff_overallProcentualError=0.0;

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
			int selRow = effmainTable.getSelectedRow();//aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) effmainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to set
			}
			//check first if is in-use!	NOTICE WE CAN EXTRACT DATA FROM JTABLE..LETS EXTRACT THIS FROM DB FOR SAFETY AND FOR PRACTICE!	
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str="select * from " + gammaEfficiencyCalibrationTable+ " where ID = " + selID;
			ResultSet res=s.executeQuery(str);
			res.next();//1 single row!!no while needed!
			String useS=res.getString(3);//column index..start from 1 is 3="Use"	
			eff_p1_a4=res.getDouble(4);
			eff_p1_a3=res.getDouble(5);
			eff_p1_a2=res.getDouble(6);
			eff_p1_a1=res.getDouble(7);
			eff_p1_a0=res.getDouble(8);
			eff_p2_a4=res.getDouble(9);
			eff_p2_a3=res.getDouble(10);
			eff_p2_a2=res.getDouble(11);
			eff_p2_a1=res.getDouble(12);
			eff_p2_a0=res.getDouble(13);
			eff_crosoverEnergy=res.getDouble(14);
			eff_overallProcentualError=res.getDouble(15);

			if(useS.equals(resources.getString("difference.yes"))){				
				//if (con1 != null)
					//con1.close();
				return;//already in-use!
			}
			changedEnergyCal=true;
			//first set NO where is Yes:
			String[] CVALUE = new String[1];
			CVALUE[0]="USE";
			Integer[] TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			String[] VALUE = new String[1];
			VALUE[0]=resources.getString("difference.no");
			String IDLINK = "USE";
			//if strings only the below work: "'"!!!!!!!!!!!!!!!!!!!!
			String IDVALUE="'"+resources.getString("difference.yes")+"'";
			
			effdbagent.update(effdbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
			
			//PreparedStatement psUpdate0 = null;

			//psUpdate0 = con1.prepareStatement("update "
				//	+ gammaEfficiencyCalibrationTable
				//	+ " set USE=? where USE=?");

			//psUpdate0.setString(1, resources.getString("difference.no"));
			//psUpdate0.setString(2, resources.getString("difference.yes"));

			//psUpdate0.executeUpdate();
			//psUpdate0.close();
			//---------------------------
			CVALUE = new String[1];
			CVALUE[0]="USE";
			TVALUE = new Integer[1];
			TVALUE[0]=Types.VARCHAR;
			VALUE = new String[1];
			VALUE[0]=resources.getString("difference.yes");
			IDLINK = "ID";
			IDVALUE=Convertor.intToString(selID);
			
			effdbagent.setSelectedRow(selRow);//no last row selection!!!
			effdbagent.update(effdbagent.getDatabaseTableName(), CVALUE, TVALUE, VALUE, IDLINK, IDVALUE);
			effdbagent.performSelection(efforderbyS);
			//PreparedStatement psUpdate = null;

			//psUpdate = con1.prepareStatement("update "
				//	+ gammaEfficiencyCalibrationTable
					//+ " set USE=? where ID=?");

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
		
		if(changedEnergyCal){
			
			GammaRoi.setEfficiencyCalibration(eff_p1_a4,eff_p1_a3,eff_p1_a2,
					eff_p1_a1, eff_p1_a0,eff_p2_a4,eff_p2_a3,eff_p2_a2,
					eff_p2_a1, eff_p2_a0,eff_crosoverEnergy,eff_overallProcentualError);
			GammaAnalysisFrame.setEfficiencyCalibration(eff_p1_a4,eff_p1_a3,eff_p1_a2,
					eff_p1_a1, eff_p1_a0,eff_p2_a4,eff_p2_a3,eff_p2_a2,
					eff_p2_a1, eff_p2_a0,eff_crosoverEnergy,eff_overallProcentualError);			
			GammaEfficiencyCalibrationFrame.setCalibrations(eff_p1_a4,eff_p1_a3,eff_p1_a2,
					eff_p1_a1, eff_p1_a0,eff_p2_a4,eff_p2_a3,eff_p2_a2,
					eff_p2_a1, eff_p2_a0,eff_crosoverEnergy);
			String title = resources.getString("dialog.cal.changed.title");
			String message = resources.getString("dialog.cal.changed.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	/**
	 * Delete efficiency calibration
	 */
	private void DELETE(){
		boolean changedEnergyCal=false;
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
			int selRow = effmainTable.getSelectedRow();//aspTable.getSelectedRow();
			if (selRow != -1) {
				selID = (Integer) effmainTable.getValueAt(selRow, 0);//aspTable.getValueAt(selRow, 0);
			} else {
				//if (con1 != null)
					//con1.close();
				return;// nothing to delete
			}
			//check first if is in-use!	WE can check from JTable but for safety lets check from DB instead!!!	
			Statement s = gammadbcon.createStatement();//con1.createStatement();
			String str="select * from " + gammaEfficiencyCalibrationTable+ " where ID = " + selID;
			ResultSet res=s.executeQuery(str);
			res.next();//1 single row!!no while needed!
			String useS=res.getString(3);//column index..start from 1 is 3="Use"		
			if(useS.equals(resources.getString("difference.yes"))){				
				changedEnergyCal=true;				
			}
			
			//now the delete
			effdbagent.delete(Convertor.intToString(selID));
			/*
			s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			res = s.executeQuery("SELECT * FROM "
					+ gammaEfficiencyCalibrationTable);
			PreparedStatement psUpdate = null;
			while (res.next()) {
				int id = res.getInt("ID");
				if (id == selID) {
					res.deleteRow();
				} else if (id > selID) {
					// since in this table ID is UNIQUE we can make
					// on-the fly update
					psUpdate = con1.prepareStatement("update "
							+ gammaEfficiencyCalibrationTable + " set ID=? where ID=?");

					psUpdate.setInt(1, id - 1);
					psUpdate.setInt(2, id);

					psUpdate.executeUpdate();
					psUpdate.close();
				}
			}*/
			effdbagent.performSelection(efforderbyS);
			//performCurrentSelection();
			// do not shutdown derby..it will be closed at frame exit!

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
		
		if(changedEnergyCal){
			String title = resources.getString("dialog.cal.deleted.title");
			String message = resources.getString("dialog.cal.deleted.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

}
