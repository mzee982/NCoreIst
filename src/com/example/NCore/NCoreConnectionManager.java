package com.example.NCore;

import android.net.UrlQuerySanitizer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Static class for managing NCore related HTTP connections
 */
// TODO: Exception handling
public final class NCoreConnectionManager {

    //
    public static final String CHARSET_UTF8 = "UTF-8";
    private static final String REQUEST_METHOD_GET = "GET";
    private static final String REQUEST_METHOD_POST = "POST";
    private static final char POST_PARAM_CONC = '=';
    private static final char POST_PARAM_SEP = '&';

    // Defaults
    private static final int READ_TIMEOUT = 10000;
    private static final int CONNECT_TIMEOUT = 15000;
    private static final boolean FOLLOW_REDIRECTS = false;

    // URLs
    public static final String BASE_URI = "https://ncore.cc";
    private static final String URL_LOGIN_PAGE = "/login.php";
    private static final String URL_INDEX_PAGE = "/index.php";
    public static final String URL_LOGIN = BASE_URI + URL_LOGIN_PAGE; // "https://ncore.cc/login.php"
    public static final String URL_INDEX = BASE_URI + URL_INDEX_PAGE; // "https://ncore.cc/index.php"

    // URL query keys
    private static final String URL_QUERY_KEY_PROBLEM = "problema";

    // HTTP POST parameters
    private static final String POST_PARAM_KEY_LANG = "set_lang";
    private static final String POST_PARAM_VALUE_LANG_HU = "hu";
    private static final String POST_PARAM_KEY_SUBMITTED = "submitted";
    private static final String POST_PARAM_VALUE_SUBMITTED_1 = "1";
    private static final String POST_PARAM_KEY_SUBMIT = "submit";
    private static final String POST_PARAM_VALUE_SUBMIT = "Belépés!";
    private static final String POST_PARAM_KEY_LOGIN_NAME = "nev";
    private static final String POST_PARAM_KEY_LOGIN_PASSWORD = "pass";
    private static final String POST_PARAM_KEY_CAPTCHA_CHALLENGE = "recaptcha_challenge_field";
    private static final String POST_PARAM_KEY_CAPTCHA_RESPONSE = "recaptcha_response_field";

    // HTTP header fields
    private static final String HTTP_HEADER_FIELD_LOCATION = "Location";

    // Login response messages
    private static final String LOGIN_RESPONSE_UNEXPECTED = "Unexpected login response";
    private static final String LOGIN_RESPONSE_PROBLEM_1 = "Hibás felhasználónév vagy jelszó!";

    /**
     * Private constructor to prevent instantiation
     */
    private NCoreConnectionManager() {

    }

