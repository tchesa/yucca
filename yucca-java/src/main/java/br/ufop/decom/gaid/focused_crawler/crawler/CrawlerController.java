package br.ufop.decom.gaid.focused_crawler.crawler;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import br.ufop.decom.gaid.queryExpansion.QueryExpansion;
import br.ufop.decom.gaid.queryExpansion.TermFrequency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufop.decom.gaid.focused_crawler.seed.SeedBuilder;
import br.ufop.decom.gaid.focused_crawler.seed.search.GoogleAjaxSearch;
import br.ufop.decom.gaid.focused_crawler.similarity.CosineSimilarity;
import br.ufop.decom.gaid.focused_crawler.threshold.summarization.ArithmeticMean;
import br.ufop.decom.gaid.focused_crawler.threshold.evaluative.SilhouetteCoefficient;
import br.ufop.decom.gaid.focused_crawler.threshold.clustering.BIRCHClustering;
import br.ufop.decom.gaid.focused_crawler.threshold.clustering.KMeansClustering;
import br.ufop.decom.gaid.focused_crawler.util.Loader;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import edu.uci.ics.crawler4j.url.WebURL;

public class CrawlerController {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerController.class);

    /**
     * Files extensions that must be ignored by the crawler.
     */
    public static final Pattern FILTERS = Pattern.compile(".*(\\.(css|js|bmp|gif|jpe?g|png|tiff?|mid|mp2|mp3|mp4"
            + "doc|docx|xsd|ppt|wav|avi|mov|mpeg|ram|m4v|pdf|rm|smil|wmv|swf|wma|zip|rar|gz))$");

    private static CrawlerController instance;
    public static double threshold;

    private Loader loader = Loader.getInstance();

    private PageFetcher pageFetcher;
    private RobotstxtConfig robotstxtConfig;
    private RobotstxtServer robotstxtServer;
    private CrawlController controller;

    private int numberOfCrawlers;

    private List<String> genreTerms;
    private List<String> contentTerms;

    public static CrawlerController getInstance() {
        if (instance == null) {
            instance = new CrawlerController();
        }

        return instance;
    }

    private CrawlerController() {
    }

    public void init(String propPath) throws Exception {

        System.out.println("LOG: Initializing crawler's configuration...");

		/*
         * Initializing genre and content terms for future purposes
		 */
        loader.setConfigFile(propPath);
        loader.init();
        genreTerms = loader.loadGenreTerms();
        contentTerms = loader.loadContentTerms();

		/*
         * processStorageFolder is a folder where intermediate crawl data is
		 * stored. crawlerStorageFolder is a folder where visited pages are
		 * stored. relevantsStorageFolder is a folder where relevant, according
		 * to desired topic, crawl data is stored.
		 */
        // TODO make storage folder be read from config file.
        String processStorageFolder = "./processProperties";
        String crawlerStorageFolder = "./crawledPages";
        String relevantsStorageFolder = "./relevantPages";

		/*
         * numberOfCrawlers shows the number of concurrent threads that should
		 * be initiated for crawling.
		 */
        //numberOfCrawlers = 4;
        numberOfCrawlers = Runtime.getRuntime().availableProcessors();

        CrawlConfig config = new CrawlConfig();

        config.setCrawlStorageFolder(processStorageFolder);
        new File(crawlerStorageFolder).mkdirs();
        new File(relevantsStorageFolder).mkdirs();

		/*
		 * Be polite: Make sure that we don't send more than 1 request per
		 * second (1000 milliseconds between requests).
		 */
        config.setPolitenessDelay(1000);

		/*
		 * You can set the maximum crawl depth her. The default value is -1 for
		 * unlimited depth
		 */
        config.setMaxDepthOfCrawling(-1);

		/*
		 * You can set the maximum number of pages to crawl. The default value
		 * is -1 for unlimited number of pages
		 */
        BufferedReader bufferedReader = null;
        try {
            InputStream inputStream = new FileInputStream("maxPagSementes.collect");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String linhaDoArquivo = bufferedReader.readLine();
            int informarMaxPags = Integer.parseInt(linhaDoArquivo);
            System.out.print("Informar max pags sementes: ");
            System.out.println(informarMaxPags);

            if (informarMaxPags == 0) {
                config.setMaxPagesToFetch(50);
            } else {
                linhaDoArquivo = bufferedReader.readLine();
                int qtdMax = Integer.parseInt(linhaDoArquivo);
                config.setMaxPagesToFetch(qtdMax);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            bufferedReader.close();
        }

		/*
		 * This config parameter can be used to set your crawl to be resumable
		 * (meaning that you can resume the crawl from a previously
		 * interrupted/crashed crawl). Note: if you enable resuming feature and
		 * want to start a fresh crawl, you need to delete the contents of
		 * rootFolder manually.
		 */
        config.setResumableCrawling(false);

		/*
		 * Instantiate the controller for this crawl.
		 */
        pageFetcher = new PageFetcher(config);
        robotstxtConfig = new RobotstxtConfig();
        robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        controller = new CrawlController(config, pageFetcher, robotstxtServer);

        /* query Expansion v2 */
        try {
            Properties prop = new Properties();
            prop.load(new FileInputStream("queryExpansion.properties"));

            boolean useQueryExpansion = Boolean.parseBoolean(prop.getProperty("use"));
            resetExpansionFiles();

            if (useQueryExpansion) {
                String method = prop.getProperty("method"); // método de expansão de termos
                int numSeeds = Integer.parseInt(prop.getProperty("numSeeds")); // numero de páginas geradas
                int k = Integer.parseInt(prop.getProperty("k")); // top termos mais frequentes
                QueryExpansion expansion;

                /*switch (method) {
                    case "TermFrequency": expansion = new TermFrequency(); break;
                    case "SimilarityMatrix": expansion = new ...
                    default: ...
                }*/
                expansion = new TermFrequency();

                System.out.println("Query expansion started");

                SeedBuilder builder = new SeedBuilder(new GoogleAjaxSearch(), numSeeds);
                List<WebURL> seeds = builder.buildGenreSeeds(); // busca por páginas usando apenas os termos de genero
                String[] genreTerms = expansion.getTerms(seeds, k); // recupera os termos mais frequentes
//                System.out.println("GENRE: {" + String.join(", ", genreTerms) + "}");
                Path file = Paths.get("genre.expansion");
                Files.write(file, Arrays.asList(genreTerms), Charset.forName("UTF-8")); // escreve os termos no arquivo

                seeds = builder.buildContentSeeds(); // busca por páginas usando apenas os termos de conteudo
                String[] contentTerms = expansion.getTerms(seeds, k); // recupera os termos mais frequentes
//                System.out.println("CONTENT: {" + String.join(", ", contentTerms) + "}");
                file = Paths.get("content.expansion");
                Files.write(file, Arrays.asList(contentTerms), Charset.forName("UTF-8")); // escreve os termos no arquivo
            }
        } catch (Throwable err) {
            System.out.println(err);
        }


        List<WebURL> seeds = getSeeds();

		/*
		 * Defines similarity threshold for focused crawling process.
		 */
        // TODO check a better pattern for different threshold implementations, maybe factory method.


        try {
            InputStream inputStream = new FileInputStream("heuLimSim.collect");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);

            String linhaDoArquivo = bufferedReader.readLine();
            int heuLimSim = Integer.parseInt(linhaDoArquivo);
            System.out.print("Heuristica limite de similaridade: ");
            System.out.println(heuLimSim);

            linhaDoArquivo = bufferedReader.readLine();
            double genreWeight = Double.parseDouble(linhaDoArquivo);
            System.out.print("Peso de gênero: ");
            System.out.println(genreWeight);

            linhaDoArquivo = bufferedReader.readLine();
            double contentWeight = Double.parseDouble(linhaDoArquivo);
            System.out.print("Peso de conteúdo: ");
            System.out.println(contentWeight);

            switch (heuLimSim) {
                case 0:         //K-Means
                    threshold = new KMeansClustering(new CosineSimilarity(genreTerms, contentTerms, genreWeight, contentWeight), seeds).getThreshold();
                    break;
                case 1:         //BIRCH
                    threshold = new BIRCHClustering(new CosineSimilarity(genreTerms, contentTerms, genreWeight, contentWeight), seeds).getThreshold();
                    break;
                case 2:         //Coeficiente de silhueta
                    threshold = new SilhouetteCoefficient(new CosineSimilarity(genreTerms, contentTerms, genreWeight, contentWeight), seeds).getThreshold();
                    break;
                case 3:         //Media aritmetica
                    threshold = new ArithmeticMean(new CosineSimilarity(genreTerms, contentTerms, genreWeight, contentWeight), seeds).getThreshold();
                    break;
            }
            bufferedReader.close();
            System.out.print("threshold: ");
            System.out.println(threshold);
            System.out.println("LOG: Similarity threshold were defined as " + threshold);
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Salvar valor de similaridade em arquivo*/
        FileWriter file = new FileWriter("similarity.fcrawler");
        PrintWriter printFile = new PrintWriter(file);
        printFile.printf("Similarity= %f", threshold);
        printFile.close();
        file.close();

        /*Salvando o valor da similaridade em um objeto*/
        ObjectOutputStream os = null;
        os = new ObjectOutputStream(new FileOutputStream("similarityObject.fcrawler"));
        os.writeObject(threshold);
        os.close();

        /* Expansão dos termos v1 */

        /*System.out.println("Query Expansion started");

        QueryExpansion expansion = new TermFrequency();
        String[] seedUrls = new String[seeds.size()];
        for (int i = 0; i < seeds.size(); i++) seedUrls[i] = seeds.get(i).getURL();
        String[] expandedTerms = expansion.getTerms(seedUrls);
        System.out.println("{" + String.join(", ", expandedTerms) + "}");*/
    }

    public void run() {
		/*
		 * Start the crawl. This is a blocking operation, meaning that your code
		 * will reach the line after this only when crawling is finished.
		 */

        System.out.println("LOG: Starting crawl process.");

        controller.start(CrawlerWorker.class, numberOfCrawlers);

        System.out.println("LOG: Crawl process finished.");
    }

    private List<WebURL> getSeeds() {
        List<WebURL> seeds = null;
        try {
            InputStream inputStream = new FileInputStream("pagSementes.collect");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String linhaDoArquivo = bufferedReader.readLine();
            int informarPagSemente = Integer.parseInt(linhaDoArquivo);
            System.out.print("Informar pag semente: ");
            System.out.println(informarPagSemente);

            if (informarPagSemente == 0) { // as paginas sementes serao geradas automaticamente
                linhaDoArquivo = bufferedReader.readLine();
                int numSeeds = Integer.parseInt(linhaDoArquivo);
                System.out.print("Num seeds: ");
                System.out.println(numSeeds);
                SeedBuilder builder = new SeedBuilder(new GoogleAjaxSearch(), numSeeds);
                seeds = builder.build();
                for (WebURL seed : seeds) {
                    controller.addSeed(seed.getURL());
                }
            } else if (informarPagSemente == 1) {
                seeds = new ArrayList<>();
                linhaDoArquivo = bufferedReader.readLine();
                while (linhaDoArquivo != null) {
                    WebURL url = new WebURL();
                    url.setURL(linhaDoArquivo);
                    seeds.add(url);
                    linhaDoArquivo = bufferedReader.readLine();
                }
                for (WebURL seed : seeds) {
                    System.out.println("num seeds: " + seeds.size());
                    controller.addSeed(seed.getURL());
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return seeds;
    }

    private void resetExpansionFiles() throws java.io.IOException {
        // cria arquivos vazios
        Path file = Paths.get("genre.expansion");
        Files.write(file, Arrays.asList(new String[0]), Charset.forName("UTF-8"));
        file = Paths.get("content.expansion");
        Files.write(file, Arrays.asList(new String[0]), Charset.forName("UTF-8"));
    }
}
