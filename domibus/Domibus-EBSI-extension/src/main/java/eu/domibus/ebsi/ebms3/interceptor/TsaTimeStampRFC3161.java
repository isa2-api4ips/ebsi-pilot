package eu.domibus.ebsi.ebms3.interceptor;


import eu.domibus.logging.DomibusLogger;
import eu.domibus.logging.DomibusLoggerFactory;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import static java.lang.System.getProperty;

/**
 * EBSI pilot example TSA example
 *
 * @author Jože Rihtaršič
 */
@Service
public class TsaTimeStampRFC3161 {
    private static final DomibusLogger LOG = DomibusLoggerFactory.getLogger(TsaTimeStampRFC3161.class);

    private static final String TIMESTAMP_REQUEST = "<?xml version='1.0' encoding='UTF-8'?><SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><SOAP-ENV:Body><tsa:service xmlns:tsa=\"urn:Entrust-TSA\"><ts:TimeStampRequest xmlns:ts=\"http://www.entrust.com/schemas/timestamp-protocol-20020207\"><ts:Digest><ds:DigestMethod xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" Algorithm=\"http://www.w3.org/2000/09/xmldsig#sha1\"/><ds:DigestValue xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\">%s</ds:DigestValue></ts:Digest><ts:Nonce>%s</ts:Nonce></ts:TimeStampRequest></tsa:service></SOAP-ENV:Body></SOAP-ENV:Envelope>";
    private static final String HTTPHeader_SAOPAction = "SOAPAction";
    private static final String HTTPHeader_ContentType = "Content-Type";
    private static final String HTTPHeader_ContentTypeValue = "text/xml;charset=UTF-8";


    public byte[] getTimestamp(byte[] value, URL timestampUrl) {
        LOG.info("start stamping for the url [{}]", timestampUrl);

        try {
            TimeStampRequestGenerator timeStampRequestGenerator = new TimeStampRequestGenerator();
            timeStampRequestGenerator.setCertReq(true);
            TimeStampRequest timeStampRequest = timeStampRequestGenerator.generate(TSPAlgorithms.SHA256, value);
            byte request[] = timeStampRequest.getEncoded();

            HttpURLConnection con = (HttpURLConnection) timestampUrl.openConnection();
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-type", "application/timestamp-query");
            con.setRequestProperty("Content-length", String.valueOf(request.length));
            OutputStream out = con.getOutputStream();
            out.write(request);
            out.flush();

            if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException("Received HTTP error: " + con.getResponseCode() + " - " + con.getResponseMessage());
            } else {
                LOG.error("Response Code: ".concat(Integer.toString(con.getResponseCode())));
            }
            InputStream in = con.getInputStream();
            TimeStampResp resp = TimeStampResp.getInstance(new ASN1InputStream(in).readObject());
            TimeStampResponse response = new TimeStampResponse(resp);
            response.validate(timeStampRequest);

            LOG.info("Status = " + response.getStatusString());

            if (response.getFailInfo() != null) {

                System.out.println("Status = " + response.getFailInfo().intValue());
                switch (response.getFailInfo().intValue()) {
                    case 0: {
                        LOG.error("unrecognized or unsupported Algorithm Identifier");
                        return null;
                    }

                    case 2: {
                        LOG.error("transaction not permitted or supported");
                        return null;
                    }

                    case 5: {
                        LOG.error("the data submitted has the wrong format");
                        return null;
                    }

                    case 14: {
                        LOG.error("the TSA’s time source is not available");
                        return null;
                    }

                    case 15: {
                        LOG.error("the requested TSA policy is not supported by the TSA");
                        return null;
                    }
                    case 16: {
                        LOG.error("the requested extension is not supported by the TSA");
                        return null;
                    }

                    case 17: {
                        LOG.error("the additional information requested could not be understood or is not available");
                        return null;
                    }

                    case 25: {
                        LOG.error("the request cannot be handled due to system failure");
                        return null;
                    }
                }
            }
            LOG.info("Timestamp: " + Base64.getEncoder().encodeToString(response.getEncoded()));
            LOG.info("TSA: " + response.getTimeStampToken().getTimeStampInfo().getTsa());
            LOG.info("Serial number:" + response.getTimeStampToken().getTimeStampInfo().getSerialNumber());
            LOG.info("Policy: " + response.getTimeStampToken().getTimeStampInfo().getPolicy());
            return response.getEncoded();
        } catch (Exception e) {
            LOG.error("Error occured while generating timestamp: ", e);
        }
        return null;
    }
    long getTime(){
        return Calendar.getInstance().getTimeInMillis();
    }

    public Element getXMLTimeStamp(String hash, String strTimeStampServerUrl) throws Exception {
        String reg = String.format(TIMESTAMP_REQUEST, hash, Calendar.getInstance().getTimeInMillis());
        Document d = callTimestampService(reg,strTimeStampServerUrl, null, null);
        setIdnessToElemetns(d.getDocumentElement());
        Element e = d.getElementById("TimeStampToken");;
        if (e == null) {
            e = (Element) d.getElementsByTagName("dsig:Signature").item(0);
        }
        return e;
    }

    private void setIdnessToElemetns(Node n) {
        if (n.getNodeType() == Node.ELEMENT_NODE) {
            Element e = (Element) n;
            if (e.hasAttribute("Id")) {
                e.setIdAttribute("Id", true);
            } else if (e.hasAttribute("id")) {
                e.setIdAttribute("id", true);
            }
            if (e.hasAttribute("ID")) {
                e.setIdAttribute("ID", true);
            }
            NodeList l = e.getChildNodes();
            for (int i = 0; i < l.getLength(); i++) {
                setIdnessToElemetns(l.item(i));
            }
        }
    }

    private Document callTimestampService(String ireq, String wsldLocatin, String soapActionNamespace, String soapAction) throws Exception {
        long t = getTime();
        long tCall, tReceive;
        Document respDoc = null;
        HttpURLConnection conn = null;
        try {
            String strLocation = wsldLocatin;
            int iVal = strLocation.indexOf("?");
            if (iVal > 0) {
                strLocation = strLocation.substring(0, iVal);
            }

            URL url = new URL(strLocation);
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);
            if (soapAction == null) {
                conn.setRequestProperty(HTTPHeader_SAOPAction, soapAction == null ? "" : (soapActionNamespace + soapAction));
            }
            conn.setRequestProperty(HTTPHeader_ContentType, HTTPHeader_ContentTypeValue);
            OutputStream os = conn.getOutputStream();

            // write post  ----------------------------------------
            os.write(ireq.getBytes("UTF-8"));

            os.flush();
            tCall = getTime() - t;
            LOG.info("Send request in " + tCall + "ms");

            // start receiving  ----------------------------------------
            tReceive = getTime() - tCall;
            LOG.info("Receive response in (" + tReceive + "ms)");

            InputStream httpIS = conn.getInputStream();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            respDoc = dbf.newDocumentBuilder().parse(httpIS);

        } catch (SAXException| ParserConfigurationException | IOException ex) {
            LOG.error( ex.getMessage(), ex);
        } finally {
            if (conn != null) {
                try {
                    conn.getInputStream().close();
                } catch (IOException ex) {

                }

            }
        }

        return respDoc;
    }


}
