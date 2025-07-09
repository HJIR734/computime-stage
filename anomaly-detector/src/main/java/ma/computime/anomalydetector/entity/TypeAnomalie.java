// FICHIER : TypeAnomalie.java (La version VRAIMENT finale)
package ma.computime.anomalydetector.entity;

public enum TypeAnomalie {
    OMISSION_POINTAGE,
    // On utilise la version courte qui est dans la base de données
    HEURE_SUP_NON_AUTORISEE, 
    RETARD,
    DEPART_ANTICIPE,
    TRAVAIL_JOUR_FERIE,      
    TRAVAIL_JOUR_REPOS 
}