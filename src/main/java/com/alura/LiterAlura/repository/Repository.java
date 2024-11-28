package com.alura.LiterAlura.repository;

import com.alura.LiterAlura.model.Libros;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Repository extends JpaRepository<Libros, Long> {
    boolean existsByTituloAndAutores_Nombre(String titulo, String nombreAutor);
}
