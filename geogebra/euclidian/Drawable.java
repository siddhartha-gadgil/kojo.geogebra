/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * Drawable.java
 *
 * Created on 13. Oktober 2001, 17:40
 */

package geogebra.euclidian;

import geogebra.kernel.Construction;
import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoPoint;
import geogebra.kernel.GeoVector;
import geogebra.kernel.Kernel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.util.ArrayList;

/**
 *
 * @author  Markus
 * @version 
 */
public abstract class Drawable {
	BasicStroke objStroke = EuclidianView.getDefaultStroke();
	BasicStroke selStroke = EuclidianView.getDefaultSelectionStroke();
	BasicStroke decoStroke = EuclidianView.getDefaultStroke();

	private int lineThickness = -1;
	public int lineType = -1;

	protected EuclidianView view;
	protected GeoElement geo;
	public int xLabel, yLabel;
	int mouseX, mouseY; // for Previewables
	protected String labelDesc; // label Description
	private String oldLabelDesc;	
	private boolean labelHasIndex = false;
	Rectangle labelRectangle = new Rectangle(); // for label hit testing
	Shape strokedShape, strokedShape2;

	
	private int lastFontSize = -1;
	
	// tracing	
	protected boolean isTracing = false;
	
	boolean createdByDrawList = false;	

	public abstract void update();
	public abstract void draw(Graphics2D g2);
	public abstract boolean hit(int x, int y);
	public abstract boolean isInside(Rectangle rect);
	public abstract GeoElement getGeoElement();
	public abstract void setGeoElement(GeoElement geo);
	
	public double getxLabel() {
		return xLabel;
	}
	
	public double getyLabel() {
		return yLabel;
	}
	
	void updateFontSize() {
	}
	
	/**
	 * Returns the bounding box of this Drawable in screen coordinates. 
	 * @return null when this Drawable is infinite or undefined	 
	 */
	public Rectangle getBounds() {		
		return null;	
	}
	
	final protected void drawLabel(Graphics2D g2) {
		if (labelDesc == null) return;		
		String label = labelDesc;
		Font oldFont  = null;
		
		// label changed: check for bold or italic tags in caption
		if (oldLabelDesc != labelDesc || labelDesc.startsWith("<")) {					
			boolean italic = false;
			
			// support for bold and italic tags in captions
			// must be whole caption
			if (label.startsWith("<i>") && label.endsWith("</i>")) {
				if (oldFont == null)
					oldFont = g2.getFont();
				
				// use Serif font so that we can get a nice curly italic x
				g2.setFont(view.getApplication().getFont(true, oldFont.getStyle() | Font.ITALIC, oldFont.getSize()));			
				label = label.substring(3, label.length() - 4);
				italic = true;
			} 
	
			if (label.startsWith("<b>") && label.endsWith("</b>")) {			
				if (oldFont == null)
					oldFont = g2.getFont();
	
				g2.setFont(g2.getFont().deriveFont(Font.BOLD + (italic ? Font.ITALIC : 0)));	
				label = label.substring(3, label.length() - 4);			
			}
		}
				
		// no index in label: draw it fast
		int fontSize = g2.getFont().getSize();
		if (oldLabelDesc == labelDesc && !labelHasIndex && lastFontSize == fontSize) {
			g2.drawString(label, xLabel, yLabel);
			labelRectangle.setLocation(xLabel, yLabel - fontSize);
		} 
		else { // label with index or label has changed:
			// do the slower index drawing routine and check for indices
			oldLabelDesc = labelDesc;
			lastFontSize = fontSize;
					
			Point p = drawIndexedString(g2, label, xLabel, yLabel);
			labelHasIndex = p.y > 0;
			labelRectangle.setBounds(xLabel, yLabel - fontSize, p.x, fontSize + p.y);			
		}		
		
		if (oldFont != null)
			g2.setFont(oldFont);
	}			
	
	/**
	 * Adapts xLabel and yLabel to make sure that the label rectangle fits fully on screen.
	 */
	final public void ensureLabelDrawsOnScreen() {
		// draw label and 
		drawLabel(view.g2Dtemp);
		
		// make sure labelRectangle fits on screen horizontally		
		if (xLabel < 3) 
			xLabel = 3;
		else
			xLabel = Math.min(xLabel, view.width - labelRectangle.width - 3);					
		if (yLabel < view.fontSize) 
			yLabel = view.fontSize;
		else
			yLabel = Math.min(yLabel, view.height - 3);	
		
		// update label rectangle position
		labelRectangle.setLocation(xLabel, yLabel - view.fontSize);		
	}
	
