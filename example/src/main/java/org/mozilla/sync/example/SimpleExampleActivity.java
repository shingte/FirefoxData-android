/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.sync.example;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import org.mozilla.sync.FirefoxSync;
import org.mozilla.sync.FirefoxSyncException;
import org.mozilla.sync.example.accountsexample.R;
import org.mozilla.sync.login.FirefoxSyncLoginManager;
import org.mozilla.sync.sync.BookmarkFolder;
import org.mozilla.sync.sync.BookmarkRecord;
import org.mozilla.sync.sync.FirefoxSyncClient;
import org.mozilla.sync.sync.HistoryRecord;
import org.mozilla.sync.sync.PasswordRecord;

import java.util.List;

/**
 * A simple example implementation of an Activity using Firefox Data.
 *
 * When the app starts, if the user is not yet signed in, they will be prompted to login in.
 * After sign in, or if the user was already signed in when the app started, the user's Firefox
 * data will be printed to logcat (or an error if there was a failure).
 *
 * Once the user's data is printed, they'll be able to sign out via the on-screen button.
 *
 * That's it! If the user wishes to take more action, they have to close the app, reopen it, and start over.
 */
public class SimpleExampleActivity extends AppCompatActivity {

    private static final String LOGTAG = "SimpleExampleActivity";

    private FirefoxSyncLoginManager loginManager;

    private Button signOutButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_example);
        getSupportActionBar().setTitle(SimpleExampleActivity.class.getSimpleName());
        initSignOutButton();

        // Store a reference to the login manager, which is used to get a FirefoxSyncClient.
        loginManager = FirefoxSync.getLoginManager(this);

        // Prompt for login or just print data.
        final FirefoxSyncLoginManager.LoginCallback callback = new SimpleExampleActivity.LogFirefoxDataLoginCallback(this, signOutButton);
        if (!loginManager.isSignedIn()) {
            final String applicationName = getResources().getString(R.string.app_name);
            loginManager.promptLogin(this, applicationName, callback);
        } else {
            loginManager.loadStoredSyncAccount(callback);
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Required callback.
        loginManager.onActivityResult(requestCode, resultCode, data);
    }

    // Make static to avoid keeping a reference to Context, which could leak memory.
    private static class LogFirefoxDataLoginCallback implements FirefoxSyncLoginManager.LoginCallback {
        private final Handler uiThread;
        private final Button signOutButton;

        private LogFirefoxDataLoginCallback(final Context context, final Button signOutButton) {
            uiThread = new Handler(context.getMainLooper());
            this.signOutButton = signOutButton;
        }

        // All callbacks are called from a background thread.
        @Override
        public void onSuccess(final FirefoxSyncClient syncClient) {
            getFirefoxDataAndLog(syncClient);
            uiThread.post(new Runnable() {
                @Override
                public void run() {
                    signOutButton.setEnabled(true); // can only be called from UI thread.
                }
            });
        }

        @Override
        public void onFailure(final FirefoxSyncException e) {
            Log.e(LOGTAG, "onFailure: failed to create a data client so we can't access Firefox data. Try again later", e);
        }

        @Override
        public void onUserCancel() { // Note: never called for loadStoredSyncAccount.
            Log.d(LOGTAG, "onUserCancel: user cancelled the login flow.");
        }

        private void getFirefoxDataAndLog(final FirefoxSyncClient syncClient) {
            final List<HistoryRecord> receivedHistory;
            final List<PasswordRecord> receivedPasswords;
            final BookmarkFolder rootBookmark;
            try {
                receivedHistory = syncClient.getAllHistory().getResult();
                receivedPasswords = syncClient.getAllPasswords().getResult();
                rootBookmark = syncClient.getAllBookmarks().getResult();
            } catch (final FirefoxSyncException e) {
                Log.e(LOGTAG, "getFirefoxDataAndLog: failed to receive data. Try again later", e);
                return;
            }

            Log.d(LOGTAG, "--- HISTORY ---");
            for (final HistoryRecord record : receivedHistory) {
                Log.d(LOGTAG, record.getTitle() + ": " + record.getURI());
            }

            Log.d(LOGTAG, "--- PASSWORDS ---");
            for (final PasswordRecord record : receivedPasswords) {
                Log.d(LOGTAG, record.getHostname() + ": " + record.getUsername());
            }

            // Note: bookmarks are a tree-like structure. For simplicity, we only print the root's children.
            Log.d(LOGTAG, "--- BOOKMARKS ---");
            Log.d(LOGTAG, "root: " + rootBookmark.getTitle());
            for (final BookmarkRecord record : rootBookmark.getBookmarks()) {
                Log.d(LOGTAG, "root child: " + record.getTitle() + ": " + record.getURI());
            }
            for (final BookmarkFolder folder : rootBookmark.getSubfolders()) {
                Log.d(LOGTAG, "root subfolder: " + folder.getTitle());
            }
        }
    }

    private void initSignOutButton() {
        signOutButton = (Button) findViewById(R.id.sign_out_button);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                v.setEnabled(false);
                loginManager.signOut();
                Toast.makeText(SimpleExampleActivity.this, R.string.simple_example_sign_out, Toast.LENGTH_LONG).show();
            }
        });
    }
}
