package com.example.NCore;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NCoreParser {

    // Exceptions
    private static final String EXCEPTION_LOGIN_PAGE_PARSE = "Error occured during parsing the login page.";
    private static final String EXCEPTION_RECAPTCHA_CHALLENGE_SCRIPT_PARSE =
            "Error occured during parsing the reCAPTCHA challenge script.";
    private static final String EXCEPTION_INDEX_PAGE_PARSE = "Error occured during parsing the index page.";
    private static final String EXCEPTION_TORRENT_LIST_PAGE_PARSE =
            "Error occured during parsing the torrent list page.";
    private static final String EXCEPTION_TORRENT_DETAILS_PAGE_PARSE =
            "Error occured during parsing the torrent details page.";
    private static final String EXCEPTION_OTHER_VERSIONS_PAGE_PARSE =
            "Error occured during parsing the other versions page.";

    // Parameters
    public static final String PARAM_CAPTCHA_CHALLENGE_SCRIPT_URL = "PARAM_CAPTCHA_CHALLENGE_SCRIPT_URL";
    public static final String PARAM_CAPTCHA_CHALLENGE_VALUE = "PARAM_CAPTCHA_CHALLENGE_VALUE";
    public static final String PARAM_CAPTCHA_IMAGE_URL = "PARAM_CAPTCHA_IMAGE_URL";
    public static final String PARAM_LOGOUT_URL = "PARAM_LOGOUT_URL";

    // HTML attributes
    private static final String HTML_ATTR_VALUE = "value";
    private static final String HTML_ATTR_SRC = "src";
    private static final String HTML_ATTR_HREF = "href";
    private static final String HTML_ATTR_TITLE = "title";
    private static final String HTML_ATTR_ONCLICK = "onclick";
    private static final String HTML_ID_RECAPTCHA_CHALLENGE = "recaptcha_challenge_field";
    private static final String HTML_ID_RECAPTCHA_IMAGE = "recaptcha_challenge_image";
    private static final String HTML_ID_LOGOUT_LINK = "menu_11";
    private static final String HTML_ID_NPA = "nPa";

    // JSON fields
    public static final String JSON_FIELD_RECAPTCHA_CHALLENGE = "challenge";

    // Regex patterns
    private static final Pattern REGEX_PATTERN_RECAPTCHA_CHALLENGE_SCRIPT_JSON =
            Pattern.compile("(?s)^.*(\\{.*\\}).*$");
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_CATEGORY = Pattern.compile("^.*tipus=(.*)$");
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_ID = Pattern.compile("^.*id=(.*)$"); // "^.*\\((.*)\\).*$"
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_DOWNLOADED = Pattern.compile("^(\\d*).*$");
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_SPEED = Pattern.compile("^(.*)\\s\\(.*$");
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_SIZE = Pattern.compile("^(.*)\\s\\(.*$");
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_IMDB = Pattern.compile("^\\[imdb:\\s(.*)\\]$");
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_OTHER_VERSIONS =
            Pattern.compile("^.*\\('.*'.*'(.*)'.*'(.*)'.*\\).*$");
    private static final Pattern REGEX_PATTERN_TORRENT_DETAILS_CATEGORY = Pattern.compile("^.*tipus=(.*)$");

    // CSS selectors
    public static final String CSS_SELECTOR_RECAPTCHA_CHALLENGE_SCRIPT =
            "#login > form > table > tbody > tr:nth-child(3) > td > center > script:nth-child(1)";
    public static final String CSS_SELECTOR_RECAPTCHA_TABLE = "#recaptcha_table";
    public static final String CSS_SELECTOR_RECAPTCHA_CHALLENGE_IMAGE = "#recaptcha_challenge_image";
    public static final String CSS_SELECTOR_RECAPTCHA_CHALLENGE_FIELD = "#recaptcha_challenge_field";
    public static final String CSS_SELECTOR_TORRENT_LIST_ITEM =
            "#main_tartalom > div.lista_all > div.box_torrent_all > div.box_torrent";
    public static final String CSS_SELECTOR_TORRENT_ITEM_CATEGORY = "div.box_alap_img > a";
    public static final String CSS_SELECTOR_TORRENT_ITEM_SECTION_TABLE_SZOVEG = "div > div > div.tabla_szoveg";
    public static final String CSS_SELECTOR_TORRENT_ITEM_NAME = "div > a";
    public static final String CSS_SELECTOR_TORRENT_ITEM_TITLE = "div > div > div.siterank > span";
    public static final String CSS_SELECTOR_TORRENT_ITEM_IMDB = "div.torrent_txt > div > div.siterank > a";
    public static final String CSS_SELECTOR_TORRENT_ITEM_SIZE = "div > div.box_meret2";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_FOBOX_TARTALOM = "#details1 > div.fobox_tartalom";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_NAME = "div.torrent_reszletek_cim";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_COL1 = "div.torrent_reszletek > div.torrent_col1";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_TYPE = "div:nth-child(2)";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_CATEGORY = "div:nth-child(2) > a:nth-child(2)";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_UPLOADED = "div:nth-child(4)";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_UPLOADER = "div:nth-child(6) > a";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_COL2 = "div.torrent_col2";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_SEEDERS = "div:nth-child(2) > a";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_LEECHERS = "div:nth-child(4) > a";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_DOWNLOADED = "div:nth-child(6)";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_SPEED = "div:nth-child(8)";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_SIZE = "div:nth-child(10)";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_FILES = "div:nth-child(14) > a";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_OTHER_VERSIONS = "div:nth-child(16) > a";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_COVER =
            "div.torrent_leiras > table > tbody > tr > td.inforbar_img > img";
    public static final String CSS_SELETOR_TORRENT_DETAILS_TITLE =
            "div.torrent_leiras > table > tbody > tr > td.inforbar_txt > div";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_MOVIE_DETAILS =
            "div.torrent_leiras > table > tbody > tr > td.inforbar_txt > table > tbody > tr";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_SAMPLE_IMAGES = "center:nth-child(3)";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_SAMPLE_IMAGE_1 =
            "table > tbody > tr:nth-child(1) > td:nth-child(1) > a";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_SAMPLE_IMAGE_2 =
            "table > tbody > tr:nth-child(1) > td:nth-child(2) > a";
    public static final String CSS_SELECTOR_TORRENT_DETAILS_SAMPLE_IMAGE_3 =
            "table > tbody > tr:nth-child(1) > td:nth-child(3) > a";
    public static final String CSS_SELECTOR_MORE_VERSIONS =
            "#profil_right > div > div.box_torrent_all_mini > div";
    public static final String CSS_SELECTOR_MORE_VERSIONS_ID_NAME =
            "div.box_nagy_mini > div.box_nev_mini_ownfree > div > div > a";
    public static final String CSS_SELECTOR_OTHER_VERSIONS_CATEGORY = "div.box_alap_img > a";
    public static final String CSS_SELECTOR_OTHER_VERSIONS_UPLOADED =
            "div.box_nagy_mini > div.box_feltoltve_other_short";
    public static final String CSS_SELECTOR_OTHER_VERSIONS_SIZE = "div.box_nagy_mini > div.box_meret2";
    public static final String CSS_SELECTOR_OTHER_VERSIONS_DOWNLOADED = "div.box_nagy_mini > div.box_d2";
    public static final String CSS_SELECTOR_OTHER_VERSIONS_SEEDERS = "div.box_nagy_mini > div.box_s2 > a";
    public static final String CSS_SELECTOR_OTHER_VERSIONS_LEECHERS = "div.box_nagy_mini > div.box_l2 > a";

    /**
     * Interface
     */
    public interface CancellationListener {
        public boolean isCancelled();
    }

    /**
     * Private constructor to prevent instantiation
     */
    private NCoreParser() {

    }

    /**
     * Extracts reCAPTCHA related data from the input HTML stream
     *
     * @param inputStream Input stream containing HTML data
     * @return            Extracted reCAPTCHA related data:
     *                    - reCAPTCHA challenge script URL
     * @throws IOException
     */
    public static Map<String,String> parseLoginForCaptcha(InputStream inputStream) throws IOException {
        String captchaChallengeScriptUrl = null;

        // Parse the input stream
        try {
            Document document = Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8,
                    NCoreConnectionManager.BASE_URI);

            Elements recaptchaChallengeScriptElements = document.select(CSS_SELECTOR_RECAPTCHA_CHALLENGE_SCRIPT);

            // Get CAPTCHA CHALLENGE SCRIPT URL
            if ((recaptchaChallengeScriptElements != null) && (recaptchaChallengeScriptElements.size() > 0)) {
                captchaChallengeScriptUrl = recaptchaChallengeScriptElements.get(0).attr(HTML_ATTR_SRC);
            }

            // Return
            if (captchaChallengeScriptUrl != null) {
                HashMap<String,String> result = new HashMap<String,String>();

                result.put(PARAM_CAPTCHA_CHALLENGE_SCRIPT_URL, captchaChallengeScriptUrl);

                return result;
            }
            else {
                return null;
            }
        } catch (IOException e) {
            throw new IOException(EXCEPTION_LOGIN_PAGE_PARSE, e);
        }
    }

    public static Map<String,String> parseReCaptchaChallengeScript(InputStream inputStream) throws IOException,
            JSONException {
        String captchaChallengeValue = null;

        try {

            // Input stream to String
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read = 0;
            while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
                baos.write(buffer, 0, read);
            }
            baos.flush();
            String challengeScriptString = baos.toString(NCoreConnectionManager.CHARSET_UTF8);

            // Find JSON object in script
            Matcher matcher = REGEX_PATTERN_RECAPTCHA_CHALLENGE_SCRIPT_JSON.matcher(challengeScriptString);

            // Extract JSON object field
            if (matcher.find()) {
                String jsonString = matcher.group(1);
                JSONTokener tokener = new JSONTokener(jsonString);
                Object next = tokener.nextValue();
                JSONObject jsonObject = (JSONObject) next;
                captchaChallengeValue = jsonObject.getString(JSON_FIELD_RECAPTCHA_CHALLENGE);
            }

            // Return
            if (captchaChallengeValue != null) {
                HashMap<String,String> result = new HashMap<String,String>();

                result.put(PARAM_CAPTCHA_CHALLENGE_VALUE, captchaChallengeValue);

                return result;
            }
            else {
                return null;
            }

        }

        catch (JSONException e) {
            throw new JSONException(EXCEPTION_RECAPTCHA_CHALLENGE_SCRIPT_PARSE);
        }

        catch (IOException e) {
            throw new IOException(EXCEPTION_RECAPTCHA_CHALLENGE_SCRIPT_PARSE, e);
        }


