package gui;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import danfulea.utils.FrameUtilities;

/**
 * The HowTo window displays some tips about the application. 
 * 
 * @author Dan Fulea, 19 May 2011
 */
@SuppressWarnings("serial")
public class HowToFrame extends JFrame{
	private final Dimension PREFERRED_SIZE = new Dimension(650, 400);
	private GammaAnalysisFrame mf;
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;
	private JScrollPane jScrollPane1 = new JScrollPane();
	private JTextArea textArea = new JTextArea();

	/**
	 * Constructor
	 * @param mf the GammaAnalysisFrame object
	 */
	public HowToFrame(GammaAnalysisFrame mf) {
		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("HowTo.NAME"));
		//this.setResizable(false);

		this.mf = mf;
		
		textArea.setBackground(GammaAnalysisFrame.textAreaBkgColor);
		textArea.setForeground(GammaAnalysisFrame.textAreaForeColor);
		createGUI();

		setDefaultLookAndFeelDecorated(true);
		FrameUtilities.createImageIcon(
				this.resources.getString("form.icon.url"), this);

		FrameUtilities.centerFrameOnScreen(this);

		setVisible(true);
		mf.setEnabled(false);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);//not necessary, exit normal!
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
		mf.setEnabled(true);
		dispose();
	}

	/**
	 * Create GUI
	 */
	private void createGUI() {		

		jScrollPane1.setBorder(new javax.swing.border.TitledBorder(
				new javax.swing.border.LineBorder(
						new java.awt.Color(0, 51, 255), 1, true),
						this.resources.getString("HowTo.title"),
				javax.swing.border.TitledBorder.CENTER,
				javax.swing.border.TitledBorder.TOP));
		jScrollPane1
				.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		jScrollPane1
				.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setAutoscrolls(true);
		textArea.setColumns(1);
		textArea.setEditable(false);

		textArea.setLineWrap(true);
		textArea.setRows(10);
		textArea.setText(this.resources.getString("HowTo"));
		textArea.setWrapStyleWord(true);
		jScrollPane1.setViewportView(textArea);

		JPanel jPanel2 = new JPanel();
		jPanel2.setLayout(new java.awt.BorderLayout());
		jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);
		jPanel2.setBackground(GammaAnalysisFrame.bkgColor);

		getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);
		pack();
	}
}
