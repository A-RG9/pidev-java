package com.wellora;

import com.wellora.dao.HealthjournalDAO;
import com.wellora.model.Healthjournal;

import java.time.LocalDate;

public class TestDAO {

    public static void main(String[] args) {

        HealthjournalDAO dao = new HealthjournalDAO();

        try {
            // 👉 YOUR CODE HERE
            Healthjournal journal = new Healthjournal();
            journal.setName("Test Journal");
            journal.setDatedebut(LocalDate.now());
            journal.setDatefin(LocalDate.now().plusDays(5));

            dao.save(journal);

            System.out.println("✅ Journal inserted!");

        } catch (Exception e) {
            System.out.println("❌ ERROR:");
            e.printStackTrace();
        }
    }
}