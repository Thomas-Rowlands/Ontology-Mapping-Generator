package jar;

import static jar.App.getOptionalWords;
import static jar.App.loadConfigFile;
import static org.junit.Assert.assertTrue;
import static jar.Mapper.*;

import org.junit.Test;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void termCombinationTest()
    {
        JSONObject config = loadConfigFile();
        JSONObject blacklist = (JSONObject) config.get("replacement_blacklist");
        String[] optionalList = getOptionalWords(config);
        Mapper.setBlacklist(blacklist);
        // Test terms that should NOT be included in mappings. (blacklist exclusions)
        assertTrue( !Mapper.compareSubStrings("testis", "testim") );
        assertTrue( !Mapper.compareSubStrings("metencephalon", "Mesencephalon") );
        assertTrue( Mapper.compareSubStrings("corneas", "cornea") );
        assertTrue( !Mapper.compareSubStrings("retina", "Retin-A") );
        assertTrue( !Mapper.compareSubStrings("retina", "Retinal") );
        assertTrue( !Mapper.compareSubStrings("soleus", "Coleus") );
        assertTrue( !Mapper.compareSubStrings("IPSCs", "IPSPs") );
        assertTrue( !Mapper.compareSubStrings("Timor", "tumor") );
    }
    
    @Test
    public void changeTextTest() {
//        System.out.println(Mapper.getSurroundingString("testis", 5));
        assertTrue(Mapper.getSurroundingString("testis", 5).equals("is$"));
//        System.out.println(Mapper.getSurroundingString("testis", 3));
        assertTrue(Mapper.getSurroundingString("testis", 3).equals("sti"));
//        System.out.println(Mapper.getSurroundingString("testis", 0));
        assertTrue(Mapper.getSurroundingString("testis", 0).equals("^te"));
    }
    
    @Test
    public void abbreviationTest() {
        assertTrue(Mapper.isAbbreviation("FCT"));
        assertTrue(Mapper.isAbbreviation("FCTs"));
        assertTrue(Mapper.isAbbreviation("FCTasTR"));
    }
}
