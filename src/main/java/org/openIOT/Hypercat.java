package org.openIOT;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the Hypercat object as dedfined in the 1.1 spec.
 * (below)
 * <p>
 * (Spec. para 4.3) defining a HyperCat:<p>
 *  A "Catalogue"object is a JSON object,
 * which MUST contain all of the following properties:
 * <p>
 * "items"- a list of the items contained by the catalogue - value of this must
 * be JSON array of zero or more JSON objects
 * <p>
 *  "item-metadata"- an array of
 * metadata objects (Relations) describing the catalogue. value of this must be
 * JSON array of metadata objects
 * <p>
 * (note that it is NOT mandatory for a catalogue to bear a unique href or id,
 * although it MAY do so as a metadata relation.)
 * <p>
 * The metadata array for Catalogues MAY contain multiple metadata objects with
 * the same rel (and val) properties
 * <p>
 * The metadata array for Catalogues MUST contain a metadata object for each of
 * the mandatory metadata object relationships. for the Catalogue object, these
 * are:
 * <p>
 * rel: "urn:X-tsbiot:rels:hasDescription:en"val: [string with URN description
 * of Catalogue]
 * <p>
 * eg: { "rel": "urn:X-tsbiot:rels:hasDescription:en", "val": "test catalogue" }
 * <p>
 * the constructors for Hypercat enforce this by requiring a metadata 
 * relation to be present for hasDescription, and for this to have a non-null
 * String as a value
 */
@JsonPropertyOrder({ "item-metadata", "items" })
public class Hypercat {

    @JsonProperty("item-metadata")
    private ArrayList<Relation> itemMetadata;
    private HashMap<String, Item> items;

    private Logger log = LoggerFactory.getLogger(Hypercat.class);
    private ObjectMapper mapper = new ObjectMapper();
    

    // HyperCat constructors.
    /**
     * Default (empty) constructor required by Jackson
     * 
     */
    public Hypercat() {
    }

    /**
     * simple constructor, returning a minimum-spec Hypercat. Requires a single
     * string parameter which should contain a text-description of the hypercat
     * 
     * @param description
     *            - a test string which should contain a text-description of the
     *            hypercat
     */
    public Hypercat(String description) {
        super();
        log.info("creating new hypercat");
        itemMetadata = new ArrayList<Relation>();
        items = new HashMap<String, Item>();
        Relation descriptionRel = new Relation("urn:X-tsbiot:rels:hasDescription:en", description);
        // log.info(" in HC constructor - relation {} created with value {}",descriptionRel.rel,descriptionRel.val
        // );
        addRelation(descriptionRel);
        String validated = validateHypercat(this);
        if (!"VALID".equals(validated)) {
            throw new InvalidHypercatException("hypercat is NOT VALID: +validated");
            };     
    }

