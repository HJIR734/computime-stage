// FICHIER : TypeAnomalie.java (La version VRAIMENT finale)
package ma.computime.anomalydetector.entity;

public enum TypeAnomalie {
    OMISSION_POINTAGE,
    HEURE_SUP_NON_AUTORISEE, 
    RETARD,
    SORTIE_ANTICIPEE,
    DEPART_ANTICIPE,
    TRAVAIL_JOUR_FERIE,      
    TRAVAIL_JOUR_REPOS,
    ABSENCE_INJUSTIFIEE,
    PAUSE_TROP_LONGUE
}