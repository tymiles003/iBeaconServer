package com.aemreunal.controller;

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

import java.util.List;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import com.aemreunal.config.GlobalSettings;
import com.aemreunal.domain.Beacon;
import com.aemreunal.domain.BeaconGroup;
import com.aemreunal.domain.Project;
import com.aemreunal.service.BeaconGroupService;
import com.aemreunal.service.BeaconService;
import com.aemreunal.service.ProjectService;

@Controller
@RequestMapping("/Project/{projectId}/BeaconGroup")
public class BeaconGroupController {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private BeaconGroupService beaconGroupService;

    @Autowired
    private BeaconService beaconService;

    /**
     * Get beacon groups that belong to a project.
     *
     * @param projectId
     *     The ID of the project
     *
     * @return The list of beacon groups that belong to the project with the specified ID
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<List<BeaconGroup>> viewBeaconGroupsOfProject(
        @PathVariable Long projectId,
        @RequestParam(value = "name", required = false, defaultValue = "") String name) {
        // First check if project exists
        Project project = projectService.findById(projectId);
        if (project == null) {
            return new ResponseEntity<List<BeaconGroup>>(HttpStatus.NOT_FOUND);
        }

        if (name.equals("")) {
            return new ResponseEntity<List<BeaconGroup>>(project.getBeaconGroups(), HttpStatus.OK);
        }

        List<BeaconGroup> beaconGroups = beaconGroupService.findBeaconGroupsBySpecs(projectId, name);
        if (beaconGroups.size() == 0) {
            return new ResponseEntity<List<BeaconGroup>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<List<BeaconGroup>>(beaconGroups, HttpStatus.OK);
    }

    /**
     * Get the beacon group with specified ID
     *
     * @param projectId
     *     The ID of the project
     * @param beaconGroupId
     *     The ID of the group
     *
     * @return The beacon group
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{beaconGroupId}", produces = "application/json")
    public ResponseEntity<BeaconGroup> viewBeaconGroup(
        @PathVariable Long projectId,
        @PathVariable Long beaconGroupId) {
        // First check if project exists
        Project project = projectService.findById(projectId);
        if (project == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }

        BeaconGroup beaconGroup = beaconGroupService.findByBeaconGroupIdAndProject(beaconGroupId, project);
        if (beaconGroup == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<BeaconGroup>(beaconGroup, HttpStatus.OK);
    }

    /**
     * Get beacons that belong to to the specified beacon group
     *
     * @param projectId
     *     The ID of the project to operate in
     * @param beaconGroupId
     *     The ID of the group
     *
     * @return The list of beacons that belong to the group
     */
    @RequestMapping(method = RequestMethod.GET, value = "/{beaconGroupId}/Beacons", produces = "application/json")
    public ResponseEntity<List<Beacon>> viewBeaconGroupMembers(
        @PathVariable Long projectId,
        @PathVariable Long beaconGroupId) {
        // First check if project exists
        Project project = projectService.findById(projectId);
        if (project == null) {
            return new ResponseEntity<List<Beacon>>(HttpStatus.NOT_FOUND);
        }

        BeaconGroup beaconGroup = beaconGroupService.findByBeaconGroupIdAndProject(beaconGroupId, project);
        if (beaconGroup == null) {
            return new ResponseEntity<List<Beacon>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<List<Beacon>>(beaconGroup.getBeacons(), HttpStatus.OK);
    }

    /**
     * Create a new beacon group in project
     * <p/>
     * {@literal @}Transactional mark via http://stackoverflow.com/questions/11812432/spring-data-hibernate
     *
     * @param projectId
     *     The ID of the project to create the beacon group in
     * @param restBeaconGroup
     *     The beacon group as JSON object
     * @param builder
     *     The URI builder for post-creation redirect
     *
     * @return The created beacon group
     */
    @Transactional
    @RequestMapping(method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<BeaconGroup> createBeaconGroupInProject(
        @PathVariable Long projectId,
        @RequestBody BeaconGroup restBeaconGroup,
        UriComponentsBuilder builder) {
        Project project = projectService.findById(projectId);
        if (project == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }

        restBeaconGroup.setProject(project);
        BeaconGroup newBeaconGroup;

        try {
            newBeaconGroup = beaconGroupService.save(restBeaconGroup);
        } catch (ConstraintViolationException | TransactionSystemException e) {
            if (GlobalSettings.DEBUGGING) {
                System.err.println("Unable to save beacon group! Constraint violation detected!");
            }
            return new ResponseEntity<BeaconGroup>(HttpStatus.BAD_REQUEST);
        }

        if (GlobalSettings.DEBUGGING) {
            System.out.println("Saved beacon group with ID = \'" + newBeaconGroup.getBeaconGroupId() +
                "\' name = \'" + newBeaconGroup.getName() +
                "\' in project with ID = \'" + projectId + "\'");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path("/BeaconGroup/{id}").buildAndExpand(newBeaconGroup.getBeaconGroupId().toString()).toUri());
        return new ResponseEntity<BeaconGroup>(newBeaconGroup, headers, HttpStatus.CREATED);
    }

    /**
     * Add beacon to the specified beacon group.
     * <p/>
     * Can return 409 if beacon already has a group.
     * <p/>
     * Ex: "/BeaconGroup/1/Add?beaconId=12"
     *
     * @param projectId
     *     The ID of the project to operate in
     * @param beaconGroupId
     *     The ID of the beacon group to add the beacon to
     * @param beaconId
     *     The ID of the beacon to add
     *
     * @return The added beacon group
     */
    @Transactional
    @RequestMapping(method = RequestMethod.POST, value = "/{beaconGroupId}/AddBeaconToGroup", produces = "application/json")
    public ResponseEntity<BeaconGroup> addBeaconToGroup(
        @PathVariable Long projectId,
        @PathVariable Long beaconGroupId,
        @RequestParam(value = "beaconId", required = true) Long beaconId) {
        // First check if project exists
        Project project = projectService.findById(projectId);
        if (project == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }

        BeaconGroup beaconGroup = beaconGroupService.findByBeaconGroupIdAndProject(beaconGroupId, project);
        if (beaconGroup == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }

        Beacon beacon = beaconService.findByBeaconIdAndProject(beaconId, project);
        if (beacon == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }

        if (beacon.getGroup() != null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.CONFLICT);
        } else {
            beacon.setGroup(beaconGroup);
            beaconService.save(beacon);
            return new ResponseEntity<BeaconGroup>(beaconGroup, HttpStatus.OK);
        }
    }

    /**
     * Delete beacon from the specified beacon group.
     * <p/>
     * Can return 400 if beacon does not have a group.
     * <p/>
     * Ex: "/BeaconGroup/1/Remove?beaconId=12"
     *
     * @param projectId
     *     The ID of the project to operate in
     * @param beaconGroupId
     *     The ID of the beacon group to remove the beacon from
     * @param beaconId
     *     The ID of the beacon to remove
     *
     * @return The removed beacon group
     */
    @Transactional
    @RequestMapping(method = RequestMethod.DELETE, value = "/{beaconGroupId}/RemoveBeaconFromGroup", produces = "application/json")
    public ResponseEntity<BeaconGroup> removeBeaconFromGroup(
        @PathVariable Long projectId,
        @PathVariable Long beaconGroupId,
        @RequestParam(value = "beaconId", required = true) Long beaconId) {
        // First check if project exists
        Project project = projectService.findById(projectId);
        if (project == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }

        BeaconGroup beaconGroup = beaconGroupService.findByBeaconGroupIdAndProject(beaconGroupId, project);
        if (beaconGroup == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }

        Beacon beacon = beaconService.findByBeaconIdAndProject(beaconId, project);
        if (beacon == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
        }

        if (beacon.getGroup() == null) {
            return new ResponseEntity<BeaconGroup>(HttpStatus.BAD_REQUEST);
        }
        beacon.setGroup(null);
        beaconService.save(beacon);
        return new ResponseEntity<BeaconGroup>(beaconGroup, HttpStatus.OK);
    }


    /**
     * Delete the specified beacon group
     *
     * @param projectId
     *     The ID of the project to operate in
     * @param beaconGroupId
     *     The ID of the beacon group to delete
     *
     * @return The deleted beacon group
     */
    @Transactional
    @RequestMapping(method = RequestMethod.DELETE, value = "/{beaconGroupId}", produces = "application/json")
    public ResponseEntity<BeaconGroup> deleteBeaconGroup(
        @PathVariable Long projectId,
        @PathVariable Long beaconGroupId,
        @RequestParam(value = "confirm", required = true) String confirmation) {

        DeleteResponse response = DeleteResponse.NOT_DELETED;
        if (confirmation.toLowerCase().equals("yes")) {
            response = beaconGroupService.delete(projectId, beaconGroupId);
        }

        switch (response) {
            case DELETED:
                return new ResponseEntity<BeaconGroup>(HttpStatus.OK);
            case FORBIDDEN:
                return new ResponseEntity<BeaconGroup>(HttpStatus.FORBIDDEN);
            case NOT_FOUND:
                return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_FOUND);
            case NOT_DELETED:
                return new ResponseEntity<BeaconGroup>(HttpStatus.NOT_ACCEPTABLE);
            default:
                return new ResponseEntity<BeaconGroup>(HttpStatus.I_AM_A_TEAPOT);
        }
    }
}
