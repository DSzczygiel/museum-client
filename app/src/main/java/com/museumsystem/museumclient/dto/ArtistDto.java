package com.museumsystem.museumclient.dto;

import java.io.Serializable;

public class ArtistDto implements Serializable {
    Long id;
    String name;
    String description;
    String birthDate;
    String deathDate;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getBirthDate() {
        return birthDate;
    }
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }
    public String getDeathDate() {
        return deathDate;
    }
    public void setDeathDate(String deathDate) {
        this.deathDate = deathDate;
    }
    @Override
    public String toString() {
        return "ArtistDto [name=" + name + ", description=" + description + ", birthDate=" + birthDate + ", deathDate="
                + deathDate + "]";
    }


}

