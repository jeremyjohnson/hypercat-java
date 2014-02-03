package org.openIOT;

import java.util.LinkedHashMap;
/* LICENCE INFORMATION for org.openIOT.Relation.java

* Copyright (c) 2014 Jeremy Johnson / AlertMe Ltd.
*  
* Enables easy creation of valid metadata for Hypercat catalogues
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
/**
 * The metadadta RElation object, representing a single metadata relation as
 * defined by the 1.1 spec from Spec 1.1: A metadata object (Relation) is a JSON
 * object which describes a single relationship between the parent object
 * (either the catalogue or catalogue item) and some other entity or concept
 * denoted by a URI.
 * 
 * All metadata objects MUST include all of the following properties:
 * 
 *"rel"-A relationship between the parent object and a target noun, expressed
 * as a predicate (verb). The value of this is the URI of the relationship as a
 * JSON string
 * 
 * 
 *"val"The entity (noun) to which the rel property applies. The value of this
 * is the URI of the concept or entity as a JSON string
 * 
 * example: item-metadata":{ "rel": "urn:X-tsbiot:rels:isContentType", "val": "
 * application/vnd.tsbiot.catalogue+json"}
 * 
 * This class represents a single metadata Relation
 * 
 * 
 * @author jdj
 * 
 */
public class Relation {

    public String rel; //eg"urn:X-tsbiot:rels:isContentType"- name of relation
    public String val; //eg "application/vnd.tsbiot.catalogue+json" MUST be a URI

    /**
     * default constructor required by Jackson
     * 
     */
    public Relation() {

    }

    /**
     * minimal Relation-object constructor. Two mandatory parameters:
     * 
     * @param rel
     *            - the REL label that indicates what the metadata relation is -
     *            eg "urn:X-tsbiot:rels:hasDescription:en"
     * @param val
     *            - the value of the metadata relation.
     *             eg "Example Hypercat with three items"
     */
    public Relation(String rel, String val) {
        this.rel = rel;
        this.val = val;
    }

    public String toString() {
        return "rel=" + this.rel + "val=" + this.val;

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
