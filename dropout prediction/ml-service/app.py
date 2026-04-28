"""
Student Dropout Prediction - Flask ML Service
Uses a Decision Tree trained on synthetic data.
Endpoint: POST /predict
"""

from flask import Flask, request, jsonify
from flask_cors import CORS
import numpy as np
from sklearn.tree import DecisionTreeClassifier
from sklearn.preprocessing import LabelEncoder

app = Flask(__name__)
CORS(app)

# ===== Train model on synthetic data =====
def generate_training_data(n=500):
    np.random.seed(42)
    X, y = [], []

    for _ in range(n):
        attendance = np.random.uniform(30, 100)
        gpa = np.random.uniform(2, 10)
        behavior = np.random.randint(1, 11)
        engagement = np.random.randint(1, 11)
        income = np.random.uniform(5000, 80000)
        scholarship = np.random.randint(0, 2)
        fee_pending = np.random.randint(0, 2)   # 1 = pending
        counseling = np.random.randint(0, 2)
        stress = np.random.randint(1, 11)

        # Rule-based label generation (mirrors Java logic)
        score = 0
        if attendance < 60: score += 25
        elif attendance < 75: score += 15
        elif attendance < 85: score += 5

        if gpa < 4: score += 20
        elif gpa < 6: score += 12
        elif gpa < 7.5: score += 5

        if behavior <= 3: score += 10
        elif behavior <= 5: score += 5

        if engagement <= 3: score += 10
        elif engagement <= 5: score += 5

        if fee_pending: score += 10
        if not scholarship: score += 5

        if income < 10000: score += 10
        elif income < 20000: score += 5

        if stress >= 8: score += 10
        elif stress >= 6: score += 5

        if not counseling and stress >= 6: score += 5

        if score < 35: label = 0    # LOW
        elif score < 65: label = 1  # MEDIUM
        else: label = 2             # HIGH

        X.append([attendance, gpa, behavior, engagement, income, scholarship, fee_pending, counseling, stress])
        y.append(label)

    return np.array(X), np.array(y)


X_train, y_train = generate_training_data(1000)
model = DecisionTreeClassifier(max_depth=8, random_state=42)
model.fit(X_train, y_train)
LABELS = ['LOW', 'MEDIUM', 'HIGH']


@app.route('/predict', methods=['POST'])
def predict():
    data = request.get_json()
    if not data:
        return jsonify({'error': 'No data provided'}), 400

    try:
        features = [[
            float(data['attendancePercentage']),
            float(data['gpa']),
            int(data['behaviorScore']),
            int(data['engagementLevel']),
            float(data['familyIncome']),
            1 if data.get('scholarship') else 0,
            1 if str(data.get('feeStatus', '')).upper() == 'PENDING' else 0,
            1 if data.get('counseling') else 0,
            int(data['stressLevel'])
        ]]

        pred = model.predict(features)[0]
        proba = model.predict_proba(features)[0]

        return jsonify({
            'dropoutRisk': LABELS[pred],
            'riskScore': float(proba[pred]),
            'probabilities': {
                'LOW': float(proba[0]),
                'MEDIUM': float(proba[1]),
                'HIGH': float(proba[2])
            }
        })

    except (KeyError, ValueError) as e:
        return jsonify({'error': f'Invalid input: {str(e)}'}), 400


@app.route('/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'model': 'DecisionTree'})


if __name__ == '__main__':
    app.run(port=5000, debug=True)
