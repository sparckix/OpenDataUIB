package org.opendata.web;

import org.opendata.domain.MenuItem;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.opendata.service.dao.UsuarioDao;
import org.opendata.domain.Usuario;
import org.opendata.domain.Menu;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;

@Controller
@RequestMapping("/users")
public class UsuarioController {

    private static Logger LOGGER = Logger.getLogger("InfoLogging");

    private UsuarioDao usuarioDao;

    /*
    @RequestMapping(method = RequestMethod.GET)
    public String createEmployeeForm(Model model) {
        model.addAttribute("user", new Usuario());
        return "users/new";
    }
    */

    @Autowired
    public void setManagerDao(UsuarioDao managerDao) {
        this.usuarioDao = managerDao;
    }

    public UsuarioDao getManagerDao() {
        return usuarioDao;
    }

    /**
     * Saves new manager to the database
     * @param usuario Manager to save
     * @return redirects to managers
     */
    @RequestMapping(value="/new", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, headers={"Accept=*/*", "Content-Type=application/json"})
    public @ResponseBody String addUser(@RequestBody Usuario usuario) {

        /*if (usuarioDao.insertUser(usuario)) {
            return "Registration successful";
        }  */
        return "Username already used";
        //return "redirect:/newa";
    }

    @RequestMapping(value="/menu", method = RequestMethod.GET)
    public @ResponseBody Menu returnMenu() {
        MenuItem m1 = new MenuItem("Home","#");
        MenuItem m2 = new MenuItem("SPARQL","#/sparql");
        return new Menu(new MenuItem[]{m1,m2});
    }

    @RequestMapping(value="/authenticate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, headers={"Accept=*/*", "Content-Type=application/json"})
    public @ResponseBody LoginResponse login(@RequestBody LoginRequest loginRequest) {
        //if(loginRequest.getEmail().equals("admin") && loginRequest.getPassword().equals("password")) {
       /*if (usuarioDao.goodUser(loginRequest.getEmail(),loginRequest.getPassword())) {
            LoginResponse response = new LoginResponse();
            response.setSessionId("Unique Generated Session ID");
            response.setStatus("OK");

            return response;
        }*/

        LoginResponse response = new LoginResponse();
        response.setSessionId(null);
        response.setStatus("Invalid user/password combination");
        return response;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleMethodArgumentNotValidException(
            MethodArgumentNotValidException error) {
        LOGGER.info(error.getBindingResult().toString());
        return "Bad value";
    }

    @ExceptionHandler(TypeMismatchException.class)
    public String handleTypeMismatchException(TypeMismatchException ex,
                                              HttpServletRequest req, HttpServletResponse resp) {
        LOGGER.info("Parameter failure: {}"
                + ex.getRootCause().getLocalizedMessage());
        LOGGER.info("Invalid value is: {}" + ex.getValue());
        LOGGER.info("Required type is: {}"
                + ex.getRequiredType().getSimpleName());

        return "Bad value";
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public String handleMessageNotReadableException(
            HttpMessageNotReadableException ex, HttpServletRequest req,
            HttpServletResponse resp) {
        LOGGER.info("Failure: {}" + ex.getRootCause().getLocalizedMessage());

        return "Bad value";
    }
    public static class LoginRequest {
        private String email;
        private String password;

        public LoginRequest() {
        }
        public LoginRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {

        private String sessionId;
        private String status;

        public LoginResponse() {
        }
        public LoginResponse(String sessionId, String status) {
            this.sessionId = sessionId;
            this.status = status;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

