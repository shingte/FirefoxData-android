/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.sync;


import org.mozilla.gecko.sync.repositories.domain.BookmarkRecord;
import org.mozilla.gecko.sync.repositories.domain.HistoryRecord;
import org.mozilla.gecko.sync.repositories.domain.PasswordRecord;
import org.mozilla.sync.login.FirefoxAccount;

import java.util.List;

/**
 * TODO:
 */
public class FirefoxSyncFirefoxAccountClient implements FirefoxSyncClient { // todo: visibility.
    private final FirefoxAccount firefoxAccount;

    public FirefoxSyncFirefoxAccountClient(final FirefoxAccount firefoxAccount) {
        this.firefoxAccount = firefoxAccount;
    }

    @Override
    public List<HistoryRecord> getHistory() {
        return null;
    }

    @Override
    public List<PasswordRecord> getPasswords() {
        return null;
    }

    @Override
    public List<BookmarkRecord> getBookmarks() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }
}
