package com.alura.LiterAlura.repository;

import com.alura.LiterAlura.model.Autor;
import com.alura.LiterAlura.model.Libros;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface Repository extends JpaRepository<Libros, Long> {
    boolean existsByTituloAndAutores_Nombre(String titulo, String nombreAutor);

    @Query("SELECT l FROM Libros l JOIN FETCH l.autores")
    List<Libros> listarLibrosRegistrados();

    @Query("SELECT l FROM Libros l WHERE l.idiomas LIKE CONCAT('%', :idioma, '%')")
    List<Libros> filtrarLibrosPorIdiomas(@Param("idioma") String idioma);
}
