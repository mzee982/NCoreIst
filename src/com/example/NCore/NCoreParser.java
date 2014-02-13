package com.example.NCore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public final class NCoreParser {

    // Exceptions
    private static final String EXCEPTION_LOGIN_PAGE_PARSE = "Error occured during parsing the login page.";
    private static final String EXCEPTION_INDEX_PAGE_PARSE = "Error occured during parsing the index page.";

    // Parameters
    public static final String PARAM_CAPTCHA_CHALLENGE_VALUE = "PARAM_CAPTCHA_CHALLENGE_VALUE";
    public static final String PARAM_CAPTCHA_IMAGE_URL = "PARAM_CAPTCHA_IMAGE_URL";
    public static final String PARAM_LOGOUT_URL = "PARAM_LOGOUT_URL";

    private static final String HTML_ATTR_VALUE = "value";
    private static final String HTML_ATTR_SRC = "src";
    private static final String HTML_ATTR_HREF = "href";
    private static final String HTML_ID_RECAPTCHA_CHALLENGE = "recaptcha_challenge_field";
    private static final String HTML_ID_RECAPTCHA_IMAGE = "recaptcha_challenge_image";
    private static final String HTML_ID_LOGOUT_LINK = "menu_11";

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

}