    /**
     * JSON-string constructor - constructs a Hypercat object from a strng
     * containing a JSON representation of the required hypercat
     * 
     * @param jsonString
     *            - the string contaning the JSON definition
     * @param isJSON
     *            - a boolean required to distinguish this method-signature from
     *            the simple-constructor version. Not used
     */
    public Hypercat(String jsonString, boolean isJSON) throws JsonParseException, JsonMappingException, IOException {

        itemMetadata = new ArrayList<Relation>();
        items = new HashMap<String, Item>();

        log.info("creating new hypercat from JSON string");

        // would use Jackson's JSONCreator functions here, except it appears to
        // have a problem with HashMaps - it cannot reliably handle parsing into
        // a Map
        // So we have to do it manually, retrieve the maps as Arraylists and
        // convert them into HashMaps

        JsonNode rootNode = mapper.readTree(jsonString);
        //JsonNode hrefNode = rootNode.path("href");
        // log.info("hrefnode value={}",hrefNode.getTextValue());

        JsonNode relationsNode = rootNode.path("item-metadata");

        Iterator<JsonNode> relations = relationsNode.getElements();
        while (relations.hasNext()) {
            ObjectNode relation = (ObjectNode) relations.next();
            // log.info("fieldnameastext rel={} val={}",relation.findValuesAsText("rel").toArray()[0],relation.findValues("val").toArray()[0]);
            String rel = (String) relation.findValuesAsText("rel").toArray()[0];
            String val = (String) relation.findValuesAsText("val").toArray()[0];
            this.addRelation(new Relation(rel, val));
        }

        // now add items to the hypercat
        // all items in the Items collection are forced into Item objects, even
        // though they may actually be hypercats.
        // items in the items collection of a cat. only have an href and a
        // collection of Relations.
        // The child Items of an added hypercat are ignored here

        JsonNode itemsNode = rootNode.path("items");
        // log.info("itemsnode="+itemsNode.toString());
        Iterator<JsonNode> items = itemsNode.getElements();
        while (items.hasNext()) {

            Item item = new Item();
            Relation reln = null;

            ObjectNode itemNode = (ObjectNode) items.next();
            // log.info("item json={}",item.toString());

            // first get Item href
            String itemHref = itemNode.findValue("href").toString();
            // log.info("href for item={}",itemHref);
            item.setHref(itemHref);

            // then get list of metadata relations and add them to
            // i-object-metadata
            JsonNode iomdNode = itemNode.path("i-object-metadata");
            relations = iomdNode.getElements();
            // log.info("node-status="+iomdNode.isArray());
            // log.info("item array={}",relations.toString());
            while (relations.hasNext()) {
                ObjectNode relation = (ObjectNode) relations.next();
                // log.info("relobject = ={}",relation.getClass());

                // log.info("fieldnameastext rel={} val={}",relation.findValuesAsText("rel").toArray()[0],relation.findValues("val").toArray()[0]);
                String rel = (String) relation.findValuesAsText("rel").toArray()[0];
                String val = (String) relation.findValuesAsText("val").toArray()[0];
                reln = new Relation(rel, val);
                // log.info("in HCJsonCreator - relation being added = {} {}",reln.rel,reln.val);
                // item.addRelation(reln);

                item.getIObjectMetadata().add(reln);
                // log.info("SIZE of item metadata JUST after adding reln with val contents {} is= {}",reln.val,item.getIObjectMetadata().size());

                // log.info("item metadata JUST after adding is="+item.getIObjectMetadata().get(0));

            }

            // finally, add the item to the hypercat's items collection
            // try adding item manually to items

            // log.info("adding item to items collection with value"+item.getIObjectMetadata().toString());
            this.items.put(itemHref, item);

            // Item itout = (Item) this.getItems().get(itemHref);

            // log.info("itemobject is "+itout.getClass());
            // log.info("itemobject has rels collecton "+itout.getIObjectMetadata().toString());
            
        }
        
        String validated = validateHypercat(this);
        if (!"VALID".equals(validated)) {
            throw new InvalidHypercatException("hypercat is NOT VALID: +validated");
            };
        
    }
    


    /**
     * constructs an Hypercat object from a textfile containing a valid JSON
     * description
     * 
     * @param fr
     *            - a FileReader object pointing at the text-file containing the
     *            definition
     */
    public Hypercat(FileReader fr) throws JsonParseException, JsonMappingException, IOException {
        this(getJsonStringFromFileReader(fr), true);
        
    }

    // hypercat functions

    /**
     * adds a Relation object to the Hypercat's metadata-collection
     * ("item--metadata")
     * 
     * @param rel
     *            - the relation object to add - (MUST be of type
     *            org.openIOT.Relation)
     */
    public void addRelation(Relation rel) {
        // log.info("relation being added"+ rel.rel);
        itemMetadata.add(rel);
    }

    /**
     * removes a Relation object from the Hypercat's metadata-collection
     * ("item--metadata")
     * 
     * @param rel
     *            - the relation object to remove - (MUST be of type
     *            org.openIOT.Relation)
     */
    public void removeRelation(Relation rel) {
        itemMetadata.remove(rel);
    }

