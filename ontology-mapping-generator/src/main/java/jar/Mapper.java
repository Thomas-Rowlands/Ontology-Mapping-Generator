package jar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.lang.Character;

public class Mapper {

    private static JSONObject blacklist;
            
    public static void output_mappings(Mapping mapping) {
        
    }
    
    public static void setBlacklist(JSONObject blacklist) {
        Mapper.blacklist = blacklist;
    }
    
    public static List<SymLink> mapOntologies(List<OntologyTerm> source, List<OntologyTerm> target, JSONObject blacklist) {
        Mapper.blacklist = blacklist;
        List<SymLink> mappings = new ArrayList<>();
        for (OntologyTerm sourceTerm: source) {
            for (OntologyTerm targetTerm: target) {
                SymLink result = compareTerms(sourceTerm, targetTerm);
                if (result != null)
                    mappings.add(result);
            }
        }
//        OntologyTerm sourceTerm = new OntologyTerm("melons", "blah", 12345, null, false);
//        OntologyTerm targetTerm = new OntologyTerm("melon", "meh", 54321, null, false);
//        SymLink result = compareTerms(sourceTerm, targetTerm);
//        if (result != null)
//            mappings.add(result);
        return mappings;
    }

    public static String standardiseTermLabel(String termLabel) {
        String[] replacementArray = {" ", ",", "-", "'"};
        for (int i = 0;i < replacementArray.length; i++) {
            termLabel = termLabel.replaceAll(replacementArray[i], "");
        }
        return termLabel.toLowerCase();
    }

    public static boolean compareSubStrings(String termLabelOne, String termLabelTwo) {
        /**
         * Compare each word within the input labels to one another, returning true if a match exists.
         */
        int fuzzy_allowance = 1;
        String[] termOneSubStrings = termLabelOne.replaceAll("-", " ").split(" ");
        String[] termTwoSubStrings = termLabelTwo.replaceAll("-", " ").split(" ");
        
        if (termOneSubStrings.length != termTwoSubStrings.length)
            return false;
        
        for (int i = 0; i < termOneSubStrings.length; i++) {
            if (!fuzzyMatch(termOneSubStrings[i], termTwoSubStrings[i], fuzzy_allowance))
                return false;
        }
        return true;
    }

    private static int[] appendToArray(int[] oldArray, int newValue) {
        int[] newArray = new int[oldArray.length + 1];
        for (int i = 0; i < oldArray.length; i++) {
            newArray[i] = oldArray[i];
        }
        newArray[newArray.length - 1] = newValue;
        return newArray;
    }

