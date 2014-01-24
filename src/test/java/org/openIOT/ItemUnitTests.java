package org.openIOT;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemUnitTests {
    private Logger log = LoggerFactory.getLogger(ItemUnitTests.class);

    /**
     * basic Item creation tests - item creation and adding of Relation objects
     */
    @Test
    public void testBasicItem() throws JsonGenerationException, JsonMappingException, IOException {

        Item i = new Item("testResource", "resource description", "application/vnd.tsbiot.catalogue+json");
        log.info("created new item" + i.toString());
        Relation r1 = new Relation("rel1", "val1");
        i.addRelation(r1);
        Relation r2 = new Relation("rel2", "val2");
        i.addRelation(r2);

        Assert.assertEquals("testResource", i.getHref());

        Relation rel = (Relation) i.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("resource description", rel.val);

        rel = (Relation) i.findFirstRelation("urn:X-tsbiot:rels:isContentType");
        Assert.assertEquals("application/vnd.tsbiot.catalogue+json", rel.val);

        log.info("testitem toString=" + i.toString());
        log.info("testitem PP = " + i.toPrettyJson());

    }

    /**
     * create-item-from-JSON-strng tests
     */
    @Test
    public void testJsonFunctions() throws JsonParseException, JsonMappingException, IOException {
        Hypercat hc6 = null;
        // test construct-resource-from-JSON-string
        String jsonString = "{  \"href\": \"http://TESTRESOURCE\",  \"i-object-metadata\": [  { \"rel\": \"urn:X-tsbiot:rels:isContentType\",  \"val\": \"application/vnd.tsbiot.catalogue+json\"  },{ \"rel\": \"urn:X-tsbiot:rels:hasDescription:en\", \"val\": \"resource1\" }  ] }";

        log.info("about to create Item object by parsing : " + jsonString);
        Item res = null;
        res = new Item(jsonString, true);
        Assert.assertEquals("http://TESTRESOURCE", res.getHref());

        Relation rel = (Relation) res.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("resource1", rel.val);

    }

    /**
     * create-item-from-file-of-JSON tests
     */
    @Test
    public void testJSONfile() throws JsonParseException, JsonMappingException, IOException {

        FileReader fr = null;
        try {
            fr = new FileReader("src/test/resources/itemExample.json");
        } catch (FileNotFoundException e) {
            log.info("File not found!" + e);
        }

        Item res = new Item(fr);
        log.info("created HC from file:" + res.toPrettyJson());
        log.info("hc has item-metadata lt of size=" + res.getIObjectMetadata().size());
        Relation rel = (Relation) res.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        log.info("descrip rel from file=" + rel.toString());
        Assert.assertEquals("A resource", rel.val);

    }

    String prettyPrint(Object o) throws JsonGenerationException, JsonMappingException, IOException {
        String output = "NO JSON";
        ObjectMapper mapper = new ObjectMapper();
        output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
        return output;
    }

}
