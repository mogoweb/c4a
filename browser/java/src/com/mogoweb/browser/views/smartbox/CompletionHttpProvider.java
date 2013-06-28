/*
 *  Copyright (c) 2013, The Linux Foundation. All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 *      * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *     * Neither the name of The Linux Foundation nor the names of its
 *       contributors may be used to endorse or promote products derived
 *       from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
 *  ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
 *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 *  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
 *  IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.mogoweb.browser.views.smartbox;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import com.mogoweb.browser.utils.Logger;

import android.net.http.AndroidHttpClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public abstract class CompletionHttpProvider extends CompletionProvider {

    private static final int HTTP_STATUS_CODE_OK = 200;

    // private AndroidHttpClient mHttpClient;
    private final int mMaxTries;

    public CompletionHttpProvider(int maxTries) {
        mMaxTries = maxTries > 0 ? maxTries : 1;
    }

    /**
     * Builds an URL containing a query for a get request.
     *
     * @param urlPattern The url of the query, containing placeholders
     * @param query The query to be peformed
     * @return the query ready for the request, or null
     */
    protected String prepareServiceQuery(String urlPattern, String query) {
        // encode the query for the URL
        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query.toString(), "UTF-8");
            if (encodedQuery == null || encodedQuery.length() < 1)
                return null;
        } catch (UnsupportedEncodingException e1) {
            Logger.info("prepareServiceQuery: encoding error.");
            return null;
        }

        // get the current language
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage();
        if (language == null || language.isEmpty())
            language = "en";

        // perform the mappings
        return urlPattern
                .replace("$CLIENT", "chrome").replace("$LANG", language)
                .replace("$QUERY", encodedQuery);
    }

    /**
     * @param serviceUrl the URL to (HTTP) GET
     * @return The content of the Http response, or Null Note: may throw
     *         exceptions too, so it's better to catch them when invokind this
     *         function.
     */
    protected String getHttpResponse(String serviceUrl) {

        // create the http client upon demand
        // if (mHttpClient == null)
        AndroidHttpClient mHttpClient = AndroidHttpClient.newInstance("Android");
        HttpGet httpGet = new HttpGet(serviceUrl);

        for (int i = 0; i < mMaxTries; i++) {

            // synchronously wait for the response from the server
            HttpResponse response;
            try {
                response = mHttpClient.execute(httpGet);
            } catch (Exception e) {
                Logger.info("getHttpResponse: error querying: " + e.getMessage());
                continue;
            }

            // got a response: get the status code and content string
            StatusLine status = response.getStatusLine();
            int statusCode = status.getStatusCode();
            String responseContent = null;
            try {
                responseContent = EntityUtils.toString(response.getEntity());
            } catch (Exception e) {
                Logger.info("getHttpResponse: can't parse the response from the server: " + e.getMessage());
            }

            // close the connection
            try {
                response.getEntity().consumeContent();
            } catch (IOException e) {
            }

            // check the status code
            if ((statusCode != HTTP_STATUS_CODE_OK) || responseContent == null) {
                Logger.info("getHttpResponse: server responded with an error: " + status.getReasonPhrase() + " (" + statusCode + ")");
                continue;
            }

            // (print out the headers)
            // for (Header h : response.getAllHeaders())
            // Logger.info("H: "+h.getName()+" v: "+h.getValue()+" s: "+h.toString());

            // we got a good response; that's all we needed
            mHttpClient.close();
            return responseContent;
        }

        // we ran out of tries.. and luck
        Logger.info("getHttpResponse: tried for " + mMaxTries + " times but the search was still unsuccessful");
        mHttpClient.close();
        return null;
    }

}