    /**
     * returns the first relation object in the metadata collection whose 'rel'
     * label exactly matches the supplied relLabel parameter
     * 
     * @param relLabel
     *            - the rel label to search for (eg
     *            "urn:X-tsbiot:rels:hasDescription:en")
     * 
     */
    public Relation findFirstRelation(String relLabel) {
        Relation rel = new Relation();
        Iterator it = this.itemMetadata.iterator();
        while (it.hasNext()) {
            rel = (Relation) it.next();
            if (rel.rel.equals(relLabel))
                return rel;
        }
        return null;
    }

    /**
     * returns an ArrayList containing all the relation objects in the metadata
     * collection whose 'rel' label exactly matches the supplied relLabel
     * parameter
     * 
     * @param relLabel
     *            - the rel label to search for (eg
     *            "urn:X-tsbiot:rels:hasDescription:en")
     * 
     */
    public ArrayList<Relation> findAllRelations(String relLabel) {
        ArrayList<Relation> relations = new ArrayList<Relation>();
        Relation rel = new Relation();
        Iterator it = this.itemMetadata.iterator();
        while (it.hasNext()) {
            rel = (Relation) it.next();
            log.info("comparing {} with {})",relLabel,rel.getRel()) ;
            if (rel.getRel().equals(relLabel))
                relations.add(rel);
        }
        return relations;
    }

    /**
     * adds an hypercat  to the Hypercat's items collection. A new Item is created, and
     * the metadata collection from the hypercat is copied to it.  
     * If an Href is not provided, then then one is automatically generated, and the
     * addItem(href, object) method below is called to add the item.  Returns the href
     * of the added item.
     * 
     * @param hc - the item to add
     * @param href - the href to uniquely identify the item
     */
    public String addItem(Hypercat hc, String href) {
      
        if (href == null || "".equals(href)) {
            href = generateHref();
        }       
        //flatten Hypercat into Item object for addition
        ArrayList rels = hc.getItemMetadata();
        Relation rel2 = (Relation) hc.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");  
        Item item = new Item("href",rel2.getVal(),"application/vnd.tsbiot.catalogue+json");
        item.setIObjectMetadata(rels);       
        return addItem(item, href);
    }

    /**
     * adds an Item object to the Hypercat's items collection. Returns the href
     * of the added item
     * 
     * @param item
     *            - the item to add
     * @param href
     *            - an href that uniquely identifies the item within the items
     *            collection. If the href supplied is not unique, the item is
     *            not added
     */
    public String addItem(Item item, String href) {

        if (!items.containsKey(href) && href != null && !"".equals(href)) {
            items.put(href, item);
            return href;
        }
        else {
            return "itemExists";
        }
    }

    /**
     * removes an item from the items collection
     * 
     * @param o
     *            - item to remove
     */
    public void removeItem(Object o) {
        // if (items.containsKey(getKey(o))){
        items.remove(o);
    }

    /**
     * convenience method returning a HashMap <string,string> of the parameter
     * contained in the query-string
     * 
     * @param query
     *            - the querystring
     */
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
    
    /**
     * Convenience method to ensure hypercat meets the minimum 1.1 specification.  
     * Returns the string "VALID" if successful, and "INVALID" followed by 
     * the reason for failure if unsuccessful
     * 
     * @param hc - the hypercat to be validated
     */
    public String validateHypercat(Hypercat hc){
        //hypercat validation-check - checks that Hypercat has a relation with non-null content-type
          
          Relation rel = (Relation) hc.findFirstRelation("urn:X-tsbiot:rels:hasDescription:en");
          if (rel==null) return "INVALID HyperCat - no relation of type rel=urn:X-tsbiot:rels:hasDescription:en is present in item-metadata";
          
          if (rel.getVal()==null || "".equals(rel.getVal())) {
              return  "INVALID HyperCat - relation of type rel=urn:X-tsbiot:rels:hasDescription:en is null or zero-length";        
          }
        return "VALID";         
      }

