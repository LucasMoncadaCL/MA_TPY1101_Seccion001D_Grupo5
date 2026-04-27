package com.panol_project.backendpanol.modules.catalog.category.domain;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {

    List<Categoria> findAll(boolean includeInactive);

    Optional<Categoria> findById(Integer id);

    boolean existsByNombre(String nombre, Integer excludingId);

    Optional<Categoria> findActiveById(Integer id);

    Categoria create(String nombre, String descripcion);

    Categoria updateNombre(Integer id, String nombre, String descripcion);

    void deactivate(Integer id);

    void deleteById(Integer id);

    int countImplementsByCategoryId(Integer categoryId);

    int countActiveImplementsByCategoryId(Integer categoryId);
}
