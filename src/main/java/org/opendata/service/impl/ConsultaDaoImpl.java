package org.opendata.service.impl;

import org.opendata.domain.Consulta;
import org.opendata.domain.Usuario;
import org.opendata.service.JpaDao;
import org.opendata.service.dao.ConsultaDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;


public class ConsultaDaoImpl extends JpaDao<Consulta, Long> implements ConsultaDao {
    public ConsultaDaoImpl() {

        super(Consulta.class);
    }


    @Override
    @Transactional(readOnly = true)
    public Set<Consulta> findByUser(Usuario usuario) {
        return usuario.getConsultasFavoritas();
        /*TypedQuery<Consulta> q1 =
                this.getEntityManager().createQuery("SELECT c FROM Consulta_Favorita cf WHERE cf.idusu = :idusu", Consulta.class).setParameter("idusu", usuario.getId());
        List<Consulta> consultas = q1.getResultList();     */
    }

}