package com.ant.crawler.core.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import org.apache.commons.collections.DoubleOrderedMap;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

import com.ant.crawler.core.conf.Configuration;
import com.ant.crawler.core.conf.PrismConfiguration;
import com.gargoylesoftware.htmlunit.html.DomNode;

public class NodeUtils {
	private static final String HREF_PREFIX;
	private static final String EXCLUDE_REGEX_TAG = "(br)|(script)|(style)";
	private static final boolean IGNORE_STYPLE_SCRIPT;
	private static final String IMG_STYLE;
	private static final Set<String> whileList;
	private static final Set<String> blackList;
	private static final boolean fullStyleAll;
	private static final boolean ignoreStyleAll;
	private static final Set<String> simpleEndTags;
	static {
		Configuration conf = PrismConfiguration.getInstance();
		HREF_PREFIX = conf.get(PrismConstants.CONTENT_LINK_PREFIX);
		IGNORE_STYPLE_SCRIPT = conf.getBoolean(
				PrismConstants.CONTENT_IGNORE_HTML_STYLE_SCRIPT, true);
		IMG_STYLE = conf.get(PrismConstants.CONTENT_IMG_STYLE, "");
		whileList = new HashSet<String>(
				conf.getStringCollection(PrismConstants.CONTENT_IGNORE_HTML_STYLE_SCRIPT_WHITELIST));
		blackList = new HashSet<String>(
				conf.getStringCollection(PrismConstants.CONTENT_IGNORE_HTML_STYLE_SCRIPT_BLACKLIST));
		fullStyleAll = blackList.contains("*");
		ignoreStyleAll = !fullStyleAll && whileList.contains("*");

		simpleEndTags = new HashSet<String>();
		simpleEndTags.add("area");
		simpleEndTags.add("base");
		simpleEndTags.add("basefont");
		simpleEndTags.add("br");
		simpleEndTags.add("col");
		simpleEndTags.add("frame");
		simpleEndTags.add("hr");
		simpleEndTags.add("img");
		simpleEndTags.add("input");
		simpleEndTags.add("link");
		simpleEndTags.add("meta");
		simpleEndTags.add("param");
	}

	/**
	 * get children which are visible.
	 * 
	 * @param tree
	 * @return children of given tree.
	 */
	public static List<Node> getChildrenNode(Node tree) {
		NodeList children = tree.getChildNodes();
		if (children.getLength() == 0) {
			return null;
		}
		List<Node> nodes = new ArrayList<Node>();
		Node node = null;
		for (int i = 0; i < children.getLength(); ++i) {
			node = children.item(i);
			if (isVisible(node)) {
				nodes.add(node);
			}
		}
		return nodes.isEmpty() ? null : nodes;
	}

	/**
	 * this method is similarity with {@link getFullStringNode} but ignore text
	 * node.
	 * 
	 * @param tree
	 * @return
	 */
	public static List<String> getStringNameNode(Node tree) {
		List<String> nodeString = new ArrayList<String>();
		if (tree != null) {
			Stack<Node> stackNode = new Stack<Node>();
			stackNode.add(tree);
			Node node = null;
			NodeList nodeChildren = null;
			while (!stackNode.isEmpty()) {
				node = stackNode.pop();
				if (node.getNodeType() != Node.TEXT_NODE) {
					nodeString.add(node.getNodeName());
					nodeChildren = node.getChildNodes();
					for (int i = 0; i < nodeChildren.getLength(); ++i) {
						stackNode.add(nodeChildren.item(i));
					}
				}
			}
		}
		return nodeString;
	}

	/**
	 * get full string node . This method ignore empty node.
	 * 
	 * @param tree
	 * @return
	 */
	public static List<String> getFullStringNode(Node tree) {
		List<String> nodeString = new ArrayList<String>();
		if (tree != null) {
			Stack<Node> stackNode = new Stack<Node>();
			stackNode.add(tree);
			Node node = null;
			NodeList nodeChildren = null;
			while (!stackNode.isEmpty()) {
				node = stackNode.pop();
				if (node.getNodeType() != Node.TEXT_NODE) {
					nodeString.add(node.getNodeName());
					nodeChildren = node.getChildNodes();
					for (int i = 0; i < nodeChildren.getLength(); ++i) {
						stackNode.add(nodeChildren.item(i));
					}
				} else if (!isTextNodeEmpty(node)) {
					nodeString.add(node.getNodeName());
				}
			}
		}
		return nodeString;
	}

