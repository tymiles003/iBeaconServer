package com.aemreunal.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aemreunal.config.GlobalSettings;
import com.aemreunal.controller.DeleteResponse;
import com.aemreunal.domain.Project;
import com.aemreunal.repository.project.ProjectRepository;
import com.aemreunal.repository.project.ProjectSpecs;

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
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private BeaconGroupService beaconGroupService;

    @Autowired
    private BeaconService beaconService;

    /**
     * Saves/updates the given project
     *
     * @param project
     *     The project to save/update
     *
     * @return The saved/updated project
     */
    public Project save(Project project) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Saving project with ID = \'" + project.getProjectId() + "\'");
        }

        return projectRepository.save(project);
    }

    /**
     * Returns the list of all the projects
     *
     * @return A list of all projects
     */
    public List<Project> findAll() {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding all projects");
        }

        List<Project> projectList = new ArrayList<Project>();

        for (Project project : projectRepository.findAll()) {
            projectList.add(project);
        }

        return projectList;
    }

    /**
     * Find projects conforming to specifications
     *
     * @param projectName
     *     The project Name field constraint
     * @param ownerName
     *     The project Owner's Name field constraint
     * @param ownerID
     *     The project Owner's ID field constraint
     *
     * @return The list of projects conforming to given constraints
     */
    public List<Project> findProjectsBySpecs(String projectName, String ownerName, Long ownerID) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding projects with Project Name = \'" + projectName + "\' Owner Name = \'" + ownerName + "\' ownerID = \'" + ownerID + "\'");
        }

        return projectRepository.findAll(ProjectSpecs.projectWithSpecification(projectName, ownerName, ownerID));
    }

    /**
     * Finds the project with the given ID
     *
     * @param id
     *     The ID of the project to search for
     *
     * @return The project the given ID
     */
    public Project findById(Long id) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding project with ID = \'" + id + "\'");
        }

        return projectRepository.findOne(id);
    }

    /**
     * Deletes the project with the given ID and deletes the beacons and beacon groups in
     * the group.
     *
     * @param id
     *     The ID of the project to delete
     *
     * @return Whether the project was deleted or not
     */
    public DeleteResponse delete(Long id) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Deleting project with ID = \'" + id + "\'");
        }
        if (projectRepository.exists(id)) {
            projectRepository.delete(id);
            return DeleteResponse.DELETED;
        } else {
            return DeleteResponse.NOT_FOUND;
        }
    }
}