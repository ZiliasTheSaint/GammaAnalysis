package gui.resources;

import java.util.ListResourceBundle;

/**
 * The resource bundle for GammaAnalysisFrame class. <br>
 * 
 * @author Dan Fulea, 17 Apr. 2011
 * 
 */
public class GammaAnalysisFrameResources extends ListResourceBundle {
	/**
	 * Returns the array of strings in the resource bundle.
	 * 
	 * @return the resources.
	 */
	public Object[][] getContents() {
		// TODO Auto-generated method stub
		return CONTENTS;
	}

	/** The resources to be localised. */
	private static final Object[][] CONTENTS = {

			// displayed images..
			{ "form.icon.url", "/danfulea/resources/personal.png" },///jdf/resources/duke.png" },
			{ "icon.url", "/danfulea/resources/personal.png" },//jdf/resources/globe.gif" },

			//{ "icon.package.url", "gui/resources/personal.png" },//this package
			//{ "icon.package.relative.url", "/gui/resources/personal.png" }, // not working for src img!!
			{ "icon.package.url", "images/personal.png" },
			
			{ "img.zoom.in", "/danfulea/resources/zoom_in.png" },
			{ "img.zoom.out", "/danfulea/resources/zoom_out.png" },
			{ "img.pan.left", "/danfulea/resources/arrow_left.png" },
			{ "img.pan.up", "/danfulea/resources/arrow_up.png" },
			{ "img.pan.down", "/danfulea/resources/arrow_down.png" },
			{ "img.pan.right", "/danfulea/resources/arrow_right.png" },
			{ "img.pan.refresh", "/danfulea/resources/arrow_refresh.png" },

			{ "img.accept", "/danfulea/resources/accept.png" },
			{ "img.insert", "/danfulea/resources/add.png" },
			{ "img.delete", "/danfulea/resources/delete.png" },
			{ "img.delete.all", "/danfulea/resources/bin_empty.png" },
			{ "img.view", "/danfulea/resources/eye.png" },
			{ "img.set", "/danfulea/resources/cog.png" },
			{ "img.report", "/danfulea/resources/document_prepare.png" },
			{ "img.today", "/danfulea/resources/time.png" },
			{ "img.open.file", "/danfulea/resources/open_folder.png" },
			{ "img.open.database", "/danfulea/resources/database_connect.png" },
			{ "img.save.database", "/danfulea/resources/database_save.png" },
			{ "img.substract.bkg", "/danfulea/resources/database_go.png" },
			{ "img.close", "/danfulea/resources/cross.png" },
			{ "img.about", "/danfulea/resources/information.png" },
			{ "img.printer", "/danfulea/resources/printer.png" },

			{ "toolbar.refresh", "Refresh" },
			{ "toolbar.refresh.toolTip",
					"Restore the chart at its original size and location and set its title!" },
			{ "toolbar.refresh.mnemonic", new Character('R') },

			{ "toolbar.today", "Today" },
			{ "toolbar.today.toolTip", "Set the date at today" },
			{ "toolbar.today.mnemonic", new Character('y') },

			{ "toolbar.day", "Day: " },
			{ "toolbar.month", "Month: " },
			{ "toolbar.year", "Year: " },
			{ "date.border", "Measurement date" },
			// { "info.border", "Spectrum additional information" },
			{ "info.border", "Spectrum information" },
			{ "toolbar.spectrum.id", "Spectrum name: " },
			{ "toolbar.spectrum.time", "Spectrum live time [sec.]: " },

			{ "toolbar.spectrum.quantity", "Measured quantity [optional]: " },
			{ "toolbar.spectrum.quantityUnit", "Quantity unit [optional]: " },

			{ "Application.NAME", "Gamma analysis" },
			{ "About.NAME", "About" },
			{ "Library.NAME", "Gamma library" },
			{ "Database.NAME", "Gamma database" },
			{ "ViewChain.NAME",
					"Gamma decay chain informations (summary of all chains)" },
			{ "HowTo.NAME", "How to..." },
			{ "Results.NAME", "Gamma results" },
			{ "CoinFrame.NAME", "True coincidence correction" },
			{ "ViewRoi.NAME", "ROI visualization (all correction performed)" },
			{ "PeakSearch.NAME", "Peak search. All existed ROIs were deleted!" },
			{ "PeakIdentify.NAME",
					"Peak identify. A good nuclide library is required!" },
			{
					"RoiSet.NAME",
					"Roi assignment. Other nuclides can be imported from library (main frame->Tools->Nuclides library)" },
			{ "Energy.FWHM.NAME", "Energy/FWHM calibrations" },
			{ "Efficiency.NAME", "Efficiency calibration" },
			{ "Energy.FWHM.SAVE.NAME", "Save/set the Energy/FWHM calibrations" },
			{ "Efficiency.SAVE.NAME", "Save/set the Efficiency calibrations" },

			{ "Author", "Author:" },
			{ "Author.name", "Dan Fulea , fulea.dan@gmail.com" },

			{ "Version", "Version:" },
			{ "Version.name", "Gamma analysis 1.3" },

			{
					"License",
					//"This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License (version 2) as published by the Free Software Foundation. \n\nThis program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. \n" },
			"Copyright (c) 2014, Dan Fulea \nAll rights reserved.\n\nRedistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n\n1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.\n\n2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.\n\n3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.\n\nTHIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 'AS IS' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n" },
			{ "pleaseWait.label", "Work in progress!" },

			{ "chart.NAME", "Gamma spectrum" },
			{ "chart.sample.NAME", "Sample spectrum" },
			{ "chart.bkg.NAME", "Background spectrum" },
			{ "chart.raw.NAME", "Raw data" },
			{ "chart.fit.NAME", "Gaussian fit" },
			{ "chart.x.Channel", "Channel" },
			{ "chart.x.keV", "keV" },
			{ "chart.y.Pulses", "Pulses" },
			{ "chart.y.Pulses.ln", "Ln(Pulses)" },
			{ "chart.y.Pulses.sqrt", "Sqrt(Pulses)" },
			{ "chart.marker", "Marker " },
			{ "chart.zoom.border", "Spectrum zoom" },
			{ "chart.navigation.border", "Spectrum navigation" },
			{ "chart.edge.start", "Start edge " },
			{ "chart.edge.end", "End edge " },

			{ "marker.left.border", "Adjust left marker" },
			{ "marker.right.border", "Adjust right marker" },
			{ "roi.creation.border", "ROI creation" },
			{ "roi.insert", "Insert ROI" },
			{ "roi.insert.toolTip", "Insert ROI between markers" },
			{ "roi.insert.mnemonic", new Character('I') },
			{ "roi.edge", "Update ROI edges" },
			{ "roi.edge.toolTip",
					"Update ROI edges for further continuum (Compton) background subtraction!" },
			{ "roi.edge.mnemonic", new Character('U') },
			{ "roi.label", "ROI ID: " },
			{ "roi.name.label", "Nuclide: " },// @@@@@@@@@@@@@
			{ "roi.operation.border", "ROI tasks" },
			{ "roi.view", "View ROI" },
			{ "roi.view.toolTip", "ROI visualization" },
			{ "roi.view.mnemonic", new Character('V') },
			{ "roi.delete", "Delete ROI" },
			{ "roi.delete.toolTip", "Delete this ROI" },
			{ "roi.delete.mnemonic", new Character('D') },
			{ "roi.deleteAll", "Delete all ROIs" },
			{ "roi.deleteAll.toolTip", "Delete all ROIs" },
			{ "roi.deleteAll.mnemonic", new Character('a') },
			{ "roi.set", "Set ROI..." },
			{ "roi.set.toolTip", "Set ROI" },
			{ "roi.set.mnemonic", new Character('S') },
			{ "roi.report", "ROIs report..." },
			{ "roi.report.toolTip", "Show results before printing" },
			{ "roi.report.mnemonic", new Character('p') },
			{ "roi.count", "Roi count: " },
			{ "rb.kev", "keV" },
			{ "rb.channel", "channel" },
			{ "rb.border", "Spectrum view" },

			{ "rb.net.border", "Net Area/FWHM mode" },
			{ "nai.rb", "default mode" },
			{ "ge.rb", "Ge calculation mode" },
			{ "gauss.rb", "Gaussian fit" },

			{
					"nai.rb.toolTip",
					"Default mode. Net area (and FWHM) is computed using channel data (channel by channel pulses sum and FWHM check)." },
			{
					"ge.rb.toolTip",
					"Germanium mode for FWHM evaluation (FWHM computed analytically using the 3 points gaussian fit method ). Net area is computed using channel by channel pulses sum!" },
			{
					"gauss.rb.toolTip",
					"Universal mode best used for peaks's deconvolution. Both Net area and FWHM are computed using the gaussian fit over all ROI points!" },

			{ "rb.mda.border", "MDA calculation mode:" },
			{ "pasternack.rb", "based on Pasternack theory" },
			{ "curie.rb", "based on Curie theory" },
			{ "default.rb", "accurate (default)" },
			{ "mdaCb", new String[] { "Pasternack", "Curie", "Default" } },

			{ "menu.file", "File" },
			{ "menu.file.mnemonic", new Character('F') },

			{ "openFile", "Open spectrum file..." },
			{ "openFile.toolTip", "Opening a valid spectrum file" },
			{ "openFile.mnemonic", new Character('O') },

			{ "openDB", "Open spectrum from database..." },
			{ "openDB.toolTip", "Opening a valid spectrum" },
			{ "openDB.mnemonic", new Character('d') },

			{ "openBKG", "Load ambiental BKG spectrum from database..." },
			{ "openBKG.toolTip",
					"Setting ambiental background spectrum for further calculations!" },
			{ "openBKG.mnemonic", new Character('L') },

			{ "saveFile", "Save spectrum in database..." },
			{ "saveFile.toolTip", "Save a valid spectrum in database!" },
			{ "saveFile.mnemonic", new Character('S') },
			
			{ "loadROI", "Load ROIs..." },
			{ "loadROI.toolTip", "Load ROIs from ROI file" },
			{ "loadROI.mnemonic", new Character('a') },
			
			{ "saveROI", "Save ROIs..." },
			{ "saveROI.toolTip", "Save ROIs in ROI file" },
			{ "saveROI.mnemonic", new Character('S') },

			{ "menu.file.exit", "Close" },
			{ "menu.file.exit.mnemonic", new Character('C') },
			{ "menu.file.exit.toolTip", "Close the application" },

			{ "menu.help", "Help" },
			{ "menu.help.mnemonic", new Character('H') },

			{ "menu.help.about", "About..." },
			{ "menu.help.about.mnemonic", new Character('A') },
			{ "menu.help.about.toolTip",
					"Several informations about this application" },

			{ "menu.help.howTo", "How to..." },
			{ "menu.help.howTo.mnemonic", new Character('H') },
			{ "menu.help.howTo.toolTip",
					"Several hints and tips about this application" },

			{ "menu.help.LF", "Look and feel..." },
			{ "menu.help.LF.mnemonic", new Character('L') },
			{ "menu.help.LF.toolTip", "Change application look and feel" },

			{ "menu.tools", "Tools" },
			{ "menu.tools.mnemonic", new Character('T') },

			{ "menu.tools.library", "Nuclides library..." },
			{ "menu.tools.library.mnemonic", new Character('N') },
			{ "menu.tools.library.toolTip", "Setup the nuclides library" },

			{ "menu.tools.peakSearch", "Peak search..." },
			{ "menu.tools.peakSearch.mnemonic", new Character('s') },
			{ "menu.tools.peakSearch.toolTip", "Peak search automatic function" },

			{ "menu.tools.peakIdentify", "Peak identify..." },
			{ "menu.tools.peakIdentify.mnemonic", new Character('i') },
			{ "menu.tools.peakIdentify.toolTip",
					"Peak identify automatic function" },

			{ "menu.view", "View" },
			{ "menu.view.mnemonic", new Character('e') },

			{ "menu.show.bkg", "Show background spectrum" },
			{ "menu.show.bkg.mnemonic", new Character('b') },
			{ "menu.show.bkg.toolTip", "Toggle show background spectrum" },
			{ "menu.view.lin", "Lin. spectrum display" },
			{ "menu.view.lin.mnemonic", new Character('L') },
			{ "menu.view.lin.toolTip", "Linear spectrum display" },
			{ "menu.view.ln", "Ln. spectrum display" },
			{ "menu.view.ln.mnemonic", new Character('n') },
			{ "menu.view.ln.toolTip", "Logarithmic spectrum display" },
			{ "menu.view.sqrt", "Sqrt. spectrum display" },
			{ "menu.view.sqrt.mnemonic", new Character('q') },
			{ "menu.view.sqrt.toolTip", "Square root spectrum display" },

			{ "menu.calibration", "Calibration" },
			{ "menu.calibration.mnemonic", new Character('C') },

			{ "menu.calibration.EnFWHM", "Energy and FWHM calibration..." },
			{ "menu.calibration.EnFWHM.mnemonic", new Character('F') },
			{ "menu.calibration.EnFWHM.toolTip",
					"Energy and FWHM calibration based on calibration spectrum" },

			{ "menu.calibration.Eff", "Efficiency calibration..." },
			{ "menu.calibration.Eff.mnemonic", new Character('c') },
			{ "menu.calibration.Eff.toolTip",
					"Efficiency calibration of gamma detector, based on calibration spectrum" },

			{ "menu.gammaAnalysis", "Gamma analysis" },
			{ "menu.gammaAnalysis.mnemonic", new Character('G') },

			{ "menu.gammaAnalysis.sample", "Sample report..." },
			{ "menu.gammaAnalysis.sample.mnemonic", new Character('S') },
			{ "menu.gammaAnalysis.sample.toolTip",
					"Sample report based on computed ROIs!" },

			{ "status.wait", "Waiting your action!" },
			{ "status.computing", "Computing..." },
			{ "status.done", "Done! " },
			{ "status.save", "Saved: " },
			{ "status.load", "Loaded: " },
			{ "status.error", "Unexpected error!" },
			{ "status.openingSpectrum", "Opening spectrum..." },
			{ "status.openSpectrum", "Spectrum loaded!" },
			{ "status.db.init", "Initializing..." },
			{ "status.spectrum.load",
					"Spectrum loaded! This frame can be closed now." },
			{ "status.spectrum.delete", "Spectrum deleted!" },
			{ "status.spectrum.save",
					"Spectrum saved! This frame can be closed now." },
			{
					"status.spectrum.warning",
					"Warning: Channels are not sorted in ascending order or there are duplicates! Please verify input data!" },
			{
					"status.bkg.warning",
					"Warning: Background number of channels differs from main spectrum! Please verify!" },
			{ "status.bkg.roi.warning",
					"Warning: Background spectrum is changed! Please re-set all ROIs!" },
			{ "status.readingEfficiencyFile", "Analysing efficiencies..." },
			{ "status.efficienciesLoaded", "Efficiencies loaded!" },
			{ "status.coin.saved", "Coincidence data saved!" },

			{ "number.error", "Insert valid positive numbers! " },
			{ "number.error.title", "Unexpected error" },
			{ "number.error2",
					"Insert valid positive numbers! Date values must be integers!" },
			{ "number.error.title2", "Unexpected error" },
			{ "number.error.noSpectrum", "No spectrum to save! " },
			{ "number.error.save.title", "Saving spectrum error" },
			{ "number.error.noSpectrum2", "Load gamma spectrum first! " },
			{ "number.error.save.title2", "Peak search error" },
			{ "number.error3",
					"Please insert ROIs and assign them to a known nuclide (ROI set)! " },
			{ "number.error.title3", "Unexpected error" },
			{ "number.error4", "Insert valid positive numbers for activities! " },
			{ "number.error.title4", "Unexpected error" },
			{ "number.error5", "At least 2 calibration points are required! " },//not used after 0.4 update. one-point calibration allowed!
			{ "number.error.title5", "Unexpected error" },
			
			/////////////
			{ "number.error55", "At least 1 calibration point is required! " },
			///////////////

			
			{
					"number.error6",
					"Crossover energy is not properly set..there are maximum 1 point at each energy interval! " },
			{ "number.error.title6", "Unexpected error" },
			{ "number.error7", "Duplicate ROIs (same energy) not allowed! " },
			{ "number.error.title7", "Unexpected error" },

			{ "number.error.useBKG.noSpectrum",
					"Please load main spectrum first! " },
			{ "number.error.useBKG.noSpectrum.title", "Error" },
			{ "number.error.useBKG",
					"Insert valid positive number for main spectrum Live Time (for BKG scaling!)" },
			{ "number.error.useBKG.title", "Unexpected error" },

			{ "number.error.roiInsert.check",
					"Spectrum live time was changed. No ROI can be inserted." },
			{ "number.error.roiInsert",
					"Insert valid positive number for main spectrum Live Time!)" },
			{ "number.error.roiInsert.title", "Unexpected error" },

			{ "dialog.coin.nodata.title", "Information:" },
			{ "dialog.coin.nodata.message",
					"Coincidence not found, nothing to save!" },
			{ "dialog.coin.overwrite.title", "Information:" },
			{ "dialog.coin.overwrite.message",
					"First delete data in order to save the new one!" },

			{ "dialog.view.roi.set.title", "Information:" },
			{ "dialog.view.roi.set.message", "Please set the ROI first!" },

			{ "dialog.cal.changed.title", "Information:" },
			{ "dialog.cal.changed.message",
					"Calibration is changed! Please re-set all ROIs!" },
			{ "dialog.cal.deleted.title", "Information:" },
			{ "dialog.cal.deleted.message",
					"In-use calibration was deleted! Please calibrate!" },

			{ "viewRoi.title", "Information:" },
			{
					"viewRoi.message",
					"This cannot work on ROIs imported from database. In order to be viewed, ROIs must be set." },

			{ "dialog.resetROI.title", "Confirm..." },
			{ "dialog.resetROI.message",
					"Do you want to try resetting all ROIs automatically?" },
			{ "dialog.resetROI.buttons", new Object[] { "Yes", "No" } },

			{ "dialog.exit.title", "Confirm..." },
			{ "dialog.exit.message", "Are you sure?" },
			{ "dialog.exit.buttons", new Object[] { "Yes", "No" } },
			{ "dialog.spectrum.title", "Spectrum error..." },
			{
					"dialog.spectrum.message",
					"Invalid spectrum file.\nA valid file must contain ONE column (pulses) of numbers OR\nTWO columns (channel and pulses) numbers!\nInvalid number: " },

			{ "dialog.effFile.title", "Efficiency file error..." },
			{
					"dialog.effFile.message",
					"Invalid efficiency file.\nA valid file must contain THREE columns (energy[keV], peakEff[%], totalEff[%]) of numbers!\nInvalid number: " },

			{ "dialog.coin.title", "Error..." },
			{ "dialog.coin.message", "Load detector efficiencies first!" },

			{ "library.master.db", "ICRP38" },
			{ "library.master.db.indexTable", "ICRP38Index" },
			{ "library.master.db.radTable", "icrp38Rad" },

			{ "library.master.jaeri.db", "JAERI03" },
			{ "library.master.jaeri.db.indexTable", "JAERI03index" },
			{ "library.master.jaeri.db.radTable", "jaeri03Rad" },

			{ "main.db", "Gamma" },
			{ "main.db.gammaNuclidesTable", "GammaNuclides" },
			{ "main.db.gammaNuclidesDetailsTable", "GammaNuclidesDetails" },
			{ "main.db.gammaNuclidesDecayChainTable", "GammaNuclidesDecayChain" },// !!!!!!!
			{ "main.db.gammaNuclidesCoincidenceTable",
					"GammaNuclidesCoincidence" },// !!!!!!!

			{ "main.db.gammaGlobalEfficiencyTable",
					"GammaGlobalEfficiencyCalibration" },

			{ "main.db.bkgTable", "GammaBackground" },
			{ "main.db.bkgTable.spectrum", "GammaBackgroundSpectrum" },
			{ "main.db.bkgTable.rois", "GammaBackgroundRois" },
			{ "main.db.standardSourceTable", "GammaStandardSource" },
			{ "main.db.standardSourceTable.spectrum",
					"GammaStandardSourceSpectrum" },
			{ "main.db.standardSourceTable.rois", "GammaStandardSourceRois" },
			{ "main.db.sampleTable", "GammaSample" },
			{ "main.db.sampleTable.spectrum", "GammaSampleSpectrum" },
			{ "main.db.sampleTable.rois", "GammaSampleRois" },
			{ "main.db.fwhmCalib", "GammaFWHMCalibration" },
			{ "main.db.effCalib", "GammaEfficiencyCalibration" },
			// { "main.db.global.effCalib", "GammaGlobalEfficiencyCalibration"
			// },
			{ "main.db.enCalib", "GammaEnergyCalibration" },

			{ "library.master.dbCb", new String[] { "ICRP38", "JAERI03" } },
			{ "library.master.dbLabel", "Database: " },
			{ "library.master.border", "Master library" },
			{ "library.master.nuclide.label", "Nuclide: " },
			{ "library.master.nuclide.button", "Retrieve nuclide informations" },
			{ "library.master.nuclide.button.mnemonic", new Character('R') },
			{ "library.master.nuclide.button.toolTip",
					"Retrieve nuclide informations from master database [based on ICRP38 report]" },

			{ "library.copy.button", "Copy" },
			{ "library.copy.mnemonic", new Character('C') },
			{ "library.copy.toolTip",
					"Copy selected nuclide into the runtime(in-use) library" },

			{ "library.view.button", "View chain..." },// "View decay chain..."
														// },
			{ "library.view.mnemonic", new Character('V') },
			{ "library.view.toolTip", "View decay chain informations!" },

			{ "library.inuse.border", "In-use library" },
			{ "stop.button", "Stop computations" },
			{ "stop.button.mnemonic", new Character('S') },
			{ "stop.button.toolTip", "Stops all calculations!" },
			{ "text.simulation.stop", "Interrupred by user!" },
			
			{ "roi.load", "roi" },
			{ "roi.load.description", "ROI file" },
			{ "data.load", "Data" },
			{ "sort.by", "Sort by: " },
			{ "records.count", "Records count: " },
			
			{ "data.eff.load", "CoincidenceData" },
			{ "data.eff.coin.load", "Nuclides" },
			{ "library.threshold.border", "Thresholds" },
			{ "library.threshold.all", "All photons" },
			{ "library.threshold.cut1", "Cutoff rad. yield <1%" },
			{ "library.threshold.cut2", "Cutoff rad. yield <2%" },
			{ "library.threshold.cut5", "Cutoff rad. yield <5%" },
			{ "library.threshold.energy", "Cutoff radiation energies <20keV" },
			{ "library.threshold.energy.value", new Double(20.0) },// keV

			// { "text.radiation.type", "Radiation: " },
			{ "text.radiation.type", "Rad.: " },
			{ "radiation.x", "X" },
			{ "radiation.gamma", "Gamma" },
			{ "radiation.annihilationQuanta", "Annih." },
			{ "text.radiation.yield", "Yield: " },
			{ "text.radiation.kev", "Energy[keV]: " },
			{ "text.radiation.from", "Radiation from: " },

			{ "text.chainDecayInfo", "Chain decay informations: " },
			// { "text.nuclide", "Nuclide: " },
			{ "text.nuclide", "Nuc.: " },
			{ "text.halfLife", "Half life: " },
			{ "text.branchingRatio", "BR: " },
			{ "text.atomicMass", "AMass: " },
			{ "text.activitySecular", "Asecular " },
			{ "text.activitySample", "Asample " },
			{ "text.activityEquilibrum", "Aequilibrum " },
			{ "text.decayCorr", "DecayCorr: " },
			{ "text.corr.type", "Decay correction type: " },
			{ "db.equilibrum.user", "user defined" },
			{ "db.equilibrum.secular", "secular" },
			{ "db.equilibrum.sample", "sampleTime_" },
			{ "db.equilibrum.default", "equilibrum_default" },

			{ "library.sampleTimeLb", "Sample time: " },
			{ "library.sampleTimeUnitLb", "units: " },
			{ "library.a_secRb", "Use secular equilibrum" },
			{ "library.a_afterRb", "Use sample time" },
			{ "library.a_eqRb", "Use default equilibrum" },
			{ "library.a_secRb.toolTip",
					"Use secular equilibrum for sample parent-daugthers chains" },
			{
					"library.a_afterRb.toolTip",
					"Use sample time for sample parent-daugthers chains. At time 0, only parent activity (always norm to 1.0) is considered!" },
			{
					"library.a_eqRb.toolTip",
					"Use equilibrum at 10 x parent half life for sample parent-daugthers chains. At time 0, only parent activity (always norm to 1.0) is considered!" },
			{ "library.decayCorr.border",
					"Master library: Parent-daughter decay correction" },

			{ "library.inuse.radTypeLb", "Rad. type: " },
			{ "library.inuse.nuclideLb", "Nuclide: " },
			{ "library.inuse.atomicMassLb", "Atomic mass: " },
			{ "library.inuse.halfLifeLb", "Half life: " },
			{ "library.inuse.halfLifeUnitsLb", "Half life units: " },
			{ "library.inuse.hluCb",
					new String[] { "y", "d", "h", "m", "s", "ms", "us" } },
			{ "library.inuse.main.add.button", "Coincidence..." },// "Add"
																	// },//"Coin. corr..."
																	// },//
																	// "Add" },
			{ "library.inuse.main.add.button.mnemonic", new Character('o') },
			{ "library.inuse.main.add.button.toolTip",
					"Coincidence correction..." },// "Insert main nuclide data."
													// },
			{ "library.inuse.main.delete.button", "Delete" },
			{ "library.inuse.main.delete.button.mnemonic", new Character('e') },
			{ "library.inuse.main.delete.button.toolTip",
					"Delete selected data." },

			{ "library.inuse.secondary.add.button", "Add" },
			{ "library.inuse.secondary.add.button.mnemonic", new Character('d') },
			{ "library.inuse.secondary.add.button.toolTip",
					"Insert secondary nuclide data." },
			{ "library.inuse.secondary.delete.button", "Delete" },
			{ "library.inuse.secondary.delete.button.mnemonic",
					new Character('l') },
			{ "library.inuse.secondary.delete.button.toolTip",
					"Delete selected data." },

			{ "library.inuse.energyLb", "Energy [keV]: " },
			{ "library.inuse.yieldLb", "Yield: " },
			{ "library.inuse.notesLb", "Nuclide: " },
			{ "library.inuse.decayCorrLb", "Decay corr.: " },

			{ "roiSet.frame.label", "ROI assigned to: " },
			{ "roiSet.frame.set", "ROI set" },
			{
					"roiSet.frame.set.toolTip",
					"ROI assignment to nuclide. This is the final operation on this particular ROI!" },
			{ "roiSet.frame.set.mnemonic", new Character('s') },

			{ "coin.frame.efficiencyLb",
					"Load coincidence-free efficiencies (theoretical)" },
			{ "coin.frame.load.button", "Load..." },
			{ "coin.frame.load.button.mnemonic", new Character('L') },
			{ "coin.frame.load.button.toolTip", "Load efficiency data table." },
			{ "coin.frame.compute.button", "Compute" },
			{ "coin.frame.compute.button.mnemonic", new Character('C') },
			{ "coin.frame.compute.button.toolTip",
					"Compute coincidence corection" },
			{ "coin.frame.save.button", "Save" },
			{ "coin.frame.save.button.mnemonic", new Character('S') },
			{ "coin.frame.save.button.toolTip", "Save coincidence corection" },
			{ "coin.frame.delete.button", "Delete" },
			{ "coin.frame.delete.button.mnemonic", new Character('D') },
			{ "coin.frame.delete.button.toolTip",
					"Delete data from coincidence corection table" },

			{ "coin.file.BM", "_BM.idt" },
			{ "coin.file.EC", "_EC.idt" },
			{ "coin.file.AL", "_AL.idt" },
			{ "coin.file.IT", "_IT.idt" },
			{ "coin.threshold.yield.value", new Double(1.0) },// 1% yield
																// radiations!!
			{ "coin.useXray", "Use gamma-Xray coincidence" },
			{ "coin.text.energy", "Energy[keV]= " },
			{ "coin.text.summout", "SUMMOUT corr.= " },
			{ "coin.text.summin", "SUMMIN corr.= " },
			{ "coin.text.coincidence", "COINCIDENCE corr.= " },

			{ "rb.bkg", "Background database" },
			{ "rb.standardSource", "Standard source database" },
			{ "rb.sample", "Sample database" },
			{ "database.delete.button", "Delete" },
			{ "database.delete.button.mnemonic", new Character('D') },
			{ "database.delete.button.toolTip", "Delete spectrum from database" },
			{ "database.overwrite.checkbox", "Overwrite selected spectrum" },
			{ "database.save.button", "Save spectrum" },
			{ "database.save.button.mnemonic", new Character('S') },
			{ "database.save.button.toolTip", "Save spectrum in database" },
			{ "database.load.button", "Load spectrum" },
			{ "database.load.button.mnemonic", new Character('L') },
			{ "database.load.button.toolTip", "Load spectrum from database" },
			{ "database.useBkg.button", "Use as BKG" },
			{ "database.useBkg.button.mnemonic", new Character('U') },
			{ "database.useBkg.button.toolTip",
					"Use spectrum for ambiental background subtraction" },
			{ "database.noBkg.button", "No BKG" },
			{ "database.noBkg.button.mnemonic", new Character('N') },
			{ "database.noBkg.button.toolTip",
					"No ambiental background subtraction" },
			{ "database.selection.border", "Database selection" },
			{ "database.tasks.border", "Database tasks" },
			{ "status.bkg.display", "Background data displayed." },
			{ "status.standardSource.display",
					"Standard source data displayed." },
			{ "status.sample.display", "Sample data displayed." },

			{ "difference.yes", "Yes" },
			{ "difference.no", "No" },

			{ "HowTo.title", "Hints and tips" },
			{
					"HowTo",
					"This software is dedicated to physicists involved in gamma spectroscopy and unlike commercial\n"
							+ "softwares such as Assayer, Gamma2000, Gamma Vision,... is completely free. In addition, it is\n"
							+ "designed to be AS SIMPLE AND EFFICIENT AS possible. \n\n"
							+ "First thing to do is open a gamma spectrum. This can be done by opening a spectrum file \n"
							+ "or by opening a previously saved spectrum. Choose File menu and select an Open command.\n"
							+ "All GUI (graphical user interface) controls are straightforward. Once a spectrum is\n"
							+ "opened, do not immediately move the mouse over the chart in order to read possible warnings.\n"
							+ "By moving mouse over the chart, status bar will display the x (channel or keV) value and \n"
							+ "the y (pulses or LN(pulses) or SQRT(pulses)) value.\n"
							+ "The spectrum file, which is usualy generated by the gamma aquisition software, must be \n"
							+ "modified (if it is not already) TO CONTAIN only two columns of data (channel and pulses) \n"
							+ "separated by white space characters (i.e. space or tab) OR TO CONTAIN only one column of data\n"
							+ "(pulses). No other data these spectrum files must contain. Spectrum live time or measurement \n"
							+ "date must be recorded somewhere and inserted in the coresponding GUI fields. In addition, make\n"
							+ "sure the CHANNELS are in ASCENDING ORDER and contain NO DUPLICATES. Once a spectrum is \n"
							+ "loaded, it is displayed on the chart.\n\n"
							+ "Second thing to do is to set a proper in-use gamma library via Tools menu->Library.\n"
							+ "Simply select a parent from master library, retrieve decay information and copy to gamma\n"
							+ "database. Of course, you can manually enter the required data if allowed.\n"
							+ "Here, you can choose the desire parent-daughter decay CORRECTION which is based on the sample\n"
							+ "time. Default option for equilibrum is considered appropriate for any purpose. Remarks:\n"
							+ "the secular equilibrum may never be reached or exact sample time is unknown in most cases.\n"
							+ "Also, the gamma-gamma and/or gamma-XRay true coincidence CORRECTION can be computed here.\n"
							+ "Since alpha efficiency and beta effciciency are basicly ZERO for gamma detectors, the \n"
							+ "coincidences are handled only for gamma-gamma and gamma-XRay cascades. The annihilation \n"
							+ "quanta (from beta plus decay) has negligible effect in most cases. Based on a quite accurate\n"
							+ "database (.idt files), the true coincidence is computed for any energy from any daughter nuclide\n"
							+ "in the radioactive chain. For simplicity, it is used the following approximation: the X-ray \n"
							+ "are emitted only during an EC (electron capure) decay and all X-rays are contribute to the main\n"
							+ "gamma-gamma true coincidence. The angular distribution is already taken into account if the\n"
							+ "GES_MC theoretical efficiencies were used. See files inside the template folder!\n"
							+ "Nuclide interference CORRECTION is not a concern here since these interferences affect mainly\n"
							+ "Compton background which is auto-corrected by subtraction (e.g. Compton counts from 1460 keV\n"
							+ "are present in Compton BKG everywhere). The true nuclide interferences reffer to multiple lines\n"
							+ "in a ROI causing the ROI to be asymmetrical. Again, there is no concern since the user has the\n"
							+ "possibility to choose the peak (few channels near the max value) and its edge separately \n"
							+ "(wide) and then performing a gaussian fit (DECONVOLUTION) for that particular peak (ROI).\n"
							+ "The sample measurement time (spectrum live time) CORRECTION can be performed after all ROIs are\n"
							+ "set! Final sample report contains the corrected activity before sample measurement!\n"
							+ "Also, in certain condition (there is only one nuclide in spectrum and global efficiency \n"
							+ "for that nuclide exists -computed if only one nuclide exists in standard source spectrum) \n"
							+ "the sample report contain the gamma global activities! \n\n"
							+ "In almost any table, you can double-click on table header (any header) to sort data! \n\n"
							+ "Click on chart to set markers and than insert gamma ROIs (Region of Interest). Sometimes might \n"
							+ "happens that chart markers cannot be set, in this case, try to click SLOWLY! Holding CTRL \n"
							+ "and any mouse button, you can move the chart by moving the mouse. Right click on the chart to \n"
							+ "copy, save or print the chart image. Press refresh button to update the chart title according to\n"
							+ "the spectrum name field. Use mouse wheel as well as left click and drag an area to zoom.\n\n"
							+ "To better view the possible ROI candidates, use different view option from view menu, such as\n"
							+ "LN view or better the SQRT view.\n\n"
							+ "Once all desired ROIs are inserted they can be adjusted by updating their edge. This operation\n"
							+ "is usefull when we have multiple peaks and we want to perform a deconvolution via \n"
							+ "Gaussian fit operation. Just choose a common (large) edge for all peaks inside, then set the\n"
							+ "net area calculation method to gaussian fit for all these ROIs.\n"
							+ "Before all ROI insertions, it is advisable to load an ambiental background (BKG) spectrum via \n"
							+ "File menu. The ROI calculation is done by subtracting the BKG spectrum.\n"
							+ "The roi assignment is done after a nuclide is set to the current ROI via ROI set command.\n"
							+ "If in ROI falls multiple radiation energies, all corresponding yields are taken into account.\n\n"
							+ "Other important initial operations are the calibrations: energy, FWHM and efficiency. These can\n"
							+ "be performed via Calibration menu. Rule of thumb: Do not use peaks (ROIs) which \n"
							+ "correspond to X-rays. They are not coincidence corrected and present multiple lines. \n"
							+ "When possible, avoid multiplets! Of course, strictly for energy calibration user can choose\n"
							+ "any peaks he wants (if its centroid energy is known)!\n"
							+ "Net area/FWHM calculation mode can be done in 3 different ways: 1. default, all computatations\n"
							+ "are done using channel by channel methods (pulse summing and looking for FWHM); 2. germanium mode,\n"
							+ "where net area is computed same as above (channel by channel pulses sum) but FWHM is evaluated\n"
							+ "using an analytical gaussian fit method using 3 points (ROI edges and ROI maximum -peak);\n"
							+ "3. gaussian fit, where both net area and FWHM are computed by gaussian fit using ALL ROI points.\n"
							+ "MDA calculation modes are straightforward: It can be done using Pasternack theory or Curie theory\n"
							+ "or an accurate default one. More information can be obtain from specific literature."
							+ "\n\n"
							+ "Users can perform the automated functions of peak search/peak identify to auto-set ROI.s\n"
							+ "These tasks require an accurate nuclide library as well as good energy/FWHM and efficiency\n"
							+ " calibrations. We always recommend to do such tasks manualy!\n\n"
							+ "This software was developped (and tested) on LINUX (Linux Mint 11 Katya) and WINDOWS \n"
							+ "(Windows 7). Due to the Java portability, NO major glitches for running under MAC OS are expected!" },

			{ "results.sample.NA", "N/A" },
			{ "results.sample.NAOR", "N/A or main spectrum is loaded from DB (BKG link not tracked)" },
			{ "results.spectrum.name", "Spectrum name: " },
			{ "results.spectrum.bkg.name", "Ambient background spectrum name: " },
			{ "results.spectrum.date", "Date: " },
			//{ "results.spectrum.bkg.date", "Date: " },
			{ "results.spectrum.time", "Aquisition live time [s]: " },
			//{ "results.spectrum.time", "Aquisition live time [s]: " },
			{ "results.sample.quanity", "Sample quantity: " },
			{ "results.cal.en", "In-use Energy calibration name (not tracked): " },
			{ "results.cal.fwhm", "In-use FWHM calibration name (not tracked): " },
			{ "results.cal.eff", "In-use Efficiency calibration name (not tracked): " },
			
			{ "results.title", "RESULTS:" },
			{ "results.roiID", "ROI ID: " },
			{ "results.roiNuclide", "Nuclide: " },
			{ "results.roiNuclide.at", " @ " },
			{ "results.roiNuclide.keV", " keV " },
			{ "results.startChannel", "Start channel: " },
			{ "results.startEnergy", "Start energy [keV]: " },
			{ "results.startEdgeChannel", "Start edge channel: " },
			{ "results.startEdgeEnergy", "Start edge energy [keV]: " },
			{ "results.centerChannel", "Center channel: " },
			{ "results.centerEnergy", "Center energy [keV]: " },
			{ "results.centroidChannel", "Centroid channel: " },
			{ "results.centroidEnergy", "Centroid energy [keV]: " },
			{ "results.peakChannel", "Peak channel: " },
			{ "results.peakEnergy", "Peak energy [keV]: " },
			{ "results.peakPulses", "Peak pulses: " },
			{ "results.endChannel", "End channel: " },
			{ "results.endEnergy", "End energy [keV]: " },
			{ "results.endEdgeChannel", "End edge channel: " },
			{ "results.endEdgeEnergy", "End edge energy [keV]: " },
			{ "results.fwhmChannel", "FWHM channel: " },
			{ "results.+-", " +/- " },
			{ "results.+-.1sigma", " +/- (1 sigma) " },
			{ "results.fwhmEnergy", "FWHM energy [keV]: " },
			{ "results.fwhmEnergyCalib", "FWHM energy from calibration [keV]: " },
			{ "results.resolution", "Resolution [%]: " },
			{ "results.resolutionCalib", "Resolution from calibration [%]: " },
			{ "results.significance", "Difference?: " },
			{ "results.bkgCounts", "Ambiental background counts [pulses]: " },
			{ "results.bkgCountsRate",
					"Ambiental background counts rate [pulses/sec]: " },
			{ "results.grossCounts", "Gross counts [pulses]: " },
			{ "results.grossCountsRate", "Gross counts rate [pulses/sec]: " },
			{ "results.comptonCounts",
					"Compton (continous background) counts [pulses]: " },
			{ "results.comptonCountsRate",
					"Compton (continous background) counts rate [pulses/sec]: " },
			{ "results.netCounts", "Net counts [pulses]: " },
			{ "results.netCountsRate", "Net counts rate [pulses/sec]: " },
			{ "results.confidenceLevel", "Confidence level [%]: " },
			{ "results.yield", "Yield: " },
			{ "results.atomicMass", "Atomic mass: " },
			{ "results.halfLife", "Half life: " },
			{ "results.netCalculationMethod", "Net/FWHM calculation method: " },
			{ "results.mdaCalculationMethod", "MDA calculation method: " },
			{ "results.detectionLimit", "Detection limit [pulses/sec]: " },
			{ "results.efficiency", "Efficiency [%]: " },
			{ "results.activity", "Activity [Bq]: " },
			{ "results.mda", "MDA [Bq]: " },
			
			{ "results.global.gross.countsrate", "Global gross counts rate [pulses/sec] = " },
			{ "results.global.bkg.countsrate", "Global background counts rate [pulses/sec] = " },
			{ "results.global.net.countsrate", "Global net counts rate [pulses/sec] = " },
			// extension ratio!!
			{
					"results.unc.info",
					"Uncertainties presented here (with explicit few exceptions) are EXTENDED uncertainties (standard confidence level = 95%)! " },
			{ "results.unc.roi.info",
					"Uncertainties presented here are standard uncertainties (1 sigma)! " },
			{ "results.activity.global", "Global activity [Bq]: " },
			{ "results.mda.global", "Global MDA [Bq]: " },
			{ "results.equivalent.global", " equivalent: " },

			{ "results.difference",
					"Is activity, statistically, greater than MDA?: " },
			{ "results.activity.corr",
					"Activity at measurement start (corrected by live time) [Bq]: " },
			{ "results.mda.corr",
					"MDA at measurement start (corrected by live time) [Bq]: " },
			{ "results.mass", "Nuclide quantity [kg]: " },
			{ "results.conc.activity", "Activity concentration " },
			{ "results.conc.mda", "MDA concentration " },
			{ "results.conc", "Sample nuclide concentration [%]: " },
			{ "results.activity.uncorr",
					"Activity (mean during live time, uncorreted) [Bq]: " },
			{ "results.mda.uncorr",
					"MDA (mean during live time, uncorreted) [Bq]: " },

			{ "results.print", "PDF print..." },
			{ "results.print.toolTip", "Print report to a PDF file" },
			{ "results.print.html", "HTML print..." },
			{ "results.print.toolTip.html", "Print report to a HTML file" },
			{ "results.print.mnemonic", new Character('P') },

			{ "viewRoi.centroidChannel", "Centroid [channel]: " },
			{ "viewRoi.centroidEnergy", "Centroid [keV]: " },
			{ "viewRoi.nuclide", "Nuclide: " },

			{ "pdf.metadata.title", "GammaAnalysis PDF" },
			{ "pdf.metadata.subject", "Gamma analysis results" },
			{ "pdf.metadata.keywords", "GammaAnalysis, PDF" },
			{ "pdf.metadata.author", "GammaAnalysis" },
			{ "pdf.content.title", "GammaAnalysis Simulation Report" },
			{ "pdf.content.subtitle", "Report generated by: " },
			{ "pdf.page", "Page " },
			{ "pdf.header", "GammaAnalysis output" },
			{ "file.extension", "pdf" },
			{ "file.description", "PDF file" },
			{ "file.extension.html", "html" },
			{ "file.description.html", "HTML file" },
			{ "dialog.overwrite.title", "Overwriting..." },
			{ "dialog.overwrite.message", "Are you sure?" },
			{ "dialog.overwrite.buttons", new Object[] { "Yes", "No" } },

			{
					"energy.fwhm.calibration.columns",
					new String[] { "Id", "Centroid[channel]", "Energy[keV]",
							"Energy_Cal[keV]", "Diff[%]", "FWHM[channel]",
							"FWHM_Cal[channel]", "Diff[%]", "Use[?]" } },

			{ "energy.fwhm.polynomes", new String[] { "1", "2", "3" } },

			{ "energy.fwhm.calib.energyLb", "Energy[keV]: " },
			{ "energy.fwhm.calib.energyPoynomeLb",
					"Energy fit polynome order: " },
			{ "energy.fwhm.calib.fwhmPoynomeLb", "FWHM fit polynome order: " },

			{ "energy.fwhm.calib.channelLb", "Channel: " },
			{ "energy.fwhm.calib.currentCalibrationCh", "Use current cal." },

			{ "energy.fwhm.calib.insert.button", "Insert" },
			{ "energy.fwhm.calib.insert.button.mnemonic", new Character('I') },
			{ "energy.fwhm.calib.insert.button.toolTip",
					"Associate centroid with known energy!" },

			{ "energy.fwhm.calib.use.button", "Use" },
			{ "energy.fwhm.calib.use.button.mnemonic", new Character('U') },
			{ "energy.fwhm.calib.use.button.toolTip", "Used for calibration!" },

			{ "energy.fwhm.calib.remove.button", "Remove" },
			{ "energy.fwhm.calib.remove.button.mnemonic", new Character('R') },
			{ "energy.fwhm.calib.remove.button.toolTip",
					"Not used for calibration!" },

			{ "energy.fwhm.calib.calibrate.button", "Calibrate" },
			{ "energy.fwhm.calib.calibrate.button.mnemonic", new Character('C') },
			{ "energy.fwhm.calib.calibrate.button.toolTip",
					"Perform calibration!" },

			{ "energy.fwhm.calib.test.button", "Test" },
			{ "energy.fwhm.calib.test.button.mnemonic", new Character('T') },
			{ "energy.fwhm.calib.test.button.toolTip", "Test calibration!" },

			{ "energy.fwhm.calib.save.button", "Save/Set..." },
			{ "energy.fwhm.calib.save.button.mnemonic", new Character('S') },
			{ "energy.fwhm.calib.save.button.toolTip",
					"Save or set new calibration!" },

			{ "energy.fwhm.calib.chart.energy.NAME", "Energy calibration" },
			{ "energy.fwhm.calib.chart.fwhm.NAME", "FWHM calibration" },
			{ "energy.fwhm.calib.chart.data.NAME", "Raw data" },
			{ "energy.fwhm.calib.chart.fit.NAME", "Fit data" },
			{ "energy.fwhm.calib.chart.energy.y", "Energy[keV]" },
			{ "energy.fwhm.calib.chart.fwhm.y", "FWHM[channels]" },

			{ "energy.fwhm.calib.test.save.border",
					"Test, save or set calibrations" },

			{ "energy.fwhm.calib.textArea.energyCal",
					"Energy calibration coefficients: a3,a2,a1,a0" },
			{ "energy.fwhm.calib.textArea.fwhmCal",
					"FWHM calibration coefficients: a3,a2,a1,a0" },
			{ "energy.fwhm.calib.textArea.energyDiff",
					"Energy overall calibration deviation (overall procentual uncertainty %):" },
			{ "energy.fwhm.calib.textArea.fwhmDiff",
					"FWHM overall calibration deviation (overall procentual uncertainty %):" },

			{ "energy.fwhm.calib.textArea.testOld",
					"Energy [keV] based on in-use calibration: " },
			{ "energy.fwhm.calib.textArea.testOld2",
					"FWHM [channels] based on in-use calibration: " },
			{ "energy.fwhm.calib.textArea.testNew",
					"Energy [keV] based on new calibration: " },
			{ "energy.fwhm.calib.textArea.testNew2",
					"FWHM [channels] based on new calibration: " },

			{ "energy.fwhm.saveCal.energyCalNameLb",
					"Energy calibration name: " },
			{ "energy.fwhm.saveCal.fwhmCalNameLb", "FWHM calibration name: " },
			{ "energy.fwhm.saveCal.setLb", "Set?: " },

			{ "energy.fwhm.saveCal.saveEnergy.button",
					"Save energy calibration" },
			{ "energy.fwhm.saveCal.saveEnergy.button.mnemonic",
					new Character('a') },
			{ "energy.fwhm.saveCal.saveEnergy.button.toolTip",
					"Save energy calibration." },

			{ "energy.fwhm.saveCal.saveFWHM.button", "Save FWHM calibration" },
			{ "energy.fwhm.saveCal.saveFWHM.button.mnemonic",
					new Character('v') },
			{ "energy.fwhm.saveCal.saveFWHM.button.toolTip",
					"Save FWHM calibration." },

			{ "energy.fwhm.saveCal.setEnergy.button", "Set energy calibration" },
			{ "energy.fwhm.saveCal.setEnergy.button.mnemonic",
					new Character('e') },
			{ "energy.fwhm.saveCal.setEnergy.button.toolTip",
					"Set energy calibration." },

			{ "energy.fwhm.saveCal.setFWHM.button", "Set FWHM calibration" },
			{ "energy.fwhm.saveCal.setFWHM.button.mnemonic", new Character('t') },
			{ "energy.fwhm.saveCal.setFWHM.button.toolTip",
					"Set FWHM calibration." },

			{ "energy.fwhm.saveCal.deleteEnergy.button",
					"Delete energy calibration" },
			{ "energy.fwhm.saveCal.deleteEnergy.button.mnemonic",
					new Character('D') },
			{ "energy.fwhm.saveCal.deleteEnergy.button.toolTip",
					"Delete energy calibration." },

			{ "energy.fwhm.saveCal.deleteFWHM.button",
					"Delete FWHM calibration" },
			{ "energy.fwhm.saveCal.deleteFWHM.button.mnemonic",
					new Character('l') },
			{ "energy.fwhm.saveCal.deleteFWHM.button.toolTip",
					"Delete FWHM calibration." },

			{
					"eff.calibration.columns",
					new String[] { "Id", "Nuclide", "Energy[keV]", "Yields",
							"Net[pulses/s]", "Eff_calc[%]", "Unc",
							"Eff_calib[%]", "Diff[%]", "Use[?]" } },

			{ "eff.calibration.nuclides.columns",
					new String[] { "Id", "Nuclide", "Activity[Bq]", "Unc[%]" } },

			{
					"eff.calibration.eff.columns",
					new String[] { "Id", "Nuclide", "Energy[keV]", "Yield",
							"Net[pluses/sec]", "Unc[pulses/sec]",
							"Eff_calc[%]", "Unc", "Eff_calib[%]", "Diff[%]",
							"Use[?]" } },

			{ "eff.polynomes", new String[] { "1", "2", "3", "4" } },
			{ "eff.calib.chart.energy.NAME", "Efficiency calibration" },
			{ "eff.calib.chart.data.NAME", "Raw data" },
			{ "eff.calib.chart.fit.NAME", "Fit data" },
			{ "eff.calib.chart.energy.x", "Energy[keV]" },
			{ "eff.calib.chart.energy.y", "Efficiency[%]" },

			{ "eff.calib.calibrate.button", "Calibrate" },
			{ "eff.calib.calibrate.button.mnemonic", new Character('C') },
			{ "eff.calib.calibrate.button.toolTip",
					"Perform efficiency calibration!" },

			{ "eff.calib.add.button", "Add" },
			{ "eff.calib.add.button.mnemonic", new Character('A') },
			{ "eff.calib.add.button.toolTip", "Add nuclide activity data!" },

			{ "eff.calib.initialize.button", "Initialize" },
			{ "eff.calib.initialize.button.mnemonic", new Character('I') },
			{ "eff.calib.initialize.button.toolTip",
					"Compute ROI efficiencies!" },

			{ "eff.calib.set.button", "Use" },
			{ "eff.calib.set.button.mnemonic", new Character('U') },
			{ "eff.calib.set.button.toolTip",
					"Use this efficiency for calibration!" },

			{ "eff.calib.remove.button", "Remove" },
			{ "eff.calib.remove.button.mnemonic", new Character('R') },
			{ "eff.calib.remove.button.toolTip",
					"Do not use this efficiency for calibration!" },

			{ "eff.calib.test.button", "Test" },
			{ "eff.calib.test.button.mnemonic", new Character('T') },
			{ "eff.calib.test.button.toolTip", "Test this calibration!" },

			{ "eff.calib.save.button", "Save/Set..." },
			{ "eff.calib.save.button.mnemonic", new Character('S') },
			{ "eff.calib.save.button.toolTip", "Save calibration!" },

			{ "eff.calib.activityLb", "Activity[Bq]" },
			{ "eff.calib.errorLb", "Uncertainty[%]" },

			{ "eff.calib.activityDate.border", "Activity date" },
			{ "eff.calib.measurementDate.border", "Measurement date" },
			{ "eff.calib.test.save.border", "Test, save or set calibrations" },

			{ "eff.calib.dayLb", "Day:" },
			{ "eff.calib.monthLb", "Month:" },
			{ "eff.calib.yearLb", "Year:" },

			{ "eff.calib.crossoverEnergyLb", "Crossover energy [keV]: " },
			{ "eff.calib.poly1Lb", "1st polynome: " },
			{ "eff.calib.poly2Lb", "2nd polynome: " },
			{ "eff.calib.testLb", "Energy[keV]: " },

			{ "eff.calib.textArea.testOld",
					"Efficiency [%] based on in-use calibration: " },
			{ "eff.calib.textArea.testNew",
					"Efficiency [%] based on new calibration: " },
			{ "eff.calib.textArea.crossover", "Crossover energy: " },
			{ "eff.calib.textArea.effCal1",
					"Efficincy calibration coefficients: 1a4,1a3,1a2,1a1,1a0" },
			{ "eff.calib.textArea.effCal2",
					"Efficincy calibration coefficients: 2a4,2a3,2a2,2a1,2a0" },
			{ "eff.calib.textArea.effDiff",
					"Efficiency overall calibration deviation (overall procentual uncertainty %):" },
			{ "eff.calib.textArea.global", "Global efficiency[%]: " },

			{ "eff.saveCal.effCalNameLb", "Efficiency calibration name: " },
			{ "eff.saveCal.setLb", "Set?: " },

			{ "eff.saveCal.save.button", "Save efficiency calibration" },
			{ "eff.saveCal.save.button.mnemonic", new Character('a') },
			{ "eff.saveCal.save.button.toolTip", "Save efficiency calibration." },

			{ "eff.saveCal.set.button", "Set efficiency calibration" },
			{ "eff.saveCal.set.button.mnemonic", new Character('e') },
			{ "eff.saveCal.set.button.toolTip", "Set efficiency calibration." },

			{ "eff.saveCal.delete.button", "Delete efficiency calibration" },
			{ "eff.saveCal.delete.button.mnemonic", new Character('D') },
			{ "eff.saveCal.delete.button.toolTip",
					"Delete efficiency calibration." },

			{ "duplicates.database.nuclides",
					"Multiple nuclides with same name found in library! Please set ROIs manually!" },

			{ "eff.saveCal.global.can", "Global efficiency can be saved!" },
			{ "eff.saveCal.global.can.not",
					"Global efficiency can NOT be saved!" },
			{ "eff.saveCal.global.saved", "Global efficiency is saved!" },

			{ "peakSearch.button", "Peak search" },
			{ "peakSearch.button.mnemonic", new Character('P') },
			{ "peakSearch.button.toolTip", "Search for ROIs!" },

			{ "peakSearch.filterLb", "Filter: " },
			{ "peakSearch.filterCb",
					new String[] { "1st derivative", "2nd derivative" } },
			{ "peakSearch.smoothLb", "Smoothing (x): " },
			{
					"peakSearch.smoothCb",
					new String[] { // 25
					"5", "10", "15", "20", "25", "30", "35", "40", "45", "50",
							"55", "60", "65", "70", "75", "80", "85", "90",
							"95", "100" } },
			{ "peakSearch.widthLb", "Peak width factor (x stdev): " },
			{
					"peakSearch.widthCb",
					new String[] { // 2.0
					"1.0", "1.1", "1.2", "1.3", "1.4", "1.5", "1.6", "1.7",
							"1.8", "1.9", "2.0", "2.1", "2.2", "2.3", "2.4",
							"2.5", "2.6", "2.7", "2.8", "2.9", "3.0", "3.1",
							"3.2", "3.3", "3.4", "3.5", "3.6", "3.7", "3.8",
							"3.9", "4.0", "4.1", "4.2", "4.3", "4.4", "4.5",
							"4.6", "4.7", "4.8", "4.9", "5.0" } },
			{ "peakSearch.integralLb", "Integral: " },
			{
					"peakSearch.integralCb",
					new String[] { // 80
							// "10","15","20","25",
							"30", "35", "40", "45", "50", "55", "60", "65",
							"70", "75", "80", "85", "90", "95", "100", "105",
							"110", "115", "120", "125", "130", "150", "200",
							"250", "300", "500", "1000", "1500", "2000",
							"3000", "4000", "5000", "7000", "10000", "20000",
							"100000", "200000", "500000", "1000000" } },
			{ "peakSearch.powerLb", "Power: " },
			{
					"peakSearch.powerCb",
					new String[] { // 13 at d1 and 5 at d2
					"2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12",
							"13", "14", "15", "16", "17", "18", "19", "20",
							"21", "22", "23", "24", "25", "30", "50", "100",
							"200", "500", "700", "1200", "1800", "3200",
							"4500", "5800", "8000" } },
			{ "peakSearch.chart.NAME", "Original gamma spectrum" },
			{ "peakSearch.chart.smooth.NAME", "Smoothed gamma spectrum" },
			{ "peakSearch.chart.d1.NAME", "1st derivative" },
			{ "peakSearch.chart.d2.NAME", "2nd derivative" },
			{ "peakSearch.chart.data.NAME", "" },
			{ "peakSearch.chart.x", "channel" },
			{ "peakSearch.chart.y", "pulses" },

			{ "peakIdentify.fwhmDeltaRb", "FWHM-based energy window" },
			{ "peakIdentify.energyDeltaRb", "Energy-based energy window" },
			{ "peakIdentify.fwhmLb1", "DELTA E = A " },
			{ "peakIdentify.fwhmLb2", "x FWHM [keV]" },
			{ "peakIdentify.energyLb1", "DELTA E = A " },
			{ "peakIdentify.energyLb2", "+ B x" },
			{ "peakIdentify.energyLb3", "E [keV]" },

			{ "peakIdentify.idLb", "Configuration ID: " },

			{ "peakIdentify.identify.button", "Identify" },
			{ "peakIdentify.identify.button.mnemonic", new Character('I') },
			{ "peakIdentify.identify.button.toolTip",
					"Try peak identify based on in-use library and calibrations!" },
			{ "peakIdentify.set.button", "Set ROIs" },
			{ "peakIdentify.set.button.mnemonic", new Character('S') },
			{ "peakIdentify.set.button.toolTip",
					"Set and assign ROIs via configuration ID!" },

			{ "peakIdentify.text.id", "Config. ID: " },
			{ "peakIdentify.text.nuc", "Nuclide: " },
			{ "peakIdentify.text.corr", "Correlation: " },
			{ "peakIdentify.text.lines", "Lines found: " },
			{ "peakIdentify.text.rois", "ROIs ID: " },
			{ "peakIdentify.text.energies", "at energies: " },
			{ "peakIdentify.text.nuc.set", "Assigned nuclide: " },

	};

}
