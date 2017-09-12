package org.opendata.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.opendata.domain.Consulta;
import org.opendata.service.dao.ConsultaDao;
import org.opendata.service.dao.UsuarioDao;
import org.opendata.domain.Usuario;

import org.springframework.security.crypto.password.PasswordEncoder;


public class DatabaseInitializer {


    private UsuarioDao userDao;
    private ConsultaDao consultaDao;

    private PasswordEncoder passwordEncoder;


    protected DatabaseInitializer() {

		/* Default constructor for reflection instantiation */

    }


    /*public DatabaseInitializer(UsuarioDao userDao, PasswordEncoder passwordEncoder) {

        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }*/

    public DatabaseInitializer(ConsultaDao consultaDao, UsuarioDao userDao) {
       this.consultaDao = consultaDao;
        this.userDao = userDao;
    }

    public void initDataBase() {

        /*Usuario userUser = new Usuario("user", this.passwordEncoder.encode("user"));
        userUser.addRole("user");
        this.userDao.save(userUser);
        */
        /*Usuario adminUser = new Usuario("hola", this.passwordEncoder.encode("admin"));
        adminUser.addRole("user");
        this.userDao.save(adminUser);*/

        Consulta c1 = new Consulta("SELECT * FROM PEPITO;");
        Set<Usuario> usuariosConsulta = new HashSet<Usuario>();
        usuariosConsulta.add(userDao.findByName("admin"));
        c1.setUsuariosConsulta(usuariosConsulta);
        this.consultaDao.save(c1);

    }

}