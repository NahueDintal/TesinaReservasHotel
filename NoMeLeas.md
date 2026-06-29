Proyecto de tesina, para segundo año, donde se desarrollara un sistema para reservas.

config de vm para el run config

--module-path /home/nahue/javafx-sdk-17.0.19/lib --add-modules javafx.controls,javafx.fxml,javafx.graphics,javafx.base

Y con el Proyect Structure, hay que poner la ruta donde está el java fx, y luego elegir todos los .jar para que funcione.

sistema-reservas-hoteles/
├── src/
│   └── main/
│       ├── java/
│       │   └── com.tesis.hotelreservas/          # Paquete base (tu dominio)
│       │       ├── MainApp.java                  # Punto de entrada (JavaFX)
│       │       │
│       │       ├── modelo/                       # Capa de dominio (entidades)
│       │       │   ├── Client.java
│       │       │   ├── Hotel.java
│       │       │   ├── Room.java
│       │       │   ├── Reservation.java
│       │       │   ├── User.java
│       │       │   └── enums/                    # Tipos fijos (estados, tipos)
│       │       │       ├── ReservationStatus.java  (EN PROCESO, PAGADO, CHECK_IN, etc.)
│       │       │       ├── RoomType.java
│       │       │       └── UserRole.java
│       │       │
│       │       ├── dao/                          # Data Access Objects (Persistencia)
│       │       │   ├── ClientDAO.java
│       │       │   ├── HotelDAO.java
│       │       │   ├── RoomDAO.java
│       │       │   ├── ReservationDAO.java
│       │       │   ├── UserDAO.java
│       │       │   └── DBConnection.java         # Conexión a BD (SQLite/MySQL)
│       │       │
│       │       ├── servicio/                     # Lógica de negocio (Servicios)
│       │       │   ├── ReservationService.java   (crear, cancelar, modificar, calcular total)
│       │       │   ├── HotelService.java         (búsquedas, disponibilidad)
│       │       │   ├── ClientService.java
│       │       │   ├── RoomService.java
│       │       │   ├── AuthService.java          (login, logout, roles)
│       │       │   └── PaymentService.java       (simulación de pagos, estados)
│       │       │
│       │       ├── controlador/                  # Controladores JavaFX (Vista ↔ Modelo)
│       │       │   ├── DashboardController.java
│       │       │   ├── MonthlyGridController.java   (lógica de la planilla)
│       │       │   ├── ReservationFormController.java
│       │       │   ├── ClientFormController.java
│       │       │   ├── LoginController.java
│       │       │   └── HotelListController.java
│       │       │
│       │       ├── vista/                        # Vistas (archivos FXML de Scene Builder)
│       │       │   ├── dashboard.fxml
│       │       │   ├── monthlyGrid.fxml
│       │       │   ├── reservationForm.fxml
│       │       │   ├── clientForm.fxml
│       │       │   ├── login.fxml
│       │       │   └── hotelList.fxml
│       │       │
│       │       └── util/                         # Utilidades transversales
│       │           ├── DateUtils.java            (formateo, cálculos de fechas)
│       │           ├── AlertUtils.java           (alertas de confirmación/error)
│       │           ├── ColorMapper.java          (mapeo de estados a colores)
│       │           └── DragDropUtils.java        (funciones reutilizables para drag & drop)
│       │
│       └── resources/                            # Recursos adicionales
│           ├── css/
│           │   └── styles.css                    (estilos globales)
│           ├── images/                           (logos, iconos)
│           └── database/
│               └── hotel.db                      (si usas SQLite, aquí el archivo)
│
├── lib/
│   └── mySql
│
├── database/                                     (alternativa: la BD fuera de src)
│   └── hotel.db
│
├── pom.xml                                       (si usas Maven)
├── README.md
└── .gitignore
