package jar;

import java.util.ArrayList;
import java.util.List;

public class Mapping {
    
}

class Match {

    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getSourceID() {
        return this.sourceID;
    }

    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    public String getTarget() {
        return this.target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public int getTargetID() {
        return this.targetID;
    }

    public void setTargetID(int targetID) {
        this.targetID = targetID;
    }

    public boolean isSourceSynonym() {
        return this.isSourceSynonym;
    }

    public void setIsSourceSynonym(boolean isSourceSynonym) {
        this.isSourceSynonym = isSourceSynonym;
    }

    public boolean isTargetSynonym() {
        return this.isTargetSynonym;
    }

    public void setIsTargetSynonym(boolean isTargetSynonym) {
        this.isTargetSynonym = isTargetSynonym;
    }

    public boolean isExactMatch() {
        return this.isExactMatch;
    }

    public void setIsExactMatch(boolean isExactMatch) {
        this.isExactMatch = isExactMatch;
    }
    private String source;
    private int sourceID;
    private String target;
    private int targetID;
    private boolean isSourceSynonym;
    private boolean isTargetSynonym;
    private boolean isExactMatch;

    public Match(String source, int sourceID, String target, int targetID, boolean isSourceSynonym, boolean isTargetSynonym, boolean isExactMatch) {
        this.setSource(source);
        this.setSourceID(sourceID);
        this.setTarget(target);
        this.setTargetID(targetID);
        this.setIsSourceSynonym(isSourceSynonym);
        this.setIsTargetSynonym(isTargetSynonym);
        this.setIsExactMatch(isExactMatch);
    }
}

class SymLink {
    

    public OntologyTerm getSource() {
        return this.source;
    }

    public void setSource(OntologyTerm source) {
        this.source = source;
    }

    public OntologyTerm getTarget() {
        return this.target;
    }

    public void setTarget(OntologyTerm target) {
        this.target = target;
    }

    public List<Match> getMatches() {
        return this.matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }

    public void addMatch(Match match) {
        this.matches.add(match);
    }

    private OntologyTerm source;
    private OntologyTerm target;
    private List<Match> matches;

    public SymLink(OntologyTerm source, OntologyTerm target, List<Match> matches) {
        this.setSource(source);
        this.setTarget(target);
        this.matches = new ArrayList<>();
        if (matches != null)
            this.setMatches(matches);
    }
}