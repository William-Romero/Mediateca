package com.dvdapp.dao;

import java.sql.SQLException;
import java.util.List;

import com.dvdapp.model.Material;

public interface MaterialDao<T extends Material> {
    T save(T material) throws SQLException;

    void update(T material) throws SQLException;

    void deleteByCode(String code) throws SQLException;

    T findByCode(String code) throws SQLException;

    List<T> findAll() throws SQLException;

    List<T> search(String text) throws SQLException;

    String findMaxCode(String prefix) throws SQLException;
}
