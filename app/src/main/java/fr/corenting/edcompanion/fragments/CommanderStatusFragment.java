package fr.corenting.edcompanion.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.NumberFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.corenting.edcompanion.R;
import fr.corenting.edcompanion.activities.LoginActivity;
import fr.corenting.edcompanion.activities.SettingsActivity;
import fr.corenting.edcompanion.models.events.CommanderPosition;
import fr.corenting.edcompanion.models.events.Credits;
import fr.corenting.edcompanion.models.events.FrontierAuthNeeded;
import fr.corenting.edcompanion.models.events.Ranks;
import fr.corenting.edcompanion.network.player.PlayerNetwork;
import fr.corenting.edcompanion.utils.InternalNamingUtils;
import fr.corenting.edcompanion.utils.MathUtils;
import fr.corenting.edcompanion.utils.MiscUtils;
import fr.corenting.edcompanion.utils.NotificationsUtils;
import fr.corenting.edcompanion.utils.OAuthUtils;
import fr.corenting.edcompanion.utils.PlayerNetworkUtils;
import fr.corenting.edcompanion.utils.RankUtils;
import fr.corenting.edcompanion.utils.SettingsUtils;

public class CommanderStatusFragment extends Fragment {

    public static final String COMMANDER_STATUS_FRAGMENT = "commander_status_fragment";

