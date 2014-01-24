package org.openIOT;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonGetter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class represents the Item object in the Hypercat 1.1 spec. See below for
 * definition.
 * 
 * (Spec. para 4.3.1) An “Item” object is a JSON object, which MUST contain all
 * of the following properties:
 * 
 * “href” - Identifier for the resource item - value of this must be a URI
 * formatted as as a JSON string “i-object-metadata” - an array of metadata
 * objects (Relations) describing the resource item.
 * 
 * The metadata array for Items MAY contain multiple metadata objects with the
 * same rel (and val) properties
 * 
 * The metadata array for Items MUST contain a metadata object for each of the
 * mandatory metadata object relationships. for the Item object, these are:
 * 
 * rel: “urn:X-tsbiot:rels:hasDescription:en” val: [string with URN description
 * of Item] rel: “urn:X-tsbiot:rels:isContentType” val: [string with URN
 * describing type of data provided by the Item]
 * 
 * 
 * eg: { "rel": "urn:X-tsbiot:rels:hasDescription:en", "val": "test item" } {
 * "rel": "urn:X-tsbiot:rels:isContentType", "val":
 * "application/vnd.tsbiot.catalogue+json" }
 * 
 * the basic constructor for Item enforces this by requiring a non-null String
 * carrying the description, and a non-null string carrying the content-type
 **/

public class Item {

    @JsonProperty("i-object-metadata")
    private ArrayList<Relation> iObjectMetadata = new ArrayList<Relation>();
    private String href;

    private Logger log = LoggerFactory.getLogger(Item.class);
    private ObjectMapper mapper = new ObjectMapper();

    // Item constructors

    /**
     * default constructor required by Jackson
     * */
    public Item() {
    }

    /**
     * constructor returning a minimum-spec Item
     * 
     * @param href
     *            - a URI that serves to uniquely identify an item within the
     *            items collection of a hypercat.
     * @param description
     *            - a text description of what the item represents
     * @param contentType
     *            - a text description of the content-type
     */
    public Item(String href, String description, String contentType) {
        this.href = href;
        Relation descriptionRel = new Relation("urn:X-tsbiot:rels:hasDescription:en", description);
        this.iObjectMetadata.add(descriptionRel);
        Relation contentTypeRel = new Relation("urn:X-tsbiot:rels:isContentType", contentType);
        this.iObjectMetadata.add(contentTypeRel);

        //log.info("Item created with href={} ", this.href);
    }

    /**
     * JSON-string constructor - constructs an item object from a valid JSON
     * string
     * 
     * @param jsonString
     *            - the string containing the Item defintion
     * @param isJSON
     *            - a boolean required to distinguish this method-signature -
     *            (currently not used to represent anything)
     * @throws JsonParseException
     *             - if the JSON cannot be parsed
     * @throws JsonMappingException
     *             - if the JSON nodes cannot be mapped to the java object
     * @throws IOException
     *             - if there is some problem reading the string
     */
    public Item(String jsonString, boolean isJSON) throws JsonParseException, JsonMappingException, IOException {

        iObjectMetadata = new ArrayList<Relation>();
        href = "";
        log.info("creating new Item from JSON string");

        /*
         * we would use Jackson's JSONCreator functions here, except it appears
         * to have a problem with HashMaps - it cannot reliably handle parsing
         * into a Map. So we have to do it manually
         */
        JsonNode rootNode = mapper.readTree(jsonString);
        JsonNode hrefNode = rootNode.path("href");
        // log.info("hrefnode value={}", hrefNode.getTextValue());
        // log.info("setting resource href to" + hrefNode.getTextValue());
        this.href = hrefNode.getTextValue();

        JsonNode relationsNode = rootNode.path("i-object-metadata");

        Iterator<JsonNode> relations = relationsNode.getElements();
        while (relations.hasNext()) {
            ObjectNode relation = (ObjectNode) relations.next();
            // log.info("fieldnameastext rel={} val={}",
            // relation.findValuesAsText("rel").toArray()[0], relation
            // .findValues("val").toArray()[0]);
            String rel = (String) relation.findValuesAsText("rel").toArray()[0];
            String val = (String) relation.findValuesAsText("val").toArray()[0];
            this.addRelation(new Relation(rel, val));
        }
    }

