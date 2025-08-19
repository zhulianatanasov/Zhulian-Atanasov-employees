package com.sirma.employees.controllers;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import com.sirma.employees.dtos.DataDto;
import com.sirma.employees.dtos.EmployeeDto;
import com.sirma.employees.dtos.PathDto;
import com.sirma.employees.services.EmployeeService;
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

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/v1/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/import/json")
    public ResponseEntity<?> fromJson(@RequestBody DataDto dataDto)
    {
        return new ResponseEntity<>(employeeService.findPairs(dataDto.getEmployees(), dataDto.getFormat()), HttpStatus.OK);
    }

    @PostMapping("/import/csv/{format}")
    public ResponseEntity<?> fromCsv(@PathVariable(name = "format") String format, HttpServletRequest request)
    {
        try {
            final InputStream inputStream = ((Part)request.getParts().toArray()[0]).getInputStream();
            final CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream));
            String[] line;
            final List<EmployeeDto> employeeList = new ArrayList<>();
            while ((line = csvReader.readNext()) != null) {
                if (line.length == 4 && NumberUtils.isCreatable(line[0])) { // to skip header or dummy if any
                    employeeList.add(EmployeeDto.from(List.of(line)));
                }
            }
            return new ResponseEntity<>(employeeService.findPairs(employeeList.stream().filter(Objects::nonNull).toList(), format), HttpStatus.OK);
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
                    .filter(r -> !r.isEmpty() && NumberUtils.isCreatable(r.get(0)))
                    .map(EmployeeDto::from)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(employeeService.findPairs(employeeList, pathDto.getFormat()), HttpStatus.OK);
        } catch (final IOException ioException) {
            ioException.printStackTrace();
            return new ResponseEntity<>(ioException.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

}