    @BindView(R.id.swipeContainer)
    public SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.commanderNameTextView)
    public TextView commanderNameTextView;

    @BindView(R.id.creditsContainer)
    public RelativeLayout creditsContainer;
    @BindView(R.id.creditsTextView)
    public TextView creditsTextView;

    @BindView(R.id.locationContainer)
    public RelativeLayout locationContainer;
    @BindView(R.id.locationTextView)
    public TextView locationsTextView;

    @BindView(R.id.federationRankLayout)
    public View federationRankLayout;
    @BindView(R.id.empireRankLayout)
    public View empireRankLayout;
    @BindView(R.id.combatRankLayout)
    public View combatRankLayout;
    @BindView(R.id.tradeRankLayout)
    public View tradeRankLayout;
    @BindView(R.id.explorationRankLayout)
    public View explorationRankLayout;
    @BindView(R.id.arenaRankLayout)
    public View arenaRankLayout;

    private NumberFormat numberFormat;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_commander_status, container, false);
        ButterKnife.bind(this, v);

        // Number format
        numberFormat = MathUtils.getNumberFormat(getContext());

        //Swipe to refresh setup
        SwipeRefreshLayout.OnRefreshListener listener = () -> {
            swipeRefreshLayout.setRefreshing(true);
            getAll();
        };
        swipeRefreshLayout.setOnRefreshListener(listener);

        // Set temporary text
        creditsTextView.setText(getResources().getString(R.string.credits, "?"));
        locationsTextView.setText(getResources().getString(R.string.unknown));
        RankUtils.setTempContent(getContext(), federationRankLayout,
                getString(R.string.rank_federation));
        RankUtils.setTempContent(getContext(), empireRankLayout, getString(R.string.rank_empire));

        RankUtils.setTempContent(getContext(), combatRankLayout, getString(R.string.rank_combat));
        RankUtils.setTempContent(getContext(), tradeRankLayout, getString(R.string.rank_trading));
        RankUtils.setTempContent(getContext(), explorationRankLayout,
                getString(R.string.rank_exploration));
        RankUtils.setTempContent(getContext(), arenaRankLayout, getString(R.string.rank_arena));

        // Hide views according to supported informations from source
        PlayerNetwork playerNetwork = PlayerNetworkUtils.getCurrentPlayerNetwork(getContext());
        if (!playerNetwork.supportCredits()) {
            creditsContainer.setVisibility(View.GONE);
        }
        if (!playerNetwork.supportLocation()) {
            locationContainer.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Setup views
        swipeRefreshLayout.setVisibility(View.VISIBLE);
        swipeRefreshLayout.setRefreshing(true);

        // Register event and get the news
        EventBus.getDefault().register(this);
        getAll();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCreditsEvent(Credits credits) {
        endLoading();
        // Check download error
        if (!credits.getSuccess()) {
            NotificationsUtils.displayDownloadErrorSnackbar(getActivity());
            return;
        }

        // Check error case
        if (credits.getBalance() == -1) {
            creditsTextView.setText(getResources().getString(R.string.unknown));
            return;
        }

        String amount = numberFormat.format(credits.getBalance());
        if (credits.getLoan() != 0) {
            String loan = numberFormat.format(credits.getLoan());
            creditsTextView.setText(getResources().getString(R.string.credits_with_loan,
                    amount, loan));
        } else {
            creditsTextView.setText(getResources().getString(R.string.credits, amount));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLoginNeededEvent(FrontierAuthNeeded event) {
        endLoading();

        OAuthUtils.storeUpdatedTokens(getContext(), "", "");

        // Show dialog
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.login_again_dialog_title)
                .setMessage(R.string.login_again_dialog_text)
                .setPositiveButton(android.R.string.ok, (d, which) -> {
                    d.dismiss();
                    Intent i = new Intent(getContext(), LoginActivity.class);
                    startActivity(i);
                })
                .setNegativeButton(android.R.string.cancel,
                        (d, which) -> d.dismiss())
                .create();

        dialog.show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPositionEvent(CommanderPosition position) {
        endLoading();
        // Check download error
        if (!position.getSuccess()) {
            NotificationsUtils.displayDownloadErrorSnackbar(getActivity());
            return;
        }

        locationsTextView.setText(position.getSystemName());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRanksEvents(Ranks ranks) {
        endLoading();
        // Check download error
        if (!ranks.getSuccess()) {
            NotificationsUtils.displayDownloadErrorSnackbar(getActivity());
            return;
        }

        RankUtils.setContent(getContext(), federationRankLayout, R.drawable.elite_federation,
                ranks.getFederation(), getString(R.string.rank_federation));
        RankUtils.setContent(getContext(), empireRankLayout, R.drawable.elite_empire,
                ranks.getEmpire(), getString(R.string.rank_empire));

        RankUtils.setContent(getContext(), combatRankLayout,
                InternalNamingUtils.getCombatLogoId(ranks.getCombat().getValue()), ranks.getCombat(),
                getString(R.string.rank_combat));
        RankUtils.setContent(getContext(), tradeRankLayout,
                InternalNamingUtils.getTradeLogoId(ranks.getTrade().getValue()), ranks.getTrade(),
                getString(R.string.rank_trading));
        RankUtils.setContent(getContext(), explorationRankLayout,
                InternalNamingUtils.getExplorationLogoId(ranks.getExplore().getValue()),
                ranks.getExplore(), getString(R.string.rank_exploration));
        RankUtils.setContent(getContext(), arenaRankLayout,
                InternalNamingUtils.getCqcLogoId(ranks.getCqc().getValue()), ranks.getCqc(),
                getString(R.string.rank_arena));
    }

    @OnClick(R.id.locationContainer)
    public void onSystemNameClick() {
        final String text = locationsTextView.getText().toString();
        MiscUtils.startIntentToSystemDetails(getContext(), text);
    }

    private void getAll() {
        // Refresh player name card title
        String cmdrName = SettingsUtils.getCommanderName(this.getContext());
        if (cmdrName.length() != 0) {
            commanderNameTextView.setText(cmdrName);
        }

        // Refresh player network object if settings changed
        PlayerNetwork playerNetwork = PlayerNetworkUtils.getCurrentPlayerNetwork(getContext());

        if (PlayerNetworkUtils.setupOk(getContext())) {
            playerNetwork.getCommanderStatus();
        } else {
            NotificationsUtils.displaySnackbarWithActivityButton(getActivity(),
                    playerNetwork.getErrorMessage(), SettingsActivity.class);
            endLoading();
        }
    }

    public void endLoading() {
        swipeRefreshLayout.setRefreshing(false);
    }
}
