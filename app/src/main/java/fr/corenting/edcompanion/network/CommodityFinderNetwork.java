package fr.corenting.edcompanion.network;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import fr.corenting.edcompanion.models.CommodityFinderResult;
import fr.corenting.edcompanion.models.apis.EDApi.CommodityFinderResponse;
import fr.corenting.edcompanion.models.events.ResultsList;
import fr.corenting.edcompanion.network.retrofit.EDApiRetrofit;
import fr.corenting.edcompanion.singletons.RetrofitSingleton;
import retrofit2.Call;
import retrofit2.internal.EverythingIsNonNull;


public class CommodityFinderNetwork {
    public static void findCommodity(Context ctx, String system, final String commodity,
                                     String landingPad, int minStockOrDemand,
                                     boolean isSellingMode) {

        // Init retrofit instance
        final EDApiRetrofit edApiRetrofit = RetrofitSingleton.getInstance()
                .getEdApiRetrofit(ctx.getApplicationContext());

        final retrofit2.Callback<List<CommodityFinderResponse>> callback = new retrofit2.Callback<List<CommodityFinderResponse>>() {
            @Override
            @EverythingIsNonNull
            public void onResponse(Call<List<CommodityFinderResponse>> call,
                                   retrofit2.Response<List<CommodityFinderResponse>> response) {

                // Check response
                final List<CommodityFinderResponse> sellersResponseBody = response.body();
                if (!response.isSuccessful() || sellersResponseBody == null) {
                    onFailure(call, new Exception("Invalid response"));
                } else {
                    processResults(sellersResponseBody);
                }
            }

            @Override
            @EverythingIsNonNull
            public void onFailure(Call<List<CommodityFinderResponse>> call,
                                  Throwable t) {
                EventBus.getDefault().post(new ResultsList<>(false,
                        new ArrayList<CommodityFinderResult>()));
            }
        };

        edApiRetrofit.findCommodity(system, commodity, landingPad, minStockOrDemand,
                minStockOrDemand, isSellingMode ? 1 : 0).enqueue(callback);
    }

    private static void processResults(List<CommodityFinderResponse> responseBody) {


        ResultsList<CommodityFinderResult> convertedResults;
        List<CommodityFinderResult> resultsList = new ArrayList<>();
        try {
            for (CommodityFinderResponse seller : responseBody) {
                resultsList.add(CommodityFinderResult.Companion.fromCommodityFinderResponse(seller));
            }
            convertedResults = new ResultsList<>(true, resultsList);

        } catch (Exception ex) {
            convertedResults = new ResultsList<>(false, new ArrayList<CommodityFinderResult>());
        }
        EventBus.getDefault().post(convertedResults);
    }
}
