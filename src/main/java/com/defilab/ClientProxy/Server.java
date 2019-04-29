package com.defilab.ClientProxy;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;
import com.defilab.sdk.entities.DefiClient;

import com.alibaba.fastjson.JSON;

import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class Server {
    
    @SuppressWarnings("unchecked")
    private static Map<String, String> appCredentials = (Map<String, String>) JSON.parse(Utils.getEnvOrDefault("APP_CREDENTIALS", "{}"));
    
    private static Route serveHealthCheck = (Request request, Response reponse) -> {
        return "OK";
    };
    
    private static void haltWithError(Integer httpCode, String errorMessage) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", errorMessage);
        Spark.halt(httpCode, JSON.toJSONString(errorResponse));
    }
    
    private static Filter verifyRequest = (Request request, Response response) -> {
        response.type("application/json");
        
        if (!request.contentType().startsWith("application/json")) {
            haltWithError(400, "Body must be JSON");
        }
        try {
            String sig = request.queryParamOrDefault("sig", "");
            String appId = request.queryParamOrDefault("app_id", "");
            String postbodyDigest = DigestUtils.sha256Hex(request.bodyAsBytes());
            if (!appCredentials.containsKey(appId)) {
                Spark.halt(401);
            }
            String appKey = appCredentials.get(appId);
            if (!sig.equals(DigestUtils.sha256Hex(appId + appKey + postbodyDigest))) {
                Spark.halt(401);
            }
        } catch (Exception e) {
            Spark.halt(401);
        }
        
    };

    @SuppressWarnings("unchecked")
    private static Route serveQuery = (Request request, Response response) -> {
        String appId = request.queryParamOrDefault("app_id", "");
        Map<String, Object> params = null;
        try {
            params = (Map<String, Object>) JSON.parse(request.body());
        } catch (Exception ex) {
            haltWithError(400, "Invalid JSON");
        }
        String dataSpec = (String) params.getOrDefault("data_spec", "");
        String qualifiers = null;
        try {
            qualifiers = (String) params.getOrDefault("qualifiers", "");
        } catch (Exception ex) {
            haltWithError(400, "qualifiers is not string");
        }
        Double price = Double.valueOf(params.getOrDefault("price", -1).toString());
        Integer timeout = Integer.valueOf(params.getOrDefault("timeout", 5).toString());
        String targetAddress = params.getOrDefault("target_address", "").toString();
        
        if (dataSpec == null || dataSpec.isEmpty() || qualifiers == null || qualifiers.isEmpty() || price < 0 || timeout <= 0) {
            haltWithError(400, "Invalid data_spec/qualifiers/price/timeout");
        }
        
        String offerId = UUID.randomUUID().toString();
	DefiClient defiClient = (DefiClient) Utils.getDefiEntity(appId, "client");
        if (targetAddress != null && !targetAddress.isEmpty()) {
            Map<String, Object> dataResp = defiClient.getData(dataSpec, qualifiers, price, targetAddress, offerId, timeout);
            return JSON.toJSONString(dataResp);
        } else {
            Map<String, Map<String, Object>> dataResp = defiClient.getData(dataSpec, qualifiers, price);
            return JSON.toJSONString(dataResp);
        }
    };
    
    public static void main(String[] args) throws Exception {
        Spark.threadPool(Integer.valueOf(Utils.getEnvOrDefault("THREADPOOL_SIZE", "36")));
        Spark.before("/query", verifyRequest);
        Spark.post("/query", serveQuery);
        Spark.get("/health_check", serveHealthCheck);
    }
}
