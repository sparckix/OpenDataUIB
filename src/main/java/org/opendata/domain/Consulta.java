package org.opendata.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


@javax.persistence.Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Consulta implements Entity {

    @Id
    @GeneratedValue
    private Long idcons;

    @Column(name="nom_cons", nullable = false)
    private String nombreCons;

    @Column(name="consulta", nullable = false, columnDefinition="TEXT")
    private String consulta;

    @ManyToMany(fetch = FetchType.EAGER, mappedBy = "consultasFavoritas")
    private Set<Usuario> usuariosConsulta = new HashSet<Usuario>(0);


    protected Consulta() {

		/* Reflection instantiation */
    }


    public Consulta(String consulta) {
        this.consulta = consulta;
    }

    public Long getIdcons() {
        return this.idcons;
    }

    public void setNombreCons(String consulta) {
        this.nombreCons = consulta;
    }

    public String getNombrecons() {
        return this.nombreCons;
    }


    public void setConsulta(String consulta) {
        this.consulta = consulta;
    }

    public String getConsulta() {
        return this.consulta;
    }

    @JsonIgnore
    public Set<Usuario> getUsuariosConsulta() {
        return this.usuariosConsulta;
    }

    @JsonIgnore
    public void setUsuariosConsulta(Set<Usuario> usuarios) {
        this.usuariosConsulta = usuarios;
    }
}