/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.HashMap;

/**
 * @author kanto
 * * José Santos nº 89129 Higino Caires nº 89094
 */
public class Data {

    private HashMap<Integer, Double> info;

    public Data() {
        this.info = new HashMap<>();
    }

    //Add info to the hashtable
    public void addInfo(int id) {
        if (info.containsKey(id)) {
            info.put(id, info.get(id) + 1);
        } else {
            info.put(id, 1.0);

        }
    }

    public HashMap<Integer, Double> getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "Data{" + "info=" + info + '}';
    }

}
