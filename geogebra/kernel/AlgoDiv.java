/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * Area of polygon P[0], ..., P[n]
 *
 */

package geogebra.kernel;

import geogebra.kernel.arithmetic.MyDouble;
import geogebra.kernel.arithmetic.NumberValue;




/**
 * Computes Div[a, b]
 * @author  Markus Hohenwarter
 * @version 
 */
public class AlgoDiv extends AlgoTwoNumFunction {  
        
    AlgoDiv(Construction cons, String label, NumberValue a, NumberValue b) {       
	  super(cons, label, a, b);     
    }   
  
    protected String getClassName() {
        return "AlgoDiv";
    }        
    
    // calc area of conic c 
    protected final void compute() {
    	if (input[0].isDefined() && input[1].isDefined()) {

    		double numerator = a.getDouble();
    		double denominator = b.getDouble();
    		
    		if (Math.abs(numerator) > MyDouble.LARGEST_INTEGER || Math.abs(denominator) > MyDouble.LARGEST_INTEGER) {
    			num.setUndefined();
    			return;
    		}

    		
    		double fraction = numerator / denominator;
    		double integer = Math.round(fraction);	
    		if (kernel.isEqual(fraction, integer)) {
    			num.setValue(integer);
    		} else if (denominator > 0)
    			{
    			double div = Math.floor(fraction);
        		num.setValue(div);
    		} else {
    			double div = Math.ceil(fraction);
        		num.setValue(div); 			
    		}
    	} else
    		num.setUndefined();
    }       
    
}
