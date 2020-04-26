package com.example.linkedinsigninjavaexample.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class LinkedInEmailModel {

    private List<ElementEmail> elements;

    public List<ElementEmail> getElements() {
        return elements;
    }

    public static class ElementEmail {
        @SerializedName("handle~")
        private Handle handle;

        public Handle getHandle() {
            return handle;
        }

        public static class Handle {
            String emailAddress;

            public String getEmailAddress() {
                return emailAddress;
            }
        }
    }

}
