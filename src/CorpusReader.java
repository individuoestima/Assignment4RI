/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author kanto
 * * José Santos nº 89129 Higino Caires nº 89094
 */
public class CorpusReader {

    public CorpusReader() {
    }

    public String readFromFile(String f) throws IOException {
        FileReader fr = new FileReader(new File(f));
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        String line;
        boolean insideText = false; //used to check if the info we want is inside the proper tags
        while ((line = br.readLine()) != null) {
            if (line.equals("<TITLE>") || line.equals("<TEXT>")) {
                insideText = true;
                continue;
            }
            if (line.equals("</TITLE>") || line.equals("</TEXT>")) {
                insideText = false;
                continue;
            }
            if (insideText) {
                //separate words on line break
                sb.append(line).append(" ");
            }
        }
        sb.append("\n");
        return sb.toString().toLowerCase();
    }

    /*public static void main(String[] args) throws IOException {
        File folder = new File("cranfield");
        File f = new File("cranfield_sentences.txt");
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles);
        String s = "";
        for(int i = 0 ; i<listOfFiles.length;i++){
             s += readFromFile(listOfFiles[i].toString());

        }
        Files.write(f.toPath(),s.getBytes());
    }*/
}