	// Michael Borcherds 2008-06-10
	final float textWidth(String str, Font font, FontRenderContext frc)
	{
		if (str.equals("")) return 0f;
		TextLayout layout = new TextLayout(str , font, frc);
		return layout.getAdvance();	
		
	}
	
	
	/* old version
	final void drawMultilineLaTeX(Graphics2D g2, Font font, Color fgColor, Color bgColor) {
		
		int fontSize = g2.getFont().getSize();
		float lineSpread = fontSize * 1.0f;
		float lineSpace = fontSize * 0.5f;

		int maxhOffset=0;
		float height=0;
		
		Dimension dim;
		
		labelDesc=labelDesc.replaceAll("\\$\\$", "\\$"); // replace $$ with $
		labelDesc=labelDesc.replaceAll("\\\\\\[", "\\$");// replace \[ with $
		labelDesc=labelDesc.replaceAll("\\\\\\]", "\\$");// replace \] with $
		labelDesc=labelDesc.replaceAll("\\\\\\(", "\\$");// replace \( with $
		labelDesc=labelDesc.replaceAll("\\\\\\)", "\\$");// replace \) with $
		
		
		String[] lines=labelDesc.split("\n");
		
		
		for (int k=0 ; k<lines.length ; k++)
		{

			String[] strings=lines[k].split("\\$");
			int heights[] = new int[strings.length];

			boolean latex=false;
			if (lines[k].indexOf('$') == -1 && lines.length == 1) 
			{
				latex=true; // just latex
			}

			int maxHeight=0;
			// calculate heights of each element
			for (int j=0 ; j<strings.length ; j++)
			{

				if (!strings[j].equals(str(" ",strings[j].length()))) // check not empty or just spaces
				{
					if (latex)
					{						
						dim = drawEquation(view.getTempGraphics2D(),0,0, strings[j], font, fgColor, bgColor);
						//dim = sHotEqn.getSizeof(strings[j]);
						//widths[j] = dim.width;				
						heights[j] = dim.height;
					}
					else
					{
						heights[j] = (int)lineSpread; //p.y;		
					}
				}
				else
				{
					heights[j]=0;
				}
				latex=!latex;
				if (heights[j] > maxHeight) maxHeight=heights[j];

			}
			
			if (k!=0) maxHeight += lineSpace;
			
			int hOffset=0;
			
			latex=false;
			if (lines[k].indexOf('$') == -1 && lines.length == 1) 
			{
				latex=true; // just latex
				//Application.debug("just latex");
			}

			// draw elements
			for (int j=0 ; j<strings.length ; j++)
			{

				if (!strings[j].equals(str(" ",strings[j].length()))) // check not empty or just spaces
				{
					
					int vOffset = (maxHeight - heights[j] )/2; // vertical centering
					
					if (latex)
					{
						
						dim = drawEquation(g2,xLabel + hOffset,(int)(yLabel + height) + vOffset, strings[j], font, fgColor, bgColor);
						hOffset+=dim.width;
					}
					else
					{				
						Point p = drawIndexedString(g2, strings[j], xLabel + hOffset, yLabel + height + vOffset + lineSpread);
						hOffset+=p.x;
					}
				}
				latex=!latex;
			}
			if (hOffset > maxhOffset) maxhOffset = hOffset;
			height += maxHeight;
		}
		labelRectangle.setBounds(xLabel, yLabel, maxhOffset, (int)height);
	}	

	// returns a string consisting of n consecutive "str"s
	final private String str(String str, int n)
	{
		if (n == 0) return "";
		else if (n == 1) return str;
		else {
			StringBuffer ret = new StringBuffer();
			
			for (int i=0 ; i<n ; i++) ret.append(str);
			return ret.toString();
		}
	} */
	
	
	/**
	 * Draw a multiline LaTeX label. 
	 * 
	 * TODO: Improve performance (caching, etc.)
	 * Florian Sonner
	 * @param g2
	 * @param font
	 * @param fgColor
	 * @param bgColor
	 */
	final void drawMultilineLaTeX(Graphics2D g2, Font font, Color fgColor, Color bgColor) {
		int fontSize = g2.getFont().getSize();
		int lineSpread = (int)(fontSize * 1.0f);
		int lineSpace = (int)(fontSize * 0.5f);
		
		// latex delimiters \[ \] \( \) $$ -> $
		labelDesc = labelDesc.replaceAll("(\\$\\$|\\\\\\[|\\\\\\]|\\\\\\(|\\\\\\))", "\\$");
		
		// split on $ but not \$
		String[] elements = labelDesc.split("(?<![\\\\])(\\$)", -1);
		
		ArrayList lineHeights = new ArrayList();
		lineHeights.add(new Integer(lineSpread + lineSpace));
		ArrayList elementHeights = new ArrayList();
		
		// use latex by default just if there is just a single element
		boolean isLaTeX = (elements.length == 1);
		
		// calculate the required space of every element
		for(int i = 0, currentLine = 0, currentElement = 0; i < elements.length; ++i) {			
			if(isLaTeX) {
				// save the height of this element by drawing it to a temporary buffer
				int height = drawEquation(view.getTempGraphics2D(font), 0, 0, elements[i], font, fgColor, bgColor).height;
				elementHeights.add(new Integer(height));
				
				// check if this element is taller than every else in the line
				if(height > ((Integer)lineHeights.get(currentLine)).intValue())
					lineHeights.set(currentLine, new Integer(height));
				
				++currentElement;
			} else {
				elements[i] = elements[i].replaceAll("\\\\\\$", "\\$");
				String[] lines = elements[i].split("\\n", -1);
				
				for(int j = 0; j < lines.length; ++j) {
					elementHeights.add(new Integer(lineSpread));
					
					// create a new line
					if(j + 1 < lines.length) {
						++currentLine;
						
						lineHeights.add(new Integer(lineSpread + lineSpace));
					}
					
					++currentElement;
				}
			}
			
			isLaTeX = !isLaTeX;
		}
		
		int width = 0;
		int height = 0;
		
		// use latex by default just if there is just a single element
		isLaTeX = (elements.length == 1);
		
		int xOffset = 0;
		int yOffset = 0;
		
		// now draw all elements
		for(int i = 0, currentLine = 0, currentElement = 0; i < elements.length; ++i) {			
			if(isLaTeX) {
				// calculate the y offset of this element by: (lineHeight - elementHeight) / 2
				yOffset = (((Integer)(lineHeights.get(currentLine))).intValue() - ((Integer)(elementHeights.get(currentElement))).intValue()) / 2;
				
				// draw the equation and save the x offset
				xOffset += drawEquation(g2, xLabel + xOffset, (int)(yLabel + height) + yOffset, elements[i], font, fgColor, bgColor).width;
				
				++currentElement;
			} else {
				String[] lines = elements[i].split("\\n", -1);
				
				for(int j = 0; j < lines.length; ++j) {
					// calculate the y offset like done with the element
					yOffset = (((Integer)(lineHeights.get(currentLine))).intValue() - ((Integer)(elementHeights.get(currentElement))).intValue()) / 2;
					
					// draw the string
					xOffset += drawIndexedString(g2, lines[j], xLabel + xOffset, yLabel + height + yOffset + lineSpread).x;
					
					// add the height of this line if more lines follow
					if(j + 1 < lines.length) {
						height += ((Integer)(lineHeights.get(currentLine))).intValue();
						
						if(xOffset > width)
							width = xOffset;
					}
					
					// create a new line if more will follow
					if(j + 1 < lines.length) {
						++currentLine;
						xOffset = 0;
					} 
					
					++currentElement;
				}
			}
			
			// last element, increase total height and check if this is the most wide element
			if(i + 1 == elements.length) {
				height += ((Integer)(lineHeights.get(currentLine))).intValue();
				
				if(xOffset > width)
					width = xOffset;
			}
			
			isLaTeX = !isLaTeX;
		}
		labelRectangle.setBounds(xLabel - 3, yLabel - 3, width + 6, height + 6);
	}

	
	private geogebra.gui.hoteqn.sHotEqn eqn;
	
