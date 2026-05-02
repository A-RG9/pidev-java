package com.wellora.services;

import java.util.List;
import com.wellora.model.Healthentry;

/**
 * AI-based Health Prediction Service
 * 
 * Analyzes the last 5 days of health data to predict future health state.
 * Uses weighted formula considering sleep, glycemia, weight, and symptoms.
 */
public class PredictionService {

    // Weight coefficients for the prediction formula
    private static final double SLEEP_WEIGHT = 0.3;
    private static final double GLYCEMIA_WEIGHT = 0.3;
    private static final double SYMPTOMS_WEIGHT = 0.4;

    // Normalization thresholds
    private static final double IDEAL_SLEEP_MIN = 7.0;
    private static final double IDEAL_SLEEP_MAX = 9.0;
    private static final double IDEAL_GLYCEMIA_MIN = 70.0;
    private static final double IDEAL_GLYCEMIA_MAX = 100.0;
    private static final double IDEAL_WEIGHT_MIN = 50.0;
    private static final double IDEAL_WEIGHT_MAX = 80.0;

    // Score thresholds for prediction categories
    private static final int GOOD_THRESHOLD = 70;
    private static final int MODERATE_THRESHOLD = 40;

    /**
     * Predicts health state based on the last 5 days of health entries.
     *
     * @param recentEntries List of recent Healthentry objects (last 5 days)
     * @return PredictionResult containing predicted state, emoji, and explanation
     */
    public PredictionResult predictHealthState(List<Healthentry> recentEntries) {
        if (recentEntries == null || recentEntries.isEmpty()) {
            return createDefaultPrediction();
        }

        // Calculate average values from recent entries
        double avgSleep = calculateAverageSleep(recentEntries);
        double avgGlycemia = calculateAverageGlycemia(recentEntries);
        double avgWeight = calculateAverageWeight(recentEntries);
        int totalSymptoms = calculateTotalSymptoms(recentEntries);

        // Normalize values to scores out of 100
        double sleepScore = normalizeSleepScore(avgSleep);
        double glycemiaScore = normalizeGlycemiaScore(avgGlycemia);
        double weightScore = normalizeWeightScore(avgWeight);
        double symptomsScore = normalizeSymptomsScore(totalSymptoms);

        // Compute final weighted score
        double finalScore = (sleepScore * SLEEP_WEIGHT)
                          + (glycemiaScore * GLYCEMIA_WEIGHT)
                          + (symptomsScore * SYMPTOMS_WEIGHT);

        // Determine prediction category
        String predictionState = determinePredictionState(finalScore);
        String emoji = getEmojiForState(predictionState);

        // Generate explanation
        String explanation = generateExplanation(
            predictionState, finalScore, avgSleep, avgGlycemia, avgWeight, totalSymptoms,
            sleepScore, glycemiaScore, weightScore, symptomsScore
        );

        return new PredictionResult(predictionState, emoji, explanation, finalScore);
    }

    /**
     * Creates a default prediction when no data is available.
     */
    private PredictionResult createDefaultPrediction() {
        String explanation = "Insufficient data for prediction. Please log at least one health entry to enable AI analysis.";
        return new PredictionResult("Unknown", "❓", explanation, 0.0);
    }

