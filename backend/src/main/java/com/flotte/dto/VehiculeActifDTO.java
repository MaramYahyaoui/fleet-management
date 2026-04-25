package com.flotte.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculeActifDTO {
	private Long vehiculeId;
	private String immatriculation;
	private String modele;
	private String type;
	private Long nombreMissions;
	private Double distanceTotale;
	private Double coutCarburant;
	private Integer kilometrage;
}
