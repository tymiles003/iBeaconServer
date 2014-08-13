package com.aemreunal.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aemreunal.config.GlobalSettings;
import com.aemreunal.controller.DeleteResponse;
import com.aemreunal.domain.Beacon;
import com.aemreunal.domain.BeaconGroup;
import com.aemreunal.domain.Project;
import com.aemreunal.repository.beaconGroup.BeaconGroupRepo;
import com.aemreunal.repository.beaconGroup.BeaconGroupSpecs;

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
public class BeaconGroupService {

    @Autowired
    private BeaconGroupRepo beaconGroupRepo;

    @Autowired
    private BeaconService beaconService;

    /**
     * Saves/updates the given beacon group
     *
     * @param beaconGroup
     *     The beacon group to save/update
     *
     * @return The saved/updated beacon group
     */
    public BeaconGroup save(BeaconGroup beaconGroup) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Saving beacon group with ID = \'" + beaconGroup.getBeaconGroupId() + "\'");
        }

        return beaconGroupRepo.save(beaconGroup);
    }

    /**
     * Returns the list of all the beacon groups
     *
     * @return A list of all beacon groups
     */
    public List<BeaconGroup> findAll() {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding all beacon groups");
        }

        List<BeaconGroup> beaconGroupList = new ArrayList<BeaconGroup>();

        for (BeaconGroup beaconGroup : beaconGroupRepo.findAll()) {
            beaconGroupList.add(beaconGroup);
        }

        return beaconGroupList;
    }

    /**
     * Finds the beacon group with the given ID in the given project
     *
     * @param beaconGroupId
     *     The ID of the beacon group to search for
     * @param project
     *     The project to search in
     *
     * @return The beacon group with the given ID
     */
    public BeaconGroup findByBeaconGroupIdAndProject(Long beaconGroupId, Project project) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding beacon group with ID = \'" + beaconGroupId + "\'");
        }
        return beaconGroupRepo.findByBeaconGroupIdAndProject(beaconGroupId, project);
    }

    /**
     * Finds the beacon groups conforming to given specifications
     *
     * @param projectId
     *     The project ID constraint
     * @param name
     *     The name field constraint
     *
     * @return The list of beacon groups conforming to given constraints
     */
    public List<BeaconGroup> findBeaconGroupsBySpecs(Long projectId, String name) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding beacon groups with projectID = \'" + projectId + "\' and name =\'" + name + "\'");
        }
        return beaconGroupRepo.findAll(BeaconGroupSpecs.beaconWithSpecification(projectId, name));
    }

    /**
     * Deletes the beacon group with the given ID and updates the beacons in the group.
     *
     * @param projectId
     *     The ID of the project to delete the beacon from
     * @param beaconGroupId
     *     The ID of the beacon group to delete
     *
     * @return Whether the beacon group was deleted or not
     */
    public DeleteResponse delete(Long projectId, Long beaconGroupId) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Deleting beacon group with ID = \'" + beaconGroupId + "\'");
        }
        if (isMember(projectId, beaconGroupId)) {
            /*
             * Because beacons own the relationship to their beaconGroups, they must be updated first.
             * For more info, refer to To-Do ID:XNYLXIWD
             */
            updateBeaconsInGroup(beaconGroupId);
            if (GlobalSettings.DEBUGGING) {
                System.out.println("Project " + projectId + " has beacon group " + beaconGroupId + ", deleting.");
            }
            beaconGroupRepo.delete(beaconGroupId);
            return DeleteResponse.DELETED;
        } else {
            if (GlobalSettings.DEBUGGING) {
                System.out.println("Project " + projectId + " does not have beacon group " + beaconGroupId + ".");
            }
            return DeleteResponse.NOT_FOUND;
        }
    }

    /**
     * Checks whether the given beacon group belongs to the given project
     *
     * @param projectId
     *     The ID of the project to check in
     * @param beaconGroupId
     *     The ID of the beacon group to check for
     *
     * @return Whether the beacon group belongs to the project
     */
    public boolean isMember(Long projectId, Long beaconGroupId) {
        List<BeaconGroup> beaconGroups = beaconGroupRepo.findAll(BeaconGroupSpecs.beaconExistsSpecification(projectId, beaconGroupId));
        return beaconGroups.size() >= 1;
    }

    /**
     * Removes the association between a group and the beacons in the group
     *
     * @param id
     *     The ID of the group
     */
    private void updateBeaconsInGroup(Long id) {
        for (Beacon beacon : beaconGroupRepo.findOne(id).getBeacons()) {
            beacon.setGroup(null);
            beaconService.save(beacon);
        }
    }
}
