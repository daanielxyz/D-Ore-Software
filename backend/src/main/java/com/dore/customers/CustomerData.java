package com.dore.customers;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerData(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 40) String phone,
        @Size(max = 120) String email,
        @Size(max = 300) String notes) {
}
