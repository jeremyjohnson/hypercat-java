package org.openIOT;

import java.util.LinkedHashMap;
/** The metadadta RElation object, representing a single metadata relation as defined by the 1.1 spec
 * from Spec 1.1:
     * A metadata object (Relation) is a JSON object which describes a single relationship between the parent object 
     * (either the catalogue or catalogue item) and some other entity or concept denoted by a URI. 
     * 
     * All metadata objects MUST include all of the following properties:
     * 
     * “rel”  -A relationship between the parent object and a target noun, expressed as a predicate (verb).  The value of
     *         this is the URI of the relationship as a JSON string
     * 
     * 
     * “val”   The entity (noun) to which the rel property applies.  The value of this is the URI of the concept or entity as a 
     *         JSON string
     * 
     * example: item-metadata":{ "rel": "urn:X-tsbiot:rels:isContentType", "val": "application/vnd.tsbiot.catalogue+json"}
     *
     *This class represents a single metadata Relation
     *
 * 
 * @author jdj
 *
 */
public class Relation {
    
    /*
     
     */
    
    public String rel;  // eg “urn:X-tsbiot:rels:isContentType” - name of relation
    public String val;  // eg "application/vnd.tsbiot.catalogue+json"  - MUST be a URI

    //default constructor required by Jackson
    public Relation(){
    
    }
    
    public Relation( String rel, String val) {
        this.rel=rel;
        this.val=val;
    }
    
    public String toString() {
        return "rel="+this.rel+ "val="+this.val ;
        
    }

    public String getRel() {
        return rel;
    }

    public void setRel(String rel) {
        this.rel = rel;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }
}
