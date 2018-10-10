package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.apache.pdfbox.pdmodel.PDDocument;

import danfulea.db.DatabaseAgent;
//import jdf.db.DBConnection;
//import jdf.db.DBOperation;
import danfulea.math.Convertor;
import danfulea.math.StatsUtil;
import danfulea.phys.GammaRoi;
import danfulea.utils.ExampleFileFilter;
import danfulea.utils.FileOperation;
import danfulea.utils.FrameUtilities;
import danfulea.utils.PDFRenderer;
import danfulea.utils.TimeUtilities;

/**
 * The GammaResults window displays the gamma analysis results. 
 * 
 * @author Dan Fulea, 19 May. 2011
 */
@SuppressWarnings({ "serial", "unused" })
public class GammaResultsFrame extends JFrame implements ActionListener {
	private final Dimension PREFERRED_SIZE = new Dimension(900, 700);
	private final Dimension ta_PREFERRED_SIZE = new Dimension(850, 500);
	private GammaAnalysisFrame mf;
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	protected ResourceBundle resources;
	
	//protected JTextArea textArea = new JTextArea();
	JEditorPane textArea = new JEditorPane("text/html", "");
	private JScrollPane jScrollPane1 = new JScrollPane(textArea);//();
	
	private int imode = 0;
	public static final int IMODE_ROISDATA = 0;
	public static final int IMODE_SAMPLEDATA = 1;
	private static final String PRINT_COMMAND = "PRINT";
	private String command = "";
	protected String outFilename = null;
	private JLabel statusL = new JLabel("Waiting...");
	
	private String htmlToPrint ="";
	private File macroFile =null;//store the temporary html file

