package services;

import models.*;
import repositories.RoomOccupancyDAO;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Servicio que encuentra:
 *   - habitaciones directas (cubren todo el rango pedido)
 *   - "hotel tours" (cadena de habitaciones que juntas cubren el rango)
 */
public class HotelTourService {

    /** Máximo de habitaciones por tour (más de 3 movimientos es incómodo). */
    private static final int MAX_SEGMENTS_PER_TOUR = 3;

    /** Máximo de tours a devolver. */
    private static final int MAX_TOURS = 5;

    private final RoomOccupancyDAO occupancyDAO;

    public HotelTourService() {
        this.occupancyDAO = new RoomOccupancyDAO();
    }

    // ============================================================
    // MÉTODO PRINCIPAL
    // ============================================================
    /**
     * @param checkIn       fecha deseada de entrada
     * @param checkOut      fecha deseada de salida
     * @param minCapacity   huéspedes (para filtrar habitaciones)
     * @param eligibleRooms habitaciones activas ya filtradas por capacidad
     * @return resultado con las habitaciones directas Y los tours posibles
     */
    public HotelTourResult findOptions(LocalDate checkIn,
                                       LocalDate checkOut,
                                       int minCapacity,
                                       List<Room> eligibleRooms) {

        // 1. Traer ocupaciones en el rango
        Map<Integer, List<OccupiedInterval>> occupied =
                occupancyDAO.findOccupiedInRange(checkIn, checkOut);

        // 2. Calcular huecos libres por habitación
        List<TourSegment> allSegments = new ArrayList<>();
        List<Room> directRooms = new ArrayList<>();

        for (Room room : eligibleRooms) {
            if (room.getCapacity() < minCapacity) continue;

            List<OccupiedInterval> roomOccupied =
                    occupied.getOrDefault(room.getNumber(), Collections.emptyList());

            List<TourSegment> roomSegments =
                    computeFreeSegments(room, roomOccupied, checkIn, checkOut);

            allSegments.addAll(roomSegments);

            // ¿Cubre TODO el rango con esta sola habitación?
            for (TourSegment seg : roomSegments) {
                if (seg.getFrom().equals(checkIn) && seg.getTo().equals(checkOut)) {
                    directRooms.add(room);
                    break;
                }
            }
        }

        // 3. Buscar tours (cadenas de 2..MAX_SEGMENTS_PER_TOUR segmentos)
        List<HotelTour> tours = new ArrayList<>();
        if (directRooms.isEmpty()) {
            findToursRecursive(checkIn, checkIn, checkOut,
                    new ArrayList<>(), new HashSet<>(), allSegments, tours);
        }

        // 4. Ordenar tours: menos movimientos, después más barato
        tours.sort(Comparator
                .comparingInt(HotelTour::getMoves)
                .thenComparingDouble(HotelTour::getTotalPrice));

        if (tours.size() > MAX_TOURS) {
            tours = tours.subList(0, MAX_TOURS);
        }

        return new HotelTourResult(directRooms, tours);
    }

    // ============================================================
    // HUECOS LIBRES POR HABITACIÓN
    // ============================================================
    private List<TourSegment> computeFreeSegments(Room room,
                                                  List<OccupiedInterval> occupied,
                                                  LocalDate rangeStart,
                                                  LocalDate rangeEnd) {

        // Ordenar ocupaciones por fecha de inicio
        List<OccupiedInterval> sorted = new ArrayList<>(occupied);
        sorted.sort(Comparator.comparing(OccupiedInterval::getFrom));

        List<TourSegment> segments = new ArrayList<>();
        LocalDate cursor = rangeStart;

        for (OccupiedInterval op : sorted) {
            if (!op.getFrom().isBefore(rangeEnd)) break;

            // Hueco antes de esta ocupación
            if (cursor.isBefore(op.getFrom())) {
                LocalDate gapEnd = op.getFrom().isBefore(rangeEnd) ? op.getFrom() : rangeEnd;
                if (cursor.isBefore(gapEnd)) {
                    segments.add(new TourSegment(room, cursor, gapEnd));
                }
            }

            // Avanzar el cursor
            if (op.getTo().isAfter(cursor)) {
                cursor = op.getTo();
            }
            if (!cursor.isBefore(rangeEnd)) break;
        }

        // Hueco final
        if (cursor.isBefore(rangeEnd)) {
            segments.add(new TourSegment(room, cursor, rangeEnd));
        }

        return segments;
    }

    // ============================================================
    // BÚSQUEDA RECURSIVA DE TOURS (con backtracking y poda)
    // ============================================================
    private void findToursRecursive(LocalDate targetStart,
                                    LocalDate currentDate,
                                    LocalDate finalEnd,
                                    List<TourSegment> currentPath,
                                    Set<Integer> usedRoomNumbers,
                                    List<TourSegment> allSegments,
                                    List<HotelTour> results) {

        // Caso base: ya cubrimos todo
        if (!currentDate.isBefore(finalEnd)) {
            if (currentPath.size() >= 2) {
                results.add(buildTour(currentPath, targetStart, finalEnd));
            }
            return;
        }

        // Poda: demasiados segmentos
        if (currentPath.size() >= MAX_SEGMENTS_PER_TOUR) return;

        // Poda: demasiados resultados ya
        if (results.size() >= MAX_TOURS * 4) return;

        for (TourSegment seg : allSegments) {
            // El segmento debe empezar <= fecha actual (o cubrir la fecha actual)
            if (seg.getFrom().isAfter(currentDate)) continue;
            // El segmento debe terminar > fecha actual (extiende la estadía)
            if (!seg.getTo().isAfter(currentDate)) continue;
            // No reusar habitación
            if (usedRoomNumbers.contains(seg.getRoom().getNumber())) continue;

            // Calcular nueva fecha (capada al final)
            LocalDate newDate = seg.getTo().isAfter(finalEnd) ? finalEnd : seg.getTo();

            currentPath.add(seg);
            usedRoomNumbers.add(seg.getRoom().getNumber());

            findToursRecursive(targetStart, newDate, finalEnd,
                    currentPath, usedRoomNumbers, allSegments, results);

            // Backtracking
            currentPath.remove(currentPath.size() - 1);
            usedRoomNumbers.remove(seg.getRoom().getNumber());
        }
    }

    private HotelTour buildTour(List<TourSegment> path, LocalDate checkIn, LocalDate checkOut) {
        double total = 0;
        for (TourSegment seg : path) total += seg.getSubtotal();
        return new HotelTour(new ArrayList<>(path), checkIn, checkOut, total);
    }

    // ============================================================
    // DTO de resultado
    // ============================================================
    public static class HotelTourResult {
        private final List<Room> directRooms;
        private final List<HotelTour> tours;

        public HotelTourResult(List<Room> directRooms, List<HotelTour> tours) {
            this.directRooms = directRooms;
            this.tours = tours;
        }

        public List<Room> getDirectRooms() { return directRooms; }
        public List<HotelTour> getTours()  { return tours; }

        public boolean hasDirect() { return !directRooms.isEmpty(); }
        public boolean hasTours()  { return !tours.isEmpty(); }
    }
}
