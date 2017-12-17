package com.kanto;/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.ArrayList;
import java.util.HashMap;
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
        double beta;
        double sigma = 0.1;

        //formula for relevant documents
        for (int i = 0; i < relevant.size(); i++) {
            for (HashMap.Entry<String, Double> entry : temp.entrySet()) {
                if (map.containsKey(entry.getKey())) {
                    if (map.get(entry.getKey()).getInfo().containsKey(relevant.get(i))) {
                        //for each score we have diferent beta's if using explicit relevance feedback
                        if (flag == true) {
                            beta = 1 / relevanceScores.get(idQuery).getInfo().get(relevant.get(i));
                        } else {
                            beta = 0.75;
                        }
                        entry.setValue(entry.getValue() + (beta * map.get(entry.getKey()).getInfo().get(relevant.get(i))) / (double) relevant.size());
                    }
                }
            }
        }


        //formula for non relevant documents
        for (int i = 0; i < nonRelevant.size(); i++) {
            for (HashMap.Entry<String, Double> entry : temp.entrySet()) {
                if (map.containsKey(entry.getKey())) {
                    if (map.get(entry.getKey()).getInfo().containsKey(nonRelevant.get(i))) {
                        entry.setValue(entry.getValue() - (sigma * map.get(entry.getKey()).getInfo().get(nonRelevant.get(i))) / (double) nonRelevant.size());
                    }
                }
            }
        }

        //get top terms in relevant documents
        ArrayList<TopWeights> top = new ArrayList<>();
        for (int i = 0; i < relevant.size(); i++) {
            for (Map.Entry<String, Data> x : map.entrySet()) {
                if (x.getValue().getInfo().containsKey(relevant.get(i))) {
                    top.add(new TopWeights(x.getKey(), x.getValue().getInfo().get(relevant.get(i))));
                }
            }
        }
        //sort heavier
        top.sort((o1, o2) -> o2.getWeight().compareTo(o1.getWeight()));

        //Add heavier terms to query if term already in there just increase the weight
        for (int i = 0; i < top.size(); i++) {
            if (!temp.containsKey(top.get(i).getWord())) {
                temp.put((top.get(i).getWord()), top.get(i).getWeight());
                text.add(top.get(i).getWord());
            }
            else{
                temp.put((top.get(i).getWord()), temp.get(top.get(i).getWord()) + top.get(i).getWeight());
            }
            if(i == 3)
                break;
        }

        //get score
        for (String text1 : text) {
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

    @Override
    public String toString() {
        return "com.kanto.RankedRetrieval{" + "score=" + score + '}';
    }

}
