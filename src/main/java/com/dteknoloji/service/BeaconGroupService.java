package com.dteknoloji.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dteknoloji.config.GlobalSettings;
import com.dteknoloji.domain.BeaconGroup;
import com.dteknoloji.repository.beaconGroup.BeaconGroupRepository;

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
    private BeaconGroupRepository repository;

    public BeaconGroup save(BeaconGroup beaconGroup) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Saving beacon group with ID = \'" + beaconGroup.getBeaconGroupId() + "\'");
        }

        return repository.save(beaconGroup);
    }

    public List<BeaconGroup> findAll() {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding all beacon groups");
        }

        List<BeaconGroup> beaconGroupList = new ArrayList<BeaconGroup>();

        for (BeaconGroup beaconGroup : repository.findAll()) {
            beaconGroupList.add(beaconGroup);
        }

        return beaconGroupList;
    }

    public BeaconGroup findById(Long id) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Finding beacon group with ID = \'" + id + "\'");
        }

        return repository.findOne(id);
    }

    public boolean delete(Long id) {
        if (GlobalSettings.DEBUGGING) {
            System.out.println("Deleting beacon group with ID = \'" + id + "\'");
        }

        if (id != null) {
            repository.delete(id);
            return true;
        } else {
            return false;
        }
    }

}