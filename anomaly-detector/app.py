# Fichier : app.py

from flask import Flask, request, jsonify
import joblib 
import pandas as pd
import numpy as np
import os
import shap

app = Flask(__name__)

# Dictionnaires globaux
models = {}
explainers = {}
expected_features = {}

def load_model_and_explainer(name, path):
    print(f"--- Chargement du modèle '{name}' depuis '{path}'... ---")
    if not os.path.exists(path):
        print(f"-> ERREUR: Fichier '{path}' est introuvable.")
        return
    try:
        model = joblib.load(path)
        models[name] = model
        print(f"-> Modèle '{name}' chargé avec succès.")
        if hasattr(model, 'feature_name_'):
            expected_features[name] = model.feature_name_
        elif hasattr(model, 'feature_names_in_'):
             expected_features[name] = model.feature_names_in_
        if 'lgbm' in str(type(model)).lower() or 'randomforest' in str(type(model)).lower(): # Supporte aussi RandomForest
            explainers[name] = shap.TreeExplainer(model)
            print(f"-> Explainer SHAP (Tree) pour '{name}' créé.")
    except Exception as e:
        print(f"-> ERREUR critique au chargement '{name}': {e}")

# Chargement de tous les modèles existants
load_model_and_explainer('omission', './models/model_prediction_personnalise.pkl')
load_model_and_explainer('hs_contextuel', './models/model_hs_contextuel.pkl')
load_model_and_explainer('retard_contextuel', './models/model_retards_contextuel.pkl')
load_model_and_explainer('sortie_anticipee_context', './models/model_sortie_anticipee.pkl')
load_model_and_explainer('compensation', './models/model_compensation.pkl')
load_model_and_explainer('absence_injustifiee', './models/model_absence_injustifiee.pkl')

# ====================================================================
# NOUVEAU : Chargement du modèle de prédiction d'absence future
# ====================================================================
load_model_and_explainer('absence_predictor', './models/absence_predictor_model.pkl')


try:
    models['encoder_jour_type'] = joblib.load('./models/encoder_jour_type.pkl')
    models['encoder_decision_compensation'] = joblib.load('./models/encoder_decision_compensation.pkl')
    models['encoder_decision_absence'] = joblib.load('./models/encoder_decision_absence.pkl')
    print("-> Tous les encodeurs ont été chargés avec succès.")
except Exception as e:
    print(f"-> ERREUR: Impossible de charger un ou plusieurs encodeurs : {e}")

def get_shap_based_justification(explainer, input_df, feature_names):
    shap_values_raw = explainer.shap_values(input_df)
    shap_values_for_instance = shap_values_raw[0]
    if len(shap_values_for_instance.shape) == 2:
        feature_impacts_values = np.mean(np.abs(shap_values_for_instance), axis=1)
    else:
        feature_impacts_values = np.abs(shap_values_for_instance)
    feature_impacts = pd.Series(feature_impacts_values, index=feature_names).sort_values(ascending=False)
    return feature_impacts.head(2).index.tolist()

# --- Tous les endpoints existants (inchangés) ---

@app.route('/predict/entree', methods=['POST'])
def predict_entree():
    # ... (ton code existant, inchangé)
    if 'omission' not in models: return jsonify({'erreur': "Modèle omission non disponible."}), 503
    data = request.get_json()
    features_df = pd.DataFrame([data])
    features_df['badge'] = features_df['badge'].astype('category')
    prediction_secondes = models['omission'].predict(features_df)[0]
    heure_predite = f"{int(prediction_secondes / 3600):02d}:{int((prediction_secondes % 3600) / 60):02d}"
    return jsonify({'suggestion_heure': heure_predite})

# ... (tous tes autres endpoints : /overtime-context, /retard-context, etc. restent ici, inchangés)
@app.route('/predict/overtime-context', methods=['POST'])
def predict_overtime_context():
    model_name = 'hs_contextuel'
    if model_name not in models or model_name not in explainers: return jsonify({'erreur': 'Modèle HS non disponible.'}), 503
    data = request.get_json()
    input_df = pd.DataFrame([data], columns=expected_features[model_name])
    model = models[model_name]
    prediction_proba = model.predict_proba(input_df)[0]
    decision_index = np.argmax(prediction_proba)
    decision = "ACCEPTER" if model.classes_[decision_index] == 1 else "REJETER"
    confiance = prediction_proba[decision_index]
    justification_features = get_shap_based_justification(explainers[model_name], input_df, expected_features[model_name])
    return jsonify({"decision": decision, "confiance": f"{confiance:.0%}", "justification": justification_features})

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

@app.route('/predict/sortie-anticipee-context', methods=['POST'])
def predict_sortie_anticipee_context():
    model_name = 'sortie_anticipee_context'
    if model_name not in models or model_name not in explainers: return jsonify({'erreur': 'Modèle sortie anticipée non disponible.'}), 503
    data = request.get_json()
    features = expected_features[model_name]
    input_df = pd.DataFrame([data], columns=features)
    model = models[model_name]
    prediction_proba = model.predict_proba(input_df)[0]
    decision_index = np.argmax(prediction_proba)
    decision = "TOLÉRER" if model.classes_[decision_index] == 1 else "JUSTIFICATION REQUISE"
    confiance = prediction_proba[decision_index]
    explainer = explainers[model_name]
    justification_features = get_shap_based_justification(explainer, input_df, features)
    return jsonify({"decision": decision, "confiance": f"{confiance:.0%}", "justification": justification_features})

