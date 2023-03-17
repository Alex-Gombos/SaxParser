package com.company;

import java.util.Vector;

public class WordDictTbl extends Vector<WordDictObj>{

	protected final String [] sColNames = new String [] {
			"ID", "IDBase", "Word", "Base",
			"Type", "SubType", "Lang", "Other"
	};

	public Object getValueAt(final int nRow, final int nCol) {
		final WordDictObj word = this.get(nRow);
		switch (nCol) {
		case 0 : return word.idWord;
		case 1 : return word.idBase;
		case 2: return word.sWord;
		case 3: return word.sBaseWord;
		case 4: return word.wType;
		case 5: return word.sSubType;
		case 6: return word.sLang;
		case 7: return word.sOther;
		}
		return null;
	}


	public int getRowCount() {
		return this.size();
	}


	public int GetRowIndex(final int nRowView) {
		return nRowView;
	}


	public int getColumnCount() {
		return sColNames.length;
	}


	public String[] GetColNames() {
		return sColNames;
	}

}
