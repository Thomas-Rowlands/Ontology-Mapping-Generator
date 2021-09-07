package jar;

import java.util.ArrayList;
import java.util.List;


public class Ontology {
    

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public DB getConnection() {
        return this.connection;
    }

    public void setConnection(DB connection) {
        this.connection = connection;
    }
    
    public int termCount() {
        return this.terms.size();
    }
    
    public List<OntologyTerm> getTerms() {
        return this.terms;
    }
    
    private String name;
    private String prefix;
    private DB connection;
    private List<OntologyTerm> terms;

    public Ontology(String name, String prefix, DB connection) {
        this.setName(name);
        this.setPrefix(prefix);
        this.setConnection(connection);
        this.setOntologyTerms();
    }
    
    public void setOntologyTerms() {
        boolean isMP = this.prefix.equals("MP");
        this.terms = this.connection.getOntologyTerms(this.prefix, isMP);
    }
}

class OntologyTerm {
    

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getNeoID() {
        return this.neoID;
    }

    public void setNeoID(int neoID) {
        this.neoID = neoID;
    }

    public List<OntologyTerm> getSynonyms() {
        return this.synonyms;
    }

    public void setSynonyms(List<OntologyTerm> synonyms) {
        this.synonyms = synonyms;
    }

    public boolean isSynonym() {
        return this.isSynonym;
    }

    public void setIsSynonym(boolean isSynonym) {
        this.isSynonym = isSynonym;
    }
    
    public int synonymCount() {
        return this.synonyms.size();
    }
    
    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }
    
    public String getMappingName() {
        return this.mappingName;
    }
    
    public void setOntologyPrefix(String ontologyPrefix) {
        this.ontologyPrefix = ontologyPrefix;
    }
    
    public String getOntologyPrefix() {
        return this.ontologyPrefix;
    }

    private String name;
    private String mappingName;
    private String id;
    private int neoID;
    private List<OntologyTerm> synonyms;
    private boolean isSynonym;
    private String ontologyPrefix;

    public OntologyTerm(String name, String mappingName, String id, int neoID, List<OntologyTerm> synonyms, boolean isTermSynonym, String ontologyPrefix) {
        this.setName(name);
        this.setMappingName(mappingName);
        this.setId(id);
        this.setNeoID(neoID);
        this.setIsSynonym(isTermSynonym);
        this.setOntologyPrefix(ontologyPrefix);
        this.synonyms = new ArrayList<OntologyTerm>();
        if (synonyms != null)
            this.setSynonyms(synonyms);
    }
    
    public void addSynonym(OntologyTerm syn) {
        this.synonyms.add(syn);
    }

}