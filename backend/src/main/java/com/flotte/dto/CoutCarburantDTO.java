package com.flotte.dto;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CoutCarburantDTO {
    private Long vehiculeId;
    private String immatriculation;
    private String modele;
    private Double coutTotal;
    private Double quantiteTotale;
    private Long nombrePleins;
}
