package com.mzee982.android.ncoreist;

import android.net.Uri;
import android.net.UrlQuerySanitizer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final String URL_TORRENT_LIST_PAGE = "/torrents.php";
    private static final String URL_OTHER_VERSIONS_PAGE = "/ajax.php";
    public static final String URL_LOGIN = BASE_URI + URL_LOGIN_PAGE; // "https://ncore.cc/login.php"
    public static final String URL_INDEX = BASE_URI + URL_INDEX_PAGE; // "https://ncore.cc/index.php"
    public static final String URL_TORRENT_LIST = BASE_URI + URL_TORRENT_LIST_PAGE; // "https://ncore.cc/torrents.php"
    public static final String URL_OTHER_VERSIONS = BASE_URI + URL_OTHER_VERSIONS_PAGE; // "https://ncore.cc/ajax.php"
    public static final String URL_RECAPTHA_IMAGE = "https://www.google.com/recaptcha/api/image";

    // URL query keys
    private static final String URL_QUERY_KEY_PROBLEM = "problema";
    private static final String URL_QUERY_KEY_TORRENT_CATEGORY = "csoport_listazas";
    private static final String URL_QUERY_KEY_TORRENT_LIST_PAGE = "oldal";
    private static final String URL_QUERY_KEY_ACTION = "action";
    private static final String URL_QUERY_KEY_ID = "id";
    private static final String URL_QUERY_KEY_FID = "fid";
    private static final String URL_QUERY_KEY_DETAILS = "details";
    private static final String URL_QUERY_KEY_RECAPTCHA_CHALLENGE = "c";

    // URL query values
    private static final String URL_QUERY_VALUE_TORRENT_CATEGORY_MOVIE = "osszes_film";
    private static final String URL_QUERY_VALUE_TORRENT_CATEGORY_SERIES = "osszes_sorozat";
    private static final String URL_QUERY_VALUE_TORRENT_CATEGORY_MUSIC = "osszes_zene";
    private static final String URL_QUERY_VALUE_TORRENT_CATEGORY_XXX = "osszes_xxx";
    private static final String URL_QUERY_VALUE_TORRENT_CATEGORY_GAME = "osszes_jatek";
    private static final String URL_QUERY_VALUE_TORRENT_CATEGORY_SOFTWARE = "osszes_program";
    private static final String URL_QUERY_VALUE_TORRENT_CATEGORY_BOOK = "osszes_konyv";
    private static final String URL_QUERY_VALUE_ACTION_DOWNLOAD = "download";
    private static final String URL_QUERY_VALUE_ACTION_DETAILS = "details";
    private static final String URL_QUERY_VALUE_ACTION_OTHER_VERSIONS = "other_versions";
    private static final String URL_QUERY_VALUE_DETAILS_1 = "1";

    // HTTP POST parameter keys
    private static final String POST_PARAM_KEY_LANG = "set_lang";
    private static final String POST_PARAM_KEY_SUBMITTED = "submitted";
    private static final String POST_PARAM_KEY_SUBMIT = "submit";
    private static final String POST_PARAM_KEY_LOGIN_NAME = "nev";
    private static final String POST_PARAM_KEY_LOGIN_PASSWORD = "pass";
    private static final String POST_PARAM_KEY_CAPTCHA_CHALLENGE = "recaptcha_challenge_field";
    private static final String POST_PARAM_KEY_CAPTCHA_RESPONSE = "recaptcha_response_field";
    private static final String POST_PARAM_KEY_SEARCH_QUERY = "mire";
    private static final String POST_PARAM_KEY_SEARCH_TARGET = "miben";
    private static final String POST_PARAM_KEY_SEARCH_TYPE = "tipus";
    private static final String POST_PARAM_KEY_SEARCH_SELECTED_TYPE = "kivalasztott_tipus";
    private static final String POST_PARAM_KEY_SEARCH_PAGE_INDEX = "oldal";
    private static final String POST_PARAM_KEY_SEARCH_SUBMIT_X = "submit.x";
    private static final String POST_PARAM_KEY_SEARCH_SUBMIT_Y = "submit.y";
    private static final String POST_PARAM_KEY_SEARCH_TAGS = "tags";

    // HTTP POST parameter values
    private static final String POST_PARAM_VALUE_LANG_HU = "hu";
    private static final String POST_PARAM_VALUE_SUBMITTED_1 = "1";
    private static final String POST_PARAM_VALUE_SUBMIT = "Belépés!";
    private static final String POST_PARAM_VALUE_SEARCH_TARGET_NAME = "name";
    private static final String POST_PARAM_VALUE_SEARCH_TARGET_DESC = "leiras";
    private static final String POST_PARAM_VALUE_SEARCH_TARGET_LABEL = "cimke";
    private static final String POST_PARAM_VALUE_SEARCH_TYPE_ALL_OWN = "all_own";
    private static final String POST_PARAM_VALUE_SEARCH_TYPE_ALL = "all";
    private static final String POST_PARAM_VALUE_SEARCH_TYPE_SELECTED = "kivalasztottak_kozott";
    private static final String POST_PARAM_VALUE_SEARCH_TYPE_ORIGINAL = "eredeti_releasekben";
    private static final String POST_PARAM_VALUE_SEARCH_SEL_TYPE_MOVIE =
            "xvid_hun,xvid,dvd_hun,dvd,dvd9_hun,dvd9,hd_hun,hd";
    private static final String POST_PARAM_VALUE_SEARCH_SEL_TYPE_SERIES =
            "xvidser_hun,xvidser,dvdser_hun,dvdser,hdser_hun,hdser";
    private static final String POST_PARAM_VALUE_SEARCH_SEL_TYPE_MUSIC = "mp3_hun,mp3,lossless_hun,lossless,clip";
    private static final String POST_PARAM_VALUE_SEARCH_SEL_TYPE_XXX = "xxx_xvid,xxx_dvd,xxx_imageset,xxx_hd";
    private static final String POST_PARAM_VALUE_SEARCH_SEL_TYPE_GAME = "game_iso,game_rip,console";
    private static final String POST_PARAM_VALUE_SEARCH_SEL_TYPE_SOFTWARE = "iso,misc,mobil";
    private static final String POST_PARAM_VALUE_SEARCH_SEL_TYPE_BOOK = "ebook_hun,ebook";
    private static final String POST_PARAM_VALUE_SEARCH_SUBMIT_X = "0";
    private static final String POST_PARAM_VALUE_SEARCH_SUBMIT_Y = "0";

    // HTTP request property keys
    private static final String REQUEST_PROPERTY_KEY_REQUESTED_WITH = "X-Requested-With";

    // HTTP request property values
    private static final String REQUEST_PROPERTY_VALUE_XMLHTTPREQUEST = "XMLHttpRequest";

    // HTTP header fields
    private static final String HTTP_HEADER_FIELD_LOCATION = "Location";
    private static final String HTTP_HEADER_FIELD_CONTENT_DISPOSITION = "Content-Disposition";

    // Login response messages
    private static final String LOGIN_RESPONSE_UNEXPECTED = "Unexpected login response";
    private static final String DOWNLOAD_RESPONSE_UNEXPECTED = "Unexpected download response";
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

    public static HttpURLConnection openAjaxGetConnection(String aUrlString) throws MalformedURLException, IOException {

        // Initialize the connection
        HttpURLConnection connection = openConnection(aUrlString);

        // AJAX request property
        connection.setRequestProperty(REQUEST_PROPERTY_KEY_REQUESTED_WITH, REQUEST_PROPERTY_VALUE_XMLHTTPREQUEST);

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

    public static HttpURLConnection openPostConnectionForResult(String aUrlString, Map<String, String> aPostParamsMap)
            throws MalformedURLException, IOException {

        // Initialize the connection
        HttpURLConnection connection = openConnection(aUrlString);

        // Prepare for HTTP POST request
        connection.setRequestMethod(REQUEST_METHOD_POST);
        connection.setDoInput(true);
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
     * Map the params for the login HTTP POST
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
     * Map the params for the torrent list search HTTP POST
     *
     * @param aCategoryIndex    Index of the torrent list category
     * @param aQuery            Mandatory search query string
     * @param aPageIndex        Index of the torrent list page
     * @return          Map of the search parameters
     * @throws UnsupportedEncodingException
     */
    public static Map<String,String> prepareSearchPostParams(
            int aCategoryIndex, String aQuery, int aPageIndex) throws UnsupportedEncodingException {

        HashMap<String,String> postParams = new HashMap<String,String>();

        // Constants
        postParams.put(POST_PARAM_KEY_SEARCH_TARGET, POST_PARAM_VALUE_SEARCH_TARGET_NAME);

        if (aCategoryIndex == 0) {
            postParams.put(POST_PARAM_KEY_SEARCH_TYPE, POST_PARAM_VALUE_SEARCH_TYPE_ALL_OWN);
        }
        else {
            postParams.put(POST_PARAM_KEY_SEARCH_TYPE, POST_PARAM_VALUE_SEARCH_TYPE_SELECTED);
        }

        // Selected types
        switch (aCategoryIndex) {
            case 0:
                break;
            case 1:
                postParams.put(POST_PARAM_KEY_SEARCH_SELECTED_TYPE, POST_PARAM_VALUE_SEARCH_SEL_TYPE_MOVIE);
                break;
            case 2:
                postParams.put(POST_PARAM_KEY_SEARCH_SELECTED_TYPE, POST_PARAM_VALUE_SEARCH_SEL_TYPE_SERIES);
                break;
            case 3:
                postParams.put(POST_PARAM_KEY_SEARCH_SELECTED_TYPE, POST_PARAM_VALUE_SEARCH_SEL_TYPE_MUSIC);
                break;
            case 4:
                postParams.put(POST_PARAM_KEY_SEARCH_SELECTED_TYPE, POST_PARAM_VALUE_SEARCH_SEL_TYPE_XXX);
                break;
            case 5:
                postParams.put(POST_PARAM_KEY_SEARCH_SELECTED_TYPE, POST_PARAM_VALUE_SEARCH_SEL_TYPE_GAME);
                break;
            case 6:
                postParams.put(POST_PARAM_KEY_SEARCH_SELECTED_TYPE, POST_PARAM_VALUE_SEARCH_SEL_TYPE_SOFTWARE);
                break;
            case 7:
                postParams.put(POST_PARAM_KEY_SEARCH_SELECTED_TYPE, POST_PARAM_VALUE_SEARCH_SEL_TYPE_BOOK);
                break;
            default:
                // None
        }

        // Search query
        postParams.put(POST_PARAM_KEY_SEARCH_QUERY, aQuery.replaceAll("\\s+", "+"));

        // Search results page index
        postParams.put(POST_PARAM_KEY_SEARCH_PAGE_INDEX, String.valueOf(aPageIndex));

        return postParams;
    }

    public static String prepareReCaptchaImageUrl(String captchaChallengeValue) {

        // URI builder for the base URL
        Uri uri = Uri.parse(URL_RECAPTHA_IMAGE);
        Uri.Builder uriBuilder = uri.buildUpon();

        // Append reCAPTCHA challenge query parameter
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_RECAPTCHA_CHALLENGE, captchaChallengeValue);

        return uriBuilder.build().toString();

    }

    /**
     * Prepare the URL for querying torrent list for the given category
     *
     * @param aCategoryIndex    Index of the torrent list category
     * @param aPageIndex        Index of the torrent list page
     * @return                  URL string
     */
    public static String prepareTorrentListUrlForCategory(int aCategoryIndex, int aPageIndex) {

        // URI builder for the base torrent list URL
        Uri uri = Uri.parse(URL_TORRENT_LIST);
        Uri.Builder uriBuilder = uri.buildUpon();

        // Append torrent category query parameters
        switch (aCategoryIndex) {
            case 0:
                break;
            case 1:
                uriBuilder.appendQueryParameter(URL_QUERY_KEY_TORRENT_CATEGORY, URL_QUERY_VALUE_TORRENT_CATEGORY_MOVIE);
                break;
            case 2:
                uriBuilder.appendQueryParameter(URL_QUERY_KEY_TORRENT_CATEGORY, URL_QUERY_VALUE_TORRENT_CATEGORY_SERIES);
                break;
            case 3:
                uriBuilder.appendQueryParameter(URL_QUERY_KEY_TORRENT_CATEGORY, URL_QUERY_VALUE_TORRENT_CATEGORY_MUSIC);
                break;
            case 4:
                uriBuilder.appendQueryParameter(URL_QUERY_KEY_TORRENT_CATEGORY, URL_QUERY_VALUE_TORRENT_CATEGORY_XXX);
                break;
            case 5:
                uriBuilder.appendQueryParameter(URL_QUERY_KEY_TORRENT_CATEGORY, URL_QUERY_VALUE_TORRENT_CATEGORY_GAME);
                break;
            case 6:
                uriBuilder.appendQueryParameter(URL_QUERY_KEY_TORRENT_CATEGORY, URL_QUERY_VALUE_TORRENT_CATEGORY_SOFTWARE);
                break;
            case 7:
                uriBuilder.appendQueryParameter(URL_QUERY_KEY_TORRENT_CATEGORY, URL_QUERY_VALUE_TORRENT_CATEGORY_BOOK);
                break;
            default:
                // None
        }

        // Append torrent list page query parameters
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_TORRENT_LIST_PAGE, String.valueOf(aPageIndex));

        return uriBuilder.build().toString();
    }

    public static String prepareTorrentDownloadUrl(long aTorrentId) {

        // URI builder for the base torrent list URL
        Uri uri = Uri.parse(URL_TORRENT_LIST);
        Uri.Builder uriBuilder = uri.buildUpon();

        // Append torrent download query parameters
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_ACTION, URL_QUERY_VALUE_ACTION_DOWNLOAD);
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_ID, String.valueOf(aTorrentId));

        return uriBuilder.build().toString();
    }

    public static String prepareTorrentDetailsUrl(long aTorrentId) {

        // URI builder for the base torrent list URL
        Uri uri = Uri.parse(URL_TORRENT_LIST);
        Uri.Builder uriBuilder = uri.buildUpon();

        // Append torrent download query parameters
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_ACTION, URL_QUERY_VALUE_ACTION_DETAILS);
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_ID, String.valueOf(aTorrentId));

        return uriBuilder.build().toString();
    }

    public static String prepareOtherVersionsUrl(String aOtherVersionsId, String aOtherVersionsFid) {

        // URI builder for the base URL
        Uri uri = Uri.parse(URL_OTHER_VERSIONS);
        Uri.Builder uriBuilder = uri.buildUpon();

        // Append query parameters
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_ACTION, URL_QUERY_VALUE_ACTION_OTHER_VERSIONS);
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_ID, aOtherVersionsId);
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_FID, aOtherVersionsFid);
        uriBuilder.appendQueryParameter(URL_QUERY_KEY_DETAILS, URL_QUERY_VALUE_DETAILS_1);

        return uriBuilder.build().toString();

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

    public static String evaluateTorrentDownloadResponse(Map<String, List<String>> headerFields) {

        // Get Content-Disposition header field
        List<String> contentDispositionStrings = headerFields.get(HTTP_HEADER_FIELD_CONTENT_DISPOSITION);

        if ((contentDispositionStrings != null) &&(contentDispositionStrings.size() > 0)) {

            // Looking for the filename
            Pattern extractFileNamePattern = Pattern.compile("^.*filename=\"(.*)\".*$");

            // Extract filename
            Matcher matcher = extractFileNamePattern.matcher(contentDispositionStrings.get(0));

            // Return filename
            if (matcher.find()) {
                return matcher.group(1);
            }

            else {
                return null;
            }

        }

        else {
            return null;
        }

    }

}