/*
var RecaptchaState = {
    site : '6LfMAt0SAAAAAH_ZwCXukEJaU5WEHE056HhfdsZH',
    rtl : false,
    challenge : '03AHJ_VutJndgEWG-OLH1-UosLhA-RR8WKPSCSrgVGpszpJoD11IFlCQ7YSWazTK6xB8Bx36tYgjH04nl46f1XY-p7kd9LGr-OLZn3Hlt_c0KNVIPjp5iQUvwIvel4iYXBom2lghyCSZxdpDqFa3BNwIrWOulXhmw97tcnvD9QzfKyFIn7lFt7pQCL6KF3uZgtN4PojLCjpM6T903WdImYDm4WE_9gINYaYzSR1xREfWW4TR9JfkQCX26rjQ5Zy7ni2RofmeSlNanu',
    is_incorrect : false,
    programming_error : '',
    error_message : '',
    server : 'https://www.google.com/recaptcha/api/',
    lang : 'hu',
    timeout : 1800
};
 */


    }

    /**
     * Extracts data from the input HTML stream
     *
     * @param inputStream Input stream containing HTML data
     * @return            Extracted data:
     *                    - Logout URL string
     * @throws IOException
     */
    public static Map<String,String> parseIndex(InputStream inputStream) throws IOException {
        String logoutUrl = null;

        // Parse the input stream
        try {
            Document document = Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8,
                    NCoreConnectionManager.BASE_URI);

            // Looking for the logout link
            Element logoutLink = document.getElementById(HTML_ID_LOGOUT_LINK);

            // Extract
            if (logoutLink != null) {
                logoutUrl = logoutLink.absUrl(HTML_ATTR_HREF);
            }

            // Return
            if (logoutUrl != null) {
                HashMap<String,String> result = new HashMap<String,String>();
                result.put(PARAM_LOGOUT_URL, logoutUrl);

                return result;
            }
            else {
                return null;
            }

        } catch (IOException e) {
            throw new IOException(EXCEPTION_INDEX_PAGE_PARSE, e);
        }

    }

    /**
     * Extracts data from the input HTML stream
     *
     * @param inputStream   Input stream containing HTML data
     * @return              Extracted data:
     *                      - Torrent item category
     *                      - Torrent item name
     *                      - Torrent item id
     *                      - Torrent item title
     *                      - Torrent item IMDB rank
     *                      - Torrent item size attribute
     * @throws IOException
     */
    public static List<TorrentEntry> parseTorrentList(InputStream inputStream, CancellationListener listener)
            throws IOException {
        CancellationListener cancellationListener;
        Document document = null;
        List<TorrentEntry> entries = new ArrayList<TorrentEntry>();

        // Listener for job cancel event
        cancellationListener = listener;

        // Parse the input stream
        try {

            // Build up DOM
            if (!cancellationListener.isCancelled()) {
                document = Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8,
                        NCoreConnectionManager.BASE_URI);
            }

            /*
             * Torrent list items
             */

            if (!cancellationListener.isCancelled()) {
                Elements torrentElements = document.select(CSS_SELECTOR_TORRENT_LIST_ITEM);

                if (torrentElements != null) {

                    // Iterate through torrent list items
                    for (Element torrentElement : torrentElements) {
                        TorrentEntry torrentEntry = new TorrentEntry();

                    /*
                     * Torrent item CATEGORY
                     */

                        Elements categoryElements = torrentElement.select(CSS_SELECTOR_TORRENT_ITEM_CATEGORY);

                        if ((categoryElements != null) && (categoryElements.size() > 0)) {

                            // Extract CATEGORY
                            Matcher matcher = REGEX_PATTERN_TORRENT_ITEM_CATEGORY.matcher(categoryElements.get(0).attr(HTML_ATTR_HREF));

                            // Add CATEGORY
                            if (matcher.find()) {
                                torrentEntry.setCategory(matcher.group(1));
                            }

                        }

                    /*
                     * Torrent item section
                     */

                        Elements tablaSzovegElements = torrentElement.select(CSS_SELECTOR_TORRENT_ITEM_SECTION_TABLE_SZOVEG);

                        if ((tablaSzovegElements != null) && (tablaSzovegElements.size() > 0)) {

                        /*
                         * Torrent item NAME and ID
                         */

                            Elements nameElements = tablaSzovegElements.get(0).select(CSS_SELECTOR_TORRENT_ITEM_NAME);

                            if ((nameElements != null) && (nameElements.size() > 0)) {

                                // Add NAME
                                torrentEntry.setName(nameElements.get(0).attr(HTML_ATTR_TITLE));

                                // Extract ID
                                Matcher matcher =
                                        REGEX_PATTERN_TORRENT_ITEM_ID.matcher(nameElements.get(0).attr(HTML_ATTR_HREF));

                                // Add ID
                                if (matcher.find()) {
                                    torrentEntry.setId(Long.parseLong(matcher.group(1)));
                                }

                            }

                        /*
                         * Torrent item TITLE
                         */

                            Elements titleElements =
                                    tablaSzovegElements.get(0).select(CSS_SELECTOR_TORRENT_ITEM_TITLE);

                            if ((titleElements != null) && (titleElements.size() > 0)) {

                                // Add TITLE
                                torrentEntry.setTitle(titleElements.get(0).attr(HTML_ATTR_TITLE));

                            }

                        /*
                         * Torrent item IMDB
                         */

                            Elements imdbElements =
                                    tablaSzovegElements.get(0).select(CSS_SELECTOR_TORRENT_ITEM_IMDB);

                            if ((imdbElements != null) && (imdbElements.size() > 0)) {

                                // Extract IMDB
                                Matcher matcher = REGEX_PATTERN_TORRENT_ITEM_IMDB.matcher(imdbElements.get(0).text());

                                // Add IMDB
                                if (matcher.find()) {
                                    String imdbString = matcher.group(1);

                                    // Formatted string
                                    torrentEntry.setImdb("[" + imdbString + "]");

                                    // Float value
                                    try {
                                        torrentEntry.setImdbValue(Float.parseFloat(imdbString));
                                    }

                                    catch (NumberFormatException e) {
                                        // TODO: Handle exception
                                    }

                                }

                            }

                        }

                    /*
                     * Torrent item SIZE
                     */

                        Elements sizeElements = torrentElement.select(CSS_SELECTOR_TORRENT_ITEM_SIZE);

                        if ((sizeElements != null) && (sizeElements.size() > 0)) {

                            // Add SIZE
                            torrentEntry.setSize(sizeElements.get(0).text());

                        }


                        // Add torrent entry
                        entries.add(torrentEntry);

                    }

                }

            }

            /*
             * Check for more pages
             */

            if (!cancellationListener.isCancelled()) {

                // Looking for next list page
                Element nextPageElement = document.getElementById(HTML_ID_NPA);

                // Has more results
                if (nextPageElement != null) entries.add(new TorrentEntry());

            }

        } catch (IOException e) {
            throw new IOException(EXCEPTION_TORRENT_LIST_PAGE_PARSE, e);
        }

        return entries;
    }

    /**
     * Extracts data from the input HTML stream
     *
     * @param inputStream   Input stream containing HTML data
     * @return              Extracted data:
     *                      - Torrent details name
     *                      - Torrent details type
     *                      - Torrent details uploaded
     *                      - Torrent details uploader
     * @throws IOException
     */
    public static TorrentDetails parseTorrentDetails(InputStream inputStream) throws IOException {
        TorrentDetails torrentDetails = new TorrentDetails();

        // Parse the input stream
        try {

            // Build up DOM
            Document document =
                    Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8, NCoreConnectionManager.BASE_URI);

            /*
             * Section 1
             */

            Elements foboxTartalomElements = document.select(CSS_SELECTOR_TORRENT_DETAILS_FOBOX_TARTALOM);

            if ((foboxTartalomElements != null) && (foboxTartalomElements.size() > 0)) {

                /*
                 * Torrent details NAME
                 */

                Elements nameElements = foboxTartalomElements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_NAME);

                // Add NAME
                if ((nameElements != null) && (nameElements.size() > 0)) {
                    torrentDetails.setName(nameElements.get(0).text());
                }

                /*
                 * Sample images
                 */

                Elements sampleImages = foboxTartalomElements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_SAMPLE_IMAGES);

                if ((sampleImages != null) && (sampleImages.size() > 0)) {

                    Elements sampleImage1 = sampleImages.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_SAMPLE_IMAGE_1);

                    // Add SAMPLE IMAGE 1
                    if ((sampleImage1 != null) && (sampleImage1.size() > 0)) {
                        torrentDetails.setSampleLargeImg1Url(sampleImage1.get(0).attr(HTML_ATTR_HREF));
                        torrentDetails.setSampleImg1Url(sampleImage1.get(0).child(0).attr(HTML_ATTR_SRC));
                    }

                    Elements sampleImage2 = sampleImages.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_SAMPLE_IMAGE_2);

                    // Add SAMPLE IMAGE 2
                    if ((sampleImage2 != null) && (sampleImage2.size() > 0)) {
                        torrentDetails.setSampleLargeImg2Url(sampleImage2.get(0).attr(HTML_ATTR_HREF));
                        torrentDetails.setSampleImg2Url(sampleImage2.get(0).child(0).attr(HTML_ATTR_SRC));
                    }

                    Elements sampleImage3 = sampleImages.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_SAMPLE_IMAGE_3);

                    // Add SAMPLE IMAGE 3
                    if ((sampleImage3 != null) && (sampleImage3.size() > 0)) {
                        torrentDetails.setSampleLargeImg3Url(sampleImage3.get(0).attr(HTML_ATTR_HREF));
                        torrentDetails.setSampleImg3Url(sampleImage3.get(0).child(0).attr(HTML_ATTR_SRC));
                    }

                }

                /*
                 * Section 2
                 */

                Elements col1Elements = foboxTartalomElements.select(CSS_SELECTOR_TORRENT_DETAILS_COL1);

                if ((col1Elements != null) && (col1Elements.size() > 0)) {

                    /*
                     * Torrent details TYPE
                     */

                    Elements titleElements = col1Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_TYPE);

                    // Add TYPE
                    if ((titleElements != null) && (titleElements.size() > 0)) {
                        torrentDetails.setType(titleElements.get(0).text());
                    }

                    /*
                     * Torrent details CATEGORY
                     */

                    Elements categoryElements = col1Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_CATEGORY);

                    // Add CATEGORY
                    if ((categoryElements != null) && (categoryElements.size() > 0)) {

                        // Extract CATEGORY
                        Matcher matcher = REGEX_PATTERN_TORRENT_DETAILS_CATEGORY.matcher(
                                categoryElements.get(0).attr(HTML_ATTR_HREF));

                        // Add CATEGORY
                        if (matcher.find()) {
                            torrentDetails.setCategory(matcher.group(1));
                        }

                    }

                    /*
                     * Torrent details UPLOADED
                     */

                    Elements uploadedElements = col1Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_UPLOADED);

                    // Add UPLOADED
                    if ((uploadedElements != null) && (uploadedElements.size() > 0)) {
                        torrentDetails.setUploaded(uploadedElements.get(0).text());
                    }

                    /*
                     * Torrent details UPLOADER
                     */

                    Elements uploaderElements = col1Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_UPLOADER);

                    // Add UPLOADER
                    if ((uploaderElements != null) && (uploaderElements.size() > 0)) {
                        torrentDetails.setUploader(uploaderElements.get(0).text());
                    }

                }

                /*
                 * Section 3
                 */

                Elements col2Elements = foboxTartalomElements.select(CSS_SELECTOR_TORRENT_DETAILS_COL2);

                if ((col2Elements != null) && (col2Elements.size() > 0)) {

                    /*
                     * Torrent details SEEDERS
                     */

                    Elements seedersElements = col2Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_SEEDERS);

                    // Add SEEDERS
                    if ((seedersElements != null) && (seedersElements.size() > 0)) {
                        torrentDetails.setSeeders(seedersElements.get(0).text());
                    }

                    /*
                     * Torrent details LEECHERS
                     */

                    Elements leechersElements = col2Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_LEECHERS);

                    // Add LEECHERS
                    if ((leechersElements != null) && (leechersElements.size() > 0)) {
                        torrentDetails.setLeechers(leechersElements.get(0).text());
                    }

                    /*
                     * Torrent details DOWNLOADED
                     */

                    Elements downloadedElements = col2Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_DOWNLOADED);

                    // Add DOWNLOADED
                    if ((downloadedElements != null) && (downloadedElements.size() > 0)) {

                        // Extract DOWNLOADED
                        Matcher matcher =
                                REGEX_PATTERN_TORRENT_ITEM_DOWNLOADED.matcher(downloadedElements.get(0).text());

                        // Add DOWNLOADED
                        if (matcher.find()) {
                            torrentDetails.setDownloaded(matcher.group(1));
                        }

                    }

                    /*
                     * Torrent details SPEED
                     */

                    Elements speedElements = col2Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_SPEED);

                    // Add SPEED
                    if ((speedElements != null) && (speedElements.size() > 0)) {

                        // Extract SPEED
                        Matcher matcher =
                                REGEX_PATTERN_TORRENT_ITEM_SPEED.matcher(speedElements.get(0).text());

                        // Add SPEED
                        if (matcher.find()) {
                            torrentDetails.setSpeed(matcher.group(1));
                        }

                    }

                    /*
                     * Torrent details SIZE
                     */

                    Elements sizeElements = col2Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_SIZE);

                    // Add SIZE
                    if ((sizeElements != null) && (sizeElements.size() > 0)) {

                        // Extract SIZE
                        Matcher matcher =
                                REGEX_PATTERN_TORRENT_ITEM_SIZE.matcher(sizeElements.get(0).text());

                        // Add SIZE
                        if (matcher.find()) {
                            torrentDetails.setSize(matcher.group(1));
                        }

                    }

                    /*
                     * Torrent details FILES
                     */

                    Elements filesElements = col2Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_FILES);

                    // Add FILES
                    if ((filesElements != null) && (filesElements.size() > 0)) {
                        torrentDetails.setFiles(filesElements.get(0).text());
                    }

                    /*
                     * Torrent details OTHER VERSIONS ID, OTHER VERSIONS FID
                     */

                    Elements otherVersionsElements =
                            col2Elements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_OTHER_VERSIONS);

                    if ((otherVersionsElements != null) && (otherVersionsElements.size() > 0)) {

                        // Extract OTHER VERSIONS ID, OTHER VERSIONS FID
                        Matcher matcher = REGEX_PATTERN_TORRENT_ITEM_OTHER_VERSIONS.matcher(otherVersionsElements.get(0).attr(HTML_ATTR_ONCLICK));

                        // Add OTHER VERSIONS ID, OTHER VERSIONS FID
                        if (matcher.find()) {
                            torrentDetails.setOtherVersionsId(matcher.group(1));
                            torrentDetails.setOtherVersionsFid(matcher.group(2));
                        }

                    }

                }

                /*
                 * Torrent details COVER
                 */

                Elements coverElements = foboxTartalomElements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_COVER);

                if ((coverElements != null) && (coverElements.size() > 0)) {
                    torrentDetails.setCoverUrl(coverElements.get(0).attr(HTML_ATTR_SRC));
                }

                /*
                 * Torrent details TITLE
                 */

                Elements titleElements = foboxTartalomElements.get(0).select(CSS_SELETOR_TORRENT_DETAILS_TITLE);

                if ((titleElements != null) && (titleElements.size() > 0)) {

                    torrentDetails.setTitle(titleElements.get(0).text());

                }

                /*
                 * Section 4
                 */

                Elements movieDetailsElements =
                        foboxTartalomElements.get(0).select(CSS_SELECTOR_TORRENT_DETAILS_MOVIE_DETAILS);

                // For each movie detail
                if (movieDetailsElements != null) {
                    for (Element movieDetailRow : movieDetailsElements) {
                        String movieDetailKey = movieDetailRow.child(0).text();
                        String movieDetailValue = movieDetailRow.child(1).text();

                        // Add RELEASE DATE
                        if ("Megjelenés éve:".equals(movieDetailKey)) torrentDetails.setReleaseDate(movieDetailValue);
                        // Add DIRECTOR
                        else if ("Rendező:".equals(movieDetailKey)) torrentDetails.setDirector(movieDetailValue);
                        // Add CAST
                        else if ("Szereplők:".equals(movieDetailKey)) torrentDetails.setCast(movieDetailValue);
                        // Add COUNTRY
                        else if ("Ország:".equals(movieDetailKey)) torrentDetails.setCountry(movieDetailValue);
                        // Add DURATION
                        else if ("Hossz:".equals(movieDetailKey)) torrentDetails.setDuration(movieDetailValue);
                        // Add LABELS
                        else if ("Címkék:".equals(movieDetailKey)) torrentDetails.setLabels(movieDetailValue);
                        // Add IMDB
                        else if ("IMDb értékelés:".equals(movieDetailKey)) torrentDetails.setImdb(movieDetailValue);

                    }

                }

            }

        } catch (IOException e) {
            throw new IOException(EXCEPTION_TORRENT_DETAILS_PAGE_PARSE, e);
        }

        return torrentDetails;
    }

    public static List<TorrentEntry> parseOtherVersions(InputStream inputStream) throws IOException {
        List<TorrentEntry> otherVersionsList = new ArrayList<TorrentEntry>();

        // Parse the input stream
        try {

            // Build up DOM
            Document document =
                    Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8, NCoreConnectionManager.BASE_URI);

            /*
             * Other versions
             */

            Elements otherVersionsElements = document.select(CSS_SELECTOR_MORE_VERSIONS);

            if (otherVersionsElements != null) {

                // Iterate trough more versions
                for (Element otherVersionElement : otherVersionsElements) {
                    TorrentEntry otherVersionEntry = new TorrentEntry();

                    /*
                     * More versions ID, NAME
                     */

                    Elements idNameElements = otherVersionElement.select(CSS_SELECTOR_MORE_VERSIONS_ID_NAME);

                    if ((idNameElements != null) && (idNameElements.size() > 0)) {

                        // Extract ID
                        Matcher matcher =
                                REGEX_PATTERN_TORRENT_ITEM_ID.matcher(idNameElements.get(0).attr(HTML_ATTR_HREF));

                        // Add ID
                        if (matcher.find()) {
                            otherVersionEntry.setId(Long.parseLong(matcher.group(1)));
                        }

                        // Add NAME
                        otherVersionEntry.setName(idNameElements.get(0).attr(HTML_ATTR_TITLE));

                    }

                    /*
                     * More versions CATEGORY
                     */

                    Elements categoryElements = otherVersionElement.select(CSS_SELECTOR_OTHER_VERSIONS_CATEGORY);

                    if ((categoryElements != null) && (categoryElements.size() > 0)) {

                        // Extract CATEGORY
                        Matcher matcher =
                                REGEX_PATTERN_TORRENT_ITEM_CATEGORY.matcher(categoryElements.get(0).attr(HTML_ATTR_HREF));

                        // Add CATEGORY
                        if (matcher.find()) {
                            otherVersionEntry.setCategory(matcher.group(1));
                        }

                    }

                    /*
                     * More versions UPLOADED
                     */

                    Elements uploadedElements = otherVersionElement.select(CSS_SELECTOR_OTHER_VERSIONS_UPLOADED);

                    if ((uploadedElements != null) && (uploadedElements.size() > 0)) {

                        // Add UPLOADED
                        otherVersionEntry.setUploaded(uploadedElements.get(0).text());

                    }

                    /*
                     * More versions SIZE
                     */

                    Elements sizeElements = otherVersionElement.select(CSS_SELECTOR_OTHER_VERSIONS_SIZE);

                    if ((sizeElements != null) && (sizeElements.size() > 0)) {

                        // Add SIZE
                        otherVersionEntry.setSize(sizeElements.get(0).text());

                    }

                    /*
                     * More versions DOWNLOADED
                     */

                    Elements downloadedElements = otherVersionElement.select(CSS_SELECTOR_OTHER_VERSIONS_DOWNLOADED);

                    if ((downloadedElements != null) && (downloadedElements.size() > 0)) {

                        // Add DOWNLOADED
                        otherVersionEntry.setDownloaded(downloadedElements.get(0).text());

                    }

                    /*
                     * More versions SEEDERS
                     */

                    Elements seedersElements = otherVersionElement.select(CSS_SELECTOR_OTHER_VERSIONS_SEEDERS);

                    if ((seedersElements != null) && (seedersElements.size() > 0)) {

                        // Add SEEDERS
                        otherVersionEntry.setSeeders(seedersElements.get(0).text());

                    }

                    /*
                     * More versions LEECHERS
                     */

                    Elements leechersElements = otherVersionElement.select(CSS_SELECTOR_OTHER_VERSIONS_LEECHERS);

                    if ((leechersElements != null) && (leechersElements.size() > 0)) {

                        // Add LEECHERS
                        otherVersionEntry.setLeechers(leechersElements.get(0).text());

                    }

                    otherVersionsList.add(otherVersionEntry);

                }

            }

        } catch (IOException e) {
            throw new IOException(EXCEPTION_OTHER_VERSIONS_PAGE_PARSE, e);
        }

        return otherVersionsList;

    }

}