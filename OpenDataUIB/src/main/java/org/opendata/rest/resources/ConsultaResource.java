package org.opendata.rest.resources;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.opendata.domain.Consulta;
import org.opendata.domain.Usuario;
import org.opendata.service.dao.ConsultaDao;
import org.opendata.service.dao.UsuarioDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


@Component
@Path("/querys")
public class ConsultaResource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDetailsService userService;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private ConsultaDao consultaDao;



    /**
     * Guarda un nuevo usuario en la BD
     * @return exito o error
     */
    @Path("fav")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Consulta addFavQuery(@Context SecurityContext sc, Consulta query) {
        /*if(usuarioDao.findByName(usuario.getUsername()) == null) {
            usuario.addRole("user");
            usuario.setPassword(this.passwordEncoder.encode(usuario.getPassword()));
            usuarioDao.create(usuario);
            return "Registration successful";
        }*/
        /* Precondición: la query existe previamente (creada con create).
        * */

        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) sc.getUserPrincipal();
        //Esto no hara falta cuando securicemos con intercept
        if (auth == null) return null;

        if (query.getIdcons() == null) {
            query = create(sc, query);
        }
        Set<Usuario> set = query.getUsuariosConsulta();
        if (set == null) {
            logger.info("IT IS NULL");
            set = new HashSet<Usuario>(0);
        }
        Usuario u = (Usuario) auth.getPrincipal();
        u.getConsultasFavoritas().add(query);
        set.add(u);
        query.setUsuariosConsulta(set);
        logger.info("favored_query(): " + ((Usuario) auth.getPrincipal()).getUsername() + " " + query.getNombrecons());
        this.usuarioDao.save(u);
        return this.consultaDao.save(query);

    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Consulta create(@Context SecurityContext sc, Consulta query) {
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) sc.getUserPrincipal();
        //Esto no hara falta cuando securicemos con intercept
        if (auth == null) return null;

        this.logger.info("create_query(): " + query);
        Consulta c = this.consultaDao.save(query);
        return addFavQuery(sc,c);
    }


    /**
     * Lista las consultas favoritas del usuario
     */

    @Path("list")
    @GET
    public String list(@Context SecurityContext sc) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectWriter userWriter;
        userWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) sc.getUserPrincipal();
        //Esto no hara falta cuando securicemos con intercept
        if (auth == null) return null;
        logger.info("list(): " + ((Usuario) auth.getPrincipal()).getUsername());
        Set<Consulta> queryList = consultaDao.findByUser(((Usuario) auth.getPrincipal()));
        return userWriter.writeValueAsString(queryList);
    }

    /*@DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public void delete(@PathParam("id") Long id) {
        this.usuarioDao.remove(id);
    }

    @POST
    //@Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("{id}")
    public void update(@PathParam("id") Long id, Usuario usuario) {
        this.logger.info("update(): " + usuario.getRoles().toString());

        this.usuarioDao.save(usuario);
    }

    @Path("menu")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Menu returnMenuAnon(@Context SecurityContext sc) {
        if (sc.isUserInRole("admin")) {
            MenuItem m1 = new MenuItem("Home","#");
            MenuItem m2 = new MenuItem("SPARQL","#/sparql");
            MenuItem m3 = new MenuItem("Admin","#/admin");
            return new Menu(new MenuItem[]{m1,m2,m3});
        }
        MenuItem m1 = new MenuItem("Home","#");
        MenuItem m2 = new MenuItem("SPARQL","#/sparql");
        return new Menu(new MenuItem[]{m1,m2});
    }  */
}