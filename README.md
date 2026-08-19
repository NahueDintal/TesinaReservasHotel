Propuesta para hotel temu.

Programa de tesina con limitaciones de desarrollo, programa de escritorio con base de datos.

La propuesta luego de la entrevista con los dueños y con los requerimientos.

Nuestra idea es pasar a virtualizar el modo que ya tienen, siguiendo la planilla virtual y agregando ciertos requerimientos que no fueron mencionados en la entrevista por el cliente pero que se ajustan a la metodología que ya usan.

Nuestra propuesta comienza con un inicio de sesión, para generar registro de los movimientos de usuario dentro del programa y generar condiciones de acceso.

Una vez iniciada la sesión, el ingreso de los datos para la reserva, para poder proporcionar un estado a la misma, se accede con un icono con un signo “+”, el cual abre la ventana para cargar los datos. Nombre, apellido, DNI, fechas de estadía, medio de pago para la reserva, etc.
Una vez “aceptada” la ventana, queda registrada la reserva en la planilla virtual, la cual es una ventana copia de la planilla física, con las habitaciones, configuración de camas, etc. 
En dicha planilla virtual, se puede prolongar con el mouse el límite de la misma, o haciendo click derecho para volver a la ventana y editar ingresando datos.

Cuando se acceda para ver estado de reservas, poder filtrar por habitaciones disponibles con cucheta, cantidad de habitaciones con uso de cochera, etc. Simplificando la búsqueda de información manteniendo el objetivo del cliente de que sea simple de visualizar. 

El programa deberá funcionar en varias computadoras pero con una base de datos, para que en la computadora de la recepción puedan tomar una reserva y que se actualice en el back office y así minimizar los riesgos de tomar una reserva para la misma fecha.
Tener a primera hora una notificación con todos los check-in y check-out del día. Para saber exactamente la cantidad de personas que van a ingresar y que se van a ir, ya tener impresa la información de las reservas y poder comenzar a gestionar las acciones de las mucamas.

Luego de las acciones cotidianas, vamos agregar queries a la base de datos, con diferentes propósitos, para la toma de decisiones.


%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%


📁 Estructura de archivos proyecto

El proyecto está organizado por función para mantener los archivos ordenados a medida que el sistema crezca.

📂 ¿Qué va en cada carpeta? 

controllers/ → Controllers de JavaFX. Manejan las acciones e interacción de las pantallas. 
Ejemplo: DashboardController.java

models/ → Clases que representan las entidades del sistema.
Ejemplo: Client.java, Reservation.java, Room.java

views/ → Interfaces .fxml creadas con Scene Builder.
Ejemplo: Dashboard.fxml

css/ → Archivos .css utilizados para darle estilo a las interfaces.

icons/ → Imágenes e iconos utilizados por la aplicación.

MainApp.java → Punto de entrada de la aplicación. Se encarga de iniciar JavaFX y la ventana principal.
🔮 Carpetas que se agregarán más adelante

📁A medida que el proyecto crezca, se pueden agregar:

services/ → Lógica de negocio del sistema.

repositories/ → Comunicación con la base de datos.

La idea será mantener una separación de responsabilidades: Controller → Service → Repository → Base de datos

📌 Regla general:
Antes de crear una nueva carpeta o colocar un archivo fuera de su ubicación habitual, consultar la estructura del proyecto para mantener la organización entre todos los integrantes.