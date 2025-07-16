# Fichier : app.py

from flask import Flask, request, jsonify
import joblib 
import pandas as pd
import numpy as np
import os
import shap

# 1. Initialiser l'application Flask
app = Flask(__name__)

# ==============================================================================
# CHARGEMENT DE TOUS LES MODÈLES AU DÉMARRAGE DE L'APPLICATION
# ==============================================================================

# --- Dictionnaire pour stocker les modèles, explainers et features attendues ---
models = {}
explainers = {}
expected_features = {}

def load_model_and_explainer(name, path):
    """Charge un modèle et, si possible, son explainer SHAP."""
    print(f"--- Chargement du modèle '{name}' depuis '{path}'... ---")
    
    if not os.path.exists(path):
        print(f"-> ERREUR: Fichier '{path}' est introuvable.")
        return

    try:
        model = joblib.load(path)
        models[name] = model
        print(f"-> Modèle '{name}' chargé avec succès.")

        # Essayer de créer un explainer et de stocker les features
        if hasattr(model, 'feature_name_'):
            expected_features[name] = model.feature_name_
        elif hasattr(model, 'feature_names_in_'):
             expected_features[name] = model.feature_names_in_
        else:
            print(f"Avertissement: Impossible de déterminer les features pour le modèle '{name}'.")

        # Créer un TreeExplainer pour les modèles en arbre (comme LightGBM)
        if 'lgbm' in str(type(model)).lower():
            explainers[name] = shap.TreeExplainer(model)
            print(f"-> Explainer SHAP (Tree) pour '{name}' créé.")
        else:
            print(f"-> Pas d'explainer SHAP automatique pour ce type de modèle ({type(model)}).")

    except Exception as e:
        print(f"-> ERREUR critique au chargement du modèle ou de l'explainer '{name}': {e}")

# --- Chargement de tous les modèles ---
load_model_and_explainer('omission', './models/model_prediction_personnalise.pkl')
load_model_and_explainer('hs_contextuel', './models/model_hs_contextuel.pkl')
load_model_and_explainer('retard_contextuel', './models/model_retards_contextuel.pkl')
load_model_and_explainer('sortie_anticipee_context', './models/model_sortie_anticipee.pkl')

# === MODIFICATION 1 : CHARGEMENT DU NOUVEAU MODÈLE ET DES ENCODEURS ===
load_model_and_explainer('compensation', './models/model_compensation.pkl')
try:
    models['encoder_jour_type'] = joblib.load('./models/encoder_jour_type.pkl')
    models['encoder_decision_compensation'] = joblib.load('./models/encoder_decision_compensation.pkl')
    print("-> Encodeurs pour la compensation chargés avec succès.")
except Exception as e:
    print(f"-> ERREUR: Impossible de charger les encodeurs pour la compensation : {e}")
# =====================================================================


# ==============================================================================
# ENDPOINTS DE L'API
# ==============================================================================

# --- Fonctions utilitaires pour les justifications ---
def get_shap_based_justification(explainer, model, input_df, feature_names):
    shap_values = explainer.shap_values(input_df)
    shap_values_for_class = shap_values[1] if isinstance(shap_values, list) else shap_values
    feature_impacts = pd.Series(shap_values_for_class[0], index=feature_names).sort_values(ascending=False)
    return feature_impacts.head(2).index.tolist()

# --- ENDPOINT N°1 : PRÉDICTION POUR LES OMISSIONS DE POINTAGE ---
@app.route('/predict/entree', methods=['POST'])
def predict_entree():
    if 'omission' not in models:
        return jsonify({'erreur': "Le modèle de prédiction d'omission n'est pas disponible."}), 503
    data = request.get_json()
    features_df = pd.DataFrame([data])
    features_df['badge'] = features_df['badge'].astype('category')
    prediction_secondes = models['omission'].predict(features_df)[0]
    heure_predite = f"{int(prediction_secondes / 3600):02d}:{int((prediction_secondes % 3600) / 60):02d}"
    return jsonify({'suggestion_heure': heure_predite})

# --- ENDPOINT N°2 : HS CONTEXTUELLES AVEC JUSTIFICATION ---
@app.route('/predict/overtime-context', methods=['POST'])
def predict_overtime_context():
    if 'hs_contextuel' not in models or 'hs_contextuel' not in explainers:
        return jsonify({'erreur': 'Le modèle ou l\'explainer HS n\'est pas disponible.'}), 503
    data = request.get_json()
    input_df = pd.DataFrame([data], columns=expected_features['hs_contextuel'])
    prediction_proba = models['hs_contextuel'].predict_proba(input_df)[0]
    decision_index = np.argmax(prediction_proba)
    decision = "ACCEPTER" if models['hs_contextuel'].classes_[decision_index] == 1 else "REJETER"
    confiance = prediction_proba[decision_index]
    justification_features = get_shap_based_justification(explainers['hs_contextuel'], models['hs_contextuel'], input_df, expected_features['hs_contextuel'])
    return jsonify({"decision": decision, "confiance": f"{confiance:.0%}", "justification": justification_features})

