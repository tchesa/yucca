package br.ufop.decom.gaid.queryExpansion;

import java.util.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TermFrequency extends QueryExpansion {

    public TermFrequency () {

    }

    @Override
    public String[] getTerms(String[] urls) {
        /*String[] urls = new String[]{
            "https://docs.python.org/3/tutorial/datastructures.html",
            "http://thomas-cokelaer.info/tutorials/python/data_structures.html",
            "https://www.datacamp.com/community/tutorials/data-structures-python",
            "http://interactivepython.org/runestone/static/pythonds/index.html",
            "https://pt.coursera.org/learn/python-data"
        };*/

        int N = 0; // total number of terms
        Map tfSingle = new HashMap<String, Float>();
        Map tfDouble = new HashMap<String, Float>();
        Map tfTriple = new HashMap<String, Float>();
        Map positions = new HashMap<String, Integer>();

        for (String url : urls) {
            try {
                String dom = readStringFromURL(url); // get page info
                System.out.print(dom.length() + ": "); // page string length
                String[] words = cleanHtml(dom); // turns the page into an array of words
                words = removeStopwords(words); // removes the stopwords from the list
                System.out.println(String.join(", ", words));
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

        int k = 10;
        if (k > entries.length) k = entries.length;
        String[] terms = new String[k];
        // gets the k-more-frequent keys

        for (int i = 0; i < k; i++) {
            Map.Entry<String, Float> entry = (Map.Entry<String, Float>)entries[i];
            System.out.println(String.format("%s: %f", entry.getKey(), entry.getValue()));
            terms[i] = entry.getKey();
        }
        return terms;
    }

    private static void AddTerm(String word, Map frequencies, Map positions, int position, int total) {
        if (!positions.containsKey(word)) positions.put(word, position);
        float value = (float)(total - (int)positions.get(word))/total;
        if (frequencies.containsKey(word)) frequencies.put(word, (float)frequencies.get(word)+value);
        else frequencies.put(word, value);
    }

    private static String readStringFromURL(String requestURL) throws IOException {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(), StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    private static String[] cleanHtml(String dom) throws StackOverflowError {
        try {
            // remove script/style/comment blocks and general tags
            dom = new jregex.Pattern("<script.*?>(\n|.)*?</script>|<style.*?>(\n|.)*?</style>|<!--(\n|.)*?-->|<(.|\n)*?>|&.*?;").replacer("").replace(dom);
            // remove non alphanumeric
            dom = new jregex.Pattern("\\W|\\d+").replacer(" ").replace(dom);
            // converts groups of whitespaces/breaklines into a tfSingle whitespace
            dom = new jregex.Pattern("(\n|\\s)+").replacer(" ").replace(dom);
            return dom.trim().toLowerCase().split(" ");
        } catch (Exception e) {
            System.out.println(e);
            return new String[0];
        }
    }

    private static String[] removeStopwords(String[] words) {
        String language = "en"; // TODO get language automaticaly
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
