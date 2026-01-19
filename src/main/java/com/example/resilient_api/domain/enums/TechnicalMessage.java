package com.example.resilient_api.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {

    INTERNAL_ERROR("500","Something went wrong, please try again", ""),
    INVALID_REQUEST("400", "Bad Request, please verify data", ""),
    INVALID_PARAMETERS("400", "Bad Parameters, please verify data", ""),
    UNSUPPORTED_OPERATION("501", "Method not supported, please try again", ""),
    TECHNOLOGY_CREATED("201", "Bootcamp created successfully", ""),
    TECHNOLOGY_ALREADY_EXISTS("400", "Bootcamp with this name already exists", "name"),
    TECHNOLOGY_NAME_REQUIRED("400", "Bootcamp name is required", "name"),
    TECHNOLOGY_DESCRIPTION_REQUIRED("400", "Bootcamp description is required", "description"),
    TECHNOLOGY_NAME_TOO_LONG("400", "Bootcamp name cannot exceed 50 characters", "name"),
    TECHNOLOGY_DESCRIPTION_TOO_LONG("400", "Bootcamp description cannot exceed 90 characters", "description"),
    BOOTCAMP_CAPACITIES_REQUIRED("400", "Bootcamp must have at least 1 capacity", "capacityIds"),
    BOOTCAMP_CAPACITIES_MIN("400", "Bootcamp must have at least 1 capacity", "capacityIds"),
    BOOTCAMP_CAPACITIES_MAX("400", "Bootcamp cannot have more than 4 capacities", "capacityIds"),
    BOOTCAMP_CAPACITIES_DUPLICATED("400", "Bootcamp cannot have duplicate capacities", "capacityIds"),
    BOOTCAMP_LAUNCH_DATE_REQUIRED("400", "Bootcamp launch date is required", "launchDate"),
    BOOTCAMP_LAUNCH_DATE_PAST("400", "Bootcamp launch date cannot be in the past", "launchDate"),
    BOOTCAMP_DURATION_INVALID("400", "Bootcamp duration must be at least 1 day", "duration"),
    BOOTCAMP_NOT_FOUND("404", "Bootcamp not found", "id"),
    BOOTCAMP_DELETED("200", "Bootcamp deleted successfully", ""),
    CAPACITY_TECHNOLOGIES_REQUIRED("400", "Bootcamp must have at least 3 capacities", "capacityIds"),
    CAPACITY_TECHNOLOGIES_MIN("400", "Bootcamp must have at least 3 capacities", "capacityIds"),
    CAPACITY_TECHNOLOGIES_MAX("400", "Bootcamp cannot have more than 20 capacities", "capacityIds"),
    CAPACITY_TECHNOLOGIES_DUPLICATED("400", "Bootcamp cannot have duplicate capacities", "capacityIds"),
    TECHNOLOGIES_NOT_FOUND("400", "Some capacities do not exist", "capacityIds"),
    TECHNOLOGY_SERVICE_ERROR("500", "Error communicating with capacity service", "")
    ;

    private final String code;
    private final String message;
    private final String param;
}

