package org.openIOT;

/* LICENCE INFORMATION for org.openIOT.ItemUnitTests.java

* Copyright (c) 2014 Jeremy Johnson / AlertMe Ltd.
*  
* Unit tests for java Item class
* 
* Written to comply with IoT Ecosystems Demonstrator Interoperability Action Plan V1.0 24th June 2013
* As found at http://www.openiot.org/apis
* 
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
* 
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

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
        
        i.addRelation(new Relation("urn:X-tsbiot:rels:1","A"));
        i.addRelation(new Relation("urn:X-tsbiot:rels:1","B"));
        i.addRelation(new Relation("urn:X-tsbiot:rels:1","C"));
        String rels = i.findAllRelations("urn:X-tsbiot:rels:1").toString();
        Assert.assertEquals("[rel=urn:X-tsbiot:rels:1val=A, rel=urn:X-tsbiot:rels:1val=B, rel=urn:X-tsbiot:rels:1val=C]",rels);

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
