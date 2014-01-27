package org.openIOT;

import java.io.BufferedReader;
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

/* The basic Hypercat object.  In the composite pattern, this is the only object that exists (ala the Lambda world), and is 'extended'
 * for leaf/resource objects by removing child objects.  Currently breaking this by subclassing Item  
 * 
 * still need to finalize rules for traversing the graph of hypercats, and how much needs to be in-memory at any given time
 * 
 */

public class HypercatUnitTests {

    private Logger log = LoggerFactory.getLogger(HypercatUnitTests.class);

    /*
     * tests: 0) creation of a basic minimum-spec hypercat
     * 
     * 1) creation of a hypercat populated with multiple Relations (some
     * identical) 2) creation of a hypercat populated with Item items (some of
     * which are Hypercats) 3) adding duplicate resource item to a hypercat
     * fails
     * 
     * 4) Create hypercat from JSON string 5) Creation of a hypercat from an
     * invalid JSON string fails
     * 
     * 6) Output a hypercat to a flat JSON string 7) Output a hypercat to a
     * pretty JSON string
     * 
     * 8) test href search on hc from searchHpercatExample.json file - should
     * return hypercat with one item 9) test rel search on hc from
     * searchHpercatExample.json file - should return a hypercat with two items
     * 10) test val search on hc from searchHpercatExample.json file - should
     * return a hypercat with three items 11) test rel&val search on hc from
     * searchHpercatExample.json file - should return a hypercat with one item
     */

    /**
     * Basic hypercat tests: 
     * 0) creation of a basic minimum-spec hypercat
     */
    @Test
    public void testBasicHypercat() {
        // test basic Hypercat creation
        Hypercat hc = new Hypercat("test-empty-catalogue");
        Relation returned = hc.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("urn:X-tsbiot:rels:hasDescription:en", returned.rel);
        Assert.assertEquals("test-empty-catalogue", returned.val);
    }

    /**
     * Populated hypercat tests 
     * 1) creation of a hypercat populated with multiple Relations (some identical) 
     * 2) creation of a hypercat populated with Item items (some of which are Hypercats) 
     * 3) adding duplicate  resource item to a hypercat fails
     * */
    @Test
    public void testPopulatedHypercat() {

        Hypercat hc = new Hypercat("test-populated-catalogue");
        Relation returned = hc.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("urn:X-tsbiot:rels:hasDescription:en", returned.rel);
        Assert.assertEquals("test-populated-catalogue", returned.val);

        // add another hypercat to the items collection of the firsts hypercat
        Hypercat hc2 = new Hypercat("child hypercat with manually-set href");
        // hc2.setHref("manual-UUID");
        hc.addItem(hc2, "manual-UUID");
        // test added item is on list, and can be retrieved from it
        Assert.assertEquals(1, hc.getItems().size());
        Item hc3 = (Item) hc.getItems().get("manual-UUID");
        log.info(" hc3 HC =" + hc3.toString());
        Relation rel2 = (Relation) hc3.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        log.info("rel metatdata from hc3=" + rel2.val);
        Assert.assertEquals("child hypercat with manually-set href", rel2.getVal());

        // add another hypercat to hc's items collection, but this time rely on
        // an autogenerated href
        Hypercat hc4 = new Hypercat("child hypercat with automatically-set href");
        String autoHref = hc.addItem(hc4,"");
        // test added item is on list, and can be retrieved from it
        Assert.assertEquals(2, hc.getItems().size());

        Item i5 = (Item) hc.getItems().get(autoHref);
        log.info(" hc5 HC =" + i5.toString());
        Relation rel3 = (Relation) i5.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        log.info("rel metatdata from i5=" + rel3.val);
        // Assert.assertEquals(autoHref, ( (Hypercat)
        // (hc.getItems().get(autoHref))).getHref() );
        Assert.assertEquals("child hypercat with automatically-set href", rel3.getVal());
        
        
        
        //test findAllRelations
        
        hc.addRelation(new Relation("urn:X-tsbiot:rels:1","A"));
        hc.addRelation(new Relation("urn:X-tsbiot:rels:1","B"));
        hc.addRelation(new Relation("urn:X-tsbiot:rels:1","C"));
        String rels = hc.findAllRelations("urn:X-tsbiot:rels:1").toString();
        Assert.assertEquals("[rel=urn:X-tsbiot:rels:1val=A, rel=urn:X-tsbiot:rels:1val=B, rel=urn:X-tsbiot:rels:1val=C]",rels);

        // test that adding the same hypercat again fails
        String localUID = hc.addItem(hc4, autoHref);
        Assert.assertEquals("itemExists", localUID);

        // add an Item to hc
        Item item = new Item("itemHref", "added Item", "text");
        hc.addItem(item,item.getHref());

        // and retrieve it
        Item retrievedItem = (Item) hc.getItems().get("itemHref");
        Assert.assertEquals("itemHref", retrievedItem.getHref());

        // test that adding the same item again fails
        localUID = hc.addItem(item,item.getHref());
        Assert.assertEquals("itemExists", localUID);
    }

