package org.openIOT;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceUnitTests {
 private Logger log = LoggerFactory.getLogger(ResourceUnitTests.class);
    


    
 @Test
 public void testBasicResource(){

     Resource i = new Resource("testResource","resource description","application/vnd.tsbiot.catalogue+json");
     log.info("created new item"+i.toString());
     Relation r1 = new Relation("rel1","val1");
     i.addRelation(r1);
     Relation r2 = new Relation ("rel2","val2");
     i.addRelation(r2);
     
     Assert.assertEquals("testResource", i.getHref());
     
     Relation rel = (Relation) i.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
     Assert.assertEquals("resource description", rel.val);
     
     rel = (Relation) i.findFirstRelation("urn:X-tsbiot:rels:isContentType");
     Assert.assertEquals("application/vnd.tsbiot.catalogue+json", rel.val);
     
     log.info("testitem toString="+i.toString());
     
     log.info("testitem PP = "+  prettyPrint(i) );

 }
 
 
 @Test
 public void testJsonFunctions() {
     Hypercat hc6 = null;
     //test construct-resource-from-JSON-string
     String jsonString = "{  \"href\": \"http://TESTRESOURCE\",  \"i-object-metadata\": [  { \"rel\": \"urn:X-tsbiot:rels:isContentType\",  \"val\": \"application/vnd.tsbiot.catalogue+json\"  },{ \"rel\": \"urn:X-tsbiot:rels:hasDescription:en\", \"val\": \"resource1\" }  ] }";
                      
     log.info("about to create Resource object by parsing : "+jsonString); 
     Resource res = null;
     try {
             res = new Resource(jsonString,true);
             } catch (JsonParseException e) {
                  e.printStackTrace();
             } catch (JsonMappingException e) {
                 e.printStackTrace();
             } catch (IOException e) {
                e.printStackTrace();
     }


     Assert.assertEquals("http://TESTRESOURCE", res.getHref());
     
     Relation rel = (Relation) res.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
     Assert.assertEquals("resource1", rel.val);
     
 }
 
 
 String prettyPrint(Object o){
     String output = "NO JSON";
     ObjectMapper mapper = new ObjectMapper();
     //mapper.setVisibility(JsonMethod.FIELD, Visibility.ANY);
    // ObjectWriter typedWriter = mapper.writerWithType(mapper.getTypeFactory().constructCollectionType(Resource.class);
    
     try {
         output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(o);
     } catch (JsonGenerationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     } catch (JsonMappingException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
     return output;
}
 
 
 
 

}