    public static boolean fuzzyMatch(String termOne, String termTwo, int allowance) {
        
        int[] nonMatchingLocations = new int[0];
        boolean abbreviationDetected = false;
        
        if (!isAbbreviation(termOne)) 
            termOne = standardiseTermLabel(termOne);
        else
            abbreviationDetected = true;
        
        if (!isAbbreviation(termTwo)) 
            termTwo = standardiseTermLabel(termTwo);
        else
            abbreviationDetected = true;
        
        if (abbreviationDetected) {
            if (termOne.equals(termTwo))
                return true;
            else
                return false;
        }
        if (termOne.length() > 4 && termTwo.length() > 4) {
            int shortestLength = termOne.length() < termTwo.length() ? termOne.length() : termTwo.length();
            int lengthDifference = Math.abs(termOne.length() - termTwo.length()); // assure the result is positive
            if (lengthDifference < 2) {
                int nonMatchingCount = lengthDifference;
                for (int i = 0; i < shortestLength; i++) {
                    if (termOne.charAt(i) != termTwo.charAt(i)) {
                        nonMatchingCount += 1;
                        nonMatchingLocations = appendToArray(nonMatchingLocations, i);
                    }
                    if (nonMatchingCount > allowance)
                        return false;
                }
                if (nonMatchingCount <= allowance) {
                    if (!termOne.equals(termTwo)) {
                        if (nonMatchingLocations.length == 0) {
                            nonMatchingLocations = appendToArray(nonMatchingLocations, Math.min(termOne.length(), termTwo.length()) - 1);
                        }
                        if (checkBlacklist(termOne, termTwo, nonMatchingLocations))
                            return true;
                        else
                            return false;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static String getSurroundingString(String term, int location) {
        /**
         * Retrieves the 3 character string surrounding the provided location.
         */
        if (location == 0)
            return "^" + term.substring(0, 2);
        if (location >= term.length() - 1)
            return term.substring(term.length() - 2, term.length()) + "$";
        return term.substring(location - 1, location + 2);
        
    }
    
    public static boolean isAbbreviation(String term) {
        /**
         * Check if all characters, except a trailing s, is upper case.
         */
        int capitalCount = 0;
        for (char letter: term.toCharArray()) {
            if (Character.isUpperCase(letter))
                capitalCount += 1;
            if (capitalCount > 1)
                return true;
        }
        return false;
    }
    
    public static boolean checkBlacklist(String termOne, String termTwo, int[] nonMatchingLocations) {
        List<String[]> changeList = new ArrayList<>();
        for (int location: nonMatchingLocations) {
            String before = "";
            String after = "";
            before = getSurroundingString(termOne, location);
            after = getSurroundingString(termTwo, location);
            String[] newChange = {before, after};
            changeList.add(newChange);
            if (location > 0)
                if ((before.equals(termOne.substring(location - 1))) && (after.equals(termTwo.substring(location - 1)))) {
                    String[] extraChange = {termOne.substring(1) + "$", termTwo.substring(1) + "$"};
                }
        }
        
        for (String[] change: changeList) {
            String before = change[0].toLowerCase();
            String after = change[1].toLowerCase();
            if (Mapper.blacklist.get(before) != null) {
                if ((Mapper.blacklist.get(before) instanceof JSONArray)) {
                    JSONArray comparators = (JSONArray)Mapper.blacklist.get(before);
                    for (Object item: comparators) {
                        if (item.toString().equals(after))
                            return false;
                    }
                } else if (Mapper.blacklist.get(before).equals(after))
                        return false;
            } else if (Mapper.blacklist.get(after) != null) {
                if ((Mapper.blacklist.get(after) instanceof JSONArray)) {
                    JSONArray comparators = (JSONArray)Mapper.blacklist.get(after);
                    for (Object item: comparators) {
                        if (item.toString().equals(before))
                            return false;
                    }
                } else if (Mapper.blacklist.get(after).equals(before))
                        return false;
            }
        }
        return true;
    }

    public static SymLink compareTerms(OntologyTerm termOne, OntologyTerm termTwo) {
        SymLink sym = new SymLink(termOne, termTwo, null);
        OntologyTerm[][] comparisonArray = new OntologyTerm[(termOne.synonymCount() * termTwo.synonymCount()) + 1][2];
        int currentComparisonPointer = 0;
        for (OntologyTerm termA: termOne.getSynonyms()) {
            for (OntologyTerm termB: termTwo.getSynonyms()) {
                OntologyTerm[] temp = {termA, termB};
                comparisonArray[currentComparisonPointer] = temp;
                currentComparisonPointer += 1;
            }
        }
        comparisonArray[currentComparisonPointer][0] = termOne;
        comparisonArray[currentComparisonPointer][1] = termTwo;
        for (int i = 0; i < comparisonArray.length; i++) {
            OntologyTerm sourceTerm =  comparisonArray[i][0];
            OntologyTerm targetTerm = comparisonArray[i][1];
            if (sourceTerm == null || targetTerm == null)
                continue;
            boolean isSourceSynonym = sourceTerm.isSynonym();
            boolean isTargetSynonym = targetTerm.isSynonym();

            if (standardiseTermLabel(sourceTerm.getMappingName()).equals(standardiseTermLabel(targetTerm.getMappingName()))) {
                Match match = new Match(sourceTerm.getName(), sourceTerm.getNeoID(), targetTerm.getName(), targetTerm.getNeoID(), isSourceSynonym, isTargetSynonym, true);
                sym.addMatch(match);
            } else if (compareSubStrings(sourceTerm.getMappingName(), targetTerm.getMappingName())) {
                Match match = new Match(sourceTerm.getName(), sourceTerm.getNeoID(), targetTerm.getName(), targetTerm.getNeoID(), isSourceSynonym, isTargetSynonym, false);
                sym.addMatch(match);
            }
        }
        if (sym.getMatches().size() > 0) 
            return sym;

        return null;
        
    }
}

class MappingAgent implements Callable<List<SymLink>> {
    private Thread t;
    private String threadName;
    private JSONObject blacklist;
    private List<OntologyTerm> source;
    private List<OntologyTerm> target;
    private List<SymLink> results;
    
    MappingAgent(String name, List<OntologyTerm> source, List<OntologyTerm> target, JSONObject blacklist) {
        this.threadName = name;
        this.source = source;
        this.target = target;
        this.blacklist = blacklist;
        System.out.println("Creating thread " + name);
    }
    
    @Override
    public List<SymLink> call() {
        System.out.println("Running thread " + this.threadName);
        this.results = Mapper.mapOntologies(this.source, this.target, this.blacklist);
        System.out.println("Thread " + threadName + " completed.");
        return this.results;
    }
}
