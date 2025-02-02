package fr.corenting.edcompanion.models.apis.EDSM;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EDSMCredits extends EDSMBaseResponse {

    @SerializedName("credits")
    public List<EDSMInnerCredits> credits;

    public class EDSMInnerCredits {
        @SerializedName("balance")
        public long balance;

        @SerializedName("loan")
        public long loan;
    }
}
