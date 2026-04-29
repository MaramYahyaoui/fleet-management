package com.flotte.controller;

import com.flotte.dto.ApiResponseDTO;
import com.flotte.dto.ConsommationDTO;
import com.flotte.dto.ConsommationMensuelleDTO;
import com.flotte.dto.CoutCarburantDTO;
import com.flotte.service.ConsommationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/consommations")
@RequiredArgsConstructor
@Tag(name = "Consommations", description = "Suivi de la consommation de carburant par véhicule")
public class ConsommationController {

    private final ConsommationService consommationService;

    // ─────────────────────────────────────────────────────────────
    //  GET ALL (avec filtres)
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Lister les consommations",
        description = "Retourne toutes les consommations. Filtres combinables : par véhicule, par période (debut + fin), ou les deux."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Liste récupérée avec succès",
            content = @Content(schema = @Schema(implementation = ConsommationDTO.class)))
    })
    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<ConsommationDTO>>> findAll(
            @Parameter(description = "ID du véhicule pour filtrer")
            @RequestParam(required = false) Long vehiculeId,
            @Parameter(description = "Date de début de période (format : yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate debut,
            @Parameter(description = "Date de fin de période (format : yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin) {

        List<ConsommationDTO> result;
        if (vehiculeId != null && debut != null && fin != null) {
            result = consommationService.findByVehiculeEtPeriode(vehiculeId, debut, fin);
        } else if (vehiculeId != null) {
            result = consommationService.findByVehicule(vehiculeId);
        } else if (debut != null && fin != null) {
            result = consommationService.findByPeriode(debut, fin);
        } else {
            result = consommationService.findAll();
        }
        return ResponseEntity.ok(ApiResponseDTO.ok(result));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET BY ID
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Trouver une consommation par ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consommation trouvée"),
        @ApiResponse(responseCode = "404", description = "Consommation introuvable", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ConsommationDTO>> findById(
            @Parameter(description = "ID de la consommation", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponseDTO.ok(consommationService.findById(id)));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET BY VEHICULE
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Consommations d'un véhicule",
        description = "Retourne tout l'historique de consommation pour un véhicule donné."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Historique récupéré"),
        @ApiResponse(responseCode = "404", description = "Véhicule introuvable", content = @Content)
    })
    @GetMapping("/vehicule/{vehiculeId}")
    public ResponseEntity<ApiResponseDTO<List<ConsommationDTO>>> findByVehicule(
            @Parameter(description = "ID du véhicule", required = true)
            @PathVariable Long vehiculeId) {
        return ResponseEntity.ok(ApiResponseDTO.ok(consommationService.findByVehicule(vehiculeId)));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET DASHBOARD COUTS
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Tableau de bord des coûts carburant",
        description = "Retourne le coût total de carburant par véhicule, trié par coût décroissant."
    )
    @ApiResponse(responseCode = "200", description = "Dashboard récupéré")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponseDTO<List<CoutCarburantDTO>>> getDashboard() {
        return ResponseEntity.ok(ApiResponseDTO.ok(consommationService.getDashboardCouts()));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET CONSOMMATION MENSUELLE
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Consommation mensuelle d'un véhicule",
        description = "Retourne la consommation agrégée mois par mois pour un véhicule donné."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consommation mensuelle récupérée"),
        @ApiResponse(responseCode = "404", description = "Véhicule introuvable", content = @Content)
    })
    @GetMapping("/mensuelle/vehicule/{vehiculeId}")
    public ResponseEntity<ApiResponseDTO<List<ConsommationMensuelleDTO>>> getMensuelle(
            @Parameter(description = "ID du véhicule", required = true)
            @PathVariable Long vehiculeId) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                consommationService.getConsommationMensuelle(vehiculeId)));
    }

    // ─────────────────────────────────────────────────────────────
    //  GET COUT TOTAL GLOBAL
    // ─────────────────────────────────────────────────────────────
    @Operation(
        summary = "Coût total global de carburant",
        description = "Retourne la somme de tous les coûts carburant de toute la flotte."
    )
    @ApiResponse(responseCode = "200", description = "Coût total calculé")
    @GetMapping("/cout-total")
    public ResponseEntity<ApiResponseDTO<Double>> getCoutTotal() {
        return ResponseEntity.ok(ApiResponseDTO.ok(consommationService.getCoutTotalGlobal()));
    }

    // ─────────────────────────────────────────────────────────────
    //  POST CREATE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Enregistrer une consommation de carburant")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Consommation enregistrée avec succès"),
        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content)
    })
    @PostMapping
    public ResponseEntity<ApiResponseDTO<ConsommationDTO>> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Données de la consommation à enregistrer",
                required = true,
                content = @Content(schema = @Schema(implementation = ConsommationDTO.class))
            )
            @Valid @RequestBody ConsommationDTO dto) {
        ConsommationDTO created = consommationService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.ok("Consommation enregistrée", created));
    }

    // ─────────────────────────────────────────────────────────────
    //  PUT UPDATE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Mettre à jour une consommation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consommation mise à jour"),
        @ApiResponse(responseCode = "400", description = "Données invalides", content = @Content),
        @ApiResponse(responseCode = "404", description = "Consommation introuvable", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<ConsommationDTO>> update(
            @Parameter(description = "ID de la consommation", required = true) @PathVariable Long id,
            @Valid @RequestBody ConsommationDTO dto) {
        return ResponseEntity.ok(ApiResponseDTO.ok(
                "Consommation mise à jour", consommationService.update(id, dto)));
    }

    // ─────────────────────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────────────────────
    @Operation(summary = "Supprimer une consommation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Consommation supprimée"),
        @ApiResponse(responseCode = "404", description = "Consommation introuvable", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(
            @Parameter(description = "ID de la consommation à supprimer", required = true)
            @PathVariable Long id) {
        consommationService.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.ok("Consommation supprimée", null));
    }
}