package com.flotte.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsommationDTO {

    private Long id;

    @NotNull(message = "L'ID du véhicule est obligatoire")
    private Long vehiculeId;

    private String vehiculeImmatriculation;

    @NotNull(message = "La date est obligatoire")
    private LocalDate date;

    @Positive(message = "La quantité de carburant doit être positive")
    private Double quantiteCarburant;

    @Positive(message = "Le coût total doit être positif")
    private Double coutTotal;
}
