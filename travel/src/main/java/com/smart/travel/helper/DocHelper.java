package com.smart.travel.helper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Tool class make it convenient to operate XML file
 * 
 * @author yongchengx.fang
 * 
 */
public class DocHelper {

	public static Document getDocument(String path) throws Exception {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;

		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilder = docBuilderFactory.newDocumentBuilder();

		doc = docBuilder.parse(new File(path));

		return doc;
	}

	public static Document getDocument(InputStream in, boolean ignoreDtd)
			throws Exception {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;

		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilder = docBuilderFactory.newDocumentBuilder();

		docBuilder.setEntityResolver(new EntityResolver() {
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				return new InputSource(new StringReader(""));
			}
		});

		doc = docBuilder.parse(in);

		return doc;
	}

	public static Document getDocument(String xmlString, boolean ignoreDtd)
			throws Exception {
		return getDocument(new ByteArrayInputStream(xmlString.getBytes()),
				ignoreDtd);
	}

	public static Document newDocument(String rootName) throws Exception {
		DocumentBuilderFactory docBuilderFactory = null;
		DocumentBuilder docBuilder = null;
		Document doc = null;

		docBuilderFactory = DocumentBuilderFactory.newInstance();
		docBuilder = docBuilderFactory.newDocumentBuilder();

		doc = docBuilder.newDocument();
		Element root = doc.createElement(rootName);

		doc.appendChild(root);

		return doc;
	}

	public static void writeDocument(Document doc, String path)
			throws Exception {
		writeDocument(doc, path, null);
	}

	/**
	 * write document to file
	 * 
	 * @param doc
	 * @param path
	 * @param type
	 * @throws Exception
	 */
	public static void writeDocument(Document doc, String path, String type)
			throws Exception {
		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

		if (type != null) {
			transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, type);
		}

		transformer.transform(new DOMSource(doc), new StreamResult(
				new FileOutputStream(path)));
	}

	public static byte[] getDocumentBytes(Node node, String encoding)
			throws Exception {
		ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(1024);

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
		transformer.transform(new DOMSource(node), new StreamResult(
				byteArrayStream));

		return byteArrayStream.toByteArray();
	}

	public static String getDocumentString(Node node, String encoding)
			throws Exception {
		ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream(1024);

		Transformer transformer = TransformerFactory.newInstance()
				.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(
				"{http://xml.apache.org/xslt}indent-amount", "4");
		transformer.setOutputProperty(OutputKeys.ENCODING, encoding);
		transformer.transform(new DOMSource(node), new StreamResult(
				byteArrayStream));

		return byteArrayStream.toString(encoding);
	}

	public static Element getElementByTagName(Node parent, String tagName) {
		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (!(nodeList.item(i) instanceof Element)
					|| !tagName.equals(nodeList.item(i).getNodeName())) {
				continue;
			}

			return (Element) nodeList.item(i);
		}

		return null;
	}

	public static List<Element> getElementsByTagName(Node parent, String tagName) {
		List<Element> elList = new ArrayList<Element>();

		NodeList nodeList = parent.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			if (!(nodeList.item(i) instanceof Element)
					|| !tagName.equals(nodeList.item(i).getNodeName())) {
				continue;
			}

			elList.add((Element) nodeList.item(i));
		}

		return elList;
	}

}
