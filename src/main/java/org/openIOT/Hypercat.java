package org.openIOT;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/* The  Hypercat object.  
 * 
 * still need to finalize rules for traversing the graph of hypercats, and how much needs to be in-memory at any given time
 * 
 * /*
 *  (Spec. para 4.3) defining a HyperCat:
 *  A "Catalogue” object is a JSON object, which MUST contain all of the following properties:
 * 
 *      “items”  - a list of the items contained by the catalogue - value of this must be JSON array of zero or more JSON objects
 *      “item-metadata”  - an array of metadata objects (Relations) describing the catalogue.   value of this must be JSON array of metadata objects
 *  
 *  (note that it is NOT mandatory for a catalogue to bear a unique href or id, although it MAY do so as a metadata relation.)
 *      
 *  The metadata array for Catalogues MAY contain multiple metadata objects with the same rel (and val) properties
 *  
 *  The metadata array for Catalogues MUST contain a metadata object for each of the mandatory metadata object relationships. 
 *  for the Catalogue object, these are:
 *  
 *  rel: “urn:X-tsbiot:rels:hasDescription:en” val: [string with URN description of Catalogue]
 *  
 *  eg: 
 *        { "rel": "urn:X-tsbiot:rels:hasDescription:en",  "val": "test catalogue" }
 *  
 *  the constructor for a basic Hypercat enforces this by requiring a non-null String carrying the description

 */


public class Hypercat {
    

   @JsonProperty("item-metadata")
   private ArrayList<Relation> itemMetadata; 
   private  HashMap <String, Object>  items ;  
   //list containing contents, either items or Hypercats.  @todo Could tighten this by by subclassing Resource?  But this breaks Jackson's serialization
  
   
   private Logger log = LoggerFactory.getLogger(Hypercat.class);
   private ObjectMapper mapper = new ObjectMapper();
   
   
    //HyperCat constructors. 
     
   //Default (empty) constructor required by Jackson
   public Hypercat() {
   }
   
   //simple constructor, returning a minimum-spec Hypercat
    public Hypercat(String description) {
        super();   
        log.info("creating new hypercat");
        itemMetadata = new ArrayList<Relation>();
        items = new HashMap <String, Object>();
        Relation descriptionRel = new Relation ("urn:X-tsbiot:rels:hasDescription:en", description); 
        log.info(" in HC constructor - relation {} created with value {}",descriptionRel.rel,descriptionRel.val );      
        addRelation(descriptionRel);
    }
    
    
    //JSON-string constructor 
    public Hypercat(String jsonString, boolean isJSON) throws JsonParseException, JsonMappingException, IOException {

    itemMetadata = new ArrayList<Relation>();
    items = new HashMap<String, Object>();
   
    log.info("creating new hypercat from JSON string");

    // would use Jackson's JSONCreator functions here, except it appears to have a problem with HashMaps - it cannot reliably handle parsing into a Map 
    // So we have to do it manually, retrieve the maps as Arraylists and convert them into HashMaps
    
     JsonNode rootNode = mapper.readTree(jsonString);
     JsonNode hrefNode = rootNode.path("href");
     log.info("hrefnode value={}",hrefNode.getTextValue());
     
     JsonNode relationsNode = rootNode.path("item-metadata");
     
     Iterator<JsonNode> relations = relationsNode.getElements();
     while (relations.hasNext()) {
         ObjectNode relation = (ObjectNode) relations.next();
         log.info("fieldnameastext rel={} val={}",relation.findValuesAsText("rel").toArray()[0],relation.findValues("val").toArray()[0]);
         String rel = (String) relation.findValuesAsText("rel").toArray()[0];
         String val = (String) relation.findValuesAsText("val").toArray()[0];
         this.addRelation(new Relation(rel,val));
     }
     
     // now add items to the hypercat
     // all items in the Items collection are forced into Resource objects, even though they may actually be hypercats.
     // items in the items collection of a cat. only have an href and a collection of Relations. 
     //The child Items of an added hypercat are ignored here
     
     JsonNode itemsNode = rootNode.path("items");
     log.info("itemsnode="+itemsNode.toString());    
     Iterator<JsonNode> items = itemsNode.getElements();
     while (items.hasNext()) {

         Resource item = new Resource("1","2","3");
         Relation reln = null;

         ObjectNode itemNode = (ObjectNode) items.next();
         //log.info("item json={}",item.toString());
         
         //first get Resource href
         String itemHref = itemNode.findValue("href").toString();
         log.info("href for item={}",itemHref);      
    //     item.setHref(href);
         
         // then get list of metadata relations and add them to i-object-metadata    
         JsonNode iomdNode = itemNode.path("i-object-metadata");
         relations = iomdNode.getElements();
         //log.info("node-status="+iomdNode.isArray());
         //log.info("item array={}",relations.toString());
         while (relations.hasNext()) {
            ObjectNode relation = (ObjectNode ) relations.next();
            log.info("relobject = ={}",relation.getClass());
            
            log.info("fieldnameastext rel={} val={}",relation.findValuesAsText("rel").toArray()[0],relation.findValues("val").toArray()[0]);
            String rel = (String) relation.findValuesAsText("rel").toArray()[0];
            String val = (String) relation.findValuesAsText("val").toArray()[0];
            reln = new Relation(rel,val);
            log.info("in HCJsonCreator - relation being added = {} {}",reln.rel,reln.val);
            //item.addRelation(reln);
            
            item.getIObjectMetadata().add(reln);
            log.info("SIZE of item metadata JUST after adding reln with val contents {} is= {}",reln.val,item.getIObjectMetadata().size());
            
            log.info("item metadata JUST after adding is="+item.getIObjectMetadata().get(0));

         }
         
         // finally, add the item to the hypercat's items collection
         //try adding item manually to items
         
         log.info("adding item to items collection with value"+item.getIObjectMetadata().toString());
         this.items.put(itemHref, item);
         
         
        Resource itout =  (Resource) this.getItems().get(itemHref);
         
         log.info("itemobject is "+itout.getClass());
         log.info("itemobject has rels collecton "+itout.getIObjectMetadata().toString());
         
     }

    }
    
  
    
