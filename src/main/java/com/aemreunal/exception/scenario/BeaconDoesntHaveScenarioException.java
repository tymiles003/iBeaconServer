package com.aemreunal.exception.scenario;

/*
 ***************************
 * Copyright (c) 2014      *
 *                         *
 * This code belongs to:   *
 *                         *
 * @author Ahmet Emre Ünal *
 * S001974                 *
 *                         *
 * emre@aemreunal.com      *
 * emre.unal@ozu.edu.tr    *
 *                         *
 * aemreunal.com           *
 ***************************
 */

public class BeaconDoesntHaveScenarioException extends NullPointerException {

    public BeaconDoesntHaveScenarioException(Long beaconId, Long scenarioId) {
        super("The beacon with ID " + beaconId + " is not part of scenario with ID " + scenarioId + ". No modifications have been made.");
    }
}
