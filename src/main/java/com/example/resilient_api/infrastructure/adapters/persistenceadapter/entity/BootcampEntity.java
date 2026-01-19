package com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table(name = "bootcamp")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BootcampEntity {
    @Id
    private Long id;
    private String name;
    private String description;
    private LocalDate launchDate;
    private Integer duration;
}