	final public Dimension drawEquation(Graphics2D g2, int x, int y, String text, Font font, Color fgColor, Color bgColor)
	{
		Dimension dim;
		if (eqn == null) {			
			eqn = new geogebra.gui.hoteqn.sHotEqn(text);
			//Application.debug(eqn.getSize());
			eqn.setDoubleBuffered(false);
			eqn.setEditable(false);	
			eqn.removeMouseListener(eqn);
			eqn.removeMouseMotionListener(eqn);				
			eqn.setDebug(false);
			eqn.setOpaque(false);
		}
		else
		{
			eqn.setEquation(text);
		}

			//setEqnFontSize();																												
			int size = (font.getSize() / 2) * 2; 
			if (size < 10) 
				size = 10;
			else if (size > 28) 
				size = 28;
			
			eqn.setFontname(font.getName());
			eqn.setFontsizes(size, size - 2, size - 4, size - 6);
			eqn.setFontStyle(font.getStyle());

			
			eqn.setForeground(fgColor);		
			eqn.setBackground(bgColor);
		
			
			//eqn.paintComponent(g2Dtemp,0,0);		
			//dim=eqn.getSizeof(text);
			eqn.paintComponent(g2,x,y);		
			dim=eqn.getSize();
			
			//Application.debug(size);
			return dim;
	}
	
