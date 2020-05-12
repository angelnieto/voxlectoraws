package es.ricardo.ws;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ExampleHandler extends DefaultHandler {
    // ===========================================================
    // Fields
    // ===========================================================
   
    private boolean in_map = false;
    private boolean in_esDeSegunda = false;
   
    private ParsedExampleDataSet myParsedExampleDataSet = new ParsedExampleDataSet();

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    public ParsedExampleDataSet getParsedData() {
            return this.myParsedExampleDataSet;
    }

    // ===========================================================
    // Methods
    // ===========================================================
    @Override
    public void startDocument() throws SAXException {
            this.myParsedExampleDataSet = new ParsedExampleDataSet();
    }

    @Override
    public void endDocument() throws SAXException {
            // Nothing to do
    }

    /** Gets be called on opening tags like:
     * <tag>
     * Can provide attribute(s), when xml was like:
     * <tag attribute="attributeValue">*/
    @Override
    public void startElement(String namespaceURI, String localName,
                    String qName, Attributes atts) throws SAXException {
    	
    	    //Descomentar para resetear la versión FREE
            //if (localName.equals("map")) {
    		if (qName.equals("map")) {
                    this.in_map = true;
            //}else if (localName.equals("esDeSegunda")) {
    		}else if (qName.equals("esDeSegunda")) {
                    this.in_esDeSegunda = true;
                    // Extract an Attribute
                    String attrValue = atts.getValue("value");
                    int i = Integer.parseInt(attrValue);
                    myParsedExampleDataSet.setExtractedInt(i);
            }
    }
   
    /** Gets be called on closing tags like:
     * </tag> */
    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
            if (localName.equals("map")) {
                    this.in_map = false;
            }else if (localName.equals("esDeSegunda")) {
                    this.in_esDeSegunda = false;
            }
    }

}
