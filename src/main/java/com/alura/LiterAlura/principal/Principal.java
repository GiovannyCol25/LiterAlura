package com.alura.LiterAlura.principal;

import com.alura.LiterAlura.model.*;
import com.alura.LiterAlura.repository.AutorRepository;
import com.alura.LiterAlura.repository.Repository;
import com.alura.LiterAlura.service.ConsumoAPI;
import com.alura.LiterAlura.service.ConvierteDatos;

import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private static final String URL_BASE = "https://gutendex.com/books/";
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    private Scanner teclado = new Scanner(System.in);
    private List<DatosLibros> datosLibros = new ArrayList<>();
    private Repository repository;
    private AutorRepository autorRepository;

    public Principal(Repository repository, AutorRepository autorRepository) {
        this.repository = repository;
        this.autorRepository = autorRepository;
    }

    public void muestraElMenu(){
        System.out.println("Bienvenido al sistema. Usa el menú para interactuar.");
        int opcion = -1;

        while (opcion != 0) {
            var menu = """
                \n=== Menú Principal ===
                1 - Buscar libros por título
                2 - Listar libros registrados
                3 - Listar autores registrados
                4 - Listar autores vivos en un determinado año
                5 - Listar libros por idiomas
                0 - Salir
                """;
            System.out.println(menu);
            System.out.print("Selecciona una opción: ");

            if (teclado.hasNextInt()) {
                opcion = teclado.nextInt();
                teclado.nextLine();

                switch (opcion) {
                    case 1:
                        buscarLibrosPorTitulo();
                        break;
                    case 2:
                        listarLibrosRegistrados();
                        break;
//                    case 3:
//                        mostrarLibrosBuscados();
//                        break;
//                    case 4:
//                        listarAutoresVivosEnUnAño();
//                        break;
//                    case 5:
//                        listarLibrosPorIdiomas();
//                        break;
                    case 0:
                        System.out.println("Saliendo del sistema. ¡Hasta luego!");
                        break;
                    default:
                        System.out.println("Opción no válida. Inténtalo nuevamente.");
                }
            } else {
                System.out.println("Error: Ingresa un número válido.");
                teclado.nextLine(); // Limpia el buffer en caso de entrada incorrecta
            }
        }
    }

    private void buscarLibrosPorTitulo() {
        System.out.println("Escribe el título del libro que deseas buscar:");
        String nombreLibro = teclado.nextLine().toLowerCase();
        String json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + nombreLibro.replace(" ", "+"));
        System.out.println(json);
        Datos datos = conversor.obtenerDatos(json, Datos.class);

        Optional<Libros> libroOpcional = datos.resultados().stream()
                .filter(l -> l.titulo().toUpperCase().contains(nombreLibro.toUpperCase()))
                .map(datosLibros -> {
                    Autor autor = autorRepository.findByNombre(datosLibros.autores().get(0).nombre())
                            .orElse(new Autor(datosLibros.autores().get(0))); // Busca si el autor ya existe
                    Libros libro = new Libros(datosLibros);
                    libro.setAutores(autor); // Relaciona el libro con el autor (nuevo o existente)
                    return libro;
                })
                .findFirst();

        if (libroOpcional.isPresent()) {
            Libros libro = libroOpcional.get();

            // Verifica si el libro ya existe en la base de datos
            if (repository.existsByTituloAndAutores_Nombre(libro.getTitulo(), libro.getAutores().getNombre())) {
                System.out.println("El libro ya está registrado en la base de datos.");
            } else {
                repository.save(libro); // Guarda el libro y lo asocia al autor existente si es necesario
                System.out.println("Libro guardado exitosamente: " + libro.getTitulo());
            }
        } else {
            System.out.println("Libro no encontrado en la API.");
        }
    }

// este codigo pendiente por modificar para listar autores registrados
//    private void buscarAutor() {
//        System.out.println("Ingresar nombre del autor a buscar: ");
//        var autor = teclado.nextLine();
//        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + autor.replace(" ", "+"));
//        var autorBuscado = conversor.obtenerDatos(json, Datos.class);
//        autorBuscado.resultados().stream()
//                        .filter(a -> a.autores().contains(autorBuscado.toString()));
//        System.out.println(autorBuscado.toString());
//    }

    private void mostrarLibrosBuscasdos(){
        List<Libros> libros = new ArrayList<>();
        libros = datosLibros.stream()
                .map(d -> new Libros(d))
                .collect(Collectors.toList());
        libros.stream()
                .sorted(Comparator.comparing(Libros::getTitulo))
                .forEach(System.out::println);
    }

    private void listarLibrosRegistrados(){
//  continuacion del metodo        .
    }
}
    

