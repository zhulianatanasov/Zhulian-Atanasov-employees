package com.sirma.employees.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class DataDto {
    private String format;
    private List<EmployeeDto> employees;
}