	/**
	 * check whether a given text node is empty
	 * 
	 * @param textNode
	 * @return
	 */
	public static boolean isTextNodeEmpty(Node textNode) {
		String textContent = textNode.getTextContent().replaceAll(
				"[\\p{Z}\\p{S}\\p{C}]", "");
		return textContent.isEmpty();
	}

	/**
	 * check whether a given node has value.
	 * 
	 * @param node
	 * @return
	 */
	public static boolean hasValue(Node node) {
		if (node.getNodeType() == Node.TEXT_NODE) {
			return !isTextNodeEmpty(node);
		}
		return true;
	}

	/**
	 * check whether a given node is visible
	 * 
	 * @param node
	 * @return
	 */
	public static boolean isVisible(Node node) {
		if (node.getNodeType() == Node.TEXT_NODE) {
			return !isTextNodeEmpty(node);
		} else if (node.getNodeType() == Node.COMMENT_NODE) {
			return false;
		}
		return !node.getNodeName().matches(EXCLUDE_REGEX_TAG);
	}

	/**
	 * Parsing a given html byte[] then output to System.out.
	 * 
	 * @param htmlBytes
	 * @param htmlEncoding
	 */

//	public static String getSimpleOuterHtml(HTMLElementImpl node) {
//		StringBuilder buffer = new StringBuilder();
//		appendSimpleOuterHtml(node, buffer);
//		return buffer.toString();
//	}

	public static String getURLOnSite(URL u) {
		if (HREF_PREFIX == null) {
			return "";
		}
		int len = HREF_PREFIX.length();
		if (u.getAuthority() != null && u.getAuthority().length() > 0)
			len += 2 + u.getAuthority().length();
		if (u.getPath() != null) {
			len += u.getPath().length();
		}
		if (u.getQuery() != null) {
			len += 1 + u.getQuery().length();
		}
		if (u.getRef() != null)
			len += 1 + u.getRef().length();

		StringBuffer result = new StringBuffer(len);
		result.append(HREF_PREFIX);
		if (u.getAuthority() != null && u.getAuthority().length() > 0) {
			result.append(u.getAuthority());
		}
		if (u.getPath() != null) {
			result.append(u.getPath());
		}
		if (u.getQuery() != null) {
			result.append('?');
			result.append(u.getQuery());
		}
		if (u.getRef() != null) {
			result.append("#");
			result.append(u.getRef());
		}
		return result.toString();
	}
	
	public static URL refineURL(URL url) throws URISyntaxException, MalformedURLException, UnsupportedEncodingException {
		url = new URL(URLDecoder.decode(url.toExternalForm(), "UTF-8"));
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		return new URL(uri.toASCIIString());
	}
	
	public static URL refineURL(String urlString) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		URL url = new URL(URLDecoder.decode(urlString, "UTF-8"));
		URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(), url.getQuery(), url.getRef());
		return new URL(uri.toASCIIString());
	}

