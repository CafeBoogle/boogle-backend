package com.boogle.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CafeWithTagDto {

    private Long id;
    private String name;
    private String address;
    private List<String> tags;
}
