package entities;
import java.util.ArrayList;
import java.util.List;

public class publication_parcours {

    private int id;
    private String image_publication;
    private int ambiance;
    private int securite;
    private String date_publication;
    private String text_publication;
    private String experience;
    private String type_publication;
    private int parcours_de_sante_id;
    private List<commentaire_publication> commentaires = new ArrayList<>();

    public publication_parcours(){}

    public publication_parcours( String image_publication, int ambiance, int securite, String date_publication, String text_publication, String experience, String type_publication,  int parcours_de_sante_id) {
        this.image_publication= image_publication;
        this.ambiance= ambiance;
        this.securite= securite;
        this.date_publication= date_publication;
        this.text_publication= text_publication;
        this.experience= experience;
        this.type_publication= type_publication;
        this.parcours_de_sante_id= parcours_de_sante_id;
    }
    public publication_parcours( int id, String image_publication, int ambiance, int securite, String date_publication, String text_publication, String experience, String type_publication,   int parcours_de_sante_id) {
        this.id = id;
        this.image_publication= image_publication;
        this.ambiance= ambiance;
        this.securite= securite;
        this.date_publication= date_publication;
        this.text_publication= text_publication;
        this.experience= experience;
        this.type_publication= type_publication;
        this.parcours_de_sante_id= parcours_de_sante_id;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getImage_publication() {
        return image_publication;
    }
    public void setImage_publication(String image_publication) {
        this.image_publication = image_publication;
    }
    public int getAmbiance() {
        return ambiance;
    }
    public void setAmbiance(int ambiance) {
        this.ambiance = ambiance;
    }
    public int getSecurite() {
        return securite;
    }
    public void setSecurite(int securite) {
        this.securite = securite;
    }
    public String getDate_publication() {
        return date_publication;
    }
    public void setDate_publication(String date_publication) {
        this.date_publication = date_publication;
    }
    public String getText_publication() {
        return text_publication;
    }
    public void setText_publication(String text_publication) {
        this.text_publication = text_publication;
    }
    public String getExperience() {
        return experience;
    }
    public void setExperience(String experience) {
        this.experience = experience;
    }
    public String getType_publication() {
        return type_publication;
    }
    public void setType_publication(String type_publication) {
        this.type_publication = type_publication;
    }
    public int getParcours_de_sante_id() {
        return parcours_de_sante_id; }

    public void setParcours_de_sante_id(int parcours_de_sante_id) {
        this.parcours_de_sante_id = parcours_de_sante_id;
    }

    public List<commentaire_publication> getCommentaires() {
        return commentaires;
    }
    public void setCommentaires(List<commentaire_publication> commentaires) {
        this.commentaires = commentaires;
    }

    @Override
    public String toString() {
        return "publication_parcours{" + "id=" + id + ", image_publication=" + image_publication + ", ambiance=" + ambiance + "securite=" + securite + "date_publication=" + date_publication + "text_publication=" + text_publication + "experience=" + experience + ", type_publication=" + type_publication + ", parcours_id=" + parcours_de_sante_id + ", comments_count=" + commentaires.size() + '}';

    }
}