	final void drawMultilineText(Graphics2D g2) {
		
		if (labelDesc == null) return;
		
		int lines = 0;				
		int fontSize = g2.getFont().getSize();
		float lineSpread = fontSize * 1.5f;

		Font font = g2.getFont();
		FontRenderContext frc = g2.getFontRenderContext();
		int xoffset = 0, yoffset = 0;

		// no index in text
		if (oldLabelDesc == labelDesc && !labelHasIndex) {		
			// draw text line by line
			int lineBegin = 0;
			int length = labelDesc.length();
			for (int i=0; i < length-1; i++) {
				if (labelDesc.charAt(i) == '\n') {
					//end of line reached: draw this line
					g2.drawString(labelDesc.substring(lineBegin, i), xLabel, yLabel + lines * lineSpread);

					int width=(int)textWidth(labelDesc.substring(lineBegin, i), font, frc);
					if (width > xoffset) xoffset = width;			
					
					lines++;
					lineBegin = i + 1;					
				}
			}
			
			float ypos = yLabel + lines * lineSpread;
			g2.drawString(labelDesc.substring(lineBegin), xLabel, ypos);

			int width=(int)textWidth(labelDesc.substring(lineBegin), font, frc);
			if (width > xoffset) xoffset = width;			
			
			// Michael Borcherds 2008-06-10
			// changed setLocation to setBounds (bugfix)
			// and added final float textWidth()
			//labelRectangle.setLocation(xLabel, yLabel - fontSize);
			int height = (int) ( (lines +1)*lineSpread);
			labelRectangle.setBounds(xLabel, yLabel - fontSize, xoffset, height );
		} 
		else { 			
			// text with indices
			// label description has changed, search for possible indices
			oldLabelDesc = labelDesc;
			
			// draw text line by line
			int lineBegin = 0;
			int length = labelDesc.length();
			xoffset = 0;
			yoffset = 0;
			for (int i=0; i < length-1; i++) {
				if (labelDesc.charAt(i) == '\n') {
					//end of line reached: draw this line
					Point p = drawIndexedString(g2, labelDesc.substring(lineBegin, i), xLabel, yLabel + lines * lineSpread);
					if (p.x > xoffset) xoffset = p.x;
					if (p.y > yoffset) yoffset = p.y;
					lines++;
					lineBegin = i + 1;					
				}
			}
					
			float ypos = yLabel + lines * lineSpread;
			Point p = drawIndexedString(g2, labelDesc.substring(lineBegin), xLabel, ypos);
			if (p.x > xoffset) xoffset = p.x;
			if (p.y > yoffset) yoffset = p.y;
			labelHasIndex = yoffset > 0;			
			int height = (int) ( (lines +1)*lineSpread);
			labelRectangle.setBounds(xLabel, yLabel - fontSize, xoffset, height );
		}
	}		
	
