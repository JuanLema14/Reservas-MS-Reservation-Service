#language: es
@Reservas @HU05
Característica: Creación de una reserva
  Como cliente registrado
  Quiero crear una reserva para un servicio
  Para asegurar mi cita en el horario que prefiero

  Antecedentes:
    Dado que el cliente "carlos@email.com" está autenticado

  @CP-05-001 @HappyPath
  Escenario: Reserva exitosa de un servicio disponible
    Dado que el servicio "Corte y peinado" tiene disponibilidad en la fecha "2026-08-20T10:00:00-05:00"
    Cuando el cliente busca el servicio "Corte y peinado"
    Y selecciona la fecha "2026-08-20T10:00:00-05:00"
    Y selecciona el empleado disponible
    Y confirma la reserva
    Entonces el sistema registra la reserva con estado "CONFIRMADA"
    Y envía una notificación de confirmación al cliente
    Y notifica al proveedor sobre la nueva reserva
    Y el código de respuesta HTTP es 201

  @CP-05-002 @Error @HorarioOcupado
  Escenario: Intento de reserva en horario no disponible
    Dado que el horario "2026-07-15T09:00:00-05:00" del empleado "Ana Estilista" ya está ocupado
    Cuando el cliente selecciona ese mismo horario
    Y el cliente confirma la reserva
    Entonces el sistema muestra el mensaje "Este horario ya no está disponible"
    Y sugiere horarios alternativos disponibles
    Y el código de respuesta HTTP es 409

  @CP-05-003 @Error @SinAutenticacion
  Escenario: Intento de reserva sin autenticación
    Dado que el usuario no ha iniciado sesión en la plataforma
    Cuando intenta reservar el servicio "Corte y peinado"
    Entonces el sistema lo redirige a la pantalla de inicio de sesión
    Y muestra el mensaje "Debes iniciar sesión para hacer una reserva"
    Y el código de respuesta HTTP es 401
