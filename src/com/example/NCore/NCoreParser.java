package com.example.NCore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
    private static final String EXCEPTION_INDEX_PAGE_PARSE = "Error occured during parsing the index page.";
    private static final String EXCEPTION_TORRENT_LIST_PAGE_PARSE =
            "Error occured during parsing the torrent list page.";

    // Parameters
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

    // Regex patterns
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_CATEGORY = Pattern.compile("^.*=(.*)$");
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_ID = Pattern.compile("^.*\\((.*)\\).*$");
    private static final Pattern REGEX_PATTERN_TORRENT_ITEM_IMDB = Pattern.compile("^\\[imdb:\\s(.*)\\]$");

    // CSS selectors
    public static final String CSS_SELECTOR_TORRENT_LIST_ITEM =
            "#main_tartalom > div.lista_all > div.box_torrent_all > div.box_torrent";
    public static final String CSS_SELECTOR_TORRENT_ITEM_CATEGORY = "div.box_alap_img > a";
    public static final String CSS_SELECTOR_TORRENT_ITEM_SECTION_TABLE_SZOVEG = "div > div > div.tabla_szoveg";
    public static final String CSS_SELECTOR_TORRENT_ITEM_NAME = "div > a";
    public static final String CSS_SELECTOR_TORRENT_ITEM_TITLE = "div > div > div.siterank > span";
    public static final String CSS_SELECTOR_TORRENT_ITEM_IMDB = "div.torrent_txt > div > div.siterank > a";
    public static final String CSS_SELECTOR_TORRENT_ITEM_SIZE = "div > div.box_meret2";

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
     *                    - reCAPTCHA challenge value
     *                    - reCAPTCHA image
     * @throws IOException
     */
    public static Map<String,String> parseLoginForCaptcha(InputStream inputStream) throws IOException {
        String captchaChallengeValue = null;
        String captchaImageUrl = null;

        // Parse the input stream
        try {
            Document document = Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8, NCoreConnectionManager.BASE_URI);

            // Looking for reCAPTCHA elements
            Element captchaChallenge = document.getElementById(HTML_ID_RECAPTCHA_CHALLENGE);
            Element captchaImage = document.getElementById(HTML_ID_RECAPTCHA_IMAGE);

            // Extract the reCAPTCHA related data
            if ((captchaChallenge != null) && (captchaImage != null)) {
                captchaChallengeValue = captchaChallenge.attr(HTML_ATTR_VALUE);
                captchaImageUrl = captchaImage.absUrl(HTML_ATTR_SRC);
            }

            // Return
            if ((captchaChallengeValue != null) && (captchaImageUrl != null)) {
                HashMap<String,String> result = new HashMap<String,String>();
                result.put(PARAM_CAPTCHA_CHALLENGE_VALUE, captchaChallengeValue);
                result.put(PARAM_CAPTCHA_IMAGE_URL, captchaImageUrl);

                return result;
            }
            else {
                return null;
            }
        } catch (IOException e) {
            throw new IOException(EXCEPTION_LOGIN_PAGE_PARSE, e);
        }
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
            Document document = Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8, NCoreConnectionManager.BASE_URI);

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
    public static List<TorrentEntry> parseTorrentList(InputStream inputStream) throws IOException {
        List<TorrentEntry> entries = new ArrayList<TorrentEntry>();

        // Parse the input stream
        try {

            // Build up DOM
            Document document = Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8, NCoreConnectionManager.BASE_URI);

            /*
             * Torrent list items
             */

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
                            Matcher matcher = REGEX_PATTERN_TORRENT_ITEM_ID.matcher(nameElements.get(0).attr(HTML_ATTR_ONCLICK));

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
                                torrentEntry.setImdb("[" + matcher.group(1) + "]");
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

            /*
             * Check for more pages
             */

            // Looking for next list page
            Element nextPageElement = document.getElementById(HTML_ID_NPA);

            // Has more results
            if (nextPageElement != null) entries.add(new TorrentEntry());

        } catch (IOException e) {
            throw new IOException(EXCEPTION_TORRENT_LIST_PAGE_PARSE, e);
        }

        return entries;
    }

}
