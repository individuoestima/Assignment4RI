public class TopWeights {
    private String word;
    private Double weight;

    public TopWeights(String word, Double weight) {
        this.word = word;
        this.weight = weight;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return "TopWeights{" +
                "word='" + word + '\'' +
                ", weight=" + weight +
                '}';
    }
}
