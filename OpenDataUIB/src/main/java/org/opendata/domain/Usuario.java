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
public class Usuario  implements Entity, UserDetails {

    @Id
    @GeneratedValue
    private Long idusu;

    @Column(name="nombre", unique = true, length = 16, nullable = false)
    private String username;

    @Column(name="clave", length = 64, nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> roles = new HashSet<String>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "Consulta_Favorita", joinColumns = {
            @JoinColumn(name = "idusu", nullable = false, updatable = false) },
            inverseJoinColumns = { @JoinColumn(name = "idcons",
                    nullable = false, updatable = false) })
    private Set<Consulta> consultasFavoritas = new HashSet<Consulta>(0);


    protected Usuario() {

		/* Reflection instantiation */
    }


    public Usuario(String name, String passwordHash) {

        this.username = name;
        this.password = passwordHash;
    }

    public Set<Consulta> getConsultasFavoritas() {
        return this.consultasFavoritas;
    }

    public void setConsultasFavoritas(Set<Consulta> consultasFavoritas) {
        this.consultasFavoritas = consultasFavoritas;
    }

    public Long getId() {

        return this.idusu;
    }


    public void setId(Long id) {

        this.idusu = id;
    }

    public void setNombre(String nombre) {

        this.username = nombre;
    }

    public Set<String> getRoles() {

        return this.roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }


    public void addRole(String role) {
        this.roles.add(role);
    }


    @Override
    public String getPassword() {

        return this.password;
    }


    public void setPassword(String clave) {

        this.password = clave;
    }


    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Set<String> roles = this.getRoles();

        if (roles == null) {
            return Collections.emptyList();
        }

        Set<GrantedAuthority> authorities = new HashSet<GrantedAuthority>();
        for (String role : roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return authorities;
    }


    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }


    @Override
    public boolean isAccountNonLocked() {
        return this.enabled;
    }

    public void setAccountNonLocked(boolean nonLocked) {
        this.enabled = nonLocked;
    }


    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
/*
import javax.persistence.*;

@Entity
@Table(name = "Usuario", catalog = "opendata")
public class Usuario {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name = "idusu", unique = true, nullable = false)
    private long idusu;
    @Column(name = "nombre", nullable = false)
    private String nombre;
    @Column(name = "clave", nullable = false)
    private String clave;

    public Usuario() {
    }

    public Usuario(String nombre, String clave) {
        this.nombre = nombre;
        this.clave = clave;
    }

    public Long getId() {
        return idusu;
    }


    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre= nombre;
    }
    public String getClave() {
        return clave;
    }
    public void setClave(String clave) {
        this.clave = clave;
    }


}           */
