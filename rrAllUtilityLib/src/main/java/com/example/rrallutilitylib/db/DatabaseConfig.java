package com.example.rrallutilitylib.db;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder
public class DatabaseConfig {
    String url;
    String username;
    String password;
    String driverClassName;
}