package com.company;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExportToCSV {

    private final Vector<WordDictObj> dataLines;

    public ExportToCSV(Vector<WordDictObj> dataLines) {
        this.dataLines = dataLines;
    }


    public String escapeSpecialCharacters(String data) {
        String escapedData = data;
        escapedData = escapedData.replaceAll("\\{", "");
        escapedData = escapedData.replaceAll("}", "");
//        escapedData = escapedData.replaceAll(",", "");
        return escapedData;
    }
    public String convertToCSV(WordDictObj data) {
        String[] strings = data.returnStringArray();
        return Stream.of(strings)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

    public void givenDataArray_whenConvertToCSV_thenOutputCreated() throws IOException {
        OutputStream csvOutputFile = new FileOutputStream("parseOutPut.csv");
        try (PrintWriter pw = (new PrintWriter(csvOutputFile, true, StandardCharsets.UTF_8))) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);

        }
    }

}
