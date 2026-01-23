package com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table(name = "bootcamp_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BootcampUserEntity {
    @Id
    private Long id;
    private Long bootcampId;
    private Long userId;
    private LocalDateTime enrolledAt;
}