    /**
     * simple-search method. in response to an input query-string, ths method
     * returns an Hypercat containing those items that match the search-string
     * 
     * @param querystring
     *            - the query string
     */
    public Hypercat searchCat(String querystring) {
        Hypercat hc = new Hypercat("Search results for querystring: " + querystring);
        HashMap qmap = getQueryMap(querystring);
        String hrefQuery = (String) qmap.get("href");
        String relQuery = (String) qmap.get("rel");
        String valQuery = (String) qmap.get("val");
        HashMap<String, Item> relResults = new HashMap<String, Item>();
        HashMap<String, Item> valResults = new HashMap<String, Item>();
        boolean relQueryPresent = (!"".equals(relQuery) && relQuery != null);
        boolean valQueryPresent = (!"".equals(valQuery) && valQuery != null);
        // log.info("this-itemslist-keyset="+this.getItems().entrySet().toString());

        Iterator it = this.getItems().keySet().iterator();
        while (it.hasNext()) {
            String key = (String) it.next();
            Item res = (Item) this.getItems().get(key);
            String hrefstr = res.getHref().replace("\"", "");

            // if the hypercat is a valid hypercat, this should only ever find
            // zero or one items. So if we find an item, immediately quit and
            // return
            if (!"".equals(hrefQuery) && hrefQuery != null) {
                // log.info("using key="+ key+" comparing query "+hrefQuery+
                // " to "+hrefstr);
                if (hrefQuery.equals(hrefstr)) {
                    hc.addItem(res,res.getHref());
                    return (hc);
                }
            }

            /*
             * rel and val are a bit more complex, since the two queries may be
             * additive. However, there is no full logical search (no OR can be
             * specified) The most straightforward method is to construct a
             * HashMap of results for each, returning one or the other for
             * single parameters, and in the case of two parameters, returning
             * the intersection-set. If an OR parameter is added to the spec in
             * future, the union of the two HashMaps could be returned instead
             */

            Iterator relIt = res.getIObjectMetadata().iterator();

            while (relIt.hasNext()) {

                Relation rel = (Relation) relIt.next();

                if (relQueryPresent && relQuery.equals(rel.getRel())) {
                    if (!relResults.containsKey(res.getHref())) {
                        relResults.put(res.getHref(), res);
                    }
                }

                if (valQueryPresent && valQuery.equals(rel.getVal())) {
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

    /**
     * convenience method for generating a new unique href
     */
    public String generateHref() {
        return UUID.randomUUID().toString();
    }

    /**
     * convenience method returning a strng of JSON from a file containing it
     * 
     */
    static String getJsonStringFromFileReader(FileReader fr) throws IOException {
        String jsonString = "";

        BufferedReader reader = new BufferedReader(fr);
        String line = null;
        while ((line = reader.readLine()) != null) {
            jsonString += line;
        }
        return jsonString;
    }

    /**
     * outputs a string of JSON contaning the Hypcercat's definition
     * 
     */
    String toJson() throws JsonGenerationException, JsonMappingException, IOException {

        String output = "NO JSON";
        ObjectMapper mapper = new ObjectMapper();
        output = mapper.writeValueAsString(this);
        return output;
    }

    /**
     * outputs a prettily-formatted indented string of JSON contaning the
     * Hypcercat's definition
     */
    String toPrettyJson() throws JsonGenerationException, JsonMappingException, IOException {
        String output = "NO JSON";
        ObjectMapper mapper = new ObjectMapper();
        output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        return output;
    }

    // getters and setters
    @JsonGetter("item-metadata")
    public ArrayList<Relation> getItemMetadata() {
        return this.itemMetadata;
    }

    public HashMap getItems() {
        return this.items;
    }

    public void setItems(HashMap<String, Item> items) {
        this.items = items;
    }

}
