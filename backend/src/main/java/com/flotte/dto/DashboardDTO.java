package com.flotte.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDTO {
	private Long totalVehicules;
	private Long vehiculesDisponibles;
	private Long vehiculesEnMission;
	private Long vehiculesEnMaintenance;
	private Long totalChauffeurs;
	private Long missionsEnCours;
	private Long totalMissions;
	private Double coutCarburantTotal;
	private Double distanceTotale;
	private List<AlertMaintenanceDTO> alertesMaintenance;
}
