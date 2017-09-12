package org.opendata.service.dao;


import org.opendata.domain.Consulta;
import org.opendata.domain.Usuario;
import org.opendata.service.GenericDao;

import java.util.List;
import java.util.Set;

public interface ConsultaDao extends GenericDao<Consulta, Long> {
    Set<Consulta> findByUser(Usuario usuario);
}