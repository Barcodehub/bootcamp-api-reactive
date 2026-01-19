package com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "bootcamp_capacity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BootcampCapacityEntity {
    @Id
    private Long id;
    private Long bootcampId;
    private Long capacityId;
}

