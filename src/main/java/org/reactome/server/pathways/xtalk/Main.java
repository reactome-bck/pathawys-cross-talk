
package org.reactome.server.pathways.xtalk;

import com.martiansoftware.jsap.*;
import org.reactome.server.graph.domain.model.ReferenceEntity;
import org.reactome.server.graph.service.GeneralService;
import org.reactome.server.graph.utils.ReactomeGraphCore;
import org.reactome.server.pathways.xtalk.config.ReactomeNeo4jConfig;
import org.reactome.server.pathways.xtalk.factory.LinkFactory;
import org.reactome.server.pathways.xtalk.model.Link;
import org.reactome.server.pathways.xtalk.util.LinkReportFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Antonio Fabregat (fabregat@ebi.ac.uk)
 */
public class Main {

    public static void main(String[] args) throws JSAPException {

        // Program Arguments -h, -p, -u, -k
        SimpleJSAP jsap = new SimpleJSAP(Main.class.getName(), "Reports interactors as possible bridges between pathways in Reactome",
                new Parameter[]{
                        new FlaggedOption("host",     JSAP.STRING_PARSER, "localhost", JSAP.NOT_REQUIRED, 'h', "host",     "The neo4j host"),
                        new FlaggedOption("port",     JSAP.STRING_PARSER, "7474",      JSAP.NOT_REQUIRED, 'p', "port",     "The neo4j port"),
                        new FlaggedOption("user",     JSAP.STRING_PARSER, "neo4j",     JSAP.NOT_REQUIRED, 'u', "user",     "The neo4j user"),
                        new FlaggedOption("score",    JSAP.STRING_PARSER, "0.45",      JSAP.NOT_REQUIRED, 's', "score",    "Threshold score for IntAct interactions (range [0.45, 1])"),
                        new FlaggedOption("output",   JSAP.STRING_PARSER,  null,       JSAP.REQUIRED,     'o', "output",   "Output file"),
                        new FlaggedOption("password", JSAP.STRING_PARSER, "neo4j",     JSAP.REQUIRED,     'k', "password", "The neo4j password")
                }
        );

        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        //Initialising ReactomeCore Neo4j configuration
        ReactomeGraphCore.initialise(config.getString("host"),
                                     config.getString("port"),
                                     config.getString("user"),
                                     config.getString("password"),
                                     ReactomeNeo4jConfig.class);
        double score = getScore(config.getString("score"));
        String output = config.getString("output");

        // Access the data using our service layer.
        GeneralService genericService = ReactomeGraphCore.getService(GeneralService.class);
        System.out.println("Database name: " + genericService.getDBInfo().getName());
        System.out.println("Database version: " + genericService.getDBInfo().getVersion());
        System.out.println("Score: " + score);
        System.out.println("Output: " + output);
        System.out.println();

        final long start = System.currentTimeMillis();
        Collection<Link> links = LinkFactory.getLinks(score);

        Set<ReferenceEntity> interactors = new HashSet<>();
        List<String> lines = new ArrayList<>();
        for (Link link : links){
            interactors.addAll(link.getBridgeMap().keySet());
            lines.add(LinkReportFactory.get().report(link));
        }
        lines.add(0, String.format(
                "Summary,Release: %d\n,Score: %.2f\n,Pair of pathways: %d\n,Bridging new molecules: %d\n\n",
                genericService.getDBInfo().getVersion(),
                score,
                links.size(),
                interactors.size()
        ));
        final long time = System.currentTimeMillis() - start;

        if(saveReport(lines, output)){
            System.out.println("Pair of pathways: " + links.size());
            System.out.println("Bridging new proteins: " + interactors.size());
            System.out.println("\nFinished in " + getTimeFormatted(time));
        }
    }

    private static boolean saveReport(List<String> report, String fileName) {
        try {
            Path path = Paths.get(fileName);
            Files.deleteIfExists(path);
            if (!Files.isSymbolicLink(path.getParent())) Files.createDirectories(path.getParent());
            Files.createFile(path);

            Files.write(path, report, Charset.forName("UTF-8"));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static double getScore(String score) {
        double rtn = 0.45;
        try {
            rtn = Double.valueOf(score);
        } catch (NumberFormatException e) {
            System.err.println("The score formatting is incorrect: '" + score + "'.");
            System.exit(1);
        }
        if (rtn < 0.45 || rtn > 1) {
            System.err.println("Accepted score range [0.45 - 1]");
            System.exit(1);
        }
        return rtn;
    }

    private static String getTimeFormatted(Long millis) {
        return String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1));
    }
}