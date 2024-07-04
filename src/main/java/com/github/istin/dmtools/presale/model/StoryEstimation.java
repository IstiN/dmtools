package com.github.istin.dmtools.presale.model;

public class StoryEstimation {
    public String title;
    public Estimation androidEstimation;
    public Estimation iosEstimation;
    public Estimation flutterEstimation;
    public Estimation reactEstimation;
    public Estimation backendEstimation;

    public StoryEstimation(String title,
                           Estimation androidEstimation,
                           Estimation iosEstimation,
                           Estimation flutterEstimation,
                           Estimation reactEstimation,
                           Estimation backendEstimation) {
        this.title = title;
        this.androidEstimation = androidEstimation;
        this.iosEstimation = iosEstimation;
        this.flutterEstimation = flutterEstimation;
        this.reactEstimation = reactEstimation;
        this.backendEstimation = backendEstimation;
    }

    @Override
    public String toString() {
        return "StoryEstimation{" +
                "title='" + title + '\'' +
                ", androidEstimation=" + androidEstimation +
                ", iosEstimation=" + iosEstimation +
                ", flutterEstimation=" + flutterEstimation +
                ", reactEstimation=" + reactEstimation +
                ", backendEstimation=" + backendEstimation +
                '}';
    }
}
