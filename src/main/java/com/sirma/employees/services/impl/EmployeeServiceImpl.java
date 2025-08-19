package com.sirma.employees.services.impl;

import com.sirma.employees.dtos.EmployeeDto;
import com.sirma.employees.dtos.PairDto;
import com.sirma.employees.services.EmployeeService;
import org.joda.time.Interval;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private double DAY_IN_MILLIS = 86400000.0;

    @Override
    public List<PairDto> findPairs(final List<EmployeeDto> employeeDtoList, final String format)
    {
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);

        //Group all records by ProjectID
        final Map<Integer, List<EmployeeDto>> employeesPerProject = employeeDtoList.stream().collect(Collectors.groupingBy(EmployeeDto::getProjectID));

        //Create pairs of records in each project bucket and return only pairs with time overlap
        final Map<Integer, List<PairDto>> pairsPerProject = generatePairs(employeesPerProject, dateTimeFormatter);

        //Sort and get the the first pair with the longest overlap
        return pairsPerProject.values().stream()
                .map(pairs -> pairs.stream()
                        .sorted(Comparator.comparing(PairDto::getDays).reversed())
                        .findFirst().orElse(null))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Map<Integer, List<PairDto>> generatePairs(final Map<Integer, List<EmployeeDto>> employeesPerProject, final DateTimeFormatter dateTimeFormatter)
    {
        final long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        final Map<Integer, List<PairDto>> pairsPerProject = new HashMap<>(employeesPerProject.size());

        employeesPerProject.keySet().forEach(key -> {
            final List<EmployeeDto> employees = employeesPerProject.get(key);
            final List<PairDto> pairs = new ArrayList<>();
            for (int i = 0; i < employees.size()-1; i++)
                for (int j = i + 1; j < employees.size(); j++) {
                    final EmployeeDto employee1 = employees.get(i);
                    final EmployeeDto employee2 = employees.get(j);

                    long employee1StartDate = getEpochMilisFromDateString(employee1.getDateFrom(), dateTimeFormatter);
                    long employee2StartDate = getEpochMilisFromDateString(employee2.getDateFrom(), dateTimeFormatter);

                    long employee1EndDate = getEpochMilisFromDateString(employee1.getDateTo(), dateTimeFormatter);
                    long employee2EndDate = getEpochMilisFromDateString(employee2.getDateTo(), dateTimeFormatter);

                    long days = getDaysIfAny(employee1StartDate, employee1EndDate, employee2StartDate, employee2EndDate);
                    if (days > 0) {
                        final PairDto pairDto = new PairDto();
                        pairDto.setEmpID1(employee1.getEmpID());
                        pairDto.setEmpID2(employee2.getEmpID());
                        pairDto.setProjectID(employee1.getProjectID());
                        pairDto.setDays(days);
                        pairs.add(pairDto);
                    }
                }

            pairsPerProject.put(key, pairs);
        });
        return pairsPerProject;
    }

    private long getDaysIfAny(long employee1StartDate, long employee1EndDate, long employee2StartDate, long employee2EndDate)
    {
        final org.joda.time.Instant startInterval1 = new org.joda.time.Instant(employee1StartDate);
        final org.joda.time.Instant endInterval1 = new org.joda.time.Instant(employee1EndDate);
        final Interval interval1 = new Interval(startInterval1, endInterval1);

        final org.joda.time.Instant startInterval2 = new org.joda.time.Instant(employee2StartDate);
        final org.joda.time.Instant endInterval2 = new org.joda.time.Instant(employee2EndDate);
        final Interval interval2 = new Interval(startInterval2, endInterval2);

        final Interval overlap = interval1.overlap(interval2);
        return overlap != null ? Math.round(Math.ceil(overlap.toDurationMillis() / DAY_IN_MILLIS)) : 0;
    }

    private long getEpochMilisFromDateString(String date, final DateTimeFormatter dateTimeFormatter)
    {
        return ("".equals(date) || "NULL".equals(date)) ?
                LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() :
                LocalDate.parse(date, dateTimeFormatter).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}
