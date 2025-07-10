# Fichier : app.py

from flask import Flask, request, jsonify
import joblib
import pandas as pd
import numpy as np
import os

# 1. Initialiser l'application Flask
app = Flask(__name__)

# ==============================================================================
# CHARGEMENT DES MODÈLES AU DÉMARRAGE DE L'APPLICATION
# ==============================================================================

# --- Modèle 1 : Prédiction de l'heure d'omission (Régression) ---
model_omission_path = './models/model_prediction_personnalise.pkl'
model_omission = None
print(f"Tentative de chargement du modèle d'omission depuis '{model_omission_path}'...")
if os.path.exists(model_omission_path):
    try:
        model_omission = joblib.load(model_omission_path)
        print("-> Modèle d'omission chargé avec succès.")
    except Exception as e:
        print(f"-> ERREUR au chargement du modèle d'omission : {e}")
else:
    print(f"-> ERREUR: Fichier '{model_omission_path}' est introuvable.")


# --- Modèle 2 : Classification des Heures Supplémentaires ---
model_hs_path = './models/model_classification_hs.pkl'
model_hs = None
print(f"Tentative de chargement du modèle HS depuis '{model_hs_path}'...")
if os.path.exists(model_hs_path):
    try:
        model_hs = joblib.load(model_hs_path)
        print("-> Modèle de classification HS chargé avec succès.")
    except Exception as e:
        print(f"-> ERREUR au chargement du modèle HS : {e}")
else:
    print(f"-> ERREUR: Fichier '{model_hs_path}' est introuvable.")


# --- Modèle 3 : Classification des Retards ---
model_retard_path = './models/lgbm_retard_classifier.pkl'
model_retard = None
print(f"Tentative de chargement du modèle de Retard depuis '{model_retard_path}'...")
if os.path.exists(model_retard_path):
    try:
        model_retard = joblib.load(model_retard_path)
        print("-> Modèle de classification de Retard chargé avec succès.")
    except Exception as e:
        print(f"-> ERREUR au chargement du modèle de Retard : {e}")
else:
    print(f"-> ERREUR: Fichier '{model_retard_path}' est introuvable.")


# ==============================================================================
# ENDPOINT N°1 : PRÉDICTION POUR LES OMISSIONS DE POINTAGE
# ==============================================================================
@app.route('/predict/entree', methods=['POST'])
def predict_entree():
    if model_omission is None:
        return jsonify({'erreur': "Le modèle de prédiction d'omission n'est pas disponible."}), 503
    try:
        data = request.get_json()
        print(f"\n--- Requête reçue sur /predict/entree pour le badge : [{data.get('badge')}] ---")
        required_keys = ['jour_de_semaine', 'jour_du_mois', 'mois', 'semaine_de_annee', 'badge']
        if not all(key in data for key in required_keys):
            return jsonify({'erreur': 'Données manquantes dans la requête'}), 400
        features_df = pd.DataFrame([data])
        features_df['badge'] = features_df['badge'].astype('category')
        prediction_secondes = model_omission.predict(features_df)[0]
        if prediction_secondes < 0:
            prediction_secondes = 0 
        heure_predite = f"{int(prediction_secondes / 3600):02d}:{int((prediction_secondes % 3600) / 60):02d}"
        print(f"-> Prédiction (omission) : {heure_predite}")
        return jsonify({'suggestion_heure': heure_predite})
    except Exception as e:
        print(f"ERREUR PENDANT LA PRÉDICTION D'OMISSION : {e}")
        return jsonify({'erreur': f'Erreur lors de la prédiction : {e}'}), 500


# ==============================================================================
# ENDPOINT N°2 : CLASSIFICATION POUR LES HEURES SUPPLÉMENTAIRES
# ==============================================================================
@app.route('/predict/overtime', methods=['POST'])
def predict_overtime():
    if model_hs is None:
        return jsonify({'erreur': 'Le modèle de classification HS n\'est pas disponible.'}), 503
    try:
        data = request.get_json()
        print(f"\n--- Requête reçue sur /predict/overtime pour le badge : [{data.get('badge')}] ---")
        required_keys = ['badge', 'jour_semaine', 'hs_secondes']
        if not all(key in data for key in required_keys):
            return jsonify({'erreur': 'Données manquantes dans la requête'}), 400
        input_df = pd.DataFrame([data])
        input_df['badge'] = input_df['badge'].astype('category')
        prediction = model_hs.predict(input_df)[0]
        proba = model_hs.predict_proba(input_df)[0]
        decision = 'ACCEPTER' if prediction == 1 else 'REJETER'
        confiance = proba[1] if decision == 'ACCEPTER' else proba[0]
        response = {'decision': decision, 'confiance': f"{confiance:.2%}"}
        print(f"-> Décision (HS) : {decision} avec une confiance de {response['confiance']}")
        return jsonify(response)
    except Exception as e:
        print(f"ERREUR PENDANT LA PRÉDICTION HS : {e}")
        return jsonify({'erreur': str(e)}), 500

# ==============================================================================
# ENDPOINT N°3 : CLASSIFICATION POUR LES RETARDS (VERSION FINALE)
# ==============================================================================
@app.route('/predict/retard', methods=['POST'])
def predict_retard():
    if model_retard is None:
        return jsonify({'erreur': 'Le modèle de classification de Retard n\'est pas disponible.'}), 503
    try:
        data = request.get_json()
        print(f"\n--- Requête reçue sur /predict/retard pour le badge : [{data.get('badge')}] ---")
        
        # On vérifie que toutes les features attendues par le modèle sont bien là
        required_keys = ['jour_semaine', 'PLANNING_FK', 'nombre_pointages']
        if not all(key in data for key in required_keys):
            return jsonify({'erreur': f'Données manquantes. Reçu: {data.keys()}. Attendu: {required_keys}'}), 400

        # On prépare le DataFrame avec les bonnes colonnes pour la prédiction
        features_for_model = {
            'jour_semaine': [data.get('jour_semaine')],
            'PLANNING_FK': [data.get('PLANNING_FK')],
            'nombre_pointages': [data.get('nombre_pointages')]
        }
        input_df = pd.DataFrame(features_for_model)
        
        # Prédiction avec le VRAI modèle
        prediction = model_retard.predict(input_df)[0]
        proba = model_retard.predict_proba(input_df)[0]

        # On définit les décisions possibles. Comme notre modèle ne prédit que 1,
        # la décision sera toujours JUSTIFICATION_REQUISE pour l'instant.
        decision = 'JUSTIFICATION_REQUISE' if prediction == 1 else 'IGNORER'
        # On prend la probabilité de la classe prédite.
        confiance_score = proba[prediction]
        
        response = {
            'decision': decision,
            'confiance': f"{confiance_score:.2%}"
        }
        print(f"-> Décision (Retard) : {decision} avec une confiance de {response['confiance']}")
        return jsonify(response)

    except Exception as e:
        print(f"ERREUR PENDANT LA PRÉDICTION DE RETARD : {e}")
        return jsonify({'erreur': str(e)}), 500


# ==============================================================================
# Lancement du serveur
# ==============================================================================
if __name__ == '__main__':
    print("\nLancement du serveur Flask...")
    app.run(debug=True, host='0.0.0.0', port=5000)