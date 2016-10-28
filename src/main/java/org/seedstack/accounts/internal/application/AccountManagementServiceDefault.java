/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 6 mars 2015
 */
package org.seedstack.accounts.internal.application;

import org.seedstack.accounts.AccountManagementService;
import org.seedstack.accounts.internal.domain.account.Account;
import org.seedstack.accounts.internal.domain.account.AccountFactory;
import org.seedstack.accounts.internal.domain.account.Role;
import org.seedstack.business.domain.Repository;
import org.seedstack.jpa.Jpa;
import org.seedstack.seed.crypto.Hash;
import org.seedstack.seed.crypto.HashingService;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.transaction.Transactional;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Default implementation
 */
@Transactional
@JpaUnit("account-jpa-unit")
public class AccountManagementServiceDefault implements AccountManagementService {

    @Inject
    private HashingService hashingService;

    @Inject
    private AccountFactory accountFactory;

    @Inject
    @Jpa
    private Repository<Account, String> accountRepository;

    @Override
    public void createAccount(String id, String password) {
        Hash hash = hashingService.createHash(password);
        Account account = accountFactory.createAccount(id, hash.getHashAsString(), hash.getSaltAsString());
        accountRepository.persist(account);
    }

    @Override
    public Set<String> getRoles(String id) {
        Set<String> roles = new HashSet<String>();
        Account account = accountRepository.load(id);
        for (Role role : account.getRoles()) {
            roles.add(role.getName());
        }
        return roles;
    }

    @Override
    public void addRole(String id, String role) {
        Account account = accountRepository.load(id);
        account.addRole(role);
        accountRepository.save(account);
    }

    @Override
    public void removeRole(String id, String role) {
        Account account = accountRepository.load(id);
        Set<Role> roles = account.getRoles();
        for (Role currentRole : roles) {
            if (currentRole.getName().equals(role)) {
                roles.remove(currentRole);
                break;
            }
        }
    }

    @Override
    public void replaceRoles(String id, Set<String> roles) {
        Account account = accountRepository.load(id);
        Set<Role> currentRoles = account.getRoles();
        currentRoles.clear();
        for (String role : roles) {
            account.addRole(role);
        }
        accountRepository.save(account);
    }

}
