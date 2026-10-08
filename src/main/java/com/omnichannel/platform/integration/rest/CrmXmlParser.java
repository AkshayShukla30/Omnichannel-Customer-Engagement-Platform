package com.omnichannel.platform.integration.rest;

import com.omnichannel.platform.integration.CustomerProfile;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigDecimal;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

final class CrmXmlParser {

    private CrmXmlParser() {
    }

    static CustomerProfile parse(String xml, String source) {
        try {
            Document doc = secureFactory().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            XPath xpath = XPathFactory.newInstance().newXPath();
            String first = xpath.evaluate("/customer/name/first", doc);
            String last = xpath.evaluate("/customer/name/last", doc);
            String balance = xpath.evaluate("/customer/account/@balance", doc);
            String loans = xpath.evaluate("/customer/loans", doc);
            return new CustomerProfile(
                    xpath.evaluate("/customer/@id", doc),
                    (first + " " + last).trim(),
                    xpath.evaluate("/customer/contact/email", doc),
                    xpath.evaluate("/customer/contact/mobile", doc),
                    xpath.evaluate("/customer/segment", doc),
                    xpath.evaluate("/customer/account/@number", doc),
                    balance.isBlank() ? BigDecimal.ZERO : new BigDecimal(balance.trim()),
                    loans.isBlank() ? 0 : Integer.parseInt(loans.trim()),
                    xpath.evaluate("/customer/card", doc),
                    source);
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException
                 | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid CRM XML", e);
        }
    }

    // DOCTYPE is rejected outright so a hostile CRM response cannot trigger XXE
    private static DocumentBuilderFactory secureFactory() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        return factory;
    }
}
