package fr.corenting.edcompanion.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import fr.corenting.edcompanion.R;
import fr.corenting.edcompanion.adapters.GalnetAdapter;
import fr.corenting.edcompanion.models.GalnetArticle;
import fr.corenting.edcompanion.models.events.GalnetNews;
import fr.corenting.edcompanion.network.GalnetNetwork;
import fr.corenting.edcompanion.utils.NotificationsUtils;
import fr.corenting.edcompanion.utils.SettingsUtils;

public class GalnetFragment extends AbstractListFragment<GalnetAdapter> {

    public static final String GALNET_FRAGMENT_TAG = "galnet_fragment";
    public static final String GALNET_REPORTS_FRAGMENT_TAG = "galnet_reports_fragment";

    private boolean reportsMode;
    private String language;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        // Check if reports only or not
        reportsMode = false;
        if (getArguments() != null) {
            reportsMode = getArguments().getBoolean("reportsMode", false);
        }

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        language = getNewsLanguage();
    }

    private String getNewsLanguage() {
        return SettingsUtils.getString(getContext(),
                getString(R.string.settings_galnet_lang));
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh if language changed
        if (!language.equals(getNewsLanguage())) {
            language = getNewsLanguage();
            getData();
        }
    }

    @Override
    void getData() {
        GalnetNetwork.getNews(getContext(), language);
    }

    @Override
    GalnetAdapter getAdapter() {
        return new GalnetAdapter(getContext(), recyclerView, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewsEvent(GalnetNews news) {
        // Error case
        if (!news.getSuccess()) {
            endLoading(true);
            NotificationsUtils.displayDownloadErrorSnackbar(getActivity());
            return;
        }

        // Else setup list according to mode
        List<GalnetArticle> newList = new ArrayList<>();
        int count = 0;
        for (GalnetArticle n : news.getArticles()) {
            boolean isReport = n.getTitle().matches(".*(Weekly).*(Report).*") ||
                    n.getTitle().contains("Starport Status Update");

            // Add the article or not depending on the mode and the title
            if ((reportsMode && isReport) || (!reportsMode && !isReport)) {
                newList.add(n);
                count++;
            }
        }
        GalnetNews copy = new GalnetNews(true, newList);

        endLoading(count == 0);
        recyclerViewAdapter.removeAllItems();
        recyclerViewAdapter.addItems(copy.getArticles());
    }
}
