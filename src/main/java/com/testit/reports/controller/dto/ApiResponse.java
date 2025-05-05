package com.testit.reports.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Create a success response
     *
     * @param message Success message
     * @param data    Response data
     * @param <T>     Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Create a success response with data
     *
     * @param data Response data
     * @param <T>  Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return success("Operation successful", data);
    }
    
    /**
     * Create a success response with message
     *
     * @param message Success message
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(message, null);
    }
    
    /**
     * Create an error response
     *
     * @param message Error message
     * @param <T>     Data type
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
