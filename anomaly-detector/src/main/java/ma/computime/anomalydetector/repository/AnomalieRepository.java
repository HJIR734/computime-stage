// Emplacement : src/main/java/ma/computime/anomalydetector/repository/AnomalieRepository.java
package ma.computime.anomalydetector.repository;

import ma.computime.anomalydetector.entity.Anomalie;
import ma.computime.anomalydetector.entity.Employe;
import ma.computime.anomalydetector.entity.TypeAnomalie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate; // <-- Assure-toi que cet import est présent

public interface AnomalieRepository extends JpaRepository<Anomalie, Long> {

    /**
     * Vérifie s'il existe déjà une anomalie d'un certain type, pour un certain employé,
     * à une date donnée.
     * Spring Data JPA va automatiquement générer la requête SQL correspondante
     * simplement en lisant le nom de la méthode. C'est sa "magie".
     *
     * @param type Le type d'anomalie à vérifier (ex: OMISSION_POINTAGE).
     * @param employe L'employé concerné.
     * @param jour La date de l'anomalie.
     * @return true si une telle anomalie existe déjà, false sinon.
     */
    boolean existsByTypeAnomalieAndEmployeAndJourAnomalie(TypeAnomalie type, Employe employe, LocalDate jour);

}