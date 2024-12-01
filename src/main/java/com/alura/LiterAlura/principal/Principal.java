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
    private List<Autor> autores;

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
                6 - Top los 10 libros más decargados
                7 - Buscar autor por su nombre
                8 - Estadisticas
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
                    case 3:
                        listarAutoresRegistrados();
                        break;
                    case 4:
                        listarAutoresVivosEnUnAño();
                        break;
                    case 5:
                        listarLibrosPorIdiomas();
                        break;
                    case 6:
                        topMasDescargados();
                        break;
                    case 7:
                        buscarAutorPorNombre();
                        break;
                    case 8:
                        obtenerEstadisticas();
                        break;
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
                            .orElse(new Autor(datosLibros.autores().get(0)));
                    Libros libro = new Libros(datosLibros);
                    libro.setAutores(autor);
                    return libro;
                })
                .findFirst();

        if (libroOpcional.isPresent()) {
            Libros libro = libroOpcional.get();

            if (repository.existsByTituloAndAutores_Nombre(libro.getTitulo(), libro.getAutores().getNombre())) {
                System.out.println("El libro ya está registrado en la base de datos.");
            } else {
                repository.save(libro);
                System.out.println("Libro guardado exitosamente: " + libro.getTitulo());
            }
        } else {
            System.out.println("Libro no encontrado en la API.");
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.listarAutoresRegistrados();

        if (autores.isEmpty()){
            System.out.println("No hay autores registrados en la base de datos.");
        }else {
            autores.forEach(autor -> {
                System.out.println("Nombre: " + autor.getNombre());
                System.out.println("Fecha de Nacimiento: " + autor.getFechaDeNacimiento());
                System.out.println("Fecha de Fallecimiento: " + (autor.getFechaDeFallecimiento() != null ? autor.getFechaDeFallecimiento() : "N/A"));
                System.out.println("-----------------------------------");
            });
        }
    }

    private void mostrarLibrosBuscados(){
        List<Libros> libros = new ArrayList<>();
        libros = datosLibros.stream()
                .map(d -> new Libros(d))
                .collect(Collectors.toList());
        libros.stream()
                .sorted(Comparator.comparing(Libros::getTitulo))
                .forEach(System.out::println);
    }

    private void listarLibrosRegistrados() {
        System.out.println("Listando los títulos de los libros registrados...");

        List<Libros> libros = repository.listarLibrosRegistrados();

        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en la base de datos.");
        } else {
            libros.forEach(libro -> {
                System.out.println("Título: " + libro.getTitulo());
                System.out.println("Autor: " + libro.getAutores().getNombre());
                System.out.println("Idioma: " + libro.getIdioma());
                System.out.println("Número de Descargas: " + libro.getNumeroDeDescargas());
                System.out.println("-----------------------------------");
            });
        }
    }

    private void listarLibrosPorIdiomas(){
        System.out.println("Elija por las iniciales el idioma a consultar: " +
                """
                        \nen = Ingles
                        es = Español
                        pr = Portugues
                        fr = Francés
                        """);
        var idiomaConsultado = teclado.nextLine().trim().toLowerCase();
        List<Libros> librosFiltrados = repository.filtrarLibrosPorIdiomas(idiomaConsultado);

        if (librosFiltrados.isEmpty()){
            System.out.println("No se encontraron libros en el idioma: " + idiomaConsultado);
        } else {
            System.out.println("Los libros registrados en el idioma " + idiomaConsultado + "son: ");
            librosFiltrados.forEach(libro -> System.out.println("- " + libro.getTitulo()));
        }
    }

    private void listarAutoresVivosEnUnAño(){
        System.out.println("Ingrese el año de consulta de autores vivos: ");
        var anioConsultado = teclado.nextInt();
        teclado.nextLine();
        List<Autor> aniosFiltrados = autorRepository.listarAutoresVivosEnUnAnio(anioConsultado);

        if (aniosFiltrados.isEmpty()){
            System.out.println("No se encontraron autores vivos en el año: " + anioConsultado);
        }else {
            System.out.println("Los autores vivos el año " + anioConsultado + "son: ");
            aniosFiltrados.forEach(autor -> System.out.println("- " + autor.getNombre()));
        }
    }

    private void topMasDescargados(){
        System.out.println("Los 10 libros más decargados son: ");
        var json = consumoAPI.obtenerDatos(URL_BASE);
        var datos = conversor.obtenerDatos(json, Datos.class);
        datos.resultados().stream()
                .sorted(Comparator.comparing(DatosLibros::numeroDeDescargas).reversed())
                .limit(10)
                .map(l -> l.titulo().toUpperCase())
                .forEach(System.out::println);
    }

    private void buscarAutorPorNombre() {
        System.out.println("Digite el nombre del autor a consultar: ");
        var consultaAutor = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + consultaAutor.replace(" ", "+"));

        if (json.isEmpty()) {
            System.out.println("No se encontraron autores con el nombre indicado.");
            return;
        }

        var datos = conversor.obtenerDatos(json, Datos.class);
        var autorEncontrado = datos.resultados().stream()
                .flatMap(d -> d.autores().stream())
                .findFirst();

        if (autorEncontrado.isPresent()) {
            System.out.println("Autor encontrado: " + autorEncontrado.get());
        } else {
            System.out.println("No se encontraron autores con el nombre indicado.");
        }
    }

    private void obtenerEstadisticas(){
        var json = consumoAPI.obtenerDatos(URL_BASE);
        var datos = conversor.obtenerDatos(json, Datos.class);
        IntSummaryStatistics estadisticas = datos.resultados().stream()
                .filter(libro -> libro.numeroDeDescargas()>0)
                .mapToInt(DatosLibros::numeroDeDescargas)
                .summaryStatistics();

        System.out.println("Estadísticas de descargas:");
        System.out.println("Media de descargas: " + estadisticas.getAverage());
        System.out.println("Máximo de descargas: " + estadisticas.getMax());
        System.out.println("Mínimo de descargas: " + estadisticas.getMin());
        System.out.println("Número total de libros con descargas: " + estadisticas.getCount());
    }
}