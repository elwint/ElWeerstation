package leertaak2;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;

public class XMLParser {
    public static void main(String[] args) throws SAXException, IOException, ParserConfigurationException{
        File inputFile = new File("output.xml");
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        MyHandler myHandler = new MyHandler();
        saxParser.parse(inputFile, myHandler);
    }
}