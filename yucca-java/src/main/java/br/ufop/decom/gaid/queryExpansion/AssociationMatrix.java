package br.ufop.decom.gaid.queryExpansion;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.helper.StringUtil;

import java.io.*;
import java.util.*;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.helper.StringUtil;

public class AssociationMatrix extends QueryExpansion {

    private String indexDir;
    private String dataDir;

    public AssociationMatrix (String indexDir, String dataDir, int numPages) {
        setIndexDir(indexDir);
        setDataDir(dataDir);
        try {
            createIndex(numPages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String[] getTerms(String[] urls) {
        return new String[0];
    }

    public void createIndex(int numFiles) throws IOException{
        Indexer indexer = new Indexer(indexDir);

        int numIndexed;

        long starTime = System.currentTimeMillis();
        System.out.println("Indexing files...");
        numIndexed = indexer.createIndex(dataDir, new TextFileFilter(), numFiles);
        long endTime = System.currentTimeMillis();

        System.out.println(numIndexed + " Files indexed, time taken: " + (endTime-starTime) + "ms");

        indexer.close();
    }

    public List<String> getExpandedTermsJaccard(List<String> originalTerms, int numExpTerms){

        Set<String> expandedTermsSet = new HashSet<>();
        try{
            for (String term : originalTerms) {
                expandedTermsSet.add(term);
                for (String string : term.split(" ")) {
                    if(string.equalsIgnoreCase("de") == false && string.equalsIgnoreCase(" ") == false && string.equalsIgnoreCase("date") == false && string.equalsIgnoreCase("html") == false){
                        //System.out.println("Expanded Terms for '" + string + "':");
                        //expandedTermsSet.add(string);
                        List<String> expanded = matrixJaccard(numExpTerms, string);
                        expandedTermsSet.addAll(expanded);
                        System.out.println("	-'"+ string + "': " + expanded);
                    }
                }
            }
            List<String> expandedTerms = new ArrayList<>();
            expandedTerms.addAll(expandedTermsSet);
            return expandedTerms;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<String> getExpandedTermsMenorDistancia(List<String> originalTerms, int numExpTerms, int maxDist){
        //List<String> expandedTerms = new Char<>();
        Set<String> expandedTermsSet = new HashSet<>();
        try{
            for (String term : originalTerms) {
                for (String string : term.split(" ")) {
                    if (string.equalsIgnoreCase("de") != true) {
                        //System.out.println("Expanded Terms for '" + string + "':");
                        expandedTermsSet.add(string);
                        List<String> expanded = matrixMenorDistancia(numExpTerms, string, maxDist);
                        expandedTermsSet.addAll(expanded);
                        System.out.println("	-'"+ string + "': " + expanded);
                    }
                }
            }
            List<String> expandedTerms = new ArrayList<>();
            expandedTerms.addAll(expandedTermsSet);
            return expandedTerms;
        }catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Jaccard
    public List<String> matrixJaccard(int numExpTerms, String term) throws IOException{

        Term termI = new Term(LuceneConstants.CONTENTS, term);

        Directory indexDirectory = FSDirectory.open(new File(indexDir).toPath());

        //create the index reader
        IndexReader reader = DirectoryReader.open(indexDirectory);

        //getting index vocabulary
        Terms termsJ = MultiFields.getFields(reader).terms(LuceneConstants.CONTENTS);

        long numTerms = termsJ.size();

        ArrayList<TwoTermsWeight> list = new ArrayList<TwoTermsWeight>();

        //System.err.println("Building association matrix...");
        //System.err.println("Weighting based on Jaccard coefficient");

        TermsEnum termsEnumJ = termsJ.iterator();
        for(int j = 0; j < numTerms; j++){
            termsEnumJ.next();
            TwoTermsWeight aux = new TwoTermsWeight(get2termsWeightJaccard(termI, new Term(LuceneConstants.CONTENTS, termsEnumJ.term()), reader), termI.text(), termsEnumJ.term().utf8ToString());
            //System.out.println(aux);
            if(aux.getWeight() > 0){
                list.add(aux);
            }
        }

        //System.out.println("Sorting result...");
        Collections.sort(list);

        List<String> result = new ArrayList<>();
        //System.out.println("======> " + list.size());
        for (int i = 1; i <= numExpTerms && list.size() > 0; i++) {
            if(list.get(i).getTerm2().length() <= 2 || StringUtil.isNumeric(list.get(i).getTerm2()) || list.get(i).getTerm2().matches(".*\\d+.*") || list.get(i).getTerm1().equalsIgnoreCase(list.get(i).getTerm2())){
                //if(numExpTerms < list.size() - 1){
                numExpTerms++;
                //}
            }
            else{
                result.add(list.get(i).getTerm2());
                //System.out.println(list.get(i).getTerm2());
            }
        }

        return result;
    }

    //getting the similarity between two terms (t1, t2) based on Jaccard coefficient
    public double get2termsWeightJaccard(Term t1, Term t2, IndexReader reader) throws IOException{

        IndexSearcher searcher = new IndexSearcher(reader);

        BooleanQuery.Builder q = new BooleanQuery.Builder();

        Query q1 = new TermQuery(t1);
        Query q2 = new TermQuery(t2);

        q.add(q1, BooleanClause.Occur.MUST);
        q.add(q2, BooleanClause.Occur.MUST);

        BooleanQuery query = q.build();

        int dfq1ANDq2 = searcher.count(query);

        BooleanQuery.Builder bq =  new BooleanQuery.Builder();

        bq.add(q1, BooleanClause.Occur.SHOULD);
        bq.add(q2, BooleanClause.Occur.SHOULD);

        int dfq1ORq2 = searcher.count(bq.build());

        return dfq1ANDq2 / (dfq1ORq2 + 0.0);
    }

    //MenorDistancia
    public List<String> matrixMenorDistancia(int numExpTerms, String term, int maxDist) throws IOException{

        Term termI = new Term(LuceneConstants.CONTENTS, term);

        Directory indexDirectory = FSDirectory.open(new File(indexDir).toPath());

        //create the index reader
        IndexReader reader = DirectoryReader.open(indexDirectory);

        //getting index vocabulary
        Terms termsJ = MultiFields.getFields(reader).terms(LuceneConstants.CONTENTS);

        long numTerms = termsJ.size();

        ArrayList<TwoTermsWeight> list = new ArrayList<TwoTermsWeight>();

        //System.err.println("Building association matrix...");
        //System.err.println("Weighting based on Jaccard coefficient");

        TermsEnum termsEnumJ = termsJ.iterator();
        for(int j = 0; j < numTerms; j++){
            termsEnumJ.next();
            TwoTermsWeight aux = new TwoTermsWeight(get2termsWeightPositions(termI,
                    new Term(LuceneConstants.CONTENTS, termsEnumJ.term()),
                    maxDist,
                    reader),
                    termI.text(),
                    termsEnumJ.term().utf8ToString());
            if(aux.getWeight() > 0){
                list.add(aux);
            }
        }

        //System.out.println("Sorting result...");
        Collections.sort(list);
        Collections.reverse(list);

        List<String> result = new ArrayList<>();

        for (int i = 1; i <= numExpTerms && list.size() > 0; i++) {
            if(list.get(i).getTerm2().length() <= 2 || StringUtil.isNumeric(list.get(i).getTerm2()) || list.get(i).getTerm2().matches(".*\\d+.*") || list.get(i).getTerm1().equalsIgnoreCase(list.get(i).getTerm2())){
                numExpTerms++;
            } else {
                result.add(list.get(i).getTerm2());
                //System.out.println(list.get(i).getTerm2());
            }
        }
        return result;
    }

    //return the number of documents where t1 and t2 co-occur within a distance limit
    public int get2termsWeightPositions(Term t1, Term t2, int maxDist, IndexReader reader) throws IOException{

        IndexSearcher searcher = new IndexSearcher(reader);

        BooleanQuery.Builder q = new BooleanQuery.Builder();

        Query q1 = new TermQuery(t1);
        Query q2 = new TermQuery(t2);

        q.add(q1, BooleanClause.Occur.MUST);
        q.add(q2, BooleanClause.Occur.MUST);

        BooleanQuery query = q.build();

        int score = Integer.MAX_VALUE;

        if(searcher.count(query) > 0){
            score = 0;
        } else {
            return score;
        }

        ScoreDoc[] docs = searcher.search(query, reader.numDocs()).scoreDocs;

        ArrayList<PositionDocId> postingsT1 = getTermPositionalPostings(reader, t1);
        ArrayList<PositionDocId> postingsT2 = getTermPositionalPostings(reader, t2);

        for (ScoreDoc scoreDoc : docs) {
            int docId = scoreDoc.doc;
            int pos1 = 0;
            int pos2 = 0;
            int minDist = Integer.MAX_VALUE;
            for (PositionDocId posting1 : postingsT1){

                if (posting1.getDocId() == docId){

                    pos1 = posting1.getPosition();
                    //System.out.println("t1: (" + pos1 + ", " + docId + ")");

                    for (PositionDocId posting2 : postingsT2) {
                        if (posting2.getDocId() == docId){
                            pos2 = posting2.getPosition();
                            //System.out.println("t2: (" + pos2 + ", " + docId + ")");
                            int dist = Math.abs(pos1 - pos2);
                            //System.out.println("Distancia: " + dist + ", MinDist = " + minDist);
                            if(dist < minDist)
                                minDist = dist;
                            //System.out.println("MinDist = " + minDist);
                        }
                    }
                }
                if(minDist == 1) {
                    break;
                }
            }
            if(minDist <= maxDist) score += minDist;
            else score += maxDist+1;
        }

        //System.out.println("(" + t1.bytes().utf8ToString() + ", " + t2.bytes().utf8ToString() + ") = " + score);
        return score/docs.length;
    }

    private ArrayList<PositionDocId> getTermPositionalPostings(IndexReader reader, Term t) throws IOException{

        int docFreq = reader.docFreq(t);

        PostingsEnum postingsEnum = MultiFields.getTermDocsEnum(reader, LuceneConstants.CONTENTS, t.bytes(), PostingsEnum.ALL);

        ArrayList<PositionDocId> postings = new ArrayList<>();

        for(int i = 0; i < docFreq; i++){
            int docId = postingsEnum.nextDoc();
            int freqTermDoc = postingsEnum.freq();
            //System.out.println("Current doc: " + docId + " (freq = " + freqTermDoc  + ")");

            for(int j = 0; j < freqTermDoc; j++){
                int position = postingsEnum.nextPosition();
                postings.add(new PositionDocId(position, docId));
                //System.out.print("(" + position + ", " + docId + ")  ");
            }
        }

        return postings;
    }

    public String getIndexDir() {
        return indexDir;
    }

    public void setIndexDir(String indexDir) {
        this.indexDir = indexDir;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }
}

class LuceneConstants {
    public static final String CONTENTS="contents";
    public static final String FILE_NAME="filename";
    public static final String FILE_PATH="filepath";
    public static final int MAX_SEARCH = 10;
}

class PositionDocId {

    private int position;
    private int docId;

    public PositionDocId() {
        // TODO Auto-generated constructor stub
        this.position = -1;
        this.docId = -1;
    }
    public PositionDocId(int position, int docId) {
        super();
        this.position = position;
        this.docId = docId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }
}

class TwoTermsWeight implements Comparable<TwoTermsWeight>, Serializable {

    private static final long serialVersionUID = 4254679063576654035L;
    private float weight;
    private String term1;
    private String term2;

    public TwoTermsWeight(float weight, String term1, String term2) {
        super();
        this.weight = weight;
        this.term1 = term1;
        this.term2 = term2;
    }

    public TwoTermsWeight(double weight, String term1, String term2) {
        super();
        this.weight = (float)weight;
        this.term1 = term1;
        this.term2 = term2;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public String getTerm1() {
        return term1;
    }

    public void setTerm1(String term1) {
        this.term1 = term1;
    }

    public String getTerm2() {
        return term2;
    }

    public void setTerm2(String term2) {
        this.term2 = term2;
    }

    public int compareTo(TwoTermsWeight o) {
        // TODO Auto-generated method stub

        if(this.getWeight() > o.getWeight())
            return -1;
        if(this.getWeight() < o.getWeight())
            return 1;
        return 0;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return "(" + getTerm1() + ", " + getTerm2() + ")" + " = " + getWeight() + " ";
    }
}