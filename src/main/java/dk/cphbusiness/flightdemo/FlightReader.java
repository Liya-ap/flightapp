package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dk.cphbusiness.utils.Utils;
import lombok.*;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader {

    public static void main(String[] args) {
        FlightReader flightReader = new FlightReader();
        try {
            List<DTOs.FlightDTO> flightList = flightReader.getFlightsFromFile("flights.json");
            List<DTOs.FlightInfo> flightInfoList = flightReader.getFlightInfoDetails(flightList);
            flightInfoList.forEach(f->{
                System.out.println("\n"+f);
            });

            String airlineName = "Lufthansa";
            List<DTOs.FlightInfo> airlineList = getAirlineList(flightInfoList, airlineName);
            Duration averageFlightTime = calculateAverageFlightTime(airlineList);
            displayAverageFlightTime(averageFlightTime);

            String origin = "Pulkovo";
            String destination = "Arkhangelsk";
            List<DTOs.FlightDTO> flights = flightReader.getFlightsBetweenAirports(flightList, origin, destination);
            flights.forEach(f->{
                System.out.println("\n"+f);
            });

            String airline = "Lufthansa";
            int totalFlightTime = flightReader.calculateTotalFlightTime(flightInfoList, airline);
            System.out.println("\n" + "Den samlede tid for airline: " + airline + " Er: " + totalFlightTime + " Minutter");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    //    public List<FlightDTO> jsonFromFile(String fileName) throws IOException {
//        List<FlightDTO> flights = getObjectMapper().readValue(Paths.get(fileName).toFile(), List.class);
//        return flights;
//    }

    public List<DTOs.FlightInfo> getFlightInfoDetails(List<DTOs.FlightDTO> flightList) {
        List<DTOs.FlightInfo> flightInfoList = flightList.stream().map(flight -> {
            Duration duration = Duration.between(flight.getDeparture().getScheduled(), flight.getArrival().getScheduled());
            DTOs.FlightInfo flightInfo = DTOs.FlightInfo.builder()
                    .name(flight.getFlight().getNumber())
                    .iata(flight.getFlight().getIata())
                    .airline(flight.getAirline().getName())
                    .duration(duration)
                    .departure(flight.getDeparture().getScheduled().toLocalDateTime())
                    .arrival(flight.getArrival().getScheduled().toLocalDateTime())
                    .origin(flight.getDeparture().getAirport())
                    .destination(flight.getArrival().getAirport())
                    .build();

            return flightInfo;
        }).toList();
        return flightInfoList;
    }

    public List<DTOs.FlightDTO> getFlightsFromFile(String filename) throws IOException {
        DTOs.FlightDTO[] flights = new Utils().getObjectMapper().readValue(Paths.get(filename).toFile(), DTOs.FlightDTO[].class);

        List<DTOs.FlightDTO> flightList = Arrays.stream(flights).toList();
        return flightList;
    }

    public List<DTOs.FlightDTO> getFlightsBetweenAirports(List<DTOs.FlightDTO> flightList, String origin, String destination) {
        List<DTOs.FlightDTO> flights = flightList.stream()
                .filter(flight -> flight != null
                        && flight.getDeparture() != null
                        && flight.getDeparture().getAirport() != null
                        && flight.getDeparture().getAirport().equals(origin)
                        && flight.getArrival() != null
                        && flight.getArrival().getAirport() != null
                        && flight.getArrival().getAirport().equals(destination))
                .toList();
        return flights;
    }


    public static Duration calculateAverageFlightTime(List<DTOs.FlightInfo> flightInfoList){
        List<Duration> durationOfFlightList = flightInfoList.stream().map(DTOs.FlightInfo::getDuration).toList();
        OptionalDouble averageSeconds = durationOfFlightList.stream().mapToLong(Duration::getSeconds).average();
        Duration flightTime = null;

        if (averageSeconds.isPresent()){
            flightTime = Duration.ofSeconds((long) averageSeconds.getAsDouble());
        }

        return flightTime;
    }

    public static void displayAverageFlightTime(Duration flightTime){
        System.out.println(flightTime);
    }

    public static List<DTOs.FlightInfo> getAirlineList(List<DTOs.FlightInfo> flightInfoList, String airlineName){
        return flightInfoList.stream().filter(flightInfo -> Objects.equals(flightInfo.getAirline(), airlineName)).toList();
    }

    public int calculateTotalFlightTime(List<DTOs.FlightInfo> flightInfoList, String airline) {
        return flightInfoList.stream()
                .filter(flight -> flight != null && flight.getAirline() != null && flight.getAirline().equals(airline))
                .filter(flight -> flight.getDuration() != null)
                .mapToInt(flight -> (int) flight.getDuration().toMinutes())
                .sum();

    }

}
