package com.bridgecrm.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.bridgecrm.App;
import com.bridgecrm.R;
import com.bridgecrm.api.model.AuthResult;
import com.bridgecrm.api.model.LoginData;
import com.bridgecrm.manager.SessionManager;
import com.bridgecrm.ui.ActivityMediator;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import me.tatarka.rxloader.RxLoader1;
import me.tatarka.rxloader.RxLoaderManager;
import me.tatarka.rxloader.RxLoaderManagerCompat;
import me.tatarka.rxloader.RxLoaderObserver;

/**
 * A login screen that offers login via email/password.
 */
public class AuthActivity extends FragmentActivity {

    // UI references.
    @InjectView(R.id.email)
    AutoCompleteTextView mEmailView;
    @InjectView(R.id.password)
    EditText mPasswordView;
    @InjectView(R.id.login_progress)
    View mProgressView;
    @InjectView(R.id.login_form)
    View mLoginFormView;

    @Inject
    SessionManager sessionManager;
    @Inject
    ActivityMediator activityMediator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(this).component().inject(this);
        setContentView(R.layout.activity_auth);
        ButterKnife.inject(this);

        // Set up the login form.
        populateAutoComplete();

        mPasswordView.setOnEditorActionListener((textView, id, keyEvent) -> {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        );

        rxLoaderManager = RxLoaderManagerCompat.get(this);
        loginJob = rxLoaderManager.create((LoginData data) -> sessionManager.tryLogin(data),
            new RxLoaderObserver<AuthResult>() {
                @Override
                public void onStarted() {
                    showProgress(true);
                }

                @Override
                public void onNext(AuthResult authResult) {
                    activityMediator.showDashboard();
                }

                @Override
                public void onError(Throwable e) {
                    showProgress(false);
                    Toast.makeText(AuthActivity.this, R.string.error_invalid_password, Toast.LENGTH_SHORT).show();
                }
            }
        ).save();
    }

    @OnClick(R.id.email_sign_in_button)
    void onTryLogin() {
        attemptLogin();
    }

    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, new LoaderCallbacks<Cursor>() {
                @Override
                public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
                    return new CursorLoader(AuthActivity.this,
                        // Retrieve data rows for the device user's 'profile' contact.
                        Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                            ContactsContract.Contacts.Data.CONTENT_DIRECTORY
                        ), ProfileQuery.PROJECTION,

                        // Select only email addresses.
                        ContactsContract.Contacts.Data.MIMETYPE +
                            " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                        .CONTENT_ITEM_TYPE},

                        // Show primary email addresses first. Note that there won't be
                        // a primary email address if the user hasn't specified one.
                        ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
                    );
                }

                @Override
                public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
                    List<String> emails = new ArrayList<>();
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {
                        emails.add(cursor.getString(ProfileQuery.ADDRESS));
                        cursor.moveToNext();
                    }

                    addEmailsToAutoComplete(emails);
                }

                @Override
                public void onLoaderReset(Loader<Cursor> cursorLoader) {

                }
            }
        );
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginJob.save().restart(new LoginData(email, password));
        }
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1
            ).setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                              }
                          }
            );

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0
            ).setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                              }
                          }
            );
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
            new ArrayAdapter<String>(AuthActivity.this,
                android.R.layout.simple_dropdown_item_1line, emailAddressCollection
            );

        mEmailView.setAdapter(adapter);
    }

    private RxLoaderManager rxLoaderManager;
    private RxLoader1<LoginData, AuthResult> loginJob;
}



