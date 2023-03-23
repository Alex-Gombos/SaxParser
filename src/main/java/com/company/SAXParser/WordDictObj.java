package com.company.SAXParser;

public class WordDictObj {
	// Words from Dictionary
	
	public String sWord = null;
	public String sBaseWord = null;
	public WORD_TYPE wType = WORD_TYPE.NOP;
	public String sLang = null;
	public String sSubType = null;
	public String sOther = null;
	public int idBase = -1;
	public int idWord = -1;
	
	@Override
	public String toString() {
		return this.sWord + ", " +
					this.sBaseWord + ", " +
					this.wType + ", " +
					this.idWord + ", " +
					this.idBase + ", " +
					this.sSubType + ", " +
					this.sLang + ", " +
					this.sOther;
	}

	public String[] returnStringArray(){
		return new String[]{this.toString()};
	}
}
