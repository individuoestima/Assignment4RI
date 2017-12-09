package com.kanto;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author kanto
 * * José Santos nº 89129 Higino Caires nº 89094
 */
class DF {
    private int check;
    private int df;

    public DF(int c) {
        this.check = c;
        this.df = 1;
    }

    public void incDf() {
        this.df += 1;
    }

    public int getCheck() {
        return check;
    }

    public void setCheck(int check) {
        this.check = check;
    }

    public int getDf() {
        return df;
    }

    public void setDf(int df) {
        this.df = df;
    }

}
