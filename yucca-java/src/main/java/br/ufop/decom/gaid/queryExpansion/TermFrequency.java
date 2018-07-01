package br.ufop.decom.gaid.queryExpansion;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.apache.tika.parser.pdf.PDFParser;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.net.URLConnection;

public class TermFrequency extends QueryExpansion {

    private ContentExtractor extractor;

    private List<String> stockTerms;
    public int k;

    public TermFrequency (List<String> stockTerms, int k) {
        extractor = new ContentExtractor();
        this.stockTerms = stockTerms;
        this.k = k;
    }

    @Override
    public String[] getTerms(String[] urls) {
//        System.out.println("num urls: " + urls.length

        int N = 0; // total number of terms
        Map tfSingle = new HashMap<String, Float>();
        Map tfDouble = new HashMap<String, Float>();
        Map tfTriple = new HashMap<String, Float>();
        Map positions = new HashMap<String, Integer>();

        for (String url : urls) {
            try {
//                System.out.println(url);
//                System.in.read();
                String dom = readStringFromURL(url); // get page info
//                System.out.print(dom.length() + ": "); // page string length
//                System.out.println(dom);
                String[] words = cleanHtml(dom); // turns the page into an array of words
//                System.in.read();
                words = removeStopwords(words); // removes the stopwords from the list
//                System.out.println(String.join(", ", words));
//                System.out.println(url);
//                PrintWriter out = new PrintWriter("filename.txt");
//                out.println(dom);
                int n = words.length;
                N += n;
                for (int i = 0; i < n; i++) { // counting frequency of terms
                    // 1-word
                    AddTerm(words[i], tfSingle, positions, i, n);
                    // 2-words
                    if (i < n-1) {
                        String word = words[i] + " " + words[i+1];
                        AddTerm(word, tfDouble, positions, i, n);
                    }
                    // 3-words
                    if (i < n-2) {
                        String word = words[i] + " " + words[i+1] + " " + words[i+2];
                        AddTerm(word, tfTriple, positions, i, n);
                    }
                }
//                System.in.read();
            } catch (Throwable err) {
                System.out.println(err);
            }
        }

        Map frequency = new HashMap<String, Float>(); // frequency final values

        // collect all terms and it's frequency values
        for (String key : (String[])tfSingle.keySet().toArray(new String[0])) frequency.put(key, (float)tfSingle.get(key)/N);
        for (String key : (String[])tfDouble.keySet().toArray(new String[0])) frequency.put(key, ((float)tfDouble.get(key)*2)/N);
        for (String key : (String[])tfTriple.keySet().toArray(new String[0])) frequency.put(key, ((float)tfTriple.get(key)*3)/N);

        Object[] entries = frequency.entrySet().toArray(); // turns the hashmap into an key/value array
        sort(entries, 0, entries.length-1); // sort downward by value

        /*for (int i = 0; i < 20; i++) {
            Map.Entry<String, Float> entry = (Map.Entry<String, Float>)entries[i];
            System.out.println("key: " + entry.getKey() + ", value: " + entry.getValue());
        }*/

//        if (k > entries.length) k = entries.length;
//        String[] terms = new String[k];
        List<String> terms = new ArrayList<String>();

        // get the k-more-frequent keys
        for (int i = 0; i < entries.length && terms.size() < k; i++) {
            Map.Entry<String, Float> entry = (Map.Entry<String, Float>)entries[i];
//            System.out.println(String.format("%s: %f", entry.getKey(), entry.getValue()));
            if (!stockTerms.contains(entry.getKey())) // o termo não está entre os termos definidos pelo usuario
                terms.add(entry.getKey());
//            else
//                System.out.println("Term [" + entry.getKey() + "] already defined.");
//            terms[i] = entry.getKey();
        }
//        System.out.println(terms.size());
//        for (int i = 0; i < terms.toArray().length; i++) {
//            System.out.println(terms.toArray(new String[terms.size()])[i]);
//        }
        return terms.toArray(new String[terms.size()]);
    }

