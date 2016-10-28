/**
 * Copyright (c) 2013-2016, The SeedStack authors <http://seedstack.org>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
/*
 * Creation : 27 févr. 2015
 */
package org.seedstack.accounts;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.seedstack.accounts.internal.domain.account.Account;
import org.seedstack.accounts.internal.domain.account.AccountFactory;
import org.seedstack.business.domain.Repository;
import org.seedstack.jpa.Jpa;
import org.seedstack.seed.it.SeedITRunner;
import org.seedstack.jpa.JpaUnit;
import org.seedstack.seed.security.SecuritySupport;
import org.seedstack.seed.security.WithUser;
import org.seedstack.seed.transaction.Transactional;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SeedITRunner.class)
public class DBSecurityIT {

    private static final String ID = "Obiwan";

    private static final String PASSWORD = "Y0daRuleZ";

    @Inject
    private AccountFactory accountFactory;

    @Inject
    @Jpa
    private Repository<Account, String> accountRepository;

    @Inject
    private SecuritySupport securitySupport;

    @Inject
    private SecurityManager securityManager;

    private static boolean initialized;

    @Transactional
    @JpaUnit("account-jpa-unit")
    @Before
    public void initBase() throws Exception {
        ThreadContext.bind(securityManager);
        if (!initialized) {
            Account account = accountFactory.createAccount(ID, "5BCBD689FA503E51D3DC7A1D711EE8D851F6A70F46A83FCF",
                    "2C80E0E3779909FA6335B25EC1D4470316630D210754317E");
            account.addRole("SEED.JEDI");
            accountRepository.persist(account);
            initialized = true;
        }
    }

    @Test
    @WithUser(id = ID, password = PASSWORD)
    public void goodCredentials() {
        assertThat(securitySupport.isAuthenticated()).isTrue();
        assertThat(securitySupport.hasRole("jedi")).isTrue();
        securitySupport.logout();
    }

    @Test(expected = AuthenticationException.class)
    public void wrongPassword() {
        connectUser(ID, "S0mePa55");
        securitySupport.logout();
    }

    private void connectUser(String id, String password) {
        Subject subject = new Subject.Builder().buildSubject();
        UsernamePasswordToken token = new UsernamePasswordToken(id, password);
        subject.login(token);
        ThreadContext.bind(subject);
    }

}
