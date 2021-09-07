package jar;

import java.util.ArrayList;
import java.util.List;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;

public class DB {

    private Driver driver;

    /**
     * @param driver the driver to set
     */
    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    /**
     * @return the optionalList
     */
    public String[] getOptionalList() {
        return optionalList;
    }

    /**
     * @param optionalList the optionalList to set
     */
    public void setOptionalList(String[] optionalList) {
        this.optionalList = optionalList;
    }
    public Driver getDriver() {
        return this.driver;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getBoltPort() {
        return this.boltPort;
    }

    public void setBoltPort(int boltPort) {
        this.boltPort = boltPort;
    }
    private String user;
    private String password;
    private int boltPort;
    private String[] optionalList;


    public DB(String uri, String user, String password, int boltPort, String[] optionalList) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        this.setUser(user);
        this.setPassword(password);
        this.setBoltPort(boltPort);
        this.setOptionalList(optionalList);
    }

    public Result runQuery(final String query) {
        try ( Session session = this.getDriver().session()) {
            Result result = session.run(query);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<OntologyTerm> getOntologyTerms(String prefix, boolean isMP) {
        final String query = "MATCH (n:" + prefix + ")-[:HAS_SYNONYM*0..]->(s) "
                + "WHERE n.isObsolete = \"false\" AND s.isObsolete = \"false\" AND n.id IS NOT NULL "
                + "RETURN ID(n) AS neo_id, n.id AS id, n.FSN AS "
                + "name, collect([s.FSN, ID(s)]) AS synonyms;";
        try ( Session session = this.getDriver().session()) {
            Result result = session.run(query);
            List<OntologyTerm> termList = new ArrayList<>();
            while (result.hasNext()) {
                Record record = result.next();
                String mappingLabel = record.get("name").asString();
                if (isMP) {
                    String[] substrings = mappingLabel.split(" ");
                    mappingLabel = "";
                    for (int i = 0; i < this.optionalList.length; i++) {
                        if (substrings[0].equals(this.optionalList[i])) {
                            for (int l = 0; l < substrings.length; l++) {
                                if (l == 0) {
                                    continue;
                                }
                                mappingLabel += substrings[l] + " ";
                            }
                            mappingLabel = mappingLabel.stripTrailing();
                        }
                    }
                    if (mappingLabel.contains(" morphology"))
                        mappingLabel = mappingLabel.replace(" morphology", "");
                    if (mappingLabel.equals(""))
                        mappingLabel = record.get("name").asString();
                }

                OntologyTerm term = new OntologyTerm(record.get("name").asString(), mappingLabel, record.get("id").asString(), record.get("neo_id").asInt(), null, false, prefix);
                List syns = record.get("synonyms").asList();
                for (Object syn : syns) {
                    List temp = (List) syn;
                    String synMappingLabel = temp.get(0).toString();
                    if (isMP) {
                        String[] substrings = synMappingLabel.split(" ");
                        synMappingLabel = "";
                        if (substrings[0].equals("abnormal") || substrings[0].equals("increased") || substrings[0].equals("decreased")) {
                            for (int i = 0; i < substrings.length; i++) {
                                if (i == 0) {
                                    continue;
                                }
                                synMappingLabel += substrings[i] + " ";
                            }
                            synMappingLabel = synMappingLabel.stripTrailing();
                        } else {
                            synMappingLabel = temp.get(0).toString();
                        }
                    }
                    OntologyTerm newSyn = new OntologyTerm(temp.get(0).toString(), synMappingLabel, null, Math.toIntExact((long) temp.get(1)), null, true, prefix);
                    term.addSynonym(newSyn);
                }
                termList.add(term);
            }
            return termList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
