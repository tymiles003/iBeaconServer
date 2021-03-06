package com.aemreunal.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
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
@Table(name = "beacon_groups")
@ResponseBody
@JsonIgnoreProperties(value = { "beacons", "project" })
public class BeaconGroup extends ResourceSupport implements Serializable {
    public static final int NAME_MAX_LENGTH        = 50;
    public static final int DESCRIPTION_MAX_LENGTH = 200;

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon group 'ID' attribute
     */
    @Id
    @Column(name = "beacon_group_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @OrderColumn
    @Access(AccessType.PROPERTY)
    private Long beaconGroupId;

    public Long getBeaconGroupId() {
        return beaconGroupId;
    }

    public void setBeaconGroupId(Long beaconGroupId) {
        this.beaconGroupId = beaconGroupId;
    }
    /*
     * END: Beacon group 'ID' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon group 'name' attribute
     */
    @Column(name = "name", nullable = false, length = NAME_MAX_LENGTH)
    @Size(min = 1, max = NAME_MAX_LENGTH)
    @Access(AccessType.PROPERTY)
    private String name = "";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    /*
     * END: Beacon group 'name' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon group 'description' attribute
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
     * END: Beacon group 'description' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon group 'beacon list' attribute
     */
   /*
    *
    * Currently, Beacon is the owner of its relationship to BeaconGroup.
    * ManyToOne are (almost) always the owner side of a bidirectional relationship in the JPA spec
    *
    * Should Beacon even know/care about which group(s) it belongs to?
    *
    * Correct mapping: 2.2.5.3.1.1. Bidirectional
    * http://docs.jboss.org/hibernate/stable/annotations/reference/en/html/entity.html
    *
    @JoinTable(name="beacon_group_members",
               joinColumns = @JoinColumn(name="beacon_group_id"),
               inverseJoinColumns = @JoinColumn(name="beacon_id")
    )
    // TODO:XNYLXIWD determine who should own this relationship
    */
    @OneToMany(targetEntity = Beacon.class,
               mappedBy = "group",
               fetch = FetchType.LAZY)
    @Access(AccessType.PROPERTY)
    // TODO @JsonIgnoreProperties(value = {})
    private Set<Beacon> beacons = new LinkedHashSet<Beacon>();

    public Set<Beacon> getBeacons() {
        CoreConfig.initLazily(beacons);
        return beacons;
    }

    public void setBeacons(Set<Beacon> beacons) {
        this.beacons = beacons;
    }

    public void addBeacon(Beacon beacon) {
        CoreConfig.initLazily(beacons);
        this.beacons.add(beacon);
    }

    public void removeBeacon(Beacon beacon) {
        CoreConfig.initLazily(beacons);
        this.beacons.remove(beacon);
    }
    /*
     * END: Beacon group 'beacon list' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon group 'project' attribute
     */
    @ManyToOne(targetEntity = Project.class,
               optional = false,
               fetch = FetchType.LAZY)
    // JoinTable & Lazy fetch-> 5.1.7: http://docs.jboss.org/hibernate/core/4.3/manual/en-US/html_single/
    @JoinTable(name = "projects_to_beacon_groups",
               joinColumns = @JoinColumn(name = "beacon_group_id"),
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
     * END: Beacon group 'project' attribute
     *------------------------------------------------------------
     */

    /*
     *------------------------------------------------------------
     * BEGIN: Beacon group 'scenario' attribute
     */
    @ManyToOne(targetEntity = Scenario.class,
               fetch = FetchType.LAZY,
               optional = false)
    @JoinTable(name = "scenarios_to_beacon_groups",
               joinColumns = @JoinColumn(name = "beacon_group_id"),
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
     * END: Beacon group 'scenario' attribute
     *------------------------------------------------------------
     */


    /*
     *------------------------------------------------------------
     * BEGIN: Beacon group 'creationDate' attribute
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
     * END: Beacon group 'creationDate' attribute
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
        if (!(obj instanceof BeaconGroup)) {
            return false;
        } else {
            return ((BeaconGroup) obj).getBeaconGroupId().equals(this.getBeaconGroupId());
        }
    }
}
