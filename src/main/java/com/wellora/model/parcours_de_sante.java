package com.wellora.model;

import java.util.ArrayList;
import java.util.List;

public class parcours_de_sante {

    private int id;
    private String nom_parcours;
    private String localisation_parcours;
    private double latitude_parcours;
    private double longitude_parcours;
    private double distance_parcours;
    private String date_creation;
    private String image_parcours;
    private List<publication_parcours> publications = new ArrayList<>();
    public parcours_de_sante() {}

    public parcours_de_sante(String nom_parcours, String localisation_parcours, double latitude_parcours, double longitude_parcours, double distance_parcours, String date_creation, String image_parcours) {
        this.nom_parcours = nom_parcours;
        this.localisation_parcours = localisation_parcours;
        this.latitude_parcours = latitude_parcours;
        this.longitude_parcours = longitude_parcours;
        this.distance_parcours = distance_parcours;
        this.date_creation = date_creation;
        this.image_parcours = image_parcours;
    }

    public parcours_de_sante(int id, String nom_parcours, String localisation_parcours, double latitude_parcours, double longitude_parcours, double distance_parcours, String date_creation, String image_parcours) {
        this.id = id;
        this.nom_parcours = nom_parcours;
        this.localisation_parcours = localisation_parcours;
        this.latitude_parcours = latitude_parcours;
        this.longitude_parcours = longitude_parcours;
        this.distance_parcours = distance_parcours;
        this.date_creation = date_creation;
        this.image_parcours = image_parcours;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom_parcours() { return nom_parcours; }

    public void setNom_parcours(String nom_parcours) {
        this.nom_parcours = nom_parcours;}

    public String getLocalisation_parcours() { return localisation_parcours; }

    public void setLocalisation_parcours(String localisation_parcours) {
        this.localisation_parcours = localisation_parcours;}

    public double getLatitude_parcours() { return latitude_parcours; }

    public void setLatitude_parcours(double latitude_parcours) {
        this.latitude_parcours = latitude_parcours;}

    public double getLongitude_parcours() { return longitude_parcours; }

    public void setLongitude_parcours(double longitude_parcours) {
        this.longitude_parcours = longitude_parcours;}

    public double getDistance_parcours() { return distance_parcours; }

    public void setDistance_parcours(double distance_parcours) {
        this.distance_parcours = distance_parcours;}

    public String getDate_creation() { return date_creation; }

    public void setDate_creation(String date_creation) {
        this.date_creation = date_creation;}

    public String getImage_parcours() { return image_parcours; }

    public void setImage_parcours(String image_parcours) {
        this.image_parcours = image_parcours;}

    public List<publication_parcours> getPublications() { return publications; }

    public void setPublications(List<publication_parcours> publications) {
        this.publications = publications;
    }
    @Override
    public String toString() {
        return "parcours_de_sante{" + "id=" + id + ", nom_parcours=" + nom_parcours + ", localisation_parcours=" + localisation_parcours + ", latitude_parcours=" + latitude_parcours + ", longitude_parcours=" + longitude_parcours + ", distance_parcours=" + distance_parcours + ", image_parcours=" + image_parcours + ", date_creation=" + date_creation +", publications_count=" + publications.size() + '}';
    }

}




