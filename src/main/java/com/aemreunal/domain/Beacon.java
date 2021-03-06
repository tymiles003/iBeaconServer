package com.aemreunal.domain;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import javax.validation.constraints.Size;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.ResponseBody;
import com.aemreunal.config.CoreConfig;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

@Entity
@Table(name = "beacons")
@ResponseBody
@JsonIgnoreProperties(value = { "project" })
public class Beacon extends ResourceSupport implements Serializable {
    // UUID hex string (including dashes) is 36 characters long
    public static final int UUID_MAX_LENGTH        = 36;
    // Major hex string is 4 characters long
    public static final int MAJOR_MIN_LENGTH       = 1;
    public static final int MAJOR_MAX_LENGTH       = 4;
    // Minor hex string is 4 characters long
    public static final int MINOR_MIN_LENGTH       = 1;
    public static final int MINOR_MAX_LENGTH       = 4;
    public static final int DESCRIPTION_MAX_LENGTH = 200;

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'ID' attribute
     */
    @Id
    @Column(name = "beacon_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @OrderColumn
    @Access(AccessType.PROPERTY)
    private Long beaconId;

    public Long getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(Long beaconId) {
        this.beaconId = beaconId;
    }
    /*
     * END: Beacon 'ID' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'UUID' attribute
     */
    @Column(name = "uuid", nullable = false, length = UUID_MAX_LENGTH)
    @Size(min = UUID_MAX_LENGTH, max = UUID_MAX_LENGTH)
    @Access(AccessType.PROPERTY)
    private String uuid = "";

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid.toUpperCase();
    }
    /*
     * END: Beacon 'UUID' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'Major' attribute
     */
    @Column(name = "major", nullable = false, length = MAJOR_MAX_LENGTH)
    @Size(min = MAJOR_MIN_LENGTH, max = MAJOR_MAX_LENGTH)
    @Access(AccessType.PROPERTY)
    private String major = "";

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major.toUpperCase();
    }
    /*
     * END: Beacon 'Major' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'Minor' attribute
     */
    @Column(name = "minor", nullable = false, length = MINOR_MAX_LENGTH)
    @Size(min = MINOR_MIN_LENGTH, max = MINOR_MAX_LENGTH)
    @Access(AccessType.PROPERTY)
    private String minor = "";

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor.toUpperCase();
    }
    /*
     * END: Beacon 'Minor' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'description' attribute
     */
    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    @Size(max = DESCRIPTION_MAX_LENGTH)
    @Access(AccessType.PROPERTY)
    private String description = "";

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    /*
     * END: Beacon 'description' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'group' attribute
     */
    @ManyToOne(targetEntity = BeaconGroup.class,
               fetch = FetchType.LAZY,
               optional = true)
    // JoinTable & Lazy fetch-> 5.1.7: http://docs.jboss.org/hibernate/core/4.3/manual/en-US/html_single/
    @JoinTable(name = "beacon_groups_to_beacons",
               joinColumns = @JoinColumn(name = "beacon_id"),
               inverseJoinColumns = @JoinColumn(name = "beacon_group_id"))
    @Access(AccessType.PROPERTY)
    private BeaconGroup group;

    public BeaconGroup getGroup() {
        CoreConfig.initLazily(group);
        return group;
    }

    public void setGroup(BeaconGroup group) {
        this.group = group;
    }
    /*
     * END: Beacon 'group' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'project' attribute
     */
    @ManyToOne(targetEntity = Project.class,
               fetch = FetchType.LAZY,
               optional = false)
    // JoinTable & Lazy fetch-> 5.1.7: http://docs.jboss.org/hibernate/core/4.3/manual/en-US/html_single/
    @JoinTable(name = "projects_to_beacons",
               joinColumns = @JoinColumn(name = "beacon_id"),
               inverseJoinColumns = @JoinColumn(name = "project_id"))
    @Access(AccessType.PROPERTY)
    private Project project;

    public Project getProject() {
        CoreConfig.initLazily(project);
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    /*
     * END: Beacon 'project' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'scenario' attribute
     */
    @ManyToOne(targetEntity = Scenario.class,
               fetch = FetchType.LAZY,
               optional = false)
    @JoinTable(name = "scenarios_to_beacons",
               joinColumns = @JoinColumn(name = "beacon_id"),
               inverseJoinColumns = @JoinColumn(name = "scenario_id"))
    @Access(AccessType.PROPERTY)
    private Scenario scenario;

    public Scenario getScenario() {
        CoreConfig.initLazily(scenario);
        return scenario;
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }
    /*
     * END: Beacon 'scenario' attribute
     *------------------------------------------------------------
     */


    /*
     *------------------------------------------------------------
     * BEGIN: Beacon 'creationDate' attribute
     */
    @Column(name = "creation_date", nullable = false)
    @Access(AccessType.PROPERTY)
    private Date creationDate = null;

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /*
     * END: Beacon 'creationDate' attribute
     *------------------------------------------------------------
     */

    @PrePersist
    private void setInitialProperties() {
        // Set beacon creation date
        if (creationDate == null) {
            setCreationDate(new Date());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Beacon)) {
            return false;
        } else {
            return ((Beacon) obj).getBeaconId() == this.getBeaconId();
        }
    }
}