//	private static void appendSimpleOuterHtml(HTMLElementImpl node,
//			StringBuilder buffer) {
//		String tagName = node.getNodeName().toLowerCase();
//		buffer.append('<');
//		buffer.append(tagName);
//		if (tagName.equals("img")) {
//			String srcAtt = node.getAttribute("src");
//			if (srcAtt != null) {
//				buffer.append(" src=\"");
//				buffer.append(Strings.strictHtmlEncode(srcAtt, true));
//				buffer.append("\" ");
//				buffer.append(IMG_STYLE);
//			}
//		} else if (tagName.equals("a")) {
//			String hrefAtt = node.getAttribute("href");
//			if (hrefAtt != null) {
//				buffer.append("  target=\"_blank\" href=\"");
//				buffer.append(Strings.strictHtmlEncode(hrefAtt, true));
//				buffer.append("\"");
//			}
//		} else if (fullStyleAll || blackList.contains(tagName)
//				|| (!ignoreStyleAll && !whileList.contains(tagName))) {
//			NamedNodeMap attributes = node.getAttributes();
//			Node attr = null;
//			for (int i = 0, n = attributes.getLength(); i < n; ++i) {
//				attr = attributes.item(i);
//				String value = attr.getNodeValue();
//				if (value != null) {
//					buffer.append(' ');
//					buffer.append(attr.getNodeName());
//					buffer.append("=\"");
//					buffer.append(Strings.strictHtmlEncode(value, true));
//					buffer.append("\"");
//				}
//			}
//		}
//		if (!node.hasChildNodes()) {
//			if (simpleEndTags.contains(tagName)) {
//				buffer.append("/>");
//			} else {
//				buffer.append("></");
//				buffer.append(tagName);
//				buffer.append('>');
//			}
//			return;
//		}
//		buffer.append('>');
//		appendInnerHTMLImpl(node, buffer);
//		buffer.append("</");
//		buffer.append(tagName);
//		buffer.append('>');
//	}
//
//	private static void appendInnerHTMLImpl(HTMLElementImpl node,
//			StringBuilder buffer) {
//		NodeImpl[] nl = node.getChildrenArray();
//		int size;
//		if (nl != null && (size = nl.length) > 0) {
//			for (int i = 0; i < size; i++) {
//				Node child = (Node) nl[i];
//				if (child instanceof HTMLElementImpl) {
//					appendSimpleOuterHtml((HTMLElementImpl) child, buffer);
//				} else if (child instanceof Text) {
//					String text = ((Text) child).getTextContent();
//					String encText = htmlEncodeChildText(child.getNodeName(),
//							text);
//					buffer.append(encText);
//				} else if (child instanceof ProcessingInstruction) {
//					buffer.append(child.toString());
//				}
//			}
//		}
//	}
//
//	private static String htmlEncodeChildText(String nodeName, String text) {
//		if (org.lobobrowser.html.parser.HtmlParser.isDecodeEntities(nodeName)) {
//			return Strings.strictHtmlEncode(text, false);
//		} else {
//			return text;
//		}
//	}
//
	/**
	 * get content of node (include html mark if node is html tag)
	 * 
	 * @param node
	 * @return
	 */
	public static String getContent(DomNode node) {
		int type = node.getNodeType();
		if (type == Node.TEXT_NODE || type == Node.ATTRIBUTE_NODE) {
			return node.getNodeValue();
		} else if (type == Node.ELEMENT_NODE) {
//			return IGNORE_STYPLE_SCRIPT ? getSimpleOuterHtml((HTMLElementImpl) node)
//					: ((HTMLElementImpl) node).getOuterHTML();
			String content = node.asXml();
			return content;
		}
		return null;
	}

	public static String getTextContent(Node node) {
		int type = node.getNodeType();
		if (type == Node.TEXT_NODE || type == Node.ATTRIBUTE_NODE) {
			return node.getNodeValue();
		} else if (type == Node.ELEMENT_NODE) {
//			return ((HTMLElementImpl) node).getInnerText();
			String content = node.getTextContent();
			return content;
		}
		return null;
	}
	
	public static void styleDescImage(Node imgNode) {
		Node sibling = getNextSibling(imgNode);
		Node text = getFirstTextNode(sibling);
		while (text == null) {
			sibling = getNextSibling(sibling);
			text = getFirstTextNode(sibling);
		}
	//	System.out.println(((TextImpl)text));
		sibling = getPreviousSibling(imgNode);
		text = getLastTextNode(sibling);
		while (text == null) {
			sibling = getPreviousSibling(sibling);
			text = getLastTextNode(sibling);
		}
	//	System.out.println(((TextImpl)text));
	}
	
	public static Node getNextSibling(Node node) {
		Node sibling = node.getNextSibling();
		while (sibling == null) {
			node = node.getParentNode();
			sibling = node.getNextSibling();
		}
		return sibling;
	}
	
	public static Node getPreviousSibling(Node node) {
		Node sibling = node.getPreviousSibling();
		while (sibling == null) {
			node = node.getParentNode();
			sibling = node.getPreviousSibling();
		}
		return sibling;
	}
	
	public static Node getFirstTextNode(Node node) {
		if (node == null) {
			return null;
		}
		if (node.getNodeType() != Node.TEXT_NODE) {
			NodeList childs = node.getChildNodes();
			Node result = null;
			for (int i = 0; i < childs.getLength(); ++i) {
				result = getFirstTextNode(childs.item(i));
				if (result != null) {
					break;
				}
			}
			return result;
		} else if (isTextNodeEmpty(node)) {
			return null;
		}
		return node;
	}
	
	public static Node getLastTextNode(Node node) {
		if (node == null) {
			return null;
		}
		if (node.getNodeType() != Node.TEXT_NODE) {
			NodeList childs = node.getChildNodes();
			Node result = null;
			for (int i = childs.getLength() - 1; i >= 0; --i) {
				result = getLastTextNode(childs.item(i));
				if (result != null) {
					break;
				}
			}
			return result;
		} else if (isTextNodeEmpty(node)) {
			return null;
		}
		return node;
	}
}
