package com.byv7.scef;
 
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
 
public class XMLParser {

    // constructor
    public XMLParser() {
    }
 
    /**
     * Getting XML from URL making HTTP request
     * @param uri string
     * */
    public String getXmlFromUrl(String uri) {
        String xml = null;

        try {

            URL url = new URL(uri);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2500);
            connection.setReadTimeout(2500);
            connection.setInstanceFollowRedirects(true);

            if( connection.getResponseCode() == HttpsURLConnection.HTTP_OK ) {
                InputStream in = connection.getInputStream();
                //下面对获取到的输入流进行读取
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                in.close();
                xml = response.toString();
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return xml;
    }

    /**
     * Getting XML DOM element
     * @param xml string
     * */
    public Document getDomElement(String xml){
        Document doc = null;
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            doc = db.parse(is);
        } catch (ParserConfigurationException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (SAXException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        } catch (IOException e) {
            Log.e("Error: ", e.getMessage());
            return null;
        }

        return doc;
    }
 
    /** Getting node value
      * @param elem element
      */
     public final String getElementValue( Node elem ) {
         Node child;
         if( elem != null){
             if (elem.hasChildNodes()){
                 for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){
                     if( child.getNodeType() == Node.TEXT_NODE  ){
                         return child.getNodeValue();
                     }
                 }
             }
         }
         return "";
     }
 
     /**
      * Getting node value
      * @param item node
      * @param str string
      * */
     public String getValue(Element item, String str) {
            NodeList n = item.getElementsByTagName(str);
            return this.getElementValue(n.item(0));
        }
}