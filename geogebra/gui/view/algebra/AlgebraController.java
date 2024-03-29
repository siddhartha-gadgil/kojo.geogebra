/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/**
 * AlgebraController.java
 *
 * Created on 05. September 2001, 09:11
 */

package geogebra.gui.view.algebra;

import geogebra.euclidian.EuclidianView;
import geogebra.kernel.GeoElement;
import geogebra.kernel.Kernel;
import geogebra.main.Application;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

import javax.swing.tree.TreePath;

public class AlgebraController
	implements MouseListener, MouseMotionListener {
	private Kernel kernel;
	private Application app;
	
	private AlgebraView view;

	//private GeoVector tempVec;
	//private boolean kernelChanged;

	/** Creates new CommandProcessor */
	public AlgebraController(Kernel kernel) {
		this.kernel = kernel;
		app = kernel.getApplication();		
	}

	void setView(AlgebraView view) {
		this.view = view;
	}

	Application getApplication() {
		return app;
	}

	Kernel getKernel() {
		return kernel;
	}
	
	/*
	 * MouseListener implementation for popup menus
	 */
	
	private GeoElement lastSelectedGeo = null;

	public void mouseClicked(java.awt.event.MouseEvent e) {	
		// right click is consumed in mousePressed
		if (e.isConsumed()) return;
		
		// LEFT CLICK on closing cross
		if (view.hitClosingCross(e.getX(), e.getY())) {			
			app.setWaitCursor();
			app.setShowAlgebraView(false);
			app.updateCenterPanel(true);
			app.setDefaultCursor();
			return;				
		}					
		
		// get GeoElement at mouse location		
		TreePath tp = view.getPathForLocation(e.getX(), e.getY());
		GeoElement geo = AlgebraView.getGeoElementForPath(tp);	
	
		// check if we clicked on the 16x16 show/hide icon
		if (geo != null) {
			Rectangle rect = view.getPathBounds(tp);		
			boolean iconClicked = rect != null && e.getX() - rect.x < 16; // distance from left border				
			if (iconClicked) {
				// icon clicked: toggle show/hide
				geo.setEuclidianVisible(!geo.isSetEuclidianVisible());
				geo.update();
				app.storeUndoInfo();
				kernel.notifyRepaint();
				return;
			}		
		}
		
		// check double click
		int clicks = e.getClickCount();
		EuclidianView ev = app.getEuclidianView();
		if (clicks == 2) {										
			app.clearSelectedGeos();
			ev.resetMode();
			if (geo != null && !Application.isControlDown(e)) {
				view.startEditing(geo, e.isShiftDown());						
			}
			return;
		} 	
		
		int mode = ev.getMode();
		if (mode == EuclidianView.MODE_MOVE ) {
			// update selection	
			if (geo == null)
				app.clearSelectedGeos();
			else {					
				// handle selecting geo
				if (Application.isControlDown(e)) {
					app.toggleSelectedGeo(geo); 	
					if (app.getSelectedGeos().contains(geo)) lastSelectedGeo = geo;
				} else if (e.isShiftDown() && lastSelectedGeo != null) {
					boolean nowSelecting = true;
					boolean selecting = false;
					boolean aux = geo.isAuxiliaryObject();
					boolean ind = geo.isIndependent();
					boolean aux2 = lastSelectedGeo.isAuxiliaryObject();
					boolean ind2 = lastSelectedGeo.isIndependent();
					
					if ((aux == aux2 && aux == true) || (aux == aux2 && ind == ind2)) {
						
						Iterator it = kernel.getConstruction().getGeoSetLabelOrder().iterator();
						
						boolean direction = geo.getLabel().compareTo(lastSelectedGeo.getLabel()) < 0;
						
						while (it.hasNext()) {
							GeoElement geo2 = (GeoElement)it.next();
							if ((geo2.isAuxiliaryObject() == aux && aux == true)
									|| (geo2.isAuxiliaryObject() == aux && geo2.isIndependent() == ind)) {
								
								if (direction && geo2 == lastSelectedGeo) selecting = !selecting;
								if (!direction && geo2 == geo) selecting = !selecting;
								
								if (selecting) {
									app.toggleSelectedGeo(geo2);
									nowSelecting = app.getSelectedGeos().contains(geo2);
								}
								
								if (!direction && geo2 == lastSelectedGeo) selecting = !selecting;
								if (direction && geo2 == geo) selecting = !selecting;
							}
						}
					}

					if (nowSelecting) {
						app.addSelectedGeo(geo); 
						lastSelectedGeo = geo;
					} else {
						app.removeSelectedGeo(lastSelectedGeo);
						lastSelectedGeo = null;
					}
					//lastSelectedGeo = geo;
					
				} else {							
					app.clearSelectedGeos();
					app.addSelectedGeo(geo);
					lastSelectedGeo = geo;
				}
			}
		} 
		else if (mode != EuclidianView.MODE_SELECTION_LISTENER) {
			// let euclidianView know about the click
			ev.clickedGeo(geo, e);
		} else 
			// tell selection listener about click
			app.geoElementSelected(geo, false);
		
		// Alt click: copy definition to input field
		if (geo != null && e.isAltDown() && app.showAlgebraInput()) {			
			// F3 key: copy definition to input bar
			app.getGlobalKeyDispatcher().handleFunctionKeyForAlgebraInput(3, geo);			
		}
		
		ev.mouseMovedOver(null);		
	}

	public void mousePressed(java.awt.event.MouseEvent e) {
		view.cancelEditing();

		boolean rightClick = Application.isRightClick(e);
		
		// RIGHT CLICK
		if (rightClick) {
			e.consume();
			
			// get GeoElement at mouse location		
			TreePath tp = view.getPathForLocation(e.getX(), e.getY());
			GeoElement geo = AlgebraView.getGeoElementForPath(tp);	
			if (geo == null) return;
			
			if (!app.containsSelectedGeo(geo)) {
				app.clearSelectedGeos();					
			}
														
			// single selection: popup menu
			if (app.selectedGeosSize() < 2) {				
				app.getGuiManager().showPopupMenu(geo, view, e.getPoint());						
			} 
			// multiple selection: properties dialog
			else {														
				app.getGuiManager().showPropertiesDialog(app.getSelectedGeos());	
			}	
		}
	}

	public void mouseReleased(java.awt.event.MouseEvent e) {
	}

	public void mouseEntered(java.awt.event.MouseEvent p1) {
		view.setClosingCrossHighlighted(false);
	}

	public void mouseExited(java.awt.event.MouseEvent p1) {		
		view.setClosingCrossHighlighted(false);
		//if (kernelChanged) {
		//	app.storeUndoInfo();
		//	kernelChanged = false;
		//}
	}

	// MOUSE MOTION LISTENER
	public void mouseDragged(MouseEvent arg0) {}

	// tell EuclidianView
	public void mouseMoved(MouseEvent e) {		
		if (view.isEditing())
			return;
		
		int x = e.getX();
		int y = e.getY();
		if (view.hitClosingCross(x, y)) {
			view.setClosingCrossHighlighted(true);
		} else {
			view.setClosingCrossHighlighted(false);
			GeoElement geo = AlgebraView.getGeoElementForLocation(view, x, y);
			EuclidianView ev = app.getEuclidianView();
			
			// tell EuclidianView to handle mouse over
			ev.mouseMovedOver(geo);								
			if (geo != null) {
				view.setToolTipText(geo.getLongDescriptionHTML(true, true));				
			} else
				view.setToolTipText(null);			
		}
		
	}

	
}
