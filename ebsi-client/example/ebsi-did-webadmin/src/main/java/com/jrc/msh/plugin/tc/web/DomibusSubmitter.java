package com.jrc.msh.plugin.tc.web;


import org.primefaces.PrimeFaces;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

@SessionScoped
@Named("domibusSubmitter")
public class DomibusSubmitter implements Serializable {

    // hostname in EBSI plan.
    private static final String DOMIBUS_HOSTNAME_C2="tomcatc2";
    private static final String DOMIBUS_HOSTNAME_C3 ="wildflyc3";

    String domibusUrlC2;
    String domibusAdminUrlC2 = "https://gateway-edelivery.westeurope.cloudapp.azure.com:883/blue/domibus/";
    String requestC2;
    String responseC2;
    int tabIndexC2 = 0;


    String domibusUrlC3;
    String domibusAdminUrlC3 = "https://gateway-edelivery.westeurope.cloudapp.azure.com:883/red/domibus/";
    String requestC3;
    String responseC3;
    int tabIndexC3 = 0;

    public DomibusSubmitter() {
        resetRequestC2();
        resetRequestC3();
    }

    // resolve hostname to ip for accessing domibus via browser for linux environment!
    public String resolveDomain(String hostname) {
        InetAddress address = null;
        try {
            address = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
           return hostname;
        }
        return address.getHostAddress();
    }


    public String getDomibusAdminUrlC2() {
        return domibusAdminUrlC2;
    }

    public void setDomibusAdminUrlC2(String domibusAdminUrlC2) {
        this.domibusAdminUrlC2 = domibusAdminUrlC2;
    }

    public String getDomibusAdminUrlC3() {
        return domibusAdminUrlC3;
    }

    public void setDomibusAdminUrlC3(String domibusAdminUrlC3) {
        this.domibusAdminUrlC3 = domibusAdminUrlC3;
    }

    public String getRequestC2() {
        if (requestC2 == null) {
            requestC2 = readFile("/examples/c2-request.xml");
        }
        return requestC2;
    }

    public String getRequestC3() {
        if (requestC3 == null) {
            requestC3 = readFile("/examples/c3-request.xml");
        }
        return requestC3;
    }

    public void setRequestC2(String requestC2) {
        this.requestC2 = requestC2;
    }

    public void setRequestC3(String requestC3) {
        this.requestC3 = requestC3;
    }


    public void resetRequestC2() {
        //this.domibusAdminUrlC2 = "http://" + resolveDomain(DOMIBUS_HOSTNAME_C2) + ":8080/domibus/";
        this.domibusAdminUrlC2 = "https://gateway-edelivery.westeurope.cloudapp.azure.com:883/blue/domibus/";
        this.domibusUrlC2 =  domibusAdminUrlC2 + "services/backend";
        this.requestC2 = null;
        this.responseC2 = null;
    }

    public void resetRequestC3() {
       // this.domibusAdminUrlC3 = "http://" + resolveDomain(DOMIBUS_HOSTNAME_C3) + ":8080/domibus/";
        this.domibusAdminUrlC3 = "https://gateway-edelivery.westeurope.cloudapp.azure.com:883/red/domibus/";
        this.domibusUrlC3 =  domibusAdminUrlC3 + "services/backend";
        this.requestC3= null;
        this.responseC3 = null;
    }

    public void clearRequestC2() {
        this.requestC2 = "";
        this.responseC2 = "";
    }

    public void clearRequestC3() {
        this.requestC3 = "";
        this.responseC3 = "";
    }

    public String getResponseC2() {
        return responseC2;
    }

    public String getResponseC3() {
        return responseC3;
    }

    public void setResponseC2(String responseC2) {
        this.responseC2 = responseC2;
    }

    public void setResponseC3(String responseC3) {
        this.responseC3 = responseC3;
    }

    public int getTabIndexC2() {
        return tabIndexC2;
    }

    public int getTabIndexC3() {
        return tabIndexC3;
    }

    public void setTabIndexC2(int tabIndexC2) {
        this.tabIndexC2 = tabIndexC2;
    }

    public void setTabIndexC3(int tabIndexC3) {
        this.tabIndexC3 = tabIndexC3;
    }

    public String getDomibusUrlC2() {
        return domibusUrlC2;
    }

    public String getDomibusUrlC3() {
        return domibusUrlC3;
    }

    public void setDomibusUrlC2(String domibusUrlC2) {
        this.domibusUrlC2 = domibusUrlC2;
    }

    public void setDomibusUrlC3(String domibusUrlC3) {
        this.domibusUrlC3 = domibusUrlC3;
    }

    static String readFile(String path) {

        InputStream inputStream = EbsiWebPluginData.class.getResourceAsStream(path);


        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            int nRead;
            byte[] data = new byte[1024];
            while (true) {

                if (!((nRead = inputStream.read(data, 0, data.length)) != -1)) break;

                buffer.write(data, 0, nRead);
            }
            buffer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(buffer.toByteArray());

    }

    public void submitRequestC2() {
        String respose = null;
        try {
            respose = submitRequest(getDomibusUrlC2(), getRequestC2());
        } catch (IOException e) {
            respose = e.getMessage();
        }
        setResponseC2(respose);
        tabIndexC2 = 1;
        PrimeFaces.current().ajax().update("tabView:tcC2Sumbitform");
        PrimeFaces.current().executeScript("PF('BlockDidEntryDialog').hide()");

    }

    public void submitRequestC3() {
        String respose = null;
        try {
            respose = submitRequest(getDomibusUrlC3(), getRequestC3());
        } catch (IOException e) {
            respose = e.getMessage();
        }
        setResponseC3(respose);
        tabIndexC3 = 1;
        PrimeFaces.current().ajax().update("tabView:tcC3Sumbitform");
        PrimeFaces.current().executeScript("PF('BlockDidEntryDialog').hide()");

    }

    public String submitRequest(String url, String request) throws IOException {

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add request header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "eDelivery-domibu-test");
        con.setRequestProperty(" Content-Type", "application/soap+xml;charset=UTF-8");

        // Send post request
        con.setDoOutput(true);
        con.setDoInput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(request);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        return prettyPrintXML(response.toString());
    }

    private String prettyPrintXML(String xmlDocument) {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();

            InputSource is = new InputSource(new StringReader(xmlDocument));
            Document domDocument = db.parse(is);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);

            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            StreamResult result = new StreamResult(new StringWriter());
            DOMSource source = new DOMSource(domDocument);
            transformer.transform(source, result);
            return result.getWriter().toString();
        } catch (RuntimeException | ParserConfigurationException | SAXException | IOException | TransformerException e) {
            // could not parse response;
        }
        return xmlDocument;

    }
}
