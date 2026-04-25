package com.flotte.controller;

import com.flotte.dto.ApiResponseDTO;
import com.flotte.dto.DashboardDTO;
import com.flotte.dto.VehiculeActifDTO;
import com.flotte.service.ReportingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/reporting")
@RequiredArgsConstructor
@Tag(name = "Reporting", description = "Tableaux de bord et statistiques globales de la flotte")
public class ReportingController {

    private final ReportingService reportingService;

    // ─────────────────────────────────────────────────────────────
    //  DASHBOARD GLOBAL
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Tableau de bord global",
        description = "Retourne les indicateurs clés de la flotte : nombre de véhicules, missions en cours, " +
                      "coût carburant total, alertes de maintenance, etc."
    )
    @ApiResponse(responseCode = "200", description = "Dashboard récupéré",
        content = @Content(schema = @Schema(implementation = DashboardDTO.class)))
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDTO<DashboardDTO>> getDashboard() {
        return ResponseEntity.ok(ApiResponseDTO.ok(reportingService.getDashboardGlobal()));
    }

    // ─────────────────────────────────────────────────────────────
    //  FLOTTE ACTIVE
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Liste de la flotte active",
        description = "Retourne tous les véhicules actuellement actifs avec leurs statistiques d'utilisation."
    )
    @ApiResponse(responseCode = "200", description = "Flotte active récupérée",
        content = @Content(schema = @Schema(implementation = VehiculeActifDTO.class)))
    @GetMapping("/flotte-active")
    public ResponseEntity<ApiResponseDTO<List<VehiculeActifDTO>>> getFLotteActive() {
        return ResponseEntity.ok(ApiResponseDTO.ok(reportingService.getFLotteActive()));
    }

    // ─────────────────────────────────────────────────────────────
    //  TOP 5 VEHICULES
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Top 5 des véhicules les plus utilisés",
        description = "Retourne les 5 véhicules ayant effectué le plus de missions, avec distance totale et coût carburant."
    )
    @ApiResponse(responseCode = "200", description = "Top 5 récupéré")
    @GetMapping("/top5-vehicules")
    public ResponseEntity<ApiResponseDTO<List<VehiculeActifDTO>>> getTop5() {
        return ResponseEntity.ok(ApiResponseDTO.ok(reportingService.getTop5VehiculesActifs()));
    }

    // ─────────────────────────────────────────────────────────────
    //  REPARTITION PAR STATUT
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Répartition des véhicules par statut",
        description = "Retourne le nombre de véhicules par statut (ACTIF, EN_PANNE, EN_MAINTENANCE). " +
                      "Format de réponse : { \"ACTIF\": 12, \"EN_PANNE\": 3, \"EN_MAINTENANCE\": 2 }"
    )
    @ApiResponse(responseCode = "200", description = "Répartition calculée")
    @GetMapping("/statuts")
    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> getStatuts() {
        return ResponseEntity.ok(ApiResponseDTO.ok(reportingService.getStatutRepartition()));
    }

    // ─────────────────────────────────────────────────────────────
    //  MISSIONS PAR MOIS
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Nombre de missions par mois",
        description = "Retourne le nombre de missions effectuées chaque mois pour une année donnée. " +
                      "Si l'année n'est pas précisée, l'année courante est utilisée. " +
                      "Format de réponse : { \"Janvier\": 15, \"Février\": 22, ... }"
    )
    @ApiResponse(responseCode = "200", description = "Statistiques mensuelles récupérées")
    @GetMapping("/missions-par-mois")
    public ResponseEntity<ApiResponseDTO<Map<String, Long>>> getMissionsParMois(
            @Parameter(description = "Année souhaitée (ex: 2025). Par défaut : année courante.")
            @RequestParam(required = false) Integer annee) {
        int year = annee != null ? annee : LocalDate.now().getYear();
        return ResponseEntity.ok(ApiResponseDTO.ok(reportingService.getMissionsParMois(year)));
    }
}