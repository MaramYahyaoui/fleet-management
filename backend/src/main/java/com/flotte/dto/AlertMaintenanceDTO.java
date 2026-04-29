package com.flotte.dto;

import lombok.*;

@Getter 
@Setter 
@NoArgsConstructor
@AllArgsConstructor 
@Builder
public class AlertMaintenanceDTO {
    private Long vehiculeId;
    private String immatriculation;
    private String modele;
    private Integer kilometrageActuel;
    private Integer seuilMaintenance;
    private Integer depassement;
    private String niveauAlerte; // CRITIQUE, ATTENTION, INFO
}
