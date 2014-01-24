package org.openIOT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
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
   //list containing contents, either items or Hypercats.  @todo Could tighten this by by subclassing Item?  But this breaks Jackson's serialization
  
   
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
     // all items in the Items collection are forced into Item objects, even though they may actually be hypercats.
     // items in the items collection of a cat. only have an href and a collection of Relations. 
     //The child Items of an added hypercat are ignored here
     
     JsonNode itemsNode = rootNode.path("items");
     log.info("itemsnode="+itemsNode.toString());    
     Iterator<JsonNode> items = itemsNode.getElements();
     while (items.hasNext()) {

         Item item = new Item();
         Relation reln = null;

         ObjectNode itemNode = (ObjectNode) items.next();
         //log.info("item json={}",item.toString());
         
         //first get Item href
         String itemHref = itemNode.findValue("href").toString();
         log.info("href for item={}",itemHref);      
         item.setHref(itemHref);
         
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
         
         
        Item itout =  (Item) this.getItems().get(itemHref);
         
         log.info("itemobject is "+itout.getClass());
         log.info("itemobject has rels collecton "+itout.getIObjectMetadata().toString());        
     }
    }
    

    // construct via a textfile containing JSON 
    public Hypercat(FileReader fr) throws JsonParseException, JsonMappingException, IOException {  
        this(getJsonString(fr),true);      
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
        
        if ((Item.class).equals(item.getClass())) {          
            Item res = (Item) item;
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

    
    public static HashMap<String, String> getQueryMap(String query)  
    {  
        String[] params = query.split("&");  
        HashMap<String, String> map = new HashMap<String, String>();  
        for (String param : params)  
        {  
            String name = param.split("=")[0];  
            String value = param.split("=")[1];  
            map.put(name, value);  
        }  
        return map;  
    } 
    
  
    public Hypercat searchCat (String querystring){
        Hypercat hc = new Hypercat("Search results for querystring: "+querystring);
        HashMap qmap = getQueryMap(querystring);
        String hrefQuery = (String) qmap.get("href");
        String relQuery = (String) qmap.get("rel");
        String valQuery = (String) qmap.get("val");
        HashMap <String, Item> relResults = new HashMap<String, Item>();
        HashMap <String, Item> valResults = new HashMap<String, Item>();
        boolean relQueryPresent = (!"".equals(relQuery) && relQuery!=null); 
        boolean valQueryPresent = (!"".equals(valQuery) && valQuery!=null); 
        log.info("this-itemslist-keyset="+this.getItems().entrySet().toString());

        Iterator it =  this.getItems().keySet().iterator(); 
        while (it.hasNext()){
            String key= (String) it.next();
            Item res = (Item)this.getItems().get(key);
            String hrefstr = res.getHref().replace("\"", "");
           
            
           //if the hypercat is a valid hypercat, this should only ever find zero or one items.  So if we find an item, immediately quit and return
            if (!"".equals(hrefQuery) && hrefQuery!=null){  
                //log.info("using key="+ key+" comparing query "+hrefQuery+ " to "+hrefstr);
                if (hrefQuery.equals(hrefstr)){
                    hc.addItem(res);
                    return(hc);              
                }   
            }
          
            /* this is a bit more complex, since the two queries may be additive.  However, there is no full logical search (no OR can be specified)
             * The most straightforward method is to construct a HashMap of results for each query, returning one or the other for single queries
             * and in the case of two non-null parameters, return the intersection-set
             * If an OR parameter is added to the spec in future, the union of the two HashMaps could be returned
             */
            
            Iterator relIt = res.getIObjectMetadata().iterator();

            
            while (relIt.hasNext()){
                
                    Relation rel = (Relation) relIt.next();
                    
                    if (relQueryPresent  && relQuery.equals(rel.getRel())){
                        if (!relResults.containsKey(res.getHref())) {
                             relResults.put(res.getHref(), res);
                        }
                    }
                      
                    if (valQueryPresent  && valQuery.equals(rel.getVal())){
                        if (!valResults.containsKey(res.getHref())) {
                             valResults.put(res.getHref(), res);
                        }
                    }
            }
        }
        
        
      if (relQueryPresent && !valQueryPresent) {
            hc.getItems().putAll(relResults);
      }
      if (valQueryPresent && !relQueryPresent) {
          hc.getItems().putAll(valResults);
      }
      if (valQueryPresent && relQueryPresent) {
          hc.getItems().putAll(valResults);
          hc.getItems().keySet().retainAll(relResults.keySet());
      }
     
      return hc;      
        
    }


    public String generateHref() {       
        return UUID.randomUUID().toString();     
    }


    String toJson() throws JsonGenerationException, JsonMappingException, IOException{
          String output = "NO JSON";
          ObjectMapper mapper = new ObjectMapper();                 
          output = mapper.writeValueAsString(this);
          return output;
    }
      
    String toPrettyJson() throws JsonGenerationException, JsonMappingException, IOException{
          String output = "NO JSON";
          ObjectMapper mapper = new ObjectMapper();                
          output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);       
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
     
     static String getJsonString(FileReader fr) throws IOException {
         String jsonString = "";
    
         BufferedReader reader = new BufferedReader(fr);
         String line = null;
         while ((line = reader.readLine()) != null) {
                 jsonString+=line;
         }
         return jsonString;
     }
    

}

