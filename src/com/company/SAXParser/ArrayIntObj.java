package com.company.SAXParser;

public class ArrayIntObj implements ImportGetINTF {

    public int[] intVal;
    public String[] stringVal;

    public ArrayIntObj(int[] ints, String[] strings) {
        this.intVal = ints;
        this.stringVal = strings;
    }

    @Override
    public int[] GetInt() {
        return new int[0];
    }

    @Override
    public String[] GetString() {
        return new String[0];
    }
}
