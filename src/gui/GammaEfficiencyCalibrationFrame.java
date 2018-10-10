package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import danfulea.math.Convertor;
import danfulea.math.Sort;
import danfulea.math.numerical.Function;
import danfulea.math.numerical.ModelingData;
import danfulea.phys.GammaRoi;
import danfulea.phys.PhysUtilities;
import danfulea.utils.FrameUtilities;

/**
 * Class for efficiency calibration. <br>
 * 
 * @author Dan Fulea, 09 Jul. 2011
 * 
 */
@SuppressWarnings("serial")
public class GammaEfficiencyCalibrationFrame extends JFrame implements
		ActionListener, Function {
	private String command;
	public boolean changeCalibration = false;
	private static final String CALIBRATE_COMMAND = "CALIBRATE";
	private static final String ADD_COMMAND = "ADD";
	private static final String INITIALIZE_COMMAND = "INITIALIZE";
	private static final String SET_COMMAND = "SET";
	private static final String REMOVE_COMMAND = "REMOVE";
	private static final String TEST_COMMAND = "TEST";
	private static final String SAVE_COMMAND = "SAVE";
	private GammaAnalysisFrame mf;
	protected Color bkgColor;
	protected Color foreColor;
	protected Color textAreaBkgColor;
	protected Color textAreaForeColor;
	private final Dimension PREFERRED_SIZE = new Dimension(900, 700);
	private final Dimension tableDimension = new Dimension(700, 200);
	private final Dimension smalltableDimension = new Dimension(300, 100);
	private final Dimension chartDimension = new Dimension(500, 250);
	private final Dimension textAreaDimension = new Dimension(300, 100);
	private final Dimension sizeCb = new Dimension(50, 21);
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;

	private JTable nucTable, effTable;
	private String[][] nucMatrix;
	private String[] nucColumnNames;

	public static String sday = "";
	public static String smonth = "";
	public static String syear = "";

	@SuppressWarnings("rawtypes")
	private JComboBox day0Cb, dayCb, month0Cb, monthCb;
	private JTextField year0Tf = new JTextField(5);
	private JTextField yearTf = new JTextField(5);
	private JTextField activityTf = new JTextField(5);
	private JTextField errActivityTf = new JTextField(3);
	private JTextField crossoverEnergyTf = new JTextField(5);
	private JTextField testTf = new JTextField(5);
	@SuppressWarnings("rawtypes")
	private JComboBox poly1Cb, poly2Cb;
	private JTextArea textArea;
	private ChartPanel cpEff = null;
	private JPanel suportMainTabP = new JPanel(new BorderLayout());
	private Vector<String> nuclideS;
	private Vector<Double> energyD;
	private Vector<Double> yieldD;
	private Vector<Double> netRateD;
	private Vector<Double> netRateErrorD;
	private Vector<Double> effProc_calcD;
	private Vector<Double> effProc_calcErrorD;
	private String[][] effData;
	protected boolean canSaveB = false;
	protected boolean canSaveGlobalB = false;
	protected double efficiencyGlobal = 0.0;
	protected double efficiencyGlobalError = 0.0;
	protected String globalNuclide = "";
	private double crossoverEnergy = 0.0;
	protected double overAllEffDeviations1;
	protected double overAllEffDeviations2;
	protected double eff_p1_a4 = 0.0;
	protected double eff_p1_a3 = 0.0;
	protected double eff_p1_a2 = 0.0;
	protected double eff_p1_a1 = 0.0;
	protected double eff_p1_a0 = 0.0;
	protected double eff_p2_a4 = 0.0;
	protected double eff_p2_a3 = 0.0;
	protected double eff_p2_a2 = 0.0;
	protected double eff_p2_a1 = 0.0;
	protected double eff_p2_a0 = 0.0;
	protected static double eff_p1_a4_current = 0.0;
	protected static double eff_p1_a3_current = 0.0;
	protected static double eff_p1_a2_current = 0.0;
	protected static double eff_p1_a1_current = 0.0;
	protected static double eff_p1_a0_current = 0.0;
	protected static double eff_p2_a4_current = 0.0;
	protected static double eff_p2_a3_current = 0.0;
	protected static double eff_p2_a2_current = 0.0;
	protected static double eff_p2_a1_current = 0.0;
	protected static double eff_p2_a0_current = 0.0;
	protected static double eff_crossoverEnergy_current = 0.0;
	protected double eff_crossoverEnergy = 0.0;
	protected double eff_overallProcentualError = 0.0;

	/**
	 * Constructor. Before creating object of this class, the roi assignment to nuclides must 
	 * be done via RoiSet.
	 * @param mf GammaAnalysisFrame object
	 * @param nucMatrix the nuclide matrix containing nuclides id and name.
	 * These nuclides are part of known standard source. 
	 * @param nucColumnNames the nuclide table column names
	 */
	public GammaEfficiencyCalibrationFrame(GammaAnalysisFrame mf,
			String[][] nucMatrix, String[] nucColumnNames) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("Efficiency.NAME"));
		changeCalibration = false;
		this.mf = mf;
		this.nucMatrix = nucMatrix;
		this.nucColumnNames = nucColumnNames;
		this.bkgColor = GammaAnalysisFrame.bkgColor;
		this.foreColor = GammaAnalysisFrame.foreColor;
		this.textAreaBkgColor = GammaAnalysisFrame.textAreaBkgColor;
		this.textAreaForeColor = GammaAnalysisFrame.textAreaForeColor;
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

		activityTf.requestFocusInWindow();
		ModelingData.func = this;// pass the function!!
		canSaveB = false;
		canSaveGlobalB = false;
	}

	/**
	 * Exit method
	 */
	private void attemptExit() {
		if (changeCalibration) {
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
		// build a customized DefaultTableModel:
		String[] effColumnNames = (String[]) resources
				.getObject("eff.calibration.eff.columns");
		DefaultTableModel dfm0 = new DefaultTableModel(null, effColumnNames) {

			// Do not allow to alter!
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		// build a customized JTable
		effTable = new JTable(dfm0) {

			// Text CENTER aligned!!
			public TableCellRenderer getCellRenderer(int row, int col) {
				TableCellRenderer renderer = super.getCellRenderer(row, col);

				((JLabel) renderer)
						.setHorizontalAlignment(SwingConstants.CENTER);
				return renderer;
			}
		};
		// some other customization:
		ListSelectionModel rowSM0 = effTable.getSelectionModel();
		rowSM0.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// single

		// selection
		if (effTable.getRowCount() != 0)
			effTable.setRowSelectionInterval(0, 0);// first row!
		// ------------

		// build a customized DefaultTableModel:
		DefaultTableModel dfm = new DefaultTableModel(nucMatrix, nucColumnNames) {

			// Do not allow to alter!
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		// build a customized JTable
		nucTable = new JTable(dfm) {

			// Text CENTER aligned!!
			public TableCellRenderer getCellRenderer(int row, int col) {
				TableCellRenderer renderer = super.getCellRenderer(row, col);

				((JLabel) renderer)
						.setHorizontalAlignment(SwingConstants.CENTER);
				return renderer;
			}
		};
		// some other customization:
		ListSelectionModel rowSM = nucTable.getSelectionModel();
		rowSM.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// single

		// selection
		if (nucTable.getRowCount() != 0)
			nucTable.setRowSelectionInterval(0, 0);// first row!
		// ------------
		JPanel tabP = new JPanel(new BorderLayout());
		tabP.add(new JScrollPane(nucTable));
		tabP.setBackground(GammaAnalysisFrame.bkgColor);
		tabP.setPreferredSize(smalltableDimension);

		String[] sarray = new String[31];
		for (int i = 1; i <= 31; i++) {
			if (i < 10)
				sarray[i - 1] = "0" + i;
			else
				sarray[i - 1] = Convertor.intToString(i);
		}
		day0Cb = new JComboBox(sarray);
		day0Cb.setMaximumRowCount(5);
		day0Cb.setPreferredSize(sizeCb);

		dayCb = new JComboBox(sarray);
		dayCb.setMaximumRowCount(5);
		dayCb.setPreferredSize(sizeCb);

		dayCb.setSelectedItem(sday);

		sarray = new String[12];
		for (int i = 1; i <= 12; i++) {
			if (i < 10)
				sarray[i - 1] = "0" + i;
			else
				sarray[i - 1] = Convertor.intToString(i);
		}
		month0Cb = new JComboBox(sarray);
		month0Cb.setMaximumRowCount(5);
		month0Cb.setPreferredSize(sizeCb);

		monthCb = new JComboBox(sarray);
		monthCb.setMaximumRowCount(5);
		monthCb.setPreferredSize(sizeCb);

		monthCb.setSelectedItem(smonth);
		yearTf.setText(syear);

		sarray = (String[]) resources.getObject("eff.polynomes");
		poly1Cb = new JComboBox(sarray);
		poly1Cb.setMaximumRowCount(5);
		poly1Cb.setPreferredSize(sizeCb);
		String s = sarray[2];// default=3
		poly1Cb.setSelectedItem((Object) s);
		poly2Cb = new JComboBox(sarray);
		poly2Cb.setMaximumRowCount(5);
		poly2Cb.setPreferredSize(sizeCb);
		poly2Cb.setSelectedItem((Object) s);

		textArea = new JTextArea();
		textArea.setCaretPosition(0);
		textArea.setEditable(false);
		textArea.setText("");
		textArea.setWrapStyleWord(true);
		textArea.setBackground(GammaAnalysisFrame.textAreaBkgColor);
		textArea.setForeground(GammaAnalysisFrame.textAreaForeColor);

		JFreeChart effChart = createEffEmptyChart();
		cpEff = new ChartPanel(effChart, false, true, true, false, true);
		cpEff.setMouseWheelEnabled(true);// mouse wheel zooming!
		cpEff.setPreferredSize(chartDimension);
		JPanel p1P = new JPanel();
		p1P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p1P.add(cpEff);
		p1P.setBackground(GammaAnalysisFrame.bkgColor);

		Character mnemonic = null;
		JButton button = null;
		JLabel label = null;
		String buttonName = "";
		String buttonToolTip = "";
		String buttonIconName = "";

		JPanel p2P = new JPanel();
		p2P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p2P.setBackground(GammaAnalysisFrame.bkgColor);
		buttonName = resources.getString("eff.calib.initialize.button");
		buttonToolTip = resources
				.getString("eff.calib.initialize.button.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, INITIALIZE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.calib.initialize.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p2P.add(button);

		JPanel tP = new JPanel();
		BoxLayout bl0 = new BoxLayout(tP, BoxLayout.Y_AXIS);
		tP.setLayout(bl0);
		tP.setBackground(GammaAnalysisFrame.bkgColor);
		tP.add(tabP);
		tP.add(Box.createRigidArea(new Dimension(0, 8)));// some space
		tP.add(p2P);

		JPanel p3P = new JPanel();
		p3P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p3P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.activityLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p3P.add(label);
		p3P.add(activityTf);

		JPanel p31P = new JPanel();
		p31P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p31P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.errorLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p31P.add(label);
		p31P.add(errActivityTf);
		errActivityTf.setText("5");

		JPanel p32P = new JPanel();
		p32P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p32P.setBackground(GammaAnalysisFrame.bkgColor);
		buttonName = resources.getString("eff.calib.add.button");
		buttonToolTip = resources.getString("eff.calib.add.button.toolTip");
		buttonIconName = resources.getString("img.insert");
		button = FrameUtilities.makeButton(buttonIconName, ADD_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.calib.add.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		p32P.add(button);

		JPanel abox = new JPanel();
		BoxLayout blabox = new BoxLayout(abox, BoxLayout.Y_AXIS);
		abox.setLayout(blabox);
		abox.setBackground(GammaAnalysisFrame.bkgColor);
		abox.add(p3P);
		abox.add(p31P);
		abox.add(p32P);

		JPanel p4P = new JPanel();
		p4P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p4P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.dayLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p4P.add(label);
		p4P.add(day0Cb);

		JPanel p41P = new JPanel();
		p41P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p41P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.monthLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p41P.add(label);
		p41P.add(month0Cb);

		JPanel p42P = new JPanel();
		p42P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p42P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.yearLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p42P.add(label);
		p42P.add(year0Tf);

		JPanel adatebox = new JPanel();
		BoxLayout bladatebox = new BoxLayout(adatebox, BoxLayout.Y_AXIS);
		adatebox.setLayout(bladatebox);
		adatebox.setBackground(GammaAnalysisFrame.bkgColor);
		adatebox.add(p4P);
		adatebox.add(p41P);
		adatebox.add(p42P);
		adatebox.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("eff.calib.activityDate.border"),
				GammaAnalysisFrame.foreColor));

		JPanel p5P = new JPanel();
		p5P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p5P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.dayLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p5P.add(label);
		p5P.add(dayCb);

		JPanel p51P = new JPanel();
		p51P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p51P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.monthLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p51P.add(label);
		p51P.add(monthCb);

		JPanel p52P = new JPanel();
		p52P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		p52P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.yearLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		p52P.add(label);
		p52P.add(yearTf);

		JPanel mdatebox = new JPanel();
		BoxLayout blmdatebox = new BoxLayout(mdatebox, BoxLayout.Y_AXIS);
		mdatebox.setLayout(blmdatebox);
		mdatebox.setBackground(GammaAnalysisFrame.bkgColor);
		mdatebox.add(p5P);
		mdatebox.add(p51P);
		mdatebox.add(p52P);
		mdatebox.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("eff.calib.measurementDate.border"),
				GammaAnalysisFrame.foreColor));

		JPanel northBase = new JPanel();
		northBase.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		northBase.setBackground(GammaAnalysisFrame.bkgColor);
		northBase.add(tP);
		northBase.add(abox);
		northBase.add(adatebox);
		northBase.add(mdatebox);
		northBase.setBorder(FrameUtilities.getGroupBoxBorder(""));

		JPanel box1 = new JPanel();
		BoxLayout blbox1 = new BoxLayout(box1, BoxLayout.Y_AXIS);
		box1.setLayout(blbox1);
		box1.setBackground(GammaAnalysisFrame.bkgColor);
		buttonName = resources.getString("eff.calib.set.button");
		buttonToolTip = resources.getString("eff.calib.set.button.toolTip");
		buttonIconName = resources.getString("img.accept");
		button = FrameUtilities.makeButton(buttonIconName, SET_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.calib.set.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		box1.add(button);
		box1.add(Box.createRigidArea(new Dimension(0, 18)));// some space
		buttonName = resources.getString("eff.calib.remove.button");
		buttonToolTip = resources.getString("eff.calib.remove.button.toolTip");
		buttonIconName = resources.getString("img.delete");
		button = FrameUtilities.makeButton(buttonIconName, REMOVE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.calib.remove.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		box1.add(button);

		suportMainTabP.setPreferredSize(tableDimension);
		suportMainTabP.add(new JScrollPane(effTable));
		suportMainTabP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel north = new JPanel();
		north.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		north.setBackground(GammaAnalysisFrame.bkgColor);
		north.add(suportMainTabP);
		north.add(box1);

		JPanel c1 = new JPanel();
		c1.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		c1.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.crossoverEnergyLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		c1.add(label);
		c1.add(crossoverEnergyTf);
		crossoverEnergyTf.setText("0");
		label = new JLabel(resources.getString("eff.calib.poly1Lb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		c1.add(label);
		c1.add(poly1Cb);
		label = new JLabel(resources.getString("eff.calib.poly2Lb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		c1.add(label);
		c1.add(poly2Cb);
		buttonName = resources.getString("eff.calib.calibrate.button");
		buttonToolTip = resources
				.getString("eff.calib.calibrate.button.toolTip");
		buttonIconName = resources.getString("img.set");
		button = FrameUtilities.makeButton(buttonIconName, CALIBRATE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.calib.calibrate.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		c1.add(button);

		JPanel resultP = new JPanel(new BorderLayout());
		resultP.setPreferredSize(textAreaDimension);
		resultP.add(new JScrollPane(textArea), BorderLayout.CENTER);
		resultP.setBackground(GammaAnalysisFrame.bkgColor);

		JPanel t1P = new JPanel();
		t1P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		t1P.setBackground(GammaAnalysisFrame.bkgColor);
		label = new JLabel(resources.getString("eff.calib.testLb"));
		label.setForeground(GammaAnalysisFrame.foreColor);
		t1P.add(label);
		t1P.add(testTf);
		JPanel t11P = new JPanel();
		t11P.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		t11P.setBackground(GammaAnalysisFrame.bkgColor);
		buttonName = resources.getString("eff.calib.test.button");
		buttonToolTip = resources.getString("eff.calib.test.button.toolTip");
		buttonIconName = null;
		button = FrameUtilities.makeButton(buttonIconName, TEST_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.calib.test.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		t11P.add(button);
		buttonName = resources.getString("eff.calib.save.button");
		buttonToolTip = resources.getString("eff.calib.save.button.toolTip");
		buttonIconName = resources.getString("img.save.database");
		button = FrameUtilities.makeButton(buttonIconName, SAVE_COMMAND,
				buttonToolTip, buttonName, this, this);
		mnemonic = (Character) resources
				.getObject("eff.calib.save.button.mnemonic");
		button.setMnemonic(mnemonic.charValue());
		t11P.add(button);
		JPanel ttP = new JPanel();
		BoxLayout bltt0 = new BoxLayout(ttP, BoxLayout.Y_AXIS);
		ttP.setLayout(bltt0);
		ttP.setBackground(GammaAnalysisFrame.bkgColor);
		ttP.add(t1P);
		ttP.add(t11P);
		ttP.setBorder(FrameUtilities.getGroupBoxBorder(
				resources.getString("eff.calib.test.save.border"), GammaAnalysisFrame.foreColor));

		JPanel cc2 = new JPanel();
		BoxLayout blcc2 = new BoxLayout(cc2, BoxLayout.Y_AXIS);
		cc2.setLayout(blcc2);
		cc2.setBackground(GammaAnalysisFrame.bkgColor);
		cc2.add(resultP);
		cc2.add(ttP);

		JPanel c2 = new JPanel();
		c2.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 2));
		c2.setBackground(GammaAnalysisFrame.bkgColor);
		c2.add(p1P);
		c2.add(cc2);

		JPanel mainP = new JPanel();
		BoxLayout bl = new BoxLayout(mainP, BoxLayout.Y_AXIS);
		mainP.setLayout(bl);
		mainP.setBackground(GammaAnalysisFrame.bkgColor);
		mainP.add(northBase);
		mainP.add(north);
		mainP.add(c1);
		mainP.add(c2);

		JPanel content = new JPanel(new BorderLayout());
		content.add(mainP);
		setContentPane(new JScrollPane(content));
		pack();
	}

	// empty not null chart
	/**
	 * Create an empty chart
	 * @return the result
	 */
	private JFreeChart createEffEmptyChart() {
		// construct an empty XY series
		XYSeries series = new XYSeries(
				resources.getString("eff.calib.chart.data.NAME"));
		XYSeries fitseries = new XYSeries(
				resources.getString("eff.calib.chart.fit.NAME"));
		// empty series!
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection fitdata = new XYSeriesCollection(fitseries);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("eff.calib.chart.energy.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("eff.calib.chart.energy.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);

		plot.setDataset(1, fitdata);
		XYItemRenderer renderer0 = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		renderer0.setSeriesPaint(0, Color.RED);
		plot.setRenderer(1, renderer0);
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("eff.calib.chart.energy.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}

	/**
	 * Internally used by calibrate method. It updates the chart with point data series and fit series.
	 * @param x1 x1 
	 * @param y01 y01
	 * @param x2 x2
	 * @param y02 y02
	 */
	private void updateEffCalibrationGraph(double[] x1, double[] y01,
			double[] x2, double[] y02) {
		cpEff.removeAll();

		int n1 = x1.length;
		int n2 = x2.length;

		XYSeries series = new XYSeries(
				resources.getString("eff.calib.chart.data.NAME"));
		XYSeries fitseries = new XYSeries(
				resources.getString("eff.calib.chart.fit.NAME"));

		for (int j = 0; j < n1; j++) {
			series.add(x1[j], y01[j]);
		}
		for (int j = 0; j < n2; j++) {
			series.add(x2[j], y02[j]);
		}
		double x0 = 0.0;

		double dlta = 1.0 / 10.0;
		int i = 0;
		while (true) {
			i++;
			double xx = x0 + (i - 1) * dlta;
			double yy = F(xx);// function!!
			fitseries.add(xx, yy);
			if (x0 + (i - 1) * dlta > x2[x2.length - 1]) {// x2 always is
															// bigger!
				break;
			}
		}
		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection fitdata = new XYSeriesCollection(fitseries);

		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("eff.calib.chart.energy.x"));
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(
				resources.getString("eff.calib.chart.energy.y"));

		// Renderer
		XYItemRenderer renderer = new StandardXYItemRenderer(
				StandardXYItemRenderer.SHAPES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.BLUE);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());

		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setDataset(0, data);
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		plot.setRenderer(0, renderer);

		plot.setDataset(1, fitdata);
		XYItemRenderer renderer0 = new StandardXYItemRenderer(
				StandardXYItemRenderer.LINES);
		renderer0.setSeriesPaint(0, Color.RED);
		plot.setRenderer(1, renderer0);
		// ///////////////////////////////////////////////
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);

		JFreeChart chart = new JFreeChart(
				resources.getString("eff.calib.chart.energy.NAME"),
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		cpEff.setChart(chart);
	}

	/**
	 * Setting up actions!
	 */
	public void actionPerformed(ActionEvent arg0) {
		command = arg0.getActionCommand();
		if (command.equals(CALIBRATE_COMMAND)) {
			calibrate();
		} else if (command.equals(ADD_COMMAND)) {
			add();
		} else if (command.equals(INITIALIZE_COMMAND)) {
			initialize();
		} else if (command.equals(SET_COMMAND)) {
			set();
		} else if (command.equals(REMOVE_COMMAND)) {
			remove();
		} else if (command.equals(TEST_COMMAND)) {
			test();
		} else if (command.equals(SAVE_COMMAND)) {
			save();
		}
	}

	/**
	 * Perform efficiency calibration.
	 */
	private void calibrate() {
		crossoverEnergy = 0.0;
		boolean neg = false;
		try {
			crossoverEnergy = Convertor.stringToDouble(crossoverEnergyTf
					.getText());
			if (crossoverEnergy < 0.0) {
				neg = true;
			}
		} catch (Exception exc) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			canSaveB = false;

			return;
		}
		if (neg) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			canSaveB = false;

			return;
		}

		int ndat = 0;
		int ndat1 = 0;
		int ndat2 = 0;
		Vector<Double> en1V = new Vector<Double>();
		Vector<Double> eff_set1V = new Vector<Double>();
		Vector<Double> eff_setError1V = new Vector<Double>();
		Vector<Double> en2V = new Vector<Double>();
		Vector<Double> eff_set2V = new Vector<Double>();
		Vector<Double> eff_setError2V = new Vector<Double>();
		Vector<Integer> rowIndex1V = new Vector<Integer>();
		Vector<Integer> rowIndex2V = new Vector<Integer>();

		for (int i = 0; i < effTable.getRowCount(); i++) {
			String s = (String) effTable.getValueAt(i, 10);// diff
			if (s.equals(resources.getString("difference.yes"))) {

				double denergy = Convertor.stringToDouble(effData[i][2]);
				if (denergy < crossoverEnergy) {
					en1V.addElement(denergy);// energy
					ndat1++;
				} else {
					en2V.addElement(denergy);// energy
					ndat2++;
				}

				double d = Convertor.stringToDouble(effData[i][6]);
				if (denergy < crossoverEnergy) {
					eff_set1V.addElement(d);
				} else {
					eff_set2V.addElement(d);
				}

				d = Convertor.stringToDouble(effData[i][7]);
				if (denergy < crossoverEnergy) {
					eff_setError1V.addElement(d);
				} else {
					eff_setError2V.addElement(d);
				}

				if (denergy < crossoverEnergy) {
					rowIndex1V.addElement(i);
				} else {
					rowIndex2V.addElement(i);
				}
				ndat++;
			}
		}

		/*if (ndat < 2) {
			String title = resources.getString("number.error.title5");
			String message = resources.getString("number.error5");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			canSaveB = false;

			return;
		}*/
		/////////////
		if (ndat < 1) {
			String title = resources.getString("number.error.title5");
			String message = resources.getString("number.error55");//to modify message, no point!!
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			canSaveB = false;

			return;
		}
		//////////////
		if (ndat ==1) {//1 point calibration. special case
			
			double[] sen=new double[1];//s from special
			double[] seff=new double[1];
			
			sen[0] = Convertor.stringToDouble(effData[0][2]);
			seff[0] = Convertor.stringToDouble(effData[0][6]);
			
			eff_crossoverEnergy = 0.0;//crossoverEnergy;
			eff_p1_a4 = 0.0;
			eff_p1_a3 = 0.0;
			eff_p1_a2 = 0.0;
			eff_p1_a1 = 0.0;
			eff_p1_a0 = 0.0;
			eff_p2_a4 = 0.0;
			eff_p2_a3 = 0.0;
			eff_p2_a2 = 0.0;
			eff_p2_a1 = Math.log(seff[0])/Math.log(sen[0]);
			eff_p2_a0 = 0.0;
			
			//the following is redundant, copy-pasted and modified from below
			double effcalib = seff[0];//Math.exp(eff_p2_a1*Math.log(sen[1]));
			String s = Convertor.formatNumber(effcalib, 2);
			effTable.setValueAt(s, 0, 8);//effTable.setValueAt(s, i, 8);
			
			double deff = seff[0];//Convertor.stringToDouble(effData[0][6]);
			double diffD = 100.0 * Math.abs(effcalib - deff) / deff;
			s = Convertor.formatNumber(diffD, 2);
			effTable.setValueAt(s, 0, 9);//effTable.setValueAt(s, i, 9);
			/////////////
			
			overAllEffDeviations1 = 0.0;
			overAllEffDeviations2 = 0.0;
			
			eff_overallProcentualError = 0.0;
			
			double[] dummyen=new double[0];
			double[] dummyeff=new double[0];
			
			energyToEff2 = new double[2];
			energyToEff2[0]=0.0;
			energyToEff2[1]=eff_p2_a1;//this is necessary for graph.
			updateEffCalibrationGraph(dummyen, dummyeff, sen, seff);
			
			canSaveB = true;
			
			// NOW DISPLAY RESULTS IN TEXT AREA:
			textArea.selectAll();
			textArea.replaceSelection("");

			if (canSaveGlobalB) {
				s = resources.getString("eff.calib.textArea.global");
				textArea.append(s + "\n");//
				s = "" + efficiencyGlobal + resources.getString("results.+-")
						+ efficiencyGlobalError;
				textArea.append(s + "\n");
			}

			s = resources.getString("eff.calib.textArea.crossover");
			textArea.append(s + "\n");
			s = "" + eff_crossoverEnergy;
			textArea.append(s + "\n");
			s = resources.getString("eff.calib.textArea.effCal1");
			s = eff_p1_a4 + "; " + eff_p1_a3 + "; " + eff_p1_a2 + "; " + eff_p1_a1
					+ "; " + eff_p1_a0;
			textArea.append(s + "\n");
			s = resources.getString("eff.calib.textArea.effCal2");
			s = eff_p2_a4 + "; " + eff_p2_a3 + "; " + eff_p2_a2 + "; " + eff_p2_a1
					+ "; " + eff_p2_a0;
			textArea.append(s + "\n");
			s = resources.getString("eff.calib.textArea.effDiff");
			textArea.append(s + "\n");
			s = "" + eff_overallProcentualError;
			textArea.append(s + "\n");
			return;
		}
		///////////////		

		if (ndat1 != 0 && ndat2 != 0)
			if (ndat1 < 2 || ndat2 < 2) {
				String title = resources.getString("number.error.title6");
				String message = resources.getString("number.error6");
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.ERROR_MESSAGE);

				canSaveB = false;

				return;
			}

		double[] energyKev1 = new double[ndat1];// xxx
		double[] effCalc1 = new double[ndat1];// yyy
		double[] effCalcError1 = new double[ndat1];

		double[] lnenergyKev1 = new double[ndat1];// real xxx
		double[] lneffCalc1 = new double[ndat1];// real yyy

		int[] rowIndex1 = new int[ndat1];// rowIndex for table!
		double[] ssig1 = new double[ndat1];
		// ==============
		double[] energyKev2 = new double[ndat2];// xxx
		double[] effCalc2 = new double[ndat2];// yyy
		double[] effCalcError2 = new double[ndat2];

		double[] lnenergyKev2 = new double[ndat2];// real xxx
		double[] lneffCalc2 = new double[ndat2];// real yyy

		int[] rowIndex2 = new int[ndat2];// rowIndex for table!
		double[] ssig2 = new double[ndat2];
		// THERE ARE NO DUPLICATES...SORT ENERGY ASCENDING!!!!

		for (int i = 0; i < ndat1; i++) {
			energyKev1[i] = en1V.elementAt(i);
			effCalc1[i] = eff_set1V.elementAt(i);
			effCalcError1[i] = eff_setError1V.elementAt(i);
			/*
			 * public static double log(double a) Returns the natural logarithm
			 * (base e) of a double value. Special cases: If the argument is NaN
			 * or less than zero, then the result is NaN. If the argument is
			 * positive infinity, then the result is positive infinity. If the
			 * argument is positive zero or negative zero, then the result is
			 * negative infinity.
			 */
			if (energyKev1[i] > 0.0) {
				lnenergyKev1[i] = Math.log(energyKev1[i]);
			} else {
				lnenergyKev1[i] = 0.0;
			}
			if (lnenergyKev1[i] < 0.0) {
				lnenergyKev1[i] = 0.0;
			}
			if (effCalc1[i] > 0.0) {
				lneffCalc1[i] = Math.log(effCalc1[i]);
			} else {
				lneffCalc1[i] = 0.0;
			}
			if (lneffCalc1[i] < 0.0) {
				lneffCalc1[i] = 0.0;
			}
			if (effCalcError1[i] > 0.0) {
				ssig1[i] = Math.log(effCalcError1[i]);
			} else {
				ssig1[i] = 1.0;// no error
			}
			if (ssig1[i] < 0.0) {
				ssig1[i] = 1.0;// no error
			}
			rowIndex1[i] = rowIndex1V.elementAt(i);
		}

		for (int i = 0; i < ndat2; i++) {
			energyKev2[i] = en2V.elementAt(i);
			effCalc2[i] = eff_set2V.elementAt(i);
			effCalcError2[i] = eff_setError2V.elementAt(i);

			if (energyKev2[i] > 0.0) {
				lnenergyKev2[i] = Math.log(energyKev2[i]);
			} else {
				lnenergyKev2[i] = 0.0;
			}
			if (lnenergyKev2[i] < 0.0) {
				lnenergyKev2[i] = 0.0;
			}
			if (effCalc2[i] > 0.0) {
				lneffCalc2[i] = Math.log(effCalc2[i]);
			} else {
				lneffCalc2[i] = 0.0;
			}
			if (lneffCalc2[i] < 0.0) {
				lneffCalc2[i] = 0.0;
			}
			if (effCalcError2[i] > 0.0) {
				ssig2[i] = Math.log(effCalcError2[i]);
			} else {
				ssig2[i] = 1.0;// no error
			}
			if (ssig2[i] < 0.0) {
				ssig2[i] = 1.0;// no error
			}
			rowIndex2[i] = rowIndex2V.elementAt(i);
		}
		// dispose vectors
		en1V = null;
		eff_set1V = null;
		eff_setError1V = null;
		en2V = null;
		eff_set2V = null;
		eff_setError2V = null;

		String s = (String) poly1Cb.getSelectedItem();
		int is = Convertor.stringToInt(s);

		s = (String) poly2Cb.getSelectedItem();
		int is2 = Convertor.stringToInt(s);

		int mma = is + 1;// 5 coefficients max!!
		int mma2 = is2 + 1;// 5 coefficients max!!

		double[] acof = new double[mma];
		int[] iia = new int[mma];
		double[] acof2 = new double[mma2];
		int[] iia2 = new int[mma2];

		for (int i = 0; i < mma; i++) {
			acof[i] = 0.0;// initial guess!! NONE!
			iia[i] = 1;// ALL FIT!
		}
		for (int i = 0; i < mma2; i++) {
			acof2[i] = 0.0;// initial guess!! NONE!
			iia2[i] = 1;// ALL FIT!
		}

		double[][] ccovar = new double[mma][mma];
		double[][] ccovar2 = new double[mma2][mma2];

		if (ndat1 >= 2)
			ModelingData.lfit(lnenergyKev1, lneffCalc1, ssig1, ndat1, acof,
					iia, mma, ccovar);

		if (ndat2 >= 2)
			ModelingData.lfit(lnenergyKev2, lneffCalc2, ssig2, ndat2, acof2,
					iia2, mma2, ccovar2);

		// set Function:=============
		eff_crossoverEnergy = crossoverEnergy;
		energyToEff1 = new double[acof.length];
		for (int i = 0; i < acof.length; i++) {
			energyToEff1[i] = acof[i];
		}
		int n = energyToEff1.length;
		if (n == 5) {// order 4
			eff_p1_a4 = energyToEff1[4];
			eff_p1_a3 = energyToEff1[3];
			eff_p1_a2 = energyToEff1[2];
			eff_p1_a1 = energyToEff1[1];
			eff_p1_a0 = energyToEff1[0];
		} else if (n == 4) {// order 3
			eff_p1_a4 = 0.0;
			eff_p1_a3 = energyToEff1[3];
			eff_p1_a2 = energyToEff1[2];
			eff_p1_a1 = energyToEff1[1];
			eff_p1_a0 = energyToEff1[0];
		} else if (n == 3) {// order 2
			eff_p1_a4 = 0.0;
			eff_p1_a3 = 0.0;
			eff_p1_a2 = energyToEff1[2];
			eff_p1_a1 = energyToEff1[1];
			eff_p1_a0 = energyToEff1[0];
		} else if (n == 2) {// order 1
			eff_p1_a4 = 0.0;
			eff_p1_a3 = 0.0;
			eff_p1_a2 = 0.0;
			eff_p1_a1 = energyToEff1[1];
			eff_p1_a0 = energyToEff1[0];
		}

		energyToEff2 = new double[acof2.length];
		for (int i = 0; i < acof2.length; i++) {
			energyToEff2[i] = acof2[i];
		}
		n = energyToEff2.length;
		if (n == 5) {// order 4
			eff_p2_a4 = energyToEff2[4];
			eff_p2_a3 = energyToEff2[3];
			eff_p2_a2 = energyToEff2[2];
			eff_p2_a1 = energyToEff2[1];
			eff_p2_a0 = energyToEff2[0];
		} else if (n == 4) {// order 3
			eff_p2_a4 = 0.0;
			eff_p2_a3 = energyToEff2[3];
			eff_p2_a2 = energyToEff2[2];
			eff_p2_a1 = energyToEff2[1];
			eff_p2_a0 = energyToEff2[0];
		} else if (n == 3) {// order 2
			eff_p2_a4 = 0.0;
			eff_p2_a3 = 0.0;
			eff_p2_a2 = energyToEff2[2];
			eff_p2_a1 = energyToEff2[1];
			eff_p2_a0 = energyToEff2[0];
		} else if (n == 2) {// order 1
			eff_p2_a4 = 0.0;
			eff_p2_a3 = 0.0;
			eff_p2_a2 = 0.0;
			eff_p2_a1 = energyToEff2[1];
			eff_p2_a0 = energyToEff2[0];
		}
		// ================ALL
		for (int i = 0; i < effTable.getRowCount(); i++) {
			double denergy = Convertor.stringToDouble(effData[i][2]);
			double deff = Convertor.stringToDouble(effData[i][6]);// eff

			double effcalib = F(denergy);
			s = Convertor.formatNumber(effcalib, 2);
			effTable.setValueAt(s, i, 8);

			double diffD = 100.0 * Math.abs(effcalib - deff) / deff;
			s = Convertor.formatNumber(diffD, 2);
			effTable.setValueAt(s, i, 9);
		}
		// ==============
		double overAllDiff = 0.0;
		double overAllDiff2 = 0.0;
		double initialError1 = 0.0;
		double initialError2 = 0.0;
		boolean data1B = true;
		boolean data2B = true;
		for (int i = 0; i < energyKev1.length; i++) {

			double effcalib = F(energyKev1[i]);
			double diffD = 100.0 * Math.abs(effcalib - effCalc1[i])
					/ effCalc1[i];
			overAllDiff = overAllDiff + diffD;
			initialError1 = initialError1 + 100.0
					* Math.abs(effCalcError1[i] / effCalc1[i]);
		}
		if (energyKev1.length != 0.0) {
			overAllDiff = overAllDiff / energyKev1.length;
			initialError1 = initialError1 / energyKev1.length;
		} else {
			overAllDiff = 0.0;
			data1B = false;
		}

		for (int i = 0; i < energyKev2.length; i++) {

			double effcalib = F(energyKev2[i]);
			double diffD = 100.0 * Math.abs(effcalib - effCalc2[i])
					/ effCalc2[i];
			overAllDiff2 = overAllDiff2 + diffD;
			initialError2 = initialError2 + 100.0
					* Math.abs(effCalcError2[i] / effCalc2[i]);
		}
		if (energyKev2.length != 0.0) {
			overAllDiff2 = overAllDiff2 / energyKev2.length;
			initialError2 = initialError2 / energyKev2.length;
		} else {
			overAllDiff2 = 0.0;
			data2B = false;
		}

		overAllEffDeviations1 = overAllDiff;
		overAllEffDeviations2 = overAllDiff2;
		if (data1B && data2B) {
			eff_overallProcentualError = (overAllEffDeviations1 + overAllEffDeviations2) / 2.0;
			eff_overallProcentualError = Math.max(eff_overallProcentualError,
					(initialError1 + initialError2) / 2.0);
		} else if (data1B) {
			eff_overallProcentualError = overAllEffDeviations1;
			eff_overallProcentualError = Math.max(eff_overallProcentualError,
					initialError1);
		} else {
			eff_overallProcentualError = overAllEffDeviations2;
			eff_overallProcentualError = Math.max(eff_overallProcentualError,
					initialError2);
		}
		canSaveB = true;// we have data to save!

		updateEffCalibrationGraph(energyKev1, effCalc1, energyKev2, effCalc2);

		// NOW DISPLAY RESULTS IN TEXT AREA:
		textArea.selectAll();
		textArea.replaceSelection("");

		if (canSaveGlobalB) {
			s = resources.getString("eff.calib.textArea.global");
			textArea.append(s + "\n");//
			s = "" + efficiencyGlobal + resources.getString("results.+-")
					+ efficiencyGlobalError;
			textArea.append(s + "\n");
		}

		s = resources.getString("eff.calib.textArea.crossover");
		textArea.append(s + "\n");
		s = "" + eff_crossoverEnergy;
		textArea.append(s + "\n");
		s = resources.getString("eff.calib.textArea.effCal1");
		s = eff_p1_a4 + "; " + eff_p1_a3 + "; " + eff_p1_a2 + "; " + eff_p1_a1
				+ "; " + eff_p1_a0;
		textArea.append(s + "\n");
		s = resources.getString("eff.calib.textArea.effCal2");
		s = eff_p2_a4 + "; " + eff_p2_a3 + "; " + eff_p2_a2 + "; " + eff_p2_a1
				+ "; " + eff_p2_a0;
		textArea.append(s + "\n");
		s = resources.getString("eff.calib.textArea.effDiff");
		textArea.append(s + "\n");
		s = "" + eff_overallProcentualError;
		textArea.append(s + "\n");

	}

	/**
	 * Add activity and uncertainty data for nuclide. 
	 */
	private void add() {
		String s = activityTf.getText();
		String s1 = errActivityTf.getText();
		double ac = 0.0;
		double errac = 0.0;
		double hlsec = 0.0;
		int rowCount = nucTable.getRowCount();
		int selRow = nucTable.getSelectedRow();
		int toBeSelected = selRow + 1;
		if (selRow == -1) {
			return;
		}
		int d0 = 0;
		int d = 0;
		int m0 = 0;
		int m = 0;
		int y0 = 0;
		int y = 0;
		// test............................
		boolean nulneg = false;
		try {
			ac = Convertor.stringToDouble(s);
			errac = Convertor.stringToDouble(s);
			d0 = Convertor.stringToInt((String) day0Cb.getSelectedItem());
			d = Convertor.stringToInt((String) dayCb.getSelectedItem());
			m0 = Convertor.stringToInt((String) month0Cb.getSelectedItem());
			m = Convertor.stringToInt((String) monthCb.getSelectedItem());
			y0 = Convertor.stringToInt((String) year0Tf.getText());
			y = Convertor.stringToInt((String) yearTf.getText());

			if (ac <= 0)
				nulneg = true;
			if (errac <= 0)
				nulneg = true;
			if (d0 <= 0 || d <= 0)
				nulneg = true;
			if (m0 <= 0 || m <= 0)
				nulneg = true;
			if (y0 <= 0 || y <= 0)
				nulneg = true;

		} catch (Exception e) {
			String title = resources.getString("number.error.title2");
			String message = resources.getString("number.error2");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			e.printStackTrace();
			return;
		}
		if (nulneg) {
			String title = resources.getString("number.error.title2");
			String message = resources.getString("number.error2");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		// =====ACtivity to date!!!
		double measurementActivity = 0.0;

		String nucS = (String) nucTable.getValueAt(selRow, 1);
		for (int i = 0; i < mf.roiV.size(); i++) {
			String roiNuc = mf.roiV.elementAt(i).getNuclide();
			if (roiNuc.equals(nucS)) {
				double hl = mf.roiV.elementAt(i).getHalfLife();
				String hlu = mf.roiV.elementAt(i).getHalfLifeUnits();
				hlsec = GammaLibraryFrame.formatHalfLife(hl, hlu);
				break;
			}
		}
		measurementActivity = PhysUtilities.decayLaw(ac, hlsec, d0, m0, y0, d,
				m, y);

		nucTable.setValueAt(Convertor.doubleToString(measurementActivity),
				selRow, 2);
		nucTable.setValueAt(s1, selRow, 3);

		if (selRow == rowCount - 1) {// the last
			toBeSelected = 0;
		}
		nucTable.setRowSelectionInterval(toBeSelected, toBeSelected);

		activityTf.setText("");
		activityTf.requestFocusInWindow();
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
	 * Initialize the calibration by performing efficiency computation on certain points 
	 * (i.e. at certain energies). If a single nuclide is present in standard source, a global efficiency 
	 * is also computed.
	 */
	private void initialize() {

		nuclideS = new Vector<String>();// "";
		energyD = new Vector<Double>();// 0.0;
		yieldD = new Vector<Double>();// ;
		netRateD = new Vector<Double>();// ;
		netRateErrorD = new Vector<Double>();// ;
		effProc_calcD = new Vector<Double>();// ;
		effProc_calcErrorD = new Vector<Double>();// ;

		double activityD = 0.0;
		double activityProcErrorD = 0.0;
		boolean nulNeg = false;

		int rowCount = nucTable.getRowCount();
		if (rowCount < 1) {
			String title = resources.getString("number.error.title3");
			String message = resources.getString("number.error3");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		String nucS = "";
		for (int i = 0; i < rowCount; i++) {
			nucS = (String) nucTable.getValueAt(i, 1);
			if (nucS.equals("NoName")) {
				String title = resources.getString("number.error.title3");
				String message = resources.getString("number.error3");
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				activityD = Convertor.stringToDouble((String) nucTable
						.getValueAt(i, 2));
				activityProcErrorD = Convertor.stringToDouble((String) nucTable
						.getValueAt(i, 3));
				if (activityD <= 0.0 || activityProcErrorD < 0.0) {
					nulNeg = true;
				}
			} catch (Exception exc) {
				String title = resources.getString("number.error.title4");
				String message = resources.getString("number.error4");
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (nulNeg) {
				String title = resources.getString("number.error.title4");
				String message = resources.getString("number.error4");
				JOptionPane.showMessageDialog(null, message, title,
						JOptionPane.ERROR_MESSAGE);
				return;
			}

			for (int j = 0; j < mf.roiV.size(); j++) {
				GammaRoi gr = mf.roiV.elementAt(j);
				if (gr.getNuclide().equals(nucS)) {
					// found an assigned nuclide!

					nuclideS.addElement(gr.getNuclide());
					energyD.addElement(gr.getCentroidEnergy());
					yieldD.addElement(gr.getYield());
					netRateD.addElement(gr.getNetCountsRate());
					netRateErrorD.addElement(gr.getNetCountsRateError());

					if (gr.getYield() != 0.0) {
						double d = 100.0 * gr.getNetCountsRate()
								/ (gr.getYield() * activityD);
						effProc_calcD.addElement(d);
						double aerr = activityD * activityProcErrorD / 100.0;

						effProc_calcErrorD.addElement(d
								* Math.sqrt((gr.getNetCountsRateError())
										* (gr.getNetCountsRateError())
										/ ((gr.getNetCountsRate()) * (gr
												.getNetCountsRate())) + aerr
										* aerr / (activityD * activityD)));
					} else {
						effProc_calcD.addElement(0.0);
						effProc_calcErrorD.addElement(0.0);
					}

				}
			}
		}

		String[] effColumnNames = (String[]) resources
				.getObject("eff.calibration.eff.columns");
		effData = new String[effProc_calcD.size()][effColumnNames.length];
		int ncols = 11;//
		int ntemp = 0;
		Vector<String> rowV = new Vector<String>();
		Vector<Object> dataV = new Vector<Object>();

		for (int i = 0; i < effProc_calcD.size(); i++) {
			rowV = new Vector<String>();

			effData[i][0] = Convertor.intToString(i + 1);
			effData[i][1] = nuclideS.elementAt(i);
			effData[i][2] = Convertor.doubleToString(energyD.elementAt(i));
			effData[i][3] = Convertor.doubleToString(yieldD.elementAt(i));
			effData[i][4] = Convertor.doubleToString(netRateD.elementAt(i));
			effData[i][5] = Convertor
					.doubleToString(netRateErrorD.elementAt(i));
			effData[i][6] = Convertor
					.doubleToString(effProc_calcD.elementAt(i));
			effData[i][7] = Convertor.doubleToString(effProc_calcErrorD
					.elementAt(i));
			effData[i][8] = "";// calib
			effData[i][9] = "";// diff
			effData[i][10] = resources.getString("difference.yes");// calib

			for (int j = 0; j < ntemp; j++) {
				if (effData[i][2] == effData[j][2]) {
					String title = resources.getString("number.error.title7");
					String message = resources.getString("number.error7");
					JOptionPane.showMessageDialog(null, message, title,
							JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			ntemp++;

			rowV.addElement(effData[i][0]);
			rowV.addElement(effData[i][1]);
			rowV.addElement(effData[i][2]);
			rowV.addElement(effData[i][3]);
			rowV.addElement(effData[i][4]);
			rowV.addElement(effData[i][5]);
			rowV.addElement(effData[i][6]);
			rowV.addElement(effData[i][7]);
			rowV.addElement(effData[i][8]);
			rowV.addElement(effData[i][9]);
			rowV.addElement(effData[i][10]);

			dataV.addElement(rowV);
		}
		// Sort
		Sort.qSort(dataV, ncols, 2);// after 3 rd element which is energy!!
		// ------------
		effData = new String[effProc_calcD.size()][effColumnNames.length];
		for (int i = 0; i < effProc_calcD.size(); i++) {
			effData[i][0] = getValueAt(dataV, i, 0).toString();
			effData[i][1] = getValueAt(dataV, i, 1).toString();
			effData[i][2] = getValueAt(dataV, i, 2).toString();
			effData[i][3] = getValueAt(dataV, i, 3).toString();
			effData[i][4] = getValueAt(dataV, i, 4).toString();
			effData[i][5] = getValueAt(dataV, i, 5).toString();
			effData[i][6] = getValueAt(dataV, i, 6).toString();
			effData[i][7] = getValueAt(dataV, i, 7).toString();
			effData[i][8] = getValueAt(dataV, i, 8).toString();
			effData[i][9] = getValueAt(dataV, i, 9).toString();
			effData[i][10] = getValueAt(dataV, i, 10).toString();
		}

		if (rowCount == 1) {
			// GLOBALLLLLLLLLLLLLLLLL
			double net = mf.globalCounts;
			double netBkg = mf.globalBkgCounts;
			double t = mf.spectrumLiveTime;
			// if here, always spectrumLiveTime>0
			double tBkg = mf.bkgSpectrumLiveTime;

			double netError = Math.sqrt(net);

			activityD = Convertor.stringToDouble((String) nucTable.getValueAt(
					0, 2));
			activityProcErrorD = Convertor.stringToDouble((String) nucTable
					.getValueAt(0, 3));
			double activityErrorD = activityProcErrorD * activityD / 100.0;

			double effGlobal = 0.0;
			double effGlobalError = 0.0;

			if (tBkg != 0.0 && netBkg != 0.0) {
				// we have ambiental BKG!!
				effGlobal = 100.0 * (net - netBkg * t / tBkg) / (t * activityD);
				netError = Math.sqrt(net + netBkg * t / tBkg);
				net = net - netBkg * t / tBkg;
				if (effGlobal < 0.0) {
					effGlobal = 0.0;// net<0.0!
				}
				effGlobalError = effGlobal
						* Math.sqrt(Math.pow(netError / net, 2)
								+ Math.pow(activityErrorD / activityD, 2));
			} else {
				effGlobal = 100.0 * net / (t * activityD);
				effGlobalError = effGlobal
						* Math.sqrt(Math.pow(netError / net, 2)
								+ Math.pow(activityErrorD / activityD, 2));
			}
			globalNuclide = (String) nucTable.getValueAt(0, 1);
			efficiencyGlobal = effGlobal;
			efficiencyGlobalError = effGlobalError;
			canSaveGlobalB = true;

			textArea.selectAll();
			textArea.replaceSelection("");

			String s = resources.getString("eff.calib.textArea.global");
			textArea.append(s + "\n");//
			s = "" + efficiencyGlobal + resources.getString("results.+-")
					+ efficiencyGlobalError;
			textArea.append(s + "\n");
		}

		DefaultTableModel dfm0 = new DefaultTableModel(effData, effColumnNames) {

			// Do not allow to alter!
			public boolean isCellEditable(int row, int col) {
				return false;
			}
		};

		// build a customized JTable
		effTable = new JTable(dfm0) {

			// Text CENTER aligned!!
			public TableCellRenderer getCellRenderer(int row, int col) {
				TableCellRenderer renderer = super.getCellRenderer(row, col);

				((JLabel) renderer)
						.setHorizontalAlignment(SwingConstants.CENTER);
				return renderer;
			}
		};
		// some other customization:
		ListSelectionModel rowSM0 = effTable.getSelectionModel();
		rowSM0.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);// single

		// selection
		if (effTable.getRowCount() != 0)
			effTable.setRowSelectionInterval(0, 0);// first row!

		suportMainTabP.removeAll();
		suportMainTabP.add(new JScrollPane(effTable));
		validate();

	}

	/**
	 * Set this efficiency to be used in calibration (fitted function)
	 */
	private void set() {
		String s = resources.getString("difference.yes");
		int selRow = effTable.getSelectedRow();
		if (selRow == -1) {
			return;
		}
		effTable.setValueAt(s, selRow, 10);
	}

	/**
	 * Remove this efficiency from calibration.
	 */
	private void remove() {
		String s = resources.getString("difference.no");
		int selRow = effTable.getSelectedRow();
		if (selRow == -1) {
			return;
		}
		effTable.setValueAt(s, selRow, 10);
	}

	/**
	 * Test efficiency calibration (in-use calibration and current calibration - if any).
	 */
	private void test() {
		String s = testTf.getText();
		boolean negB = false;
		double d = 0.0;// channel
		try {
			d = Convertor.stringToDouble(s);

		} catch (Exception ex) {
			negB = true;
		}
		if (d < 0.0) {
			negB = true;
		}
		if (negB) {
			String title = resources.getString("number.error.title");
			String message = resources.getString("number.error");
			JOptionPane.showMessageDialog(null, message, title,
					JOptionPane.ERROR_MESSAGE);

			return;
		}

		textArea.selectAll();
		textArea.replaceSelection("");

		double e = 0.0;
		if (d != 0.0) {
			e = Math.log(d);
		}
		if (e < 0.0)
			e = 0.0;

		double dd = 0.0;
		if (d < eff_crossoverEnergy)
			dd = Math.exp(eff_p1_a4_current * e * e * e * e + eff_p1_a3_current
					* e * e * e + eff_p1_a2_current * e * e + eff_p1_a1_current
					* e + eff_p1_a0_current);
		else
			dd = Math.exp(eff_p2_a4_current * e * e * e * e + eff_p2_a3_current
					* e * e * e + eff_p2_a2_current * e * e + eff_p2_a1_current
					* e + eff_p2_a0_current);
		s = resources.getString("eff.calib.textArea.testOld");
		textArea.append(s + dd + "\n");

		if (d < eff_crossoverEnergy)
			dd = Math.exp(eff_p1_a4 * e * e * e * e + eff_p1_a3 * e * e * e
					+ eff_p1_a2 * e * e + eff_p1_a1 * e + eff_p1_a0);
		else
			dd = Math.exp(eff_p2_a4 * e * e * e * e + eff_p2_a3 * e * e * e
					+ eff_p2_a2 * e * e + eff_p2_a1 * e + eff_p2_a0);

		s = resources.getString("eff.calib.textArea.testNew");
		textArea.append(s + dd + "\n");
	}

	/**
	 * Set efficiency calibration. This is two order 3 polynomials intersected at a 
	 * cross-over energy (if cross-over energy is set to 0, then a single polynomial is used). The function is Ln(Eff) = F(Ln(En)).
	 * @param e1_a4 e_p1_a4
	 * @param e1_a3 e_p1_a3
	 * @param e1_a2 e_p1_a2
	 * @param e1_a1 e_p1_a1
	 * @param e1_a0 e_p1_a0
	 * @param e2_a4 e_p2_a4
	 * @param e2_a3 e_p2_a3
	 * @param e2_a2 e_p2_a2
	 * @param e2_a1 e_p2_a1
	 * @param e2_a0 e_p2_a0
	 * @param e_cross the crossover energy
	 */
	public static void setCalibrations(double e1_a4, double e1_a3,
			double e1_a2, double e1_a1, double e1_a0, double e2_a4,
			double e2_a3, double e2_a2, double e2_a1, double e2_a0,
			double e_cross) {
		eff_p1_a4_current = e1_a4;
		eff_p1_a3_current = e1_a3;
		eff_p1_a2_current = e1_a2;
		eff_p1_a1_current = e1_a1;
		eff_p1_a0_current = e1_a0;

		eff_p2_a4_current = e2_a4;
		eff_p2_a3_current = e2_a3;
		eff_p2_a2_current = e2_a2;
		eff_p2_a1_current = e2_a1;
		eff_p2_a0_current = e2_a0;

		eff_crossoverEnergy_current = e_cross;
	}

	/**
	 * Go to database and set and/or save calibration.
	 */
	private void save() {

		GammaEfficiencyCalibrationSaveFrame gecsf = new GammaEfficiencyCalibrationSaveFrame(
				this);
		if (canSaveGlobalB) {
			gecsf.statusL
					.setText(resources.getString("eff.saveCal.global.can"));
		} else {
			gecsf.statusL.setText(resources
					.getString("eff.saveCal.global.can.not"));
		}
	}

	// =========IMPLEMENTATION OF FUNCTION INTERFACE===================
	/**
	 * Interface method.
	 */
	public void printSequence(String s) {// for printing
		System.out.println(s);
	}

	// -------------------------------------------------------
	private double[] energyToEff1;
	private double[] energyToEff2;

	/**
	 * Interface method. The fitting function.
	 */
	public double F(double x) {// y=f(x)
		double y = 0.0;
		double e = 0.0;
		if (x != 0.0) {
			e = Math.log(x);
		}
		if (e < 0.0)
			e = 0.0;

		if (x < crossoverEnergy) {
			for (int i = 0; i < energyToEff1.length; i++) {
				y = y + energyToEff1[i] * Math.pow(e, i);
			}
			y = Math.exp(y);
		} else {
			for (int i = 0; i < energyToEff2.length; i++) {
				y = y + energyToEff2[i] * Math.pow(e, i);
			}
			y = Math.exp(y);
		}

		return y;
		// return 0.0;
	}

	// --------------------------------------------------------
	public double[] FD(double x) {// 0=->y=f(x) and 1-> dy=f'(x)
		return null;
	}

	public double MF(double[] x) {// y=f(x1,x2,...)
		return 0.0;
	}

	public double[] DMF(double[] x) {// y'1=df(x1,x2,...)/dx1;...the vector
										// gradient
		// df[1..n] evaluated at the input point x
		return null;
	}

	// ==============3D func====================
	public double F3D(double x, double y, double z) {
		return 0.0;
	}

	public double yy1(double x) {
		return 0.0;
	}

	public double yy2(double x) {
		return 0.0;
	}

	public double z1(double x, double y) {
		return 0.0;
	}

	public double z2(double x, double y) {
		return 0.0;
	}

	// end 3d====================================
	// non linear equation systems: root finding
	public double[] vecfunc(int n, double[] x) {
		return null;
	}

	// ==========================================
	public double[] aF(double x, int ma) {// poli fit
		double[] y = new double[ma];

		for (int i = 0; i < ma; i++) {
			y[i] = Math.pow(x, i);
		}
		// y[0]=1.0;
		// y[1]=x;
		// y[2]=x*x;
		// y[3]=x*x*x;
		return y;
	}

	// fdf used in mrqmin-mrqcof routine!!!!!!!!!!!!!!!!!!
	public double fdf(double x, double[] a, double[] dyda, int na) {
		// gauss fit etc. nonliniar
		// double y = 0.0;

		// double expt = Math.exp(-(x - a[1]) * (x - a[1]) / (2.0 * a[2] *
		// a[2]));
		// y = a[0] * expt;
		// dyda[0] = expt;
		// dyda[1] = y * ((x - a[1]) / (a[2] * a[2]));
		// dyda[2] = y * ((x - a[1]) * (x - a[1]) / (a[2] * a[2] * a[2]));
		return 0.0;// y;
	}

	// ===========================================================
	public double[] derivF(double x, double[] y) {
		return null;
	}

	// ============2point
	public void load(double x1, double[] v, double[] y) {
		return;
	}

	public void load1(double x1, double[] v, double[] y) {
		return;
	}

	public void load2(double x2, double[] v, int nn2, double[] y) {
		return;
	}

	public void score(double x2, double[] y, double[] f) {
		return;
	}

	public void difeq(int k, int k1, int k2, int jsf, int is1, int isf,
			int indexv[], int ne, double[][] s, double[][] y) {
		return;
	}

	// ================================================
	public double g(double t) {// g(t)=FREDHOLM
		return 0.0;
	}

	public double ak(double t, double s) {// KERNEL
		return 0.0;
	}

	public double g(int k, double t) {// voltera
		return 0.0;
	}

	public double ak(int k, int l, double t, double s) {// voltera
		return 0.0;
	}

	public void kermom(double[] w, double y, int m) {
		return;
	}

	// =========END IMPLEMENTATION OF FUNCTION INTERFACE===================
}
