#language: es
@Reservas @HU06
Característica: Cancelación de una reserva por el cliente
  Como cliente
  Quiero cancelar una reserva que hice
  Para liberar el horario cuando ya no puedo asistir

  Antecedentes:
    Dado que el cliente "carlos@email.com" está autenticado

  @CP-06-001 @HappyPath
  Escenario: Cancelación exitosa dentro del tiempo permitido
    Dado que el cliente tiene la reserva "e3000000-0000-0000-0000-000000000001" confirmada
    Y la reserva es más de 24 horas en el futuro
    Cuando el cliente accede a "Mis reservas"
    Y selecciona esa reserva
    Y confirma la cancelación con comentario "No puedo asistir"
    Entonces el sistema cambia el estado de la reserva a "CANCELADA"
    Y libera el horario para otros clientes
    Y envía una notificación de cancelación al proveedor

  @CP-06-002 @Error @CancelacionFueraDeTiempo
  Escenario: Cancelación fuera del tiempo permitido (menos de 24 horas)
    Dado que el cliente tiene la reserva "e3000000-0000-0000-0000-000000000002" confirmada
    Y la reserva es menos de 24 horas en el futuro
    Cuando el cliente intenta cancelar la reserva
    Entonces el sistema muestra el mensaje de cancelación fuera de tiempo
    Y no procesa la cancelación

  @CP-06-003 @Error @ReservaNoExiste
  Escenario: Intento de cancelar una reserva inexistente
    Dado que no existe una reserva con ID "00000000-0000-0000-0000-000000000000"
    Cuando el cliente intenta cancelar esa reserva
    Entonces el sistema muestra el error de reserva no encontrada
    Y el código de respuesta HTTP es 404
