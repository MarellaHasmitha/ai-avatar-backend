package com.aiavatar.aibackend.dto;

public class VoiceResponse {

    private String id;
    private String name;
    private String description;
    private String gender;
    private String language;
    private String country;

    public VoiceResponse() {
    }

    public VoiceResponse(
            String id,
            String name,
            String description,
            String gender,
            String language,
            String country) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.gender = gender;
        this.language = language;
        this.country = country;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}