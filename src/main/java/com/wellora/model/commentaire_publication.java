package com.wellora.model;

public class commentaire_publication {
    private int id;
    private String commentaire;
    private String date_commentaire;
    private int publication_parcours_id;

    public commentaire_publication() {}

    public commentaire_publication(String commentaire, String date_commentaire, int publication_parcours_id) {
        this.commentaire = commentaire;
        this.date_commentaire = date_commentaire;
        this.publication_parcours_id = publication_parcours_id;
    }
    public commentaire_publication(int id, String commentaire, String date_commentaire, int publication_parcours_id) {
        this.id = id;
        this.commentaire = commentaire;
        this.date_commentaire = date_commentaire;
        this.publication_parcours_id = publication_parcours_id;
    }

    public int getId() {
        return id;}

    public void setId(int id) {
        this.id = id;}

    public String getCommentaire() {return commentaire;}

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;}

    public String getDate_commentaire() {
        return date_commentaire;}

    public void setDate_commentaire(String date_commentaire) {
        this.date_commentaire = date_commentaire;
    }

    public int getPublication_parcours_id() {
        return publication_parcours_id;
    }
    public void setPublication_parcours_id(int publication_parcours_id) {
        this.publication_parcours_id = publication_parcours_id;
    }

    @Override
    public String toString() {
        return "commentaire_parcours{"  + "id=" + id + ", date_commentaire=" + date_commentaire + ", publication_id=" + publication_parcours_id + '}';
    }
}
