/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

 */

/**
 * GeoGebra Application
 *
 * @author Markus Hohenwarter
 */
package geogebra.main;

import geogebra.GeoGebra;
import geogebra.euclidian.EuclidianController;
import geogebra.euclidian.EuclidianView;
import geogebra.gui.inputbar.AlgebraInput;
import geogebra.gui.util.ImageSelection;
import geogebra.io.MyXMLio;
import geogebra.kernel.AlgoElement;
import geogebra.kernel.ConstructionDefaults;
import geogebra.kernel.GeoElement;
import geogebra.kernel.Kernel;
import geogebra.kernel.Macro;
import geogebra.kernel.Relation;
import geogebra.plugin.GgbAPI;
import geogebra.plugin.PluginManager;
import geogebra.util.ImageManager;
import geogebra.util.LowerCaseDictionary;
import geogebra.util.Util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public abstract class Application implements KeyEventDispatcher {

	// disabled parts
	public static final boolean PRINT_DEBUG_MESSAGES = true;

	// license file
	public static final String LICENSE_FILE = "/geogebra/gui/_license.txt";
	
	// jar file names
	public final static String CAS_JAR_NAME = "geogebra_cas.jar";
	public static final String[] JAR_FILES = { 
			"geogebra.jar", 
			"geogebra_main.jar",
			"geogebra_gui.jar", 
			CAS_JAR_NAME, 
			"geogebra_export.jar",
			"geogebra_properties.jar" };
	
	// supported GUI languages (from properties files)
	public static ArrayList supportedLocales = new ArrayList();
	static {
		supportedLocales.add(new Locale("sq")); // Albanian
		
		supportedLocales.add(new Locale("ar")); // Arabic
		supportedLocales.add(new Locale("eu")); // Basque
		supportedLocales.add(new Locale("bs")); // Bosnian
		supportedLocales.add(new Locale("bg")); // Bulgarian
		supportedLocales.add(new Locale("ca")); // Catalan
		supportedLocales.add(new Locale("zh", "CN")); // Chinese (Simplified)
		supportedLocales.add(new Locale("zh", "TW")); // Chinese (Traditional)
		supportedLocales.add(new Locale("hr")); // Croatian
		supportedLocales.add(new Locale("cz")); // Czech
		supportedLocales.add(new Locale("da")); // Danish
		supportedLocales.add(new Locale("nl")); // Dutch
		supportedLocales.add(new Locale("en")); // English
		supportedLocales.add(new Locale("en", "GB")); // English (UK)
		supportedLocales.add(new Locale("en", "AU")); // English (Australia)
		supportedLocales.add(new Locale("et")); // Estonian
		supportedLocales.add(new Locale("fi")); // Finnish
		supportedLocales.add(new Locale("fr")); // French
		supportedLocales.add(new Locale("gl")); // Galician
		supportedLocales.add(new Locale("ka")); // Georgian
		supportedLocales.add(new Locale("de")); // German
		supportedLocales.add(new Locale("de", "AT")); // German (Austria)
		supportedLocales.add(new Locale("el")); // Greek
		// supportedLocales.add(new Locale("gu")); // Gujarati
		supportedLocales.add(new Locale("iw")); // Hebrew
		// supportedLocales.add(new Locale("hi")); // Hindi
		supportedLocales.add(new Locale("hu")); // Hungarian
		supportedLocales.add(new Locale("is")); // Icelandic
		supportedLocales.add(new Locale("in")); // Indonesian
		supportedLocales.add(new Locale("it")); // Italian
		supportedLocales.add(new Locale("ja")); // Japanese
		supportedLocales.add(new Locale("ko")); // Korean
		supportedLocales.add(new Locale("lt")); // Lithuanian
		supportedLocales.add(new Locale("mk")); // Macedonian
		supportedLocales.add(new Locale("mr")); // Marati
		// supportedLocales.add(new Locale("ne")); // Nepalese
		supportedLocales.add(new Locale("no", "NO")); // Norwegian (Bokmal)
		supportedLocales.add(new Locale("no", "NO", "NY")); // Norwegian(Nynorsk)
		// supportedLocales.add(new Locale("oc")); // Occitan
		supportedLocales.add(new Locale("fa")); // Persian
		supportedLocales.add(new Locale("pl")); // Polish
		supportedLocales.add(new Locale("pt", "BR")); // Portugese (Brazil)
		supportedLocales.add(new Locale("pt", "PT")); // Portuguese (Portugal)
		// supportedLocales.add(new Locale("pa")); // Punjabi
		supportedLocales.add(new Locale("ro")); // Romanian
		supportedLocales.add(new Locale("ru")); // Russian
		supportedLocales.add(new Locale("sr")); // Serbian
		supportedLocales.add(new Locale("si")); // Sinhala (Sri Lanka)
		
		supportedLocales.add(new Locale("sk")); // Slovakian
		supportedLocales.add(new Locale("sl")); // Slovenian
		supportedLocales.add(new Locale("es")); // Spanish
		supportedLocales.add(new Locale("sv")); // Swedish
		// supportedLocales.add(new Locale("ty")); // Tahitian
		supportedLocales.add(new Locale("ta")); // Tamil
		
		// supportedLocales.add(new Locale("te")); // Telugu
		supportedLocales.add(new Locale("th")); // Thai

		supportedLocales.add(new Locale("tr")); // Turkish
		// supportedLocales.add(new Locale("uk")); // Ukrainian
		// supportedLocales.add(new Locale("ur")); // Urdu
		supportedLocales.add(new Locale("vi")); // Vietnamese
		supportedLocales.add(new Locale("cy")); // Welsh

		// TODO: remove IS_PRE_RELEASE
		if (GeoGebra.IS_PRE_RELEASE)
			supportedLocales.add(new Locale("ji")); // Yiddish
	}

	// specialLanguageNames: Java does not show an English name for all
	// languages
	// supported by GeoGebra, so some language codes have to be treated
	// specially
	public static Hashtable specialLanguageNames = new Hashtable();
	static {
		specialLanguageNames.put("bs", "Bosnian");
		specialLanguageNames.put("zhCN", "Chinese Simplified");
		specialLanguageNames.put("zhTW", "Chinese Traditional");
		specialLanguageNames.put("cz", "Czech");
		specialLanguageNames.put("en", "English (US)");
		specialLanguageNames.put("enGB", "English (UK)");
		specialLanguageNames.put("enAU", "English (Australia)");
		specialLanguageNames.put("deAT", "German (Austria)");
		specialLanguageNames.put("gl", "Galician");
		specialLanguageNames.put("noNO", "Norwegian (Bokm\u00e5l)");
		specialLanguageNames.put("noNONY", "Norwegian (Nynorsk)");
		specialLanguageNames.put("ptBR", "Portuguese (Brazil)");
		specialLanguageNames.put("ptPT", "Portuguese (Portugal)");
		specialLanguageNames.put("si", "Sinhala"); // better than Sinhalese
	}

	public static final Color COLOR_SELECTION = new Color(230, 230, 245);

	// Font settings
	public static final int MIN_FONT_SIZE = 10;
	// currently used application fonts

	private int appFontSize;


	// file extension string
	public static final String FILE_EXT_GEOGEBRA = "ggb";
	// Added for Intergeo File Format (Yves Kreis) -->
	public static final String FILE_EXT_INTERGEO = "i2g";
	// <-- Added for Intergeo File Format (Yves Kreis)
	public static final String FILE_EXT_GEOGEBRA_TOOL = "ggt";
	public static final String FILE_EXT_PNG = "png";
	public static final String FILE_EXT_EPS = "eps";
	public static final String FILE_EXT_PDF = "pdf";
	public static final String FILE_EXT_EMF = "emf";
	public static final String FILE_EXT_SVG = "svg";
	public static final String FILE_EXT_HTML = "html";
	public static final String FILE_EXT_HTM = "htm";
	public static final String FILE_EXT_TEX = "tex";

	protected File currentPath, currentImagePath, currentFile = null;

	// page margin in cm
	public static final double PAGE_MARGIN_X = 1.8 * 72 / 2.54;
	public static final double PAGE_MARGIN_Y = 1.8 * 72 / 2.54;

	private static final String RB_MENU = "/geogebra/properties/menu";
	private static final String RB_COMMAND = "/geogebra/properties/command";
	private static final String RB_ERROR = "/geogebra/properties/error";
	private static final String RB_PLAIN = "/geogebra/properties/plain";
	public static final String RB_JAVA_UI = "/geogebra/properties/javaui";

	private static final String RB_SETTINGS = "/geogebra/export/settings";
	private static final String RB_ALGO2COMMAND = "/geogebra/kernel/algo2command";
	// Added for Intergeo File Format (Yves Kreis) -->
	private static final String RB_ALGO2INTERGEO = "/geogebra/kernel/algo2intergeo";
	// <-- Added for Intergeo File Format (Yves Kreis)

	// private static Color COLOR_STATUS_BACKGROUND = new Color(240, 240, 240);
	
	public static final int DEFAULT_ICON_SIZE = 32;

	private JFrame frame;
	private AppletImplementation appletImpl;
	private FontManager fontManager;

	protected GuiManager appGuiManager;
	private CasManager casView;

	private Component mainComp;
	private boolean isApplet = false;
	private boolean showResetIcon = false;
	public boolean runningInFrame = false; // don't want to show resetIcon if
											// running in Frame

	protected Kernel kernel;
	private MyXMLio myXMLio;

	protected EuclidianView euclidianView;
	private EuclidianController euclidianController;
	protected GeoElementSelectionListener currentSelectionListener;
	private GlobalKeyDispatcher globalKeyDispatcher;

	// For language specific settings
	private Locale currentLocale;
	private ResourceBundle rbmenu, rbcommand, rbcommandOld, rberror, rbplain,
			rbsettings;
	private ImageManager imageManager;
	private int maxIconSize = DEFAULT_ICON_SIZE;
	
	public boolean runningOnSugar = false;

	// Hashtable for translation of commands from
	// local language to internal name
	// key = local name, value = internal name
	private Hashtable translateCommandTable;
	// command dictionary
	private LowerCaseDictionary commandDict;

	private boolean INITING = false;
	protected boolean showAlgebraView = true;
	private boolean showAuxiliaryObjects = false;
	private boolean showAlgebraInput = true;
	private boolean showCmdList = true;
	protected boolean showToolBar = true;
	protected boolean showMenuBar = true;
	protected boolean showConsProtNavigation = false;
	private boolean[] showAxes = { true, true };
	private boolean showGrid = false;
	private boolean antialiasing = true;
	private boolean showSpreadsheet = false;
	private boolean printScaleString = false;
	private int labelingStyle = ConstructionDefaults.LABEL_VISIBLE_AUTOMATIC;

	private boolean rightClickEnabled = true;
	private boolean chooserPopupsEnabled = true;
	private boolean labelDragsEnabled = true;
	private boolean shiftDragZoomEnabled = true;
	private boolean isErrorDialogsActive = true;
	private boolean isErrorDialogShowing = false;
	private boolean isOnTheFlyPointCreationActive = true;

	private static LinkedList fileList = new LinkedList();
	private boolean isSaved = true;
	protected JPanel centerPanel;

	protected JSplitPane sp;
	private JSplitPane sp2;
	private DividerChangeListener spChangeListener;
	protected int initSplitDividerLocationHOR2 = 250; // init value
	protected int initSplitDividerLocationVER2 = 300; // init value
	protected int initSplitDividerLocationHOR = 650; // init value
	protected int initSplitDividerLocationVER = 400; // init value
	protected boolean horizontalSplit = true; // 

	private ArrayList selectedGeos = new ArrayList();
	private GgbAPI ggbapi = null;
	private PluginManager pluginmanager = null;

	public Application(String[] args, JFrame frame, boolean undoActive) {
		this(args, frame, null, null, undoActive);
	}

	public Application(String[] args, AppletImplementation appletImpl,
			boolean undoActive) {
		this(args, null, appletImpl, null, undoActive);
	}

	public Application(String[] args, Container comp, boolean undoActive) {
		this(args, null, null, comp, undoActive);
	}

	protected Application(String[] args, JFrame frame,
			AppletImplementation appletImpl, Container comp, boolean undoActive) {

		/*
		 * if (args != null) { for (int i=0; i < args.length; i++) {
		 * Application.debug("argument " + i + ": " + args[i]);
		 * JOptionPane.showConfirmDialog( null, "argument " + i + ": " +
		 * args[i], "Arguments", JOptionPane.DEFAULT_OPTION,
		 * JOptionPane.PLAIN_MESSAGE); } }
		 */

		// Michael Borcherds 2008-05-05
		// added to help debug applets
		System.out.println("GeoGebra " + GeoGebra.VERSION_STRING + ", "
				+ GeoGebra.BUILD_DATE + ", Java "
				+ System.getProperty("java.version"));

		isApplet = appletImpl != null;
		JApplet applet = null;
		if (frame != null) {
			mainComp = frame;
		} else if (isApplet) {
			applet = appletImpl.getJApplet();
			mainComp = applet;
			setApplet(appletImpl);
		} else {
			mainComp = comp;
		}
		
		// init codebase
		initCodeBase();

		// needed for JavaScript getCommandName(), getValueString() to work
		// (security problem running non-locally)
		if (isApplet) {
			AlgoElement.initAlgo2CommandBundle(this);
			// needs command.properties in main.jar
			// causes problems when not in English
			// initCommandBundle();
		}

		fontManager = new FontManager();
		imageManager = new ImageManager(mainComp);
	
		// init locale
		setLocale(mainComp.getLocale());
		
		// init kernel
		initKernel();
		// kernel = new Kernel(this); //ggb3D 2008-10-26 : in Application3D,
		// changed for Kernel3D
		kernel.setPrintDecimals(Kernel.STANDARD_PRINT_DECIMALS);
		
		// init xml io for construction loading
		myXMLio = new MyXMLio(kernel, kernel.getConstruction());

		// init euclidian view
		euclidianController = new EuclidianController(kernel);
		euclidianView = new EuclidianView(euclidianController, showAxes,
				showGrid);
		euclidianView.setAntialiasing(antialiasing);

		// set frame
		if (!isApplet && frame != null) {
			setFrame(frame);
		}
		
		// load file on startup and set fonts
		// INITING: to avoid multiple calls of setLabels() and
		// updateContentPane()
		INITING = true;
		setFontSize(12);
				
		if (!isApplet) {
			// init preferences
			GeoGebraPreferences.getPref().initDefaultXML(this);
		}

		// open file given by startup parameter
		boolean fileLoaded = handleFileArg(args);

		if (!isApplet) {
			// load XML preferences
			currentPath = GeoGebraPreferences.getPref().getDefaultFilePath();
			currentImagePath = GeoGebraPreferences.getPref()
					.getDefaultImagePath();
			if (!fileLoaded)
				GeoGebraPreferences.getPref().loadXMLPreferences(this);
		}

		setUndoActive(undoActive);
		
		// applet/command line options like file loading on startup
		handleOptionArgs(args); 
		
		INITING = false;

		// for key listening
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
				.addKeyEventDispatcher(this);

		// Mathieu Blossier - place for code to test 3D packages

		// init plugin manager for applications
		if (!isApplet)
			pluginmanager = getPluginManager();
		
		isSaved = true;
	}

	public void initKernel() {
		// Application.debug("initKernel() : Application");
		kernel = new Kernel(this);
	}

	/**
	 * Returns this application's GUI manager which is an instance of
	 * geogebra.gui.ApplicationGUImanager. Loads gui jar file and creates GUI
	 * manager if needed.
	 * 
	 * @return Object to avoid import geogebra.gui.ApplicationGUImanager in
	 *         Application. Note that the gui jar file may not be loaded at all
	 *         in applets.
	 */
	final public synchronized GuiManager getGuiManager() {
		if (appGuiManager == null) {
			appGuiManager = new geogebra.gui.DefaultGuiManager(Application.this);
			// // this code wraps the creation of the DefaultGuiManager and is
			// // necessary to allow dynamic loading of this class
			// ActionListener al = new ActionListener() {
			// public void actionPerformed(ActionEvent e) {
			// appGuiManager = new
			// geogebra.gui.DefaultGuiManager(Application.this);
			// }
			// };
			// al.actionPerformed(null);
		}

		return appGuiManager;
	}

	final public boolean hasGuiManager() {
		return appGuiManager != null;
	}
	
	final public JApplet getJApplet() {
		if (appletImpl == null)
			return null;
		else
			return appletImpl.getJApplet();
	}
	
	final public Font getBoldFont() {
		return fontManager.getBoldFont();
	}
	
	final public Font getPlainFont() {
		return fontManager.getPlainFont();
	}
	
	final public Font getSerifFont() {
		return fontManager.getSerifFont();
	}
	
	final public Font getSmallFont() {
		return fontManager.getSmallFont();
	}
	
	final public Font getFont(boolean serif, int style, int size) {
		String name = serif ? 
				fontManager.getSerifFont().getFontName() :
				fontManager.getPlainFont().getFontName();	
		return FontManager.getFont(name, style, size);
	}
	
	/**
	 * Returns a font that can display testString.
	 */
	public Font getFontCanDisplay(String testString) {
		return getFontCanDisplay(testString, false, Font.PLAIN, appFontSize);
	}	
	
	/**
	 * Returns a font that can display testString.
	 */
	public Font getFontCanDisplay(String testString, int fontStyle) {
		return getFontCanDisplay(testString, false, fontStyle, appFontSize);
	}
	
	/**
	 * Returns a font that can display testString.
	 */
	public Font getFontCanDisplay(String testString, boolean serif, int fontStyle, int fontSize) {
		return fontManager.getFontCanDisplay(testString, serif, fontStyle, fontSize);
	}

	

	// private static boolean initInBackground_first_time = true;

	public void setUnsaved() {
		isSaved = false;
	}

	public void setSaved() {
		isSaved = true;
	}

	public boolean isIniting() {
		return INITING;
	}
	
	public void fileNew() {
		// clear all 
		clearConstruction();
		
		// clear input bar
		if (hasGuiManager() && showAlgebraInput()) {
			AlgebraInput ai = (AlgebraInput)(getGuiManager().getAlgebraInput());
			ai.clear();
		}
		
		// reset spreadsheet columns, reset trace columns
		if (hasGuiManager()) {
			getGuiManager().resetSpreadsheet();
		}
		
		getEuclidianView().resetMaxLayerUsed();
	}

	/**
	 * Returns labeling style. See the constants in ConstructionDefaults (e.g.
	 * LABEL_VISIBLE_AUTOMATIC)
	 */
	public int getLabelingStyle() {
		return labelingStyle;
	}

	/**
	 * Sets labeling style. See the constants in ConstructionDefaults (e.g.
	 * LABEL_VISIBLE_AUTOMATIC)
	 */
	public void setLabelingStyle(int labelingStyle) {
		this.labelingStyle = labelingStyle;
	}

	/**
	 * Updates the GUI of the main component.
	 */
	public void updateContentPane() {
		updateContentPane(true);
	}

	/**
	 * Updates the GUI of the framd and its size.
	 */
	public void updateContentPaneAndSize() {
		if (INITING)
			return;

		updateContentPane(false);
		if (frame != null && frame.isShowing()) {
			getGuiManager().updateFrameSize();
		}
		updateComponentTreeUI();
	}

	private void updateContentPane(boolean updateComponentTreeUI) {
		if (INITING)
			return;

		Container cp;
		if (isApplet)
			cp = appletImpl.getJApplet().getContentPane();
		else if (frame != null)
			cp = frame.getContentPane();
		else
			cp = (Container) mainComp;
		
		addMacroCommands();
		cp.removeAll();
		cp.add(buildApplicationPanel());
		fontManager.setFontSize(appFontSize);

		// update sizes
		euclidianView.updateSize();

		// update layout
		if (updateComponentTreeUI) {
			updateComponentTreeUI();
		}

		// reset mode and focus
		setMoveMode();
		if (mainComp.isShowing())
			euclidianView.requestFocusInWindow();

		System.gc();
	}

	protected void updateComponentTreeUI() {
		if (appletImpl != null) {
			SwingUtilities.updateComponentTreeUI(appletImpl.getJApplet());
		}
		else if (frame != null) {
			SwingUtilities.updateComponentTreeUI(frame);
		}
		else if (mainComp != null) {
			SwingUtilities.updateComponentTreeUI(mainComp);
		}

	}

	/**
	 * Builds a panel with all components that should be shown on screen (like
	 * toolbar, input field, algebra view).
	 */
	public JPanel buildApplicationPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		// CENTER: Algebra View, Euclidian View
		// euclidian panel with view and status bar
		if (centerPanel != null)
			centerPanel.removeAll();
		centerPanel = new JPanel(new BorderLayout());

		if (showToolBar) {
			// NORTH: Toolbar
			panel.add(getGuiManager().getToolbarPanel(), BorderLayout.NORTH);
		}

		// updateCenterPanel
		updateCenterPanel(true);
		panel.add(centerPanel, BorderLayout.CENTER);
		// border for center panel
		centerPanel.setBorder(BorderFactory.createMatteBorder(showToolBar?1:0, 0, showAlgebraInput?1:0, 0, Color.lightGray));

		// SOUTH: inputField
		if (showAlgebraInput) {
			JComponent algebraInput = getGuiManager().getAlgebraInput();
			panel.add(algebraInput, BorderLayout.SOUTH);
		}

		// init labels
		setLabels();
		
		// Menubar; if the main component is a JPanel, we need to add the 
		// menubar manually to the north
		if (showMenuBar() && mainComp instanceof JPanel) {
			JPanel menuBarPanel = new JPanel(new BorderLayout());
			menuBarPanel.add(getGuiManager().getMenuBar(), BorderLayout.NORTH);
			menuBarPanel.add(panel, BorderLayout.CENTER);
			return menuBarPanel;
		} else {
			// standard case: return 
			return panel;
		}
	}

	public void updateCenterPanel(boolean updateUI) {
		centerPanel.removeAll();

		JPanel euclidianPanel = new JPanel(new BorderLayout());
		euclidianPanel.setBackground(Color.white);
		euclidianPanel.add(euclidianView, BorderLayout.CENTER);

		if (showConsProtNavigation) {
			JComponent consProtNav = getGuiManager()
					.getConstructionProtocolNavigation();
			euclidianPanel.add(consProtNav, BorderLayout.SOUTH);
			consProtNav.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0,
					Color.lightGray));
		}

		JComponent cp2 = null;
		if (showAlgebraView) {
			JComponent algView = getGuiManager().getAlgebraView();
			algView.setBorder(BorderFactory.createEmptyBorder(3, 3, 0, 0));
			JScrollPane algPanel = new JScrollPane(algView);
			
			if (horizontalSplit) {
				// LEFT: Algebra view
				// RIGHT: Euclidian view
				sp2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, algPanel, euclidianPanel);
				sp2.setDividerLocation(initSplitDividerLocationHOR2);
				// algebra view: line at right
				algPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.lightGray));
				// euclidian view: line at left
				euclidianPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.lightGray));
			} 
			else {	
				// TOP: Euclidian view
				// BOTTOM: Algebra view
				sp2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, euclidianPanel, algPanel);
				sp2.setDividerLocation(initSplitDividerLocationVER2);
				// euclidian view: line at bottom
				euclidianPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
				// algebra view: line at top
				algPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray)); 
			}
			sp2.setBorder(BorderFactory.createEmptyBorder());

			if (spChangeListener == null)
				spChangeListener = new DividerChangeListener();
			sp2.addPropertyChangeListener("dividerLocation", spChangeListener);
			cp2 = sp2;
		} else {
			cp2 = euclidianPanel;
		}

		JComponent cp1 = null;
		if (showSpreadsheet) {
			JComponent spreadsheetPanel = getGuiManager().getSpreadsheetView();
			
			if (horizontalSplit) {
				// LEFT: Euclidian View
				// RIGHT: Spreadsheet
				sp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, cp2, spreadsheetPanel);
				sp.setDividerLocation(initSplitDividerLocationHOR);
				// euclidian view: line at right
				cp2.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.lightGray));
				// euclidian view: line at right
				spreadsheetPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.lightGray));
			} else {
				// TOP: EuclidianView
				// BOTTOM: Spreadsheet
				sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, cp2, spreadsheetPanel);
				sp.setDividerLocation(initSplitDividerLocationVER);
				// euclidian view: line at bottom
				cp2.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.lightGray));
				// euclidian view: line at top
				spreadsheetPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.lightGray));
			}
			sp.setBorder(BorderFactory.createEmptyBorder());

			if (spChangeListener == null)
				spChangeListener = new DividerChangeListener();
			sp.addPropertyChangeListener("dividerLocation", spChangeListener);
			cp1 = sp;
		} else {
			cp1 = cp2;
		}
		
		// put everything into centerPanel
		centerPanel.add(cp1, BorderLayout.CENTER);

		if (updateUI) {
			centerPanel.updateUI(); // needed for applets
			updateComponentTreeUI();
		}
	}

	public JPanel getCenterPanel() {
		return centerPanel;
	}

	/**
	 * Handles command line options (like -language).
	 */
	private void handleOptionArgs(String[] args) {
		if (args != null) {
			// handle all options (starting with --)
			for (int i = 0; i < args.length; i++) {
				if (args[i].startsWith("--")) {
					// option found: get option's name and value
					int equalsPos = args[i].indexOf('=');
					String optionName = equalsPos <= 2 ? args[i].substring(2)
							: args[i].substring(2, equalsPos);
					String optionValue = equalsPos < 0
							|| equalsPos == args[i].length() - 1 ? "" : args[i]
							.substring(equalsPos + 1);

					if (optionName.equals("help")) {
						// help message
						System.out
								.println("Usage: java -jar geogebra.jar [OPTION] [FILE]\n"
										+ "Start GeoGebra with the specified OPTIONs and open the given FILE.\n"
										+ "  --help\t\tprint this message\n"
										+ "  --language=LANGUAGE_CODE\t\tset language using locale strings, e.g. en, de, de_AT, ...\n"
										+ "  --showAlgebraInput=BOOLEAN\tshow/hide algebra input field\n"
										+ "  --showAlgebraWindow=BOOLEAN\tshow/hide algebra window\n"
										+ "  --showSpreadsheet=BOOLEAN\tshow/hide spreadsheet\n"
										+ "  --fontSize=NUMBER\tset default font size\n"
										+ "  --sugar\n"
										+ "  --showSplash=BOOLEAN\tenable/disable the splash screen\n"
										+ "  --enableUndo=BOOLEAN\tenable/disable Undo\n"
										+ "  --showAxes=BOOLEAN\tshow/hide coordinate axes\n"
										+ "  --antiAliasing=BOOLEAN\tturn anti-aliasing on/off\n");
					System.exit(0);
					} else if (optionName.equals("language")) {
						setLocale(getLocale(optionValue));

						// Application.debug("lanugage option: " + optionValue);
						// Application.debug("locale: " + initLocale);
					} else if (optionName.equals("showAlgebraInput")) {
						setShowAlgebraInput(!optionValue.equals("false"));
					} else if (optionName.equals("showAlgebraWindow")) {
						setShowAlgebraView(!optionValue.equals("false"));
					} else if (optionName.equals("showSpreadsheet")) {
						setShowSpreadsheetView(!optionValue.equals("false"));
					} else if (optionName.equals("fontSize")) {
						setFontSize(Integer.parseInt(optionValue));
					} else if (optionName.equals("sugar")) {
						runningOnSugar=true;
					} else if (optionName.equals("enableUndo")) {
						setUndoActive(!optionValue.equals("false"));
					} else if (optionName.equals("showAxes")) {
						showAxes[0] = !optionValue.equals("false");
						showAxes[1] = showAxes[0];
					} else if (optionName.equals("showGrid")) {
						showGrid = !optionValue.equals("false");
					} else if (optionName.equals("antiAliasing")) {
						antialiasing = !optionValue.equals("false");
					}
				}
			}
		}
	}

	/**
	 * Opens a file specified as last command line argument
	 * 
	 * @return true if a file was loaded successfully
	 */
	private boolean handleFileArg(String[] args) {

		if (args == null || args.length < 1)
			return false;

		String fileArgument = null;
		// the filename is the last command line argument
		// and should not be an option, i.e. it does not start
		// with "-" like -open, -print or -language
		if (args[args.length - 1].charAt(0) == '-')
			// option
			return false;
		else {
			// take last argument as filename
			fileArgument = args[args.length - 1];
		}

		try {
			boolean success;
			String lowerCase = fileArgument.toLowerCase(Locale.US);
			boolean isMacroFile = lowerCase.endsWith(FILE_EXT_GEOGEBRA_TOOL);
			if (lowerCase.startsWith("http") || lowerCase.startsWith("file")) {
				//replace all whitespace characters by %20 in URL string
				fileArgument = fileArgument.replaceAll("\\s", "%20");
				URL url = new URL(fileArgument);
				success = loadXML(url, isMacroFile);
				updateContentPane(true);
			} else if (lowerCase.startsWith("base64://")) {

				// substring to strip off base64://
				byte[] zipFile = geogebra.util.Base64.decode(fileArgument
						.substring(9));
				// byte [] zipFile =
				// geogebra.util.Base64.decode("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");
				// Application.debug(""+(char)zipFile[0]+ (char)zipFile[1] +
				// (char)zipFile[2]);
				success = loadXML(zipFile);
				updateContentPane(true);
			} else {
				File f = new File(fileArgument);
				f = f.getCanonicalFile();
				getGuiManager().loadFile(f, isMacroFile);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	final public Kernel getKernel() {
		return kernel;
	}

	public void setApplet(AppletImplementation appletImpl) {
		isApplet = true;
		this.appletImpl = appletImpl;
		mainComp = appletImpl.getJApplet();
	}

	public void setShowResetIcon(boolean flag) {
		if (flag != showResetIcon) {
			showResetIcon = flag;
			euclidianView.updateBackground();
		}
	}

	final public boolean showResetIcon() {
		return showResetIcon && !runningInFrame;
	}

	public void reset() {
		if (appletImpl != null) {
			appletImpl.reset();
		} else if (currentFile != null) {
			getGuiManager().loadFile(currentFile, false);
		} else
			clearConstruction();
	}

	public void refreshViews() {
		euclidianView.updateBackground();
		kernel.notifyRepaint();
	}
	
	public void setFrame(JFrame frame) {
		isApplet = false;
		mainComp = frame;

		this.frame = frame;
		updateTitle();
		
		// Windows 7 uses this for the Toolbar icon too
		// (needs to be larger)
		frame.setIconImage(getInternalImage("geogebra32.gif"));
		
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		WindowListener[] wl = frame.getWindowListeners();
		if (wl == null || wl.length == 0) {
			// window closing listener
			WindowAdapter windowListener = new WindowAdapter() {
				public void windowClosing(WindowEvent event) {
					exit();
				}
			};
			frame.addWindowListener(windowListener);
		}
	}

	final public boolean isApplet() {
		return isApplet;
	}

	public boolean isStandaloneApplication() {
		return !isApplet && (mainComp instanceof JFrame);
	}

	public synchronized JFrame getFrame() {
//		creating a frame does not work within Kojo (causes a blank screen when opening files)
//		if (frame == null) {
//			frame = getGuiManager().createFrame();
//		}

		return frame;
	}

	public Component getMainComponent() {
		return mainComp;
	}

	public EuclidianView getEuclidianView() {
		return euclidianView;
	}

	public void geoElementSelected(GeoElement geo, boolean addToSelection) {
		if (currentSelectionListener != null)
			currentSelectionListener.geoElementSelected(geo, addToSelection);
	}

	/**
	 * Sets a mode where clicking on an object will notify the given selection
	 * listener.
	 */
	public void setSelectionListenerMode(GeoElementSelectionListener sl) {
		currentSelectionListener = sl;
		if (sl != null)
			setMode(EuclidianView.MODE_SELECTION_LISTENER);
		else
			setMoveMode();
	}

	public GeoElementSelectionListener getCurrentSelectionListener() {
		return currentSelectionListener;
	}

	public void setMoveMode() {
		setMode(EuclidianView.MODE_MOVE);
	}
	
	/** 
	 * Sets the maximum pixel size (width and height) of 
	 * all icons in the user interface. Larger icons are scaled
	 * down.
	 * @param pixel: max icon size between 16 and 32 pixels
	 */
	public void setMaxIconSize(int pixel) {
		maxIconSize = Math.min(32, Math.max(16, pixel));
	}
	
	public int getMaxIconSize() {
		return maxIconSize;
	}

	public ImageIcon getImageIcon(String filename) {
		return getImageIcon(filename, null);
	}

	public ImageIcon getImageIcon(String filename, Color borderColor) {
		
		ImageIcon icon = imageManager.getImageIcon("/geogebra/gui/images/" + filename,
				borderColor);
		// scale icon if necessary
		icon = ImageManager.getScaledIcon(icon, Math.min(icon.getIconWidth(), maxIconSize), 
					Math.min(icon.getIconHeight(), maxIconSize));
		return icon;
	}

	public ImageIcon getToolBarImage(String filename, Color borderColor) {
		
		String path = "/geogebra/gui/toolbar/images/" + filename;
		ImageIcon icon = imageManager.getImageIcon(path, borderColor);

		if (icon == null) {
			// load3DJar();
			// try to find this image in 3D extension
			path = "/geogebra/geogebra3D/images/" + filename;
			icon = imageManager.getImageIcon(path, borderColor);
		}
		
		// scale icon if necessary
		icon = ImageManager.getScaledIcon(icon, Math.min(icon.getIconWidth(), maxIconSize), 
					Math.min(icon.getIconHeight(), maxIconSize));

		return icon;
	}

	public ImageIcon getEmptyIcon() {
		
		return imageManager.getImageIcon("/geogebra/gui/images/empty.gif");
	}

	public Image getInternalImage(String filename) {
		
		return imageManager
				.getInternalImage("/geogebra/gui/images/" + filename);
	}

	public Image getRefreshViewImage() {
		// don't need to load gui jar as reset image is in main jar
		return imageManager.getInternalImage("/geogebra/main/view-refresh.png");
	}

	public Image getPlayImage() {
		// don't need to load gui jar as reset image is in main jar
		return imageManager.getInternalImage("/geogebra/main/nav_play.png");
	}

	public Image getPauseImage() {
		// don't need to load gui jar as reset image is in main jar
		return imageManager.getInternalImage("/geogebra/main/nav_pause.png");
	}

	public BufferedImage getExternalImage(String filename) {
		return imageManager.getExternalImage(filename);
	}

	public void addExternalImage(String filename, BufferedImage image) {
		imageManager.addExternalImage(filename, image);
	}

	// public void startEditing(GeoElement geo) {
	// if (showAlgebraView)
	// getApplicationGUImanager().startEditingAlgebraView(geo);
	// }

	public final void zoom(double px, double py, double zoomFactor) {
		euclidianView.zoom(px, py, zoomFactor, 15, true);
	}

	/**
	 * Sets the ratio between the scales of y-axis and x-axis, i.e. ratio =
	 * yscale / xscale;
	 * 
	 * @param zoomFactor
	 */
	public final void zoomAxesRatio(double axesratio) {
		euclidianView.zoomAxesRatio(axesratio, true);
	}

	public final void setStandardView() {
		euclidianView.setStandardView(true);
	}

	public final void setViewShowAllObjects() {
		euclidianView.setViewShowAllObjects(true);
	}

	/***************************************************************************
	 * LOCALE part
	 **************************************************************************/

	/**
	 * Creates a Locale object according to the given language code. The
	 * languageCode string should consist of two letters for the language, two
	 * letters for the country and two letters for the variant. E.g. "en" ...
	 * language: English , no country specified, "deAT" or "de_AT" ... language:
	 * German , country: Austria, "noNONY" or "no_NO_NY" ... language: Norwegian
	 * , country: Norway, variant: Nynorsk
	 */
	public static Locale getLocale(String languageCode) {
		// remove "_" from string
		languageCode = languageCode.replaceAll("_", "");

		Locale loc;
		if (languageCode.length() == 6) {
			// language, country
			loc = new Locale(languageCode.substring(0, 2), languageCode
					.substring(2, 4), languageCode.substring(4, 6));
		} else if (languageCode.length() == 4) {
			// language, country
			loc = new Locale(languageCode.substring(0, 2), languageCode
					.substring(2, 4));
		} else {
			// language only
			loc = new Locale(languageCode.substring(0, 2));
		}
		return loc;
	}

	/**
	 * set language via iso language string
	 */
	public void setLanguage(Locale locale) {
		if (locale == null
				|| currentLocale.toString().equals(locale.toString()))
			return;

		if (!INITING) {
			setMoveMode();
		}

		// load resource files
		setLocale(locale);

		// update right angle style in euclidian view (different for German)
		if (euclidianView != null)
			euclidianView.updateRightAngleStyle(locale);

		setLabels(); // update display

		System.gc();
	}

	/*
	 * removed Michael Borcherds 2008-03-31 private boolean reverseLanguage =
	 * false; //FKH 20040822 final public boolean isReverseLanguage() { //FKH
	 * 20041010 // for Chinese return reverseLanguage; }
	 */

	/*
	 * in French, zero is singular, eg 0 dcimale rather than 0 decimal places
	 */
	public boolean isZeroPlural(Locale locale) {
		String lang = locale.getLanguage();
		if (lang.startsWith("fr"))
			return false;
		return true;
	}

	// For Hebrew and Arabic. Guy Hed, 25.8.2008
	private boolean rightToLeftReadingOrder = false;

	final public boolean isRightToLeftReadingOrder() {
		return rightToLeftReadingOrder;
	}

	// for basque you have to say "A point" instead of "point A"
	private boolean reverseNameDescription = false;
	private boolean isAutoCompletePossible = true;

	final public boolean isReverseNameDescriptionLanguage() {
		// for Basque
		return reverseNameDescription;
	}

	/**
	 * Returns whether autocomplete should be used at all. Certain languages
	 * make problems with auto complete turned on (e.g. Korean).
	 */
	final public boolean isAutoCompletePossible() {
		return isAutoCompletePossible;
	}

	private void updateReverseLanguage(Locale locale) {
		String lang = locale.getLanguage();
		// reverseLanguage = "zh".equals(lang); removed Michael Borcherds
		// 2008-03-31
		reverseNameDescription = "eu".equals(lang);

		// Guy Hed, 25.8.2008.
		// Guy Hed, 26.4.2009 - added Yiddish and Persian as RTL languages
		rightToLeftReadingOrder = ("iw".equals(lang) || "ar".equals(lang)
				|| "fa".equals(lang) || "ji".equals(lang));
		// Another option:
		// rightToLeftReadingOrder =
		// (Character.getDirectionality(getPlain("Algebra").charAt(1)) ==
		// Character.DIRECTIONALITY_RIGHT_TO_LEFT);

		// turn off auto-complete for Korean
		isAutoCompletePossible = !"ko".equals(lang);
	}

	// Michael Borcherds 2008-02-23
	public boolean languageIs(Locale locale, String lang) {
		return locale.getLanguage().equals(lang);
	}

	

	StringBuffer testCharacters = new StringBuffer();



	public void setLocale(Locale locale) {
		if (locale == currentLocale) return;
		Locale oldLocale = currentLocale;

		// only allow special locales due to some weird server
		// problems with the naming of the property files
		currentLocale = getClosestSupportedLocale(locale);
		updateResourceBundles();

		// update font for new language (needed for e.g. chinese)
		try {
			fontManager.setLanguage(currentLocale);
		} catch (Exception e) {
			e.printStackTrace();
			showError(e.getMessage());

			// go back to previous locale
			currentLocale = oldLocale;
			updateResourceBundles();
		}

		updateReverseLanguage(locale);
	}

	/**
	 * Returns a locale object that has the same country and/or language as
	 * locale. If the language of locale is not supported an English locale is
	 * returned.
	 */
	private static Locale getClosestSupportedLocale(Locale locale) {
		int size = supportedLocales.size();

		// try to find country and variant
		String country = locale.getCountry();
		String variant = locale.getVariant();

		if (country.length() > 0) {
			for (int i = 0; i < size; i++) {
				Locale loc = (Locale) supportedLocales.get(i);
				if (country.equals(loc.getCountry())
						&& variant.equals(loc.getVariant()))
					// found supported country locale
					return loc;
			}
		}

		// try to find language
		String language = locale.getLanguage();
		for (int i = 0; i < size; i++) {
			Locale loc = (Locale) supportedLocales.get(i);
			if (language.equals(loc.getLanguage()))
				// found supported country locale
				return loc;
		}

		// we didn't find a matching country or language,
		// so we take English
		return Locale.ENGLISH;
	}

	public ResourceBundle initAlgo2CommandBundle() {
		return MyResourceBundle.loadSingleBundleFile(RB_ALGO2COMMAND);
	}

	// Added for Intergeo File Format (Yves Kreis) -->
	public ResourceBundle initAlgo2IntergeoBundle() {
		return MyResourceBundle.loadSingleBundleFile(RB_ALGO2INTERGEO);
	}

	// <-- Added for Intergeo File Format (Yves Kreis)

	private void updateResourceBundles() {
		if (rbmenu != null)
			rbmenu = MyResourceBundle.createBundle(RB_MENU, currentLocale);
		if (rberror != null)
			rberror = MyResourceBundle.createBundle(RB_ERROR, currentLocale);
		if (rbplain != null)
			rbplain = MyResourceBundle.createBundle(RB_PLAIN, currentLocale);
		if (rbcommand != null)
			rbcommand = MyResourceBundle
					.createBundle(RB_COMMAND, currentLocale);
	}

	private void fillCommandDict() {
		if (rbcommand == rbcommandOld)
			return;
		rbcommandOld = rbcommand;

		if (translateCommandTable == null)
			translateCommandTable = new Hashtable();
		if (commandDict == null)
			commandDict = new LowerCaseDictionary();
		translateCommandTable.clear();
		commandDict.clear();

		// Enumeration e = rbcommand.getKeys();
		Iterator it = kernel.getAlgebraProcessor().getCmdNameIterator();
		while (it.hasNext()) {
			String internal = (String) it.next();
			// Application.debug(internal);
			if (!internal.endsWith("Syntax") && !internal.equals("Command")) {
				String local = rbcommand.getString((String) internal);
				if (local != null) {
					local = local.trim();
					// case is ignored in translating local command names to
					// internal names!
					translateCommandTable.put(local.toLowerCase(), internal);
					commandDict.addEntry(local);
				}
			}
		}

		addMacroCommands();
	}

	private void addMacroCommands() {
		if (commandDict == null || kernel == null || !kernel.hasMacros())
			return;

		ArrayList macros = kernel.getAllMacros();
		for (int i = 0; i < macros.size(); i++) {
			String cmdName = ((Macro) macros.get(i)).getCommandName();
			if (!commandDict.contains(cmdName))
				commandDict.addEntry(cmdName);
		}
	}

	public void removeMacroCommands() {
		if (commandDict == null || kernel == null || !kernel.hasMacros())
			return;

		ArrayList macros = kernel.getAllMacros();
		for (int i = 0; i < macros.size(); i++) {
			String cmdName = ((Macro) macros.get(i)).getCommandName();
			commandDict.removeEntry(cmdName);
		}
	}

	public Locale getLocale() {
		return currentLocale;
	}

	/*
	 * properties methods
	 */

	final public String getPlain(String key) {
		if (rbplain == null) {
			initPlainResourceBundle();
		}

		try {
			return rbplain.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	private void initPlainResourceBundle() {
		rbplain = MyResourceBundle.createBundle(RB_PLAIN, currentLocale);
		if (rbplain != null)
			kernel.updateLocalAxesNames();
	}

	// Michael Borcherds 2008-03-25
	// replace "%0" by arg0
	final public String getPlain(String key, String arg0) {
		String[] ss = { arg0 };
		return getPlain(key, ss);
	}

	// Michael Borcherds 2008-03-25
	// replace "%0" by arg0, "%1" by arg1
	final public String getPlain(String key, String arg0, String arg1) {
		String[] ss = { arg0, arg1 };
		return getPlain(key, ss);
	}

	// Michael Borcherds 2008-03-30
	// replace "%0" by arg0, "%1" by arg1, "%2" by arg2
	final public String getPlain(String key, String arg0, String arg1,
			String arg2) {
		String[] ss = { arg0, arg1, arg2 };
		return getPlain(key, ss);
	}

	// Michael Borcherds 2008-03-30
	// replace "%0" by arg0, "%1" by arg1, "%2" by arg2, "%3" by arg3
	final public String getPlain(String key, String arg0, String arg1,
			String arg2, String arg3) {
		String[] ss = { arg0, arg1, arg2, arg3 };
		return getPlain(key, ss);
	}

	// Michael Borcherds 2008-03-30
	// replace "%0" by arg0, "%1" by arg1, "%2" by arg2, "%3" by arg3, "%4" by
	// arg4
	final public String getPlain(String key, String arg0, String arg1,
			String arg2, String arg3, String arg4) {
		String[] ss = { arg0, arg1, arg2, arg3, arg4 };
		return getPlain(key, ss);
	}

	// Michael Borcherds 2008-03-25
	// Markus Hohenwarter 2008-09-18
	// replace "%0" by args[0], "%1" by args[1], etc
	final public String getPlain(String key, String[] args) {
		String str = getPlain(key);

		sbPlain.setLength(0);
		for (int i = 0; i < str.length(); i++) {
			char ch = str.charAt(i);
			if (ch == '%') {
				// get number after %
				i++;
				int pos = str.charAt(i) - '0';
				if (pos >= 0 && pos < args.length)
					// success
					sbPlain.append(args[pos]);
				else
					// failed
					sbPlain.append(ch);
			} else {
				sbPlain.append(ch);
			}
		}

		return sbPlain.toString();
	}

	private StringBuffer sbPlain = new StringBuffer();

	final public String getMenu(String key) {
		if (rbmenu == null) {
			rbmenu = MyResourceBundle.createBundle(RB_MENU, currentLocale);
		}

		try {
			return rbmenu.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	final public String getError(String key) {
		if (rberror == null) {
			rberror = MyResourceBundle.createBundle(RB_ERROR, currentLocale);
		}

		try {
			return rberror.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	/**
	 * Initializes the translated command names for this application. Note: this
	 * will load the properties files first.
	 */
	final public void initTranslatedCommands() {
		if (rbcommand == null) {
			rbcommand = MyResourceBundle
					.createBundle(RB_COMMAND, currentLocale);
			fillCommandDict();
			kernel.updateLocalAxesNames();
		}
	}

	final public String getCommand(String key) {
		initTranslatedCommands();

		try {
			return rbcommand.getString(key);
		} catch (Exception e) {
			return key;
		}
	}

	final public String getSetting(String key) {
		if (rbsettings == null)
			rbsettings = MyResourceBundle.loadSingleBundleFile(RB_SETTINGS);

		try {
			return rbsettings.getString(key);
		} catch (Exception e) {
			return null;
		}
	}

	public boolean propertiesFilesPresent() {
		return rbplain != null;
	}

	/**
	 * translate command name to internal name. Note: the case of localname is
	 * NOT relevant
	 */
	final public String translateCommand(String localname) {
		if (localname == null)
			return null;
		if (translateCommandTable == null)
			return localname;

		// note: lookup lower case of command name!
		Object value = translateCommandTable.get(localname.toLowerCase());
		if (value == null)
			return localname;
		else
			return (String) value;
	}

	public void showRelation(GeoElement a, GeoElement b) {
		JOptionPane.showConfirmDialog(mainComp, new Relation(kernel).relation(
				a, b), getPlain("ApplicationName") + " - "
				+ getCommand("Relation"), JOptionPane.DEFAULT_OPTION,
				JOptionPane.INFORMATION_MESSAGE);

	}

	public void showHelp(String key) {
		final String text = getPlain(key); // Michael Borcherds changed to use
		// getPlain() and removed try/catch

		JOptionPane.showConfirmDialog(mainComp, text,
				getPlain("ApplicationName") + " - " + getMenu("Help"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
	}

	public void showError(String key) {
		showErrorDialog(getError(key));
	}

	public void showError(MyError e) {
		showErrorDialog(e.getLocalizedMessage());
	}

	public void showErrorDialog(String msg) {
		if (!isErrorDialogsActive)
			return;

		// Application.debug("showErrorDialog: "+msg);

		isErrorDialogShowing = true;

		// TODO investigate why this freezes Firefox sometimes
		JOptionPane.showConfirmDialog(mainComp, msg,
				getPlain("ApplicationName") + " - " + getError("Error"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE);

		isErrorDialogShowing = false;
	}

	public boolean isErrorDialogShowing() {
		return isErrorDialogShowing;
	}

	public void showMessage(String message) {
		// Application.debug("showMessage: "+message);

		JOptionPane.showConfirmDialog(mainComp, message,
				getPlain("ApplicationName") + " - " + getMenu("Info"),
				JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Downloads a bitmap from the URL and stores it in this application's
	 * imageManager. Michael Borcherds
	 * 
	 * @return fileName of image stored in imageManager
	 * 
	 *         public String getImageFromURL(String url) { try{
	 * 
	 *         BufferedImage img=javax.imageio.ImageIO.read(new URL(url));
	 *         return createImage(img, "bitmap.png"); } catch (Exception e)
	 *         {return null;} }
	 */

	public void setWaitCursor() {
		Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		mainComp.setCursor(waitCursor);

		if (euclidianView != null)
			euclidianView.setCursor(waitCursor);

		if (appGuiManager != null)
			appGuiManager.allowGUIToRefresh();
	}

	public void setDefaultCursor() {
		mainComp.setCursor(Cursor.getDefaultCursor());
		if (euclidianView != null)
			euclidianView.setCursor(Cursor.getDefaultCursor());
	}

	public void doAfterRedefine(GeoElement geo) {
		if (appGuiManager != null)
			getGuiManager().doAfterRedefine(geo);
	}

	/*
	 * private methods for display
	 */

	public File getCurrentFile() {
		return currentFile;
	}

	public File getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(File file) {
		currentPath = file;
	}

	public void setCurrentFile(File file) {
		currentFile = file;
		if (currentFile != null) {
			currentPath = currentFile.getParentFile();
			addToFileList(currentFile);
		}
		updateTitle();
		updateMenubar();
	}

	public static void addToFileList(File file) {
		if (file == null || !file.exists())
			return;

		// add or move fileName to front of list
		fileList.remove(file);
		fileList.addFirst(file);
	}

	public static File getFromFileList(int i) {
		if (fileList.size() > i)
			return (File) fileList.get(i);
		else
			return null;
	}

	public static int getFileListSize() {
		return fileList.size();
	}

	public void updateTitle() {
		if (frame == null)
			return;

		getGuiManager().updateFrameTitle();
	}

	public void setFontSize(int points) {
		if (points == appFontSize)
			return;
		appFontSize = points;
		isSaved = false;

		resetFonts();

		if (!INITING) {
			if (appletImpl != null)
				SwingUtilities.updateComponentTreeUI(appletImpl.getJApplet());
			if (frame != null)
				SwingUtilities.updateComponentTreeUI(frame);
		}
	}

	public void resetFonts() {
		fontManager.setFontSize(appFontSize);
		updateFonts();
	}

	public void updateFonts() {
		if (euclidianView != null)
			euclidianView.updateFonts();

		if (appGuiManager != null)
			getGuiManager().updateFonts();
	}

	

	public int getFontSize() {
		return appFontSize;
	}

	private void setLabels() {
		if (INITING)
			return;

		if (appGuiManager != null) {
			getGuiManager().setLabels();
		}

		if (rbplain != null)
			kernel.updateLocalAxesNames();

		updateCommandDictionary();
	}

	/**
	 * Returns name of given tool.
	 * 
	 * @param mode
	 *            number
	 */
	public String getToolName(int mode) {
		return getToolNameOrHelp(mode, true);
	}

	/**
	 * Returns the tool help text for the given tool.
	 * 
	 * @param mode
	 *            number
	 */
	public String getToolHelp(int mode) {
		return getToolNameOrHelp(mode, false);
	}

	/**
	 * Returns the tool name and tool help text for the given tool as an HTML
	 * text that is useful for tooltips.
	 * 
	 * @param mode
	 *            : tool ID
	 */
	public String getToolTooltipHTML(int mode) {
		StringBuffer sbTooltip = new StringBuffer();
		sbTooltip.append("<html><b>");
		sbTooltip.append(Util.toHTMLString(getToolName(mode)));
		sbTooltip.append("</b><br>");
		sbTooltip.append(Util.toHTMLString(getToolHelp(mode)));
		sbTooltip.append("</html>");
		return sbTooltip.toString();
	}

	private String getToolNameOrHelp(int mode, boolean toolName) {
		// macro
		String ret;

		if (mode >= EuclidianView.MACRO_MODE_ID_OFFSET) {
			// MACRO
			int macroID = mode - EuclidianView.MACRO_MODE_ID_OFFSET;
			try {
				Macro macro = kernel.getMacro(macroID);
				if (toolName) {
					// TOOL NAME
					ret = macro.getToolName();
					if ("".equals(ret))
						ret = macro.getCommandName();
				} else {
					// TOOL HELP
					ret = macro.getToolHelp();
					if ("".equals(ret))
						ret = macro.getNeededTypesString();
				}
			} catch (Exception e) {
				Application
						.debug("Application.getModeText(): macro does not exist: ID = "
								+ macroID);
				// e.printStackTrace();
				return "";
			}
		} else {
			// STANDARD TOOL
			String modeText = EuclidianView.getModeText(mode);
			if (toolName) {
				// tool name
				ret = getMenu(modeText);
			} else {
				// tool help
				ret = getMenu(modeText + ".Help");
			}
		}

		return ret;
	}

	public ImageIcon getModeIcon(int mode) {
		ImageIcon icon;

		Color border = Color.lightGray;

		// macro
		if (mode >= EuclidianView.MACRO_MODE_ID_OFFSET) {
			int macroID = mode - EuclidianView.MACRO_MODE_ID_OFFSET;
			try {
				Macro macro = kernel.getMacro(macroID);
				String iconName = macro.getIconFileName();
				BufferedImage img = getExternalImage(iconName);
				if (img == null)
					// default icon
					icon = getToolBarImage("mode_tool_32.png", border);
				else
					// use image as icon
					icon = new ImageIcon(ImageManager.addBorder(img, border));
			} catch (Exception e) {
				Application.debug("macro does not exist: ID = " + macroID);
				return null;
			}
		} else {
			// standard case
			String modeText = EuclidianView.getModeText(mode);
			// bugfix for Turkish locale added Locale.US
			String iconName = "mode_" + modeText.toLowerCase(Locale.US)
					+ "_32.gif";
			icon = getToolBarImage(iconName, border);
			if (icon == null) {
				Application.debug("icon missing for mode " + modeText + " ("
						+ mode + ")");
			}
		}
		
		return icon;
	}

	public void setSplitDividerLocationHOR(int loc) {
		initSplitDividerLocationHOR = loc;
		// debug(loc+"setSplitDividerLocationHOR");
	}

	public void setSplitDividerLocationVER(int loc) {
		initSplitDividerLocationVER = loc;
		// debug(loc+"setSplitDividerLocationVER");
	}

	public void setSplitDividerLocationHOR2(int loc) {
		initSplitDividerLocationHOR2 = loc;
		// debug(loc+"setSplitDividerLocationHOR2");
	}

	public void setSplitDividerLocationVER2(int loc) {
		initSplitDividerLocationVER2 = loc;
		// debug(loc+"setSplitDividerLocationVER2");
	}

	public void setHorizontalSplit(boolean flag) {
		if (flag == horizontalSplit)
			return;

		horizontalSplit = flag;
		if (sp == null)
			return;
		if (flag) {
			sp.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		} else {
			sp.setOrientation(JSplitPane.VERTICAL_SPLIT);
		}
	}

	public boolean isHorizontalSplit() {
		return horizontalSplit;
	}

	public void setShowAlgebraView(boolean flag) {
		if (showAlgebraView == flag)
			return;

		showAlgebraView = flag;
		if (showAlgebraView) {
			getGuiManager().attachAlgebraView();
			getGuiManager().setShowAuxiliaryObjects(showAuxiliaryObjects);
		} else {
			if (hasGuiManager())
				getGuiManager().detachAlgebraView();
		}

		updateMenubar();
		isSaved = false;
	}

	public void setShowSpreadsheetView(boolean flag) {
		if (showSpreadsheet == flag)
			return;

		showSpreadsheet = flag;
		if (showSpreadsheet) {
			getGuiManager().attachSpreadsheetView();
		} else {
			getGuiManager().detachSpreadsheetView();
		}

		updateMenubar();
		isSaved = false;
	}

	final public boolean showAlgebraView() {
		return showAlgebraView;
	}

	// Michael Borcherds 2008-01-14
	final public boolean showSpreadsheetView() {
		return showSpreadsheet;
	}

	public boolean showAlgebraInput() {
		return showAlgebraInput;
	}

	public void setShowAlgebraInput(boolean flag) {
		showAlgebraInput = flag;
		updateMenubar();
	}

	public boolean showCmdList() {
		return showCmdList;
	}

	public void setShowCmdList(boolean flag) {
		if (showCmdList == flag)
			return;

		showCmdList = flag;
		getGuiManager().updateAlgebraInput();
		updateMenubar();
	}

	/**
	 * Displays the construction protocol navigation
	 */
	public void setShowConstructionProtocolNavigation(boolean flag) {
		if (flag == showConsProtNavigation)
			return;
		showConsProtNavigation = flag;

		getGuiManager().setShowConstructionProtocolNavigation(flag);
		updateMenubar();
	}

	public boolean showConsProtNavigation() {
		return showConsProtNavigation;
	}

	public boolean showAuxiliaryObjects() {
		return showAuxiliaryObjects;
	}

	public void setShowAuxiliaryObjects(boolean flag) {
		showAuxiliaryObjects = flag;

		if (showAlgebraView)
			getGuiManager().setShowAuxiliaryObjects(flag);
		updateMenubar();
	}

	public void setShowMenuBar(boolean flag) {
		showMenuBar = flag;
	}

	public void setShowToolBar(boolean toolbar, boolean help) {
		showToolBar = toolbar;

		if (showToolBar) {
			getGuiManager().setShowToolBarHelp(help);
		}
	}

	public boolean showToolBar() {
		return showToolBar;
	}

	public boolean showMenuBar() {
		return showMenuBar;
	}

	public void setUndoActive(boolean flag) {
		// don't allow undo when running with restricted permissions
		if (flag && !hasFullPermissions) {
			flag = false;
		}
		
		if (kernel.isUndoActive() == flag)
			return;
		
		kernel.setUndoActive(flag);
		if (flag) {
			kernel.initUndoInfo();
		}

		if (appGuiManager != null)
			getGuiManager().updateActions();

		isSaved = true;
	}

	public boolean isUndoActive() {
		return kernel.isUndoActive();
	}

	/**
	 * Enables or disables right clicking in this application. This is useful
	 * for applets.
	 */
	public void setRightClickEnabled(boolean flag) {
		rightClickEnabled = flag;
	}

	/**
	 * Enables or disables popups when multiple objects selected This is useful
	 * for applets.
	 */
	public void setChooserPopupsEnabled(boolean flag) {
		chooserPopupsEnabled = flag;
	}
	
	/**
	 * Enables or disables label dragging in this application. This is useful
	 * for applets.
	 */
	public void setLabelDragsEnabled(boolean flag) {
		labelDragsEnabled = flag;
	}

	final public boolean isRightClickEnabled() {
		return rightClickEnabled;
	}

	final public boolean areChooserPopupsEnabled() {
		return chooserPopupsEnabled;
	}

	final public boolean isLabelDragsEnabled() {
		return labelDragsEnabled;
	}

	public boolean letRename() {
		return true;
	}

	public boolean letDelete() {
		return true;
	}

	public boolean letRedefine() {
		return true;
	}

	public boolean letShowPopupMenu() {
		return rightClickEnabled;
	}

	public boolean letShowPropertiesDialog() {
		return rightClickEnabled;
	}

	public void updateToolBar() {
		if (!showToolBar)
			return;

		getGuiManager().updateToolbar();

		if (!INITING) {
			if (appletImpl != null)
				SwingUtilities.updateComponentTreeUI(appletImpl.getJApplet());
			if (frame != null)
				SwingUtilities.updateComponentTreeUI(frame);
		}

		setMoveMode();
	}

	public void updateMenubar() {
		// This might work out better for Kojo to keep UI and context menu in sync
//		if (!hasGuiManager())
//			return;
		
		if (!showMenuBar || !hasGuiManager())
			return;

		getGuiManager().updateMenubar();
		getGuiManager().updateActions();
		System.gc();
	}

	private void updateSelection() {
		if (!showMenuBar || !hasGuiManager())
			return;

		getGuiManager().updateMenubarSelection();
	}

	public void updateMenuWindow() {
		if (!showMenuBar || !hasGuiManager())
			return;

		getGuiManager().updateMenuWindow();
		getGuiManager().updateMenuFile();
		System.gc();
	}

	public void updateCommandDictionary() {
		// make sure all macro commands are in dictionary
		fillCommandDict();

		if (appGuiManager != null) {
			getGuiManager().updateAlgebraInput();
		}
	}

	/**
	 * // think about this Downloads the latest jar files from the GeoGebra
	 * server.
	 * 
	 * private void updateGeoGebra() { try { File dest = new File(codebase +
	 * Application.JAR_FILE); URL jarURL = new URL(Application.UPDATE_URL +
	 * Application.JAR_FILE);
	 * 
	 * if (dest.exists()) { // check if jarURL is newer then dest try {
	 * URLConnection connection = jarURL.openConnection(); if
	 * (connection.getLastModified() <= dest.lastModified()) { showMessage("No
	 * update available"); return; } } catch (Exception e) { // we don't know if
	 * the file behind jarURL is newer than dest // so don't do anything
	 * showMessage("No update available: " + (e.getMessage())); return; } } //
	 * copy JAR_FILE if (!CopyURLToFile.copyURLToFile(this, jarURL, dest))
	 * return; // copy properties file dest = new File(codebase +
	 * Application.PROPERTIES_FILE); jarURL = new URL(Application.UPDATE_URL +
	 * Application.PROPERTIES_FILE); if (!CopyURLToFile.copyURLToFile(this,
	 * jarURL, dest)) return; // copy jscl file dest = new File(codebase +
	 * Application.JSCL_FILE); jarURL = new URL(Application.UPDATE_URL +
	 * Application.JSCL_FILE); if (!CopyURLToFile.copyURLToFile(this, jarURL,
	 * dest)) return;
	 * 
	 * 
	 * showMessage("Update finished. Please restart GeoGebra."); } catch
	 * (Exception e) { showError("Update failed: "+ e.getMessage()); } }
	 */

	/**
	 * Clears the current construction. Used for File-New.
	 */
	public void clearConstruction() {
		if (isSaved() || saveCurrentFile()) {
			kernel.clearConstruction();

			kernel.initUndoInfo();
			setCurrentFile(null);
			setMoveMode();
		}
	}

	public void exit() {
		// glassPane is active: don't exit now!
		if (glassPaneListener != null)
			return;

		if (isSaved() || appletImpl != null || saveCurrentFile()) {
			if (appletImpl != null) {
				setApplet(appletImpl);
				appletImpl.showApplet();
			} else {
				frame.setVisible(false);
			}
		}
	}

	public synchronized void exitAll() {
		// glassPane is active: don't exit now!
		if (glassPaneListener != null)
			return;

		getGuiManager().exitAll();
	}

	// returns true for YES or NO and false for CANCEL
	public boolean saveCurrentFile() {
		return getGuiManager().saveCurrentFile();
	}

	/*
	 * public void updateStatusLabelAxesRatio() { if (statusLabelAxesRatio !=
	 * null) statusLabelAxesRatio.setText(
	 * euclidianView.getXYscaleRatioString()); }
	 */

	public void setMode(int mode) {
		if (mode != EuclidianView.MODE_SELECTION_LISTENER)
			currentSelectionListener = null;

		if (appGuiManager != null)
			getGuiManager().setMode(mode);
		else if (euclidianView != null)
			euclidianView.setMode(mode);
	}

	final public int getMode() {
		return euclidianView.getMode();
	}

	/***************************************************************************
	 * SAVE / LOAD methodes
	 **************************************************************************/

	/**
	 * Loads construction file
	 * 
	 * @return true if successful
	 */
	final public boolean loadXML(File file, boolean isMacroFile) {
		try {

			FileInputStream fis = null;
			fis = new FileInputStream(file);
			boolean success = loadXML(fis, isMacroFile);
			if (success && !isMacroFile) {
				setCurrentFile(file);
			}
			return success;
		} catch (Exception e) {
			setCurrentFile(null);
			e.printStackTrace();
			showError(getError("LoadFileFailed") + ":\n" + file);
			return false;
		}
	}

	/**
	 * Loads construction file from URL
	 * 
	 * @return true if successful
	 */
	final public boolean loadXML(URL url, boolean isMacroFile) {
		try {
			boolean success = loadXML(url.openStream(), isMacroFile);
			
			// set current file
			if (!isMacroFile && url.toExternalForm().startsWith("file")) {
				String path = url.getPath();
				path = path.replaceAll("%20", " ");
				File f = new File(path);
				if (f.exists())
					setCurrentFile(f);
			}
			
			return success;
		} catch (Exception e) {
			setCurrentFile(null);
			e.printStackTrace();
			showError(getError("LoadFileFailed") + ":\n" + url);
			return false;
		}
	}

	public boolean loadXML(byte[] zipFile) {
		try {
			myXMLio.readZipFromString(zipFile);

			kernel.initUndoInfo();
			isSaved = true;
			setCurrentFile(null);
			// command list may have changed due to macros
			updateCommandDictionary();
			return true;
		} catch (Exception err) {
			setCurrentFile(null);
			err.printStackTrace();
			return false;
		}
	}

	private boolean loadXML(InputStream is, boolean isMacroFile)
			throws Exception {
		try {
			if (!isMacroFile) {
				setMoveMode();
			}

			BufferedInputStream bis = new BufferedInputStream(is);
			myXMLio.readZipFromInputStream(bis, isMacroFile);
			is.close();
			bis.close();

			if (!isMacroFile) {
				kernel.initUndoInfo();
				isSaved = true;
				setCurrentFile(null);
			}

			// command list may have changed due to macros
			updateCommandDictionary();
			return true;
		} catch (MyError err) {
			setCurrentFile(null);
			showError(err);
			return false;
		}
	}

	/**
	 * Saves all objects.
	 * 
	 * @return true if successful
	 */
	final public boolean saveGeoGebraFile(File file) {
		try {
			setWaitCursor();
			myXMLio.writeGeoGebraFile(file);
			isSaved = true;
			setDefaultCursor();
			return true;
		} catch (Exception e) {
			setDefaultCursor();
			showError("SaveFileFailed");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Saves given macros to file.
	 * 
	 * @return true if successful
	 */
	final public boolean saveMacroFile(File file, ArrayList macros) {
		try {
			setWaitCursor();
			myXMLio.writeMacroFile(file, macros);
			setDefaultCursor();
			return true;
		} catch (Exception e) {
			setDefaultCursor();
			showError("SaveFileFailed");
			e.printStackTrace();
			return false;
		}
	}

	// FKH 20040826
	public String getXML() {
		return myXMLio.getFullXML();
	}

	public void setXML(String xml, boolean clearAll) {
		if (clearAll)
			setCurrentFile(null);

		try {
			myXMLio.processXMLString(xml, clearAll, false);
		} catch (MyError err) {
			err.printStackTrace();
			showError(err);
		} catch (Exception e) {
			e.printStackTrace();
			showError("LoadFileFailed");
		}
	}

	// endFKH

	public String getPreferencesXML() {
		return myXMLio.getPreferencesXML();
	}

	public byte[] getMacroFileAsByteArray() {
		try {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			myXMLio.writeMacroStream(os, kernel.getAllMacros());
			os.flush();
			return os.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void loadMacroFileFromByteArray(byte[] byteArray,
			boolean removeOldMacros) {
		try {
			if (removeOldMacros)
				kernel.removeAllMacros();

			if (byteArray != null) {
				ByteArrayInputStream is = new ByteArrayInputStream(byteArray);
				myXMLio.readZipFromInputStream(is, true);
				is.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	final public MyXMLio getXMLio() {
		return myXMLio;
	}

	public boolean isSaved() {
		return isSaved;
	}

	public void storeUndoInfo() {
		if (isUndoActive()) {
			kernel.storeUndoInfo();
			isSaved = false;
		}
	}

	public void restoreCurrentUndoInfo() {
		if (isUndoActive()) {
			kernel.restoreCurrentUndoInfo();
			isSaved = false;
		}
	}

	/*
	 * final public void clearAll() { // load preferences
	 * GeoGebraPreferences.loadXMLPreferences(this); updateContentPane(); //
	 * clear construction kernel.clearConstruction(); kernel.initUndoInfo();
	 * 
	 * isSaved = true; System.gc(); }
	 */

	/**
	 * Returns gui settings in XML format
	 */
	public String getGUItagXML() {
		StringBuffer sb = new StringBuffer();
		sb.append("<gui>\n");

		sb.append("\t<show");
		sb.append(" algebraView=\"");
		sb.append(showAlgebraView);

		// Michael Borcherds 2008-04-25
		sb.append("\" spreadsheetView=\"");
		sb.append(showSpreadsheet);

		sb.append("\" auxiliaryObjects=\"");
		sb.append(showAuxiliaryObjects);
		sb.append("\" algebraInput=\"");
		sb.append(showAlgebraInput);
		sb.append("\" cmdList=\"");
		sb.append(showCmdList);
		sb.append("\"/>\n");

		if (sp2 != null || sp != null) {
			sb.append("\t<splitDivider");
			sb.append(" loc=\"");
			sb.append(initSplitDividerLocationHOR);
			sb.append("\" locVertical=\"");
			sb.append(initSplitDividerLocationVER);
			sb.append("\" loc2=\""); // bugfix Michael Borcherds 2008-04-24
			// added \" at start
			sb.append(initSplitDividerLocationHOR2);
			sb.append("\" locVertical2=\"");
			sb.append(initSplitDividerLocationVER2);
			sb.append("\" horizontal=\"");
			if (sp != null)
				sb.append(sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT);
			else
				sb.append(sp2.getOrientation() == JSplitPane.HORIZONTAL_SPLIT);
			sb.append("\"/>\n");
		}

		// save custom toolbar if we have one
		if (appGuiManager != null) {
			String cusToolbar = getGuiManager().getCustomToolbarDefinition();

			if (cusToolbar != null
					&& !cusToolbar.equals(getGuiManager()
							.getDefaultToolbarString())) {
				sb.append("\t<toolbar");
				sb.append(" str=\"");
				sb.append(cusToolbar);
				sb.append("\"/>\n");
			}
		}

		// labeling style
		if (labelingStyle != ConstructionDefaults.LABEL_VISIBLE_AUTOMATIC) {
			sb.append("\t<labelingStyle ");
			sb.append(" val=\"");
			sb.append(labelingStyle);
			sb.append("\"/>\n");
		}

		sb.append("\t<font ");
		sb.append(" size=\"");
		sb.append(appFontSize);
		sb.append("\"/>\n");

		sb.append(getConsProtocolXML());

		sb.append("</gui>\n");

		return sb.toString();
	}

	public String getCompleteUserInterfaceXML() {
		StringBuffer sb = new StringBuffer();

		// save gui tag settings
		sb.append(getGUItagXML());

		// save euclidianView settings
		sb.append(getEuclidianView().getXML());

		// save spreadsheetView settings
		if (showSpreadsheet) {
			sb.append(getGuiManager().getSpreadsheetViewXML());
		}

		// coord style, decimal places settings etc
		sb.append(kernel.getKernelXML());

		// save cas view seeting and cas session
		// if (casView != null) {
		// sb.append(((geogebra.cas.view.CASView) casView).getGUIXML());
		// sb.append(((geogebra.cas.view.CASView) casView).getSessionXML());
		// }

		return sb.toString();
	}

	public String getConsProtocolXML() {
		if (appGuiManager == null)
			return "";

		StringBuffer sb = new StringBuffer();

		// construction protocol
		if (getGuiManager().isUsingConstructionProtocol()) {
			sb.append(getGuiManager().getConsProtocolXML());
		}

		return sb.toString();
	}

	/**
	 * Returns the CodeBase URL.
	 */
	public URL getCodeBase() {
		if (codebase == null) {
			initCodeBase();
		}
		return codebase;
	}
	private URL codebase;
	private static boolean hasFullPermissions = false;
	
	private void initCodeBase() {
		try {
			// application codebase
			String path = GeoGebra.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm();
			// remove "geogebra.jar" from end of codebase string
			if (path.endsWith(JAR_FILES[0] + "!/")) 
				path = path.substring(0, path.length() -  JAR_FILES[0].length() - 2);
			
			// set codebase
			codebase = new URL(path);	
			hasFullPermissions = true;
		} 
		catch (Exception e) {
			System.out.println("GeoGebra is running with restricted permissions.");
			hasFullPermissions = false;
			
			if (appletImpl != null) {
				// applet codebase
				codebase = appletImpl.getJApplet().getCodeBase();
			}
		}
		
		// TODO: remove
		System.out.println("codebase: " + codebase);
	}
	
	final public static boolean hasFullPermissions() {
		return hasFullPermissions;
	}

	/* selection handling */

	final public int selectedGeosSize() {
		return selectedGeos.size();
	}

	final public ArrayList getSelectedGeos() {
		return selectedGeos;
	}

	final public GeoElement getLastCreatedGeoElement() {
		return kernel.getConstruction().getLastGeoElement();
	}

	/**
	 * geos must contain GeoElement objects only.
	 * 
	 * @param geos
	 */
	final public void setSelectedGeos(ArrayList geos) {
		clearSelectedGeos(false);
		if (geos != null) {
			for (int i = 0; i < geos.size(); i++) {
				GeoElement geo = (GeoElement) geos.get(i);
				addSelectedGeo(geo, false);
			}
		}
		kernel.notifyRepaint();
		updateSelection();
	}

	/*
	 * Michael Borcherds 2008-03-03 modified to select all of a layer pass
	 * layer==-1 to select all objects
	 */
	final public void selectAll(int layer) {
		clearSelectedGeos(false);

		Iterator it = kernel.getConstruction().getGeoSetLabelOrder().iterator();
		while (it.hasNext()) {
			GeoElement geo = (GeoElement) it.next();
			if (layer == -1 || geo.getLayer() == layer)
				addSelectedGeo(geo, false);
		}
		kernel.notifyRepaint();
		updateSelection();
	}

	final public void selectAllPredecessors() {

		for (int i = 0; i < selectedGeos.size(); i++) {
			GeoElement geo = (GeoElement) selectedGeos.get(i);
			TreeSet tree = geo.getAllPredecessors();
			Iterator it2 = tree.iterator();
			while (it2.hasNext())
				addSelectedGeo((GeoElement) it2.next(), false);
		}
		kernel.notifyRepaint();
		updateSelection();
	}

	final public void selectAllDescendants() {

		for (int i = 0; i < selectedGeos.size(); i++) {
			GeoElement geo = (GeoElement) selectedGeos.get(i);
			Set tree = geo.getAllChildren();
			Iterator it2 = tree.iterator();
			while (it2.hasNext())
				addSelectedGeo((GeoElement) it2.next(), false);
		}
		kernel.notifyRepaint();
		updateSelection();
	}

	final public void clearSelectedGeos() {
		clearSelectedGeos(true);
		updateSelection();
	}

	public void clearSelectedGeos(boolean repaint) {
		int size = selectedGeos.size();
		if (size > 0) {
			for (int i = 0; i < size; i++) {
				GeoElement geo = (GeoElement) selectedGeos.get(i);
				geo.setSelected(false);
			}
			selectedGeos.clear();
			if (repaint)
				kernel.notifyRepaint();
		}
		updateSelection();
	}

	/**
	 * @param element
	 */
	final public void toggleSelectedGeo(GeoElement geo) {
		toggleSelectedGeo(geo, true);
	}

	final public void toggleSelectedGeo(GeoElement geo, boolean repaint) {
		if (geo == null)
			return;

		boolean contains = selectedGeos.contains(geo);
		if (contains) {
			selectedGeos.remove(geo);
			geo.setSelected(false);
		} else {
			selectedGeos.add(geo);
			geo.setSelected(true);
		}

		if (repaint)
			kernel.notifyRepaint();
		updateSelection();
	}

	final public boolean containsSelectedGeo(GeoElement geo) {
		return selectedGeos.contains(geo);
	}

	final public void removeSelectedGeo(GeoElement geo) {
		removeSelectedGeo(geo, true);
	}

	final public void removeSelectedGeo(GeoElement geo, boolean repaint) {
		if (geo == null)
			return;

		selectedGeos.remove(geo);
		geo.setSelected(false);
		if (repaint)
			kernel.notifyRepaint();
		updateSelection();
	}

	final public void addSelectedGeo(GeoElement geo) {
		addSelectedGeo(geo, true);
	}

	final public void addSelectedGeo(GeoElement geo, boolean repaint) {
		if (geo == null || selectedGeos.contains(geo))
			return;

		selectedGeos.add(geo);
		geo.setSelected(true);
		if (repaint)
			kernel.notifyRepaint();
		updateSelection();
	}

	// remember split divider location
	public class DividerChangeListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent e) {
			Number value = (Number) e.getNewValue();
			int newDivLoc = value.intValue();

			// first split pane
			if (e.getSource() == sp) {
				if (sp.getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
					setSplitDividerLocationHOR(newDivLoc);
				else
					setSplitDividerLocationVER(newDivLoc);
				isSaved = false;
			}
			// second split pane
			else if (e.getSource() == sp2) {
				if (sp2.getOrientation() == JSplitPane.HORIZONTAL_SPLIT)
					setSplitDividerLocationHOR2(newDivLoc);
				else
					setSplitDividerLocationVER2(newDivLoc);
				isSaved = false;
			}

			if (appletImpl != null)
				SwingUtilities.updateComponentTreeUI(appletImpl.getJApplet());
		}
	}

	/* Event dispatching */
	private GlassPaneListener glassPaneListener;

	public void startDispatchingEventsTo(JComponent comp) {
		if (appGuiManager != null) {
			getGuiManager().closeOpenDialogs();
		}

		if (glassPaneListener == null) {
			Component glassPane = getGlassPane();
			if (glassPane == null)
				return;

			glassPaneListener = new GlassPaneListener(glassPane,
					getContentPane(), comp);

			// mouse
			glassPane.addMouseListener(glassPaneListener);
			glassPane.addMouseMotionListener(glassPaneListener);

			// keys
			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.addKeyEventDispatcher(glassPaneListener);

			glassPane.setVisible(true);
		}
	}

	public void stopDispatchingEvents() {
		if (glassPaneListener != null) {
			Component glassPane = getGlassPane();
			if (glassPane == null)
				return;

			glassPane.removeMouseListener(glassPaneListener);
			glassPane.removeMouseMotionListener(glassPaneListener);

			KeyboardFocusManager.getCurrentKeyboardFocusManager()
					.removeKeyEventDispatcher(glassPaneListener);

			glassPane.setVisible(false);
			glassPaneListener = null;
		}
	}

	public Component getGlassPane() {
		if (mainComp == frame)
			return frame.getGlassPane();
		else if (appletImpl != null && mainComp == appletImpl.getJApplet())
			return appletImpl.getJApplet().getGlassPane();
		else
			return null;
	}

	public Container getContentPane() {
		if (mainComp == frame)
			return frame.getContentPane();
		else if (appletImpl != null && mainComp == appletImpl.getJApplet())
			return appletImpl.getJApplet().getContentPane();
		else
			return null;
	}

	/*
	 * KeyEventDispatcher implementation to handle key events globally for the
	 * application
	 */
	public boolean dispatchKeyEvent(KeyEvent e) {
		// make sure the event is not consumed
		if (e.isConsumed())
			return true;

		// check if key event came from this main component
		// (needed to take care of multiple application windows or applets)
		Component eventPane = SwingUtilities.getRootPane(e.getComponent());
		Component mainPane = SwingUtilities.getRootPane(mainComp);
		if (eventPane != mainPane) {
			// ESC from dialog: close it
			if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
				Component rootComp = SwingUtilities.getRoot(e.getComponent());
				if (rootComp instanceof JDialog) {
					((JDialog) rootComp).setVisible(false);
					return true;
				}
			}

			// key event came from another window or applet: ignore it
			return false;
		}

		// if the glass pane is visible, don't do anything
		// (there might be an animation running)
		Component glassPane = getGlassPane();
		if (glassPane != null && glassPane.isVisible())
			return false;

		// handle global keys like ESC and function keys
		return getGlobalKeyDispatcher().dispatchKeyEvent(e);
	}

	final public GlobalKeyDispatcher getGlobalKeyDispatcher() {
		if (globalKeyDispatcher == null)
			globalKeyDispatcher = new GlobalKeyDispatcher(this);
		return globalKeyDispatcher;
	}

	public boolean isPrintScaleString() {
		return printScaleString;
	}

	public void setPrintScaleString(boolean printScaleString) {
		this.printScaleString = printScaleString;
	}

	public File getCurrentImagePath() {
		return currentImagePath;
	}

	public void setCurrentImagePath(File currentImagePath) {
		this.currentImagePath = currentImagePath;
	}
	
	/**
	 * Loads text file and returns content as String.
	 */
	public String loadTextFile(String s) {
        StringBuffer sb = new StringBuffer();        
        try {
          InputStream is = Application.class.getResourceAsStream(s);
          BufferedReader br = new BufferedReader(new InputStreamReader(is));
          String thisLine;
          while ((thisLine = br.readLine()) != null) {  
             sb.append(thisLine);
             sb.append("\n");
         }
      }
        catch (Exception e) {
          e.printStackTrace();
          }
          return sb.toString();
      }

	public final boolean isOnTheFlyPointCreationActive() {
		return isOnTheFlyPointCreationActive;
	}

	public final void setOnTheFlyPointCreationActive(
			boolean isOnTheFlyPointCreationActive) {
		this.isOnTheFlyPointCreationActive = isOnTheFlyPointCreationActive;
	}

	public final boolean isErrorDialogsActive() {
		return isErrorDialogsActive;
	}

	public final void setErrorDialogsActive(boolean isErrorDialogsActive) {
		this.isErrorDialogsActive = isErrorDialogsActive;
	}

	public final boolean isShiftDragZoomEnabled() {
		return shiftDragZoomEnabled;
	}

	public final void setShiftDragZoomEnabled(boolean shiftDragZoomEnabled) {
		this.shiftDragZoomEnabled = shiftDragZoomEnabled;
	}

	/**
	 * PluginManager gets API with this H-P Ulven 2008-04-16
	 */
	public GgbAPI getGgbApi() {
		if (ggbapi == null) {
			ggbapi = new GgbAPI(this);
		}

		return ggbapi;
	}

	/*
	 * GgbAPI needs this H-P Ulven 2008-05-25
	 */
	public PluginManager getPluginManager() {
		if (pluginmanager == null) {
			pluginmanager = new PluginManager(this);
		}
		return pluginmanager;
	}// getPluginManager()

	// Michael Borcherds 2008-06-22
	public static void debug(String s) {
		if (PRINT_DEBUG_MESSAGES) {

			if (s == null)
				s = "null";

			Throwable t = new Throwable();
			StackTraceElement[] elements = t.getStackTrace();

			// String calleeMethod = elements[0].getMethodName();
			String callerMethodName = elements[1].getMethodName();
			String callerClassName = elements[1].getClassName();

			// Application.debug("CallerClassName=" + callerClassName +
			// " , Caller method name: " + callerMethodName);
			// Application.debug("Callee method name: " + calleeMethod);

			Calendar calendar = new GregorianCalendar();

			int min = calendar.get(Calendar.MINUTE);
			String minS = (min < 10) ? "0" + min : "" + min;

			int sec = calendar.get(Calendar.SECOND);
			String secS = (sec < 10) ? "0" + sec : "" + sec;

			String srcStr = "[" + callerClassName + "." + callerMethodName
					+ "] at " + calendar.get(Calendar.HOUR) + ":" + minS + ":"
					+ secS;

			srcStr += " free memory: "
					+ (Runtime.getRuntime().freeMemory() / 1024) + "KB";

			// multi line message
			if (s.indexOf("\n") > -1) {
				System.out.println("*** BEGIN Message from " + srcStr);
				System.out.println(s);
				System.out.println("*** END Message from " + srcStr + "\n");
			}
			// one line message
			else {
				System.out.println("*** Message from " + srcStr);
				System.out.println("  " + s + "\n");
			}
		}
	}

	// Michael Borcherds 2008-06-22
	public static void printStacktrace(String message) {
		try {

			throw new Exception(message);

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	// check if we are on a mac
	public static String OS = System.getProperty("os.name").toLowerCase(Locale.US);
	public static boolean MAC_OS = OS.startsWith("mac");
	public static boolean WINDOWS = OS.startsWith("windows"); // Michael Borcherds 2008-03-21
	
			/* current possible values http://mindprod.com/jgloss/properties.html
			 * AIX
		Digital Unix
		FreeBSD
		HP UX
		Irix
		Linux
		Mac OS
		Mac OS X
		MPE/iX
		Netware 4.11
		OS/2
		Solaris
		Windows 2000
		Windows 7
		Windows 95
		Windows 98
		Windows NT
		Windows Vista
		Windows XP */
	
	// make sure still works in the future on eg Windows 9 
	public static boolean WINDOWS_VISTA_OR_LATER = WINDOWS && !OS.startsWith("windows 2000")
														   && !OS.startsWith("windows 95")
														   && !OS.startsWith("windows 98")
														   && !OS.startsWith("windows nt")
														   && !OS.startsWith("windows xp");

	/*
	 * needed for padding in Windows XP or earlier
	 * without check, checkbox isn't shown in Vista, Win 7
	 */
    public void setEmptyIcon(JCheckBoxMenuItem cb) {
        if (!WINDOWS_VISTA_OR_LATER)
                cb.setIcon(getEmptyIcon());
    }

    /*
	 * check for alt pressed (but not ctrl) (or ctrl but not alt on MacOS)
	 */
	public static boolean isAltDown(InputEvent e) {
		// we don't want to act when AltGr is down
		// as it is used eg for entering {[}] is some locales
		// NB e.isAltGraphDown() doesn't work
		if (e.isAltDown() && e.isControlDown())
			return false;

		return MAC_OS ? e.isControlDown() : e.isAltDown();
	}

	public static boolean isControlDown(InputEvent e) {

		/*
		 * debug("isMetaDown = "+e.isMetaDown()); debug("isControlDown =
		 * "+e.isControlDown()); debug("isShiftDown = "+e.isShiftDown());
		 * debug("isAltDown = "+e.isAltDown()); debug("isAltGrDown =
		 * "+e.isAltGraphDown()); debug("fakeRightClick = "+fakeRightClick);
		 */

		if (fakeRightClick)
			return false;

		boolean ret = (MAC_OS && e.isMetaDown()) // Mac: meta down for
				// multiple
				// selection
				|| (!MAC_OS && e.isControlDown()); // non-Mac: Ctrl down for
		// multiple selection

		// debug("isPopupTrigger = "+e.isPopupTrigger());
		// debug("ret = " + ret);
		return ret;
		// return e.isControlDown();
	}

	private static boolean fakeRightClick = false;

	public static boolean isRightClick(MouseEvent e) {

		// right-click returns isMetaDown on MAC_OS
		// so we want to return true for isMetaDown
		// if it occurred first at the same time as
		// a popup trigger
		if (MAC_OS && !e.isMetaDown())
			fakeRightClick = false;

		if (MAC_OS && e.isPopupTrigger() && e.isMetaDown())
			fakeRightClick = true;

		/*
		 * debug("isMetaDown = "+e.isMetaDown()); debug("isControlDown =
		 * "+e.isControlDown()); debug("isShiftDown = "+e.isShiftDown());
		 * debug("isAltDown = "+e.isAltDown()); debug("isAltGrDown =
		 * "+e.isAltGraphDown()); debug("isPopupTrigger = "+e.isPopupTrigger());
		 * debug("fakeRightClick = "+fakeRightClick);
		 */

		if (fakeRightClick)
			return true;

		boolean ret =
		// e.isPopupTrigger() ||
		(MAC_OS && e.isControlDown()) // Mac: ctrl click = right click
				|| (!MAC_OS && e.isMetaDown()); // non-Mac: right click = meta
		// click

		// debug("ret = " + ret);
		return ret;
		// return e.isMetaDown();
	}

	// used by PropertyDialogGeoElement and MenuBarImpl
	// for the Rounding Menus
	final public static int roundingMenuLookup[] = { 0, 1, 2, 3, 4, 5, 10, 15,
			-1, 3, 5, 10, 15 };
	final public static int decimalsLookup[] = { 0, 1, 2, 3, 4, 5, -1, -1, -1,
			-1, 6, -1, -1, -1, -1, 7 };
	final public static int figuresLookup[] = { -1, -1, -1, 9, -1, 10, -1, -1,
			-1, -1, 11, -1, -1, -1, -1, 12 };

	public String[] getRoundingMenu() {
		String[] strDecimalSpaces = {
				getPlain("ADecimalPlaces", "0"),
				getPlain("ADecimalPlace", "1"),
				getPlain("ADecimalPlaces", "2"),
				getPlain("ADecimalPlaces", "3"),
				getPlain("ADecimalPlaces", "4"),
				getPlain("ADecimalPlaces", "5"),
				getPlain("ADecimalPlaces", "10"),
				getPlain("ADecimalPlaces", "15"),
				"---", // separator
				getPlain("ASignificantFigures", "3"),
				getPlain("ASignificantFigures", "5"),
				getPlain("ASignificantFigures", "10"),
				getPlain("ASignificantFigures", "15") };

		// zero is singular in eg French
		if (!isZeroPlural(getLocale()))
			strDecimalSpaces[0] = getPlain("ADecimalPlace", "0");

		return strDecimalSpaces;
	}

	final public static String[] strDecimalSpacesAC = { "0 decimals",
			"1 decimals", "2 decimals", "3 decimals", "4 decimals",
			"5 decimals", "10 decimals", "15 decimals", "", "3 figures",
			"5 figures", "10 figures", "15 figures" };

	// Rounding Menus end

	public void deleteSelectedObjects() {
		if (letDelete()) {
			Object[] geos = getSelectedGeos().toArray();
			for (int i = 0; i < geos.length; i++) {
				GeoElement geo = (GeoElement) geos[i];
				if (!geo.isFixed())
					geo.removeOrSetUndefinedIfHasFixedDescendent();
			}
			storeUndoInfo();
		}

	}

	private static MessageDigest messageDigestMD5 = null;

	public static MessageDigest getMessageDigestMD5()
			throws NoSuchAlgorithmException {
		if (messageDigestMD5 == null)
			messageDigestMD5 = MessageDigest.getInstance("MD5");

		return messageDigestMD5;
	}

	/**
	 * stores an image in the application's imageManager.
	 * 
	 * @return fileName of image stored in imageManager
	 */
	public String createImage(BufferedImage img, String fileName) {
		try {
			// Michael Borcherds 2007-12-10 START moved MD5 code from GeoImage
			// to here
			String zip_directory = "";
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (img == null)
					Application.debug("image==null");
				ImageIO.write(img, "png", baos);
				byte[] fileData = baos.toByteArray();

				MessageDigest md;
				md = MessageDigest.getInstance("MD5");
				byte[] md5hash = new byte[32];
				md.update(fileData, 0, fileData.length);
				md5hash = md.digest();
				zip_directory = convertToHex(md5hash);
			} catch (Exception e) {
				Application.debug("MD5 Error");
				zip_directory = "images";
				// e.printStackTrace();
			}

			String fn = fileName;
			int index = fileName.lastIndexOf(File.separator);
			if (index != -1)
				fn = fn.substring(index + 1, fn.length()); // filename without
			// path
			fn = Util.processFilename(fn);

			// filename will be of form
			// "a04c62e6a065b47476607ac815d022cc\liar.gif"
			fileName = zip_directory + File.separator + fn;

			// Michael Borcherds 2007-12-10 END

			// write and reload image to make sure we can save it
			// without problems
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			myXMLio.writeImageToStream(os, fileName, img);
			os.flush();
			ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());

			// reload the image
			img = ImageIO.read(is);
			is.close();
			os.close();

			setDefaultCursor();
			if (img == null) {
				showError("LoadFileFailed");
				return null;
			}

			// make sure this filename is not taken yet
			BufferedImage oldImg = imageManager.getExternalImage(fileName);
			if (oldImg != null) {
				// image with this name exists already
				if (oldImg.getWidth() == img.getWidth()
						&& oldImg.getHeight() == img.getHeight()) {
					// same size and filename => we consider the images as equal
					return fileName;
				} else {
					// same name but different size: change filename
					// Michael Borcherds: this bit of code should now be
					// redundant as it
					// is near impossible for the filename to be the same unless
					// the files are the same
					int n = 0;
					do {
						n++;
						int pos = fileName.lastIndexOf('.');
						String firstPart = pos > 0 ? fileName.substring(0, pos)
								: "";
						String extension = pos < fileName.length() ? fileName
								.substring(pos) : "";
						fileName = firstPart + n + extension;
					} while (imageManager.getExternalImage(fileName) != null);
				}
			}

			imageManager.addExternalImage(fileName, img);

			return fileName;
		} catch (Exception e) {
			setDefaultCursor();
			e.printStackTrace();
			showError("LoadFileFailed");
			return null;
		} catch (java.lang.OutOfMemoryError t) {
			Application.debug("Out of memory");
			System.gc();
			setDefaultCursor();
			// t.printStackTrace();
			showError("Out of memory error");
			return null;
		}
	}

	// code from freenet
	// http://emu.freenetproject.org/pipermail/cvs/2007-June/040186.html
	// GPL2
	public static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	public static String getExtension(File file) {
		String fileName = file.getName();
		int dotPos = fileName.lastIndexOf('.');

		if (dotPos <= 0 || dotPos == fileName.length() - 1)
			return "";
		else
			return fileName.substring(dotPos + 1).toLowerCase(Locale.US); // Michael
		// Borcherds
		// 2008
		// -
		// 02
		// -
		// 06
		// added
		// .
		// toLowerCase
		// (
		// Locale
		// .
		// US
		// )
	}

	public static File addExtension(File file, String fileExtension) {
		if (file == null)
			return null;
		if (getExtension(file).equals(fileExtension))
			return file;
		else
			return new File(file.getParentFile(), // path
					file.getName() + '.' + fileExtension); // filename
	}

	public static File removeExtension(File file) {
		if (file == null)
			return null;
		String fileName = file.getName();
		int dotPos = fileName.indexOf('.');

		if (dotPos <= 0)
			return file;
		else
			return new File(file.getParentFile(), // path
					fileName.substring(0, dotPos));
	}

	public final LowerCaseDictionary getCommandDictionary() {
		return commandDict;
	}

	final static int MEMORY_CRITICAL = 100 * 1024;
	static Runtime runtime = Runtime.getRuntime();

	public boolean freeMemoryIsCritical() {

		if (runtime.freeMemory() > MEMORY_CRITICAL)
			return false;

		System.gc();

		return runtime.freeMemory() < MEMORY_CRITICAL;
	}

	public long freeMemory() {
		return runtime.freeMemory();
	}

	public long getHeapSize() {
		return runtime.maxMemory();
	}

	public void traceMethodsOn(boolean on) {
		runtime.traceMethodCalls(on);
	}

	public void copyGraphicsViewToClipboard() {

		clearSelectedGeos();

		Thread runner = new Thread() {
			public void run() {
				setWaitCursor();

				EuclidianView ev = getEuclidianView();

				double scale = 2d;
				double size = ev.getExportWidth() * ev.getExportHeight();

				// Windows XP clipboard has trouble with images larger than this
				// at double scale (with scale = 2d)
				// TODO check Mac, Linux??
				if (size > 750000) {
					scale = 2.0 * Math.sqrt(750000 / size);
				}

				// copy drawing pad to the system clipboard
				Image img = ev.getExportImage(scale);

				// make white color transparent in image
				// disabled - seems to cause trouble with eg openoffice on
				// clipboard
				// exporting to PNG then importing OK
				// Transparency.makeColorTransparent((BufferedImage) img,
				// Color.WHITE);

				ImageSelection imgSel = new ImageSelection(img);
				Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
						imgSel, null);

				// this doesn't work very well
				// eg can't paste into Paint (WinXP)
				// GraphicExportDialog export = new GraphicExportDialog(app);
				// export.setDPI("150");
				// export.exportPNG(true);

				setDefaultCursor();
			}
		};
		runner.start();

	}

	private Rectangle screenSize = null;

	/*
	 * gets the screensize (taking into account toolbars etc)
	 */
	public Rectangle getScreenSize() {
		if (screenSize == null) {
			GraphicsEnvironment env = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			screenSize = env.getMaximumWindowBounds();
		}

		return screenSize;

	}

	Cursor transparentCursor = null;
	public boolean useTransparentCursorWhenDragging = false;

	public void setUseTransparentCursorWhenDragging(
			boolean useTransparentCursorWhenDragging) {
		this.useTransparentCursorWhenDragging = useTransparentCursorWhenDragging;
	}

	public Cursor getTransparentCursor() {

		if (transparentCursor == null) {
			int[] pixels = new int[16 * 16];
			Image image = Toolkit.getDefaultToolkit().createImage(
					new MemoryImageSource(16, 16, pixels, 0, 16));
			transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(
					image, new Point(0, 0), "invisibleCursor");
		}
		return transparentCursor;
	}
	
	public boolean onlyGraphicsViewShowing() {
		return !showSpreadsheetView() && !showAlgebraView();
	}

	public AppletImplementation getApplet() {
			return appletImpl;
		
	}

}