	/**
	 * Constructor. 
	 * @param mf, the GammaAnalysisFrame object
	 * @param imode imode (ROI or sample)
	 */
	public GammaResultsFrame(GammaAnalysisFrame mf, int imode) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("Results.NAME"));
		// this.setResizable(false);

		this.imode = imode;
		this.mf = mf;

		createGUI();

		setDefaultLookAndFeelDecorated(true);
		FrameUtilities.createImageIcon(
				this.resources.getString("form.icon.url"), this);

		FrameUtilities.centerFrameOnScreen(this);

		setVisible(true);
		mf.setEnabled(false);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);// not necessary,
																// exit normal!
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				attemptExit();
			}
		});
	}

	/**
	 * Setting up the frame size.
	 */
	public Dimension getPreferredSize() {
		return PREFERRED_SIZE;
	}

	/**
	 * Exit method
	 */
	private void attemptExit() {
		//File macroFile = new File(outFilename)
		if (macroFile!=null){//always not null!!! here
		try{
			macroFile.delete();
		} catch (Exception e){
			e.printStackTrace();
		}
		}
		
		mf.setEnabled(true);
		dispose();
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

		//JPanel taHolder = new JPanel(new BorderLayout());
		//taHolder.setBorder(new javax.swing.border.TitledBorder(
		//		new javax.swing.border.LineBorder(
		//				new java.awt.Color(0, 51, 255), 1, true),
		//		this.resources.getString("results.title"),
		//		javax.swing.border.TitledBorder.CENTER,
		//		javax.swing.border.TitledBorder.TOP));
		
		jScrollPane1.setBorder(new javax.swing.border.TitledBorder(
				new javax.swing.border.LineBorder(
						new java.awt.Color(0, 51, 255), 1, true),
				this.resources.getString("results.title"),
				javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.TOP));
		jScrollPane1
				.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jScrollPane1
				.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setAutoscrolls(true);
		//textArea.setColumns(1);
		textArea.setEditable(false);
		///*****************************
		jScrollPane1.setPreferredSize(ta_PREFERRED_SIZE);

		//textArea.setLineWrap(true);
		//textArea.setRows(10);
		//String s = "";h
		if (imode == IMODE_ROISDATA)
			htmlToPrint = parseRoiReport();
		else if (imode == IMODE_SAMPLEDATA)
			htmlToPrint = parseSampleReport();
		//create HTML document============
		String htmlTemp = "temp.html";
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String filename = currentDir + file_sep + htmlTemp; 
		macroFile=new File(filename);
		try {
			FileWriter sigfos = new FileWriter(macroFile);//filename);
			sigfos.write(htmlToPrint);
			sigfos.close();			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		//////and display it		
		try {
			java.net.URL filenamURL = new File(filename).toURI().toURL();
			//helpURL = new java.net.URL(filename);		
				//ClassLoader.getSystemResource(filename);//"input.html");
			textArea.setPage(filenamURL);//helpURL);
		//System.out.println(helpURL.toString());
					
		} catch (Exception  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//.setText(htmlToPrint);//error!!!!!!!!!!!
		
		//textArea.setWrapStyleWord(true);
		//jScrollPane1.setViewportView(textArea);
		
		//taHolder.add(textArea, BorderLayout.CENTER);
		//taHolder.setBackground(GammaAnalysisFrame.bkgColor);
		
		textArea.setBackground(GammaAnalysisFrame.textAreaBkgColor);
		textArea.setForeground(GammaAnalysisFrame.textAreaForeColor);

		JPanel jPanel2 = new JPanel();
		jPanel2.setLayout(new java.awt.BorderLayout());
		jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);
		//jPanel2.add(taHolder, java.awt.BorderLayout.CENTER);
		jPanel2.setBackground(GammaAnalysisFrame.bkgColor);

		buttonName = resources.getString("results.print.html");
		buttonToolTip = resources.getString("results.print.toolTip.html");
		buttonIconName = resources.getString("img.printer");
		button = FrameUtilities.makeButton(buttonIconName, PRINT_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources.getObject("results.print.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		JPanel butP = new JPanel();
		butP.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 1));
		butP.add(button);
		butP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel mainP = new JPanel(new BorderLayout());
		mainP.add(jPanel2, BorderLayout.CENTER);
		mainP.add(butP, BorderLayout.SOUTH);

		// setContentPane(mainP);
		JPanel content = new JPanel(new BorderLayout());
		content.add(mainP);

		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		initStatusBar(statusBar);
		content.add(statusBar, BorderLayout.PAGE_END);

		setContentPane(new JScrollPane(content));
		pack();
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
	 * Generates report for each ROI
	 * @return the result
	 */
	private String parseRoiReport() {
		int day = 0;
		int month = 0;
		int year = 0;
		boolean nulneg = false;
		String spectrumName = "";// mf.quantity is ok
		String measurementDate = "";
		if (mf.measurementDate.equals("")) {// no spectrum save or load
			spectrumName = mf.idSpectrumTf.getText();
			try {
				day = Convertor
						.stringToInt((String) mf.dayCb.getSelectedItem());
				month = Convertor.stringToInt((String) mf.monthCb
						.getSelectedItem());
				year = Convertor.stringToInt(mf.yearTf.getText());

				//TimeUtilities.setDate(day, month, year);
				TimeUtilities tu = new TimeUtilities(day, month, year);
				//measurementDate = TimeUtilities.formatDate();
				measurementDate = tu.formatDate();

				if (year < 0 || month < 0 || day < 0)
					nulneg = true;
			} catch (Exception e) {
				measurementDate = "?";
			}
			if (nulneg) {
				measurementDate = "?";
			}
		} else {
			spectrumName = mf.spectrumName;
			measurementDate = mf.measurementDate;
		}
		// =============================================

		String resultS = this.resources.getString("results.spectrum.name")
				+ spectrumName + "; "
				+ this.resources.getString("results.spectrum.date")
				+ measurementDate + "<br><br>";//"\n\n";
		resultS = resultS + this.resources.getString("results.unc.roi.info")
				+ "<br><br>";//"\n\n";

		for (int i = 0; i < mf.roiV.size(); i++) {
			GammaRoi gr = mf.roiV.elementAt(i);
			// -----------------detection limit-----------
			// det limit and eror= mda*yield*eff[%]/100.0
			double ld = gr.getMda_Bq() * gr.getYield()
					* gr.getEfficiencyProcentual() / 100.0;
			double unc_ld = gr.getMda_BqError() * gr.getYield()
					* gr.getEfficiencyProcentual() / 100.0;// neglect eff error!
			// accurate:
			if (gr.getMda_Bq() > 0.0 && gr.getEfficiencyProcentual() > 0.0) {
				unc_ld = ld
						* Math.sqrt(Math.abs(Math.pow(
								gr.getMda_BqError() / gr.getMda_Bq(), 2.0)
								- Math.pow(gr.getEfficiencyProcentualError()
										/ gr.getEfficiencyProcentual(), 2.0)));
			}
			// -------------------
			resultS = resultS
					+ this.resources.getString("results.roiID")
					+ (i + 1)
					+ "; "
					+ this.resources.getString("results.roiNuclide")
					+ gr.getNuclide()
					+ this.resources.getString("results.roiNuclide.at")
					+ Convertor.formatNumber(gr.getCentroidEnergy(), 2)
					+ this.resources.getString("results.roiNuclide.keV")
					+ "<br>"//"\n"
					+ this.resources.getString("results.startChannel")
					+ gr.getStartChannel()
					+ "; "
					+ this.resources.getString("results.startEnergy")
					+ Convertor.formatNumber(gr.getStartEnergy(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.startEdgeChannel")
					+ gr.getStartEdgeChannel()
					+ "; "
					+ this.resources.getString("results.startEdgeEnergy")
					+ Convertor.formatNumber(gr.getStartEdgeEnergy(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.centerChannel")
					+ Convertor.formatNumber(gr.getCenterChannel(), 2)
					+ "; "
					+ this.resources.getString("results.centerEnergy")
					+ Convertor.formatNumber(gr.getCenterEnergy(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.centroidChannel")
					+ Convertor.formatNumber(gr.getCentroidChannel(), 2)
					+ "; "
					+ this.resources.getString("results.centroidEnergy")
					+ Convertor.formatNumber(gr.getCentroidEnergy(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.peakChannel")
					+ Convertor.formatNumber(gr.getPeakChannel(), 2)
					+ "; "
					+ this.resources.getString("results.peakEnergy")
					+ Convertor.formatNumber(gr.getPeakEnergy(), 2)
					+ "; "
					+ this.resources.getString("results.peakPulses")
					+ Convertor.formatNumber(gr.getPeakPulses(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.endChannel")
					+ gr.getEndChannel()
					+ "; "
					+ this.resources.getString("results.endEnergy")
					+ Convertor.formatNumber(gr.getEndEnergy(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.endEdgeChannel")
					+ gr.getEndEdgeChannel()
					+ "; "
					+ this.resources.getString("results.endEdgeEnergy")
					+ Convertor.formatNumber(gr.getEndEdgeEnergy(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.fwhmChannel")
					+ Convertor.formatNumber(gr.getFWHMChannel(), 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getFWHMChannelError(), 2)
					+ "; "
					+ this.resources.getString("results.fwhmEnergy")
					+ Convertor.formatNumber(gr.getFwhmEnergy(), 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getFwhmEnergyError(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.fwhmEnergyCalib")
					+ Convertor.formatNumber(gr.getFwhmEnergyCalib(), 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getFwhmEnergyCalibError(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.resolution")
					+ Convertor.formatNumber(gr.getResolution(), 2)
					+ "; "
					+ this.resources.getString("results.resolutionCalib")
					+ Convertor.formatNumber(gr.getResolutionCalib(), 2)
					+ "; "
					+ this.resources.getString("results.significance")
					+ gr.getSignificance()
					+ "<br>"//"\n"
					+ this.resources.getString("results.bkgCounts")
					+ Convertor.formatNumber(gr.getBkgCounts(), 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getBkgCountsError(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.bkgCountsRate")
					+ Convertor.formatNumberScientific(gr.getBkgCountsRate())//, 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumberScientific(gr.getBkgCountsRateError())//, 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.grossCounts")
					+ Convertor.formatNumber(gr.getGrossCounts(), 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getGrossCountsError(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.grossCountsRate")
					+ Convertor.formatNumberScientific(gr.getGrossCountsRate())//, 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumberScientific(gr.getGrossCountsRateError())//, 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.comptonCounts")
					+ Convertor.formatNumber(gr.getComptonCounts(), 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getComptonCountsError(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.comptonCountsRate")
					+ Convertor.formatNumberScientific(gr.getComptonCountsRate())//, 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumberScientific(gr.getComptonCountsRateError())//, 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.netCounts")
					+ Convertor.formatNumber(gr.getNetCounts(), 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getNetCountsError(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.netCountsRate")
					+ Convertor.formatNumberScientific(gr.getNetCountsRate())//, 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumberScientific(gr.getNetCountsRateError())//, 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.confidenceLevel")
					+ Convertor.formatNumber(gr.getConfidenceLevel(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.roiNuclide")
					+ gr.getNuclide()
					+ "; "
					+ this.resources.getString("results.yield")
					+ Convertor.formatNumber(gr.getYield(), 2)
					+ "<br>"//"\n"
					+ this.resources.getString("results.atomicMass")
					+ Convertor.formatNumber(gr.getAtomicMass(), 2)
					+ "; "
					+ this.resources.getString("results.halfLife")
					+ Convertor.formatNumber(gr.getHalfLife(), 2)
					+ " "
					+ gr.getHalfLifeUnits()
					+ "<br>"//"\n"
					+ this.resources.getString("results.efficiency")
					+ Convertor.formatNumber(gr.getEfficiencyProcentual(), 5)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getEfficiencyProcentualError(),
							5) + "<br>"//"\n"
					+ this.resources.getString("results.netCalculationMethod")
					+ gr.getNetCalculationMethod() + "<br>"//"\n"
					+ this.resources.getString("results.mdaCalculationMethod")
					+ gr.getMdaCalculationMethod() + "<br>"//"\n"
					+ this.resources.getString("results.detectionLimit")
					+ Convertor.formatNumberScientific(ld)//, 2)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumberScientific(unc_ld)//, 2)
					+ "<br>"//"\n"
					
					+ ( (gr.getDifference().equals("Yes"))?
						" <b><font color='green'>":" <b><font color='blue'>")			        
					+ this.resources.getString("results.activity")					
					+ Convertor.formatNumber(gr.getActivity_Bq(), 3)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getActivity_BqError(), 3)
					+ "</font></b> "
					+ "<br>"//"\n" 
					
					+ ( (gr.getDifference().equals("Yes"))?
							" <b><font color='blue'>":" <b><font color='green'>")
					+ this.resources.getString("results.mda")
					+ Convertor.formatNumber(gr.getMda_Bq(), 3)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(gr.getMda_BqError(), 3)
					+ "</font></b> "
					+ "<br>"//"\n"
					
					+" <b><font color='blue'>"
					+ this.resources.getString("results.difference")
					+ gr.getDifference()
					+ "</font></b> "
					+ "<br>";//\n";
			resultS = resultS + "----------------------------------"
					+"<br>";//"\n";
		}
		// -----------------
		return resultS;
	}	
	
	/**
	 * Perform final activity calculation and generate overall sample report.
	 * @return the result
	 */
	private String parseSampleReport() {
		int day = 0;
		int month = 0;
		int year = 0;
		boolean nulneg = false;
		String spectrumName = "";// mf.quantity is ok
		String measurementDate = "";
		if (mf.measurementDate.equals("")) {// no spectrum save or load
			spectrumName = mf.idSpectrumTf.getText();
			try {
				day = Convertor
						.stringToInt((String) mf.dayCb.getSelectedItem());
				month = Convertor.stringToInt((String) mf.monthCb
						.getSelectedItem());
				year = Convertor.stringToInt(mf.yearTf.getText());
				
				TimeUtilities tu = new TimeUtilities(day, month, year);
				measurementDate = tu.formatDate();

				if (year < 0 || month < 0 || day < 0)
					nulneg = true;
			} catch (Exception e) {
				measurementDate = "?";
			}
			if (nulneg) {
				measurementDate = "?";
			}
		} else {
			spectrumName = mf.spectrumName;
			measurementDate = mf.measurementDate;
		}
		// =============================================
		//When file is bundled inside the jar then it become byte stream instead of 
		//a normal File object.=>SOLUTION CREATE A HTML File!
		String imageRelFilePath = this.resources.getString("icon.package.url");
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String filename = currentDir + file_sep + imageRelFilePath; 
		java.net.URL filenamURL = null;
		try {
			filenamURL = new File(filename).toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String imgS="";
		imgS = "<img src="+
				filenamURL//ClassLoader.getSystemResource(resources.getString("icon.package.url")).toString()//"personal.png"//resources.getString("icon.url")
							+" width='64' height='64' > <br>";
		
		String resultS = imgS+ this.resources.getString("results.spectrum.name")
				+ spectrumName + "; "
				+ this.resources.getString("results.spectrum.date")
				+ measurementDate+ "; "
				+ this.resources.getString("results.spectrum.time")
				+ mf.spectrumLiveTime+ "; "
				+ this.resources.getString("results.sample.quanity")
				+ mf.quantity+" "+ mf.quantityUnit
				+"<br>"				
				+ this.resources.getString("results.spectrum.bkg.name")
				+ mf.bkgSpectrumName+ "; "
				+ this.resources.getString("results.spectrum.date")
				+ mf.bkgSpectrumDate+ "; "
				+ this.resources.getString("results.spectrum.time")
				+ mf.bkgSpectrumLiveTime	
				+"<br>"	
				+ this.resources.getString("results.cal.en")
				+ mf.energyCalName				
				+ "<br>"
				+ this.resources.getString("results.cal.fwhm")
				+ mf.fwhmCalName				
				+ "<br>"
				+ this.resources.getString("results.cal.eff")
				+ mf.effCalName				
				
				+ "<br><br>";// "\n\n";
		resultS = resultS + this.resources.getString("results.unc.info")
				+ "<br><br>";//"\n\n";
		//===============
		
		//========================
		double globalcountsrate=mf.globalCounts/mf.spectrumLiveTime;
		double errglobalcountsrate=Math.sqrt(mf.globalCounts)/mf.spectrumLiveTime;
		double bkgglobalcountsrate=0.0;//mf.globalBkgCounts/mf.bkgSpectrumLiveTime;
		double errbkgglobalcountsrate=0.0;//Math.sqrt(mf.globalBkgCounts)/mf.bkgSpectrumLiveTime;
		double netcountsrate=globalcountsrate-bkgglobalcountsrate;
		double errnetcountsrate=errglobalcountsrate;
		if (mf.bkgSpectrumLiveTime != 0.0 && mf.globalBkgCounts != 0.0) {
			errnetcountsrate = Math.sqrt(mf.globalCounts + 
				mf.globalBkgCounts * mf.spectrumLiveTime / mf.bkgSpectrumLiveTime)/
				mf.spectrumLiveTime;
			bkgglobalcountsrate=mf.globalBkgCounts/mf.bkgSpectrumLiveTime;
			errbkgglobalcountsrate=Math.sqrt(mf.globalBkgCounts)/mf.bkgSpectrumLiveTime;
			netcountsrate=globalcountsrate-bkgglobalcountsrate;
		}
		resultS=resultS+
		resources.getString("results.global.gross.countsrate")+Convertor.formatNumber(globalcountsrate,5)+
		this.resources.getString("results.+-.1sigma")+Convertor.formatNumber(errglobalcountsrate,5)+
		"<br>";//"\n";
		resultS=resultS+
		resources.getString("results.global.bkg.countsrate")+Convertor.formatNumber(bkgglobalcountsrate,5)+
		this.resources.getString("results.+-.1sigma")+Convertor.formatNumber(errbkgglobalcountsrate,5)+
		"<br>";//"\n";
		resultS=resultS+
		resources.getString("results.global.net.countsrate")+Convertor.formatNumber(netcountsrate,5)+
		this.resources.getString("results.+-.1sigma")+Convertor.formatNumber(errnetcountsrate,5)+
		"<br><br>";//"\n\n";
		//================
		double nAvogadro = 6.02252E+26;// per kmol!
		double mass = 0.0;
		double massError = 0.0;
		double activQuantity = 0.0;
		double activQuantityError = 0.0;
		double mdaQuantity = 0.0;
		double mdaQuantityError = 0.0;

		double concentration = 0.0;
		String umass = "";

		double w = 0.0;// weight of activity
		double wa = 0.0;// weight x activity
		double w2sa2 = 0.0;// weight2 x Activity_error2
		double v2sa2 = 0.0;// weight2 x MDA_error2
		double v = 0.0;// weight of MDA
		double va = 0.0;// weight x MDA
		// ----------Store the nuclides......
		String[] nuclides = new String[mf.roiV.size()];
		for (int i = 0; i < mf.roiV.size(); i++) {
			GammaRoi gr = mf.roiV.elementAt(i);
			nuclides[i] = gr.getNuclide();
		}
		// ---activFinal will store activities for all nuclides in sample
		Vector<Double> activFinalV = new Vector<Double>();
		Vector<Double> activFinalErrorV = new Vector<Double>();
		Vector<Double> mdaFinalV = new Vector<Double>();
		Vector<Double> mdaFinalErrorV = new Vector<Double>();

		Vector<Double> liveTimeV = new Vector<Double>();
		Vector<String> nuclideFinalV = new Vector<String>();
		Vector<Double> atomicMassFinalV = new Vector<Double>();
		Vector<Double> halfLifeFinalV = new Vector<Double>();
		Vector<String> halfLifeUnitsFinalV = new Vector<String>();
		Vector<String> differenceV = new Vector<String>();

		Vector<Double> activFinalCorrV = new Vector<Double>();
		Vector<Double> activFinalErrorCorrV = new Vector<Double>();
		Vector<Double> mdaFinalCorrV = new Vector<Double>();
		Vector<Double> mdaFinalErrorCorrV = new Vector<Double>();
		// -----------------------------
		for (int i = 0; i < mf.roiV.size(); i++) {
			if (!nuclides[i].equals("")) {// not null
				GammaRoi gr = mf.roiV.elementAt(i);
				// Activity
				if (gr.getActivity_BqError() != 0.0) {
					w = gr.getActivity_Bq() / gr.getActivity_BqError();// weight
																		// of
																		// activity
					wa = gr.getActivity_Bq() * gr.getActivity_Bq()
							/ gr.getActivity_BqError();// weight x activity
				} else {
					w = 0.0;
					wa = 0.0;
				}
				// MDA
				if (gr.getMda_BqError() != 0.0) {
					v = gr.getMda_Bq() / gr.getMda_BqError();// weight of MDA
					va = gr.getMda_Bq() * gr.getMda_Bq() / gr.getMda_BqError();// weight
																				// x
																				// MDA
				} else {
					v = 0.0;
					va = 0.0;
				}

				w2sa2 = gr.getActivity_Bq() * gr.getActivity_Bq();
				v2sa2 = gr.getMda_Bq() * gr.getMda_Bq();

				for (int j = i + 1; j < mf.roiV.size(); j++) {
					//System.out.println("Enter j");
					if (nuclides[i].equals(nuclides[j])) {
						//System.out.println("Enter j nuc found");
						// we have same nuclides
						nuclides[j] = "";// reset to null!
						GammaRoi grj = mf.roiV.elementAt(j);
						if (grj.getActivity_BqError() != 0.0) {
							w = w + grj.getActivity_Bq()
									/ grj.getActivity_BqError();
							wa = wa + grj.getActivity_Bq()
									* grj.getActivity_Bq()
									/ grj.getActivity_BqError();
						}

						if (grj.getMda_BqError() != 0.0) {
							v = v + grj.getMda_Bq() / grj.getMda_BqError();
							va = va + grj.getMda_Bq() * grj.getMda_Bq()
									/ grj.getMda_BqError();
						}

						w2sa2 = w2sa2 + grj.getActivity_Bq()
								* grj.getActivity_Bq();
						v2sa2 = v2sa2 + grj.getMda_Bq() * grj.getMda_Bq();
					}// same nuclide
				}// for j
				if (w != 0.0) {
					double mean = wa / w;
					double stdevOfMean = Math.sqrt(w2sa2) / w;
					double degrees = StatsUtil.evaluateDegreesOfFreedom(
							stdevOfMean, mean);
					if (StatsUtil.failB) degrees=10000.0;//stdev=0!!
					//if (degrees<1.0) degrees=1.0;
					double tfactor = StatsUtil.getStudentFactor(degrees);
	//System.out.println("tfac= "+tfactor+" "+StatsUtil.failB+" df= "+degrees);
					activFinalV.addElement(wa / w);
					double err=tfactor * Math.sqrt(w2sa2) / w;
					if (err>wa / w){
						err=wa / w;//100% error
					}
					activFinalErrorV.addElement(err);//tfactor * Math.sqrt(w2sa2) / w);
				} else {
					activFinalV.addElement(0.0);
					activFinalErrorV.addElement(0.0);
				}

				if (v != 0.0) {
					double mean = va / v;
					double stdevOfMean = Math.sqrt(v2sa2) / v;
//System.out.println(stdevOfMean+" mean= "+mean);					
					double degrees = StatsUtil.evaluateDegreesOfFreedom(
							stdevOfMean, mean);
					if (StatsUtil.failB) degrees=10000.0;//stdev=0!!
//System.out.println("df= "+degrees);					
					double tfactor = StatsUtil.getStudentFactor(degrees);
//System.out.println("tfac= "+tfactor+" "+StatsUtil.failB+" df= "+degrees);
					mdaFinalV.addElement(va / v);
					double err=tfactor * Math.sqrt(v2sa2) / v;
					if (err>va / v){
						err=va / v;//100% error
					}
					mdaFinalErrorV.addElement(err);
				} else {
					mdaFinalV.addElement(0.0);
					mdaFinalErrorV.addElement(0.0);
				}

				liveTimeV.addElement(gr.getLiveTime());
				nuclideFinalV.addElement(gr.getNuclide());
				atomicMassFinalV.addElement(gr.getAtomicMass());
				halfLifeFinalV.addElement(gr.getHalfLife());
				halfLifeUnitsFinalV.addElement(gr.getHalfLifeUnits());

				int n = activFinalV.size();// curent size!
				// accurate MDA is only on each ROI!!! Here degrees of freedom
				// are auto-evaluated!=>
				// can lead to inaccurace results!
//System.out.println("a= "+activFinalV.elementAt(n - 1)+" err "+activFinalErrorV.elementAt(n - 1));
//System.out.println("mda= "+mdaFinalV.elementAt(n - 1)+" err "+mdaFinalErrorV.elementAt(n - 1));

				boolean diffB = StatsUtil.ttest(activFinalV.elementAt(n - 1),
						mdaFinalV.elementAt(n - 1),
						activFinalErrorV.elementAt(n - 1),
						mdaFinalErrorV.elementAt(n - 1));
//System.out.println("diff= "+diffB);				
				if (diffB
						&& activFinalV.elementAt(n - 1) > mdaFinalV
								.elementAt(n - 1)) {
					differenceV.addElement(resources
							.getString("difference.yes"));// "Yes");
				} else {
					differenceV
							.addElement(resources.getString("difference.no"));// "No");
				}

			}// if (!nuclides[i].equals(""))
		}// for i
			// ============now the results
		for (int i = 0; i < activFinalV.size(); i++) {
			// ---------first activity live time correction
			double hl = GammaLibraryFrame.formatHalfLife(
					halfLifeFinalV.elementAt(i),
					halfLifeUnitsFinalV.elementAt(i));
			double corr = 1.0;
			if (hl != 0.0)
				corr = (liveTimeV.elementAt(i) * Math.log(2.0) / hl)
						/ (1.0 - Math.exp(-liveTimeV.elementAt(i)
								* Math.log(2.0) / hl));

			activFinalCorrV.addElement(activFinalV.elementAt(i) * corr);
			activFinalErrorCorrV.addElement(activFinalErrorV.elementAt(i)
					* corr);

			mdaFinalCorrV.addElement(mdaFinalV.elementAt(i) * corr);
			mdaFinalErrorCorrV.addElement(mdaFinalErrorV.elementAt(i) * corr);
			// ---------end correction
			mass = activFinalCorrV.elementAt(i) * hl
					* atomicMassFinalV.elementAt(i)
					/ (nAvogadro * Math.log(2.0));
			massError = activFinalErrorCorrV.elementAt(i) * hl
					* atomicMassFinalV.elementAt(i)
					/ (nAvogadro * Math.log(2.0));

			if (mf.quantity != 0.0) {
				activQuantity = activFinalCorrV.elementAt(i) / mf.quantity;
				activQuantityError = activFinalErrorCorrV.elementAt(i)
						/ mf.quantity;
				
				mdaQuantity = mdaFinalCorrV.elementAt(i) / mf.quantity;
				mdaQuantityError = mdaFinalErrorCorrV.elementAt(i)
						/ mf.quantity;
				
				umass = "[Bq/" + mf.quantityUnit + "]: ";

				if (mf.quantityUnit.equals("kg")) {
					concentration = 100.0 * mass / mf.quantity;
				}
			}
			// ---------------------

			resultS = resultS
					+ this.resources.getString("results.roiNuclide")
					+ nuclideFinalV.elementAt(i)
					+ "; "
					+ this.resources.getString("results.atomicMass")
					+ atomicMassFinalV.elementAt(i)
					+ "; "
					+ this.resources.getString("results.halfLife")
					+ Convertor.formatNumber(halfLifeFinalV.elementAt(i), 2)
					+ " "
					+ halfLifeUnitsFinalV.elementAt(i)
					+ "<br>"//"\n"

					+ this.resources.getString("results.activity.corr")
					+ Convertor.formatNumber(activFinalCorrV.elementAt(i), 3)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(activFinalErrorCorrV.elementAt(i),
							3)
					+ "<br>"//"\n"
					+ this.resources.getString("results.mda.corr")
					+ Convertor.formatNumber(mdaFinalCorrV.elementAt(i), 3)
					+ this.resources.getString("results.+-")
					+ Convertor
							.formatNumber(mdaFinalErrorCorrV.elementAt(i), 3)
					+ "<br>"//"\n" 
					+" <b><font color='blue'>"
					+ this.resources.getString("results.difference")
					+ differenceV.elementAt(i) 
					+ "</font></b> "
					+ "<br><br>"//"\n\n"
					+ this.resources.getString("results.mass")
					+ Convertor.formatNumberScientific(mass)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumberScientific(massError)
					+ "<br>";//"\n";

			if (mf.quantity != 0.0) {				        
						
				resultS = resultS
						+ ( (differenceV.elementAt(i).equals(resources
								.getString("difference.yes")))?
								" <b><font color='green'>":" <b><font color='blue'>")
						+ this.resources.getString("results.conc.activity")
						+ umass + Convertor.formatNumber(activQuantity, 3)
						+ this.resources.getString("results.+-")
						+ Convertor.formatNumber(activQuantityError, 3)
						+ "; (&lt;"+Convertor.formatNumber(
								activQuantity+activQuantityError, 3)+")"
						+ "</font></b> "
						+ "<br>";//"\n";
				
				resultS = resultS
						+ ( (differenceV.elementAt(i).equals(resources
								.getString("difference.yes")))?
								" <b><font color='blue'>":" <b><font color='green'>")
				+ this.resources.getString("results.conc.mda")
				+ umass + Convertor.formatNumber(mdaQuantity, 3)
				+ this.resources.getString("results.+-")
				+ Convertor.formatNumber(mdaQuantityError, 3)
				+ "; (&lt;"+Convertor.formatNumber(
						mdaQuantity, 3)+")"
				+ "</font></b> "
				+ "<br>";//"\n";
				
				if (mf.quantityUnit.equals("kg")) {
					resultS = resultS
							+ this.resources.getString("results.conc")
							+ Convertor.formatNumberScientific(concentration)
							+ "<br>";//"\n";
				}
			}
			// -----------------------------------------------------------
			resultS = resultS // + "\n"
					+ this.resources.getString("results.activity.uncorr")
					+ Convertor.formatNumber(activFinalV.elementAt(i), 3)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(activFinalErrorV.elementAt(i), 3)
					+ "<br>"//"\n"
					+ this.resources.getString("results.mda.uncorr")
					+ Convertor.formatNumber(mdaFinalV.elementAt(i), 3)
					+ this.resources.getString("results.+-")
					+ Convertor.formatNumber(mdaFinalErrorV.elementAt(i), 3)
					+ "<br>";//"\n";

			resultS = resultS + "----------------------------------"
			+"<br>";//"\n";
		}
		// =========================
		// GLOBAL!!!
		boolean haveGlobal = false;
		String globalS = "<br>"//"\n" 
		+ "";
		if (nuclideFinalV.size() == 1) {
			// one nuclide=>compute global activity if
			// nuclide's global efficiency is available!
			try {
				String datas = resources.getString("data.load");
				currentDir = System.getProperty("user.dir");
				file_sep = System.getProperty("file.separator");
				String opens = currentDir + file_sep + datas;
				String gammaDB = resources.getString("main.db");
				String dbName = gammaDB;
				opens = opens + file_sep + dbName;

				// GammaGlobalEfficiencyCalibration
				String s = "select * from "
						+ resources
								.getString("main.db.gammaGlobalEfficiencyTable")
						+ " WHERE NUCLIDE = " + "'"
						+ nuclideFinalV.elementAt(0) + "'";

				Connection con1 = DatabaseAgent.getConnection(opens, "", "");//DBConnection
						//.getDerbyConnection(opens, "", "");
				//DBOperation.select(s, con1);
				DatabaseAgent.select(con1, s);

				double efficiency = 0.0;// number here!!!!
				double efficiencyError = 0.0;
				if (DatabaseAgent.getRowCount() == 1) {//DBOperation.getRowCount() == 1) {
					efficiency = (Double) DatabaseAgent.getValueAt(0, 2);//DBOperation.getValueAt(0, 2);
					efficiencyError = (Double) DatabaseAgent.getValueAt(0, 3);//DBOperation.getValueAt(0, 3);
					haveGlobal = true;
				}
				double efficiencyDegreesOfFreedom = StatsUtil
						.evaluateDegreesOfFreedom(efficiencyError, efficiency);
				if (StatsUtil.failB) {
					efficiencyDegreesOfFreedom = 10000;// error=0=>infinit
				}
				// System.out.println("merge");

				double net = mf.globalCounts;
				double netBkg = mf.globalBkgCounts;
				double t = mf.spectrumLiveTime;// if here, always
												// spectrumLiveTime>0
				double tBkg = mf.bkgSpectrumLiveTime;

				double netError = Math.sqrt(net);
				double netBkgError = Math.sqrt(netBkg);

				double activityGlobal = 0.0;
				double activityGlobalError = 0.0;
				double activityDegreesOfFreedom = 0.0;
				double ldGlobal = 0.0;
				double ldGlobalError = 0.0;
				double ldDegreesOfFreedom = 0.0;
				double mdaGlobal = 0.0;
				double mdaGlobalError = 0.0;
				double mdaDegreesOfFreedom = 0.0;

				if (efficiency != 0.0) {
					if (tBkg != 0.0 && netBkg != 0.0) {
						// we have ambiental BKG!!
						activityGlobal = 100.0 * (net - netBkg * t / tBkg)
								/ (t * efficiency);
						netError = Math.sqrt(net + netBkg * t / tBkg);
						net = net - netBkg * t / tBkg;
						if (activityGlobal < 0.0) {
							activityGlobal = 0.0;// net<0.0!
						}
						activityGlobalError = activityGlobal
								* Math.sqrt(Math.pow(netError / net, 2)
										+ Math.pow(
												efficiencyError / efficiency, 2));
					} else {
						activityGlobal = 100.0 * net / (t * efficiency);
						activityGlobalError = activityGlobal
								* Math.sqrt(Math.pow(netError / net, 2)
										+ Math.pow(
												efficiencyError / efficiency, 2));
					}
				} else {
					activityGlobal = 0.0;
					activityGlobalError = 0.0;
				}

				double netDegreesOfFreedom = StatsUtil
						.evaluateDegreesOfFreedom(netError, net);
				if (StatsUtil.failB) netDegreesOfFreedom=10000.0;//stdev=0!!
				// construct terms for Welch-Satterthwaite formuls (linear
				// combination!)
				double abcompus0 = 0.0;
				if (activityGlobal != 0.0)
					abcompus0 = activityGlobalError / activityGlobal;
				double[] ab0 = new double[2];
				double[] f0 = new double[2];
				f0[0] = efficiencyDegreesOfFreedom;
				f0[1] = netDegreesOfFreedom;
				if (efficiency != 0.0)
					ab0[0] = efficiencyError / efficiency;
				else
					ab0[0] = 0.0;
				ab0[1] = netError / net;
				activityDegreesOfFreedom = StatsUtil
						.getEffectiveDegreesOfFreedom(abcompus0, ab0, f0);
				// here always f[0], f[1]>0 and num>0! so is fail safe!!!
				double tfactorA = StatsUtil
						.getStudentFactor(activityDegreesOfFreedom);
				// if degrees of freedom =0 ...t=1000 MAXITER..fail!!
				activityGlobalError = activityGlobalError * tfactorA;
				if (activityGlobalError>activityGlobal)
					activityGlobalError=activityGlobal;//100% error

				if (tBkg != 0.0 && netBkg != 0.0) {
					// we have ambiental BKG!!!
					if (mf.iMDA == GammaAnalysisFrame.MDA_CALCULATION_PASTERNACK) {
						ldGlobal = 3.29
								* (1.645 + Math.sqrt(2.706025 + 2.0 * netBkg))
								/ tBkg;
						ldGlobalError = netBkgError * 2.0 * 1.645
								/ ((Math.sqrt(2.706025 + 2.0 * netBkg)) * tBkg);
						ldDegreesOfFreedom = StatsUtil
								.evaluateDegreesOfFreedom(ldGlobalError,
										ldGlobal);
						if (StatsUtil.failB) ldDegreesOfFreedom=10000.0;//stdev=0!!
					} else if (mf.iMDA == GammaAnalysisFrame.MDA_CALCULATION_CURIE) {
						ldGlobal = (2.71 + 4.65 * Math.sqrt(netBkg)) / tBkg;
						ldGlobalError = netBkgError * 4.65
								* Math.sqrt(1.0 / (4.0 * netBkg)) / tBkg;
						ldDegreesOfFreedom = StatsUtil
								.evaluateDegreesOfFreedom(ldGlobalError,
										ldGlobal);
						if (StatsUtil.failB) ldDegreesOfFreedom=10000.0;//stdev=0!!
					} else if (mf.iMDA == GammaAnalysisFrame.MDA_CALCULATION_DEFAULT) {
						ldGlobal = (2.706025 + 3.29 * Math.sqrt(netBkg)) / tBkg;
						ldGlobalError = netBkgError * 1.645
								/ (Math.sqrt(netBkg) * tBkg);
						ldDegreesOfFreedom = StatsUtil
								.evaluateDegreesOfFreedom(ldGlobalError,
										ldGlobal);
						if (StatsUtil.failB) ldDegreesOfFreedom=10000.0;//stdev=0!!
					}

					if (efficiency != 0.0) {
						mdaGlobal = 100.0 * ldGlobal / (efficiency);
						mdaGlobalError = mdaGlobal
								* Math.sqrt(Math.pow(ldGlobalError / ldGlobal,
										2)
										+ Math.pow(
												efficiencyError / efficiency, 2));
					} else {
						mdaGlobal = 0.0;
						mdaGlobalError = 0.0;
					}
					String diffS = "";
					if (efficiency != 0.0) {
						// construct terms for Welch-Satterthwaite formuls
						// (linear combination!)
						double abcompus = mdaGlobalError / mdaGlobal;
						double[] ab = new double[2];
						double[] f = new double[2];
						f[0] = efficiencyDegreesOfFreedom;
						f[1] = ldDegreesOfFreedom;
						ab[0] = efficiencyError / efficiency;
						ab[1] = ldGlobalError / ldGlobal;
						mdaDegreesOfFreedom = StatsUtil
								.getEffectiveDegreesOfFreedom(abcompus, ab, f);

						double tfactor = StatsUtil
								.getStudentFactor(mdaDegreesOfFreedom);
						mdaGlobalError = mdaGlobalError * tfactor;
						// here always f[0], f[1]>0 and num>0! so is fail
						// safe!!!
						if (mdaGlobalError>mdaGlobal)
							mdaGlobalError=mdaGlobal;//100% error
						// here stdev=stdevOfMean but we choose
						// stdevofMean due to degreesOfFreedom evaluation!!!!

						boolean diffB = StatsUtil.ttest_default_unc(
								activityGlobal, mdaGlobal, activityGlobalError,
								mdaGlobalError, activityDegreesOfFreedom,
								mdaDegreesOfFreedom);

						if (diffB && (activityGlobal > mdaGlobal)) {
							diffS = resources.getString("difference.yes");// "Yes");
						} else {
							diffS = resources.getString("difference.no");// "No");
						}
					} else {
						diffS = resources.getString("difference.no");
					}
					globalS = globalS
							+ this.resources
									.getString("results.activity.global")
							+ Convertor.formatNumber(activityGlobal, 3)
							+ this.resources.getString("results.+-")
							+ Convertor.formatNumber(activityGlobalError, 3)
							+ this.resources
									.getString("results.equivalent.global")
							+ nuclideFinalV.elementAt(0)
							+ "<br>"//"\n"
							+ this.resources.getString("results.mda.global")
							+ Convertor.formatNumber(mdaGlobal, 3)
							+ this.resources.getString("results.+-")
							+ Convertor.formatNumber(mdaGlobalError, 3)
							+ this.resources
									.getString("results.equivalent.global")
							+ nuclideFinalV.elementAt(0) + "\n"
							+ this.resources.getString("results.difference")
							+ diffS 
							+"<br>";//+ "\n";

				} else {
					globalS = globalS
							+ this.resources
									.getString("results.activity.global")
							+ Convertor.formatNumber(activityGlobal, 3)
							+ this.resources.getString("results.+-")
							+ Convertor.formatNumber(activityGlobalError, 3)
							+ this.resources
									.getString("results.equivalent.global")
							+ nuclideFinalV.elementAt(0)
							+ "<br>";//"\n";
				}
				// ------------
				if (con1 != null)
					con1.close();
			} catch (Exception exc) {
				exc.printStackTrace();
			}

		}
		// end GLOBAL
		if (haveGlobal)
			resultS = resultS + globalS;
		return resultS;
	}

	/**
	 * All actions are set here
	 */
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		command = arg0.getActionCommand();
		if (command.equals(PRINT_COMMAND)) {
			printReport();
		}
	}

	/**
	 * Print the report (save as PDF file)
	 */
	private void printReport() {
		String FILESEPARATOR = System.getProperty("file.separator");
		String currentDir = System.getProperty("user.dir");
		File infile = null;

		String ext = resources.getString("file.extension.html");
		String pct = ".";
		String description = resources.getString("file.description.html");
		ExampleFileFilter eff = new ExampleFileFilter(ext, description);

		String myDir = currentDir + FILESEPARATOR;//
		// File select
		JFileChooser chooser = new JFileChooser(myDir);
		chooser.addChoosableFileFilter(eff);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		int returnVal = chooser.showSaveDialog(this);// parent=this frame
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			infile = chooser.getSelectedFile();
			outFilename = chooser.getSelectedFile().toString();

			int fl = outFilename.length();
			String test = outFilename.substring(fl - 5);// exstension lookup!!
			String ctest = pct + ext;
			if (test.compareTo(ctest) != 0)
				outFilename = chooser.getSelectedFile().toString() + pct + ext;

			if (infile.exists()) {
				String title = resources.getString("dialog.overwrite.title");
				String message = resources
						.getString("dialog.overwrite.message");

				Object[] options = (Object[]) resources
						.getObject("dialog.overwrite.buttons");
				int result = JOptionPane
						.showOptionDialog(this, message, title,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE, null, options,
								options[0]);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}

			}

			//new GammaReport(this);
			performPrintReport();
			statusL.setText(resources.getString("status.save") + outFilename);
		} else {
			return;
		}
	}
	
	/**
	 * Actual pdf renderer is called here. Called by printReport.
	 */
	public void performPrintReport(){
		/*PDDocument doc = new PDDocument();
		PDFRenderer renderer = new PDFRenderer(doc);
		try{
			renderer.setTitle(resources.getString("pdf.content.title"));
			renderer.setSubTitle(
					resources.getString("pdf.content.subtitle")+
					resources.getString("pdf.metadata.author")+ ", "+
							new Date());
						
			//String str = " \n" + textArea.getText();
			String str = textArea.getText();
		
			//renderer.renderTextHavingNewLine(str);//works!!!!!!!!!!!!!!!!
			renderer.renderTextEnhanced(str);
			//renderer.
			//textArea.
			
			renderer.addPageNumber();
			renderer.close();		
			doc.save(new File(outFilename));
			doc.close();
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		
		
		//iTextonly---------WORKS!!!!!!!!!!!!!!!
		/*String str = textArea.getText();
		final Document document = new Document();
		File file = new File(outFilename);
		try {
			DocWriter docWriter = PdfWriter.getInstance(document, 
					new FileOutputStream(file));
			document.open();
			HTMLWorker worker = new HTMLWorker(document);
			worker.parse(new StringReader(str));
			document.close();
			docWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//HTML HAS BUILD IN TO PDF IN WANT: NO CONVERT JUST COPY !!!
		String htmlTemp = "temp.html";
		String currentDir = System.getProperty("user.dir");
		String file_sep = System.getProperty("file.separator");
		String filename = currentDir + file_sep + htmlTemp; 
		
		//we can also do copy, this way...more advanced:
		try {
			FileOperation.copyFile(filename, outFilename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*int i = 0;
		StringBuffer desc = new StringBuffer();
		System.out.println(filename);
		try {
			//first read
			FileInputStream in = new FileInputStream(filename);
			while ((i = in.read()) != -1) {
				desc.append((char) i);			
			}
			in.close();
			//then write:
			FileWriter sigfos = new FileWriter(outFilename);
			sigfos.write(desc.toString());
			sigfos.close();			
			
		} catch (Exception ex) {

		}
		// WORKS!
		*/
	}
}
