package geogebra.main;

import geogebra.euclidian.EuclidianView;
import geogebra.export.WorksheetExportDialog;
import geogebra.kernel.ConstructionDefaults;
import geogebra.kernel.GeoAngle;
import geogebra.kernel.GeoBoolean;
import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoNumeric;
import geogebra.kernel.GeoPoint;
import geogebra.kernel.GeoVector;
import geogebra.kernel.Kernel;
import geogebra.kernel.PointProperties;
import geogebra.kernel.arithmetic.ExpressionNode;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.text.JTextComponent;

/**
 * Handles global keys like ESC, DELETE, and function keys.
 * 
 * @author Markus Hohenwarter
 */
public class GlobalKeyDispatcher implements KeyEventDispatcher {
	
	private Application app;

	public GlobalKeyDispatcher(Application app) {
		this.app = app;
	}

	/**
	 * This method is called by the current KeyboardFocusManager 
	 * before they are dispatched to their targets, 
	 * allowing it to handle the key event and consume it. 
	 */
	public boolean dispatchKeyEvent(KeyEvent event) {
		// ignore key events coming from text components (i.e. text fields and text areas)
		if (event.isConsumed() || event.getSource() instanceof JTextComponent) {			
			return false;
		} 	
		 				 
		boolean consumed = false;				
		switch (event.getID()) {
			case KeyEvent.KEY_PRESSED:
				consumed = handleKeyPressed(event);
				break;
				
			case KeyEvent.KEY_TYPED:
				consumed = handleKeyTyped(event);
				break;
		}
		
		if (consumed) {
			event.consume();
		}
		return consumed;
	}
	
	/**
	 * The "key pressed" event is generated when a key is pushed down. 
	 */
	private boolean handleKeyPressed(KeyEvent event) {	
		// GENERAL KEYS: 
		// handle ESC, function keys, zooming with Ctrl +, Ctlr -, etc.
		if (handleGeneralKeys(event)) {
			return true;
		}
				
		// SELECTED GEOS: 
		// handle function keys, arrow keys, +/- keys for selected geos, etc.	
		if (handleSelectedGeosKeys(event, app.getSelectedGeos())) {
			return true;
		}	
		
		return false;
	}
	
	/**
	 * "Key typed" events are higher-level and generally 
	 * do not depend on the platform or keyboard layout. 
	 * They are generated when a Unicode character is entered, 
	 * and are the preferred way to find out about character input.
	 */
	private boolean handleKeyTyped(KeyEvent event) {	
		// ignore key events coming from tables like the spreadsheet to
		// allow start editing
		if (event.getSource() instanceof JTable) {			
			return false;
		} 	
		
		
		// show RENAME dialog when a letter is typed		
		char ch = event.getKeyChar();		
		if (Character.isLetter(ch) && 
			 !event.isMetaDown() &&
			 !event.isAltDown() &&
			 !event.isControlDown()) 
		{
			GeoElement geo;					
			if (app.selectedGeosSize() == 1) {
				// selected geo
				geo = (GeoElement) app.getSelectedGeos().get(0);										
			}				
			else {
				// last created geo
				geo = app.getLastCreatedGeoElement();			
			}	
			
			// open rename dialog
			if (geo != null) {							
				app.getGuiManager().showRenameDialog(geo, true, Character.toString(ch), false);
				return true;
			}
		}		
		
		return false;
	}

