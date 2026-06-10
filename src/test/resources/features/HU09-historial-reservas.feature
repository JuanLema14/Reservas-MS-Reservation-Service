#language: es
@Historial @HU09
Característica: Historial de reservas del cliente
  Como cliente
  Quiero ver el historial de todas mis reservas
  Para llevar un registro de los servicios que he utilizado

  Antecedentes:
    Dado que el cliente "carlos@email.com" está autenticado

  @CP-09-001 @HappyPath
  Escenario: Visualización del historial completo de reservas
    Dado que el cliente tiene reservas registradas en el sistema
    Cuando el cliente accede a la sección "Mis reservas"
    Entonces el sistema muestra una lista con todas sus reservas
    Y cada reserva muestra: servicio, proveedor, fecha, hora y estado
    Y el historial incluye reservas en estado "CONFIRMADA"
    Y el historial incluye reservas en estado "COMPLETADA"
    Y el historial incluye reservas en estado "CANCELADA"

  @CP-09-002 @HappyPath @Filtrado
  Escenario: Filtrado del historial por estado Cancelada
    Dado que el cliente tiene reservas en distintos estados
    Cuando el cliente aplica el filtro de estado "CANCELADA"
    Entonces el sistema muestra únicamente las reservas con estado "CANCELADA"
    Y no muestra reservas con otros estados

  @CP-09-003 @HappyPath @Filtrado
  Escenario: Filtrado del historial por estado Confirmada
    Dado que el cliente tiene reservas en distintos estados
    Cuando el cliente aplica el filtro de estado "CONFIRMADA"
    Entonces el sistema muestra únicamente las reservas con estado "CONFIRMADA"
    Y no muestra reservas con otros estados

  @CP-09-004 @HistorialVacio
  Escenario: Historial vacío para cliente sin reservas
    Dado que el cliente "sin.reservas@email.com" no ha realizado ninguna reserva
    Cuando accede a "Mis reservas"
    Entonces el sistema muestra el mensaje "Aún no tienes reservas registradas"
    Y muestra un botón "Explorar servicios"
