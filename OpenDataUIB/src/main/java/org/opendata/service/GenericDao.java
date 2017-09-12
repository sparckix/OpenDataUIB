package org.opendata.service;

import org.opendata.domain.Entity;

import java.util.List;

public interface GenericDao<T extends Entity, I> {

    void create(T entity);

    T save(T entity);

    void remove(I id);

    T find(I id);

    List<T> findAll();

}