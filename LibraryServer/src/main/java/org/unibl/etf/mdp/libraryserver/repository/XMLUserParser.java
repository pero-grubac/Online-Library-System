package org.unibl.etf.mdp.libraryserver.repository;

import org.unibl.etf.mdp.model.User;
import org.unibl.etf.mdp.model.StatusEnum;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class XMLUserParser {

	public static List<User> parseUsersFromXML(String filePath) {
		List<User> users = new ArrayList<>();

		try {
			File xmlFile = new File(filePath);
			if (!xmlFile.exists() || xmlFile.length() == 0) {
				System.out.println("XML file is empty or does not exist.");
				return users;
			}

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(xmlFile);

			document.getDocumentElement().normalize();
			//System.out.println("Parsed Document:");
			//printNode(document.getDocumentElement(), "");

			NodeList nodeList = document.getElementsByTagName("object");
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;

					if ("org.unibl.etf.mdp.model.User".equals(element.getAttribute("class"))) {
						User user = parseUser(element);
						if (user != null) {
							users.add(user);
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		users.stream().forEach(System.out::println);
		return users;
	}

	private static User parseUser(Element element) {
		User user = new User();

		NodeList properties = element.getElementsByTagName("void");
		for (int j = 0; j < properties.getLength(); j++) {
			Element property = (Element) properties.item(j);
			String propertyName = property.getAttribute("property");

			switch (propertyName) {
			case "id":
				user.setId(parseInteger(getTextContent(property), "id"));
				break;
			case "firstName":
				user.setFirstName(getTextContent(property));
				break;
			case "lastName":
				user.setLastName(getTextContent(property));
				break;
			case "address":
				user.setAddress(getTextContent(property));
				break;
			case "email":
				user.setEmail(getTextContent(property));
				break;
			case "username":
				user.setUsername(getTextContent(property));
				break;
			case "password":
				user.setPassword(getTextContent(property));
				break;
			case "status":
				user.setStatus(parseStatus(getTextContent(property)));
				break;
			default:
				System.err.println("Unknown property: " + propertyName);
				break;
			}
		}

		return user;
	}

	private static String getTextContent(Element property) {
		if (property == null)
			return null;

		Node childNode = property.getFirstChild();
		if (childNode != null) {
			String content = childNode.getTextContent();
			return content != null ? content.trim() : null;
		}
		return null;
	}

	private static int parseInteger(String value, String propertyName) {
		if (value != null && !value.isBlank()) {
			try {
				return Integer.parseInt(value.trim());
			} catch (NumberFormatException e) {
				System.err.println("Invalid format for property '" + propertyName + "': " + value);
			}
		}
		return 0;
	}

	private static StatusEnum parseStatus(String value) {
		if (value != null && !value.isBlank()) {
			try {
				return StatusEnum.valueOf(value.trim().toUpperCase());
			} catch (IllegalArgumentException e) {
				System.err.println("Invalid status value: " + value);
			}
		}
		return null;
	}

	private static void printNode(Node node, String indent) {
		if (node.getNodeType() == Node.ELEMENT_NODE) {
			System.out.println(indent + "Node: " + node.getNodeName());

			String value = node.getTextContent().trim();
			if (!value.isEmpty() && node.getChildNodes().getLength() == 1) {
				System.out.println(indent + "  Value: " + value);
			}

			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				printNode(children.item(i), indent + "  ");
			}
		} else if (node.getNodeType() == Node.TEXT_NODE) {
			String text = node.getTextContent().trim();
			if (!text.isEmpty()) {
				System.out.println(indent + "Text: " + text);
			}
		}
	}
}
