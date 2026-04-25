package com.flotte.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChauffeurDTO {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le numéro de permis est obligatoire")
    private String permis;

    @Min(value = 0) @Max(value = 60)
    private Integer experience;

    private Long nombreMissions;
    private Double distanceTotale;
}
