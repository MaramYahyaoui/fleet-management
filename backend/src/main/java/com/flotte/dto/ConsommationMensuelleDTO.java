package com.flotte.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsommationMensuelleDTO {
	private int annee;
	private int mois;
	private String moisLibelle;
	private Double coutTotal;
	private Double quantiteTotale;
}
