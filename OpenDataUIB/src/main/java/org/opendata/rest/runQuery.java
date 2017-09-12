package org.opendata.rest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.Json;
import javax.json.stream.JsonParser;*/

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


/**
 * Created with IntelliJ IDEA.
 * User: isaac
 * Date: 12/11/13
 * Time: 15:27
 * To change this template use File | Settings | File Templates.
 */
public class runQuery {


    public static String _server = "http://130.206.30.27:8080/";
    public static String _pathRepo = "openrdf-sesame/repositories/";
    public static String _repo = "UIB";
    public static String _user = "admin";
    public static String _pass = "admin";
    public static String Pref = "uib";
    public static String _keySPARQLBindings ="bindings";
    public static String _keySPARQLResults = "results";
    public static String NS = "http://datesobertes.uib.es/memoria2012.owl#";
    public static String _fullPrefix = "PREFIX "+Pref+":<"+NS+"> ";
    private static final Logger logger = LoggerFactory.getLogger("runQuery");



    private static String query(String query) throws IOException {

//        System.out.println("Plain Query:\t"+query);
        String input = URLEncoder.encode(_fullPrefix + query, "UTF-8");

        logger.info("Running query: " + query);
//        System.out.println("UTF8 Query:\t"+input);

        URL url = new URL(_server + _pathRepo + _repo + "?query=" + input+"&infer=true");

//        System.out.println("URL Query:\t"+url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/sparql-results+json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));

        String output;
        String result ="";
        while ((output = br.readLine()) != null) {
            result += output;
        }

        conn.disconnect();


        return result;
    }

    private static void _export(String query) throws IOException {

//        System.out.println("Plain Query:\t"+query);
        String input = URLEncoder.encode(_fullPrefix + query, "UTF-8");
//        System.out.println("UTF8 Query:\t"+input);

        URL url = new URL("http://130.206.30.27:8080/openrdf-workbench/repositories/UIB/query?action=exec&queryLn=SPARQL&query=" + input +"&limit=100&infer=true&&limit=0&Accept=application/rdf+json");

//        System.out.println("URL Query:\t"+url);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/rdf+json");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                    + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
                (conn.getInputStream())));
        InputStream in = conn.getInputStream();
        FileOutputStream out = new FileOutputStream("/tmp/result");

        byte[] b = new byte[1024];
        int count;

        while ((count = in.read(b)) > 0)
        {
            out.write(b, 0, count);
        }

        conn.disconnect();
        out.close();
    }

    public static void export(String query) throws IOException {
        _export(query);
    }
    public static net.sf.json.JSONArray run(String query) throws IOException {
        String output;
        query = query.replaceAll("base",Pref);
        output = query(query);

        net.sf.json.JSONObject json = (net.sf.json.JSONObject) JSONSerializer.toJSON(output);
        return json.getJSONObject(_keySPARQLResults).getJSONArray(_keySPARQLBindings);
    }
}
