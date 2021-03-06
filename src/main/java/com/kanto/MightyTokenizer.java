package com.kanto;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.tartarus.snowball.ext.englishStemmer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author kanto
 * * José Santos nº 89129 Higino Caires nº 89094
 */
public class MightyTokenizer {

    //list used to store stopwords
    ArrayList<String> stopWords = new ArrayList<>();

    public MightyTokenizer(String pathstop) throws FileNotFoundException {

        //clean spaces on file with regexp on sublime \s+ with \n
        //read stopwords when incialized
        Scanner read = new Scanner(new File(pathstop));
        while (read.hasNextLine()) {
            stopWords.add(read.nextLine());
        }
    }

    public ArrayList<String> remove(String s) {

        //remove special stuff
        s = s.replace("\n", "").replace("\r", "").replaceAll("\\p{Punct}+", "");
        s = s.replaceAll("[ ]+", " ");

        //remove stopwords
        ArrayList<String> CleanText = new ArrayList<>();
        boolean check;
        String[] text = s.split(" ");
        for (String text1 : text) {
            if(stopWords.contains(text1) || text1.equals("")){
                continue;
            }
            else{
                englishStemmer stemmer = new englishStemmer();
                stemmer.setCurrent(text1);
                stemmer.stem();
                CleanText.add(stemmer.getCurrent());
            }
        }
        return CleanText;
    }
}
