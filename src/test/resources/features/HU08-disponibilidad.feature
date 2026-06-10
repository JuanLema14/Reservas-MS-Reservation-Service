#language: es
@Disponibilidad @HU08
Característica: Consulta de disponibilidad en tiempo real
  Como cliente
  Quiero ver la disponibilidad actualizada de un servicio
  Para elegir el horario correcto sin conflictos

  Antecedentes:
    Dado que el cliente "carlos@email.com" está autenticado

  @CP-08-001 @HappyPath
  Escenario: Visualización correcta de horarios disponibles e indisponibles
    Dado que el servicio "Corte y peinado" existe en el catálogo
    Y la fecha seleccionada es "2026-08-20"
    Cuando el sistema consulta la disponibilidad para esa fecha
    Entonces muestra los horarios disponibles correctamente
    Y muestra los horarios ocupados correctamente

  @CP-08-002 @Error @SinDisponibilidad
  Escenario: Sin disponibilidad en la fecha seleccionada
    Dado que todos los horarios del empleado "Ana Estilista" están bloqueados el "2026-07-15"
    Cuando el cliente selecciona esa fecha para el servicio "Corte y peinado"
    Entonces el sistema muestra el mensaje "No hay disponibilidad para esta fecha"
    Y sugiere las próximas fechas disponibles
