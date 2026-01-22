package com.fitness.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format ")
    private String  email;
    @Email(message = "Password is required ")
    @Size(min = 6,message = "password must have at-least of 6 characters")
    private String Password ;
    private String firstName;
    private String lastName;



}
