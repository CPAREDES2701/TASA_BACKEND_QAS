package com.incloud.hcp.jco.controlLogistico.dto;

import java.util.ArrayList;
import java.util.HashMap;

public class RepModifDatCombusRegImports {
    private HashMap<String, String> titulosField;
    private ArrayList<HashMap<String, Object>> data;
    private int porcIndMod;

    public HashMap<String, String> getTitulosField() {
        return titulosField;
    }

    public void setTitulosField(HashMap<String, String> titulosField) {
        this.titulosField = titulosField;
    }

    public ArrayList<HashMap<String, Object>> getData() {
        return data;
    }

    public void setData(ArrayList<HashMap<String, Object>> data) {
        this.data = data;
    }

    public int getPorcIndMod() {
        return porcIndMod;
    }

    public void setPorcIndMod(int porcIndMod) {
        this.porcIndMod = porcIndMod;
    }
}
