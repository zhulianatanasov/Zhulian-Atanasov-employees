package com.sirma.employees.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class EmployeeDto {
    private Long empID;
    private Integer projectID;
    private String dateFrom;
    private String dateTo;

    public static EmployeeDto from(final List<String> record)
    {
        try {
            return new EmployeeDto(Long.valueOf(record.get(0)), Integer.valueOf(record.get(1)),
                    record.get(2), record.get(3));
        } catch (final Exception e) {
            //throw new InvalidCsvDataException("Invalid csv row -> " + String.join(", ", record), e);
            //or log
            //...
            return null;
        }
    }
}
