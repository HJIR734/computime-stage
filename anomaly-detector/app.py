# Fichier : app.py

from flask import Flask, request, jsonify
import joblib
import pandas as pd
import numpy as np
import os # Importé pour la gestion des chemins de fichiers

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


# ==============================================================================
# ENDPOINT N°1 : PRÉDICTION POUR LES OMISSIONS DE POINTAGE
# ==============================================================================
@app.route('/predict/entree', methods=['POST'])
def predict_entree():
    if model_omission is None:
        return jsonify({'erreur': "Le modèle de prédiction d'omission n'est pas disponible."
                        }), 503

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

        response = {
            'decision': decision,
            'confiance': f"{confiance:.2%}"
        }
        print(f"-> Décision (HS) : {decision} avec une confiance de {response['confiance']}")
        return jsonify(response)

    except Exception as e:
        print(f"ERREUR PENDANT LA PRÉDICTION HS : {e}")
        return jsonify({'erreur': str(e)}), 500


# ==============================================================================
# Lancement du serveur
# ==============================================================================
if __name__ == '__main__':
    print("\nLancement du serveur Flask...")
    app.run(debug=True, port=5000)