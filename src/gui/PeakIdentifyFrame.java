package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import danfulea.db.DatabaseAgent;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.math.Convertor;
import danfulea.math.Sort;
import danfulea.phys.GammaRoi;
import danfulea.utils.FrameUtilities;

@SuppressWarnings("serial")
/**
 * Class used for automatically identify peaks from library. 
 * Use this with caution, a good nuclear physicist always setup ROIs manually. 
 * 
 * @author Dan Fulea, 14 Jun. 2011
 */
public class PeakIdentifyFrame extends JFrame implements ActionListener{
	
	private GammaAnalysisFrame mf;
	private final Dimension PREFERRED_SIZE = new Dimension(800, 700);
	private final Dimension textAreaDimension = new Dimension(750, 500);
	private final Dimension sizeCb = new Dimension(60, 21);
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;
	private JRadioButton fwhmDeltaRb,energyDeltaRb;
	private JTextField afwhmTf=new JTextField(5);
	private JTextField aenergyTf=new JTextField(5);
	private JTextField benergyTf=new JTextField(5);
	@SuppressWarnings("rawtypes")
	private JComboBox idCb;
	private JTextArea textArea;
	private String command = "";
	private static final String IDENTIFY_COMMAND = "IDENTIFY";
	private static final String SET_COMMAND = "SET";
	private String gammaDB = "";
	private String inUseLibraryTable = "";
	private String inUseLibraryDetailTable = "";
	private String[] nucConfig;
	private String[] configID;
	private String[] roiIDConfig;
	
	/**
	 * Constructor
	 * @param mf the GammaAnalysisFrame object
	 */
	public PeakIdentifyFrame(GammaAnalysisFrame mf){
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("PeakIdentify.NAME"));
		this.mf=mf;
		gammaDB = resources.getString("main.db");
		inUseLibraryTable = resources.getString("main.db.gammaNuclidesTable");
		inUseLibraryDetailTable = resources.getString("main.db.gammaNuclidesDetailsTable");
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
	 * Program close
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

	@SuppressWarnings("rawtypes")
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
		afwhmTf.setText("1");
		aenergyTf.setText("7");
		benergyTf.setText("0.05");
		fwhmDeltaRb = new JRadioButton(resources.getString("peakIdentify.fwhmDeltaRb"));
		fwhmDeltaRb.setForeground(GammaAnalysisFrame.foreColor);
		fwhmDeltaRb.setBackground(GammaAnalysisFrame.bkgColor);
		energyDeltaRb = new JRadioButton(resources.getString("peakIdentify.energyDeltaRb"));
		energyDeltaRb.setBackground(GammaAnalysisFrame.bkgColor);
		energyDeltaRb.setForeground(GammaAnalysisFrame.foreColor);
		ButtonGroup group = new ButtonGroup();
		group.add(fwhmDeltaRb);
		group.add(energyDeltaRb);
		fwhmDeltaRb.setSelected(true);
		
