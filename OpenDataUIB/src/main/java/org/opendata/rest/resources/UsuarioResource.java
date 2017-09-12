package org.opendata.rest.resources;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.opendata.domain.Menu;
import org.opendata.domain.MenuItem;
import org.opendata.domain.Usuario;
import org.opendata.rest.TokenUtils;
import org.opendata.service.dao.UsuarioDao;
import org.opendata.transfer.UsuarioTransfer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
@Path("/user")
public class UsuarioResource {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private UserDetailsService userService;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    @Qualifier("authenticationManager")
    private AuthenticationManager authManager;


    /**
     * Guarda un nuevo usuario en la BD
     * @param usuario Usuario a crear
     * @return exito o error
     */
    @Path("new")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public String addUser(Usuario usuario) {

        /*if (usuarioDao.ssave(usuario)) {
            return "Registration successful";
        }
        return "Username already used";*/
        if(usuarioDao.findByName(usuario.getUsername()) == null) {
            usuario.addRole("user");
            usuario.setPassword(this.passwordEncoder.encode(usuario.getPassword()));
            usuarioDao.create(usuario);
            return "Registration successful";
        }
        return "Username already used";
    }

    @GET
    public String list(@Context SecurityContext sc) throws JsonGenerationException, JsonMappingException, IOException {
        ObjectWriter userWriter;
        if (sc.isUserInRole("admin")) {
            userWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
            List<Usuario> userList = usuarioDao.findAll();
            return userWriter.writeValueAsString(userList);
        }
        return null;
    }

    @DELETE
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


    @Path("authenticate")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public UsuarioTransfer authenticate(@FormParam("username") String username, @FormParam("password") String password) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = this.authManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        Map<String, Boolean> roles = new HashMap<String, Boolean>();

		/*
		 * Reload user as password of authentication principal will be null after authorization and
		 * password is needed for token generation
		 */
        UserDetails userDetails = this.userService.loadUserByUsername(username);

        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            roles.put(authority.toString(), Boolean.TRUE);
        }

        return new UsuarioTransfer(userDetails.getUsername(), roles, TokenUtils.createToken(userDetails));
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
    }
}