package com.aemreunal.controller.project;

import net.minidev.json.JSONObject;

import java.util.List;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import com.aemreunal.config.GlobalSettings;
import com.aemreunal.controller.DeleteResponse;
import com.aemreunal.domain.Project;
import com.aemreunal.service.ProjectService;
import com.aemreunal.service.UserService;

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

@Controller
@RequestMapping(GlobalSettings.PROJECT_PATH_MAPPING)
public class ProjectController {
    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    /**
     * Get all projects of the user. Optionally the user may search their projects by
     * name
     *
     * @param username
     *     The username of the owner of the projects
     * @param projectName
     *     (Optional) The name of the project
     *
     * @return All existing projects (Optionally, all that match the given criteria)
     */
    @RequestMapping(method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<Project>> getAllProjects(
        @PathVariable String username,
        @RequestParam(value = "name", required = false, defaultValue = "") String projectName) {
        if (projectName.equals("")) {
            return new ResponseEntity<List<Project>>(projectService.findAllBelongingTo(username), HttpStatus.OK);
        } else {
            return getProjectsWithMatchingCriteria(username, projectName);
        }
    }

    /**
     * Returns the list of projects that match a given criteria
     *
     * @param username
     *     The username of the owner of the projects
     * @param projectName
     *     (Optional) The name of the project
     *
     * @return The list of projects that match the given criteria
     */
    private ResponseEntity<List<Project>> getProjectsWithMatchingCriteria(String username, String projectName) {
        List<Project> projects = projectService.findProjectsBySpecs(username, projectName);
        if (projects.size() == 0) {
            return new ResponseEntity<List<Project>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<List<Project>>(projects, HttpStatus.OK);
    }

    /**
     * Get the project with the specified ID
     *
     * @param projectId
     *     The ID of the project
     *
     * @return The project
     */
    @RequestMapping(method = RequestMethod.GET, value = GlobalSettings.PROJECT_ID_MAPPING, produces = "application/json;charset=UTF-8")
    public ResponseEntity<Project> getProjectById(
        // TODO Handle username
        @PathVariable String username,
        @PathVariable Long projectId) {
        Project project = projectService.findById(username, projectId);
        if (project == null) {
            // TODO move null check to service as an exception
            return new ResponseEntity<Project>(HttpStatus.NOT_FOUND);
        }
        // TODO add links
        return new ResponseEntity<Project>(project, HttpStatus.OK);
    }

    /**
     * Creates a new project from the submitted JSON object in the request body.
     * <p/>
     * In the case of a constraint violation occurring during the save operation, a {@link
     * ConstraintViolationException} will be thrown from the {@link
     * com.aemreunal.service.ProjectService#save(com.aemreunal.domain.Project) save()}
     * method of {@link com.aemreunal.service.ProjectService}, propagated to this method
     * and then thrown from this one. This exception will be caught by the {@link
     * com.aemreunal.controller.project.ProjectControllerAdvice#constraintViolationExceptionHandler(javax.validation.ConstraintViolationException)
     * constraintViolationExceptionHandler()} of the {@link com.aemreunal.controller.project.ProjectControllerAdvice
     * ProjectControllerAdvice} class.
     *
     * @param projectFromJson
     *     The project as JSON object
     * @param builder
     *     The URI builder for post-creation redirect
     *
     * @return The created project
     *
     * @throws ConstraintViolationException
     */
    @RequestMapping(method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public ResponseEntity<JSONObject> createProject(
        @PathVariable String username,
        @RequestBody Project projectFromJson,
        UriComponentsBuilder builder)
        throws ConstraintViolationException {
        Project savedProject;
        savedProject = projectService.save(username, projectFromJson);
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Saved project with Name = \'" + savedProject.getName() + "\' ID = \'" + savedProject.getProjectId() + "\'");
        }
        String projectSecret = projectService.resetSecret(username, savedProject);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(builder.path(GlobalSettings.PROJECT_SPECIFIC_MAPPING)
                                   .buildAndExpand(
                                       username,
                                       savedProject.getProjectId().toString())
                                   .toUri());
        return new ResponseEntity<JSONObject>(savedProject.getCreateResponse(projectSecret), headers, HttpStatus.CREATED);
    }

//    /**
//     * Adds the HATEOAS links to the project object. Object must be saved afterwards to
//     * ensure persistance of added links.
//     *
//     * @param project
//     *     Project to add the links to
//     *
//     * @return The project with links added
//     */
    // TODO https://github.com/spring-projects/spring-hateoas#link-builder
    /*
    Produces:
        "links": [
            {
              "rel": "self",
              "href": "http://localhost:8080/project/2"
            },
            {
              "rel": "beacons",
              "href": "http://localhost:8080/project/2/beacon?uuid=&major=&minor="
            },
            {
              "rel": "groups",
              "href": "http://localhost:8080/project/2/beacongroup?name="
            }
        ]
    private Project addLinks(Project project) {
        project.getLinks().add(linkTo(methodOn(ProjectController.class).getProjectById(project.getProjectId(), "<Project secret>")).withSelfRel());
        project.getLinks().add(ControllerLinkBuilder.linkTo(methodOn(BeaconController.class).viewBeaconsOfProject(project.getProjectId(), "", "", "")).withRel("beacons"));
        project.getLinks().add(ControllerLinkBuilder.linkTo(methodOn(BeaconGroupController.class).viewBeaconGroupsOfProject(project.getProjectId(), "")).withRel("groups"));
        return project;
    }
    */

    /**
     * Delete the specified project, along with all the beacons, beacon groups and
     * scenarios in the project.
     * <p/>
     * To delete the project, confirmation must be supplied as a URI parameter, in the
     * form of "?confirm=yes". If not supplied, the project will not be deleted.
     *
     * @param username
     *     The username of the owner of the project to delete
     * @param projectId
     *     The ID of the project to delete
     * @param confirmation
     *     The confirmation parameter
     *
     * @return The status of deletion action
     */
    @RequestMapping(method = RequestMethod.DELETE, value = GlobalSettings.PROJECT_ID_MAPPING)
    public ResponseEntity<Project> deleteProject(
        // TODO Handle username
        @PathVariable String username,
        @PathVariable Long projectId,
        @RequestParam(value = "confirm", required = true) String confirmation) {

        DeleteResponse response = DeleteResponse.NOT_DELETED;
        if (confirmation.toLowerCase().equals("yes")) {
            response = projectService.delete(username, projectId);
        }

        // TODO clear up responses
        switch (response) {
            case DELETED:
                return new ResponseEntity<Project>(HttpStatus.OK);
            case FORBIDDEN:
                return new ResponseEntity<Project>(HttpStatus.FORBIDDEN);
            case NOT_FOUND:
                return new ResponseEntity<Project>(HttpStatus.NOT_FOUND);
            case NOT_DELETED:
                return new ResponseEntity<Project>(HttpStatus.PRECONDITION_FAILED);
            default:
                return new ResponseEntity<Project>(HttpStatus.I_AM_A_TEAPOT);
        }
    }
}