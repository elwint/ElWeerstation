package leertaak2;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class MyHandler extends DefaultHandler{
    
    boolean bSTN = false;
    boolean bDATE = false;
    boolean bTIME = false;
    boolean bTEMP = false;
    boolean bDEWP = false;
    boolean bSTP = false;
    boolean bSLP = false;
    boolean bVISIB = false;
    boolean bWDSP = false;
    boolean bPRCP = false;
    boolean bSNDP = false;
    
    @Override
    public void startDocument(){
        System.out.println("Start parsing...\n");
    }
    
    @Override
    public void endDocument() {
        System.out.println("Done parsing.\n");
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch(qName){
            case "MEASUREMENT":
                String station = attributes.getValue("MEASUREMENT");
                break;
            case "STN":
                bSTN = true;
                break;
            case "DATE":
                bDATE = true;
                break;
            case "TIME":
                bTIME = true;
                break;
            case "TEMP":
                bTEMP = true;
                break;
            case "DEWP":
                bDEWP = true;
                break;
            case "STP":
                bSTP = true;
                break;
            case "SLP":
                bSLP = true;
                break;
            case "VISIB":
                bVISIB = true;
                break;
            case "WDSP":
                bWDSP = true;
                break;
            case "PRCP":
                bPRCP = true;
                break;
            case "SNDP":
                bSNDP = true;
                break;
        }
    }
    
    @Override
    public void endElement(String namespaceURI, String localName, String qName){
        if(qName.equalsIgnoreCase("MEASUREMENT")){
            System.out.println("End Element: "+qName);
        }
    }
    
    @Override
    public void characters(char[] ch, int start, int length){
         if(bDATE){
            System.out.println("Date: "+ new String(ch, start, length));
            bDATE = false;
        }else if(bTIME){
            System.out.println("Time: "+ new String(ch, start, length));
            bTIME = false;
        }else if(bTEMP){
            System.out.println("Temp: "+ new String(ch, start, length));
            bTEMP = false;
        }else if(bDEWP){
            System.out.println("Dauwpunt: "+ new String(ch, start, length));
            bDEWP = false;
        }else if(bSTP){
            System.out.println("Luchtdruk stationsniveau: "+ new String(ch, start, length));
            bSTP = false;
        }else if(bSLP){
            System.out.println("Luchtdruk zeeniveau: "+ new String(ch, start, length));
            bSLP = false;
        }else if(bVISIB){
            System.out.println("Zichtbaarheid: "+ new String(ch, start, length));
            bVISIB = false;
        }else if(bWDSP){
            System.out.println("Windsnelheid: "+ new String(ch, start, length));
            bWDSP = false;
        }else if(bPRCP){
            System.out.println("Neerslag: "+ new String(ch, start, length));
            bPRCP = false;
        }else if(bSNDP){
            System.out.println("Sneeuw: "+ new String(ch, start, length));
            bSNDP = false;
        }
    }
}
