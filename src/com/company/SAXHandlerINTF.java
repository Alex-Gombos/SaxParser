package com.company;

import org.xml.sax.ContentHandler;

import com.company.ArrayIntObj;
import com.company.VData;

public interface SAXHandlerINTF extends ContentHandler {
	public VData<ArrayIntObj> ReadData();
	public void Init();
}
