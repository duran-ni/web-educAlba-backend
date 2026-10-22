package dev.duran.web_educAlba_backend.academy.student;

// Datos de entrada para editar el titulo y la descripcion de un material ya
// subido. El archivo en si no se puede editar por aqui: para cambiarlo hay
// que eliminar el material y subir uno nuevo
public record MaterialDetailsRequest(
    String title,
    String description
) {

}
