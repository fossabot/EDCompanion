package fr.corenting.edcompanion.models.apis.EDSM;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class EDSMCredits extends EDSMBaseResponse {

    @SerializedName("credits")
    public List<EDSMInnerCredits> credits;

    public class EDSMInnerCredits {
        @SerializedName("balance")
        public int balance;

        @SerializedName("loan")
        public int loan;
    }
}