	/**
	 * Handles general keys like ESC and function keys that don't involved
	 * selected GeoElements.
	 */
	public boolean handleGeneralKeys(KeyEvent event) {
		boolean consumed = false;

		// ESC and function keys
		switch (event.getKeyCode()) {					
			case KeyEvent.VK_ESCAPE:		
				// ESC: set move mode				
				app.setMoveMode();
				consumed = true;			
				break;	
											
			case KeyEvent.VK_ENTER:	
				// check not spreadsheet
				if (!(event.getSource() instanceof JTable)) {			

					// ENTER: set focus to input field
					if (app.showAlgebraInput() && 
						!app.getGuiManager().getAlgebraInput().hasFocus()) 
					{
						app.getGuiManager().getAlgebraInput().requestFocus(); 
						consumed = true;
					}
				}
				break;			
				
							
			// F9 updates construction
			// cmd-f9 on Mac OS
			case KeyEvent.VK_F9:
				if (!app.isApplet() || app.isRightClickEnabled()) {
					app.getKernel().updateConstruction();
					app.setUnsaved();
					consumed = true;
				}
				break;
		}
						
		// Ctrl key down
		if (Application.isControlDown(event)) {
		
			switch (event.getKeyCode()) {				
			case KeyEvent.VK_1:
				// Ctrl-1: set objects back to the default size (for font size 12)
				changeFontsAndGeoElements(12, false);
				break;
				
			case KeyEvent.VK_2:
				// Ctrl-2: large font size and thicker lines for projectors etc
				int fontSize = Math.min(32, app.getFontSize() + 4);
				changeFontsAndGeoElements(fontSize, false);
				break;
			
			case KeyEvent.VK_3:
				// Ctrl-3: set black/white mode printing and visually impaired users
				changeFontsAndGeoElements(app.getFontSize(), true);
				break;
				
			case KeyEvent.VK_C:
				// Ctrl-shift-c: copy graphcis view to clipboard
				//   should also work in applets with no menubar
				if (event.isShiftDown()) {
					app.copyGraphicsViewToClipboard();	
					consumed = true;
				}
				break;
			
			// Ctrl + F: refresh views
			case KeyEvent.VK_F:
				app.refreshViews();
				consumed = true;								
				break;
				
			// needed on MacOS
			// Cmd + Y: Redo
			case KeyEvent.VK_Y:
				app.getGuiManager().redo();
				consumed = true;
				break;
				
				/*
				 * export <applet> tag to clipboard eg for Moodle
				 */
			case KeyEvent.VK_M:
				if (!app.isApplet() && event.isShiftDown()) {
					app.clearSelectedGeos();
					WorksheetExportDialog d = new WorksheetExportDialog(app); 		

					Toolkit toolkit = Toolkit.getDefaultToolkit();
					Clipboard clipboard = toolkit.getSystemClipboard();
					JPanel appCP = app.getCenterPanel();
					int width, height;
					if (appCP != null) {
						width = appCP.getWidth();
						height = appCP.getHeight();
					} else {
						width = WorksheetExportDialog.DEFAULT_APPLET_WIDTH;
						height = WorksheetExportDialog.DEFAULT_APPLET_HEIGHT;
					}		

					clipboard.setContents(new StringSelection(d.getAppletTag(null, width, height, false)), null);
					d.setVisible(false);
					d.dispose();
					consumed = true;
				}
				break;
				

				
			// ctrl-R updates construction
			// make sure it works in applets without a menubar
			case KeyEvent.VK_R:
				if (!app.isApplet() || app.isRightClickEnabled()) {
					app.getKernel().updateConstruction();
					app.setUnsaved();
					consumed = true;
				}
				break;
				
			// Ctrl-(shift)-Q
			// (deprecated - doesn't work on MacOS)
			case KeyEvent.VK_Q:
				if (event.isShiftDown())
					app.selectAllDescendants();
				else
					app.selectAllPredecessors();
				consumed = true;
				break;
											
			// Ctrl + "+", Ctrl + "-" zooms in or out in graphics view
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_EQUALS:				
				app.getEuclidianView().getEuclidianController().zoomInOut(event);
				app.setUnsaved();
				consumed = true;					
				break;
				
			// Ctrl + D: toggles algebra style: value, definition, command
			case KeyEvent.VK_D:
			case KeyEvent.VK_BACK_QUOTE:
				Kernel kernel = app.getKernel();
				kernel.setAlgebraStyle((kernel.getAlgebraStyle() + 1) % 3);
				kernel.updateConstruction();
				app.setUnsaved();
				consumed = true;
				break;
			}
		}

		return consumed;
	}

