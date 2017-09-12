package org.opendata.rest.resources;

/**
 * Created with IntelliJ IDEA.
 * User: isaac
 * Date: 12/11/13
 * Time: 15:14
 * To change this template use File | Settings | File Templates.
 */

//TODO checkear strings null



import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.opendata.rest.runQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.Produces;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Pattern;


@Path("/QueryResource")
public class QueryResource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());


    @GET
    @Path("/export/{query}")
    public Response export(
            @PathParam("query") String query) throws JSONException, IOException {

        query = URLDecoder.decode(query, "UTF-8");
        char[] temp = query.toCharArray();
        temp[query.toLowerCase().indexOf(" ?")] = '{';
        temp[query.toLowerCase().indexOf(" where")] = '}';
        query = String.valueOf(temp);
        query = query.replaceAll("(?i)SELECT","CONSTRUCT");

        /*if (format.equals("")) {
            //Export default format (RDF)
            br = runQuery.export(query);
            return Response.ok(br, MediaType.APPLICATION_OCTET_STREAM).build();
        }
        else {*/
            //format = format.split("/")[2];
        runQuery.export(query);
        File f = new File("/tmp/result");
        Response.ResponseBuilder response = Response.ok((Object) f);
        response.type(MediaType.TEXT_PLAIN_TYPE);
        response.header("Content-Disposition", "attachment; filename=\"result.rdf\"");
        return response.build();
        //}
    }



    @GET
    @Path("/doquery/{query}{plot:(/plot)?}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String customQuery(@PathParam("query") String query,
                              @PathParam("plot") String shouldPlot) throws JSONException, UnsupportedEncodingException {


        Map<String, Integer> hm = new HashMap<String, Integer>();
        int count = 0;

        JSONArray resultPlot  = new JSONArray();
        JSONArray nodes  = new JSONArray();
        JSONArray links = new JSONArray();

        query = URLDecoder.decode(query, "UTF-8");

        JSONArray res = null;
        try {
            res = runQuery.run(query);
            Iterator<JSONObject> it = res.iterator();

            JSONArray result = new JSONArray();

            while (it.hasNext()) {
                JSONObject obj = it.next();
                Iterator<?> itobj = obj.keys();
                JSONObject ar = new JSONObject();

                while (itobj.hasNext()) {
                    String key = (String) itobj.next();
                    String temp = (String) obj.getJSONObject(key).get("value");
                    ar.put(key, temp);
                    if(!shouldPlot.equals("") && !hm.containsKey(temp)) {
                        hm.put(temp,count);
                        count++;
                        JSONObject node = new JSONObject();
                        node.put("name",temp);
                        nodes.put(node);
                    }
                }
                /*JSONObject ar = new JSONObject();
                ar.put("subject", subject2);
                ar.put("predicate", predicate);
                ar.put("object", object);*/
                result.put(ar);
            }
            if (shouldPlot.equals(""))
                return result.toString();
            else if (result.length()>0) {
                int group = 1;
                JSONObject exampleObject = result.getJSONObject(0);
                String prefix = "(([^\\s]+\\:[^\\s]+)|(\\?\\w+))";
                List<String> allMatches = new ArrayList<String>();
                java.util.regex.Matcher m = Pattern.compile(prefix+"[\\s]*"+prefix+"[\\s]*"+prefix+"[\\s]*((\\.)|$|})")
                        .matcher(query);

                while (m.find()) {
                    allMatches.add(m.group());
                }
                for (String i : allMatches) {
                    String[] arr = i.split(" ");
                    for (int j=0; j < arr.length; j++) {
                        arr[j] = arr[j].replace("?","");
                    }

                    JSONObject node = new JSONObject();
                    int id_subject;
                    int id_object;
                    /* Tratamiento de casos
                     * arr[0]: sujeto
                     * arr[1]: predicado
                     * arr[2]: objeto
                      * */
                    if(!exampleObject.has(arr[0]) && !exampleObject.has(arr[2])) continue; //sujeto ni objeto estan en resultado
                    //JSONObject[] result_object = (JSONObject[]) result.toArray();
                    if(exampleObject.has(arr[0]) && !exampleObject.has(arr[2])) {
                        //Sujeto aparece en el resultado y objeto no
                        if(!hm.containsKey(arr[2])) {
                            hm.put(arr[2],count);
                            id_object = count;
                            count++;
                            node.put("name",arr[2]);
                            nodes.put(node);
                        }
                        else {
                            id_object = hm.get(arr[2]);
                        }
                        /* Para cada nodo resultado añadimos la relacion arr[0] - arr[1] - arr[2]
                        *  Precondicion: los nodos del resultado han sido insertados previamente en hm TODO
                        * */
                        //for (JSONObject o : result_object) {
                         for (int j=0; j < result.length(); j++) {
                             JSONObject o = result.getJSONObject(j);
                             JSONObject link = new JSONObject();
                             //String subject = (String) o.get(arr[0]);
                             String subject = (String) o.get(arr[0]);//getJSONObject(arr[0]).get("value");
                             id_subject = hm.get(subject);
                             link.put("source",id_subject);
                             link.put("target",id_object);
                             logger.info(subject+" con ID "+id_subject+ " y "+arr[2]);
                             links.put(link);
                             //tratamiento color nodo
                             JSONObject cnode = nodes.getJSONObject(id_subject);
                             if (!cnode.has("group")) cnode.put("group", group);
                        }
                        group++;
                    }
                    else if (!exampleObject.has(arr[0]) && exampleObject.has(arr[2])) {
                        //Sujeto no aparece en el resultado pero objeto si
                        if(!hm.containsKey(arr[0])) {
                            hm.put(arr[0],count);
                            id_subject = count;
                            count++;
                            node.put("name",arr[0]);
                            nodes.put(node);
                        }
                        else {
                            id_subject = hm.get(arr[0]);
                        }
                        //for (JSONObject o : result_object) {
                        for (int j=0; j < result.length(); j++) {
                            JSONObject o = result.getJSONObject(j);
                            JSONObject link = new JSONObject();
                            //String object = (String) o.get(arr[2]);
                            String object = (String) o.get(arr[2]);//getJSONObject(arr[2]).get("value");
                            id_object = hm.get(object);
                            link.put("source",id_subject);
                            link.put("target",id_object);
                            logger.info(arr[0] +" con ID "+id_subject+ " y "+ object + " con id "+id_object);
                            links.put(link);

                            JSONObject cnode = nodes.getJSONObject(id_object);
                            if (!cnode.has("group")) cnode.put("group", group);
                        }
                        group++;
                    }
                    else {
                        //Sujeto y objeto aparecen en el resultado
                        //for (JSONObject o : result_object) {
                        int group_subject = group;
                        group++;
                        int group_object = group;
                        for (int j=0; j < result.length(); j++) {
                            JSONObject o = result.getJSONObject(j);
                            JSONObject link = new JSONObject();
                            String subject = (String) o.get(arr[0]);
                            String object = (String) o.get(arr[2]);
                            //String subject = (String) o.getJSONObject(arr[0]).get("value");
                            //String object = (String) o.getJSONObject(arr[2]).get("value");

                            id_subject = hm.get(subject);
                            id_object = hm.get(object);
                            link.put("source",id_subject);
                            link.put("target",id_object);
                            logger.info(subject+" con ID "+id_subject+ " y "+object + " con id "+id_object);
                            links.put(link);

                            JSONObject cnode = nodes.getJSONObject(id_subject);
                            JSONObject cnode1 = nodes.getJSONObject(id_object);
                            if (!cnode.has("group")) cnode.put("group", group_subject);
                            if (!cnode1.has("group")) cnode1.put("group", group_object);
                        }
                        group++;
                    }
                }
                JSONObject f = new JSONObject();
                f.put("nodes",nodes);
                f.put("links",links);
                return f.toString();

            }
            else {
                return result.toString();
            }

        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    @GET
    @Path("/count/{resource}{predicateFilter:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String getCount(@PathParam("resource") String resource,
                           @PathParam("predicateFilter") String predicateFilter) throws JSONException {

        resource = resource.replace("?","\\?");
        String query = "";
        if (predicateFilter.equals("")) {
            query = "SELECT (COUNT(?s) AS ?c) WHERE {"
                + " {BIND (" + resource + " AS ?s) ?s ?p ?o .}"
                + " UNION {BIND (" + resource +" AS ?p) ?s ?p ?o .}" +
                " UNION {BIND (" + resource + " AS ?o) ?s ?p ?o .} }";
        }
        else {
            predicateFilter = predicateFilter.split("/")[1];
            query = "SELECT (COUNT(?s) AS ?c) WHERE {"
                    + " {BIND (" + resource+ " AS ?s) BIND (" + predicateFilter + " AS ?p) ?s ?p ?o .}"
                    + " UNION {BIND (" + resource + " AS ?o) BIND (" + predicateFilter + " AS ?p) ?s ?p ?o .} }";
        }

        JSONArray res = null;
        try {
            res = runQuery.run(query);
            Iterator<JSONObject> it = res.iterator();

            JSONArray result = new JSONArray();


            while (it.hasNext()) {
                JSONObject obj = it.next();
                String count = (String) obj.getJSONObject("c").get("value");
                return count;
            }


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }


    //Cambiar plot por plot no cualquier cosa
    //TODO FIX LOGIC PLOT
    @GET
    @Path("/list/{subject}/{limit}/{offset}{plot:(/plot)?}{predicateFilter:(/[^/]+?)?}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String getPropertiesAndObjects(
            @PathParam("subject") String subject,
            @PathParam("limit") String limit,
            @PathParam("offset") String offset,
            @PathParam("predicateFilter") String predicateFilter,
            @PathParam("plot") String shouldPlot) throws JSONException {

        subject = subject.replace("?","\\?");
        String query = "";
        if (predicateFilter.equals("")) {
            query = "SELECT ?s ?p ?o WHERE {"
                    + " {BIND (" + subject + " AS ?s) ?s ?p ?o .}"
                    + " UNION {BIND (" + subject +" AS ?p) ?s ?p ?o .}" +
                    " UNION {BIND (" + subject + " AS ?o) ?s ?p ?o .} } ORDER BY ?s LIMIT "+ limit + " OFFSET "+ offset;
        }
        else {
            predicateFilter = predicateFilter.split("/")[1];
            query = "SELECT ?s ?p ?o WHERE {"
                    + " {BIND (" + subject + " AS ?s) BIND (" + predicateFilter + " AS ?p) ?s ?p ?o .}"
                    + " UNION {BIND (" + subject + " AS ?o) BIND (" + predicateFilter + " AS ?p) ?s ?p ?o .} } ORDER BY ?s LIMIT "+ limit + " OFFSET "+ offset;
        }
        /*String query =
                " SELECT * WHERE" +
                        " { ?s ?p ?o . " +
                        " FILTER (regex(str(?s),\"" + subject + "\"))" +
                        " } LIMIT 5";        //Hay que  mejorar el query para tener un OPTIONAL + FILTER por objeto*/
        JSONArray res = null;
        try {
            int count = 0;
            int group_subject = 1;
            int group_object = 2;
            Map<String, Integer> hm = new HashMap<String, Integer>();

            res = runQuery.run(query);
            Iterator<JSONObject> it = res.iterator();

            JSONArray result = new JSONArray();
            JSONArray nodes  = new JSONArray();
            JSONArray links = new JSONArray();


            while (it.hasNext()) {
                JSONObject obj = it.next();

                String subject2 = (String) obj.getJSONObject("s").get("value");
                String predicate = (String) obj.getJSONObject("p").get("value");
                String object = (String) obj.getJSONObject("o").get("value");

                //arregla uris: es realmente necesario? o lo hace la capa de presentación!
//                if (predicate.contains(runQuery.NS)){
//                    predicate = predicate.substring(runQuery.NS.length());
//                }
//
//                if (object.contains(runQuery.NS)){
//                    object = object.substring(runQuery.NS.length());
//                }
//
//                if (subject2.contains(runQuery.NS)){
//                    subject2 = subject2.substring(runQuery.NS.length());
//                }

                if (!shouldPlot.equals("")) {
                    JSONObject node = new JSONObject();
                    JSONObject link = new JSONObject();
                    int id_subject;
                    int id_object;

                /* Tratamiento sujeto */
                    if(!hm.containsKey(subject2)) {
                        hm.put(subject2,count);
                        id_subject = count;
                        count++;
                        node.put("name",subject2);
                        node.put("group",group_subject);
                        nodes.put(node);
                    }
                    else {
                        id_subject = hm.get(subject2);
                    }

                /* Tratamiento objeto */
                    node = new JSONObject();
                    if(!hm.containsKey(object)) {
                        hm.put(object,count);
                        id_object = count;
                        count++;
                        node.put("name",object);
                        node.put("group",group_object);
                        nodes.put(node);
                    }
                    else {
                        id_object = hm.get(object);
                    }

                /* Tratamiento relacion */
                    link.put("source",id_subject);
                    link.put("target",id_object);
                    links.put(link);
                }
                else {
                    JSONObject ar = new JSONObject();
                    ar.put("subject", subject2);
                    ar.put("predicate", predicate);
                    ar.put("object", object);
                    result.put(ar);
                }
            }
            if (!shouldPlot.equals("")) {
                JSONObject f = new JSONObject();
                f.put("nodes",nodes);
                f.put("links",links);
                return f.toString();
            }
            else {
                return result.toString();
            }


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    @GET
    @Path("/listlike/{subject}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
    public String getPropertiesAndObjects(@PathParam("subject") String resource) {

        /*" SELECT * WHERE" +
                " { ?s ?p ?o . " +
                " FILTER (regex(str(?s),\"" + resource + "\"))" +
                " }  ";*/

        String query = "SELECT ?s ?p ?o WHERE { ?s ?p ?o . FILTER(regex(str(?s),\""+ resource + "\") " +
                "|| regex(str(?p),\""+ resource + "\") || regex(str(?o),\""+ resource + "\")) }";

        JSONArray res = null;
        try {
            res = runQuery.run(query);
            Iterator<JSONObject> it = res.iterator();

            JSONArray result = new JSONArray();


            while (it.hasNext()) {
                JSONObject obj = it.next();

                String subject2 = (String) obj.getJSONObject("s").get("value");
                String predicate = (String) obj.getJSONObject("p").get("value");
                String object = (String) obj.getJSONObject("o").get("value");

                JSONObject ar = new JSONObject();
                ar.put("subject", subject2);
                ar.put("predicate", predicate);
                ar.put("object", object);

                result.put(ar);
            }
            return result.toString();


        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }
}