    /**
     * Opens a new HttpURLConnection for the given URL and initializes with default values
     *
     * @param aUrlString Target URL in string format
     * @return           An initialized HttpURLConnection for the given URL
     * @throws MalformedURLException, IOException
     */
    private static HttpURLConnection openConnection(String aUrlString) throws MalformedURLException, IOException {

        // Initialize a default cookie manager
        initializeCookieManager();

        // Open the connection for the URL
        // TODO: URL specific exception handling?
        URL url = new URL(aUrlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        // new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.1.84", 8888))

        // Set default values
        connection.setReadTimeout(READ_TIMEOUT);
        connection.setConnectTimeout(CONNECT_TIMEOUT);
        connection.setInstanceFollowRedirects(FOLLOW_REDIRECTS);

        // Additional request property default values
        // TODO: Request properties with default values
        //connection.setRequestProperty();
        //Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
        //Accept-Language: en-US,en;q=0.5
        //Accept-Encoding: gzip, deflate
        //Referer: https://ncore.cc/login.php

        return connection;
    }

    /**
     * Opens a new HttpURLConnection and prepares for HTTP GET request.
     * Calls {@link #openConnection(String)} to initialize the connection.
     *
     * @param aUrlString Target URL in string format
     * @return           An initialized HttpURLConnection for the given URL
     * @throws MalformedURLException, IOException
     */
    public static HttpURLConnection openGetConnection(String aUrlString) throws MalformedURLException, IOException {

        // Initialize the connection
        HttpURLConnection connection = openConnection(aUrlString);

        // Prepare for HTTP GET request
        connection.setRequestMethod(REQUEST_METHOD_GET);
        connection.setDoInput(true);
        connection.setDoOutput(false);

        return connection;
    }

    /**
     * Opens a new HttpURLConnection and prepares for HTTP POST request.
     * Calls {@link #openConnection(String)} to initialize the connection.
     *
     * @param aUrlString     Target URL in string format
     * @param aPostParamsMap Parameters to POST
     * @return               An initialized HttpURLConnection for the given URL
     * @throws MalformedURLException, IOException
     */
    public static HttpURLConnection openPostConnection(String aUrlString, Map<String, String> aPostParamsMap)
            throws MalformedURLException, IOException {

        // Initialize the connection
        HttpURLConnection connection = openConnection(aUrlString);

        // Prepare for HTTP POST request
        connection.setRequestMethod(REQUEST_METHOD_POST);
        connection.setDoInput(false);
        connection.setDoOutput(true);

        // Prepare POST params
        byte[] postParams = preparePostParams(aPostParamsMap);

        // Posting content
        connection.setFixedLengthStreamingMode(postParams.length);
        DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
        dataOutputStream.write(postParams);
        dataOutputStream.flush();

        return connection;
    }

    /**
     * Initialize a default cookie manager
     */
    private static void initializeCookieManager() {
        if (CookieHandler.getDefault() == null) {
            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
        }
    }

    /**
     * Concatenate the params in HTTP POST parameter format
     *
     * @param aPostParamsMap HTTP POST input parameters map
     * @return               Concatenated params in proper format
     * @throws UnsupportedEncodingException
     */
    private static byte[] preparePostParams(Map<String,String> aPostParamsMap) throws UnsupportedEncodingException {
        // TODO: Rewrite to using Uri.Builder

        //
        StringBuilder postParams = new StringBuilder();

        // Concatenate the params in POST parameter format
        for (Map.Entry<String,String> entry : aPostParamsMap.entrySet()) {
            String key = URLEncoder.encode(entry.getKey(), CHARSET_UTF8);
            String value = URLEncoder.encode(entry.getValue(), CHARSET_UTF8);
            String param = key + POST_PARAM_CONC + value;

            // Add separator between params
            if (postParams.length() > 0) {
                postParams.append(POST_PARAM_SEP);
            }

            postParams.append(param);
        }

        return postParams.toString().getBytes(CHARSET_UTF8);
    }

    /**
     * Concatenate the params for the login HTTP POST
     *
     * @param loginName                Mandatory login name
     * @param loginPassword            Mandatory login password
     * @param captchaChallengeValue    CAPTCHA challenge value
     * @param captchaResponseValue     CAPTCHA response value
     * @param loginWithCaptcha         Log in with or without CAPTCHA
     * @return                         Map of the login parameters
     * @throws UnsupportedEncodingException
     */
    public static Map<String,String> prepareLoginPostParams(
            String loginName,
            String loginPassword,
            String captchaChallengeValue,
            String captchaResponseValue,
            boolean loginWithCaptcha) throws UnsupportedEncodingException {

        HashMap<String,String> postParams = new HashMap<String,String>();

        // Constants
        postParams.put(POST_PARAM_KEY_LANG, POST_PARAM_VALUE_LANG_HU);
        postParams.put(POST_PARAM_KEY_SUBMITTED, POST_PARAM_VALUE_SUBMITTED_1);
        postParams.put(POST_PARAM_KEY_SUBMIT, POST_PARAM_VALUE_SUBMIT);

        // Login information
        postParams.put(POST_PARAM_KEY_LOGIN_NAME, loginName);
        postParams.put(POST_PARAM_KEY_LOGIN_PASSWORD, loginPassword);

        // Optional CAPTCHA data
        if (loginWithCaptcha) {
            postParams.put(POST_PARAM_KEY_CAPTCHA_CHALLENGE, captchaChallengeValue);
            postParams.put(POST_PARAM_KEY_CAPTCHA_RESPONSE, captchaResponseValue);
        }

        return postParams;
    }

    /**
     * Evaluate the login HTTP response
     *
     * @param responseCode    HTTP response code
     * @param headerFields    HTTP response headers
     * @return                NULL in case of successful login, error message otherwise.
     * @throws MalformedURLException
     */
    public static String evaluateLoginResponse(int responseCode, Map<String, List<String>> headerFields)
            throws MalformedURLException {

        // HTTP redirect (302) response
        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            List<String> locationStrings = headerFields.get(HTTP_HEADER_FIELD_LOCATION);

            // HTTP Location header field is found
            if ((locationStrings != null) && (locationStrings.size() == 1)) {
                URL locationUrl = new URL(new URL(BASE_URI), locationStrings.get(0));

                // SUCCESSFUL login
                if (URL_INDEX_PAGE.equals(locationUrl.getPath())) {
                    return null;
                }

                // Login FAILED
                else if (URL_LOGIN_PAGE.equals(locationUrl.getPath())) {

                    // Parse the query part of the location URL
                    UrlQuerySanitizer sanitizer = new UrlQuerySanitizer();
                    sanitizer.setAllowUnregisteredParamaters(true);
                    sanitizer.parseQuery(locationUrl.getQuery());
                    String queryValue = sanitizer.getValue(URL_QUERY_KEY_PROBLEM);

                    if (queryValue != null) {
                        switch (Integer.parseInt(queryValue)) {
                            case 1:
                                return LOGIN_RESPONSE_PROBLEM_1;
                            default:
                                // Unexpected response
                                return LOGIN_RESPONSE_UNEXPECTED;
                        }
                    }

                    // Unexpected response
                    else {
                        return LOGIN_RESPONSE_UNEXPECTED;
                    }
                }

                // Unexpected response
                else {
                    return LOGIN_RESPONSE_UNEXPECTED;
                }

            }

            // Unexpected response
            else {
                return LOGIN_RESPONSE_UNEXPECTED;
            }

        }

        // Unexpected response
        else {
            return LOGIN_RESPONSE_UNEXPECTED;
        }

    }

}
