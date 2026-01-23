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
    TECHNOLOGY_SERVICE_ERROR("500", "Error communicating with capacity service", ""),
    USER_SERVICE_ERROR("500", "Error communicating with user service", ""),
    USER_NOT_FOUND("404", "User not found", "userId"),
    USER_ALREADY_ENROLLED("400", "User is already enrolled in this bootcamp", "userId"),
    MAX_BOOTCAMPS_REACHED("400", "User cannot enroll in more than 5 bootcamps", "userId"),
    BOOTCAMP_DATE_CONFLICT("400", "User is already enrolled in a bootcamp with conflicting dates", "bootcampId"),
    ENROLLMENT_NOT_FOUND("404", "Enrollment not found", "enrollmentId"),
    ENROLLMENT_CREATED("201", "User enrolled successfully", ""),
    ENROLLMENT_DELETED("200", "User unenrolled successfully", ""),
    TOKEN_EXPIRED("401", "Authentication token has expired", "token"),
    TOKEN_INVALID("401", "Invalid authentication token", "token"),
    TOKEN_REQUIRED("401", "Authentication token is required", "Authorization"),
    UNAUTHORIZED_ACTION("403", "You are not authorized to perform this action", "")
    ;

    private final String code;
    private final String message;
    private final String param;
}

