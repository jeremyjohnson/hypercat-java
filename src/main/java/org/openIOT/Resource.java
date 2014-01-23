package org.openIOT;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 *  (Spec. para 4.3.1) 
 *  An “Resource” object is a JSON object, which MUST contain all of the following properties:
 * 
 *      “href”  - Identifier for the resource item - value of this must be a URI formatted as as a JSON string
 *      “i-object-metadata”  - an array of metadata objects (Relations)  describing the resource item.  
 *      
 *  The metadata array for Items MAY contain multiple metadata objects with the same rel (and val) properties
 *  
 *  The metadata array for Items MUST contain a metadata object for each of the mandatory metadata object relationships. 
 *  for the Resource object, these are:
 *  
 *  rel: “urn:X-tsbiot:rels:hasDescription:en” val: [string with URN description of Resource]
 *  rel: “urn:X-tsbiot:rels:isContentType”  val: [string with URN describing type of data provided by the Resource]
 *  
 *  
 *  eg: 
 *        { "rel": "urn:X-tsbiot:rels:hasDescription:en",  "val": "test item" }
 *        { "rel": "urn:X-tsbiot:rels:isContentType", "val": "application/vnd.tsbiot.catalogue+json" }
 *  
 *  the constructor for Resource enforces this by requiring a non-null String carrying the description, and a non-null string carrying the content-type 

 */

public class Resource {   

    
    @JsonProperty("i-object-metadata")
    private ArrayList<Relation> iObjectMetadata  = new ArrayList<Relation> (); 
    // alternative:  private HashMap <String, Relation> iObjectMetadata  = new HashMap <String, Relation>();
    private String href;  
     
     private Logger log = LoggerFactory.getLogger(Resource.class);
     private ObjectMapper mapper = new ObjectMapper();
     
     
    //Resource constructors
    //default constructor required by Jackson
    public Resource() { 
    }
    
    //constructor returning a minimum-spec Resource
    public Resource( String href, String description, String contentType ) {
        this.href = href;       
        Relation descriptionRel = new Relation ("urn:X-tsbiot:rels:hasDescription:en", description);
        this.iObjectMetadata.add(descriptionRel);
        Relation contentTypeRel = new Relation ("urn:X-tsbiot:rels:isContentType", contentType);
        this.iObjectMetadata.add(contentTypeRel);    
        
        log.info("Resource created with href={} ",this.href);
    }
    
    //JSON-string constructor
    public Resource (String jsonString, boolean isJSON) throws JsonParseException, JsonMappingException, IOException {

        iObjectMetadata = new ArrayList<Relation>();
        href= "";
        log.info("creating new Resource from JSON string");

        // would use Jackson's JSONCreator functions here, except it appears to have a problem with HashMaps - it cannot reliably handle parsing into a Map 
        // So we have to do it manually, retrieve the maps as Arraylists and convert them into HashMaps
        
         JsonNode rootNode = mapper.readTree(jsonString);
         JsonNode hrefNode = rootNode.path("href");
         log.info("hrefnode value={}",hrefNode.getTextValue());
         log.info("setting resource href to"+hrefNode.getTextValue());
         this.href=hrefNode.getTextValue();
         
         JsonNode relationsNode = rootNode.path("i-object-metadata");
         
         Iterator<JsonNode> relations = relationsNode.getElements();
         while (relations.hasNext()) {
             ObjectNode relation = (ObjectNode) relations.next();
             log.info("fieldnameastext rel={} val={}",relation.findValuesAsText("rel").toArray()[0],relation.findValues("val").toArray()[0]);
             String rel = (String) relation.findValuesAsText("rel").toArray()[0];
             String val = (String) relation.findValuesAsText("val").toArray()[0];
             this.addRelation(new Relation(rel,val));
         }
    }
    

    //or via a textfile containing JSON 
    public Resource(FileReader fr) throws JsonParseException, JsonMappingException, IOException {  
        this(new Hypercat().getJsonString(fr),true);      
    }
    
    
    
    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public ArrayList<Relation>  getIObjectMetadata() {
        return iObjectMetadata;
    }

    public void setIObjectMetadata(ArrayList<Relation>  iObjectMetadata) {
        this.iObjectMetadata = iObjectMetadata;
    }
    
    
    
    public void addRelation(Relation rel){
       // if (this.iObjectMetadata==null) this.iObjectMetadata = new ArrayList<Relation>();

        Logger log3 = LoggerFactory.getLogger(Resource.class);
        
        String size = "###";
 //       log3.info("relation being added in addRel="+rel.toString());

        size=new Integer(this.iObjectMetadata.size()).toString();
        log3.info("size before adding rel="+size);
        
        this.iObjectMetadata.add(rel);
        
        size=new Integer(this.iObjectMetadata.size()).toString();
 //       log3.info("size after adding rel="+size);
        
   //     log3.info("iomd after rel added ="+this.iObjectMetadata.toString());
    }
    
    public Relation findFirstRelation(String relLabel){       
        Relation rel  = new Relation();      
        Iterator it = this.iObjectMetadata.iterator();       
        while (it.hasNext()){
            rel =(Relation) it.next();
            if (rel.rel.equals(relLabel)) return rel;            
        }
        return null;
     }
      
    public ArrayList<Relation> findAllRelations(String relLabel){       
        ArrayList<Relation> relations  = new ArrayList<Relation>() ;
        Relation rel  = new Relation();      
        Iterator it = this.iObjectMetadata.iterator();       
        while (it.hasNext()){
            rel =(Relation) it.next();
            if (rel.rel.equals(relLabel)) relations.add(rel);            
        }
        return relations;
     }
          
    public String toString() {
        String output = "";
        output = "Href:"+this.href+ "  Relations: ";
        Iterator it = this.iObjectMetadata.iterator();
        while (it.hasNext()){          
            Relation rel = (Relation) it.next();
            output = output+ " | "+rel.getRel()+"="+rel.getVal();
        }
        
    return ("Resource:" +output);
    }

    String toJson(){
        String output = "NO JSON";
        ObjectMapper mapper = new ObjectMapper();         
        try {
            output = mapper.writeValueAsString(this);
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
        }
        return output;
  }
    
  String toPrettyJson(){
        String output = "NO JSON";
        ObjectMapper mapper = new ObjectMapper();         
        try {
            output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
            } catch (JsonGenerationException e) {
                e.printStackTrace();
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
        }
        return output;
  }
        

}
