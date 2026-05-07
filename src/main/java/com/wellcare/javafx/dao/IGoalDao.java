package com.wellcare.javafx.dao;

import com.wellcare.javafx.model.Goal;

import java.util.List;

public interface IGoalDao {
    void addGoal(Goal goal);
    List<Goal> getAllGoals();
    List<Goal> getGoalsByPatient(String patientName);
    List<Goal> getGoalsByCoach(String coachId);
    void updateGoal(Goal goal);
    void deleteGoal(int id);
}
