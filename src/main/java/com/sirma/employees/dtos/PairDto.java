package com.sirma.employees.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PairDto {
    private Long empID1;
    private Long empID2;
    private Integer projectID;
    private Long days;
}