	/**
	 * Handle pressed key for selected GeoElements
	 * 
	 * @return if key was consumed
	 */
	private boolean handleSelectedGeosKeys(KeyEvent event, ArrayList geos) {
		if (geos == null || geos.size() == 0)
			return false;
		
		int keyCode = event.getKeyCode();
		
		// FUNCTION and DELETE keys
		switch (keyCode) {
			case KeyEvent.VK_F3:
				// F3 key: copy definition to input field				
				if (geos.size() == 1)
					handleFunctionKeyForAlgebraInput(3, (GeoElement) geos.get(0));
				else {
					// F3 key: copy definitions to input field as list 			
					JTextComponent textComponent = app.getGuiManager().getAlgebraInputTextField();				
					
					StringBuffer sb = new StringBuffer();
					sb.append('{');
					
					Iterator it = geos.iterator();
					while (it.hasNext()) {
						sb.append(((GeoElement) it.next()).getFormulaString(ExpressionNode.STRING_TYPE_GEOGEBRA, false));
						if (it.hasNext()) sb.append(",");
					}
					sb.append('}');

					textComponent.setText(sb.toString());
					break;
					
				}
				return true;
				
			case KeyEvent.VK_F4:
				// F4 key: copy value to input field				
				handleFunctionKeyForAlgebraInput(4, (GeoElement) geos.get(0));
				return true;
				
			case KeyEvent.VK_F5:
				// F5 key: copy label to input field				
				handleFunctionKeyForAlgebraInput(5, (GeoElement) geos.get(0));
				return true;
				
			case KeyEvent.VK_DELETE:
				// DELETE selected objects
				if (!app.isApplet() || app.isRightClickEnabled()) {
					app.deleteSelectedObjects();
					return true;
				}
			
			case KeyEvent.VK_BACK_SPACE:
				// DELETE selected objects
				// Note: ctrl-h generates a KeyEvent.VK_BACK_SPACE event, so check for ctrl too
				if (!event.isControlDown() && (!app.isApplet() || app.isRightClickEnabled())) {
					app.deleteSelectedObjects();
					return true;
				}
				break;						
		}				
		
		// ignore key events coming from tables like the spreadsheet to
		// allow start editing, moving etc
		if (event.getSource() instanceof JTable) {			
			return false;
		} 	
		
		// SPECIAL KEYS
		double changeVal = 0; // later: changeVal = base or -base
		// Shift : base = 0.1
		// Default : base = 1
		// Ctrl : base = 10
		// Alt : base = 100
		double base = 1;
		if (event.isShiftDown())
			base = 0.1;
		if (Application.isControlDown(event))
			base = 10;
		if (event.isAltDown())
			base = 100;

		// check for arrow keys: try to move objects accordingly
		boolean moved = false;
		
		switch (keyCode) {
			case KeyEvent.VK_UP:
				changeVal = base;			
				moved = handleArrowKeyMovement(geos, 0, changeVal);
				break;
	
			case KeyEvent.VK_DOWN:
				changeVal = -base;
				moved = handleArrowKeyMovement(geos, 0, changeVal);
				break;
	
			case KeyEvent.VK_RIGHT:
				changeVal = base;
				moved = handleArrowKeyMovement(geos, changeVal, 0);
				break;
	
			case KeyEvent.VK_LEFT:
				changeVal = -base;
				moved = handleArrowKeyMovement(geos, changeVal, 0);
				break;
		}
	
		
		if (moved)
			return true;

		// F2, PLUS, MINUS keys
		switch (keyCode) {
			case KeyEvent.VK_F2:
				// handle F2 key to start editing first selected element
				if (app.hasGuiManager()) {
					app.getGuiManager().startEditing((GeoElement) geos.get(0));
					return true;
				}			
				break;
				
			case KeyEvent.VK_PLUS:
			case KeyEvent.VK_ADD:
			case KeyEvent.VK_EQUALS:
			case KeyEvent.VK_UP:
			case KeyEvent.VK_RIGHT:
				changeVal = base;
				break;
	
			case KeyEvent.VK_MINUS:
			case KeyEvent.VK_SUBTRACT:
			case KeyEvent.VK_DOWN:
			case KeyEvent.VK_LEFT:
				changeVal = -base;
				break;
		}

		if (changeVal == 0) {
			char keyChar = event.getKeyChar();
			if (keyChar == '+')
				changeVal = base;
			else if (keyChar == '-')
				changeVal = -base;
		}

		// change all geoelements
		if (changeVal != 0) {
			for (int i=geos.size()-1; i>=0; i--) {
				GeoElement geo = (GeoElement) geos.get(i);								

				if (geo.isChangeable()) {
					// update number
					if (geo.isGeoNumeric()) {
						GeoNumeric num = (GeoNumeric) geo;
						double newValue = num.getValue() + changeVal * num.animationIncrement;
						if (num.animationIncrement > Kernel.MIN_PRECISION) {
							// round to decimal fraction, e.g. 2.800000000001 to 2.8
							newValue = app.getKernel().checkDecimalFraction(newValue, 1 / num.animationIncrement);
							if (num.isGeoAngle())
								newValue = Kernel.PI_180 * app.getKernel().checkDecimalFraction(newValue * Kernel.CONST_180_PI, 1 / num.animationIncrement);
							else
								newValue = app.getKernel().checkDecimalFraction(newValue, 1 / num.animationIncrement);
						}
						num.setValue(newValue);					
					} 
					
					// update point on path
					else if (geo.isGeoPoint()) {
						GeoPoint p = (GeoPoint) geo;
						if (p.hasPath()) {
							p.addToPathParameter(changeVal * p.animationIncrement);
						}
					}
				}	
				
				// update parent algo of dependent geo to update randomNumbers
				else if (!geo.isIndependent()) {
					// update labeled random number
					if (geo.isLabelSet() && geo.isGeoNumeric()) {
						GeoNumeric num = (GeoNumeric) geo;
						if (num.isRandomNumber()) {
							num.updateRandomNumber();
						}
					} 
					
					// update parent algorithm for unlabeled random numbers
					// and all other algorithms
					geo.getParentAlgorithm().update();
				}
			}
			
			// update all geos together
			GeoElement.updateCascade(geos, getTempSet());
			app.getKernel().notifyRepaint();
	
			return true;
		}

		return false;
	}
	
