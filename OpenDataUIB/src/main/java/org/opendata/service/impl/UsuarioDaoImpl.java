package org.opendata.service.impl;


import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import org.opendata.service.JpaDao;
import org.opendata.domain.Usuario;

import org.opendata.service.dao.UsuarioDao;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;


public class UsuarioDaoImpl extends JpaDao<Usuario, Long> implements UsuarioDao {

    public UsuarioDaoImpl() {

        super(Usuario.class);
    }


    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Usuario user = this.findByName(username);
        if (null == user) {
            throw new UsernameNotFoundException("The user with name " + username + " was not found");
        }

        return user;
    }


    @Override
    @Transactional(readOnly = true)
    public Usuario findByName(String name) {
        final CriteriaBuilder builder = this.getEntityManager().getCriteriaBuilder();
        final CriteriaQuery<Usuario> criteriaQuery = builder.createQuery(this.entityClass);

        Root<Usuario> root = criteriaQuery.from(this.entityClass);
        Path<String> namePath = root.get("username");
        criteriaQuery.where(builder.equal(namePath, name));

        TypedQuery<Usuario> typedQuery = this.getEntityManager().createQuery(criteriaQuery);
        List<Usuario> users = typedQuery.getResultList();

        if (users.isEmpty()) {
            return null;
        }

        return users.iterator().next();
    }

}
/*import org.hibernate.Query;
import org.springframework.stereotype.Repository;
import org.opendata.domain.Usuario;
import org.opendata.service.dao.UsuarioDao;

import java.util.ArrayList;
import java.util.List;

@Repository("usuarioDao")
public class UsuarioDaoImpl extends HibernateDao<Usuario, Long> implements UsuarioDao {

    @Override
    public boolean removeUser(Usuario user) {
        /* logic to specify when you can't delete an user
        // ok, remove as usual
        if (userExists(user))
            remove(user);
        return true;
    }
    @Override
    public boolean insertUser(Usuario user) {
        if(!userExists(user)) {
            add(user);
            return true;
        }
        return false;
    }

    @Override
    public boolean goodUser(String nombre, String clave) {
        List<Usuario> userList = new ArrayList<Usuario>();
        Query query = currentSession().createQuery("from Usuario u where u.nombre = :nombre and u.clave = :clave");
        query.setParameter("nombre", nombre);
        query.setParameter("clave", clave);
        userList = query.list();
        if (userList.size() > 0)
            return true;
        else
            return false;
    }
    public boolean userExists(Usuario user) {
        List<Usuario> userList = new ArrayList<Usuario>();
        Query query = currentSession().createQuery("from Usuario u where u.nombre = :nombre");
        query.setParameter("nombre", user.getNombre());
        userList = query.list();
        if (userList.size() > 0)
            return true;
        else
            return false;
    }

}        */
