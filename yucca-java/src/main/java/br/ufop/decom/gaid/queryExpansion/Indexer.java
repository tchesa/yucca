package br.ufop.decom.gaid.queryExpansion;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.br.BrazilianAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.util.ArrayList;
import java.util.Arrays;

public class Indexer {

    private IndexWriter writer;

    public Indexer(String indexDirectoryPath) throws IOException{
        File indexFile = new File(indexDirectoryPath);

        //removing older index
        deleteDir(indexFile);

        //this directory will contain the indexes
        Directory indexDirectory = FSDirectory.open(new File(indexDirectoryPath).toPath());

        //defining the analyzer type
//        CharArraySet stopWords = new CharArraySet(new StopWords().getBrazilianStopSet(), true);
        CharArraySet stopWords = new CharArraySet(new ArrayList<String>(Arrays.asList(Stopwords.getStopwords("pt-br"))), true);
        stopWords.addAll(BrazilianAnalyzer.getDefaultStopSet());
        StandardAnalyzer analyzer = new StandardAnalyzer(stopWords);

        //configuring the writer
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        //create the indexer
        writer = new IndexWriter(indexDirectory, config);
    }

    //close the IndexWriter
    public void close() throws CorruptIndexException, IOException{
        writer.close();
    }

    private Document getDocument(File file) throws IOException{

        Document document = new Document();

        FieldType myftype = new FieldType();
        myftype.setStored(false);
        myftype.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        myftype.setStoreTermVectors(true);
        myftype.setStoreTermVectorOffsets(true);
        myftype.setStoreTermVectorPositions(true);
        //index file contents
        Field contentField = new Field(LuceneConstants.CONTENTS, new FileReader(file), myftype);

        //index file name
        Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), myftype);

        //index file path
        Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), myftype);

        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);

        return document;
    }

    private void indexFile(File file) throws IOException{
        System.out.println("Indexing "+ file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    public int createIndex(String dataDirPath, TextFileFilter filter, int numFiles) throws IOException{

        //get all files in the data directory
        File[] files = new File(dataDirPath).listFiles();
        int aux = 0;
        for (File file: files){
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file) && aux < numFiles){
                aux++;
                indexFile(file);
            }
        }

        return writer.numDocs();
    }

    public boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete(); // The directory is empty now and can be deleted.
    }
}

class TextFileFilter implements FileFilter {
    public boolean accept(File pathname) {
        // TODO Auto-generated method stub
        return pathname.getName().toLowerCase().endsWith(".txt");
    }
}