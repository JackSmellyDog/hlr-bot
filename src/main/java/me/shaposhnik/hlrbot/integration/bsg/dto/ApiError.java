package me.shaposhnik.hlrbot.integration.bsg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
public class ApiError implements Serializable {
    private int error;
    private String errorDescription;
}