    //or via a textfile containing JSON 
    public Hypercat(File defFile) {
    
    }
 

    
  // hypercat functions  
    
    public void addRelation( Relation rel){
        log.info("relation being added"+ rel.rel);
        itemMetadata.add(rel);     
    }
      
    public void removeRelation(Relation rel){
        itemMetadata.remove(rel);      
    }
    
    public Relation findFirstRelation(String relLabel){       
        Relation rel  = new Relation();      
        Iterator it = this.itemMetadata.iterator();       
        while (it.hasNext()){
            rel =(Relation) it.next();
            if (rel.rel.equals(relLabel)) return rel;            
        }
        return null;
     }
      
    public ArrayList<Relation> findAllRelations(String relLabel){       
        ArrayList<Relation> relations  = new ArrayList<Relation>() ;
        Relation rel  = new Relation();      
        Iterator it = this.itemMetadata.iterator();       
        while (it.hasNext()){
            rel =(Relation) it.next();
            if (rel.rel.equals(relLabel)) relations.add(rel);            
        }
        return relations;
     }
    
    
    public String addItem(Object item) {     
        String href="";
        log.info("class="+item.getClass());
        
        if ((Resource.class).equals(item.getClass())) {          
            Resource res = (Resource) item;
            href=res.getHref();
            log.info("href from item is"+href);
        }
         
        if (href==null || "".equals(href)){
            href=this.generateHref();
        }
       // log.info("in single-arg AddItem - href="+href);
        
        return addItem(item,href); 
    }
        
        

    public String addItem(Object item, String href) {
 
        if (!items.containsKey(href) && href!=null &&!"".equals(href)){      
            items.put(href, item);
            //log.info("item  {} put into items list",href);
            return href;         
        }
        else {
           // log.info("item {} already exists!",href);
            return "itemExists";    
        }
    }
  
    
    public void removeItem(Object o){    
       //if (items.containsKey(getKey(o))){
           items.remove(o); 
       
    }
    
  
    public Hypercat searchCat (String querystring){
        
        Hypercat hc = new Hypercat("");
        
        return hc;      
        
    }


    public String generateHref() {
        
        return UUID.randomUUID().toString();     
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
    

    
    // getters and setters
    public ArrayList<Relation> getItemMetadata(){
        return this.itemMetadata; 
     }
     
     public HashMap getItems(){
         return this.items; 
      }
     
     public void setItems(HashMap<String, Object> items) {
         this.items = items;
     }
    

}

