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
                //System.out.println("\n"+f);
            });

            String airlineName = "Lufthansa";
            List<DTOs.FlightInfo> airlineList = getAirlineList(flightInfoList, airlineName);
            Duration averageFlightTime = calculateAverageFlightTime(airlineList);
            displayAverageFlightTime(averageFlightTime);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


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

}
