package me.shaposhnik.hlrbot.integration.bsg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiError implements Serializable {
    private int error;
    private String errorDescription;
}
