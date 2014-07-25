package com.dteknoloji.domain;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.ResponseBody;
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
@JsonIgnoreProperties(value = { "links" })
public class Beacon extends ResourceSupport implements Serializable {
    // UUID hex string (including dashes) is 36 characters long
    public static final int UUID_MAX_LENGTH = 36;
    // Major hex string is 4 characters long
    public static final int MAJOR_MIN_LENGTH = 1;
    public static final int MAJOR_MAX_LENGTH = 4;
    // Minor hex string is 4 characters long
    public static final int MINOR_MIN_LENGTH = 1;
    public static final int MINOR_MAX_LENGTH = 4;
    public static final int DESCRIPTION_MAX_LENGTH = 200;

    @Id
    @Column(name = "beacon_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long beaconId;

    @Column(name = "uuid", nullable = false, length = UUID_MAX_LENGTH)
    @Size(min = UUID_MAX_LENGTH, max = UUID_MAX_LENGTH)
    private String uuid = "";

    @Column(name = "major", nullable = false, length = MAJOR_MAX_LENGTH)
    @Size(min = MAJOR_MIN_LENGTH, max = MAJOR_MAX_LENGTH)
    private String major = "";

    @Column(name = "minor", nullable = false, length = MINOR_MAX_LENGTH)
    @Size(min = MINOR_MIN_LENGTH, max = MINOR_MAX_LENGTH)
    private String minor = "";

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description = "";

    @ManyToOne(targetEntity = BeaconGroup.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = true)
    @JsonIgnoreProperties(value = { "name", "description", "beacons" })
    private BeaconGroup group;

    public Long getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(Long beaconId) {
        this.beaconId = beaconId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid.toUpperCase();
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major.toUpperCase();
    }

    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor.toUpperCase();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BeaconGroup getGroup() {
        return group;
    }

    public void setGroup(BeaconGroup group) {
        this.group = group;
    }
}