    private static void AddTerm(String word, Map frequencies, Map positions, int position, int total) {
        if (!positions.containsKey(word)) positions.put(word, position);
        float value = (float)(total - (int)positions.get(word))/total;
        if (frequencies.containsKey(word)) frequencies.put(word, (float)frequencies.get(word)+value);
        else frequencies.put(word, value);
    }

    private static String readStringFromURL(String requestURL) throws IOException {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
//            System.out.println(scanner.nextLine().trim());
            String first = scanner.nextLine().trim();
            if (first.contains("%PDF")) { // it is a pdf file
                System.out.println("pdf file");
                PDDocument doc = PDDocument.load(new URL(requestURL).openStream());
                String dom = new PDFTextStripper().getText(doc);
                doc.close();
                return dom;
            } else if (first.contains("<!DOCTYPE html")) {
                System.out.println("html file");
                Scanner scanner2 = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString());
                scanner2.useDelimiter("\\A");
                return scanner2.hasNext() ? scanner2.next() : "";
            } else {
                System.out.println("unknown file type");
                return "";
            }
        }
        /*try {
            URL url = new URL(requestURL);
            URLConnection connection = url.openConnection();
            InputStream input = connection.getInputStream();

            StringBuilder textBuilder = new StringBuilder();
            Reader reader = new BufferedReader(new InputStreamReader(input, Charset.forName(StandardCharsets.UTF_8.name())));
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
            return textBuilder.toString();
        } catch (Exception e) {
            return "";
        }*/
    }

    private String[] cleanHtml(String dom) throws StackOverflowError {
        try {
//            System.out.println("ORIGINAL: " + dom);
            // remove script/style/comment blocks and general tags
            // dom = new jregex.Pattern("<script(\n|.)*?>(\n|.)*?(</script>|$)|<style(\n|.)*?>(\n|.)*?(</style>|$)|<!--(\n|.)*?(-->|$)|<(.|\n)*?>|&.*?/g").replacer("").replace(dom);
            dom = extractor.extractFromDocument(dom);
//            System.out.println("EXTRACTED: " + dom);
            // remove non alphanumeric
            dom = new jregex.Pattern("[^A-zÀ-ú]|\\d+").replacer(" ").replace(dom);
            // converts groups of whitespaces/breaklines into a tfSingle whitespace
            dom = new jregex.Pattern("(\n|\\s)+").replacer(" ").replace(dom);
            return dom.trim().toLowerCase().split(" ");
        } catch (Exception e) {
//            System.out.println(e);
            return new String[0];
        }
    }

    private static String[] removeStopwords(String[] words) {
        String language = "pt-br"; // TODO get language automatically
        List<String> list = new ArrayList<String>();
        String[] stopwords = Stopwords.getStopwords(language);
        for (String w : words) {
            boolean found = false;
            for (String v : stopwords) {
                if (v.equals(w)) {
                    found = true;
                    break;
                }
            }
            if (found) continue;
            list.add(w);
        }
        return list.toArray(new String[0]);
    }

    // quicksort for map entries
    private static void sort(Object[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high); // pi is partitioning index, arr[pi] is now at right place
            // Recursively sort elements before partition and after partition
            sort(arr, low, pi-1);
            sort(arr, pi+1, high);
        }
    }

    private static int partition(Object[] arr, int low, int high) {
        Map.Entry<String, Float> pivot = (Map.Entry<String, Float>)arr[high];
        int i = (low-1); // index of smaller element
        for (int j=low; j<high; j++) {
            // If current element is smaller than or equal to pivot
            Map.Entry<String, Float> target = (Map.Entry<String, Float>)arr[j];
            if (target.getValue() >= pivot.getValue()) {
                i++;
                // swap arr[i] and arr[j]
                Object temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        // swap arr[i+1] and arr[high] (or pivot)
        Object temp = arr[i+1];
        arr[i+1] = arr[high];
        arr[high] = temp;
        return i+1;
    }
}