	private TreeSet tempSet;	
	private TreeSet getTempSet() {
		if (tempSet == null) {
			tempSet = new TreeSet();
		}
		return tempSet;
	}

	
	/**
	 * Handles function key for given GeoElement:	 
	 * F3: copy definition to input field
	 * F4: copy value to input field
	 * F5: copy name to input field
	 * @param fkey numer
	 */
	public void handleFunctionKeyForAlgebraInput(int fkey, GeoElement geo) {
		if (!app.hasGuiManager() || !app.showAlgebraInput()) 
			return;		
		JTextComponent textComponent = app.getGuiManager().getAlgebraInputTextField();				
				
		switch (fkey) {				
			case 3: // F3 key: copy definition to input field
				textComponent.setText(geo.getDefinitionForInputBar());
				break;
							
			case 4: // F4 key: copy value to input field	
				textComponent.replaceSelection(" " + geo.getValueForInputBar() + " ");
				break;
				
			case 5: // F5 key: copy name to input field					
				textComponent.replaceSelection(" " + geo.getLabel() + " ");
				break;				
		}
				
		textComponent.requestFocusInWindow();			
	}
	
	/**
	 * Tries to move the given objects after pressing an arrow key on the keyboard.
	 * 
	 * @param keyCode: VK_UP, VK_DOWN, VK_RIGHT, VK_LEFT
	 * @return whether any object was moved
	 */
	private boolean handleArrowKeyMovement(ArrayList geos, double xdiff, double ydiff) {	
		GeoElement geo = (GeoElement) geos.get(0);
		
		// don't move slider, they will be handled later
		if (geos.size() == 1 && geo.isGeoNumeric() && geo.isChangeable()) {
			return false;
		}
	
		// set translation vector
		if (tempVec == null)
			tempVec = new GeoVector(app.getKernel().getConstruction());
		double xd = geo.animationIncrement * xdiff;
		double yd = geo.animationIncrement * ydiff;						
		tempVec.setCoords(xd, yd, 0);
		
		// move objects
		boolean moved = GeoElement.moveObjects(geos, tempVec, null);
		
		// nothing moved
		if (!moved) {
			for (int i=0; i< geos.size(); i++) {
				 geo = (GeoElement) geos.get(i);
				// toggle boolean value
				if (geo.isChangeable() && geo.isGeoBoolean()) {
					GeoBoolean bool = (GeoBoolean) geo;
					bool.setValue(!bool.getBoolean());
					bool.updateCascade();
					moved = true;
				}
			}
		}
			

		if (moved)
			app.getKernel().notifyRepaint();

		return moved;
	}
	private GeoVector tempVec;

