package com.company.SAXParser;

import java.util.Vector;


public class VData <T extends ImportGetINTF> extends Vector<T> {
	
	private static final long serialVersionUID = 2634405915301623058L;
	
	final int nColRegex;
	final String [] colNames;
	
	// +++++++++++++++ CONSTRUCTOR +++++++++++++++++
	
	public VData(final int nColRegex, final String [] colNames) {
		this.colNames  = colNames;
		this.nColRegex = nColRegex;
	}
	public VData() {
		colNames = null;
		nColRegex = -1;
	}
	
	// +++++++++++++++++ MEMBER FUNCTIONS +++++++++++
	
	public String [] GetNames() {
		return colNames;
	}
	
	public boolean HasNames() {
		return colNames != null;
	}
	
	public int GetRegexCol() {
		return nColRegex;
	}
}