    /**
     * hypercat-from-JSON tests 
     *  4) Create hypercat from JSON string
     *  5) Creation of a hypercat from an invalid JSON string fails
     */
    
    @Test
    public void testJsonFunctions() throws JsonGenerationException, JsonMappingException, IOException {
        Hypercat hc6 = null;
        // test construct-hypercat-from-JSON-string
        String jsonString = "{\"item-metadata\":[{\"rel\":\"urn:X-tsbiot:rels:isContentType\",\"val\":\"application/vnd.tsbiot.catalogue+json\"},{\"rel\":\"urn:X-tsbiot:rels:hasDescription:en\",\"val\":\"Test Description\"}],"
                + "\"items\":["
                + "{\"href\":\"http://FIXME\",\"i-object-metadata\":[{\"rel\":\"urn:X-tsbiot:rels:isContentType\",\"val\":\"application/vnd.tsbiot.catalogue+json\"},{\"rel\":\"urn:X-tsbiot:rels:hasDescription:en\",\"val\":\"resource1\"}]},"
                + "{\"href\":\"http://FIXME2\",\"i-object-metadata\":[{\"rel\":\"urn:X-tsbiot:rels:isContentType\",\"val\":\"application/vnd.tsbiot.catalogue+json\"},{\"rel\":\"urn:X-tsbiot:rels:hasDescription:en\",\"val\":\"resource2\"}]} "
                + "]}";

        log.info("about to create Hypercat object by parsing : " + jsonString);

        try {
            hc6 = new Hypercat(jsonString, true);
        } catch (JsonParseException e) {
            log.info("problem parsing JSON:" + e);
        } catch (JsonMappingException e) {
            log.info("problem maping JSON to hypercat object:" + e);
        } catch (IOException e) {
            log.info("I/O problem reading JSON" + e);
        }

        log.info(" itemmetadata = ", hc6.getItemMetadata().toString());
        // log.info(" in HC constructor - created hypercat from JSON with description{} created with value {}",hc6.getItemMetadata().get("urn:X-tsbiot:rels:hasDescription:en")
        // );

        Relation rel = (Relation) hc6.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("Test Description", rel.val);
        
        
        //test toJson function
        log.info("IMD="+hc6.getItemMetadata().toString());
        log.info("toJson="+hc6.toJson());
        log.info("jsonSt="+jsonString);
   //     Assert.assertEquals(jsonString,hc6.toJson());

        log.info("itemslist =" + hc6.getItems().toString());

        log.info("keyset=" + hc6.getItems().keySet().toString());

        String key = "\"http://FIXME2\"";
        log.info("items list has size= {}", hc6.getItems().size());

        Object itout = (Object) hc6.getItems().get(key);

        log.info("itemobject class for key={} is {} ", key, itout.getClass());

        // log.info("itout href={}",itout.getHref());
        


        Item hydratedItem = (Item) itout;
        String output = "";
        log.info("metadata aray=" + hydratedItem.getIObjectMetadata().toString());

        log.info("hydrated item metadata 1 is " + output);

        log.info("retrieved item = " + hydratedItem.toPrettyJson());

    }

