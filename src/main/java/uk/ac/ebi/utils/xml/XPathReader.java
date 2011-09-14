package uk.ac.ebi.utils.xml;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by the ISA team
 * Modified from example here: http://www.javabeat.net/tips/182-how-to-query-xml-using-xpath.html
 * @author Eamonn Maguire (eamonnmag@gmail.com)
 */
public class XPathReader {

    private InputStream xmlFile;
    private Document xmlDocument;
    private XPath xPath;

    public XPathReader(InputStream xmlFile) {
        this.xmlFile = xmlFile;
        initObjects();
    }

    private void initObjects() {
        try {
            xmlDocument = DocumentBuilderFactory.
                    newInstance().newDocumentBuilder().
                    parse(xmlFile);
            xPath = XPathFactory.newInstance().
                    newXPath();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    public Object read(String expression,
                       QName returnType) {
        try {
            XPathExpression xPathExpression =
                    xPath.compile(expression);
            return xPathExpression.evaluate
                    (xmlDocument, returnType);
        } catch (XPathExpressionException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}