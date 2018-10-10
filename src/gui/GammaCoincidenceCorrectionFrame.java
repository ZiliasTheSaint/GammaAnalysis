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
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import danfulea.db.DatabaseAgent;
import danfulea.db.DatabaseAgentSupport;
//import jdf.db.AdvancedSelectPanel;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.math.Convertor;
import danfulea.math.Sort;
import danfulea.utils.FrameUtilities;

/**
 * Frame for handling gamma coincidence correction!. <br>
 * 
 * @author Dan Fulea, 30 May 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaCoincidenceCorrectionFrame extends JFrame implements
		ActionListener, ItemListener, Runnable {
	private GammaLibraryFrame mf;
	private final Dimension PREFERRED_SIZE = new Dimension(800, 720);
	private final Dimension smalltableDimension = new Dimension(500, 200);
	private final Dimension textAreaDimension = new Dimension(500, 200);
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;
	protected Color bkgColor;
	//private AdvancedSelectPanel asp = null;
	private JPanel suportSp = new JPanel(new BorderLayout());

	private String[] nuclides;
	private double[] BR;
	private double[] xenergy;
	private double[] xyield;
	private JTextField loadEfficienciesTf = new JTextField(50);
	private JTextArea textArea;
	private JCheckBox useXRayCoin;

	private JLabel statusL = new JLabel();
	private volatile Thread computationTh = null;// computation thread!
	private volatile Thread statusTh = null;// status display thread!
	private int delay = 100;
	private int frameNumber = -1;
	private String statusRunS = "";
	private String command = "";
	private static final String LOAD_COMMAND = "LOAD";
	private static final String COMPUTE_COMMAND = "COMPUTE";
	private static final String SAVE_COMMAND = "SAVE";
	private static final String DELETE_COMMAND = "DELETE";

	private Vector<Double> eff_energyV = null;
	private Vector<Object> effData = null;
	private static StringBuffer stringBuffer = new StringBuffer();
	private String effFile = "";
	private double[] m_peak;
	private double[] n_peak;
	private double[] m_total;
	private double[] n_total;
	private double sumXrayYieldEffTotal = 0.0;
	private Vector<Double> e_readV, y_readV, ecLevelFeedV;
	private Vector<Integer> parentLevelV, daughterLevelV;
	private double[] pout, pin;
	private Vector<Double> energyV, pOutV, pInV, yieldsRealV;
	private double XRAYfactor = 1.0;// 1 if taken into account or 0 if neglect!
	private Vector<Object> corrData;
	protected static int IDLink = 0;
	
	/**
	 * The connection
	 */
	private Connection gammadbcon = null;
	/**
	 * Nested table primary key column name. It is nested table so NRCRT+ID makes a valid priKey NOT A SINGLE COLUMN
	 */
	private String nestedTablePrimaryKey = "NRCRT";
	/**
	 * Shared column name for main table and nested table
	 */
	private String IDlinkS = "ID";
	/**
	 * The JTable component associated to nested table
	 */
	private JTable nestedTable;
	
	/**
	 * The column used for sorting data in nested table (ORDER BY SQL syntax)
	 */
	private String nestedorderbyS = "NRCRT";
	/**
	 * The database agent associated to nested table
	 */
	private DatabaseAgentSupport nesteddbagent;
	
	private JComboBox<String> nestedorderbyCb;
	private final Dimension sizeOrderCb = new Dimension(200, 21);

	/**
	 * Constructor.
	 * @param nuclides array of nuclides in chain
	 * @param BR array of branching ratios
	 * @param xenergy array of energies
	 * @param xyield array of energy yields
	 * @param mf the calling class
	 */
	public GammaCoincidenceCorrectionFrame(String[] nuclides, double[] BR,
			double[] xenergy, double[] xyield, GammaLibraryFrame mf) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("CoinFrame.NAME"));

		this.nuclides = nuclides;
		this.BR = BR;
		this.xenergy = xenergy;
		this.xyield = xyield;
		this.mf = mf;
		this.bkgColor = mf.bkgColor;
		//=====================
		nestedTablePrimaryKey = "NRCRT";
		nestedorderbyS = nestedTablePrimaryKey;
		IDlinkS = "ID";
		DatabaseAgent.ID_CONNECTION = DatabaseAgent.DERBY_CONNECTION;
		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String opens = currentDir + file_sep + datas;
		String dbName = mf.gammaDB;
		opens = opens + file_sep + dbName;
		gammadbcon = DatabaseAgent.getConnection(opens, "", "");
		nesteddbagent = new DatabaseAgentSupport(gammadbcon, 
				nestedTablePrimaryKey,
				mf.gammaNuclidesCoincidenceTable);
		nesteddbagent.setHasValidAIColumn(false);
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
	 * Reset global variables for re-use.
	 */
	private void cleanUp() {
		nuclides = null;
		BR = null;
		xenergy = null;
		xyield = null;
		eff_energyV = null;
		effData = null;
		m_peak = null;
		n_peak = null;
		m_total = null;
		n_total = null;

		e_readV = null;
		y_readV = null;
		ecLevelFeedV = null;
		parentLevelV = null;
		daughterLevelV = null;
		pout = null;
		pin = null;
		energyV = null;
		yieldsRealV = null;
		pOutV = null;
		pInV = null;

		corrData = null;
	}

	/**
	 * Exit method
	 */
	private void attemptExit() {
		cleanUp();
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
		Character mnemonic = null;
		JButton button = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";
		
		//-------------------------
		nestedorderbyCb = nesteddbagent.getOrderByComboBox();
		nestedorderbyCb.setMaximumRowCount(5);
		nestedorderbyCb.setPreferredSize(sizeOrderCb);
		nestedorderbyCb.addItemListener(this);
		JPanel orderP = new JPanel();
		orderP.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		JLabel label = new JLabel(resources.getString("sort.by"));//"Sort by: ");
		label.setForeground(mf.foreColor);
		orderP.add(label);
		orderP.add(nestedorderbyCb);
		orderP.setBackground(mf.bkgColor);
		label = new JLabel(resources.getString("records.count"));//"Records count: ");
		label.setForeground(mf.foreColor);
		orderP.add(label);
		orderP.add(nesteddbagent.getRecordsLabel());// recordsCount);
		//-----------------------------------------				

		useXRayCoin = new JCheckBox(resources.getString("coin.useXray"));
		useXRayCoin.setBackground(bkgColor);
		useXRayCoin.setForeground(mf.foreColor);
		useXRayCoin.setSelected(true);
		// --------------------------------------------------
		textArea = new JTextArea();
		textArea.setCaretPosition(0);
		textArea.setEditable(false);
		textArea.setText("");
		textArea.setWrapStyleWord(true);
		textArea.setBackground(mf.textAreaBkgColor);
		textArea.setForeground(mf.textAreaForeColor);

		JPanel resultP = new JPanel(new BorderLayout());
		resultP.setPreferredSize(textAreaDimension);
		resultP.add(new JScrollPane(textArea), BorderLayout.CENTER);
		resultP.setBackground(bkgColor);
		// -------------
		buttonName = resources.getString("coin.frame.load.button");
		buttonToolTip = resources.getString("coin.frame.load.button.toolTip");
		buttonIconName = resources.getString("img.open.file");
		button = FrameUtilities.makeButton(buttonIconName, LOAD_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("coin.frame.load.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		JPanel p2P = new JPanel();
		p2P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p2P.add(loadEfficienciesTf);
		loadEfficienciesTf.setEditable(false);
		p2P.add(button);
		p2P.setBackground(bkgColor);
		p2P.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("coin.frame.efficiencyLb"), mf.foreColor));

		buttonName = resources.getString("coin.frame.compute.button");
		buttonToolTip = resources
				.getString("coin.frame.compute.button.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, COMPUTE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("coin.frame.compute.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		JPanel p21P = new JPanel();
		p21P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p21P.add(useXRayCoin);
		p21P.add(button);
		p21P.setBackground(bkgColor);

		suportSp.setPreferredSize(smalltableDimension);//@@@@@@@@@@@@@@@@@@@@@@@@
		JScrollPane scrollPane = new JScrollPane(nestedTable);
		nestedTable.setFillsViewportHeight(true);
		suportSp.add(scrollPane);
		
		JPanel p3P = new JPanel();
		p3P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p3P.add(suportSp);
		
		p3P.setBackground(bkgColor);

		buttonName = resources.getString("coin.frame.save.button");
		buttonToolTip = resources.getString("coin.frame.save.button.toolTip");
		buttonIconName = resources.getString("img.save.database");
		button = FrameUtilities.makeButton(buttonIconName, SAVE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("coin.frame.save.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());

		JPanel p31P = new JPanel();
		p31P.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p31P.add(button);

		buttonName = resources.getString("coin.frame.delete.button");
		buttonToolTip = resources.getString("coin.frame.delete.button.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName, DELETE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("coin.frame.delete.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p31P.add(button);
		p31P.setBackground(bkgColor);

		JPanel box2P = new JPanel();
		BoxLayout bl2 = new BoxLayout(box2P, BoxLayout.Y_AXIS);
		box2P.setLayout(bl2);
		box2P.add(p31P);
		box2P.add(p3P);
		box2P.add(orderP);//@@@@@@@@@@@@@@@@@
		box2P.setBackground(bkgColor);

		JPanel boxP = new JPanel();
		BoxLayout bl = new BoxLayout(boxP, BoxLayout.Y_AXIS);
		boxP.setLayout(bl);
		boxP.add(p2P);
		boxP.add(p21P);
		boxP.setBackground(bkgColor);

		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		initStatusBar(statusBar);

		JPanel content = new JPanel();
		BoxLayout bl11 = new BoxLayout(content, BoxLayout.Y_AXIS);
		content.setLayout(bl11);
		content.add(boxP);
		content.add(resultP);
		content.add(box2P);
		content.add(statusBar, BorderLayout.PAGE_END);
		// -------------------------------------------------
		setContentPane(new JScrollPane(content));
		content.setOpaque(true); // content panes must be opaque
		pack();

	}

	/**
	 * Initialize status bar.
	 * 
	 * @param toolBar toolbar
	 */
	private void initStatusBar(JToolBar toolBar) {
		JPanel toolP = new JPanel();
		toolP.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));

		toolP.add(statusL);
		toolBar.add(toolP);
		statusL.setText(resources.getString("status.wait"));
	}

	/**
	 * Most actions are set here
	 */
	public void actionPerformed(ActionEvent arg0) {
		command = arg0.getActionCommand();
		if (command.equals(LOAD_COMMAND)) {
			load();
		} else if (command.equals(COMPUTE_COMMAND)) {
			compute();
		} else if (command.equals(SAVE_COMMAND)) {
			save();
		} else if (command.equals(DELETE_COMMAND)) {
			delete();
		}

	}
	
	/**
	 * JCombobox actions are set here
	 */
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		if (e.getSource() == nestedorderbyCb) {
			sort();
		} 
	}
	
	/**
	 * Sorts data from main table
	 */
	private void sort() {
		nestedorderbyS = (String) nestedorderbyCb.getSelectedItem();

		nesteddbagent.setLinks(IDlinkS, Convertor.intToString(IDLink));
		nesteddbagent.performSelection(nestedorderbyS);
	}

	/**
	 * Save coincidence correction.
	 */
	private void save() {
		if (corrData == null) {

			String title = resources.getString("dialog.coin.nodata.title");
			String message = resources.getString("dialog.coin.nodata.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (corrData.size() < 1) {
			String title = resources.getString("dialog.coin.nodata.title");
			String message = resources.getString("dialog.coin.nodata.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		//============================
		nesteddbagent.setLinks(IDlinkS, Convertor.intToString(IDLink));
		nesteddbagent.performSelection(nestedorderbyS);
		//String rowsCountS = nesteddbagent.getRecordsLabel().getText();
		int rowsCount = nesteddbagent.getRowsCount();//Convertor.stringToInt(rowsCountS);
		if (rowsCount>0){
			String title = resources
					.getString("dialog.coin.overwrite.title");
			String message = resources
					.getString("dialog.coin.overwrite.message");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		for (int i = 0; i < corrData.size(); i++) {
			String[] data = new String[nesteddbagent.getAllColumnCount()];
			
			int kCol = 0;
			data[kCol] = Convertor.intToString(i + 1);
			kCol++;
			data[kCol] = Convertor.intToString(IDLink);
			kCol++;
			data[kCol] = getValueAt(corrData, i, 0).toString();// en
			kCol++;
			data[kCol] = getValueAt(corrData, i, 1).toString();// yield
			kCol++;
			data[kCol] =getValueAt(corrData, i, 2).toString();
			kCol++;
			data[kCol] =getValueAt(corrData, i, 3).toString();
			kCol++;
			data[kCol] =getValueAt(corrData, i, 4).toString();
			kCol++;
			
			nesteddbagent.insertAll(data);
		}
		
		nesteddbagent.performSelection(nestedorderbyS);//links are already set above
		//============================
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = mf.gammaDB;
			opens = opens + file_sep + dbName;
			// make a connection
			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			// first make a selection to retrieve usefull data
			String s = "SELECT * FROM " + mf.gammaNuclidesCoincidenceTable
					+ " where ID = " + IDLink;
			DBOperation.select(s, con1);
			int rowcount = DBOperation.getRowCount();
			if (rowcount > 0) {
				String title = resources
						.getString("dialog.coin.overwrite.title");
				String message = resources
						.getString("dialog.coin.overwrite.message");
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			PreparedStatement psInsert = null;

			psInsert = con1.prepareStatement("insert into "
					+ mf.gammaNuclidesCoincidenceTable + " values "
					+ "(?, ?, ?, ?, ?,?,?)");
			for (int i = 0; i < corrData.size(); i++) {
				psInsert.setString(1, Convertor.intToString(i + 1));
				psInsert.setString(2, Convertor.intToString(IDLink));
				psInsert.setString(3, getValueAt(corrData, i, 0).toString());// en
				psInsert.setString(4, getValueAt(corrData, i, 1).toString());// yield
				psInsert.setString(5, getValueAt(corrData, i, 2).toString());
				psInsert.setString(6, getValueAt(corrData, i, 3).toString());
				psInsert.setString(7, getValueAt(corrData, i, 4).toString());

				psInsert.executeUpdate();
			}

			if (psInsert != null)
				psInsert.close();
			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/
		// ---
		//performCurrentSelection();
		statusL.setText(resources.getString("status.coin.saved"));
	}

	/**
	 * Delete coincidence correction data
	 */
	private void delete() {
		
		nesteddbagent.delete(nesteddbagent.getDatabaseTableName(), IDlinkS, Convertor.intToString(IDLink));
		
		//display null table
		nesteddbagent.setLinks(IDlinkS, Convertor.intToString(IDLink));
		nesteddbagent.performSelection(nestedorderbyS);
		
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = mf.gammaDB;
			opens = opens + file_sep + dbName;
			// make a connection
			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");

			Statement s = con1.createStatement(ResultSet.TYPE_FORWARD_ONLY,
					ResultSet.CONCUR_UPDATABLE);
			ResultSet res = s.executeQuery("SELECT * FROM "
					+ mf.gammaNuclidesCoincidenceTable);
			while (res.next()) {
				int id = res.getInt("ID");
				if ((id == IDLink)) {
					res.deleteRow();
				}
			}

			//performCurrentSelection();

			if (res != null)
				res.close();
			if (s != null)
				s.close();
			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/

		statusL.setText(resources.getString("status.done"));
	}

	/**
	 * Load detector efficiency array
	 */
	private void load() {

		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");

		String effData = resources.getString("data.eff.load");
		String opens = currentDir + file_sep + datas + file_sep + effData;

		// File select
		JFileChooser chooser = new JFileChooser(opens);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = chooser.showOpenDialog(this);// parent=this frame
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			effFile = chooser.getSelectedFile().toString();
			loadEfficienciesTf.setText(effFile);

			statusRunS = resources.getString("status.readingEfficiencyFile");
			startThread();
		}

	}

	/**
	 * Read efficiency data from file
	 */
	private void readFile() {
		// reset data
		stringBuffer = new StringBuffer();
		eff_energyV = new Vector<Double>();
		Vector<Double> peakEfficiencyV = new Vector<Double>();
		Vector<Double> totalEfficiencyV = new Vector<Double>();
		effData = new Vector<Object>();
		// -------------
		int iread = 0;
		@SuppressWarnings("unused")
		int lnr = 0;// data number
		int lnrr = 0;// line number
		char lineSep = '\n';
		boolean enB = true;// read energy
		boolean effpB = true;// read peak eff

		boolean haveData = false;

		FileInputStream in = null;
		try {
			//FileInputStream in = new FileInputStream(effFile);
			in = new FileInputStream(effFile);

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
									.getString("dialog.effFile.title");
							String message = resources
									.getString("dialog.effFile.message") + s;
							JOptionPane.showMessageDialog(null, message, title,
									JOptionPane.ERROR_MESSAGE);

							stopThread();
							statusL.setText(resources.getString("status.error"));

							peakEfficiencyV = null;
							totalEfficiencyV = null;

							return;
						}
						// /////////////////////
						if (enB) {
							eff_energyV.addElement(Convertor.stringToDouble(s));
							enB = false;// next read peakEff
						} else if (effpB) {
							peakEfficiencyV.addElement(Convertor
									.stringToDouble(s));
							effpB = false;// next read total eff
						} else {
							// last row data=total eff!
							totalEfficiencyV.addElement(Convertor
									.stringToDouble(s));
							enB = true;// next read energy
							effpB = true;

							Vector<String> rowEff = new Vector<String>();
							rowEff.addElement(eff_energyV.elementAt(lnrr)
									.toString());
							rowEff.addElement(peakEfficiencyV.elementAt(lnrr)
									.toString());
							rowEff.addElement(totalEfficiencyV.elementAt(lnrr)
									.toString());
							effData.addElement(rowEff);
							rowEff = null;
						}

						lnr++;
					}// have data

					if ((char) iread == lineSep) {
						lnrr++;
					}

					// Finally empty the buffer!
					int nn = stringBuffer.capacity();
					stringBuffer.delete(0, nn);// cleanUp
					stringBuffer.trimToSize();// 0 size!
				}// else
			}// main while
			//in.close();
		}// try
		catch (Exception exc) {

			stopThread();
			statusL.setText(resources.getString("status.error"));

			peakEfficiencyV = null;
			totalEfficiencyV = null;

			exc.printStackTrace();
			return;
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
		// --------------------if here, all are good!
		prepareEfficiencies();
		// -------------------finalization---
		// only energy is needed!!!!
		peakEfficiencyV = null;
		totalEfficiencyV = null;

		stopThread();
		statusL.setText(resources.getString("status.efficienciesLoaded"));
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
	 * Prepare efficiency data for interpolation
	 */
	private void prepareEfficiencies() {
		// first sort by energies
		Sort.qSort(effData, 3, 0);// after energy; energy, peakEff,totalEff=3
									// columns
		// second prepare m, n data vectors: eff=m*energy+n
		m_peak = new double[effData.size()];
		n_peak = new double[effData.size()];
		m_total = new double[effData.size()];
		n_total = new double[effData.size()];
		for (int i = 0; i < effData.size() - 1; i++) {
			m_peak[i] = (Convertor.stringToDouble(getValueAt(effData, i, 1)
					.toString()) - // 1=peak,0=energy
					Convertor.stringToDouble(getValueAt(effData, i + 1, 1)
							.toString()))
					/ (Convertor.stringToDouble(getValueAt(effData, i, 0)
							.toString()) - Convertor.stringToDouble(getValueAt(
							effData, i + 1, 0).toString()));
			m_total[i] = (Convertor.stringToDouble(getValueAt(effData, i, 2)
					.toString()) - // 2=total,0=energy
					Convertor.stringToDouble(getValueAt(effData, i + 1, 2)
							.toString()))
					/ (Convertor.stringToDouble(getValueAt(effData, i, 0)
							.toString()) - Convertor.stringToDouble(getValueAt(
							effData, i + 1, 0).toString()));
			n_peak[i] = (Convertor.stringToDouble(getValueAt(effData, i + 1, 1)
					.toString())
					* Convertor.stringToDouble(getValueAt(effData, i, 0)
							.toString()) - Convertor.stringToDouble(getValueAt(
					effData, i, 1).toString())
					* Convertor.stringToDouble(getValueAt(effData, i + 1, 0)
							.toString()))
					/ (Convertor.stringToDouble(getValueAt(effData, i, 0)
							.toString()) - Convertor.stringToDouble(getValueAt(
							effData, i + 1, 0).toString()));
			n_total[i] = (Convertor
					.stringToDouble(getValueAt(effData, i + 1, 2).toString())
					* Convertor.stringToDouble(getValueAt(effData, i, 0)
							.toString()) - Convertor.stringToDouble(getValueAt(
					effData, i, 2).toString())
					* Convertor.stringToDouble(getValueAt(effData, i + 1, 0)
							.toString()))
					/ (Convertor.stringToDouble(getValueAt(effData, i, 0)
							.toString()) - Convertor.stringToDouble(getValueAt(
							effData, i + 1, 0).toString()));
		}
		m_peak[effData.size() - 1] = m_peak[effData.size() - 2];
		n_peak[effData.size() - 1] = n_peak[effData.size() - 2];
		m_total[effData.size() - 1] = m_total[effData.size() - 2];
		n_total[effData.size() - 1] = n_total[effData.size() - 2];

		effData = null;// not needed anymore!
	}

	/**
	 * Compute coincidence correction
	 */
	private void compute() {
		if (m_total == null) {
			String title = resources.getString("dialog.coin.title");

			String message = resources.getString("dialog.coin.message");

			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		sumXrayYieldEffTotal = 0.0;
		for (int i = 0; i < xenergy.length; i++) {
			sumXrayYieldEffTotal = sumXrayYieldEffTotal + xyield[i]
					* getTotalEff(xenergy[i]);
		}

		if (useXRayCoin.isSelected()) {
			XRAYfactor = 1.0;
		} else {
			XRAYfactor = 0.0;
		}

		energyV = new Vector<Double>();
		yieldsRealV = new Vector<Double>();
		pOutV = new Vector<Double>();
		pInV = new Vector<Double>();

		// aLways nuclides.length>0..include parent!
		for (int t = 0; t < nuclides.length; t++) {
			String fileName = "";

			String fileName0 = nuclides[t];
			double bRatio = BR[t];

			// BM files:
			fileName = fileName0 + resources.getString("coin.file.BM");
			if (readCoinFile(fileName)) {
				computeIndividualCorrection(bRatio);
			}

			// EC files:
			fileName = fileName0 + resources.getString("coin.file.EC");
			if (readCoinFile(fileName)) {
				computeIndividualCorrection(bRatio);
			}

			// AL files:
			fileName = fileName0 + resources.getString("coin.file.AL");
			if (readCoinFile(fileName)) {
				computeIndividualCorrection(bRatio);
			}

			// IT files:
			fileName = fileName0 + resources.getString("coin.file.IT");
			if (readCoinFile(fileName)) {
				computeIndividualCorrection(bRatio);
			}
		}// end for by nuclides
			// --------finnaly---
			// sorting
		int ncols = 5;// en,yieldReal,pout,pin (1-pout and 1+pin),
						// (1-pout)(1+pin)
		int nrows = energyV.size();
		Vector<String> rowInfo = new Vector<String>();
		corrData = new Vector<Object>();
		double d = 0.0;
		for (int i = 0; i < nrows; i++) {
			rowInfo = new Vector<String>();
			rowInfo.addElement(energyV.elementAt(i).toString());
			rowInfo.addElement(yieldsRealV.elementAt(i).toString());
			d = Math.abs(1 - pOutV.elementAt(i));// 1-pout..ABS=just in case!
			rowInfo.addElement(Convertor.doubleToString(d));// summout
			d = Math.abs(1 + pInV.elementAt(i));// 1+pin
			rowInfo.addElement(Convertor.doubleToString(d));// summin
			d = Math.abs((1 - pOutV.elementAt(i)) * (1 + pInV.elementAt(i)));
			rowInfo.addElement(Convertor.doubleToString(d));// total corr
			corrData.addElement(rowInfo);
		}

		Sort.qSort(corrData, ncols, 0);// after energy

		// display:
		textArea.selectAll();
		textArea.replaceSelection("");
		for (int i = 0; i < corrData.size(); i++) {
			String s = resources.getString("coin.text.energy")
					+ Convertor.formatNumber(Convertor
							.stringToDouble(getValueAt(corrData, i, 0)
									.toString()), 2)
					+ "; "
					+ resources.getString("coin.text.summout")
					+ Convertor.formatNumber(Convertor
							.stringToDouble(getValueAt(corrData, i, 2)
									.toString()), 3)
					+ "; "
					+ resources.getString("coin.text.summin")
					+ Convertor.formatNumber(Convertor
							.stringToDouble(getValueAt(corrData, i, 3)
									.toString()), 3)
					+ "; "
					+ resources.getString("coin.text.coincidence")
					+ Convertor.formatNumber(Convertor
							.stringToDouble(getValueAt(corrData, i, 4)
									.toString()), 3) + "\n";
			textArea.append(s);
		}
		statusL.setText(resources.getString("status.done"));
	}

	/**
	 * Internally used by compute method.
	 * @param bRatio bRatio
	 */
	private void computeIndividualCorrection(double bRatio) {
		// first initialize some variable
		pout = new double[e_readV.size()];
		pin = new double[e_readV.size()];

		// adding energy values to overall energy array and initialize to 0
		// some variable
		for (int i = 0; i < e_readV.size(); i++) {
			energyV.addElement(e_readV.elementAt(i));
			yieldsRealV.addElement(bRatio * y_readV.elementAt(i) / 100.0);// number!!
			pout[i] = 0.0;
			pin[i] = 0.0;
		}

		// ---------Now compute correction------------
		for (int i = 0; i < e_readV.size() - 1; i++) {
			for (int j = i + 1; j < e_readV.size(); j++) {
				int p = parentLevelV.elementAt(i);
				int d = daughterLevelV.elementAt(j);
				if (p == d) {
					// we have a cascade
					int pp = parentLevelV.elementAt(j);
					pout[i] = pout[i]
							+ y_readV.elementAt(j)
							* bRatio
							* getTotalEff(e_readV.elementAt(j))// number!
							/ 100.0// here due to y_readV which is %!!
							+ XRAYfactor
							* sumXrayYieldEffTotal
							* bRatio
							* (ecLevelFeedV.elementAt(p) + ecLevelFeedV
									.elementAt(pp)) / 100.0;// level feed per
															// 100

					pout[j] = pout[j] + y_readV.elementAt(i) * bRatio
							* getTotalEff(e_readV.elementAt(i)) / 100.0
							+ XRAYfactor * sumXrayYieldEffTotal * bRatio
							* (ecLevelFeedV.elementAt(pp)) / 100.0;

					// check if there are any summin energy:
					for (int k = j + 1; k < e_readV.size(); k++) {
						if ((parentLevelV.elementAt(k) == parentLevelV
								.elementAt(j))
								&& (daughterLevelV.elementAt(k) == daughterLevelV
										.elementAt(i))) {
							// we have summin
							pin[k] = pin[k]
									+ y_readV.elementAt(i)
									* bRatio
									* getPeakEff(e_readV.elementAt(i))
									/ 100.0
									* y_readV.elementAt(j)
									* bRatio
									* getPeakEff(e_readV.elementAt(j))
									/ 100.0
									/ (y_readV.elementAt(k) * bRatio
											* getPeakEff(e_readV.elementAt(k)) / 100.0);
						}
					}// end for for sumin

				}// end cascade p==d
			}// end for j
		}// end for i

		for (int i = 0; i < e_readV.size(); i++) {
			// colect data in master array!
			pOutV.addElement(pout[i]);
			pInV.addElement(pin[i]);
		}
	}

	/**
	 * Read coincidence data file
	 * @param fileName filename
	 * @return true on success
	 */
	private boolean readCoinFile(String fileName) {
		stringBuffer = new StringBuffer();

		String datas = resources.getString("data.load");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String effData = resources.getString("data.eff.load");
		String coinData = resources.getString("data.eff.coin.load");
		String opens = currentDir + file_sep + datas + file_sep + effData
				+ file_sep + coinData + file_sep + fileName;
		// initialize reading vectors:
		e_readV = new Vector<Double>();
		y_readV = new Vector<Double>();
		parentLevelV = new Vector<Integer>();
		daughterLevelV = new Vector<Integer>();
		ecLevelFeedV = new Vector<Double>();

		double yThreshold = (Double) resources
				.getObject("coin.threshold.yield.value");

		int iread = 0;
		@SuppressWarnings("unused")
		int lnr = 0;// data number
		@SuppressWarnings("unused")
		int lnrr = 0;// line number
		char lineSep = '\n';
		String GAMMALINES = "$GAMMALINES:";
		boolean gammaB = false;
		boolean enB = true;// read energy
		boolean yB = true;// read yield
		boolean erryB = true;// read err yield
		boolean pB = true;// read parent level
		boolean dB = true;// read daughter level
		String LEVELS = "$LEVELS:";
		boolean levelsB = false;
		boolean elB = true;// energy level
		boolean ecB = true;// feed level by EC
		boolean bpB = true;// feed level by B+
		boolean bmB = true;// feed level by B- or IT
		boolean alB = true;// feed level by ALPHA
		boolean kB = true;// EC from level k
		boolean l1B = true;// EC from level L1
		boolean l2B = true;// EC from level L2
		boolean l3B = true;// EC from level L3
		boolean metaB = true;// metastable (1) or not (0)
		String LEVELSGAMMABRANCH = "$LEVELS-GAMMABranch:";// if here =>break
															// loop!

		boolean haveData = false;
		// test file existence:
		File tst = new File(opens);
		if (!tst.exists()) {
			return false;
		}

		// -----------------Now fetch data
		try {
			FileInputStream in = new FileInputStream(opens);

			while ((iread = in.read()) != -1) {
				if (!Character.isWhitespace((char) iread)) {
					stringBuffer.append((char) iread);
					haveData = true;
				} else {
					if (haveData)// we have data
					{
						haveData = false;// reset
						String s = stringBuffer.toString();

						// test if we must stop right now!
						if (s.equals(LEVELSGAMMABRANCH)) {
							break;// exit the loop!
							// WARNING: stringBuffer is not clear=>all readFile
							// in this class
							// must start with new stringBuffer!!!
						}

						// fetch data if permitted
						if (gammaB) {
							// gamma
							if (s.equals(LEVELS)) {
								// next time we have a start
								gammaB = false;
								levelsB = true;// start level data!!
							} else if (enB) {
								e_readV.addElement(Convertor.stringToDouble(s));
								enB = false;
							} else if (yB) {
								y_readV.addElement(Convertor.stringToDouble(s));
								yB = false;
							} else if (erryB) {
								erryB = false;
							} else if (pB) {
								parentLevelV.addElement(Convertor
										.stringToInt(s));
								pB = false;
							} else if (dB) {
								daughterLevelV.addElement(Convertor
										.stringToInt(s));

								enB = true;
								yB = true;
								erryB = true;
								pB = true;// again

								// remove if necessary
								int index = y_readV.size() - 1;
								if (y_readV.elementAt(index) < yThreshold) {// THRESHOLD!
									e_readV.removeElementAt(index);
									y_readV.removeElementAt(index);
									parentLevelV.removeElementAt(index);
									daughterLevelV.removeElementAt(index);
								}
							}

						} else if (levelsB) {
							// levels
							if (elB) {
								elB = false;
							} else if (ecB) {
								ecLevelFeedV.addElement(Convertor
										.stringToDouble(s));
								ecB = false;
							} else if (bpB)
								bpB = false;
							else if (bmB)
								bmB = false;
							else if (alB)
								alB = false;
							else if (kB)
								kB = false;
							else if (l1B)
								l1B = false;
							else if (l2B)
								l2B = false;
							else if (l3B)
								l3B = false;
							else if (metaB) {
								elB = true;
								ecB = true;
								bpB = true;
								bmB = true;
								alB = true;
								kB = true;
								l1B = true;
								l2B = true;
								l3B = true;
							}
						}
						// check if we have a start:
						if (s.equals(GAMMALINES)) {
							// next time we have a start
							gammaB = true;
						}

						lnr++;
					}// have data

					if ((char) iread == lineSep) {
						lnrr++;
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

			// stopThread();
			// statusL.setText(resources.getString("status.error"));

			exc.printStackTrace();
			return false;
		}

		return true;// successfully read file!
	}

	/**
	 * Internally used by computation. Retrieve total (global or gross) efficiency.
	 * @param energy energy
	 * @return the result
	 */
	private double getTotalEff(double energy) {
		double result = 1.0;
		// looking in what bin we are:
		int index = eff_energyV.size() - 1;// last bin!!
		for (int i = 0; i < eff_energyV.size(); i++) {
			if (energy < eff_energyV.elementAt(i)) {
				index = i - 1;
				break;
			}
		}
		if (index == -1)
			index = 0;// use 0
		// ------------------
		result = m_total[index] * energy + n_total[index];
		return result / 100.0;// number not %!!
	}

	/**
	 * Internally used by computation. Retrieve photo-peak efficiency.
	 * @param energy energy
	 * @return the result
	 */
	private double getPeakEff(double energy) {
		double result = 1.0;
		// looking in what bin we are:
		int index = eff_energyV.size() - 1;// last bin!!
		for (int i = 0; i < eff_energyV.size(); i++) {
			if (energy < eff_energyV.elementAt(i)) {
				index = i - 1;
				break;
			}
		}
		if (index == -1)
			index = 0;// use 0
		// ------------------
		result = m_peak[index] * energy + n_peak[index];

		return result / 100.0;// number not %!!
	}

	/**
	 * Start the computation thread.
	 */
	private void startThread() {
		stopAnim=false;
		if (computationTh == null) {
			computationTh = new Thread(this);
			computationTh.start();// Allow one simulation at time!
			//setEnabled(false);
		}

		if (statusTh == null) {
			statusTh = new Thread(this);
			statusTh.start();
		}
	}

	/**
	 * Stop the computation thread.
	 */
	private void stopThread() {
		stopAnim=true;
		statusTh = null;
		frameNumber = 0;
		computationTh = null;
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
			if (command.equals(LOAD_COMMAND)) {
				readFile();
			}
		}
	}

	/**
	 * Initialize database
	 */
	private void performQueryDb() {
		nesteddbagent.setLinks(IDlinkS, Convertor.intToString(IDLink));//for display
		nesteddbagent.init();
		//nestedorderbyS = nestedTablePrimaryKey;//already set in constructor!!!
		
		nestedTable = nesteddbagent.getMainTable();
		//ListSelectionModel rowSM2 = nestedTable.getSelectionModel();
		//rowSM2.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);//ListSelectionModel.SINGLE_SELECTION);
		//rowSM2.addListSelectionListener(this);//No listener
		
		/*try {
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = mf.gammaDB;
			opens = opens + file_sep + dbName;

			String s = "select * from " + mf.gammaNuclidesCoincidenceTable
					+ " WHERE ID = " + IDLink + " ORDER BY NRCRT";

			Connection con1 = DBConnection.getDerbyConnection(opens, "", "");
			DBOperation.select(s, con1);

			asp = new AdvancedSelectPanel();
			suportSp.add(asp, BorderLayout.CENTER);

			JTable mainTable = asp.getTab();

			ListSelectionModel rowSM = mainTable.getSelectionModel();
			rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

			if (mainTable.getRowCount() > 0) {
				// always display last row!
				mainTable.setRowSelectionInterval(mainTable.getRowCount() - 1,
						mainTable.getRowCount() - 1);
			}

			if (con1 != null)
				con1.close();

		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}

	/*private void performCurrentSelection() {
		suportSp.remove(asp);
		performQueryDb();
		validate();
	}*/

}