	/**
	 * Draws a string str with possible indices to g2 at position x, y. 
	 * The indices are drawn using the given indexFont. 
	 * Examples for strings with indices: "a_1" or "s_{ab}"
	 * @param g2
	 * @param str
	 * @return additional pixel needed to draw str (x-offset, y-offset) 
	 */
	public static Point drawIndexedString(Graphics2D g2, String str, float xPos, float yPos) {
		Font g2font = g2.getFont();
		Font indexFont = getIndexFont(g2font);
		Font font = g2font;
		TextLayout layout;
		FontRenderContext frc = g2.getFontRenderContext();

		int indexOffset = indexFont.getSize() / 2;
		float maxY = 0;
		int depth = 0;
		float x = xPos;
		float y = yPos;
		int startPos = 0;
		if (str == null) return null;
		int length = str.length();

		for (int i=0; i < length; i++) {
			switch (str.charAt(i)) {
				case '_':			
					//	draw everything before _															
					if (i > startPos) {
						font = (depth == 0) ? g2font : indexFont;
						y = yPos + depth * indexOffset;
						if (y > maxY) maxY = y;			
						String tempStr = str.substring(startPos, i);
						layout = new TextLayout(tempStr, font, frc);
						g2.setFont(font);						
						g2.drawString(tempStr, x, y);			 	
						x += layout.getAdvance();		
					}					
					startPos = i + 1;
					depth++;
			
					// check if next character is a '{' (beginning of index with several chars)
					if (startPos < length && str.charAt(startPos) != '{') {
						font = (depth == 0) ? g2font : indexFont;										
						y = yPos + depth * indexOffset;
						if (y > maxY) maxY = y;
						String tempStr = str.substring(startPos, startPos+1);
						layout = new TextLayout(tempStr, font, frc);
						g2.setFont(font);
						g2.drawString(tempStr, x, y);
						x += layout.getAdvance();	
						depth--;																									
					}
					i++;
					startPos++; 
					break;				
			
				case '}': // end of index with several characters
					if (depth > 0) {						
						if (i > startPos) {
							font = (depth == 0) ? g2font : indexFont;		
							y = yPos + depth * indexOffset;
							if (y > maxY) maxY = y;
							String tempStr = str.substring(startPos, i);
							layout = new TextLayout(tempStr, font, frc);
							g2.setFont(font);
							g2.drawString(tempStr, x, y);
							x += layout.getAdvance();
						}												
						startPos = i+1;
						depth--;		
					}
					break;		
			}		
		}
	
		if (startPos < length) {
			font = (depth == 0) ? g2font : indexFont;
			y = yPos + depth * indexOffset;
			if (y > maxY) maxY = y;
			String tempStr = str.substring(startPos);
			layout = new TextLayout(tempStr, font, frc);
			g2.setFont(font);
			g2.drawString(tempStr, x, y);
			x += layout.getAdvance();
		}	
		g2.setFont(g2font);
		return new Point(Math.round(x - xPos), Math.round(maxY - yPos));
	}
	
	private static Font getIndexFont(Font f) {
		//	index font size should be at least 8pt
		int newSize = Math.max( (int) (f.getSize() * 0.9) , 8);	
		return f.deriveFont(f.getStyle(), newSize);	 	 
	}

	/**
	 * Adds geo's label offset to xLabel and yLabel.
	 * 
	 * @return whether something was changed
	 */
	final protected boolean addLabelOffset() {
		return addLabelOffset(false);
	}
		
	/**
	 * Adds geo's label offset to xLabel and yLabel.
	 * @param ensureLabelOnScreen: if true we make sure that the label is drawn on screen
	 * 
	 * @return whether something was changed
	 */
	final protected boolean addLabelOffset(boolean ensureLabelOnScreen) {
		if (ensureLabelOnScreen) {
			// MAKE SURE LABEL STAYS ON SCREEN
			int xLabelOld = xLabel;
			int yLabelOld = yLabel;			
			xLabel += geo.labelOffsetX;
			yLabel += geo.labelOffsetY;
			
			// change xLabel and yLabel so that label stays on screen
			ensureLabelDrawsOnScreen();
			
			// something changed?
			return xLabelOld != xLabel || yLabelOld != yLabel;
		}
		else {
			// STANDARD BEHAVIOUR
			if (geo.labelOffsetX == 0 && geo.labelOffsetY == 0) return false;
				
			int x = xLabel + geo.labelOffsetX;
			int y = yLabel + geo.labelOffsetY;
			
			// don't let offset move label out of screen
			int xmax = view.width - 15;
			int ymax = view.height - 5;
			if (x < 5 || x > xmax ) return false;
			if (y < 15 || y > ymax) return false;
			
			xLabel = x;
			yLabel = y;
			return true;
		}
	}
	
	/**
	 * Was the label clicked at? (mouse pointer
	 * location (x,y) in screen coords)
	 */		
	public boolean hitLabel(int x, int y) {
		return labelRectangle.contains(x, y);
	}

	final void updateStrokes(GeoElement geo) {
		strokedShape = null;
		strokedShape2 = null;		
		
		if (lineThickness != geo.lineThickness) {
			lineThickness = geo.lineThickness;
			lineType = geo.lineType;

			float width = lineThickness / 2.0f;
			objStroke = EuclidianView.getStroke(width, lineType);
			decoStroke = EuclidianView.getStroke(width, EuclidianView.LINE_TYPE_FULL);
			selStroke =
				EuclidianView.getStroke(
					width + EuclidianView.SELECTION_ADD,
					EuclidianView.LINE_TYPE_FULL);
		} else if (lineType != geo.lineType) {
			lineType = geo.lineType;

			float width = lineThickness / 2.0f;
			objStroke = EuclidianView.getStroke(width, lineType);
		}
	}
	