	/**
	 * Changes the font size of the user interface and construction element styles (thickness,
	 * size) for a given fontSize. 
	 * @param fontSize: 12-32pt
	 * @param grayScale: whether only black should be used as a color
	 * @return whether change was performed
	 */	
	private boolean changeFontsAndGeoElements(int fontSize, boolean blackWhiteMode) {
		if (app.isApplet()) 
			return false;
		
		app.setWaitCursor();
			
		// determine styles
		// set new default line thickness
		int oldFontSize = app.getFontSize();
		int angleSizeIncr = fontSize - oldFontSize;
		int incr = getPointSizeInc(oldFontSize, fontSize);
		
		// construction defaults
		ConstructionDefaults cd = app.getKernel().getConstruction().getConstructionDefaults();
		cd.setDefaultLineThickness(EuclidianView.DEFAULT_LINE_THICKNESS + incr);
		cd.setDefaultPointSize(EuclidianView.DEFAULT_POINT_SIZE + incr);
		cd.setDefaultAngleSize(EuclidianView.DEFAULT_ANGLE_SIZE + angleSizeIncr);
		// blackWhiteMode: set defaults for new GeoElements
		cd.setBlackWhiteMode(blackWhiteMode);
		
		// change application font size
		app.setFontSize(fontSize);
		if (app.hasGuiManager())
			app.getGuiManager().updateSpreadsheetColumnWidths();
		
		// apply styles to to selected or all geos
		Iterator it = null;
		if (app.getSelectedGeos().size() == 0) {
			// change all geos
			it = app.getKernel().getConstruction().getGeoSetConstructionOrder().iterator();
		} else {
			// just change selected geos
			it = app.getSelectedGeos().iterator();
		}	
		while (it.hasNext()) {
			GeoElement geo = (GeoElement) it.next();
			setGeoProperties(geo, incr, incr, angleSizeIncr, blackWhiteMode);
		}
		
		app.getKernel().updateConstruction();
		app.setUnsaved();
		app.storeUndoInfo();
		
		app.setDefaultCursor();
		return true;
	}
	
	private int getPointSizeInc(int oldFontSize, int newFontSize) {
		if (oldFontSize == newFontSize) return 0;
		int step = newFontSize > oldFontSize ? 1 : -1;
		
		int left = Math.min(oldFontSize, newFontSize);
		int right = Math.max(oldFontSize, newFontSize);
		int [] borders = { 16, 22, 28 };
		int incr = 0;
		for (int i=0; i < borders.length; i++) {
			if (left < borders[i] && borders[i] <= right) {
				incr = incr + step;
			}
		}
		
		return incr;
	}
	
	private void setGeoProperties(GeoElement geo, int lineThicknessIncr, int pointSizeIncr, 
			int angleSizeIncr, boolean blackWhiteMode) 
	{
		if (!geo.isGeoText() && !geo.isGeoImage() && !geo.isGeoPolygon()) { // affects bounding box
			int lineThickness = Math.max(0, geo.getLineThickness() + lineThicknessIncr);
			geo.setLineThickness(lineThickness);
		}
			
		if (geo instanceof PointProperties) {
			PointProperties p = (PointProperties) geo;
			int pointSize = Math.max(0, p.getPointSize() + pointSizeIncr);
			p.setPointSize(pointSize);
		}
			
		if (geo.isGeoAngle()) {
			GeoAngle angle = (GeoAngle) geo;
			int angleSize = Math.max(0, angle.getArcSize() + angleSizeIncr);
			angle.setArcSize(angleSize);
		}
			
		if (blackWhiteMode) {
			geo.setAlphaValue(0f);
			geo.setObjColor(Color.black);
		}
	}
	
}
