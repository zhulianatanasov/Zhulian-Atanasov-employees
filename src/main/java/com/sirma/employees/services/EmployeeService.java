package com.sirma.employees.services;

import com.sirma.employees.dtos.DataDto;
import com.sirma.employees.dtos.EmployeeDto;
import com.sirma.employees.dtos.PairDto;

import java.util.List;

public interface EmployeeService {

    List<PairDto> findPairs(final List<EmployeeDto> employeeDtos, final String format);
}
