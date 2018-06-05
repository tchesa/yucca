package br.ufop.decom.gaid.focused_crawler.integration;

import br.ufop.decom.gaid.focused_crawler.similarity.CosineSimilarity;
import br.ufop.decom.gaid.focused_crawler.similarity.SimilarityMetric;
import br.ufop.decom.gaid.focused_crawler.util.Loader;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mvosouza on 08/03/17.
 */
public class Teste {
    public static String url = "https://sites.google.com/site/proftheobaldo/disciplinas/banco-de-dados";

    public static String text = "Prof. Anderson TheobaldoPesquisar o site\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Seja bem vindoBlogDisciplinasAlgoritmos e Estruturas de DadosArquitetura de SistemasArquitetura de Sistemas DistribuídosAuditoria de SistemasBanco de Dados Para RedesImplementação de Banco de DadosLinguagem de ProgramaçãoMetodologias de Desenvolvimento de SistemasModelagem de DadosOrganização e Arquitetura de ComputadoresProgramação Para InternetProgramação para ServidoresProjeto de Desenvolvimento de SistemasTenologias WEBSobreSitemap\n" +
            "\n" +
            "\n" +
            " \n" +
            "\n" +
            "Disciplinas\u200E > \u200E\n" +
            "  \n" +
            "\n" +
            "Banco de Dados Para Redes\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "A perspectiva da disciplina de Banco de Dados aponta para um trabalho conjunto com outras disciplinas na formação do aluno no curso de Redes de Computadores. Ao desenvolver habilidades e competências dos alunos de maneira mais abrangente, envolvendo a análise de novos modos de compreensão a partir da relação entre as aplicações e os sistemas de Banco de Dados, a disciplina pode proporcionar ao profissional de Redes de Computadores melhores condições de desenvolvimento do seu trabalho. Destaca-se, portanto, a prática de interação participativa em grupo de trabalho de campos e saberes conexos. A construção de um pensamento interdisciplinar  pode favorecer a exploração de diversos prismas de análise, propiciando uma visão dinâmica e inclusiva no processo de TI, sendo esta uma das demandas atuais para a formação de novos profissionais da área.\n" +
            "\n" +
            "\n" +
            " \n" +
            " \n" +
            "\n" +
            "\n" +
            "\n" +
            "            Subpáginas (4):\n" +
            "\n" +
            "Aulas\n" +
            "\n" +
            "\n" +
            "Exercícios\n" +
            "\n" +
            "\n" +
            "Plano de Ensino\n" +
            "\n" +
            "\n" +
            "Vídeos\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "Comments\n" +
            "\n" +
            " \n" +
            " \n" +
            " \n" +
            " \n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            "\n" +
            " \n" +
            " \n" +
            "\n" +
            "Sign in|Recent Site Activity|Report Abuse|Print Page|Powered By Google Sites";

    private Loader loader = Loader.getInstance();
    public SimilarityMetric similarityMetric;

    private List<String> genreTerms;
    private List<String> contentTerms;
    private List<String> urlTerms;

    public Teste() {
        onStart();
    }

    public void onStart() {
        synchronized (loader) {
            if (!loader.isInitialized()) {
                loader.init();
            }
            this.genreTerms = loader.loadGenreTerms();
            this.contentTerms = loader.loadContentTerms();
            this.urlTerms = new ArrayList<>();
            urlTerms.addAll(genreTerms);
            urlTerms.addAll(contentTerms);
        }

        this.similarityMetric = new CosineSimilarity(genreTerms, contentTerms, urlTerms, 0.5, 0.5, 0.8, 0.1,
                0.0);
    }

    public static void main(String[] args) {
        Teste teste = new Teste();

        try {
            System.out.println(url);
            System.out.println(text);
            System.out.println("Similaridade= "+teste.similarityMetric.similarity2(text, url, 333));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
