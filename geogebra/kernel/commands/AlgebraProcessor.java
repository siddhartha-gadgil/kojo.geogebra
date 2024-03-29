package geogebra.kernel.commands;

import geogebra.kernel.AlgoDependentLine;
import geogebra.kernel.CircularDefinitionException;
import geogebra.kernel.Construction;
import geogebra.kernel.GeoAngle;
import geogebra.kernel.GeoBoolean;
import geogebra.kernel.GeoConic;
import geogebra.kernel.GeoElement;
import geogebra.kernel.GeoFunction;
import geogebra.kernel.GeoFunctionable;
import geogebra.kernel.GeoLine;
import geogebra.kernel.GeoList;
import geogebra.kernel.GeoNumeric;
import geogebra.kernel.GeoPoint;
import geogebra.kernel.GeoText;
import geogebra.kernel.GeoVec2D;
import geogebra.kernel.GeoVec3D;
import geogebra.kernel.GeoVector;
import geogebra.kernel.Kernel;
import geogebra.kernel.arithmetic.BooleanValue;
import geogebra.kernel.arithmetic.Command;
import geogebra.kernel.arithmetic.Equation;
import geogebra.kernel.arithmetic.ExpressionNode;
import geogebra.kernel.arithmetic.ExpressionValue;
import geogebra.kernel.arithmetic.Function;
import geogebra.kernel.arithmetic.MyDouble;
import geogebra.kernel.arithmetic.MyList;
import geogebra.kernel.arithmetic.MyStringBuffer;
import geogebra.kernel.arithmetic.NumberValue;
import geogebra.kernel.arithmetic.Parametric;
import geogebra.kernel.arithmetic.Polynomial;
import geogebra.kernel.arithmetic.TextValue;
import geogebra.kernel.arithmetic.ValidExpression;
import geogebra.kernel.arithmetic.VectorValue;
import geogebra.kernel.parser.ParseException;
import geogebra.kernel.parser.Parser;
import geogebra.main.Application;
import geogebra.main.MyError;

import java.util.ArrayList;
import java.util.Iterator;

public class AlgebraProcessor {
	
	private Kernel kernel;
	private Construction cons;
	private Application app;
	private Parser parser;
	protected CommandDispatcher cmdDispatcher;
	
	protected ExpressionValue eval; //ggb3D : used by AlgebraProcessor3D in extended processExpressionNode
	
	public AlgebraProcessor(Kernel kernel) {
		this.kernel = kernel;
		cons = kernel.getConstruction();
		
		cmdDispatcher = new CommandDispatcher(kernel);
		app = kernel.getApplication();
		parser = kernel.getParser();
	}
	
	public Iterator getCmdNameIterator() {
		return cmdDispatcher.getCmdNameIterator();
	}
	
	final public GeoElement[] processCommand(Command c, boolean labelOutput) throws MyError {
		return cmdDispatcher.processCommand(c, labelOutput);
	}
	
	/**
	 * for AlgebraView changes in the tree selection and redefine dialog
	 * @return changed geo
	 */
	public GeoElement changeGeoElement(
			GeoElement geo,
			String newValue,
			boolean redefineIndependent) {
						
			try {
				return changeGeoElementNoExceptionHandling(geo, newValue, redefineIndependent, true);
			} catch (Exception e) {
				app.showError(e.getMessage());
				return null;
			}						
	}	
	
