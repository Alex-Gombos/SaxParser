package com.company.SAXParser;

import org.xml.sax.ContentHandler;

import java.io.IOException;

public interface SAXHandlerINTF extends ContentHandler {
	public VData<ArrayIntObj> ReadData() throws IOException;
	public void Init();
}
