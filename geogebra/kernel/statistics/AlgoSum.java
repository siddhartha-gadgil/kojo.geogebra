/* 
GeoGebra - Dynamic Mathematics for Everyone
http://www.geogebra.org

This file is part of GeoGebra.

This program is free software; you can redistribute it and/or modify it 
under the terms of the GNU General Public License as published by 
the Free Software Foundation.

*/

package geogebra.kernel.statistics;

import geogebra.kernel.GeoList;
import geogebra.kernel.GeoNumeric;
import geogebra.kernel.Construction;

/**
 * Mean of a list
 * @author Michael Borcherds
 * @version 2008-02-18
 */

public class AlgoSum extends AlgoStats1D {

	private static final long serialVersionUID = 1L;

	public AlgoSum(Construction cons, String label, GeoList geoList) {
        super(cons,label,geoList,AlgoStats1D.STATS_SIGMAX);
    }
    
    public AlgoSum(Construction cons, String label, GeoList geoList, GeoNumeric n) {
        super(cons,label,geoList,n,AlgoStats1D.STATS_SIGMAX);
    }

    protected String getClassName() {
        return "AlgoSum";
    }
}
