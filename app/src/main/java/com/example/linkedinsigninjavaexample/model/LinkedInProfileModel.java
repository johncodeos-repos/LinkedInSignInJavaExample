package com.example.linkedinsigninjavaexample.model;


import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LinkedInProfileModel {

    private StName firstName;
    private StName lastName;
    private ProfilePicture profilePicture;
    private String id;

    public String getId() {
        return id;
    }

    public StName getFirstName() {
        return firstName;
    }

    public StName getLastName() {
        return lastName;
    }

    public ProfilePicture getProfilePicture() {
        return profilePicture;
    }

    public static class StName {
        Localized localized;

        public Localized getLocalized() {
            return localized;
        }

        public static class Localized {
            @SerializedName("en_US")
            String enUs;

            public String getEnUs() {
                return enUs;
            }
        }
    }

    public static class ProfilePicture {
        @SerializedName("displayImage~")
        DisplayImage displayImage;

        public DisplayImage getDisplayImage() {
            return displayImage;
        }

        public static class DisplayImage {
            List<Element> elements;

            public List<Element> getElements() {
                return elements;
            }

            public static class Element {
                List<Identifier> identifiers;

                public List<Identifier> getIdentifiers() {
                    return identifiers;
                }

                public static class Identifier {
                    String identifier;

                    public String getIdentifier() {
                        return identifier;
                    }

                }
            }
        }


    }
}