package geogebra.main;


import geogebra.kernel.GeoBoolean;
import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoPoint;
import geogebra.kernel.GeoText;
import geogebra.kernel.arithmetic.NumberValue;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.text.JTextComponent;


/**
 * Handles all geogebra.gui package related objects and methods for Application.
 * This is done to be able to put class files of geogebra.gui.* packages into a
 * separate gui jar file.
 */
public interface GuiManager {
	
	public boolean isPropertiesDialogSelectionListener();	
	public boolean isInputFieldSelectionListener();
	public void clearPreferences();
	public JComponent getAlgebraView();
	public JComponent getSpreadsheetView();
	public boolean hasSpreadsheetView();
	public void updateSpreadsheetColumnWidths();
	
	public void setColumnWidth(int column, int width);	
	public void setScrollToShow(boolean scrollToShow);
	
	public int getHighestUsedSpreadsheetColumn(); 
	public int getSpreadsheetTraceRow(int traceColumn);
	
	public void attachSpreadsheetView();
	public void detachSpreadsheetView();	
	public void attachAlgebraView();
	public void detachAlgebraView();
	
	public String getSpreadsheetViewXML();
	public String getConsProtocolXML();
	
	public  void setShowAuxiliaryObjects(boolean flag);

	public JComponent getAlgebraInput();
	public JTextComponent getAlgebraInputTextField();
	public void updateAlgebraInput();

	public void doAfterRedefine(GeoElement geo);

	public JComponent getToolbarPanel() ;
	
	public void setShowToolBarHelp(boolean flag);
	
	public void updateToolbar();
	public void allowGUIToRefresh();
	
	public void updateFrameSize();
	public void updateFrameTitle();
	public JFrame createFrame();
	
	public void exitAll();

	public JComponent getConstructionProtocolNavigation();
	
	public void setShowConstructionProtocolNavigation(boolean show);
	public void setShowConstructionProtocolNavigation(boolean show, boolean playButton, double playDelay, boolean protButton);

	public boolean isConsProtNavigationPlayButtonVisible();

	public boolean isConsProtNavigationProtButtonVisible() ;

	/**
	 * Displays the construction protocol dialog
	 */
	public void showConstructionProtocol();

	public JDialog getConstructionProtocol() ;
	public void setConstructionStep(int step);
	public void updateConstructionProtocol();
	
	public boolean isUsingConstructionProtocol() ;
	                                              

	public int getToolBarHeight();

	public String getDefaultToolbarString();
	public void updateFonts() ;

	public void setLabels() ;
	public void initMenubar();
	public void updateMenubar();
	public void updateMenubarSelection();
	public void updateMenuWindow();
	public void updateMenuFile();

	public JMenuBar getMenuBar() ;

	public void setMenubar(JMenuBar newMenuBar);

	public void showAboutDialog();

	public void showPrintPreview();

	public void showDrawingPadPopup(Component invoker, Point p);

	/**
	 * Displays the popup menu for geo at the position p in the coordinate space
	 * of the component invoker
	 */
	public void showPopupMenu(GeoElement geo, Component invoker, Point p);
	
	/**
	 * Displays the porperties dialog for geos
	 */
	public void showPropertiesDialog(ArrayList geos) ;


	public void showPropertiesDialog();

	/**
	 * Displays the porperties dialog for the drawing pad
	 */
	public void showDrawingPadPropertiesDialog();
	/**
	 * Displays the configuration dialog for the toolbar
	 */
	public void showToolbarConfigDialog() ;


	/**
	 * Displays the rename dialog for geo
	 */
	public void showRenameDialog(GeoElement geo, boolean storeUndo,
			String initText, boolean selectInitText);

	/**
	 * Displays the text dialog for a given text.
	 */
	public void showTextDialog(GeoText text);
	/**
	 * Creates a new text at given startPoint
	 */
	public void showTextCreationDialog(GeoPoint startPoint) ;



	public JDialog createTextDialog(GeoText text, GeoPoint startPoint);

