package com.babyfs.tk.galaxy.demo.model;



public class ExcelModel {


    private  String en;

    @Override
    public String toString() {
        return "ExcelModel{" +
                "en='" + en + '\'' +
                ", ita='" + ita + '\'' +
                ", fra='" + fra + '\'' +
                ", ch='" + ch + '\'' +
                '}';
    }

    public String getEn() {
        return en;
    }

    public void setEn(String en) {
        this.en = en;
    }

    public String getIta() {
        return ita;
    }

    public void setIta(String ita) {
        this.ita = ita;
    }

    public String getFra() {
        return fra;
    }

    public void setFra(String fra) {
        this.fra = fra;
    }

    public String getCh() {
        return ch;
    }

    public void setCh(String ch) {
        this.ch = ch;
    }

    private  String  ita;

    private  String  fra;
    private String ch;

}