    /**
     * Calculates average sleep hours from recent entries.
     */
    private double calculateAverageSleep(List<Healthentry> entries) {
        return entries.stream()
                .mapToDouble(Healthentry::getSommeil)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculates average glycemia from recent entries.
     */
    private double calculateAverageGlycemia(List<Healthentry> entries) {
        return entries.stream()
                .mapToDouble(Healthentry::getGlycemie)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculates average weight from recent entries.
     */
    private double calculateAverageWeight(List<Healthentry> entries) {
        return entries.stream()
                .mapToDouble(Healthentry::getPoids)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculates total symptoms count from recent entries.
     */
    private int calculateTotalSymptoms(List<Healthentry> entries) {
        return entries.stream()
                .mapToInt(entry -> entry.getSymptoms() != null ? entry.getSymptoms().size() : 0)
                .sum();
    }

    /**
     * Normalizes sleep hours to a score out of 100.
     * Optimal range: 7-9 hours. Score decreases outside this range.
     */
    private double normalizeSleepScore(double sleepHours) {
        if (sleepHours >= IDEAL_SLEEP_MIN && sleepHours <= IDEAL_SLEEP_MAX) {
            return 100.0;
        } else if (sleepHours < IDEAL_SLEEP_MIN) {
            double deficit = IDEAL_SLEEP_MIN - sleepHours;
            return Math.max(0, 100 - (deficit * 20));
        } else {
            double excess = sleepHours - IDEAL_SLEEP_MAX;
            return Math.max(0, 100 - (excess * 15));
        }
    }

    /**
     * Normalizes glycemia to a score out of 100.
     * Optimal range: 70-100 mg/dL. Score decreases outside this range.
     */
    private double normalizeGlycemiaScore(double glycemia) {
        if (glycemia >= IDEAL_GLYCEMIA_MIN && glycemia <= IDEAL_GLYCEMIA_MAX) {
            return 100.0;
        } else if (glycemia < IDEAL_GLYCEMIA_MIN) {
            double deficit = IDEAL_GLYCEMIA_MIN - glycemia;
            return Math.max(0, 100 - (deficit * 2));
        } else {
            double excess = glycemia - IDEAL_GLYCEMIA_MAX;
            return Math.max(0, 100 - (excess * 1.5));
        }
    }

    /**
     * Normalizes weight to a score out of 100.
     * Optimal range: 50-80 kg. Score decreases outside this range.
     */
    private double normalizeWeightScore(double weight) {
        if (weight >= IDEAL_WEIGHT_MIN && weight <= IDEAL_WEIGHT_MAX) {
            return 100.0;
        } else if (weight < IDEAL_WEIGHT_MIN) {
            double deficit = IDEAL_WEIGHT_MIN - weight;
            return Math.max(0, 100 - (deficit * 3));
        } else {
            double excess = weight - IDEAL_WEIGHT_MAX;
            return Math.max(0, 100 - (excess * 2.5));
        }
    }

    /**
     * Normalizes symptoms count to a score out of 100.
     * Fewer symptoms = higher score.
     */
    private double normalizeSymptomsScore(int symptomsCount) {
        if (symptomsCount == 0) {
            return 100.0;
        } else if (symptomsCount == 1) {
            return 80.0;
        } else if (symptomsCount == 2) {
            return 60.0;
        } else if (symptomsCount == 3) {
            return 40.0;
        } else if (symptomsCount == 4) {
            return 20.0;
        } else {
            return 0.0;
        }
    }

    /**
     * Determines the prediction state based on the final score.
     */
    private String determinePredictionState(double score) {
        if (score >= GOOD_THRESHOLD) {
            return "Good";
        } else if (score >= MODERATE_THRESHOLD) {
            return "Moderate";
        } else {
            return "Risk";
        }
    }

    /**
     * Returns the emoji corresponding to the prediction state.
     */
    private String getEmojiForState(String state) {
        switch (state) {
            case "Good":
                return "😊";
            case "Moderate":
                return "😐";
            case "Risk":
                return "⚠️";
            default:
                return "❓";
        }
    }

    /**
     * Generates a detailed explanation of the prediction.
     */
    private String generateExplanation(String state, double score,
            double avgSleep, double avgGlycemia, double avgWeight, int totalSymptoms,
            double sleepScore, double glycemiaScore, double weightScore, double symptomsScore) {

        StringBuilder explanation = new StringBuilder();
        explanation.append(String.format("Based on your last %d days of health data, ",
                Math.min(5, totalSymptoms > 0 ? 5 : 1)));
        explanation.append(String.format("your predicted health state is '%s' %s ", state, getEmojiForState(state)));
        explanation.append(String.format("(Score: %.1f/100).\n\n", score));

        explanation.append("Key factors:\n");
        explanation.append(String.format("• Sleep: %.1f hrs (Score: %.0f/100) - %s\n",
                avgSleep, sleepScore, getSleepFeedback(avgSleep)));
        explanation.append(String.format("• Glycemia: %.1f g/L (Score: %.0f/100) - %s\n",
                avgGlycemia, glycemiaScore, getGlycemiaFeedback(avgGlycemia)));
        explanation.append(String.format("• Weight: %.1f kg (Score: %.0f/100) - %s\n",
                avgWeight, weightScore, getWeightFeedback(avgWeight)));
        explanation.append(String.format("• Symptoms: %d total (Score: %.0f/100) - %s\n\n",
                totalSymptoms, symptomsScore, getSymptomsFeedback(totalSymptoms)));

        explanation.append(getRecommendation(state, avgSleep, avgGlycemia, avgWeight, totalSymptoms));

        return explanation.toString();
    }

    /**
     * Provides feedback on sleep quality.
     */
    private String getSleepFeedback(double sleep) {
        if (sleep >= IDEAL_SLEEP_MIN && sleep <= IDEAL_SLEEP_MAX) {
            return "Optimal range";
        } else if (sleep < IDEAL_SLEEP_MIN) {
            return "Below recommended";
        } else {
            return "Above recommended";
        }
    }

    /**
     * Provides feedback on glycemia levels.
     */
    private String getGlycemiaFeedback(double glycemia) {
        if (glycemia >= IDEAL_GLYCEMIA_MIN && glycemia <= IDEAL_GLYCEMIA_MAX) {
            return "Normal range";
        } else if (glycemia < IDEAL_GLYCEMIA_MIN) {
            return "Below normal";
        } else {
            return "Above normal";
        }
    }

    /**
     * Provides feedback on weight status.
     */
    private String getWeightFeedback(double weight) {
        if (weight >= IDEAL_WEIGHT_MIN && weight <= IDEAL_WEIGHT_MAX) {
            return "Healthy range";
        } else if (weight < IDEAL_WEIGHT_MIN) {
            return "Below ideal";
        } else {
            return "Above ideal";
        }
    }

    /**
     * Provides feedback on symptoms.
     */
    private String getSymptomsFeedback(int symptoms) {
        if (symptoms == 0) {
            return "No symptoms reported";
        } else if (symptoms <= 2) {
            return "Mild symptoms";
        } else if (symptoms <= 4) {
            return "Moderate symptoms";
        } else {
            return "Frequent symptoms";
        }
    }

    /**
     * Generates personalized recommendations based on prediction.
     */
    private String getRecommendation(String state, double sleep, double glycemia,
            double weight, int symptoms) {
        StringBuilder rec = new StringBuilder("Recommendations:\n");

        if ("Good".equals(state)) {
            rec.append("• Maintain your current healthy habits!\n");
            if (sleep < IDEAL_SLEEP_MIN || sleep > IDEAL_SLEEP_MAX) {
                rec.append("• Try to keep sleep between 7-9 hours for optimal health.\n");
            }
        } else if ("Moderate".equals(state)) {
            rec.append("• Monitor your health indicators more closely.\n");
            if (sleep < IDEAL_SLEEP_MIN) {
                rec.append("• Aim for 7-9 hours of sleep per night.\n");
            }
            if (glycemia < IDEAL_GLYCEMIA_MIN || glycemia > IDEAL_GLYCEMIA_MAX) {
                rec.append("• Consider dietary adjustments to stabilize blood sugar.\n");
            }
            if (symptoms > 0) {
                rec.append("• Track symptom patterns and consult if they persist.\n");
            }
        } else {
            rec.append("• Please consult with a healthcare professional.\n");
            if (sleep < IDEAL_SLEEP_MIN) {
                rec.append("• Prioritize improving sleep quality and duration.\n");
            }
            if (glycemia < IDEAL_GLYCEMIA_MIN || glycemia > IDEAL_GLYCEMIA_MAX) {
                rec.append("• Monitor blood sugar levels regularly.\n");
            }
            if (symptoms > 2) {
                rec.append("• Address recurring symptoms with medical guidance.\n");
            }
        }

        return rec.toString();
    }

    /**
     * Returns a detailed explanation of the prediction methodology.
     */
    public String getPredictionExplanation() {
        return "The AI Health Prediction uses a weighted formula to analyze your recent health data:\n\n" +
               "• Sleep Quality (30% weight): Optimal range is 7-9 hours per night\n" +
               "• Glycemia Levels (30% weight): Normal range is 70-100 mg/dL\n" +
               "• Symptom Frequency (40% weight): Fewer symptoms indicate better health\n\n" +
               "Each factor is normalized to a score out of 100, then combined using the weights.\n" +
               "Final Score >= 70: Good | 40-69: Moderate | < 40: Risk\n\n" +
               "This prediction helps identify trends and potential health concerns early.";
    }

    /**
     * Result container for prediction output.
     */
    public static class PredictionResult {
        private final String state;
        private final String emoji;
        private final String explanation;
        private final double score;

        public PredictionResult(String state, String emoji, String explanation, double score) {
            this.state = state;
            this.emoji = emoji;
            this.explanation = explanation;
            this.score = score;
        }

        public String getState() {
            return state;
        }

        public String getEmoji() {
            return emoji;
        }

        public String getExplanation() {
            return explanation;
        }

        public double getScore() {
            return score;
        }

        public String getDisplayText() {
            return String.format("%s %s (%.1f/100)", emoji, state, score);
        }
    }
}
