#language: es
@Reservas @HU07
Característica: Cancelación de una reserva por el proveedor
  Como proveedor de servicios
  Quiero poder cancelar una reserva existente
  Para gestionar imprevistos en mi agenda

  Antecedentes:
    Dado que el proveedor "salon@bellavida.com" está autenticado

  @CP-07-001 @HappyPath
  Escenario: Cancelación exitosa por parte del proveedor
    Dado que existe la reserva "e3000000-0000-0000-0000-000000000001" para el proveedor
    Cuando el proveedor accede a "Gestión de reservas"
    Y selecciona esa reserva
    Y ingresa el motivo "Emergencia del establecimiento"
    Y confirma la cancelación de la reserva
    Entonces el sistema cambia el estado a "CANCELADA"
    Y notifica al cliente con el motivo de cancelación
    Y libera el horario en la agenda

  @CP-07-002 @Error @ReservaDeOtroProveedor
  Escenario: Intento de cancelar una reserva de otro proveedor
    Dado que la reserva "e3000000-0000-0000-0000-000000000005" pertenece a otro proveedor
    Cuando el proveedor intenta cancelar esa reserva
    Entonces el sistema muestra el error de acceso denegado
    Y no cancela la reserva
    Y el código de respuesta HTTP es 403
