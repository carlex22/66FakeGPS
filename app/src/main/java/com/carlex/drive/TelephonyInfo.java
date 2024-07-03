package com.carlex.drive;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityCdma;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

public class TelephonyInfo {

    public static JSONObject getTelephonyDetails(Context context) {
        JSONObject telephonyDetails = new JSONObject();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        try {
            telephonyDetails.put("IMEI", telephonyManager.getDeviceId());
            telephonyDetails.put("IMSI", telephonyManager.getSubscriberId());
            telephonyDetails.put("SIM Operator", telephonyManager.getSimOperator());
            telephonyDetails.put("SIM Operator Name", telephonyManager.getSimOperatorName());
            telephonyDetails.put("SIM Country ISO", telephonyManager.getSimCountryIso());
            telephonyDetails.put("SIM Serial Number", telephonyManager.getSimSerialNumber());
            telephonyDetails.put("Phone Number", telephonyManager.getLine1Number());
            telephonyDetails.put("Network Operator", telephonyManager.getNetworkOperator());
            telephonyDetails.put("Network Operator Name", telephonyManager.getNetworkOperatorName());
            telephonyDetails.put("Network Type", telephonyManager.getNetworkType());

            List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
            JSONArray cellInfoArray = new JSONArray();
            if (cellInfos != null) {
                for (CellInfo cellInfo : cellInfos) {
                    JSONObject cellInfoJson = new JSONObject();
                    if (cellInfo instanceof CellInfoGsm) {
                        CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) cellInfo).getCellIdentity();
                        cellInfoJson.put("Type", "GSM");
                        cellInfoJson.put("CID", cellIdentityGsm.getCid());
                        cellInfoJson.put("LAC", cellIdentityGsm.getLac());
                        cellInfoJson.put("MCC", cellIdentityGsm.getMcc());
                        cellInfoJson.put("MNC", cellIdentityGsm.getMnc());
                    } else if (cellInfo instanceof CellInfoLte) {
                        CellIdentityLte cellIdentityLte = ((CellInfoLte) cellInfo).getCellIdentity();
                        cellInfoJson.put("Type", "LTE");
                        cellInfoJson.put("CI", cellIdentityLte.getCi());
                        cellInfoJson.put("TAC", cellIdentityLte.getTac());
                        cellInfoJson.put("MCC", cellIdentityLte.getMcc());
                        cellInfoJson.put("MNC", cellIdentityLte.getMnc());
                    } else if (cellInfo instanceof CellInfoCdma) {
                        CellIdentityCdma cellIdentityCdma = ((CellInfoCdma) cellInfo).getCellIdentity();
                        cellInfoJson.put("Type", "CDMA");
                        cellInfoJson.put("BasestationId", cellIdentityCdma.getBasestationId());
                        cellInfoJson.put("NetworkId", cellIdentityCdma.getNetworkId());
                        cellInfoJson.put("SystemId", cellIdentityCdma.getSystemId());
                    }
                    cellInfoArray.put(cellInfoJson);
                }
                telephonyDetails.put("Cell Info", cellInfoArray);
            }

            List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
            JSONArray subscriptionArray = new JSONArray();
            if (subscriptionInfos != null) {
                for (SubscriptionInfo subscriptionInfo : subscriptionInfos) {
                    JSONObject subscriptionJson = new JSONObject();
                    subscriptionJson.put("SubscriptionId", subscriptionInfo.getSubscriptionId());
                    subscriptionJson.put("CarrierName", subscriptionInfo.getCarrierName().toString());
                    subscriptionJson.put("DisplayName", subscriptionInfo.getDisplayName().toString());
                    subscriptionJson.put("CountryIso", subscriptionInfo.getCountryIso());
                    subscriptionJson.put("IccId", subscriptionInfo.getIccId());
                    subscriptionJson.put("Number", subscriptionInfo.getNumber());
                    subscriptionArray.put(subscriptionJson);
                }
                telephonyDetails.put("Subscription Info", subscriptionArray);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return telephonyDetails;
    }
}
