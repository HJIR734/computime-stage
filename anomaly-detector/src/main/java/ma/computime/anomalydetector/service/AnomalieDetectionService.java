// Emplacement : ma/computime/anomalydetector/service/AnomalieDetectionService.java
package ma.computime.anomalydetector.service;

import ma.computime.anomalydetector.entity.*;
import ma.computime.anomalydetector.repository.AnomalieRepository;
import ma.computime.anomalydetector.repository.EmployeRepository;
import ma.computime.anomalydetector.repository.PointageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnomalieDetectionService {

    @Autowired private EmployeRepository employeRepository;
    @Autowired private PointageRepository pointageRepository;
    @Autowired private AnomalieRepository anomalieRepository;

    public void lancerDetectionPourTous(LocalDate jour) {
        List<Employe> employes = employeRepository.findAll();

        for (Employe employe : employes) {
            List<Pointage> pointagesDuJour = pointageRepository.findByBadgeEmploye(employe.getBadge())
                    .stream()
                    .filter(p -> p.getDateMouvement().toLocalDate().equals(jour))
                    .sorted(Comparator.comparing(Pointage::getDateMouvement))
                    .collect(Collectors.toList());

            if (pointagesDuJour.isEmpty()) {
                continue;
            }

            // On vérifie les omissions en premier, car c'est l'anomalie la plus prioritaire.
            detecterOmission(employe, jour, pointagesDuJour);

            // On ne vérifie les autres règles que si le nombre de pointages est pair.
            if (pointagesDuJour.size() % 2 == 0) {
                Planning planning = employe.getPlanning();
                if (planning == null) {
                    continue;
                }

                Optional<Horaire> horaireDuJourOpt = planning.getHorairePourJour(jour.getDayOfWeek());
                
                if (horaireDuJourOpt.isPresent()) {
                    Horaire horaireDuJour = horaireDuJourOpt.get();
                    detecterRetard(employe, jour, pointagesDuJour, horaireDuJour);
                    detecterHeureSupplementaire(employe, jour, pointagesDuJour, horaireDuJour);
                }
            }
        }
    }

    private void detecterOmission(Employe employe, LocalDate jour, List<Pointage> pointages) {
        if (pointages.size() % 2 != 0) {
            String message = "Nombre de pointages impair détecté (" + pointages.size() + " pointages).";
            creerEtSauverAnomalie(employe, jour, TypeAnomalie.OMISSION_POINTAGE, message, "Vérifier les pointages de la journée.");
        }
    }

    // Cette méthode n'est maintenant appelée que si le nombre de pointages est pair.
    private void detecterRetard(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire) {
        LocalTime heureDebutTheorique = horaire.getHeureDebutTheorique();
        if (heureDebutTheorique == null) return;
        
        Pointage premierPointage = pointages.get(0);
        if (premierPointage.getDateMouvement().toLocalTime().isAfter(heureDebutTheorique)) {
            long minutes = Duration.between(heureDebutTheorique, premierPointage.getDateMouvement().toLocalTime()).toMinutes();
            if (minutes > 0) {
                String message = "Arrivée à " + premierPointage.getDateMouvement().toLocalTime().withNano(0) +
                                 " au lieu de " + heureDebutTheorique + " (retard de " + minutes + " minutes).";
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.RETARD, message, "Justificatif de retard requis.");
            }
        }
    }

    // Cette méthode n'est maintenant appelée que si le nombre de pointages est pair.
    private void detecterHeureSupplementaire(Employe employe, LocalDate jour, List<Pointage> pointages, Horaire horaire) {
        LocalTime heureFinTheorique = horaire.getHeureFinTheorique();
        if (heureFinTheorique == null) return;

        Pointage dernierPointage = pointages.get(pointages.size() - 1);
        if (dernierPointage.getDateMouvement().toLocalTime().isAfter(heureFinTheorique)) {
            long minutes = Duration.between(heureFinTheorique, dernierPointage.getDateMouvement().toLocalTime()).toMinutes();
            if (minutes > 0) {
                String message = "Sortie à " + dernierPointage.getDateMouvement().toLocalTime().withNano(0) +
                                 " alors que la journée se termine à " + heureFinTheorique + ".";
                creerEtSauverAnomalie(employe, jour, TypeAnomalie.HEURE_SUP_NON_AUTORISEE, message, "Valider ou rejeter les heures supplémentaires.");
            }
        }
    }
    
    private void creerEtSauverAnomalie(Employe employe, LocalDate jour, TypeAnomalie type, String message, String suggestion) {
        if (anomalieRepository.existsByEmployeAndJourAnomalieAndTypeAnomalie(employe, jour, type)) {
            return;
        }

        Anomalie anomalie = new Anomalie();
        anomalie.setEmploye(employe);
        anomalie.setJourAnomalie(jour);
        anomalie.setTypeAnomalie(type);
        anomalie.setMessage(message);
        anomalie.setSuggestion(suggestion);
        anomalie.setStatut(StatutAnomalie.EN_ATTENTE);
        
        anomalieRepository.save(anomalie);
    }
}