# Fichier : app.py (version de débogage, sans try/except au chargement)

from flask import Flask, request, jsonify
import joblib
import numpy as np

# 1. Initialiser l'application Flask
app = Flask(__name__)

# 2. Charger notre modèle sauvegardé au démarrage de l'application.
# Nous enlevons le try/except pour que le programme s'arrête et affiche
# une erreur claire si le chargement échoue.
print("Tentative de chargement du modèle depuis './models/model_prediction_entree.pkl'...")
model = joblib.load('./models/model_prediction_entree.pkl')
print("Modèle chargé avec succès.")


# 3. Créer l'endpoint de prédiction
@app.route('/predict/entree', methods=['POST'])
def predict_entree():
    # La vérification "if model is None" n'est plus nécessaire ici car si le chargement
    # échoue, l'application n'aura même pas démarré.

    # Récupérer les données JSON envoyées par le client (notre app Java)
    data = request.get_json()

    if not data:
        return jsonify({'erreur': 'Aucune donnée fournie'}), 400

    try:
        # Préparer les données pour le modèle dans le bon ordre
        # L'ordre doit être : jour_de_semaine, jour_du_mois, semaine_de_annee, mois
        features = [
            data['jour_de_semaine'],
            data['jour_du_mois'],
            data['semaine_de_annee'],
            data['mois']
        ]

        # Faire la prédiction
        prediction_secondes = model.predict([features])[0]

        # Renvoyer la prédiction au format JSON
        return jsonify({
            'prediction_secondes': prediction_secondes,
            'suggestion_heure': f"{int(prediction_secondes / 3600):02d}:{int((prediction_secondes % 3600) / 60):02d}"
        })

    except KeyError as e:
        return jsonify({'erreur': f'Donnée manquante dans la requête : {e}'}), 400
    except Exception as e:
        return jsonify({'erreur': f'Erreur lors de la prédiction : {e}'}), 500

# Lancer le serveur si on exécute ce script directement
if __name__ == '__main__':
    # On utilise le port 5000 pour ne pas entrer en conflit avec Spring Boot (port 8080)
    app.run(debug=True, port=5000)