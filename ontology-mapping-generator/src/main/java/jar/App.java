package jar;


import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App {

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-s":
            }
        }
        System.out.println("Loading configuration file...");
        JSONObject config = loadConfigFile();
        JSONObject blacklist = (JSONObject) config.get("replacement_blacklist");
        String[] optionalList = getOptionalWords(config);
        
        System.out.println("Loading ontology terms...");
        DB con = new DB("bolt://localhost/", "neo4j", "12345", 7687, optionalList);
        Ontology sourceOnt = new Ontology("MP", "MP", con);
        System.out.println("MP terms extracted: " + sourceOnt.termCount());
        Ontology targetOnt = new Ontology("MESH", "MESH", con);
        System.out.println("MESH terms extracted: " + targetOnt.termCount());

        System.out.println("Creating mapping threads...");

        int coreCount = Runtime.getRuntime().availableProcessors();
        int chunkSize = Math.abs(sourceOnt.termCount() / coreCount);
        double remainder = Math.ceil(sourceOnt.termCount() % coreCount);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Set<Callable<List<SymLink>>> callables = new HashSet<Callable<List<SymLink>>>();

        for (int i = 0; i < coreCount; i++) {
            int end = 0;
            if (i == coreCount - 1) {
                end = ((i + 1) * chunkSize) + (int) remainder;
            } else {
                end = (i + 1) * chunkSize;
            }
            List<OntologyTerm> dividedSource = sourceOnt.getTerms().subList(i * chunkSize, end);
            MappingAgent agent = new MappingAgent("Thread-" + i, dividedSource, targetOnt.getTerms(), blacklist);
            callables.add(agent);
        }
        List<Future<List<SymLink>>> resultFutures = null;
        try {
            resultFutures = Executors.newFixedThreadPool(coreCount).invokeAll(callables);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<SymLink> mappings = new ArrayList<>();
        try {
            for (Future<List<SymLink>> result : resultFutures) {
                if (result.get().size() > 0) {
                    mappings.addAll(result.get());
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

//        List<SymLink> mappings = Mapper.mapOntologies(sourceOnt.getTerms(), targetOnt.getTerms(), blacklist);
        outputMappings(mappings);
        executorService.shutdown();

        System.exit(0);
    }
    
    public static String[] getOptionalWords(JSONObject config) {
        JSONObject optionalObj = (JSONObject) config.get("optional_list");
        JSONArray optionalStrings = (JSONArray)optionalObj.get("MP");
        String[] result = new String[optionalStrings.size()];
        for (int i = 0; i < optionalStrings.size(); i++) {
            result[i] = optionalStrings.get(i).toString();
        }
        return result;
    }

    public static void outputMappings(List<SymLink> mappings) {
        String time = new SimpleDateFormat("_dd-MM-yyyy_HH-mm-ss").format(new Date());
        File file = new File("output_" + time + ".json");
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(file, mappings);
            System.out.println("Results successfully written to output_" + time + ".json");
        } catch (IOException e) { 
            e.printStackTrace();
        }

    }

    public static JSONObject loadConfigFile() {
        String path = "config.json";
        try ( Reader reader = new FileReader(path)) {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            return jsonObject;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