	final public static void drawWithValueStrokePure(Shape shape, Graphics2D g2) {
		Object oldHint = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);			
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);			
		g2.draw(shape);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldHint);			
	}		
	
	final public static void fillWithValueStrokePure(Shape shape, Graphics2D g2) {
		Object oldHint = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);			
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);	
		g2.fill(shape);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, oldHint);			
	}
	
	//private StringBuffer command = new StringBuffer();
	private double[] coords = new double[2];
	
	public void recordToSpreadsheet(GeoElement geo) {
		
		
		// stop spurious numbers after undo
		if (view.getKernel().isViewReiniting()) return;
		
        // record to spreadsheet tool & trace to spreadsheet
    	Construction cons = view.getKernel().getConstruction();
    	{
    		int row;
    		String col;
	    	
    		switch (geo.getGeoClassType()) {
    		
    		/* TODO: use this, rather than the code in GeoNumeric.update()
    		case GeoElement.GEO_CLASS_NUMERIC:
    			
    			cons.getApplication().getGuiManager().traceToSpreadsheet(geo);
    			
    			break;*/
    			
    		case GeoElement.GEO_CLASS_POINT:
    	    	//Application.debug("GEO_CLASS_POINT");   		
	    		GeoPoint P = (GeoPoint)geo;
	    		
	    		boolean polar = P.getMode() == Kernel.COORD_POLAR;
	    		
		    	if (polar)
		    		P.getPolarCoords(coords);
		    	else
		    		P.getInhomCoords(coords);
		    	
		    	
		    	if (P.getLastTrace1() != coords[0] || P.getLastTrace2() != coords[1]) {
		    		
		    		cons.getApplication().getGuiManager().traceToSpreadsheet(geo);
		    		/*
			    	col = P.getTraceColumn1(); // call before getTraceRow()
			    	row = P.getTraceRow();
			    	if (row > 0) {
		    	    	//Application.debug(col+row);   		
				    	cons.getApplication().getGuiManager().setScrollToShow(true);
				    	
				    	GeoNumeric traceCell = new GeoNumeric(cons,col+row,coords[0]);
				    	traceCell.setAuxiliaryObject(true);
				    	
				    	col = P.getTraceColumn2(); // call before getTraceRow()
		    	    	//Application.debug(col+row);   		
				    	
				    	GeoNumeric traceCell2;
				    	
				    	if (polar) traceCell2 = new GeoAngle(cons,col+row,coords[1]);
				    	else traceCell2 = new GeoNumeric(cons,col+row,coords[1]);
				    	
				    	traceCell2.setAuxiliaryObject(true);
				    	
				    	cons.getApplication().getGuiManager().setScrollToShow(false);	
				    	
				    	P.setLastTrace1(coords[0]);
				    	P.setLastTrace2(coords[1]);
			    	}*/
			    	
		    	}
	    	break;
	    	
    		case GeoElement.GEO_CLASS_VECTOR:
    	        // record to spreadsheet tool
    			GeoVector vector = (GeoVector)geo;


		    	vector.getInhomCoords(coords);
		    	
		    	if (vector.getLastTrace1() != coords[0] || vector.getLastTrace2() != coords[1]) {
		    		
		    		cons.getApplication().getGuiManager().traceToSpreadsheet(geo);
		    		
		    		/*
			    	col = vector.getTraceColumn1();
			    	row = vector.getTraceRow();
			    	if (row > 0) {
				    	cons.getApplication().getGuiManager().setScrollToShow(true);
			    		
			    		GeoNumeric traceCell = new GeoNumeric(cons,col+row,coords[0]);
				    	traceCell.setAuxiliaryObject(true);
				    	GeoNumeric traceCell2 = new GeoNumeric(cons,vector.getTraceColumn2()+row,coords[1]);
				    	traceCell2.setAuxiliaryObject(true);
				    	
				    	cons.getApplication().getGuiManager().setScrollToShow(false);
				    	
				    	vector.setLastTrace1(coords[0]);
				    	vector.setLastTrace2(coords[1]);
			    	}*/
			    	
		    	}
    	    	 	    			
    			break;
    		}
    	}    	
	}

}