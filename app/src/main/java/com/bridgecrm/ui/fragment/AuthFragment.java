package com.bridgecrm.ui.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.bridgecrm.App;
import com.bridgecrm.R;
import com.bridgecrm.api.model.AuthResult;
import com.bridgecrm.manager.SessionManager;
import com.bridgecrm.ui.ActivityMediator;
import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.Tools;
import com.stanfy.enroscar.async.rx.RxLoad;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import eu.inmite.android.lib.validations.form.annotations.MinLength;
import eu.inmite.android.lib.validations.form.annotations.NotEmpty;
import eu.inmite.android.lib.validations.form.annotations.RegExp;
import me.tatarka.rxloader.RxLoaderManager;
import me.tatarka.rxloader.RxLoaderManagerCompat;
import me.tatarka.rxloader.RxLoaderObserver;
import timber.log.Timber;

public class AuthFragment extends BaseBlurDialogFragment {

    // UI references.
    @NotEmpty(messageId = R.string.error_field_required, order = 1)
    @RegExp(value = RegExp.EMAIL, messageId = R.string.auth_error_invalid_email, order = 2)
    @InjectView(R.id.auth_email)
    AutoCompleteTextView emailView;

    @NotEmpty(messageId = R.string.error_field_required, order = 3)
    @MinLength(value = 4, messageId = R.string.auth_error_invalid_password, order = 4)
    @InjectView(R.id.auth_password)
    EditText passwordView;

    @Inject
    protected SessionManager sessionManager;
    @Inject
    protected ActivityMediator activityMediator;

    protected RxLoaderManager rxLoaderManager;
    private AuthFragmentOperator asyncOperator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.instance().component().inject(this);
        rxLoaderManager = RxLoaderManagerCompat.get(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        asyncOperator = AuthFragmentOperator.build().withinFragment(this).operations(this).get();
        handleEmailAutoComplete();
        populateAutoComplete();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Auth events
    ///////////////////////////////////////////////////////////////////////////

    @OnEditorAction(R.id.auth_password)
    boolean onPasswordEdit(int id) {
        if (id == getResources().getInteger(R.integer.action_done)) {
            onAuthAttempt();
            return true;
        }
        return false;
    }

    @OnClick(R.id.auth_proceed_button)
    void onAuthAttempt(){};

    class AuthCallback extends RxLoaderObserver<AuthResult> {

        @Override
        public void onStarted() {
            setCancelable(false);
            /*showProgress(true);*/
        }

        @Override
        public void onNext(AuthResult authResult) {
            activityMediator.showDashboard();
            getActivity().finish();
        }

        @Override
        public void onError(Throwable e) {
            setCancelable(true);
            /*showProgress(false);*/
            Toast.makeText(getActivity(), R.string.auth_error_incorrect_password, Toast.LENGTH_SHORT).show();
        }

    }

    ///////////////////////////////////////////////////////////////////////////
    // Data population
    ///////////////////////////////////////////////////////////////////////////

    @RxLoad
    Async<List<String>> findAccountEmails() {
        return Tools.asyncCursor(getActivity())
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

    protected void populateAutoComplete() {
        asyncOperator.forceFindAccountEmails();
    }

    protected void handleEmailAutoComplete() {
        asyncOperator.when()
            .findAccountEmailsIsFinished()
            .subscribe(
                emails -> emailView.setAdapter(new ArrayAdapter<>(getActivity(), R.layout.support_simple_spinner_dropdown_item, emails)),
                e -> Timber.w(e, "Problem getting accounts")
            );
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