	/**
	 * for AlgebraView changes in the tree selection and redefine dialog
	 * @return changed geo
	 */
	public GeoElement changeGeoElementNoExceptionHandling(GeoElement geo, String newValue, boolean redefineIndependent, boolean storeUndoInfo) 
	throws Exception {
		String oldLabel, newLabel;
		ValidExpression ve;
		GeoElement[] result;

		try {
			ve = parser.parse(newValue);			
			oldLabel = geo.getLabel();
			newLabel = ve.getLabel();

			if (newLabel == null) {
				newLabel = oldLabel;
				ve.setLabel(newLabel);
			}
			
			// make sure that points stay points and vectors stay vectors
			if (ve instanceof ExpressionNode) {
				ExpressionNode n = (ExpressionNode) ve;
				if (geo.isGeoPoint()) 
					n.setForcePoint();
				else if (geo.isGeoVector())
					n.setForceVector();
				else if (geo.isGeoFunction())
					n.setForceFunction();
			}

			if (newLabel.equals(oldLabel)) {
				// try to overwrite                
				result = processValidExpression(ve, redefineIndependent);
				if (result != null && storeUndoInfo)
					app.storeUndoInfo();
				return result[0];
			} else if (cons.isFreeLabel(newLabel)) {
				ve.setLabel(oldLabel);
				// rename to oldLabel to enable overwriting
				result = processValidExpression(ve, redefineIndependent);
				result[0].setLabel(newLabel); // now we rename	
				if (storeUndoInfo)
					app.storeUndoInfo();
				return result[0];
			} else {
				String str[] = { "NameUsed", newLabel };
				throw new MyError(app, str);
			}
		} catch (CircularDefinitionException e) {
			Application.debug("CircularDefinition");
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(app.getError("InvalidInput") + ":\n" + newValue);
		} catch (MyError e) {
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage());
		} catch (Error e) {
			e.printStackTrace();
			throw new Exception(app.getError("InvalidInput") + ":\n" + newValue);
		}
	}
	
	/*
	 * methods for processing an input string
	 */
	// returns non-null GeoElement array when successful
	public GeoElement[] processAlgebraCommand(String cmd, boolean storeUndo) {
		
		try {
			return processAlgebraCommandNoExceptionHandling(cmd, storeUndo);
		} catch (Exception e) {
			app.showError(e.getMessage());
			return null;
		}	
	}
	
	public GeoElement[] processAlgebraCommandNoExceptionHandling(
			String cmd, 
			boolean storeUndo) 
	throws Exception {
		ValidExpression ve;					
		
		try {
			ve = parser.parse(cmd);
		} catch (ParseException e) {
			e.printStackTrace();
			throw new Exception(app.getError("InvalidInput") + ":\n" + cmd);
		} catch (MyError e) {
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage());
		} catch (Error e) {
			e.printStackTrace();
			throw new Exception(app.getError("InvalidInput") + ":\n" + cmd);
		}

		// process ValidExpression (built by parser)     
		GeoElement[] geoElements = null;
		try {
			geoElements = processValidExpression(ve);
			if (storeUndo && geoElements != null)
				app.storeUndoInfo();
		} catch (MyError e) {
			e.printStackTrace();
			throw new Exception(e.getLocalizedMessage());
		} catch (CircularDefinitionException e) {
			Application.debug("CircularDefinition");
			throw e;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new Exception("InvalidInput");
		}
		return geoElements;
	}

	/**
	 * Parses given String str and tries to evaluate it to a double.
	 * Returns Double.NaN if something went wrong.
	 */
	public double evaluateToDouble(String str) {
		try {
			ValidExpression ve = parser.parseExpression(str);
			ExpressionNode en = (ExpressionNode) ve;
			en.resolveVariables();
			NumberValue nv = (NumberValue) en.evaluate();
			return nv.getDouble();
		} catch (Exception e) {
			e.printStackTrace();
			app.showError("InvalidInput");
			return Double.NaN;
		} catch (MyError e) {
			e.printStackTrace();
			app.showError(e);
			return Double.NaN;
		} catch (Error e) {
			e.printStackTrace();
			app.showError("InvalidInput");
			return Double.NaN;
		}
	}
	
	/**
	 * Parses given String str and tries to evaluate it to a GeoBoolean object.
	 * Returns null if something went wrong.
	 */
	public GeoBoolean evaluateToBoolean(String str) {
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		GeoBoolean bool = null;
		try {
			ValidExpression ve = parser.parse(str);		
			GeoElement [] temp = processValidExpression(ve);
			bool = (GeoBoolean) temp[0];
		} catch (CircularDefinitionException e) {
			Application.debug("CircularDefinition");
			app.showError("CircularDefinition");
		} catch (Exception e) {		
			e.printStackTrace();
			app.showError("InvalidInput");
		} catch (MyError e) {
			e.printStackTrace();
			app.showError(e);
		} catch (Error e) {
			e.printStackTrace();
			app.showError("InvalidInput");
		} 
		
		cons.setSuppressLabelCreation(oldMacroMode);
		return bool;
	}

	/**
	 * Parses given String str and tries to evaluate it to a List object.
	 * Returns null if something went wrong.
	 * Michael Borcherds 2008-04-02
	 */
	public GeoList evaluateToList(String str) {
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		GeoList list = null;
		try {
			ValidExpression ve = parser.parse(str);		
			GeoElement [] temp = processValidExpression(ve);
			list = (GeoList) temp[0];
		} catch (CircularDefinitionException e) {
			Application.debug("CircularDefinition");
			app.showError("CircularDefinition");
		} catch (Exception e) {		
			e.printStackTrace();
			app.showError("InvalidInput");
		} catch (MyError e) {
			e.printStackTrace();
			app.showError(e);
		} catch (Error e) {
			e.printStackTrace();
			app.showError("InvalidInput");
		} 
		
		cons.setSuppressLabelCreation(oldMacroMode);
		return list;
	}

	/**
	 * Parses given String str and tries to evaluate it to a GeoFunction
	 * Returns null if something went wrong.
	 * Michael Borcherds 2008-04-04
	 */
	public GeoFunction evaluateToFunction(String str) {
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		GeoFunction func = null;
		try {
			ValidExpression ve = parser.parse(str);		
			GeoElement [] temp = processValidExpression(ve);
			
			if (temp[0].isGeoFunctionable()) {
				GeoFunctionable f = (GeoFunctionable) temp[0];
				func = f.getGeoFunction();
			}						
			else 
				app.showError("InvalidInput");
			
		} catch (CircularDefinitionException e) {
			Application.debug("CircularDefinition");
			app.showError("CircularDefinition");
		} catch (Exception e) {		
			e.printStackTrace();
			app.showError("InvalidInput");
		} catch (MyError e) {
			e.printStackTrace();
			app.showError(e);
		} catch (Error e) {
			e.printStackTrace();
			app.showError("InvalidInput");
		} 
		
		cons.setSuppressLabelCreation(oldMacroMode);
		return func;
	}

	/**
	 * Parses given String str and tries to evaluate it to a NumberValue
	 * Returns null if something went wrong.
	 * Michael Borcherds 2008-08-13
	 */
	public NumberValue evaluateToNumeric(String str) {
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		NumberValue num = null;
		try {
			ValidExpression ve = parser.parse(str);		
			GeoElement [] temp = processValidExpression(ve);
			num = (NumberValue) temp[0];
		} catch (CircularDefinitionException e) {
			Application.debug("CircularDefinition");
			app.showError("CircularDefinition");
		} catch (Exception e) {		
			e.printStackTrace();
			app.showError("InvalidInput");
		} catch (MyError e) {
			e.printStackTrace();
			app.showError(e);
		} catch (Error e) {
			e.printStackTrace();
			app.showError("InvalidInput");
		} 
		
		cons.setSuppressLabelCreation(oldMacroMode);
		return num;
	}

	/**
	 * Parses given String str and tries to evaluate it to a GeoPoint.
	 * Returns null if something went wrong.
	 */
	public GeoPoint evaluateToPoint(String str) {
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		GeoPoint p = null;
		GeoElement [] temp = null;;
		try {
			ValidExpression ve = parser.parse(str);
			if (ve instanceof ExpressionNode) {
				ExpressionNode en = (ExpressionNode) ve;
				en.setForcePoint();	
			}
			 
			 temp = processValidExpression(ve);
			 p = (GeoPoint) temp[0];
		} catch (CircularDefinitionException e) {
			Application.debug("CircularDefinition");
			app.showError("CircularDefinition");
		} catch (Exception e) {		
			e.printStackTrace();
			app.showError("InvalidInput");
		} catch (MyError e) {
			e.printStackTrace();
			app.showError(e);
		} catch (Error e) {
			e.printStackTrace();
			app.showError("InvalidInput");
		} 
		
		cons.setSuppressLabelCreation(oldMacroMode);
		return p;
	}
	
	/**
	 * Parses given String str and tries to evaluate it to a GeoText.
	 * Returns null if something went wrong.
	 */
	public GeoText evaluateToText(String str, boolean createLabel) {
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(!createLabel);

		GeoText text = null;
		GeoElement [] temp = null;;
		try {
			ValidExpression ve = parser.parse(str);			
			temp = processValidExpression(ve);
			text = (GeoText) temp[0];
		} catch (CircularDefinitionException e) {
			Application.debug("CircularDefinition");
			app.showError("CircularDefinition");
		} catch (Exception e) {		
			e.printStackTrace();
			app.showError("InvalidInput");
		} catch (MyError e) {
			e.printStackTrace();
			app.showError(e);
		} catch (Error e) {
			e.printStackTrace();
			app.showError("InvalidInput");
		} 
		
		cons.setSuppressLabelCreation(oldMacroMode);
		return text;
	}

	/**
	 * Checks if label is valid.	 
	 */
	public String parseLabel(String label) throws ParseException {
		return parser.parseLabel(label);
	}

	public GeoElement[] processValidExpression(ValidExpression ve)
		throws MyError, Exception {
		return processValidExpression(ve, true);
	}

	/**
	 * processes valid expression. 
	 * @param ve
	 * @param redefineIndependent == true: independent objects are redefined too
	 * @return
	 * @throws MyError
	 * @throws Exception
	 */
	public GeoElement[] processValidExpression(
		ValidExpression ve,
		boolean redefineIndependent)
		throws MyError, Exception {
			
		// check for existing labels		
		String[] labels = ve.getLabels();
		GeoElement replaceable = null;
		if (labels != null && labels.length > 0) {
			boolean firstTime = true;
			for (int i = 0; i < labels.length; i++) {
				GeoElement geo = kernel.lookupLabel(labels[i]);
				if (geo != null) {
					if (geo.isFixed()) {
						String[] strs =
							{
								"IllegalAssignment",
								"AssignmentToFixed",
								":\n",
								geo.getLongDescription()};
						throw new MyError(app, strs);
					} else {
						// replace (overwrite or redefine) geo
						if (firstTime) { // only one geo can be replaced
							replaceable = geo;
							firstTime = false;
						}
					}
				}
			}
		}
		
		GeoElement[] ret;
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		if (replaceable != null)
			cons.setSuppressLabelCreation(true);
		
		// we have to make sure that the macro mode is
		// set back at the end
		try {
			if (ve instanceof ExpressionNode) {
				ret = processExpressionNode((ExpressionNode) ve);
			}
	
			// Command		
			else if (ve instanceof Command) {
				ret = cmdDispatcher.processCommand((Command) ve, true);
			}
	
			// Equation in x,y (linear or quadratic are valid): line or conic
			else if (ve instanceof Equation) {
				ret = processEquation((Equation) ve);
			}
	
			// explicit Function in x
			else if (ve instanceof Function) {
				ret = processFunction(null, (Function) ve);	
			}						
	
			// Parametric Line        
			else if (ve instanceof Parametric) {
				ret = processParametric((Parametric) ve);
			}
	
//			// Assignment: variable
//			else if (ve instanceof Assignment) {
//				ret = processAssignment((Assignment) ve);
//			} 
			
			else
				throw new MyError(app, "Unhandled ValidExpression : " + ve);
		}
		finally {
			cons.setSuppressLabelCreation(oldMacroMode);
		}
			
		//	try to replace replaceable geo by ret[0]		
		if (replaceable != null && ret != null && ret.length > 0) {						
			// a changeable replaceable is not redefined:
			// it gets the value of ret[0]
			// (note: texts are always redefined)
			if (!redefineIndependent
				&& replaceable.isChangeable()
				&& !(replaceable.isGeoText())) {
				try {
					replaceable.set(ret[0]);
					replaceable.updateRepaint();
					ret[0] = replaceable;
				} catch (Exception e) {					
					String errStr = app.getError("IllegalAssignment") + "\n" +
						replaceable.getLongDescription() + "     =     " 
						+
						ret[0].getLongDescription(); 
					throw new MyError(app, errStr);
				}
			}
			// redefine
			else {
				try {							
					// SPECIAL CASE: set value
					// new and old object are both independent and have same type:
					// simply assign value and don't redefine
					if (replaceable.isIndependent() && ret[0].isIndependent() &&
							replaceable.getGeoClassType() == ret[0].getGeoClassType()) 
					{
						replaceable.set(ret[0]);						
						replaceable.updateRepaint();
						ret[0] = replaceable;										
					}
					
					// STANDARD CASE: REDFINED 
					else {					
						GeoElement newGeo = ret[0];
						cons.replace(replaceable, newGeo);
						
						// now all objects have changed
						// get the new object with same label as our result
						String newLabel = newGeo.isLabelSet() ? newGeo.getLabel() : replaceable.getLabel();
						ret[0] = kernel.lookupLabel(newLabel, false);						
					}
				} catch (CircularDefinitionException e) {
					throw e;
				} catch (Exception e) {
					e.printStackTrace();
					throw new MyError(app, "ReplaceFailed");
				} catch (MyError e) {
					e.printStackTrace();
					throw new MyError(app, "ReplaceFailed");
				}
			}
		}
			
		return ret;
	}

	private GeoElement[] processFunction(ExpressionNode funNode, Function fun) {		
		fun.initFunction();		
		
		String label = fun.getLabel();
		GeoFunction f;
		GeoElement[] ret = new GeoElement[1];

		GeoElement[] vars = fun.getGeoElementVariables();				
		boolean isIndependent = (vars == null || vars.length == 0);
		
		if (isIndependent) {
			f = kernel.Function(label, fun);			
		} else {			
			f = kernel.DependentFunction(label, fun);
		}
		ret[0] = f;		
		return ret;
	}

	private GeoElement[] processEquation(Equation equ) throws MyError {		
//		Application.debug("EQUATION: " + equ);        
//		Application.debug("NORMALFORM POLYNOMIAL: " + equ.getNormalForm());        		
		
		try {
			equ.initEquation();	

			// consider algebraic degree of equation        
			switch (equ.degree()) {
				// linear equation -> LINE   
				case 1 :
					return processLine(equ);
	
				// quadratic equation -> CONIC                                  
				case 2 :
					return processConic(equ);
	
				default :
					throw new MyError(app, "InvalidEquation");
			}
		} 
		catch (MyError eqnError) {
			eqnError.printStackTrace();
			
        	// invalid equation: maybe a function of form "y = <rhs>"?			
			String lhsStr = equ.getLHS().toString().trim();
			if (lhsStr.equals("y")) {
				try {
					// try to create function from right hand side
					Function fun = new Function(equ.getRHS());

					// try to use label of equation							
					fun.setLabel(equ.getLabel());
					return processFunction(null, fun);
				}
				catch (MyError funError) {
					funError.printStackTrace();
				}        
			} 
			
			// throw invalid equation error if we get here
			if (eqnError.getMessage() == "InvalidEquation")
				throw eqnError;
			else {
				String [] errors = {"InvalidEquation", eqnError.getLocalizedMessage()};
				throw new MyError(app, errors);
			}
        }        
	}

	private GeoElement[] processLine(Equation equ) {
		double a = 0, b = 0, c = 0;
		GeoLine line;
		GeoElement[] ret = new GeoElement[1];
		String label = equ.getLabel();
		Polynomial lhs = equ.getNormalForm();
		boolean isExplicit = equ.isExplicit("y");		
		boolean isIndependent = lhs.isConstant();

		if (isIndependent) {
			// get coefficients            
			a = lhs.getCoeffValue("x");
			b = lhs.getCoeffValue("y");
			c = lhs.getCoeffValue("");
			line = kernel.Line(label, a, b, c);
		} else
			line = kernel.DependentLine(label, equ);

		if (isExplicit) {
			line.setToExplicit();
			line.updateRepaint();
		}
		ret[0] = line;
		return ret;
	}

	private GeoElement[] processConic(Equation equ) {
		double a = 0, b = 0, c = 0, d = 0, e = 0, f = 0;
		GeoElement[] ret = new GeoElement[1];
		GeoConic conic;
		String label = equ.getLabel();
		Polynomial lhs = equ.getNormalForm();
		boolean isExplicit = equ.isExplicit("y");
		boolean isSpecific =
			!isExplicit && (equ.isExplicit("yy") || equ.isExplicit("xx"));
		boolean isIndependent = lhs.isConstant();

		if (isIndependent) {
			a = lhs.getCoeffValue("xx");
			b = lhs.getCoeffValue("xy");
			c = lhs.getCoeffValue("yy");
			d = lhs.getCoeffValue("x");
			e = lhs.getCoeffValue("y");
			f = lhs.getCoeffValue("");
			conic = kernel.Conic(label, a, b, c, d, e, f);
		} else
			conic = kernel.DependentConic(label, equ);
		if (isExplicit) {
			conic.setToExplicit();
			conic.updateRepaint();
		} else if (isSpecific || conic.getType() == GeoConic.CONIC_CIRCLE) {
			conic.setToSpecific();
			conic.updateRepaint();
		}
		ret[0] = conic;
		return ret;
	}

	private GeoElement[] processParametric(Parametric par)
		throws CircularDefinitionException {
		
		/*
		ExpressionValue temp = P.evaluate();
        if (!temp.isVectorValue()) {
            String [] str = { "VectorExpected", temp.toString() };
            throw new MyParseError(kernel.getApplication(), str);        
        }

        v.resolveVariables();
        temp = v.evaluate();
        if (!(temp instanceof VectorValue)) {
            String [] str = { "VectorExpected", temp.toString() };
            throw new MyParseError(kernel.getApplication(), str);
        } */       
		
		// point and vector are created silently
		boolean oldMacroMode = cons.isSuppressLabelsActive();
		cons.setSuppressLabelCreation(true);

		// get point
		ExpressionNode node = par.getP();
		node.setForcePoint();
		GeoElement[] temp = processExpressionNode(node);
		GeoPoint P = (GeoPoint) temp[0];

		//	get vector
		node = par.getv();
		node.setForceVector();
		temp = processExpressionNode(node);
		GeoVector v = (GeoVector) temp[0];

		// switch back to old mode
		cons.setSuppressLabelCreation(oldMacroMode);

		// Line through P with direction v
		GeoLine line;
		// independent line
		if (P.isConstant() && v.isConstant()) {
			line = new GeoLine(cons);
			line.setCoords(-v.y, v.x, v.y * P.inhomX - v.x * P.inhomY);
		}
		// dependent line
		else {
			line = kernel.Line(par.getLabel(), P, v);
		}
		line.setToParametric(par.getParameter());
		line.updateRepaint();
		GeoElement[] ret = { line };
		return ret;
	}

	protected GeoElement[] processExpressionNode(ExpressionNode n) throws MyError {					
		// command is leaf: process command
		if (n.isLeaf() && n.getLeft() instanceof Command) {
			Command c = (Command) n.getLeft();
			c.setLabels(n.getLabels());
			return cmdDispatcher.processCommand(c, true);
		}			
		
		// ELSE:  resolve variables and evaluate expressionnode		
		n.resolveVariables();			
		eval = n.evaluate(); //ggb3D : used by AlgebraProcessor3D in extended processExpressionNode		
		boolean dollarLabelFound = false;		
		
		// leaf (no new label specified): just return the existing GeoElement
		if (eval.isGeoElement() &&  n.getLabel() == null) 
		{
			// take care of spreadsheet $ names: don't loose the wrapper ExpressionNode here
			// check if we have a Variable 
			ExpressionNode myNode = n;
			if (myNode.isLeaf()) myNode = myNode.getLeftTree();
			switch (myNode.getOperation()) {
				case ExpressionNode.$VAR_COL:
				case ExpressionNode.$VAR_ROW:
				case ExpressionNode.$VAR_ROW_COL:
					// don't do anything here: we need to keep the wrapper ExpressionNode
					// and must not return the GeoElement here	
					dollarLabelFound = true;
					break;
					
				default:
					// return the GeoElement
					GeoElement[] ret = {(GeoElement) eval };
					return ret;
			}			
		}		
		
		if (eval.isBooleanValue())
			return processBoolean(n, eval);
		else if (eval.isNumberValue())
			return processNumber(n, eval);
		else if (eval.isVectorValue())
			return processPointVector(n, eval);	
		else if (eval.isTextValue())
			return processText(n, eval);		
		else if (eval instanceof Function) {
			return processFunction(n, (Function) eval);			
		} 		
		else if (eval instanceof MyList) {
			return processList(n, (MyList) eval);
		} 
		
		// REMOVED due to issue 131: http://code.google.com/p/geogebra/issues/detail?id=131
//		// expressions like 2 a (where a:x + y = 1)
//		//TODO A1=b doesn't work for these objects
//		else if (eval instanceof GeoLine) {
//			if (((GeoLine)eval).getParentAlgorithm() instanceof AlgoDependentLine) {
//				GeoElement[] ret = {(GeoElement) eval };
//				return ret;
//			}
// 
//		}
		
		// e.g. B1 = A1 where A1 is a GeoElement and B1 does not exist yet
		// create a copy of A1
		else if (eval.isGeoElement()) {
			if (n.getLabel() != null || dollarLabelFound) {
				return processGeoCopy(n.getLabel(), n);	
			}									
		}		
		
		// if we get here, nothing worked
		Application.debug(
				"Unhandled ExpressionNode: " + eval + ", " + eval.getClass());
		return null;
	}

	private GeoElement[] processNumber(
		ExpressionNode n,
		ExpressionValue evaluate) {
		GeoElement[] ret = new GeoElement[1];
		String label = n.getLabel();
		boolean isIndependent = n.isConstant();
		MyDouble eval = ((NumberValue) evaluate).getNumber();
		boolean isAngle = eval.isAngle();
		double value = eval.getDouble();

		if (isIndependent) {
			if (isAngle)
				ret[0] = new GeoAngle(cons, label, value);
			else
				ret[0] = new GeoNumeric(cons, label, value);
		} else {
			ret[0] = kernel.DependentNumber(label, n, isAngle);
		}	
		
		if (n.isForcedFunction()) {
			ret[0] = ((GeoFunctionable)(ret[0])).getGeoFunction();
		}
		
		return ret;
	}
	
	private GeoElement [] processList(ExpressionNode n, MyList evalList) {		
		String label = n.getLabel();		
				
		GeoElement[] ret = new GeoElement[1];
		
		// no operations or no variables are present, e.g.
		// { a, b, 7 } or  { 2, 3, 5 } + {1, 2, 4}
		if (!n.hasOperations() || n.isConstant()) {		
			
			// PROCESS list items to generate a list of geoElements		
			ArrayList geoElements = new ArrayList();
			boolean isIndependent = true;
							
			// make sure we don't create any labels for the list elements
			boolean oldMacroMode = cons.isSuppressLabelsActive();
			cons.setSuppressLabelCreation(true);
			
			int size = evalList.size();
			for (int i=0; i < size; i++) {
				ExpressionNode en = evalList.getListElement(i);
				// we only take one resulting object	
				GeoElement [] results = processExpressionNode(en);						
				GeoElement geo = results[0];										
				
				// add to list
				geoElements.add(geo);						
				if (geo.isLabelSet() || !geo.isIndependent())
					isIndependent = false;			
			}		
			cons.setSuppressLabelCreation(oldMacroMode);
			
			// Create GeoList object			
			ret[0] = kernel.List(label, geoElements, isIndependent);			
		}
		
		// operations and variables are present
		// e.g. {3, 2, 1} + {a, b, 2}
		else {			
			ret[0] = kernel.ListExpression(label, n);			
		}
		
		return ret;
	}

	private GeoElement[] processText(
		ExpressionNode n,
		ExpressionValue evaluate) {
		GeoElement[] ret = new GeoElement[1];
		String label = n.getLabel();

		boolean isIndependent = n.isConstant();

		if (isIndependent) {
			MyStringBuffer eval = ((TextValue) evaluate).getText();
			ret[0] = kernel.Text(label, eval.toValueString());
		} else
			ret[0] = kernel.DependentText(label, n);
		return ret;
	}
	
	private GeoElement[] processBoolean(
		ExpressionNode n,
		ExpressionValue evaluate) {
		GeoElement[] ret = new GeoElement[1];
		String label = n.getLabel();

		boolean isIndependent = n.isConstant();

		if (isIndependent) {				
			ret[0] = kernel.Boolean(label, ((BooleanValue) evaluate).getBoolean());
		} else
			ret[0] = kernel.DependentBoolean(label, n);
		return ret;
	}

	private GeoElement[] processPointVector(
		ExpressionNode n,
		ExpressionValue evaluate) {
		String label = n.getLabel();				        
		
		GeoVec2D p = ((VectorValue) evaluate).getVector();
		
		boolean polar = p.getMode() == Kernel.COORD_POLAR;		
		
		// we want z = 3 + i to give a (complex) GeoPoint not a GeoVector
		boolean complex = p.getMode() == Kernel.COORD_COMPLEX;
		
		GeoVec3D[] ret = new GeoVec3D[1];
		boolean isIndependent = n.isConstant();

		// make vector, if label begins with lowercase character
		if (label != null) {
			if (!(n.isForcedPoint() || n.isForcedVector())) { // may be set by MyXMLHandler
				if (!complex && Character.isLowerCase(label.charAt(0)))
					n.setForceVector();
				else
					n.setForcePoint();
			}
		}
		boolean isVector = n.isVectorValue();
		
		// check for free "complex" points like 2 + i that only depend on i
		if (complex && !isIndependent) {
			GeoElement [] vars = n.getGeoElementVariables();
			if (vars.length == 1 && "i".equals(vars[0].getLabel())) {
				isIndependent = true;			
				complex = true;
			}
	
		}

		if (isIndependent) {
			// get coords
			double x = p.getX();
			double y = p.getY();
			if (isVector)
				ret[0] = kernel.Vector(label, x, y);
			else
				ret[0] = kernel.Point(label, x, y, complex);			
		} else {			
			if (isVector)
				ret[0] = kernel.DependentVector(label, n);
			else
				ret[0] = kernel.DependentPoint(label, n, complex);
		}
		if (polar) {
			ret[0].setMode(Kernel.COORD_POLAR);
			ret[0].updateRepaint();
		} else if (complex) {
			ret[0].setMode(Kernel.COORD_COMPLEX);
			ret[0].updateRepaint();
		}
		return ret;
	}
	
	/** 
	 * Creates a dependent copy of origGeo with label
	 */
	private GeoElement[] processGeoCopy(String copyLabel, ExpressionNode origGeoNode) {
		GeoElement[] ret = new GeoElement[1];
		ret[0] = kernel.DependentGeoCopy(copyLabel, origGeoNode);		
		return ret;
	}

//	/**
//	 * Processes assignments, i.e. input of the form leftVar = geoRight where geoRight is an existing GeoElement.
//	 */
//	private GeoElement[] processAssignment(String leftVar, GeoElement geoRight) throws MyError {		
//		GeoElement[] ret = new GeoElement[1];
//
//		// don't allow copying of dependent functions
//		
//		/*
//		if (
//			geoRight instanceof GeoFunction && !geoRight.isIndependent()) {
//			String[] str = { "IllegalAssignment", rightVar };
//			throw new MyError(app, str);
//		}
//		*/
//
//		
//		GeoElement geoLeft = cons.lookupLabel(leftVar, false);
//		if (geoLeft == null) { // create kernel object and copy values
//			geoLeft = geoRight.copy();
//			geoLeft.setLabel(leftVar);
//			ret[0] = geoLeft;
//		} else { // overwrite
//			ret[0] = geoRight;
//		}
//		
//		
//		if (ret[0] != null && !ret[0].isLabelSet()) {
//			ret[0].setLabel(null);
//		}
//		
//		return ret;
//	}
	

}