# --- ENDPOINT N°3 : RETARDS CONTEXTUELS ---
@app.route('/predict/retard-context', methods=['POST'])
def predict_retard_context():
    if 'retard_contextuel' not in models:
        return jsonify({'erreur': "Le modèle de prédiction des retards n'est pas disponible."}), 503
    data = request.get_json()
    input_df = pd.DataFrame([data], columns=expected_features['retard_contextuel'])
    model = models['retard_contextuel']
    prediction_proba = model.predict_proba(input_df)[0]
    decision_index = np.argmax(prediction_proba)
    decision = "TOLÉRER" if model.classes_[decision_index] == 1 else "JUSTIFICATION REQUISE"
    confiance = prediction_proba[decision_index]
    justification = []
    if input_df['duree_retard_minutes'].iloc[0] <= 15: justification.append("Retard de courte durée.")
    if input_df['nb_retards_mois_precedent'].iloc[0] <= 2: justification.append("Bon historique de ponctualité.")
    if input_df['est_debut_semaine'].iloc[0] == 1: justification.append("Contexte 'Lundi Matin' pris en compte.")
    if input_df['duree_retard_minutes'].iloc[0] > 20: justification.append("Retard important (>20 min).")
    if not justification: justification.append("Analyse standard.")
    return jsonify({"decision": decision, "confiance": f"{confiance:.0%}", "justification": justification})


# --- ENDPOINT N°4 : SORTIE ANTICIPÉE CONTEXTUELLE AVEC JUSTIFICATION SHAP ---
@app.route('/predict/sortie-anticipee-context', methods=['POST'])
def predict_sortie_anticipee_context():
    if 'sortie_anticipee_context' not in models or 'sortie_anticipee_context' not in explainers:
        return jsonify({'erreur': 'Le modèle ou l\'explainer de sortie anticipée n\'est pas disponible.'}), 503

    try:
        data = request.get_json()
        print(f"\n--- Requête reçue sur /predict/sortie-anticipee-context : {data} ---")
        features = expected_features['sortie_anticipee_context']
        input_df = pd.DataFrame([data], columns=features)
        model = models['sortie_anticipee_context']
        prediction_proba = model.predict_proba(input_df)[0]
        decision_index = np.argmax(prediction_proba)
        decision = "TOLÉRER" if model.classes_[decision_index] == 1 else "JUSTIFICATION REQUISE"
        confiance = prediction_proba[decision_index]
        explainer = explainers['sortie_anticipee_context']
        justification_features = get_shap_based_justification(explainer, model, input_df, features)
        justification_textuelle = []
        for feature in justification_features:
            if feature == 'duree_anticipation_minutes':
                justification_textuelle.append(f"Durée de l'anticipation ({int(data[feature])} min) a été le facteur principal.")
            elif feature == 'est_fin_semaine':
                justification_textuelle.append("Le fait que ce soit la fin de semaine a été pris en compte.")
            elif feature == 'nb_hs_recentes_heures':
                justification_textuelle.append(f"L'historique des heures sup ({int(data[feature])}h) a influencé la décision.")
            elif feature == 'charge_travail_jour':
                 justification_textuelle.append("La charge de travail du jour a été un facteur.")
        if not justification_textuelle: justification_textuelle.append("Analyse standard des données.")
        response = {"decision": decision, "confiance": f"{confiance:.0%}", "justification": justification_textuelle}
        print(f"-> Décision finale : {response}")
        return jsonify(response)
        
    except Exception as e:
        print(f"ERREUR PENDANT LA PRÉDICTION DE SORTIE ANTICIPÉE : {e}")
        return jsonify({'erreur': str(e)}), 500

# === MODIFICATION 2 : AJOUT DU NOUVEL ENDPOINT POUR LA COMPENSATION ===
# --- ENDPOINT N°5 : PRÉDICTION POUR LA COMPENSATION ---
@app.route('/predict/compensation', methods=['POST'])
def predict_compensation():
    required_models = ['compensation', 'encoder_jour_type', 'encoder_decision_compensation']
    if not all(key in models for key in required_models):
        return jsonify({'erreur': 'Le modèle ou les encodeurs de compensation ne sont pas disponibles.'}), 503

    try:
        data = request.get_json()
        print(f"\n--- Requête reçue sur /predict/compensation : {data} ---")
        
        input_data = {
            'type_jour': data.get('type_jour'),
            'duree_travaillee_heures': data.get('duree_travaillee_heures'),
            'solde_repos_actuel_heures': data.get('solde_repos_actuel_heures')
        }
        input_df = pd.DataFrame([input_data])
        
        input_df['type_jour_encoded'] = models['encoder_jour_type'].transform(input_df['type_jour'])
        
        features_for_model = input_df[['type_jour_encoded', 'duree_travaillee_heures', 'solde_repos_actuel_heures']]
        # S'assurer que les noms de colonnes sont corrects pour le modèle
        features_for_model.columns = expected_features['compensation'] 
        
        model = models['compensation']
        prediction_encoded = model.predict(features_for_model)[0]
        prediction_proba = model.predict_proba(features_for_model)[0]
        
        decision = models['encoder_decision_compensation'].inverse_transform([prediction_encoded])[0]
        confiance = np.max(prediction_proba)

        justification = [f"Suggestion basée sur la durée travaillée ({data['duree_travaillee_heures']}h) et le solde de repos de l'employé ({data['solde_repos_actuel_heures']}h)."]
        
        response = {
            "decision": f"COMPENSATION_{decision}", # ex: COMPENSATION_PAYE ou COMPENSATION_REPOS
            "confiance": f"{confiance:.0%}",
            "justification": justification
        }
        
        print(f"-> Décision finale : {response}")
        return jsonify(response)
        
    except Exception as e:
        print(f"ERREUR PENDANT LA PRÉDICTION DE COMPENSATION : {e}")
        return jsonify({'erreur': str(e)}), 500
# =======================================================================

# ==============================================================================
# Lancement du serveur
# ==============================================================================
if __name__ == '__main__':
    print("\nLancement du serveur Flask...")
    app.run(debug=True, host='0.0.0.0', port=5001)