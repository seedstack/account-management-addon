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

import org.seedstack.accounts.IncorrectPasswordException;
import org.seedstack.accounts.PasswordChangeService;
import org.seedstack.accounts.internal.domain.account.Account;
import org.seedstack.business.domain.Repository;
import org.seedstack.jpa.Jpa;
import org.seedstack.seed.crypto.Hash;
import org.seedstack.seed.crypto.HashingService;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.transaction.Transactional;

import javax.inject.Inject;

/**
 * Default implementation
 */
public class PasswordChangeServiceDefault implements PasswordChangeService {

    @Inject
    private HashingService hashingService;

    @Inject
    @Jpa
    private Repository<Account, String> accountRepository;

    @Inject
    private SecuritySupport securitySupport;

    @Override
    @Transactional
    @JpaUnit("account-jpa-unit")
    public void changePassword(String currentPassword, String newPassword) throws IncorrectPasswordException {
        if (!securitySupport.isAuthenticated()) {
            throw new IllegalStateException("Only connected user can change his password");
        }
        String id = securitySupport.getIdentityPrincipal().getPrincipal().toString();
        Account account = accountRepository.load(id);
        if (account == null) {
            throw new IllegalStateException("User not connected with his database id");
        }
        if (!hashingService.validatePassword(currentPassword, new Hash(account.getHashedPassword(), account.getSalt()))) {
            throw new IncorrectPasswordException();
        }
        Hash newHash = hashingService.createHash(newPassword);
        account.setHashedPassword(newHash.getHashAsString());
        account.setSalt(newHash.getSaltAsString());
        accountRepository.save(account);
    }

}
