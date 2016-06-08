package com.github.mattcarrier.docker4gradle.core

class ImageConfiguration {
    String name
    String image
    List<String> env

    ImageConfiguration(String name) {
        this.name = name
    }
}
