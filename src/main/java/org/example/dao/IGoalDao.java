package org.example.dao;

import org.example.models.Goal;
import java.util.List;

public interface IGoalDao {
    void addGoal(Goal goal);
    List<Goal> getAllGoals();
    List<Goal> getGoalsByPatient(String patientName);
    void updateGoal(Goal goal);
    void deleteGoal(int id);
}