@app.route('/predict/compensation', methods=['POST'])
def predict_compensation():
    required_models = ['compensation', 'encoder_jour_type', 'encoder_decision_compensation']
    if not all(key in models for key in required_models): return jsonify({'erreur': 'Modèle compensation non disponible.'}), 503
    data = request.get_json()
    input_data = {'type_jour': data.get('type_jour'), 'duree_travaillee_heures': data.get('duree_travaillee_heures'), 'solde_repos_actuel_heures': data.get('solde_repos_actuel_heures')}
    input_df = pd.DataFrame([input_data])
    input_df['type_jour_encoded'] = models['encoder_jour_type'].transform(input_df['type_jour'])
    features_for_model = input_df[['type_jour_encoded', 'duree_travaillee_heures', 'solde_repos_actuel_heures']]
    features_for_model.columns = expected_features['compensation'] 
    model = models['compensation']
    prediction_encoded = model.predict(features_for_model)[0]
    prediction_proba = model.predict_proba(features_for_model)[0]
    decision = models['encoder_decision_compensation'].inverse_transform([prediction_encoded])[0]
    confiance = np.max(prediction_proba)
    justification = [f"Suggestion basée sur la durée travaillée et le solde de repos."]
    response = {"decision": f"COMPENSATION_{decision}", "confiance": f"{confiance:.0%}", "justification": justification}
    return jsonify(response)
    
@app.route('/predict/absence-injustifiee', methods=['POST'])
def predict_absence_injustifiee():
    model_name = 'absence_injustifiee'
    encoder_name = 'encoder_decision_absence'
    if model_name not in models or encoder_name not in models or model_name not in explainers: 
        return jsonify({'erreur': "Modèle absence non disponible."}), 503
    
    data = request.get_json()
    features = expected_features[model_name]
    input_df = pd.DataFrame([data], columns=features)
    
    model = models[model_name]
    prediction_proba = model.predict_proba(input_df)[0]
    prediction_encoded = np.argmax(prediction_proba)
    decision_encoder = models[encoder_name]
    decision = decision_encoder.inverse_transform([prediction_encoded])[0]
    confiance = np.max(prediction_proba)
    
    explainer = explainers[model_name]
    justification_features = get_shap_based_justification(explainer, input_df, features)
    
    justification_textuelle = []
    for feature in justification_features:
        if feature == 'solde_conges_jours':
            valeur = int(data.get(feature, 0))
            if valeur <= 2:
                justification_textuelle.append(f"Le faible solde de congés ({valeur} j) a été un facteur clé.")
            else:
                justification_textuelle.append(f"Le solde de congés de l'employé ({valeur} j) a été pris en compte.")
        elif feature == 'nb_absences_injustifiees_annee':
            valeur = int(data.get(feature, 0))
            if valeur > 2:
                justification_textuelle.append(f"L'historique d'absences récurrentes ({valeur}) a fortement influencé la décision.")
            else:
                justification_textuelle.append(f"L'historique d'absences ({valeur}) a été un facteur.")
        elif feature == 'duree_absence_jours':
            valeur = int(data.get(feature, 0))
            if valeur > 2:
                 justification_textuelle.append(f"La longue durée de l'absence ({valeur} jours) a été un élément important.")
            else:
                 justification_textuelle.append(f"La durée de l'absence ({valeur} jour(s)) a été analysée.")
        elif feature == 'est_adjacent_weekend_ferie':
            if data.get(feature) == 1:
                justification_textuelle.append("Le fait que l'absence soit proche d'un jour de repos a été considéré.")
    if not justification_textuelle:
        justification_textuelle.append("Suggestion basée sur l'analyse générale des données.")
    
    response = {"decision": decision, "confiance": f"{confiance:.0%}", "justification": justification_textuelle}
    return jsonify(response)


# ====================================================================
# NOUVEL ENDPOINT POUR LA PRÉDICTION D'ABSENCE FUTURE
# ====================================================================
@app.route('/predict/absence-future', methods=['POST'])
def predict_absence_future():
    model_name = 'absence_predictor'
    if model_name not in models:
        return jsonify({'error': "Le modèle de prédiction d'absence future n'est pas disponible."}), 503

    data = request.get_json()
    if not data:
        return jsonify({'error': 'Données d\'entrée manquantes.'}), 400

    try:
        # On s'assure que les features sont dans le bon ordre
        input_df = pd.DataFrame([data], columns=expected_features[model_name])
        
        model = models[model_name]
        
        # On prédit la probabilité d'être absent (classe 1)
        probabilite_absence = model.predict_proba(input_df)[0, 1]

        return jsonify({
            'probabilite_absence': round(float(probabilite_absence), 4)
        })
    except Exception as e:
        # On log l'erreur pour le débogage
        print(f"Erreur lors de la prédiction d'absence future: {e}")
        return jsonify({'error': f"Une erreur interne est survenue: {e}"}), 500


if __name__ == '__main__':
    print("\nLancement du serveur Flask...")
    app.run(debug=True, host='0.0.0.0', port=5001)