package com.omnichannel.platform.integration.rest;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

final class LegacyXmlWriter {

    private LegacyXmlWriter() {
    }

    static String toXml(JsonNode c) {
        try {
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = doc.createElement("customer");
            root.setAttribute("id", c.path("id").asText());
            doc.appendChild(root);

            Element name = child(doc, root, "name", null);
            child(doc, name, "first", c.path("name").path("first").asText());
            child(doc, name, "last", c.path("name").path("last").asText());

            Element contact = child(doc, root, "contact", null);
            child(doc, contact, "email", c.path("contact").path("email").asText());
            child(doc, contact, "mobile", c.path("contact").path("mobile").asText());

            child(doc, root, "segment", c.path("segment").asText());
            Element account = child(doc, root, "account", null);
            account.setAttribute("number", c.path("account").path("number").asText());
            account.setAttribute("balance", c.path("account").path("balance").decimalValue().toPlainString());
            child(doc, root, "loans", c.path("loans").asText("0"));
            child(doc, root, "card", c.path("card").asText());

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter out = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(out));
            return out.toString();
        } catch (ParserConfigurationException | TransformerException e) {
            throw new IllegalStateException("Cannot build legacy XML", e);
        }
    }

    private static Element child(Document doc, Element parent, String name, String text) {
        Element element = doc.createElement(name);
        if (text != null) {
            element.setTextContent(text);
        }
        parent.appendChild(element);
        return element;
    }
}
