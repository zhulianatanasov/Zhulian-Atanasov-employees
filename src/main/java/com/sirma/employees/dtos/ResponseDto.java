package com.sirma.employees.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ResponseDto {
    private List<PairDto> pairList;
    private List<EmployeeDto> csv;
}
