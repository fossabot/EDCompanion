package fr.corenting.edcompanion.models.apis.EDSM;

import com.google.gson.annotations.SerializedName;

public class EDSMRanks  extends EDSMBaseResponse {
    @SerializedName("ranks")
    public EDSMInnerRanks ranks;

    @SerializedName("progress")
    public EDSMInnerRanks progress;

    @SerializedName("ranksVerbose")
    public EDSMInnerRanksNames ranksNames;

    public class EDSMInnerRanksNames {
        @SerializedName("Combat")
        public String combat;

        @SerializedName("Trade")
        public String trade;

        @SerializedName("Explore")
        public String explore;

        @SerializedName("CQC")
        public String cqc;

        @SerializedName("Federation")
        public String federation;

        @SerializedName("Empire")
        public String empire;
    }

    public class EDSMInnerRanks {
        @SerializedName("Combat")
        public int combat;

        @SerializedName("Trade")
        public int trade;

        @SerializedName("Explore")
        public int explore;

        @SerializedName("CQC")
        public int cqc;

        @SerializedName("Federation")
        public int federation;

        @SerializedName("Empire")
        public int empire;
    }
}
