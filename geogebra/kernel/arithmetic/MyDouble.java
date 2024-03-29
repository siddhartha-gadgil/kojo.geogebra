/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

/*
 * MyDouble.java
 *
 * Created on 07. Oktober 2001, 12:23
 */

package geogebra.kernel.arithmetic;

import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoNumeric;
import geogebra.kernel.Kernel;
import geogebra.util.MyMath;

import java.util.HashSet;

/**
 *
 * @author  Markus Hohenwarter
 * @version 
 */
public class MyDouble  extends ValidExpression 
implements NumberValue {

    private double val;
    private boolean isAngle = false;    
    
    private Kernel kernel;
    
    public static double LARGEST_INTEGER = 9007199254740992.0; // 0x020000000000000
    
    public MyDouble(Kernel kernel) {
    	this(kernel, 0.0);
    }
    
    /** Creates new MyDouble */
    public MyDouble(Kernel kernel, double x) {
    	this.kernel = kernel;
        val = x;                      
    }
    
    public MyDouble(MyDouble d) {
    	kernel = d.kernel;
        val = d.val;        
        isAngle = d.isAngle;
    }    
    
	public ExpressionValue deepCopy(Kernel kernel) {
		 MyDouble ret = new MyDouble(this);
		 ret.kernel = kernel;
		 return ret;
	}   
    
    final public void set(double x) {
    	val = x; 
    }
    
    public void resolveVariables() {    	
    }

    
	public String toString() {
		if (isAngle) {
			// convert to angle value first, see issue 87
			// http://code.google.com/p/geogebra/issues/detail?id=87
			double angleVal = kernel.convertToAngleValue(val);
			return kernel.formatAngle(angleVal).toString();
		} else {
			return kernel.format(val);
		}
	}
    
	final public String toValueString() {
		return toString();
	}
	
	final public String toLaTeXString(boolean symbolic) {
		return toString();
	}
    
    public void setAngle() { 
    	isAngle = true;
    }
    
    public boolean isAngle() { return isAngle; }
    

    final public MyDouble random() {
    	val = Math.random();
    	isAngle = false; 
    	return this;
    }
    
    /** c = a + b */
    final public static void add(MyDouble a, MyDouble b, MyDouble c) {   
        c.isAngle = a.isAngle && b.isAngle;
        c.set(a.val + b.val); 
    }
    
    /** c = a - b */
    final public static void sub(MyDouble a, MyDouble b, MyDouble c) {
        c.isAngle = a.isAngle && b.isAngle;
        c.set(a.val - b.val); 
    }
    
    /** c = a * b */
    final public static void mult(MyDouble a, MyDouble b, MyDouble c) {
        c.isAngle = a.isAngle || b.isAngle;
    	c.set(a.val * b.val);
    }
    
    /** c = a / b */
    final public static void div(MyDouble a, MyDouble b, MyDouble c) {   	
        c.isAngle = a.isAngle && !b.isAngle;
		c.set(a.val / b.val); 
    }
    
    /** c = pow(a,b) */
    final public static void pow(MyDouble a, MyDouble b, MyDouble c) {    		
        c.isAngle = a.isAngle && !b.isAngle;
    	c.set(Math.pow(a.val, b.val));
    }
    
    final public MyDouble cos() {
    	val = Math.cos(val);
    	isAngle = false;
    	return this; 
    }
    
    final public MyDouble sin() {  
    	val = Math.sin(val);
    	isAngle = false; 
    	return this; 
    }
  
   final public MyDouble tan() {  		
	    // Math.tan() gives a very large number for tan(pi/2)
	    // but should be undefined for pi/2, 3pi/2, 5pi/2, etc.
   		if (kernel.isEqual(val % Math.PI, Kernel.PI_HALF)) {
   			val = Double.NaN;
   		} else {
   			val = Math.tan(val);
   		}  		 
  		isAngle = false;  
  		return this; 
  	}
  	
    final public MyDouble acos() { isAngle = kernel.arcusFunctionCreatesAngle; set(Math.acos(val)); return this;  }
    final public MyDouble asin() { isAngle = kernel.arcusFunctionCreatesAngle; set(Math.asin(val)); return this;  }
    final public MyDouble atan() { isAngle = kernel.arcusFunctionCreatesAngle; set(Math.atan(val)); return this;  }
    
    final public MyDouble log() {  val = Math.log(val);  isAngle = false; return this; }
    final public MyDouble log10() {  val = Math.log(val)/MyMath.LOG10;  isAngle = false; return this; }
    final public MyDouble log2() {  val = Math.log(val)/MyMath.LOG2;  isAngle = false; return this; }
    
    final public MyDouble exp() {  val = Math.exp(val);  isAngle = false; return this; }    
    final public MyDouble sqrt() {  val = Math.sqrt(val); isAngle = false;  return this; }    
    final public MyDouble cbrt() {  val = MyMath.cbrt(val); isAngle = false;  return this; }
    final public MyDouble abs() {  val = Math.abs(val);  return this; }    
	
    final public MyDouble floor() {  
    	// angle in degrees
    	// kernel.checkInteger() needed otherwise floor(60�) gives 59�
		if (isAngle && kernel.getAngleUnit() == Kernel.ANGLE_DEGREE) {
			set(Kernel.PI_180 * Math.floor( kernel.checkInteger(val * Kernel.CONST_180_PI)));	
		}
		else {		
			// number or angle in radians
			set(Math.floor(kernel.checkInteger(val))); 
		}				
		return this;
    }
	
    final public MyDouble ceil() {
    	// angle in degrees
    	// kernel.checkInteger() needed otherwise ceil(241�) fails
		if (isAngle && kernel.getAngleUnit() == Kernel.ANGLE_DEGREE) {
			set(Kernel.PI_180 * Math.ceil(kernel.checkInteger(val * Kernel.CONST_180_PI)));		
		}
		else {		
			// number or angle in radians
			set( Math.ceil(kernel.checkInteger(val)));
		}				
		return this;
    }
	
	final public MyDouble round() {
		// angle in degrees
		if (isAngle && kernel.getAngleUnit() == Kernel.ANGLE_DEGREE) {
			set( Kernel.PI_180 * MyDouble.round(val * Kernel.CONST_180_PI) );		
		}
		else {		
			// number or angle in radians
			set( MyDouble.round(val) );
		}				
		return this;
	}
	
	/*
	 * Java quirk/bug Round(NaN) = 0
	 */
	final public static double round(double x) {
		if (!(Double.isInfinite(x) || Double.isNaN(x)))		
			return Math.round(x);
		else
			return x;
		
	}	
	

	
    final public MyDouble sgn() {  
        val = MyMath.sgn(kernel, val);         
        isAngle = false;
        return this; 
    }    
    
	final public MyDouble cosh() {  
		val = MyMath.cosh(val);
		isAngle = false; 
		return this; 
	}
	
	final public MyDouble sinh() {  
		val = MyMath.sinh(val);
		isAngle = false; 
		return this; 
	}
	
	final public MyDouble tanh() {  
		val = MyMath.tanh(val);
		isAngle = false;  
		return this; 
	}
	
	final public MyDouble acosh() {  
		val = MyMath.acosh(val);
		isAngle = false; 
		return this; 
	}

	final public MyDouble asinh() {  
		val = MyMath.asinh(val);
		isAngle = false; 
		return this; 
	}

	final public MyDouble atanh() {  
		val = MyMath.atanh(val);
		isAngle = false;  
		return this; 
	}
	
	final public MyDouble factorial() {
		val = MyMath.factorial(val);
		isAngle = false;
		return this;
	}
	
	final public MyDouble gamma() {
		val = MyMath.gamma(val, kernel);
		isAngle = false;
		return this;
	}	
  
	final public MyDouble apply(Functional f) {
		val = f.evaluate(val);
		isAngle = false; // want function to return numbers eg f(x) = sin(x), f(45�)
		return this;
	}
    
    /*
     * interface NumberValue
     */    
    final public MyDouble getNumber() {
    	return new MyDouble(this);
    	
    	/* Michael Borcherds 2008-05-20
    	 * removed unstable optimisation
    	 * fails for eg -2 sin(x) - 5 cos(x)
    	if (isInTree()) {
			// used in expression node tree: be careful
    		 return new MyDouble(this);
		} else {
			// not used anywhere: reuse this object
			return this;
		}	      */
    }
    
    
    public boolean isConstant() {
        return true;
    }
    
    final public HashSet getVariables() {
        return null;
    }      
    
    final public boolean isLeaf() {
        return true;
    }
    
    final public ExpressionValue evaluate() {
        return this;
    }
    
    final public double getDouble() {
        return val;
    }
    
	final public GeoElement toGeoElement() {
		GeoNumeric num = new GeoNumeric(kernel.getConstruction());
		num.setValue(val);
		return num;
	}
    
	public boolean isNumberValue() {
		return true;
	}

	public boolean isVectorValue() {
		return false;
	}
	
	public boolean isBooleanValue() {
		return false;
	}

	public boolean isPolynomialInstance() {
		return false;
	}
	
	public boolean isTextValue() {
		return false;
	}   
	
	final public boolean isExpressionNode() {
		return false;
	}
	
 
	public boolean isListValue() {
	    return false;
	}	
     

	
	final public boolean contains(ExpressionValue ev) {
		return ev == this;
	}    	
}
