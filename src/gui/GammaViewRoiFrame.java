package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ResourceBundle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import danfulea.math.Convertor;
import danfulea.phys.GammaRoi;
import danfulea.utils.FrameUtilities;

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

/**
 * The GammaViewRoi window displays the ROI and ROI fit. 
 * 
 * @author Dan Fulea, 07 Jun 2011
 */
@SuppressWarnings("serial")
public class GammaViewRoiFrame extends JFrame {

	private final Dimension PREFERRED_SIZE = new Dimension(700, 700);
	private GammaAnalysisFrame mf;
	private GammaRoi gr;
	private static final String BASE_RESOURCE_CLASS = "gui.resources.GammaAnalysisFrameResources";
	private ResourceBundle resources;

	/**
	 * Constructor
	 * @param mf the GammaAnalysisFrame object
	 * @param gr the GammaRoi object
	 */
	public GammaViewRoiFrame(GammaAnalysisFrame mf, GammaRoi gr) {
		this.mf = mf;
		this.gr = gr;

		resources = ResourceBundle.getBundle(BASE_RESOURCE_CLASS);
		this.setTitle(resources.getString("ViewRoi.NAME"));

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
		mf.setEnabled(true);
		dispose();
	}

	/**
	 * Create GUI
	 */
	private void createGUI() {
		JFreeChart gammaChart = getGammaChart();
		ChartPanel cp = new ChartPanel(gammaChart, false, true, true, false,
				true);
		JPanel content = new JPanel(new BorderLayout());
		content.add(cp);
		setContentPane(new JScrollPane(content));
		pack();
	}

	// chart
	/**
	 * Return the ROI chart
	 * @return the result
	 */
	private JFreeChart getGammaChart() {
		double[] channel = gr.getChannelData();
		double[] pulses = gr.getComptonCorrectedPulses();
		if (channel == null)
			return null;

		XYSeries series = new XYSeries(resources.getString("chart.raw.NAME"));
		XYSeries fitseries = new XYSeries(resources.getString("chart.fit.NAME"));

		int n = channel.length;
		for (int j = 0; j < n; j++) {
			series.add(channel[j], pulses[j]);
		}

		if (gr.getNetCalculationMethod_internal() == GammaRoi.NET_CALCULATION_GAUSSIAN) {
			double dlta = 1.0 / 10.0;
			int i = 0;
			while (true) {
				i++;
				double x = channel[0] + (i - 1) * dlta;
				double y = gr.F(x);
				fitseries.add(x, y);
				if (channel[0] + (i - 1) * dlta > channel[channel.length - 1]) {
					break;
				}
			}
		}

		// multipoint-fit

		// Data collection!...only one series per collection for full control!
		XYSeriesCollection data = new XYSeriesCollection(series);
		XYSeriesCollection fitdata = new XYSeriesCollection(fitseries);
		// Axis
		NumberAxis xAxis = new NumberAxis(
				resources.getString("chart.x.Channel"));// "Channel");
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = new NumberAxis(resources.getString("chart.y.Pulses"));// "Pulses");
		// Now the plot:
		XYPlot plot = new XYPlot();
		plot.setOrientation(PlotOrientation.VERTICAL);
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		// allow chart movement by pressing CTRL and drag with mouse!
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
		// 1st axis
		plot.setDomainAxis(0, xAxis);// the axis index;axis
		plot.setRangeAxis(0, yAxis);
		// //////////////////////////////////DATASET AND
		// RENDERER/////////////////
		int idataset = 0;
		plot.setDataset(idataset, data);// idataset=0!
		XYItemRenderer renderer = new StandardXYItemRenderer(
		// StandardXYItemRenderer.SHAPES_AND_LINES);//LINES);
				StandardXYItemRenderer.LINES);
		// the series index (zero-based)in Collection...always 0 as seen above!
		renderer.setSeriesPaint(0, Color.RED);
		renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
		plot.setRenderer(idataset, renderer);// idataset=0!

		idataset = 1;// channel,pulses BKG
		if (gr.getNetCalculationMethod_internal() == GammaRoi.NET_CALCULATION_GAUSSIAN) {
			plot.setDataset(idataset, fitdata);
			XYItemRenderer renderer0 = new StandardXYItemRenderer(
					StandardXYItemRenderer.LINES);
			renderer0.setSeriesPaint(0, Color.BLUE);
			plot.setRenderer(idataset, renderer0);
		}

		String chartName = resources.getString("viewRoi.centroidChannel")
				+ Convertor.formatNumber(gr.getCentroidChannel(), 2) + "; "
				+ resources.getString("viewRoi.centroidEnergy")
				+ Convertor.formatNumber(gr.getCentroidEnergy(), 2) + "; "
				+ resources.getString("viewRoi.nuclide") + gr.getNuclide();

		JFreeChart chart = new JFreeChart(chartName,
				JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		chart.setBackgroundPaint(GammaAnalysisFrame.bkgColor);
		return chart;
	}

}