    /**
     * JSON file constructor - constructs an item object from a text-file
     * containing valid JSON
     * 
     * @param fr
     *            - a FileReader object pointing at the source file
     * @throws JsonParseException
     *             - if the JSON cannot be parsed
     * @throws JsonMappingException
     *             - if the JSON nodes cannot be mapped to the java object
     * @throws IOException
     *             - if there is some problem reading the string
     */
    public Item(FileReader fr) throws JsonParseException, JsonMappingException, IOException {
        this(new Hypercat().getJsonStringFromFileReader(fr), true);
    }

    /**
     * gets the Item, unique href.
     */
    public String getHref() {
        return href;
    }

    /**
     * Sets the item's Href. This MUST be unique within an items-collection
     * 
     * @param href
     *            - href to set (the specification suggests an URI)
     */
    public void setHref(String href) {
        this.href = href;
    }

    @JsonGetter("i-object-metadata")
    public ArrayList<Relation> getIObjectMetadata() {
        return iObjectMetadata;
    }

    /**
     * sets the item's collection of metadata relations
     * 
     */
    public void setIObjectMetadata(ArrayList<Relation> iObjectMetadata) {
        this.iObjectMetadata = iObjectMetadata;
    }

    /**
     * adds a Relation object definng a metadata-relation to the Item's
     * metadadta-collection (i-object-metadata)
     * 
     * @param rel
     *            - the Relation object to add - must be of class
     *            org.openIOT.Relation
     */

    public void addRelation(Relation rel) {
        // if (this.iObjectMetadata==null) this.iObjectMetadata = new
        // ArrayList<Relation>();

      //  Logger log3 = LoggerFactory.getLogger(Item.class);

        // String size = "###";
        // log3.info("relation being added in addRel="+rel.toString());

        // size=new Integer(this.iObjectMetadata.size()).toString();
        // log3.info("size before adding rel="+size);

        this.iObjectMetadata.add(rel);

        // size=new Integer(this.iObjectMetadata.size()).toString();
        // log3.info("size after adding rel="+size);

        // log3.info("iomd after rel added ="+this.iObjectMetadata.toString());
    }

    /**
     * returns the first relation object in teh metadata collection whose 'rel'
     * label exactly matches the supplied relLabel parameter
     * 
     * @param relLabel
     *            - the rel label to search for (eg
     *            "urn:X-tsbiot:rels:hasDescription:en")
     * 
     */
    public Relation findFirstRelation(String relLabel) {
        Relation rel = new Relation();
        Iterator it = this.iObjectMetadata.iterator();
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
        Iterator it = this.iObjectMetadata.iterator();
        while (it.hasNext()) {
            rel = (Relation) it.next();
            if (rel.rel.equals(relLabel))
                relations.add(rel);
        }
        return relations;
    }

    /**
     * returns a java string representation of the Item object
     */
    public String toString() {
        String output = "";
        output = "Href:" + this.href + "  Relations: ";
        Iterator it = this.iObjectMetadata.iterator();
        while (it.hasNext()) {
            Relation rel = (Relation) it.next();
            output = output + " | " + rel.getRel() + "=" + rel.getVal();
        }
        return ("Item:" + output);
    }

    /**
     * returns a flat (unformatted) JSON string defining the Item object
     * 
     */
    String toJson() throws JsonGenerationException, JsonMappingException, IOException {
        String output = "NO JSON";
        ObjectMapper mapper = new ObjectMapper();
        output = mapper.writeValueAsString(this);
        return output;
    }

    /**
     * returns a pretty (formatted) JSON string defining the Item object
     * 
     */
    String toPrettyJson() throws JsonGenerationException, JsonMappingException, IOException {
        String output = "NO JSON";
        ObjectMapper mapper = new ObjectMapper();
        output = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this);
        return output;
    }

}
