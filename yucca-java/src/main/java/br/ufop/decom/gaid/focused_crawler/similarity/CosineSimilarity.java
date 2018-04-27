package br.ufop.decom.gaid.focused_crawler.similarity;

import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CosineSimilarity implements SimilarityMetric {

    Logger logger = LoggerFactory.getLogger(CosineSimilarity.class);

    private List<String> genreTerms, contentTerms, urlTerms;

    /**
     * Genre and content weights sum must be equal to 1. Benchmark values -
     * genre: 0.3, content:0.7.
     */
    private double genreWeight, contentWeight;

    /**
     * A weight for genre and content weighted mean result.
     */
    private double genreAndContentWeight;

    /**
     * URL similarity weight = (Genre and content combination weight) - 1.
     */
    private double urlWeight;
    private double threshold;

    public CosineSimilarity() {
        this.genreTerms = new ArrayList<>();
        this.contentTerms = new ArrayList<>();
        this.urlTerms = new ArrayList<>();

        try {
            InputStream inputStream = new FileInputStream("pesos.collect");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String linhaDoArquivo = bufferedReader.readLine();
            this.genreWeight = Double.parseDouble(linhaDoArquivo);
            System.out.print("Peso de gênero: ");
            System.out.println(this.genreWeight);

            linhaDoArquivo = bufferedReader.readLine();
            this.contentWeight = Double.parseDouble(linhaDoArquivo);
            System.out.print("Peso de conteúdo: ");
            System.out.println(this.contentWeight);

            linhaDoArquivo = bufferedReader.readLine();
            this.genreAndContentWeight = Double.parseDouble(linhaDoArquivo);
            System.out.print("Peso de gênero e conteúdo: ");
            System.out.println(this.genreAndContentWeight);

            linhaDoArquivo = bufferedReader.readLine();
            this.urlWeight = Double.parseDouble(linhaDoArquivo);
            System.out.print("Peso da URL: ");
            System.out.println(this.urlWeight);

            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.threshold = 0.0;

    }

    public CosineSimilarity(List<String> genreTerms, List<String> contentTerms, double genreWeight, double contentWeight) {
        this.genreTerms = genreTerms;
        this.contentTerms = contentTerms;

        if (genreWeight + contentWeight > 1) {
            this.genreWeight = genreWeight / (genreWeight + contentWeight);
            this.contentWeight = contentWeight / (genreWeight + contentWeight);
        } else {
            this.genreWeight = genreWeight;
            this.contentWeight = contentWeight;
        }
    }

    public CosineSimilarity(List<String> genreTerms, List<String> contentTerms, List<String> urlTerms,
                            double genreWeight, double contentWeight, double genreAndContentWeight, double urlWeight,
                            double threshold) {
        this.genreTerms = genreTerms;
        this.contentTerms = contentTerms;
        this.urlTerms = urlTerms;

        if (genreWeight + contentWeight > 1) {
            this.genreWeight = genreWeight / (genreWeight + contentWeight);
            this.contentWeight = contentWeight / (genreWeight + contentWeight);
        } else {
            this.genreWeight = genreWeight;
            this.contentWeight = contentWeight;
        }

        if (genreAndContentWeight + urlWeight > 1) {
            this.genreAndContentWeight = genreAndContentWeight / (genreAndContentWeight + urlWeight);
            this.urlWeight = urlWeight / (genreAndContentWeight + urlWeight);
        } else {
            this.genreAndContentWeight = genreAndContentWeight;
            this.urlWeight = urlWeight;
        }

        this.threshold = threshold;
    }

    @Override
    public double similarity(String text) throws IOException {
        double finalSimilarity = 0.0;

        double genreSimilarity = sim(augmentedTermFrequency(this.genreTerms, text));
        double contentSimilarity = sim(augmentedTermFrequency(this.contentTerms, text));

        finalSimilarity = this.genreWeight * genreSimilarity + this.contentWeight * contentSimilarity;

        return finalSimilarity;
    }

    @Override
    public double similarity(String pageText, String urlText) throws IOException {
        double finalSimilarity = 0.0;

        double genreSimilarity = sim(augmentedTermFrequency(this.genreTerms, pageText));
        double contentSimilarity = sim(augmentedTermFrequency(this.contentTerms, pageText));
        double urlSimilarity = sim(augmentedTermFrequency(this.urlTerms, urlText));

        double genreAndContentCombination = this.genreWeight * genreSimilarity + this.contentWeight * contentSimilarity;

        finalSimilarity = this.genreAndContentWeight * genreAndContentCombination + this.urlWeight * urlSimilarity;

        return finalSimilarity;
    }

    /*Metodo criado para teste*/
    public double similarity(String pageText, String urlText, int docid) throws IOException {
        double finalSimilarity = 0.0;

        double genreSimilarity = sim(augmentedTermFrequency(this.genreTerms, pageText, docid));
        double contentSimilarity = sim(augmentedTermFrequency(this.contentTerms, pageText, docid));
        double urlSimilarity = sim(augmentedTermFrequency(this.urlTerms, urlText, docid));

        double genreAndContentCombination = this.genreWeight * genreSimilarity + this.contentWeight * contentSimilarity;

        finalSimilarity = this.genreAndContentWeight * genreAndContentCombination + this.urlWeight * urlSimilarity;

        /*Salvando info em arquivos*/
        FileWriter file = new FileWriter("Texto/" + docid + ".fcrawler", true);
        PrintWriter printFile = new PrintWriter(file);
        printFile.print("\n\nGenreWeight = " + this.genreWeight + "\tGenreSimilarity = " + genreSimilarity + "\nContentWeight = " + this.contentWeight + "\tContentSimilarity = " + contentSimilarity);
        printFile.print("\nGenreAndContentWeight = " + this.genreAndContentWeight + "\tGenreAndContentCombination = " + genreAndContentCombination + "\nUrlWeight = " + this.urlWeight + "\tUrlSimilarity = " + urlSimilarity);
        printFile.printf("\nurlText = %s", urlText);
        printFile.printf("%s", pageText);
        printFile.close();
        file.close();

        return finalSimilarity;
    }

    public double similarity2(String pageText, String urlText, int docid) throws IOException {
        double finalSimilarity = 0.0;

        double genreSimilarity = sim(weightTermQuery(this.genreTerms, docid), augmentedTermFrequency(this.genreTerms, pageText, docid));
        double contentSimilarity = sim(weightTermQuery(this.contentTerms, docid), augmentedTermFrequency(this.contentTerms, pageText, docid));
        double urlSimilarity = sim(weightTermQuery(this.urlTerms, docid), augmentedTermFrequency(this.urlTerms, urlText, docid));

        double genreAndContentCombination = this.genreWeight * genreSimilarity + this.contentWeight * contentSimilarity;

        finalSimilarity = this.genreAndContentWeight * genreAndContentCombination + this.urlWeight * urlSimilarity;

        /*Salvando info em arquivos*/
        FileWriter file = new FileWriter("Texto/" + docid + ".fcrawler", true);
        PrintWriter printFile = new PrintWriter(file);
        printFile.print("\n\nGenreWeight = " + this.genreWeight + "\tGenreSimilarity = " + genreSimilarity + "\nContentWeight = " + this.contentWeight + "\tContentSimilarity = " + contentSimilarity);
        printFile.print("\nGenreAndContentWeight = " + this.genreAndContentWeight + "\tGenreAndContentCombination = " + genreAndContentCombination + "\nUrlWeight = " + this.urlWeight + "\tUrlSimilarity = " + urlSimilarity);
        printFile.printf("\nurlText = %s", urlText);
        printFile.printf("%s", pageText);
        printFile.close();
        file.close();

        return finalSimilarity;
    }

    @Override
    public boolean isSimilar(double similarityValue) {
        return similarityValue >= this.threshold;
    }

    /*Teste*/
    public List<Double> augmentedTermFrequency(List<String> terms, String text, int docid) throws IOException {
        Map<String, Integer> index = new HashMap<>();
        List<Double> tf = new ArrayList<>();
        FileWriter file = new FileWriter("Texto/" + docid + ".fcrawler", true);
        PrintWriter printFile = new PrintWriter(file);
        int maxShingle = 2;

        for (String term : terms) {
            index.put(StringUtils.lowerCase(StringUtils.stripAccents(term)), 0); //Adiciona os termos com frequencia 0
            int shingleSize = term.split(" ").length;
            if (shingleSize > maxShingle) {
                maxShingle = shingleSize;
            }
        }
        printFile.printf("\nNumero de termos: %d", index.size());

        text = StringUtils.lowerCase(StringUtils.stripAccents(text));

        Analyzer analyzer = new StandardAnalyzer(new StringReader(""));
        ShingleFilter filter = new ShingleFilter(analyzer.tokenStream(null, text));
        filter.setMaxShingleSize(maxShingle);
        filter.reset();


        printFile.printf("\nTermos:\n");

        /*Contabiliza a freq dos termos*/
        while (filter.incrementToken()) {
            String token = filter.getAttribute(CharTermAttribute.class).toString();
            if (index.containsKey(token)) {
                index.replace(token, index.get(token) + 1);
                printFile.printf("\t%s Freq: %d\n", token, index.get(token));
            }
        }

        /*for (String str: index.keySet()){
            printFile.printf("\nTermo: %s \tfreq: %d",str,index.get(str));
        }*/
        printFile.close();
        file.close();

        filter.end();
        filter.close();
        analyzer.close();

        for (String key : index.keySet()) {
            tf.add((double) index.get(key)); //adiciona a freq dos termos no texto
        }

        double maxFreq = Collections.max(tf);

        for (int i = 0; i < tf.size(); i++) {
            tf.set(i, tf.get(i) / maxFreq);
            //tf.set(i, 0.5 + (0.5 * tf.get(i) / maxFreq)); //Altera o valor da posição i
        }

        return tf;
    }

    public List<Double> augmentedTermFrequency(List<String> terms, String text) throws IOException {
        Map<String, Integer> index = new HashMap<>();
        List<Double> tf = new ArrayList<>();

        int maxShingle = 2;

        for (String term : terms) {
            index.put(StringUtils.lowerCase(StringUtils.stripAccents(term)), 0);
            int shingleSize = term.split(" ").length;
            if (shingleSize > maxShingle) {
                maxShingle = shingleSize;
            }
        }

        text = StringUtils.lowerCase(StringUtils.stripAccents(text));

        Analyzer analyzer = new StandardAnalyzer(new StringReader(""));
        ShingleFilter filter = new ShingleFilter(analyzer.tokenStream(null, text));
        filter.setMaxShingleSize(maxShingle);
        filter.reset();

        while (filter.incrementToken()) {
            String token = filter.getAttribute(CharTermAttribute.class).toString();
            if (index.containsKey(token)) {
                index.replace(token, index.get(token) + 1);
            }
        }

        filter.end();
        filter.close();
        analyzer.close();

        for (String key : index.keySet()) {
            tf.add((double) index.get(key));
        }

        double maxFreq = Collections.max(tf);

        for (int i = 0; i < tf.size(); i++) {
            tf.set(i, (tf.get(i) / maxFreq));
        }

        return tf;
    }

    public double sim(List<Double> normalizedTF) {
        double similarity = 0.0;
        double dividend = 0.0;
        double divisor = 0.0;

        for (Double freq : normalizedTF) {
            dividend += freq;
            divisor += freq * freq;
        }

        divisor = Math.sqrt(divisor);
        divisor *= Math.sqrt(normalizedTF.size());

        if (divisor > 0) {
            similarity = dividend / divisor;
        }

        return similarity;
    }

    private List<Double> weightTermQuery(List<String> terms, int docid) {
        Map<String, Integer> index = new HashMap<>();
        List<Double> wis = new ArrayList<>();

        for (String term : terms) {
            String aux = StringUtils.lowerCase(StringUtils.stripAccents(term));
            if (index.get(aux) == null)
                index.put(aux, 1);
            else
                index.replace(aux, index.get(aux) + 1);
        }

        for (String key : index.keySet()) {
            wis.add((double) index.get(key));
        }

        double maxFreq = Collections.max(wis);

        for (int i = 0; i < wis.size(); i++) {
            wis.set(i, (0.5 + (0.5 * wis.get(i)) / maxFreq));
        }
        FileWriter file = null;
        try {
            file = new FileWriter("Texto/" + docid + ".fcrawler", true);
            PrintWriter printFile = new PrintWriter(file);

            printFile.printf("Termos e seus Wis\n");
            Object[] aux = index.keySet().toArray();
            for (int i = 0; i < wis.size(); i++) {
                printFile.printf(String.format("Termo: %s \tPeso: %f\n", aux[i], wis.get(i)));
            }

            printFile.close();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wis;
    }

    public double sim(List<Double> wis, List<Double> wif) {
        double similarity = 0.0;
        double dividend = 0.0;
        double divisorWij = 0.0;
        double divisorWis = 0.0;
        double divisor;

        for (int i = 0; i < wis.size(); i++) {
            dividend += wif.get(i) * wis.get(i);
            divisorWij += wif.get(i) * wif.get(i);
            divisorWis += wis.get(i) * wis.get(i);
        }

        divisor = Math.sqrt(divisorWij);
        divisor *= Math.sqrt(divisorWis);

        if (divisor > 0) {
            similarity = dividend / divisor;
        }

        return similarity;
    }

}
