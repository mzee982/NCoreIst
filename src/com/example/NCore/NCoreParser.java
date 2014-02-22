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

    private static final String HTML_ATTR_VALUE = "value";
    private static final String HTML_ATTR_SRC = "src";
    private static final String HTML_ATTR_HREF = "href";
    private static final String HTML_ATTR_TITLE = "title";
    private static final String HTML_ATTR_ALT = "alt";
    private static final String HTML_ATTR_ONCLICK = "onclick";
    private static final String HTML_ID_RECAPTCHA_CHALLENGE = "recaptcha_challenge_field";
    private static final String HTML_ID_RECAPTCHA_IMAGE = "recaptcha_challenge_image";
    private static final String HTML_ID_LOGOUT_LINK = "menu_11";
    private static final String HTML_ID_NPA = "nPa";
    private static final String HTML_CLASS_BOX_TORRENT_ALL = "box_torrent_all";
    private static final String HTML_CLASS_BOX_TORRENT = "box_torrent";
    private static final String HTML_CLASS_BOX_ALAP_IMG = "box_alap_img";
    private static final String HTML_CLASS_TORRENT_TXT = "torrent_txt";
    private static final String HTML_CLASS_TABLA_SZOVEG = "tabla_szoveg";
    private static final String HTML_CLASS_INFOLINK = "infolink";

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

    public static List<TorrentEntry> parseTorrentList(InputStream inputStream) throws IOException {
        List<TorrentEntry> entries = new ArrayList<TorrentEntry>();

        //
        Pattern extractIdPattern = Pattern.compile("^.*'(.*)'.*$");

        // Parse the input stream
        try {

            Document document = Jsoup.parse(inputStream, NCoreConnectionManager.CHARSET_UTF8, NCoreConnectionManager.BASE_URI);

            // Looking for torrent entries
            Elements boxTorrentElements = document.getElementsByClass(HTML_CLASS_BOX_TORRENT);

            // Found
            if (boxTorrentElements != null) {

                // Iterate through torrent entries
                for (Element element : boxTorrentElements) {
                    TorrentEntry torrentEntry = new TorrentEntry();

                    Elements boxAlapImgElements = element.getElementsByClass(HTML_CLASS_BOX_ALAP_IMG);

                    // Get category
                    if ((boxAlapImgElements != null) && (boxAlapImgElements.size() > 0)) {
                        Elements altElements = boxAlapImgElements.get(0).getElementsByAttribute(HTML_ATTR_ALT);

                        //
                        if ((altElements != null) && (altElements.size() > 0)) {

                            // Add torrent category
                            torrentEntry.setCategory(altElements.get(0).attr(HTML_ATTR_ALT));

                        }

                    }

                    Elements tablaSzoveg = element.getElementsByClass(HTML_CLASS_TABLA_SZOVEG);

                    // Get torrent name and title
                    if ((tablaSzoveg != null) && (tablaSzoveg.size() > 0)) {
                        Elements titleElements = tablaSzoveg.get(0).getElementsByAttribute(HTML_ATTR_TITLE);

                        //
                        if (titleElements != null) {

                            // Add torrent name
                            if (titleElements.size() > 0) torrentEntry.setName(titleElements.get(0).attr(HTML_ATTR_TITLE));

                            // Add torrent title
                            if (titleElements.size() > 1) torrentEntry.setTitle(titleElements.get(1).attr(HTML_ATTR_TITLE));

                        }

                        Elements onClickElements = tablaSzoveg.get(0).getElementsByAttribute(HTML_ATTR_ONCLICK);

                        // Get torrent id
                        if ((onClickElements != null) && (onClickElements.size() > 0)) {
                            String onClickString = onClickElements.get(0).attr(HTML_ATTR_ONCLICK);

                            // Extract id
                            Matcher matcher = extractIdPattern.matcher(onClickString);

                            if (matcher.find()) {
                                torrentEntry.setId(Long.parseLong(matcher.group(1)));
                            }

                        }

                    }

                    Elements infoLinkElements = element.getElementsByClass(HTML_CLASS_INFOLINK);

                    // Get IMDB info
                    if ((infoLinkElements != null) && (infoLinkElements.size() > 0)) {
                        torrentEntry.setImdb(infoLinkElements.get(0).text());
                    }


                    // Add torrent entry
                    entries.add(torrentEntry);

                }

            }

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
