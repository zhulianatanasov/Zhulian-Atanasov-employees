package com.sirma.employees.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode
public class PathDto {
    private String format;
    private String delimiter;
    private String path;
}