	/**
	 * Displays the redefine dialog for geo
	 * 
	 * @param allowTextDialog: whether text dialog should be used for texts
	 */
	public void showRedefineDialog(GeoElement geo, boolean allowTextDialog);

	/**
	 * Creates a new slider at given location (screen coords).
	 * 
	 * @return whether a new slider (number) was create or not
	 */
	public boolean showSliderCreationDialog(int x, int y) ;

	/**
	 * Creates a new image at the given location (real world coords).
	 * 
	 * @return whether a new image was create or not
	 */
	public boolean loadImage(GeoPoint loc, boolean fromClipboard);

	public Color showColorChooser(Color currentColor);

	/**
	 * gets String from clipboard Michael Borcherds 2008-04-09
	 */
	public String getStringFromClipboard();
	/**
	 * gets an image from the clipboard Then the image file is loaded and stored
	 * in this application's imageManager. Michael Borcherds 2008-05-10
	 * 
	 * @return fileName of image stored in imageManager
	 */
	public String getImageFromClipboard() ;

	public void initFileChooser();
	public void initPropertiesDialog();

	/**
	 * Shows a file open dialog to choose an image file, Then the image file is
	 * loaded and stored in this application's imageManager.
	 * 
	 * @return fileName of image stored in imageManager
	 */
	public String getImageFromFile();
	
	  // returns true for YES or NO and false for CANCEL
    public boolean saveCurrentFile() ;

    public boolean save() ;
	
	public boolean saveAs() ;
	   

	public File showSaveDialog(String fileExtension, File selectedFile,
			String fileDescription) ;

	public File showSaveDialog(String[] fileExtensions, File selectedFile,
			String[] fileDescriptions) ;


	public void openFile() ;
	public void doOpenFiles(File[] files,
			boolean allowOpeningInThisInstance) ;

	public void doOpenFiles(File[] files,
			boolean allowOpeningInThisInstance, String extension);

	public boolean loadFile(final File file, boolean isMacroFile);

	public void updateActions() ;

	public int getMenuBarHeight() ;

	public int getAlgebraInputHeight() ;

	public AbstractAction getShowAxesAction() ;
	public AbstractAction getShowGridAction() ;



	/**
	 * Creates a new text at given startPoint
	 */
	public void showBooleanCheckboxCreationDialog(Point loc, GeoBoolean bool) ;

	/**
	 * Shows a modal dialog to enter a number or number variable name.
	 */
	public NumberValue showNumberInputDialog(String title, String message,
			String initText) ;

	/**
	 * Shows a modal dialog to enter an angle or angle variable name.
	 * 
	 * @return: Object[] with { NumberValue, AngleInputDialog } pair
	 */
	public Object[] showAngleInputDialog(String title, String message,
			String initText) ;

	
	
	 public void setToolBarDefinition(String toolBarDefinition) ;

	    public String getToolBarDefinition() ;
	  

	    public void removeFromToolbarDefinition(int mode) ;
	    
	    public void addToToolbarDefinition(int mode);
	    
	    public void showURLinBrowser(URL url) ;
	    

	    public void openHelp(String command) ;
	    
	    public void showURLinBrowser(String strURL);
	    
	   

	    /**
	     * Returns text "Created with <ApplicationName>" and link
	     * to application homepage in html.
	     */
	    public String getCreatedWithHTML() ;
	    
	    public void setMode(int mode) ;
	    public void setToolbarMode(int mode);
	    
	    /**
	        *  Exports construction protocol as html 
	        */
	     public void exportConstructionProtocolHTML() ;
	    

		public  String getCustomToolbarDefinition();
		
		public void closeOpenDialogs() ;
		
		public AbstractAction getRedoAction() ;

		public AbstractAction getUndoAction() ;	    	 	
		
		public void startEditing(GeoElement geo);
		 
		public void undo();
		 
		public void redo();
		public void traceToSpreadsheet(GeoElement p);
		public void startCollectingSpreadsheetTraces();
		public void stopCollectingSpreadsheetTraces();
		public void resetSpreadsheet();
		 

}
