package com.example;

import com.salesforce.functions.jvm.sdk.Context;
import com.salesforce.functions.jvm.sdk.InvocationEvent;
import com.salesforce.functions.jvm.sdk.SalesforceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;
import org.apache.wss4j.dom.message.WSSecTimestamp;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.util.XMLUtils;

import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import org.w3c.dom.Document;

/**
 * Describe WssecurityjavaFunction here.
 */
public class WssecurityjavaFunction implements SalesforceFunction<FunctionInput, FunctionOutput> {
  private static final Logger LOGGER = LoggerFactory.getLogger(WssecurityjavaFunction.class);
  private final Crypto crypto;
  private DocumentBuilderFactory factory;

  public WssecurityjavaFunction() throws WSSecurityException {
    WSSConfig.init();

    this.crypto = CryptoFactory.getInstance(this.initializateCryptoProperties());

    this.factory = DocumentBuilderFactory.newInstance();
    this.factory.setNamespaceAware(true);
  }

  @Override
  public FunctionOutput apply(InvocationEvent<FunctionInput> event, Context context)
      throws Exception {

    String soapMessageToSign = event.getData().getXmlBody();
    String signedSoapMessage = this.signSoapMessage(soapMessageToSign);

    return new FunctionOutput(signedSoapMessage);
  }

  private String signSoapMessage(String soapMessage) throws Exception {
    Document doc = toSOAPPart(soapMessage);

    WSSecHeader wsSecHeader = new WSSecHeader(doc);
    wsSecHeader.setMustUnderstand(true);
    wsSecHeader.insertSecurityHeader();

    WSSecTimestamp wsSecTimestamp = new WSSecTimestamp(wsSecHeader);
    wsSecTimestamp.setTimeToLive(3600);
    wsSecTimestamp.build();

    WSSecSignature wsSecSignature = new WSSecSignature(wsSecHeader);
    wsSecSignature.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
    wsSecSignature.setUserInfo("mykey", "security");
    wsSecSignature.setDigestAlgo(WSConstants.SHA512);
    wsSecSignature.setSigCanonicalization(WSConstants.C14N_EXCL_OMIT_COMMENTS);

    Document signedDocument = wsSecSignature.build(crypto);
    String signedSoapMessage = XMLUtils.prettyDocumentToString(signedDocument);

    return signedSoapMessage;
  }

  private Properties initializateCryptoProperties() {
    Properties res = new Properties();
    res.setProperty("org.apache.ws.security.crypto.provider", "org.apache.ws.security.components.crypto.Merlin");
    res.setProperty("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
    res.setProperty("org.apache.ws.security.crypto.merlin.keystore.password", "security");
    res.setProperty("org.apache.ws.security.crypto.merlin.keystore.alias", "mykey");
    res.setProperty("org.apache.ws.security.crypto.merlin.alias.password", "security");
    res.setProperty("org.apache.ws.security.crypto.merlin.file", "identity.jks");
    return res;
  }

  /**
   * Convert an SOAP Envelope as a String to a org.w3c.dom.Document.
   */
  public org.w3c.dom.Document toSOAPPart(String xml) throws Exception {
    try (InputStream in = new ByteArrayInputStream(xml.getBytes())) {
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(in);
    }
  }

  public static String documentToString(Document doc) throws IOException, TransformerException {
    StringWriter sw = new StringWriter();
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

    transformer.transform(new DOMSource(doc), new StreamResult(sw));
    return sw.toString();
  }
}
