/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * AlgoJoinPoints.java
 *
 * Created on 30. August 2001, 21:37
 */

package geogebra.kernel;



/**
 *
 * @author  Markus
 * @version 
 */
public class AlgoJoinPointsRay extends AlgoElement {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private GeoPoint P, Q;  // input
    private GeoRay  ray;     // output       
        
    /** Creates new AlgoJoinPoints */
    AlgoJoinPointsRay(Construction cons, String label, GeoPoint P, GeoPoint Q) {
        super(cons);
        this.P = P;
        this.Q = Q;                
        ray = new GeoRay(cons, P); 
        setInputOutput(); // for AlgoElement
        
        // compute line through P, Q
        compute();      
        ray.setLabel(label);
    }   
    
    protected String getClassName() {
        return "AlgoJoinPointsRay";
    }
    
    // for AlgoElement
    protected void setInputOutput() {
        input = new GeoElement[2];
        input[0] = P;
        input[1] = Q;
        
        output = new GeoElement[1];        
        output[0] = ray;        
        setDependencies(); // done by AlgoElement
    }    
    
    GeoRay getRay() { return ray; }
    GeoPoint getP() { return P; }
    GeoPoint getQ() { return Q; }
    
    // calc the line g through P and Q    
    protected final void compute() {
        // g = P v Q  <=>  g_n : n = P x Q
        // g = cross(P, Q)
        GeoVec3D.lineThroughPoints(P, Q, ray);        
    }   
    
    final public String toString() {
        StringBuffer sb = new StringBuffer();
        
        // Michael Borcherds 2008-03-31
        // simplified to allow better translation
        sb.append(app.getPlain("RayThroughAB",P.getLabel(),Q.getLabel()));

        /*
        sb.append(app.getPlain("RayThrough"));
        sb.append(' ');
        sb.append(P.getLabel());
        sb.append(", ");
        sb.append(Q.getLabel());*/
        
        return sb.toString();
    }
}
