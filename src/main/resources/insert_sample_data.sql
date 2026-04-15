-- Insert sample data for testing Examens and Ordonnance tables
-- Run this SQL in your MySQL database

USE wellora;

-- Insert sample Examens (Lab Results) data
INSERT INTO examens (type_examen, nom_examen, date_examen, resultat, status, notes, doctor_analysis, doctor_treatment, consultation_id) VALUES
('Blood Test', 'Complete Blood Count (CBC)', CURDATE(), 
 'hemoglobin: 14.5 g/dL\nWBC: 7,500 cells/μL\nPlatelets: 250,000/μL\nRBC: 4.8 million/μL\nHematocrit: 42%', 
 'COMPLETED', 'All values within normal range', 'Analyse normale, pas de signe d\'infection ou d\'anémie', 'Aucun traitement nécessaire', 1),

('Lipid Panel', 'Cholesterol Test', DATE_SUB(CURDATE(), INTERVAL 10 DAY),
 'Total Cholesterol: 195 mg/dL\nHDL: 55 mg/dL\nLDL: 120 mg/dL\nTriglycerides: 110 mg/dL',
 'COMPLETED', 'Borderline cholesterol levels', 'Cholestérol légèrement élevé, surveiller l\'alimentation', 'Régime méditerranéen, exercice régulier', 1),

('Urinalysis', 'Urine Test', DATE_SUB(CURDATE(), INTERVAL 3 DAY),
 'pH: 6.0\nSpecific Gravity: 1.015\nProtein: Negative\nGlucose: Negative\nNo abnormalities detected',
 'COMPLETED', 'Kidney function normal', 'Fonction rénale normale', 'Aucun traitement nécessaire', 1),

('Blood Test', 'Blood Glucose Test', DATE_SUB(CURDATE(), INTERVAL 5 DAY),
 'Fasting Glucose: 95 mg/dL\nPostprandial: 120 mg/dL\nHbA1c: 5.2%',
 'COMPLETED', 'Normal glucose levels', 'Métabolisme du glucose normal', 'Continuer une alimentation équilibrée', 1),

('Thyroid Panel', 'Thyroid Function Test', DATE_SUB(CURDATE(), INTERVAL 15 DAY),
 'TSH: 2.5 mIU/L\nT4: 1.2 ng/dL\nT3: 3.0 pg/mL',
 'COMPLETED', 'Thyroid function normal', 'Fonction thyroïdienne normale', 'Aucun traitement nécessaire', 1);

-- Insert sample Ordonnance (Prescriptions) data
INSERT INTO ordonnance (date_ordonnance, medicament, dosage, forme, duree_traitement, instructions, frequency, diagnosis_code, status, consultation_id) VALUES
(CURDATE(), 'Amoxicillin', '500mg', 'Capsule', '7 jours', 'Prendre 3 fois par jour après les repas', '3 fois/jour', 'J01.0', 'active', 1),

(CURDATE(), 'Paracetamol', '1000mg', 'Tablet', '5 jours', 'Prendre en cas de douleur ou fièvre. Maximum 3 comprimés par jour.', 'Selon besoin (max 3/jour)', 'R50.9', 'active', 1),

(DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Atorvastatin', '20mg', 'Tablet', '30 jours', 'Prendre le soir au coucher', '1 fois/jour soir', 'E78.0', 'active', 1),

(DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Metformin', '500mg', 'Tablet', '90 jours', 'Prendre deux fois par jour pendant les repas', '2 fois/jour', 'E11.9', 'active', 1),

(DATE_SUB(CURDATE(), INTERVAL 5 DAY), 'Omeprazole', '20mg', 'Capsule', '14 jours', 'Prendre le matin à jeun 30 minutes avant le petit-déjeuner', '1 fois/jour matin', 'K21.0', 'active', 1);

-- Verify the data was inserted
SELECT 'Examens inserted:' as message, COUNT(*) as count FROM examens
UNION ALL
SELECT 'Ordonnances inserted:', COUNT(*) FROM ordonnance;