package com.aluracursos.literalura.principal;

import com.aluracursos.literalura.model.Autor;
import com.aluracursos.literalura.model.Datos;
import com.aluracursos.literalura.model.DatosLibro;
import com.aluracursos.literalura.model.Libro;
import com.aluracursos.literalura.repository.AutorRepository;
import com.aluracursos.literalura.repository.LibroRepository;
import com.aluracursos.literalura.service.ConsumoAPI;
import com.aluracursos.literalura.service.ConvierteDatos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class Principal {
    private final String URL_BASE = "https://gutendex.com/books/";
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoAPI = new ConsumoAPI();
    private ConvierteDatos conversor = new ConvierteDatos();
    @Autowired
    private LibroRepository libroRepository;
    @Autowired
    private AutorRepository autorRepository;

    public void muestraMenu() {
        var opcion = -1;
        while (opcion != 0) {
            var menu = """
                    -----------
                    Elija la opción a través de su número:
                    1- buscar libro por título
                    2- listar libros registrados
                    3- listar autores registrados
                    4- listar autores vivos en un determinado año
                    5- listar libros por idioma
                    0- salir
                    -----------
                    """;
            System.out.println(menu);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1:
                    buscarLibroPorTitulo();
                    break;
                case 2:
                    listarLibrosRegistrados();
                    break;
                case 3:
                    listarAutoresRegistrados();
                    break;
                case 4:
                    listarAutoresVivosEnAnio();
                    break;
                case 5:
                    listarLibrosPorIdioma();
                    break;
                case 0:
                    System.out.println("Cerrando la aplicación...");
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }

    private void buscarLibroPorTitulo() {
        System.out.println("Escribe el nombre del libro que deseas buscar:");
        var tituloLibro = teclado.nextLine();
        var json = consumoAPI.obtenerDatos(URL_BASE + "?search=" + tituloLibro.replace(" ", "+"));

        var datos = conversor.obtenerDatos(json, Datos.class);

        Optional<DatosLibro> libroEncontrado = datos.libros().stream()
                .filter(l -> l.titulo().toUpperCase().contains(tituloLibro.toUpperCase()))
                .findFirst();

        if (libroEncontrado.isPresent()) {
            DatosLibro datosLibro = libroEncontrado.get();

            Optional<Libro> libroExistente = libroRepository.findByTitulo(datosLibro.titulo());

            if (libroExistente.isPresent()) {
                System.out.println("\nEl libro '" + libroExistente.get().getTitulo() + "' ya está registrado en la base de datos.");
            } else {
                Libro libro = new Libro(datosLibro);
                Autor autor = new Autor(datosLibro.autor().get(0));

                Optional<Autor> autorExistente = autorRepository.findByNombre(autor.getNombre());
                if (autorExistente.isPresent()){
                    libro.setAutor(autorExistente.get());
                } else {
                    libro.setAutor(autor);
                }

                libroRepository.save(libro);
                System.out.println("--- Libro ---");
                System.out.println(libro);
                System.out.println("Libro guardado exitosamente!");
            }
        } else {
            System.out.println("\nLibro no encontrado en la API.");
        }
    }

    private void listarLibrosRegistrados() {
        List<Libro> libros = libroRepository.findAll();
        if (libros.isEmpty()) {
            System.out.println("No hay libros registrados en la base de datos.");
        } else {
            libros.forEach(System.out::println);
        }
    }

    private void listarAutoresRegistrados() {
        List<Autor> autores = autorRepository.findAll();
        if (autores.isEmpty()) {
            System.out.println("No hay autores registrados en la base de datos.");
        } else {
            autores.forEach(System.out::println);
        }
    }

    private void listarAutoresVivosEnAnio() {
        System.out.println("Ingrese el año para buscar autores vivos:");
        var anio = teclado.nextInt();
        teclado.nextLine();

        List<Autor> autoresVivos = autorRepository.findByFechaDeNacimientoLessThanEqualAndFechaDeFallecimientoGreaterThanEqual(
                String.valueOf(anio), String.valueOf(anio));

        if (autoresVivos.isEmpty()) {
            System.out.println("No se encontraron autores vivos en el año " + anio);
        } else {
            autoresVivos.forEach(a -> System.out.println("Autor: " + a.getNombre() + " (Nacimiento: " + a.getFechaDeNacimiento() + ", Fallecimiento: " + a.getFechaDeFallecimiento() + ")"));
        }
    }

    private void listarLibrosPorIdioma() {
        System.out.println("Ingrese el idioma para buscar los libros:");
        System.out.println("es- español");
        System.out.println("en- inglés");
        System.out.println("fr- francés");
        System.out.println("pt- portugués");
        var idioma = teclado.nextLine();

        List<Libro> librosPorIdioma = libroRepository.findByIdioma(idioma);

        if (librosPorIdioma.isEmpty()) {
            System.out.println("No se encontraron libros en el idioma " + idioma);
        } else {
            librosPorIdioma.forEach(System.out::println);
        }
    }
}