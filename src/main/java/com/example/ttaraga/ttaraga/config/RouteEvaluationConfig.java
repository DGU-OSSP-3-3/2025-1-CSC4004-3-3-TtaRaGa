package com.example.ttaraga.ttaraga.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "route-evaluation")
public class RouteEvaluationConfig {

    private double slopeWeight;
    private double sceneryWeight;
    private double congestionWeight;
    private double weatherWeight;

    public double getSlopeWeight() {
        return slopeWeight;
    }

    public void setSlopeWeight(double slopeWeight) {
        this.slopeWeight = slopeWeight;
    }

    public double getSceneryWeight() {
        return sceneryWeight;
    }

    public void setSceneryWeight(double sceneryWeight) {
        this.sceneryWeight = sceneryWeight;
    }

    public double getCongestionWeight() {
        return congestionWeight;
    }

    public void setCongestionWeight(double congestionWeight) {
        this.congestionWeight = congestionWeight;
    }

    public double getWeatherWeight() {
        return weatherWeight;
    }

    public void setWeatherWeight(double weatherWeight) {
        this.weatherWeight = weatherWeight;
    }
}