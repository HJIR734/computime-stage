package ma.computime.anomalydetector.service;

// ========================================================================
// IMPORTS NÉCESSAIRES (C'est ça qui manquait)
// ========================================================================
import ma.computime.anomalydetector.dto.AnomalieInfo;
import ma.computime.anomalydetector.entity.*;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
// ========================================================================


@Service
public class AnomalieDetectionService {

    // --- Injections des Repositories ---
    @Autowired
    private PointageRepository pointageRepository;

    @Autowired
    private EmployeRepository employeRepository;


    // --- Méthode Principale de Détection ---
    @Transactional(readOnly = true)
    public List<AnomalieInfo> detecterAnomaliesPourTous(LocalDate jour) {
        List<AnomalieInfo> toutesLesAnomalies = new ArrayList<>();
        List<Employe> employes = employeRepository.findAll();

        for (Employe employe : employes) {
            // 1. Récupérer les pointages du jour pour l'employé, triés par heure
            List<Pointage> pointagesDuJour = pointageRepository.findByBadgeEmploye(employe.getBadge()).stream()
                    .filter(p -> p.getDateMouvement().toLocalDate().equals(jour))
                    .sorted(Comparator.comparing(Pointage::getDateMouvement))
                    .collect(Collectors.toList());

            // 2. Récupérer l'horaire théorique pour cet employé, ce jour-là
            Optional<Horaire> horaireOpt = getHorairePourEmploye(employe, jour.getDayOfWeek());

            if (horaireOpt.isPresent() && !"Repos".equalsIgnoreCase(horaireOpt.get().getType())) {
                // CAS 1 : L'employé DEVAIT travailler
                Horaire horaire = horaireOpt.get();
                
                // On cherche la plage horaire principale (celle qui commence le plus tôt)
                Optional<PlageHoraire> plagePrincipaleOpt = horaire.getPlagesHoraires().stream()
                        .min(Comparator.comparing(PlageHoraire::getDebut));
                
                if (plagePrincipaleOpt.isEmpty()) {
                    continue; // Pas de plage définie, on ne peut rien faire pour cet employé
                }
                
                PlageHoraire plagePrincipale = plagePrincipaleOpt.get();

                if (pointagesDuJour.isEmpty()) {
                    // Règle : ABSENCE
                    toutesLesAnomalies.add(creerAnomalie(employe, "ABSENCE", "Aucun pointage détecté pour un jour travaillé."));
                } else {
                    Pointage premierPointage = pointagesDuJour.get(0);
                    Pointage dernierPointage = pointagesDuJour.get(pointagesDuJour.size() - 1);

                    // Règle : RETARD
                    LocalTime heureDebutTheorique = convertirDoubleEnHeure(plagePrincipale.getDebut());
                    if (premierPointage.getDateMouvement().toLocalTime().isAfter(heureDebutTheorique.plusMinutes(plagePrincipale.getToleranceEntree()))) {
                        toutesLesAnomalies.add(creerAnomalie(employe, "RETARD", "Le premier pointage à " + premierPointage.getDateMouvement().toLocalTime() + " est après l'heure de début prévue (" + heureDebutTheorique + ")."));
                    }
                    
                    // Règle : SORTIE ANTICIPÉE (basée sur la fin de la dernière plage horaire du jour)
                    Optional<PlageHoraire> dernierePlageOpt = horaire.getPlagesHoraires().stream()
                        .max(Comparator.comparing(PlageHoraire::getFin));

                    if(dernierePlageOpt.isPresent()){
                        PlageHoraire dernierePlage = dernierePlageOpt.get();
                        LocalTime heureFinTheorique = convertirDoubleEnHeure(dernierePlage.getFin());
                        if (dernierPointage.getDateMouvement().toLocalTime().isBefore(heureFinTheorique.minusMinutes(dernierePlage.getToleranceSortie()))) {
                            toutesLesAnomalies.add(creerAnomalie(employe, "SORTIE_ANTICIPEE", "Le dernier pointage à " + dernierPointage.getDateMouvement().toLocalTime() + " est avant l'heure de fin prévue (" + heureFinTheorique + ")."));
                        }
                    }
                }
            } else {
                // CAS 2 : L'employé NE DEVAIT PAS travailler (jour de repos)
                if (!pointagesDuJour.isEmpty()) {
                    toutesLesAnomalies.add(creerAnomalie(employe, "TRAVAIL_JOUR_REPOS", "Pointages détectés un jour de repos."));
                }
            }
            
            // Règle OMISSION_POINTAGE (toujours valide)
            if (!pointagesDuJour.isEmpty() && pointagesDuJour.size() % 2 != 0) {
                toutesLesAnomalies.add(creerAnomalie(employe, "OMISSION_POINTAGE", "Nombre de pointages impair (" + pointagesDuJour.size() + ") détecté."));
            }
        }
        return toutesLesAnomalies;
    }


    // --- Méthodes d'Aide (Helpers) ---

    private Optional<Horaire> getHorairePourEmploye(Employe employe, DayOfWeek jourDeLaSemaine) {
        if (employe.getPlanning() == null || !"Hebdomadaire".equalsIgnoreCase(employe.getPlanning().getType())) {
            return Optional.empty();
        }

        String nomJourFrancais = convertirDayOfWeekEnFrancais(jourDeLaSemaine);

        return employe.getPlanning().getJours().stream()
                .filter(jour -> nomJourFrancais.equalsIgnoreCase(jour.getLibelle()))
                .findFirst()
                .map(Jour::getHoraire); // Prend l'horaire du jour trouvé
    }

    private LocalTime convertirDoubleEnHeure(Double heureDecimale) {
        if (heureDecimale == null) return LocalTime.MIDNIGHT;
        int heures = heureDecimale.intValue();
        int minutes = (int) Math.round((heureDecimale - heures) * 60);
        return LocalTime.of(heures, minutes);
    }
    
    private String convertirDayOfWeekEnFrancais(DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case MONDAY: return "Lundi";
            case TUESDAY: return "Mardi";
            case WEDNESDAY: return "Mercredi";
            case THURSDAY: return "Jeudi";
            case FRIDAY: return "Vendredi";
            case SATURDAY: return "Samedi";
            case SUNDAY: return "Dimanche";
            default: return "";
        }
    }

    private AnomalieInfo creerAnomalie(Employe employe, String type, String message) {
        return new AnomalieInfo(type, message, employe.getBadge(), employe.getPrenom() + " " + employe.getNom());
    }
}