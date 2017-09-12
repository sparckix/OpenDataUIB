package org.opendata.service.dao;

import org.opendata.service.GenericDao;
import org.opendata.domain.Usuario;

import org.springframework.security.core.userdetails.UserDetailsService;


public interface UsuarioDao extends GenericDao<Usuario, Long>, UserDetailsService {

    Usuario findByName(String name);

}
/*
import org.opendata.domain.Usuario;
import org.opendata.service.GenericDao;
public interface UsuarioDao extends GenericDao<Usuario, Long> {

    /**
     * Remove an user from the system
     * @param user User to remove
     * @return {@code true} if employee is not assigned to any task
     * or timesheet. Else {@code false}.

    public boolean insertUser(Usuario user);
    public boolean removeUser(Usuario user);
    public boolean goodUser(String nombre, String clave);
}

*/