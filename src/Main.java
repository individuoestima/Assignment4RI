/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.util.*;

/*import org.deeplearning4j.models.word2vec.Word2Vec;
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator;
import org.deeplearning4j.text.sentenceiterator.SentenceIterator;
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor;
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory;
import org.deeplearning4j.text.tokenization.tokenizerfactory.TokenizerFactory;*/

/**
 * @author kanto
 * * José Santos nº 89129 Higino Caires nº 89094
 */
public class Main {

    public static void toFile(HashMap<Integer, RankedRetrieval> ranking, String file) {
        //write to file
        try {
            PrintWriter writer = new PrintWriter(file, "UTF-8");
            writer.println("query_id     doc_id     doc_score");
            for (HashMap.Entry<Integer, RankedRetrieval> entry : ranking.entrySet()) {
                //copy rankings to a list in order to sort them
                List<Map.Entry<Integer, Double>> entries = new ArrayList(entry.getValue().getScore().entrySet());
                entries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
                for (int i = 0; i < entries.size(); i++) {
                    if (entries.get(i).getValue() == 0.0) {
                        continue;
                    }
                    writer.println(entry.getKey() + "     " + entries.get(i).getKey() + "     " + entries.get(i).getValue());
                }
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void readFiles(File[] listOfFiles, HashMap<String, Data> map, HashMap<String, DF> df, MightyTokenizer mt) throws IOException {
        ArrayList<String> text = null;
        CorpusReader cr = new CorpusReader();
        Arrays.sort(listOfFiles);
        for (int i = 0; i < listOfFiles.length; i++) {
            text = mt.remove(cr.readFromFile(listOfFiles[i].toString()));
            //Insert into temp hashmap in order to get weight of term
            HashMap<String, Data> temp = new HashMap<>();
            //Indexing process
            for (int j = 0; j < text.size(); j++) {
                //get word
                String textaux = text.get(j);
                if (textaux.equals("")) {
                    continue;
                }
                //obtain df of each term
                if (!df.containsKey(textaux)) {
                    DF var = new DF(i);
                    df.put(textaux, var);
                } else {
                    if (i != df.get(textaux).getCheck()) {
                        df.get(textaux).setCheck(i);
                        df.get(textaux).incDf();
                    }
                }
                //insert into hashmap occurrences
                if (!temp.containsKey(textaux)) {
                    Data d = new Data();
                    //add info about this word and the doc id
                    d.addInfo(i + 1);
                    temp.put(textaux, d);
                } else {
                    temp.get(textaux).addInfo(i + 1);
                }
            }

            //calculate tf
            double sum = 0;
            for (HashMap.Entry<String, Data> entry : temp.entrySet()) {
                for (HashMap.Entry<Integer, Double> entries : entry.getValue().getInfo().entrySet()) {
                    entry.getValue().getInfo().put(entries.getKey(), (1 + Math.log10(entries.getValue())));
                    sum += entries.getValue() * entries.getValue();
                }
            }
            sum = Math.sqrt(sum);
            //Normalize and Insert into hashmap
            for (HashMap.Entry<String, Data> entry : temp.entrySet()) {
                for (HashMap.Entry<Integer, Double> entries : entry.getValue().getInfo().entrySet()) {
                    if (!map.containsKey(entry.getKey())) {
                        Data d = new Data();
                        d.getInfo().put(entries.getKey(), entries.getValue() / sum);
                        map.put(entry.getKey(), d);
                    } else {
                        map.get(entry.getKey()).getInfo().put(entries.getKey(), entries.getValue() / sum);
                    }
                }
            }
            temp.clear();
        }

        //write to index file
        try {
            PrintWriter writer = new PrintWriter("index", "UTF-8");
            for (HashMap.Entry<String, Data> entry : map.entrySet()) {
                writer.print(entry.getKey());
                HashMap<Integer, Double> x = entry.getValue().getInfo();
                for (HashMap.Entry<Integer, Double> entries : x.entrySet()) {
                    writer.print("," + entries.getKey() + ":" + entries.getValue());
                }
                writer.println();
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
        }

    }

    public static void readIndex(HashMap<String, Data> map, HashMap<String, DF> df) throws IOException {
        //Read index file
        FileReader fr = new FileReader("index");
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            String[] t = line.split(",");
            Data d = new Data();
            //get docid and weight for each term
            for (int i = 1; i < t.length; i++) {
                String[] values = t[i].split(":");
                d.getInfo().put(Integer.parseInt(values[0]), Double.parseDouble(values[1]));
            }
            DF var = new DF(0);
            var.setDf(t.length - 1);
            //insert into hashmap both the DF and weights of each term
            df.put(t[0], var);
            map.put(t[0], d);
        }
    }

    public static void readRelevance(HashMap<Integer, Data> relevanceScores, File f) throws IOException {
        //Read relevance file
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String line;
        while ((line = br.readLine()) != null) {
            String[] t = line.split(" ");
            Data d = new Data();
            for (int i = 1; i < t.length; i++) {
                if (relevanceScores.containsKey(Integer.parseInt(t[0]))) {
                    relevanceScores.get(Integer.parseInt(t[0])).getInfo().put(Integer.parseInt(t[1]), Double.parseDouble(t[2]));
                } else {
                    d.getInfo().put(Integer.parseInt(t[1]), Double.parseDouble(t[2]));
                    relevanceScores.put(Integer.parseInt(t[0]), d);
                }
            }

        }
    }

    public static void rankQ(File fQuery, MightyTokenizer mt, HashMap<String, Data> map, HashMap<String, DF> df, HashMap<Integer, RankedRetrieval> ranking, int total) throws IOException {
        //Rank queries
        FileReader fr = new FileReader(fQuery);
        BufferedReader br = new BufferedReader(fr);
        String line;
        int idQuery = 1;
        //read query
        while ((line = br.readLine()) != null) {
            ArrayList<String> text = mt.remove(line);
            RankedRetrieval r = new RankedRetrieval(total);
            //get ranking based on tf-idf ranked retrieval method
            r.ranking(map, text, df);
            //insert into hashmap
            ranking.put(idQuery, r);
            idQuery += 1;
        }
    }

    public static ArrayList<Integer> getFirst10(HashMap<Integer, Double> rank) {
        List<Map.Entry<Integer, Double>> entries = new ArrayList(rank.entrySet());
        entries.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        ArrayList<Integer> documents = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            documents.add(entries.get(i).getKey());
        }
        return documents;
    }

    public static void Rocchio(boolean flag, File fQuery, MightyTokenizer mt, HashMap<String, Data> map, HashMap<String, DF> df, HashMap<Integer, RankedRetrieval> rank, HashMap<Integer, Data> relevanceScores) throws IOException {

        //Rerank queries
        FileReader fr = new FileReader(fQuery);
        BufferedReader br = new BufferedReader(fr);
        String line;
        int idQuery = 1;
        //read query
        while ((line = br.readLine()) != null) {
            ArrayList<String> text = mt.remove(line);
            RankedRetrieval r = rank.get(idQuery);
            ArrayList<Integer> Relevant = new ArrayList<>();
            ArrayList<Integer> nonRelevant = new ArrayList<>();
            //get first 10 ranking documents
            ArrayList<Integer> docs = getFirst10(rank.get(idQuery).getScore());
            if (flag == true) {
                //explicit
                for (int i = 0; i < docs.size(); i++) {
                    if (relevanceScores.get(idQuery).getInfo().containsKey(docs.get(i))) {
                        Relevant.add(docs.get(i));
                    } else {
                        nonRelevant.add(docs.get(i));
                    }
                }
            } else {
                //implicit
                Relevant = docs;
                nonRelevant.clear();
            }
            //recalculate ranking with the feedback we have
            r.rocchioFeedback(map, text, df, Relevant, nonRelevant, flag, relevanceScores, idQuery);
            idQuery += 1;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 5) {
            System.out.println("Não incluiste todos os argumentos!");
            System.exit(0);
        }
        String pathfiles = args[0];
        String pathstop = args[1];
        File fQuery = new File(args[2]);
        if (!(new File(pathfiles).exists())) {
            System.out.println("O path para a pasta de ficheiros não existe!");
            System.exit(0);
        }
        if (!(new File(pathstop).exists())) {
            System.out.println("O path para o ficheiro das stopwords não existe!");
            System.exit(0);
        }
        if (!fQuery.exists()) {
            System.out.println("Ficheiro das queries não encontrado!");
            System.exit(0);
        }
        boolean flag = true;
        //explicit
        if (args[4].equals("-exp")) {
            flag = true;
        } else if (args[4].equals("-imp")) {
            //implicit
            flag = false;
        } else {
            System.out.println("Modo de feeback errado!");
            System.exit(0);
        }
        MightyTokenizer mt = new MightyTokenizer(pathstop);

        HashMap<String, Data> map = new HashMap<>();
        HashMap<String, DF> df = new HashMap<>();

        File folder = new File(pathfiles);
        File[] listOfFiles = folder.listFiles();

        if (!(new File("index").exists())) {
            System.out.println("Reading Files");
            readFiles(listOfFiles, map, df, mt);
            System.out.println("Done reading files!");
        } else {
            System.out.println("Reading Index File");
            readIndex(map, df);
            System.out.println("Done reading index!");
        }
        File fRelevance = new File("relevance.txt");
        if(!fRelevance.exists()){
            System.out.println("Ficheiro de relevância não encontrado");
            System.exit(0);
        }
        HashMap<Integer, Data> relevanceScores = new HashMap<>();
        readRelevance(relevanceScores, fRelevance);

        /*String file = "cranfield_sentences.txt";
        if(!(new File(file).exists())){
            System.out.println("Ficheiro cranfield_sentences.txt não encontrado!");
        }
        SentenceIterator iter = new BasicLineIterator(file);
        TokenizerFactory t = new DefaultTokenizerFactory();
        t.setTokenPreProcessor(new CommonPreprocessor());
        Word2Vec vec = new Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(1)
                .layerSize(100)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(t)
                .build();
        vec.fit();
        Collection<String> lst = vec.wordsNearest("light", 10);
        System.out.println(lst);*/

        //Rank documents for each query
        HashMap<Integer, RankedRetrieval> ranking = new HashMap<>();
        rankQ(fQuery, mt, map, df, ranking, listOfFiles.length);

        //get feedback and recalculate scores based on it
        Rocchio(flag, fQuery, mt, map, df, ranking, relevanceScores);

        toFile(ranking, args[3]);
    }
}
