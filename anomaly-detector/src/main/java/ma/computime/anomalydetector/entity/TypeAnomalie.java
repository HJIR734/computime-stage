// FICHIER : TypeAnomalie.java (La version VRAIMENT finale)
package ma.computime.anomalydetector.entity;

public enum TypeAnomalie {
    OMISSION_POINTAGE,
    // On utilise la version courte qui est dans la base de données
    HEURE_SUP_NON_AUTORISEE, 
    TRAVAIL_JOUR_FERIE,
    RETARD,
    DEPART_ANTICIPE
}