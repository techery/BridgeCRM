package com.bridgecrm.ui.activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
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
import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.Tools;
import com.stanfy.enroscar.async.rx.RxLoad;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import eu.inmite.android.lib.validations.form.FormValidator;
import eu.inmite.android.lib.validations.form.annotations.MinLength;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;
import eu.inmite.android.lib.validations.form.callback.SimpleErrorPopupCallback;
import me.tatarka.rxloader.RxLoader1;
import me.tatarka.rxloader.RxLoaderManager;
import me.tatarka.rxloader.RxLoaderManagerCompat;
import me.tatarka.rxloader.RxLoaderObserver;
import timber.log.Timber;

/**
 * A login screen that offers login via email/password.
 */
public class AuthActivity extends BaseActivity {

    // UI references.
    @NotEmpty(messageId = R.string.error_field_required, order = 1)
    @RegExp(value = RegExp.EMAIL, messageId = R.string.error_invalid_email, order = 2)
    @InjectView(R.id.auth_email)
    AutoCompleteTextView emailView;

    @NotEmpty(messageId = R.string.error_field_required, order = 3)
    @MinLength(value = 4, messageId = R.string.error_invalid_password, order = 4)
    @InjectView(R.id.auth_password)
    EditText passwordView;

    @InjectView(R.id.login_progress)
    View progressView;
    @InjectView(R.id.login_form)
    View loginFormView;

    @Inject
    SessionManager sessionManager;
    @Inject
    ActivityMediator activityMediator;

    private RxLoaderManager rxLoaderManager;
    private RxLoader1<LoginData, AuthResult> loginJob;

    private AuthActivityOperator asyncOperator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(this).component().inject(this);
        setContentView(R.layout.activity_auth);
        ButterKnife.inject(this);

        asyncOperator = AuthActivityOperator.build().operations(this).withinActivity(this).get();

        // Set up the login form.
        handleEmailAutoComplete();
        populateAutoComplete();

        rxLoaderManager = RxLoaderManagerCompat.get(this);
        loginJob = buildLoginJob();
    }

    @OnEditorAction(R.id.auth_password)
    boolean onPasswordEdit(int id) {
        if (id == getResources().getInteger(R.integer.action_done)) {
            onAttemptLogin();
            return true;
        }
        return false;
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    @OnClick(R.id.email_sign_in_button)
    public void onAttemptLogin() {
        boolean isValid = FormValidator.validate(this, new SimpleErrorPopupCallback(getApplicationContext(), true));
        if (isValid) {
            loginJob.restart(new LoginData(emailView.getText().toString(), passwordView.getText().toString()));
        }
    }


    private RxLoader1<LoginData, AuthResult> buildLoginJob() {
        return rxLoaderManager.create((LoginData data) -> sessionManager.tryLogin(data),
            new RxLoaderObserver<AuthResult>() {
                @Override
                public void onStarted() {
                    showProgress(true);
                }

                @Override
                public void onNext(AuthResult authResult) {
                    activityMediator.showDashboard();
                    finish();
                }

                @Override
                public void onError(Throwable e) {
                    showProgress(false);
                    Toast.makeText(AuthActivity.this, R.string.error_incorrect_password, Toast.LENGTH_SHORT).show();
                }
            }
        ).save();

    }

    @RxLoad
    Async<List<String>> findAccountEmails() {
        return Tools.asyncCursor(this)
            .uri(Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY))
            .projection(ProfileQuery.PROJECTION)
            .selection(ContactsContract.Contacts.Data.MIMETYPE + " = ?")
            .selectionArgs(new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE})
            .convert(cursor -> {
                    List<String> emails = new ArrayList<>();
                    while (cursor.moveToNext()) {
                        emails.add(cursor.getString(ProfileQuery.ADDRESS));
                    }
                    return emails;
                }
            )
            .get();
    }

    private void populateAutoComplete() {
        asyncOperator.forceFindAccountEmails();
    }

    private void handleEmailAutoComplete() {
        asyncOperator.when()
            .findAccountEmailsIsFinished()
            .subscribe(
                emails -> addEmailsToAutoComplete(emails),
                e -> Timber.w(e, "Problem getting accounts")
            );
    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(AuthActivity.this,
            R.layout.support_simple_spinner_dropdown_item, emailAddressCollection
        );
        emailView.setAdapter(adapter);
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

            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            loginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1
            ).setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                              }
                          }
            );

            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            progressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0
            ).setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  progressView.setVisibility(show ? View.VISIBLE : View.GONE);
                              }
                          }
            );
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressView.setVisibility(show ? View.VISIBLE : View.GONE);
            loginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
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

}



