package com.kanto;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author kanto
 * * José Santos nº 89129 Higino Caires nº 89094
 */
public class RankedRetrieval {

    private HashMap<Integer, Double> score;

    public RankedRetrieval() {
        this.score = new HashMap<>();
    }

    public HashMap<Integer, Double> getScore() {
        return score;
    }

    public void ranking(HashMap<String, Data> map, ArrayList<String> text, HashMap<String, Double> df) {
        //get ranking of each document

        //process query, get TF
        HashMap<String, Double> temp = new HashMap<>();
        for (String text1 : text) {
            if (!temp.containsKey(text1)) {
                //add info about this word and the reps
                temp.put(text1, 1.0);
            } else {
                temp.put(text1, temp.get(text1) + 1);
            }
        }
        //Calculate tf-idf for each term of the query
        double sum = 0;
        for (HashMap.Entry<String, Double> entry : temp.entrySet()) {
            if (!df.containsKey(entry.getKey())) {
                entry.setValue(0.0);
            } else {
                entry.setValue((1 + Math.log10(entry.getValue())) * df.get(entry.getKey()));//Math.log10(this.total / df.get(entry.getKey()).getDf()));
                sum += entry.getValue() * entry.getValue();
            }
        }
        sum = Math.sqrt(sum);
        //Normalize - query vector
        for (HashMap.Entry<String, Double> entry : temp.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }

        //get score
        for (String text1 : text) {
            //words that exist in query but don't exist in documents
            if (!map.containsKey(text1)) {
                continue;
            }
            for (HashMap.Entry<Integer, Double> entry : map.get(text1).getInfo().entrySet()) {
                if (score.containsKey(entry.getKey())) {
                    score.put(entry.getKey(), score.get(entry.getKey()) + (entry.getValue() * temp.get(text1)));
                } else {
                    score.put(entry.getKey(), entry.getValue() * temp.get(text1));
                }
            }
        }
    }


    public void rocchioFeedback(HashMap<String, Data> map, ArrayList<String> text, HashMap<String, Double> df, ArrayList<Integer> relevant, ArrayList<Integer> nonRelevant, boolean flag, HashMap<Integer, Data> relevanceScores, int idQuery) {
        //get ranking of each document with roochio feedback
        score.clear();
        //process query, get TF
        HashMap<String, Double> temp = new HashMap<>();
        for (String text1 : text) {
            if (!temp.containsKey(text1)) {
                //add info about this word and the reps
                temp.put(text1, 1.0);
            } else {
                temp.put(text1, temp.get(text1) + 1);
            }
        }
        //Calculate tf-idf for each term of the query
        double sum = 0;
        for (HashMap.Entry<String, Double> entry : temp.entrySet()) {
            if (!df.containsKey(entry.getKey())) {
                entry.setValue(0.0);
            } else {
                entry.setValue((1 + Math.log10(entry.getValue())) * df.get(entry.getKey()));
                sum += entry.getValue() * entry.getValue();
            }
        }
        sum = Math.sqrt(sum);
        //Normalize - query vector
        for (HashMap.Entry<String, Double> entry : temp.entrySet()) {
            entry.setValue(entry.getValue() / sum);
        }

        //ROCCHIO FEEDBACK

        //In class ,alpha = 1, so we did not added it here.

        HashMap<String, Double> TermsForRocchioPositives = new HashMap<>();
        HashMap<String, Double> TermsForRocchioNegatives = new HashMap<>();

        //get all terms in the relevant documents and calculate final value
        for(int i = 0 ; i<relevant.size();i++){
            for (HashMap.Entry<String, Data> entry : map.entrySet()) {
                if(!TermsForRocchioPositives.containsKey(entry.getKey())){
                    if(entry.getValue().getInfo().containsKey(relevant.get(i))){
                        TermsForRocchioPositives.put(entry.getKey(),entry.getValue().getInfo().get(relevant.get(i)));
                    }
                }
                else{
                    if(entry.getValue().getInfo().containsKey(relevant.get(i))) {
                        TermsForRocchioPositives.put(entry.getKey(), TermsForRocchioPositives.get(entry.getKey()) + entry.getValue().getInfo().get(relevant.get(i)));
                    }
                }
            }
        }

        //get all terms in the non relevant documents and calculate final value
        for(int i = 0 ; i<nonRelevant.size();i++){
            for (HashMap.Entry<String, Data> entry : map.entrySet()) {
                if(!TermsForRocchioNegatives.containsKey(entry.getKey())){
                    if(entry.getValue().getInfo().containsKey(nonRelevant.get(i))){
                        TermsForRocchioNegatives.put(entry.getKey(),entry.getValue().getInfo().get(nonRelevant.get(i)));
                    }
                }
                else{
                    if(entry.getValue().getInfo().containsKey(nonRelevant.get(i))) {
                        TermsForRocchioNegatives.put(entry.getKey(), TermsForRocchioNegatives.get(entry.getKey()) +entry.getValue().getInfo().get(nonRelevant.get(i)));
                    }
                }
            }
        }


        //Sort by heavier weight
        List<Map.Entry<String, Double>> positiveTerms = new ArrayList(TermsForRocchioPositives.entrySet());
        positiveTerms.sort((o1,o2)->o2.getValue().compareTo(o1.getValue()));

        List<Map.Entry<String, Double>> negativeTerms = new ArrayList(TermsForRocchioNegatives.entrySet());
        negativeTerms.sort((o1,o2)->o2.getValue().compareTo(o1.getValue()));
        double beta=0.75/relevant.size();
        double sigma = 0.25/nonRelevant.size();
        //Add relevant to query, heaviest 5 terms, previously calculated we just need to add it

        for(int i = 0 ;i<positiveTerms.size();i++){
            if(!temp.containsKey(positiveTerms.get(i).getKey())){
                temp.put(positiveTerms.get(i).getKey(),positiveTerms.get(i).getValue() * beta);

            }
            else{
                temp.put(positiveTerms.get(i).getKey(),temp.get(positiveTerms.get(i).getKey()) + (positiveTerms.get(i).getValue() * beta));

            }
            if(i == 4){
                break;
            }
        }

        //Add non relevant to query, heaviest 5 terms previously calculated we just need to add it
        for(int i = 0 ;i<negativeTerms.size();i++){
            if(!temp.containsKey(negativeTerms.get(i).getKey())){
                temp.put(negativeTerms.get(i).getKey(),-negativeTerms.get(i).getValue()*sigma);

            }
            else{
                temp.put(negativeTerms.get(i).getKey(),temp.get(negativeTerms.get(i).getKey()) -(negativeTerms.get(i).getValue()*sigma));

            }
            if(i == 4){
                break;
            }
        }


        for(HashMap.Entry<String, Double> term : temp.entrySet()){
            //words that exist in query but don't exist in documents
            if (!map.containsKey(term.getKey())) {
                continue;
            }
            //remove negatives
            if(term.getValue() <= 0.0){
                continue;
            }
            for (HashMap.Entry<Integer, Double> entry : map.get(term.getKey()).getInfo().entrySet()) {
                if (score.containsKey(entry.getKey())) {
                    score.put(entry.getKey(), score.get(entry.getKey()) + (entry.getValue() * temp.get(term.getKey())));
                } else {
                    score.put(entry.getKey(), entry.getValue() * temp.get(term.getKey()));
                }
            }
        }
    }

    @Override
    public String toString() {
        return "com.kanto.RankedRetrieval{" + "score=" + score + '}';
    }

}
