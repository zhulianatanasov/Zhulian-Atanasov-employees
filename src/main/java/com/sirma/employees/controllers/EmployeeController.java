package com.sirma.employees.controllers;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import com.sirma.employees.dtos.*;
import com.sirma.employees.services.EmployeeService;
import com.sirma.utils.CommonUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sirma.utils.CommonUtils.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/v1/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/import/json")
    public ResponseEntity<?> fromJson(@RequestBody DataDto dataDto)
    {
        final List<PairDto> pairList = employeeService.findPairs(dataDto.getEmployees(), dataDto.getFormat());
        return new ResponseEntity<>(new ResponseDto(pairList, dataDto.getEmployees()), HttpStatus.OK);
    }

    @PostMapping("/import/csv/{format}/{separator}")
    public ResponseEntity<?> fromCsv(@PathVariable(name = "format") String format, @PathVariable(name = "separator") Character separator, HttpServletRequest request)
    {
        try {
            final Character delimiter = separatorToDelimiter(separator);

            final InputStream inputStream = ((Part)request.getParts().toArray()[0]).getInputStream();
            final CSVReader csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream))
                    .withCSVParser(new CSVParserBuilder().withSeparator(delimiter).build())
                    .build();
            final List<EmployeeDto> employeeList = new ArrayList<>();
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length == 4 && NumberUtils.isCreatable(trim(line[0]))) { // to skip header or dummy if any
                    employeeList.add(EmployeeDto.from(List.of(line)));
                }
            }
            final List<PairDto> pairList = employeeService.findPairs(employeeList.stream().filter(Objects::nonNull).toList(), format);
            return new ResponseEntity<>(new ResponseDto(pairList, employeeList), HttpStatus.OK);
        } catch (final IOException | ServletException | CsvValidationException e) {
            e.printStackTrace();
            return new ResponseEntity<> ("Exception -> " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/import/path")
    public ResponseEntity<?> fromPath(@RequestBody PathDto pathDto)
    {
        try (final Stream<String> lines = Files.lines(Paths.get(pathDto.getPath()))) {
            final List<List<String>> records = lines.map(line -> Arrays.asList(line.split(pathDto.getDelimiter())))
                    .collect(Collectors.toList());
            final List<EmployeeDto> employeeList = records.stream()
                    .filter(r -> !r.isEmpty() && NumberUtils.isCreatable(trim(r.get(0))))
                    .map(EmployeeDto::from)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            final List<PairDto> pairList = employeeService.findPairs(employeeList, pathDto.getFormat());
            return new ResponseEntity<>(new ResponseDto(pairList, employeeList), HttpStatus.OK);
        } catch (final IOException ioException) {
            ioException.printStackTrace();
            return new ResponseEntity<>(ioException.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping(value = "/import/text/{format}/{separator}")
    public ResponseEntity<?> fromPath(@RequestBody String csv, @PathVariable(name = "format") String format, @PathVariable(name = "separator") Character separator)
    {
        final Character delimiter = separatorToDelimiter(separator);
        final String[] lines = csv.split("\n");
        final List<List<String>> records = Arrays.stream(lines).toList().stream().map(line -> Arrays.asList(line.split(String.valueOf(delimiter))))
                .collect(Collectors.toList());
        final List<EmployeeDto> employeeList = records.stream()
                .filter(r -> !r.isEmpty() && NumberUtils.isCreatable(trim(r.get(0))))
                .map(EmployeeDto::from)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        final List<PairDto> pairList = employeeService.findPairs(employeeList, format);
        return new ResponseEntity<>(new ResponseDto(pairList, employeeList), HttpStatus.OK);
    }

}