		JPanel p1 = new JPanel();
		p1.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p1.setBackground(GammaAnalysisFrame.bkgColor);
		p1.add(fwhmDeltaRb);
		label=new JLabel(resources.getString("peakIdentify.fwhmLb1"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p1.add(label);
		p1.add(afwhmTf);
		label=new JLabel(resources.getString("peakIdentify.fwhmLb2"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p1.add(label);
		
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p2.setBackground(GammaAnalysisFrame.bkgColor);
		p2.add(energyDeltaRb);
		label=new JLabel(resources.getString("peakIdentify.energyLb1"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p2.add(label);
		p2.add(aenergyTf);
		label=new JLabel(resources.getString("peakIdentify.energyLb2"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p2.add(label);
		p2.add(benergyTf);
		label=new JLabel(resources.getString("peakIdentify.energyLb3"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p2.add(label);
		
		JPanel p3 = new JPanel();
		p3.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p3.setBackground(GammaAnalysisFrame.bkgColor);
		buttonName = resources.getString("peakIdentify.identify.button");
		buttonToolTip = resources.getString("peakIdentify.identify.button.toolTip");
		buttonIconName = resources.getString("img.view");
		button = FrameUtilities.makeButton(buttonIconName, IDENTIFY_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("peakIdentify.identify.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p3.add(button);
		
		textArea = new JTextArea();
		textArea.setCaretPosition(0);
		textArea.setEditable(false);
		textArea.setText("");
		textArea.setWrapStyleWord(true);
		textArea.setBackground(GammaAnalysisFrame.textAreaBkgColor);
		textArea.setForeground(GammaAnalysisFrame.textAreaForeColor);
		JPanel resultP = new JPanel(new BorderLayout());
		resultP.setPreferredSize(textAreaDimension);
		resultP.add(new JScrollPane(textArea), BorderLayout.CENTER);
		resultP.setBackground(GammaAnalysisFrame.bkgColor);
		
		idCb = new JComboBox();
		idCb.setMaximumRowCount(5);
		idCb.setPreferredSize(sizeCb);
		JPanel p4 = new JPanel();
		p4.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 2));
		p4.setBackground(GammaAnalysisFrame.bkgColor);
		label=new JLabel(resources.getString("peakIdentify.idLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p4.add(label);
		p4.add(idCb);
		buttonName = resources.getString("peakIdentify.set.button");
		buttonToolTip = resources.getString("peakIdentify.set.button.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, SET_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("peakIdentify.set.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p4.add(button);
		
		JPanel northP = new JPanel();
		BoxLayout blnorthP = new BoxLayout(northP, BoxLayout.Y_AXIS);
		northP.setLayout(blnorthP);
		northP.setBackground(GammaAnalysisFrame.bkgColor);
		northP.add(p1, null);
		northP.add(p2, null);
		northP.add(p3, null);
		
		JPanel mainP = new JPanel(new BorderLayout());
		mainP.add(northP, BorderLayout.NORTH);
		mainP.add(resultP, BorderLayout.CENTER);
		mainP.add(p4, BorderLayout.SOUTH);
		mainP.setBackground(GammaAnalysisFrame.bkgColor);
		
		JPanel content = new JPanel(new BorderLayout());		
		content.add(mainP, BorderLayout.CENTER);
		setContentPane(new JScrollPane(content));
		content.setOpaque(true); 
		pack();
	}
	
	/**
	 * Setting up actions!
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		// String command = arg0.getActionCommand();
		command = arg0.getActionCommand();
		if (command.equals(IDENTIFY_COMMAND)) {
			identify();
		} else if (command.equals(SET_COMMAND)) {
			set();
		}
		
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Peak identify routine.
	 */
	private void identify(){
		boolean fwhmB=fwhmDeltaRb.isSelected();
		double afwhm = 0.0;
		double aenergy = 0.0;
		double benergy=0.0;
		boolean nulneg = false;
		try {
			if (fwhmB){
				afwhm = Convertor.stringToDouble(afwhmTf.getText());
				if (afwhm <= 0)
					nulneg = true;
			} else {
				aenergy = Convertor.stringToDouble(aenergyTf.getText());
				benergy = Convertor.stringToDouble(benergyTf.getText());
				if (benergy <= 0)
					nulneg = true;
			}
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
		//-----------------------
		int n= mf.roiV.size();
		
			
		try {
			// prepare db query data
			String datas = resources.getString("data.load");
			String currentDir = System.getProperty("user.dir");
			String file_sep = System.getProperty("file.separator");
			String opens = currentDir + file_sep + datas;
			String dbName = gammaDB;
			opens = opens + file_sep + dbName;
			// make a connection
			Connection con1 = DatabaseAgent.getConnection(opens, "", "");//DBConnection.getDerbyConnection(opens, "", "");
			// first make a selection to retrieve usefull data
			String s = "select * from " + inUseLibraryTable;
			//DBOperation.select(s, con1);
			DatabaseAgent.select(con1, s);

			int rows = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
			int[] IDs =new int[rows];
			
			Vector<String> configIDV= new Vector<String>();
			Vector<String> nucV = new Vector<String>();
			Vector<String> corrCoefV= new Vector<String>();			
			Vector<String> linesConfigV= new Vector<String>();
			Vector<String> roiIDConfigV= new Vector<String>();
			Vector<String> energyFoundConfigV= new Vector<String>();
			
			int ncols = 6;// id,nuc,corr,lines,roiId,energies
			Vector<String> rowInfo = new Vector<String>();
			Vector<Object> dataInfo = new Vector<Object>();
			
			for (int j=0;j<rows;j++){
				IDs[j]=Convertor.stringToInt(DatabaseAgent.getValueAt(j, 0).toString());//DBOperation.getValueAt(j, 0).toString());
				nucV.addElement(DatabaseAgent.getValueAt(j, 1).toString());//DBOperation.getValueAt(j, 1).toString());
				//System.out.println(""+IDs[j]);
			}
						
			idCb.removeAllItems();			
			
			double[] corrCoef =new double[rows];
			configID =new String[rows];
			roiIDConfig =new String[rows];
			String[] energyFoundConfig =new String[rows];
			String[] linesConfig =new String[rows];
			nucConfig =new String[rows];
			//LOOP OVER ALL DATABASE NUCLIDES	
			for (int j=0;j<rows;j++){
				rowInfo = new Vector<String>();
				//reset
				double tmpNum=0.0;
				double y2=0.0;
				double tmpNe2=0.0;
				
				configIDV.addElement(Convertor.intToString(j+1));
				idCb.addItem(Convertor.intToString(j+1));
				Vector<Integer> roiIdV=new Vector<Integer>();
				Vector<Double> energyFoundV=new Vector<Double>();
				int linesFound=0;
				
				//now retrieve data:	
				s = "select * from " + inUseLibraryDetailTable+
				" where ID = " + IDs[j] + " ORDER BY NRCRT";
				//DBOperation.select(s, con1);
				DatabaseAgent.select(con1, s);
				int ndata = DatabaseAgent.getRowCount();//DBOperation.getRowCount();
				
				int linesTotal=ndata;
				
				//LOOP OVER PARTICULAR NUCLIDE DATA
				for (int k=0;k<ndata;k++){
					double kron=0.0;//Kronecker!
					double en = Convertor.stringToDouble(DatabaseAgent.getValueAt(k, 2).toString());//DBOperation.getValueAt(k, 2).toString());
					double y= Convertor.stringToDouble(DatabaseAgent.getValueAt(k, 3).toString());//DBOperation.getValueAt(k, 3).toString());
					double effProc=GammaRoi.getEffFromEnergy(en);
					y2=y2+y*y;
					//LOOP OVER ALL ROIS
					for (int i=0;i<n;i++){
						GammaRoi gr=mf.roiV.elementAt(i);
						double e0=gr.getCentroidEnergy();
						double fwhm=gr.getFwhmEnergyCalib();
						double countsRate=gr.getGrossCountsRate();
						//any rate here used only in correlation calculation!!!
						double deltaE=0.0;
						if (fwhmB){
							deltaE=afwhm*fwhm;
						} else {
							deltaE=aenergy+benergy*e0;
						}
						deltaE=Math.abs(deltaE);//>0
			
						if ((en>=e0-deltaE) && (en<=e0+deltaE)){
							//found some energy line
							kron=1.0;//mark!!
							linesFound++;
							roiIdV.addElement(i+1);
							energyFoundV.addElement(en);
						} else {
							kron = 0.0;
						}
						tmpNum=tmpNum+kron*y*Math.abs(100.0*countsRate/effProc);
						tmpNe2=tmpNe2+kron*Math.abs((100.0*countsRate/effProc)*
								(100.0*countsRate/effProc));
					}//for after mf.roiV.size()
					
				} //after all nuclide energies and...
				if (y2*tmpNe2!=0.0){
				   corrCoefV.addElement(Convertor.doubleToString
						   (tmpNum/Math.sqrt(y2*tmpNe2)));
				} else
					corrCoefV.addElement("0.0");	
				//----------
				String ss="";
				String ss2="";
				String ss3=linesFound+"/"+linesTotal;
				for (int l=0;l<roiIdV.size();l++){
					ss=ss+roiIdV.elementAt(l)+"_";
					ss2=ss2+energyFoundV.elementAt(l)+"_";
				}
				roiIDConfigV.addElement(ss);
				energyFoundConfigV.addElement(ss2);
				linesConfigV.addElement(ss3);
				//---------
				rowInfo.addElement(configIDV.elementAt(j));
				rowInfo.addElement(nucV.elementAt(j));
				rowInfo.addElement(corrCoefV.elementAt(j).toString());
				rowInfo.addElement(linesConfigV.elementAt(j));
				rowInfo.addElement(roiIDConfigV.elementAt(j));
				rowInfo.addElement(energyFoundConfigV.elementAt(j));

				dataInfo.addElement(rowInfo);
			}// for after all nuclides!	
			
			Sort.qSort(dataInfo, ncols, 2);// after corr!ascending!!
						
			textArea.selectAll();
			textArea.replaceSelection("");
			
			for (int i=0;i<rows;i++){//descending!!
				configID[i]= getValueAt(dataInfo, rows-1-i, 0).toString();
				nucConfig[i]= getValueAt(dataInfo, rows-1-i, 1).toString();
				corrCoef[i]= Convertor.stringToDouble(
						getValueAt(dataInfo, rows-1-i, 2).toString());
				linesConfig[i]= getValueAt(dataInfo, rows-1-i, 3).toString();
				roiIDConfig[i]= getValueAt(dataInfo, rows-1-i, 4).toString();
				energyFoundConfig[i]= getValueAt(dataInfo, rows-1-i, 5).toString();
				
				///========
				s=resources.getString("peakIdentify.text.id")+configID[i]+"; "
				+ resources.getString("peakIdentify.text.nuc")+nucConfig[i]+"; "
				+ resources.getString("peakIdentify.text.corr")+
				Convertor.formatNumber(corrCoef[i], 2)+"; "
				+ resources.getString("peakIdentify.text.lines")+linesConfig[i]+"; "
				+ resources.getString("peakIdentify.text.rois")+roiIDConfig[i]+"; "
				+ resources.getString("peakIdentify.text.energies")+energyFoundConfig[i]
				+ " \n";
				textArea.append(s);
			}
			
				
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
	 * Return the array of ROI IDs based on encoded String (e.g. 1_4_6)
	 * @param s s
	 * @return the result
	 */
	private int[] getROIsIDs(String s){
		Vector<Integer> idRoiV=new Vector<Integer>();
		StringTokenizer st=new StringTokenizer(s,"_");
		while(st.hasMoreElements()){
			String token=st.nextToken();			
			idRoiV.addElement(Convertor.stringToInt(token));
		}
		int[] result=new int[idRoiV.size()];
		for (int i=0;i<idRoiV.size();i++){
			result[i]=idRoiV.elementAt(i);
			//System.out.println(result[i]+"");
		}
		
		return result;
	}
	
	/**
	 * Set the configuration, i.e. the nuclide assignment.
	 */
	private void set(){
		int index=idCb.getSelectedIndex();
		int realIndex=0;
		//0=1;1=2....
		for (int i=0;i<nucConfig.length;i++){
			if (Convertor.stringToInt(configID[i])==index+1){
				realIndex=i;
			}
		}
		String nuc=nucConfig[realIndex];
		String rois=roiIDConfig[realIndex];
		int[] roisIds=getROIsIDs(rois);
		mf.setROIs(roisIds, nuc);		
		if (GammaAnalysisFrame.failPI){
			textArea.append("\n");
			textArea.append(GammaAnalysisFrame.warningsPI);
			textArea.append("\n");
		} else{
			String s= resources.getString("peakIdentify.text.nuc.set")+
			nuc + " \n";
			textArea.append(s);
		}
	}
}
