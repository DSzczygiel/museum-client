package com.museumsystem.museumclient.dto;
import java.io.Serializable;
import java.util.Arrays;

public class ArtworkDto implements Serializable {
    Long id;
    String title;
    Integer creationYear;
    ArtistDto artist;
    byte[] photo;
    String description;
    String maintenanceDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Integer getCreationYear() {
        return creationYear;
    }
    public void setCreationYear(Integer creationYear) {
        this.creationYear = creationYear;
    }
    public ArtistDto getArtist() {
        return artist;
    }
    public void setArtist(ArtistDto artist) {
        this.artist = artist;
    }
    public byte[] getPhoto() {
        return photo;
    }
    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getMaintenanceDate() {
        return maintenanceDate;
    }

    public void setMaintenanceDate(String maintenanceDate) {
        this.maintenanceDate = maintenanceDate;
    }

    @Override
    public String toString() {
        return "ArtworkDto [title=" + title + ", creationDate=" + creationYear + ", artist=" + artist.toString() + ", photo="
                + Arrays.toString(photo) + ", description=" + description + "]";
    }


}
