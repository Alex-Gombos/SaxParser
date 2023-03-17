package com.company;

import org.xml.sax.ContentHandler;

import com.company.ArrayIntObj;
import com.company.VData;

import java.io.IOException;

public interface SAXHandlerINTF extends ContentHandler {
	public VData<ArrayIntObj> ReadData() throws IOException;
	public void Init();
}
