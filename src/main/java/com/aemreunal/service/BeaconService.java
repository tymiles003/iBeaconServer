package com.aemreunal.service;

import java.util.ArrayList;
import java.util.List;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aemreunal.config.GlobalSettings;
import com.aemreunal.domain.Beacon;
import com.aemreunal.domain.Project;
import com.aemreunal.exception.beacon.BeaconAlreadyExistsException;
import com.aemreunal.exception.beacon.BeaconNotFoundException;
import com.aemreunal.exception.project.ProjectNotFoundException;
import com.aemreunal.repository.beacon.BeaconRepo;
import com.aemreunal.repository.beacon.BeaconSpecs;

/*
 **************************
 * Copyright (c) 2014     *
 *                        *
 * This code belongs to:  *
 *                        *
 * Ahmet Emre Ünal        *
 * S001974                *
 *                        *
 * aemreunal@gmail.com    *
 * emre.unal@ozu.edu.tr   *
 *                        *
 * aemreunal.com          *
 **************************
 */

@Transactional
@Service
public class BeaconService {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private BeaconRepo beaconRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Saves/updates the given beacon
     *
     * @param beacon
     *     The beacon to save/update
     *
     * @return The saved/updated beacon
     */
    public Beacon save(String username, Long projectId, Beacon beacon) throws ConstraintViolationException, BeaconAlreadyExistsException {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Saving beacon with ID = \'" + beacon.getBeaconId() + "\'");
        }
        // Even though the 'project' variable is only used inside the if-clause,
        // the Project is found no matter what to ensure it exists and legitimate.
        Project project = projectService.findProjectById(username, projectId);
        if (beacon.getProject() == null) {
            // This means it hasn't been saved yet
            if(beaconExists(username, projectId, beacon)) {
                // First, verify the beacon doesn't already exist
                throw new BeaconAlreadyExistsException(beacon);
            }
            beacon.setProject(project);
        }
        return beaconRepo.save(beacon);
    }

    private boolean beaconExists(String username, Long projectId, Beacon beacon) {
        List<Beacon> beacons = searchBeaconsBySpecs(username, projectId, beacon.getUuid(), beacon.getMajor(), beacon.getMinor());
        return beacons.size() != 0;
    }

    /**
     * Finds beacons conforming to given specifications
     *
     * @param projectId
     *     The project ID constraint
     * @param uuid
     *     The UUID field constraint
     * @param major
     *     The Major field constraint
     * @param minor
     *     The Minor field constraint
     *
     * @return The list of beacons conforming to given constraints
     */
    public List<Beacon> findBeaconsBySpecs(String username,
                                           Long projectId,
                                           String uuid,
                                           String major,
                                           String minor)
        throws BeaconNotFoundException {
        List<Beacon> beacons = searchBeaconsBySpecs(username, projectId, uuid, major, minor);
        if (beacons.size() == 0) {
            throw new BeaconNotFoundException();
        }
        return beacons;
    }

    private List<Beacon> searchBeaconsBySpecs(String username, Long projectId, String uuid, String major, String minor) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding beacons with UUID = \'" + uuid + "\' major = \'" + major + "\' minor = \'" + minor + "\'");
        }
        Project project = projectService.findProjectById(username, projectId);
        return beaconRepo.findAll(BeaconSpecs.beaconWithSpecification(project.getProjectId(), uuid, major, minor));
    }

    public Beacon queryForBeacon(String uuid,
                                 String major,
                                 String minor,
                                 String projectSecret)
    throws BeaconNotFoundException {
        List beaconObjects = beaconRepo.findAll(BeaconSpecs.beaconWithSpecification(null, uuid, major, minor));
        for (Object beaconObject : beaconObjects) {
            Beacon beacon = (Beacon) beaconObject;
            if(passwordEncoder.matches(projectSecret, beacon.getProject().getProjectSecret())) {
                return beacon;
            }
        }
        throw new BeaconNotFoundException();
    }

    /**
     * Finds the {@link com.aemreunal.domain.Beacon beacon} with the specified projectId
     * in a {@link com.aemreunal.domain.Project project}.
     *
     * @param username
     *     The username of the {@link com.aemreunal.domain.User owner} of the project
     * @param projectId
     *     The ID of the project
     * @param beaconId
     *     The ID of the beacon to find
     *
     * @return The beacon
     *
     * @throws com.aemreunal.exception.beacon.BeaconNotFoundException
     *     If the specified beacon does not exist.
     * @throws com.aemreunal.exception.project.ProjectNotFoundException
     *     If the specified project does not exist.
     */
    public Beacon getBeacon(String username, Long projectId, Long beaconId) throws BeaconNotFoundException, ProjectNotFoundException {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding beacon with ID = \'" + beaconId + "\' in project = \'" + projectId + "\'");
        }
        Project project = projectService.findProjectById(username, projectId);
        Beacon beacon = beaconRepo.findByBeaconIdAndProject(beaconId, project);
        if (beacon == null) {
            throw new BeaconNotFoundException(beaconId);
        }
        return beacon;
    }

    /**
     * Returns the list of {@link com.aemreunal.domain.Beacon beacons} that belong to a
     * {@link com.aemreunal.domain.Project project}.
     *
     * @param username
     *     The username of the {@link com.aemreunal.domain.User owner} of the project
     * @param projectId
     *     The ID of the project
     *
     * @return The list of beacons that belong to a project. Returns an empty list if the
     * project has no beacons
     */
    public List<Beacon> getBeaconsOfProject(String username, Long projectId) {
        Project project = projectService.findProjectById(username, projectId);
        List<Beacon> beacons = new ArrayList<Beacon>();
        for (Beacon beacon : project.getBeacons()) {
            beacons.add(beacon);
        }
        return beacons;
    }

    public Beacon delete(String username, Long projectId, Long beaconId) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Deleting beacon with ID = \'" + beaconId + "\'");
        }

        Beacon beacon = this.getBeacon(username, projectId, beaconId);
        beaconRepo.delete(beaconId);
        return beacon;
    }
}
