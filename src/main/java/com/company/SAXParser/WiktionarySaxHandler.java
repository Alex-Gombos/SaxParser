package com.company.SAXParser;

import java.io.IOException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class WiktionarySaxHandler extends DefaultHandler implements SAXHandlerINTF {
	
	// TODO: some conceptual Refactoring
	
	private final static String [] colNames = {"ID", "IDPage", "Word", "Lang", "Text"};
	private final static int nColRegex = 2;
	
	private final Matcher matchLang = Pattern.compile(
			"==\\{\\{limba\\|([-\\p{L}]++)\\}\\}=="
			+ "|\\{\\{[wW]ikipedia\\|(\\p{L}++)\\}\\}")
			.matcher("");
	private final Matcher matchWParenth = Pattern.compile("(\\([^)]++\\)) *+").matcher("");
	
	public enum WIKI_NODES{
		NONE("", false, null),
		ARTICLE("page", false, WIKI_NODES.NONE), // Base Node
		WORD("title", WIKI_NODES.ARTICLE), ID("id", WIKI_NODES.ARTICLE),
		REV("revision", false, WIKI_NODES.ARTICLE), TEXT("text", WIKI_NODES.REV)
		;
		// ++++++++++++++++++
		private final String sNode;
		private final boolean doRead;
		private final WIKI_NODES parent;
		// ++++++++++++++++++
		private WIKI_NODES(final String node, final WIKI_NODES parent) {
			this.parent = parent;
			this.sNode  = node;
			this.doRead = true;
		}
		private WIKI_NODES(final String node, final boolean readElement, final WIKI_NODES parent) {
			this.parent = (parent == null) ? this : parent;
			this.sNode  = node;
			this.doRead = readElement;
		}
		// ++++++ GET  ++++++
		public static WIKI_NODES GetNode(final String node, final WIKI_NODES parent) {
			for(final WIKI_NODES nodeE : WIKI_NODES.values()) {
				if( ! parent.equals(nodeE.parent)) {
					continue;
				}
				if(node.equals(nodeE.sNode)) {
					return nodeE;
				}
			}
			return null;
		}
		/* (non-Javadoc)
		 * @see tools.imp.xml.XmlNodeINTF#GetNode()
		 */
		public String GetNode() {
			return sNode;
		}
		/* (non-Javadoc)
		 * @see tools.imp.xml.XmlNodeINTF#ReadElement()
		 */
		public boolean ReadElement() {
			return doRead;
		}
		/* (non-Javadoc)
		 * @see tools.imp.xml.XmlNodeINTF#IsNode(java.lang.String)
		 */
		public boolean IsNode(final String str) {
			return this.sNode.equalsIgnoreCase(str);
		}
		public WIKI_NODES Parent() {
			return parent;
		}
	};
	
	// ++++++++++++++
	
	private WIKI_NODES tip = WIKI_NODES.NONE;

	private final VData<ArrayIntObj> vData =
			new VData<ArrayIntObj>(nColRegex, colNames );
	private final WordDictTbl vWords = new WordDictTbl ();
	private final boolean doDict = true;
	private final String sLANG = "ron";

	// Init Fields
	private int iPMID = 0;
	private String sWord = "";
	private String sText = "";
	private String sLang = "";

	// ++++++++++++++++ MEMBER FUNCTIONS +++++++++++++++
	public VData<ArrayIntObj> ReadData() throws IOException {
		if(doDict) {
			System.out.println("Saving Dictionary");
			ExportToCSV exportToCSV = new ExportToCSV(vWords);
			exportToCSV.givenDataArray_whenConvertToCSV_thenOutputCreated();
			//new ExportCSV().Write(vWords);
		}
		return vData;
	}
	public void Init() {}
	
	// +++++++++++++++ PROCESS NODES ++++++++++++++++
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		final WIKI_NODES nodeE = WIKI_NODES.GetNode(qName, tip);
		if(nodeE != null) {
			tip = nodeE;
			
			// Init // TODO: Refactor => Factory
			if(tip == WIKI_NODES.ARTICLE) {
				iPMID = 0;
				sWord = "";
				sText = "";
				sLang = "";
			}
		}
	}
	
	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {

		if(tip.IsNode(qName)) {
			if(tip.equals(WIKI_NODES.ARTICLE)) {
				// SAVE the Record
				sText = sText.trim();
				matchLang.reset(sText);
				if(matchLang.find()) {
					if(matchLang.group(1) != null && ! matchLang.group(1).isEmpty()) {
						sLang = matchLang.group(1);
					} else {
						matchLang.group(2);
					}
				}
				final Vector<String> vSections = this.Section(sText, 0);
				if(vSections.size() > 0) {
					sLang += " -- " + vSections.toString();
					// add words to "Dictionary"
					if(doDict) this.Dictionary(vSections, sWord, iPMID, vWords);
				}
				final ArrayIntObj data = new ArrayIntObj(
						new int [] {iPMID},
						new String [] {sWord, sLang, sText}
						);
				vData.add(data);
				// clear previous values: is done at INIT of Article
			}
			
			tip = tip.Parent();
		}
	}
	public Vector<String> Section(final String str, final int nStart) {
		final Vector<String> vSections = new Vector<> ();
		for(int npos=nStart; npos < str.length(); npos++) {
			if(str.charAt(npos) == '{') {
				int nSubStart = npos + 1;
				int counter = 1;
				while(counter > 0) {
					npos ++;
					if(npos >= str.length()) break;
					if(str.charAt(npos) == '{') { counter ++; }
					if(str.charAt(npos) == '}') { counter --; }
				}
				// ERROR
				if(counter > 0) { vSections.add("!! ERR !!"); break; }
				if(str.charAt(nSubStart) == '{') {
					nSubStart ++;
				} else continue; // skip simple "{..}";
				final int nEnd;
				if(str.charAt(npos - 1) == '}') {
					nEnd = npos - 1;
				} else {
					nEnd = npos;
				}
				vSections.add(str.subSequence(nSubStart, nEnd).toString());
			}
		}
		return vSections;
	}
	public void Dictionary(final Vector<String> vSections, final String sBase, final int idBase,
			final Vector<WordDictObj> vResult) {
		// Error("Started: " + sBase);
		for(final String str : vSections) {
			final int nposStart;
			final WORD_TYPE type;
			if(str.startsWith("substantiv-")) {
				nposStart = 11;
				type = WORD_TYPE.SUBST;
			} else if(str.startsWith("verb-")) {
				nposStart = 5;
				type = WORD_TYPE.VERB;
			} else if(str.startsWith("adjectiv-")) {
				nposStart = 9;
				type = WORD_TYPE.ADJECTIV;
			} else if(str.startsWith("numeral-")) {
				nposStart = 5;
				type = WORD_TYPE.NUMERAL;
			} else {
				continue;
			}
			final int nposLang = str.indexOf("|", nposStart);
			if(nposLang < 0) { Error("No Language: " + str); continue; }
			// Language
			int npos = nposLang;
			final String sLang = str.substring(nposStart, npos).trim(); // may need trim()
			// skip other languages
			if(sLANG != null && ! sLANG.equals(sLang)) continue;
			npos ++;
			//
			String sOther = "";
			// Base Word
			final WordDictObj word0 = new WordDictObj();
			vResult.add(word0);
			word0.sWord = word0.sBaseWord = sBase;
			word0.idWord = word0.idBase = idBase;
			word0.sLang = sLang; word0.wType = type;
			while(npos < str.length()) {
				int nposNext = str.indexOf("|", npos);
				if(nposNext < 0) nposNext = str.length();
				final String sPart = str.substring(npos, nposNext).trim();
				final int nposEq = sPart.indexOf("=");
				if(nposEq > 0 && nposEq < sPart.length()) {
					// can contain multiple words;
					final String sWAll = sPart.substring(nposEq + 1).trim();
					final String sSubType = sPart.substring(0, nposEq);
					// TODO: find all modifiers;
					if(sSubType.equals("gen") || sSubType.equals("cj")) {
						sOther = sOther + (sOther.isEmpty() ? "" : ", ") + sWAll;
						npos = nposNext + 1;
						continue;
					}
					// gen={{m}}/{{f}}; vs {{inv}};
					if(sWAll.equals("-") || sWAll.isEmpty() || sWAll.startsWith("{{"))
						{ npos = nposNext + 1; continue; }
					for(final String sW : sWAll.split("[/,]")) {
						if(sW.isEmpty()) continue;
						final WordDictObj word = new WordDictObj();
						vResult.add(word);
						word.sWord = this.Trim(sW);
						word.sBaseWord = sBase; word.sLang = sLang;
						word.wType = type; word.sSubType = sSubType;
						word.sOther = sOther;
						word.idBase = idBase;
						word.idWord = vResult.size(); // TODO: unique IDs;
						//System.out.println(Arrays.toString(word.returnStringArray()));
						matchWParenth.reset(word.sWord);
						while(matchWParenth.find()) {
							word.sOther = (word.sOther.isEmpty() ? "" : word.sOther + " ")
									+ matchWParenth.group(1);
							if(matchWParenth.start() == 0) {
								word.sWord = word.sWord.substring(matchWParenth.end());
								matchWParenth.reset(word.sWord);
							} else {
								word.sWord = word.sWord.substring(0, matchWParenth.start()).trim();
							}
						}
					}
				}
				// Next
				npos = nposNext + 1;
			}
			word0.sOther = sOther;
		}
	}
	public String Trim(final String str) {
		// trim: "[[...]]";
		boolean hasChanged = false;
		int nposStart = 0;
		while(nposStart < str.length()) {
			if(str.charAt(nposStart) == '[') { hasChanged = true; nposStart++; continue; }
			break;
		}
		int nposEnd = str.length() - 1;
		while(nposEnd >= 0) {
			if(str.charAt(nposEnd) == ']') { hasChanged = true; nposEnd--; continue; }
			break;
		}
		if(hasChanged) {
			return str.substring(nposStart, nposEnd + 1);
		}
		return str;
	}
	public void Error(final String sErr) {
		System.out.println("Error: " + sErr);
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if(tip.ReadElement()) {
			final String sVal = new String(ch, start, length);
			// System.out.println(sVal);
			
			switch(tip) {
			case WORD : {
				sWord += sVal;
				break; }
			case ID : {
				iPMID = Integer.parseInt(sVal);
				break; }
			case TEXT : {
				sText += sVal;
				break; }
			}
		}
	}
}
