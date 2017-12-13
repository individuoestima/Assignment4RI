package com.kanto;

import java.util.*;

public class evaluation {

    private double dcg;
    private double perfect;

    public evaluation(){

    }

    public double getNdcg() {
        return dcg;
    }

    public double log2(double num) {
        return (Math.log10(num/Math.log(2)));
    }

    //convert scores
    public double realRel(double num){
        if(num == 4.0)
            return 1.0;
        if(num == 3.0)
            return 2.0;
        if(num == 2.0)
            return 3.0;
        return 4.0;
    }

    public void calculateNDCG(HashMap<Integer,RankedRetrieval> ranking,HashMap<Integer, Data> relevanceScores){
        for(HashMap.Entry<Integer,RankedRetrieval> entry: ranking.entrySet()){
            List<Map.Entry<Integer, Double>> entries = new ArrayList(entry.getValue().getScore().entrySet());
            entries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
            if(!relevanceScores.get(entry.getKey()).getInfo().containsKey(entries.get(0).getKey())){
                dcg = dcg + 0;
            }
            else{
                dcg = dcg + realRel(relevanceScores.get(entry.getKey()).getInfo().get(entries.get(0).getKey()));
            }
            for (int i = 1;i<10;i++){
                if(!relevanceScores.get(entry.getKey()).getInfo().containsKey(entries.get(i).getKey())){
                    dcg+=0;
                }
                else{
                    double relevance = relevanceScores.get(entry.getKey()).getInfo().get(entries.get(i).getKey());
                    //log da posição
                    dcg = dcg + (realRel(relevance) / log2(i+1));
                }
            }
        }
        for(HashMap.Entry<Integer,Data> entry: relevanceScores.entrySet()) {
            List<Map.Entry<Integer, Double>> entries = new ArrayList(entry.getValue().getInfo().entrySet());
            entries.sort(Comparator.comparing(Map.Entry::getValue));
            perfect = perfect + realRel(entries.get(0).getValue());
            for (int i = 1;i<10;i++){
                //if GS doesn't have enough entries.
                if(i == entries.size()){
                    break;
                }
                perfect = perfect + (realRel(entries.get(i).getValue())/log2(i+1));
            }
        }
        System.out.println(dcg);
        System.out.println(perfect);
        dcg = dcg/225;
        perfect = perfect/225;
        System.out.println(dcg/perfect);
    }
}