    @Test
    public void testJSONfile() throws JsonParseException, JsonMappingException, IOException {
        // test construct-from-file
        FileReader fr = null;
        try {
            fr = new FileReader("src/test/resources/hypercatExample.json");
        } catch (FileNotFoundException e) {
            log.info("File not found!" + e);
        }

        Hypercat hc = new Hypercat(fr);
        log.info("created HC from file:" + hc.toPrettyJson());
        log.info("hc has item-metadata lt of size=" + hc.getItemMetadata().size());
        Relation rel = (Relation) hc.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        log.info("descrip rel from file=" + rel.toString());
        Assert.assertEquals("ingestiontestcat", rel.val);

        // test print as JSON()
        // test prettyprint as JSON()


    }

    /** 
     * Search tests:
     * * 8) test href search on hc from searchHpercatExample.json file - should
     *      return hypercat with one item
     *   9) test rel search on hc from searchHpercatExample.json file - should
     *      return a hypercat with two items 
     *  10) test val search on hc from searchHpercatExample.json file - should 
     *      return a hypercat with three items 
     *  11) test rel&val search on hc from searchHpercatExample.json file -
     *      should return a hypercat with one item
     */
    @Test
    public void testSearch() throws JsonParseException, JsonMappingException, IOException {
        // test search-catalogue-and-return-cat
        FileReader fr = null;
        try {
            fr = new FileReader("src/test/resources/searchHypercatExample.json");
        } catch (FileNotFoundException e) {
            log.info("File not found!" + e);
        }
        Hypercat hc = new Hypercat(fr);
        log.info("created HC from file:" + hc.toPrettyJson());
        log.info("hc has item-metadata lt of size=" + hc.getItemMetadata().size());
        Relation rel = (Relation) hc.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        log.info("descrip rel from file=" + rel.toString());                
        
        Assert.assertEquals("Search Test Catalogue", rel.val);

        // test href search - should return hypercat with one item
        Hypercat results = hc.searchCat("href=http://A");
        rel = (Relation) results.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("Search results for querystring: href=http://A", rel.val);
        Assert.assertEquals(1, results.getItems().size());
      log.info("results hc = " + results.toPrettyJson());

        // test rel search - should return a hypercat with two items
        results = hc.searchCat("rel=urn:X-tsbiot:rels:1");
        rel = (Relation) results.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("Search results for querystring: rel=urn:X-tsbiot:rels:1", rel.val);
        Assert.assertEquals(2, results.getItems().size());
        log.info("results hc = " + results.toPrettyJson());

        // test val search - should return a hypercat with three items
        results = hc.searchCat("val=3");
        rel = (Relation) results.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("Search results for querystring: val=3", rel.val);
        Assert.assertEquals(3, results.getItems().size());
        log.info("results hc = " + results.toPrettyJson());

        // test rel&val search - should return a hypercat with one item
        results = hc.searchCat("rel=urn:X-tsbiot:rels:isAnIntegerValue&val=3");
        rel = (Relation) results.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
        Assert.assertEquals("Search results for querystring: rel=urn:X-tsbiot:rels:isAnIntegerValue&val=3", rel.val);
        Assert.assertEquals(1, results.getItems().size());
        log.info("results hc = " + results.toPrettyJson());
        
        

    }

    String prettyPrint(Object o) throws JsonGenerationException, JsonMappingException, IOException {
        String output = "NO JSON";
        ObjectMapper mapper = new ObjectMapper();
        // mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
        // ObjectWriter typedWriter =
        // mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Item.class);
        output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);

        return output;
    }

}
