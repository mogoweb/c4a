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
package com.mogoweb.browser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import com.mogoweb.browser.utils.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.sql.Timestamp;
import java.util.Date;

public class CrashHandler implements UncaughtExceptionHandler {

    private static CrashHandler sCrashHandler;
    private static Context mContext;
    private static ExceptionInfo mExceptionInfo;
    private static IntentFilter mIntentFilter;
    private static NetworkChangeReceiver mNetworkChangeReceiver;
    private static UncaughtExceptionHandler mDefaultUEH;

    private static String TAG = "SWE_UI";
    private static String FILENAME = "swe_crash.log";
    private static boolean mNeedToUnregisterReceiver = false;

    public static boolean mFromBroadcastReciever = false;

    private class ExceptionInfo {

        private String getVersion() {
            return "";// return mContext.getString(R.string.swe_browser_partial_version);
        }

        private String getTimeStamp(){
            Date d = new Date();
            return (new Timestamp(d.getTime())).toString();
        }

        private String getCommitId() {
            // String commitId = mContext.getString(R.string.swe_browser_commit_id);
            return "";// return (commitId.isEmpty() ? "" : "." + commitId.substring(0, 8)) ;
        }

        private JSONArray readFromFile() {
            //Read the file
            JSONArray finalResult = null;
            try {
                FileInputStream fis = mContext.openFileInput(FILENAME);
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                JSONTokener tokener = new JSONTokener(sb.toString());
                finalResult = new JSONArray(tokener);
            } catch (IOException e) {
                // writing exception to log
                e.printStackTrace();
                Log.v(TAG, "Exception = "+e.toString());
            } catch (JSONException e) {
                // writing exception to log
                e.printStackTrace();
                Log.v(TAG, "Exception = "+e.toString());
            }
            return finalResult;
        }

        private void processException(Exception e) {

            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            String stacktrace = result.toString();
            printWriter.close();
            writeToFile(stacktrace);
        }

        private void writeToFile(String stacktrace) {
            FileOutputStream fos = null;
            try {
                //Get the File Stream path
                File  logFile = mContext.getFileStreamPath(FILENAME);
                JSONArray mJsonArray = null;
                if (logFile.exists()) {
                    //Read the existing JSON array from the file
                    mJsonArray = readFromFile();
                } else {
                    //Create an empty JSON array
                    mJsonArray =  new JSONArray();
                }

                //Recreate the file
                fos = mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);

                //Create the JSON Object with the information of version, timestamp and stacktrace
                JSONObject jObject = new JSONObject();
                jObject.put("version", getVersion()+getCommitId());
                jObject.put("timestamp", getTimeStamp());
                jObject.put("trace", stacktrace);

                //Append this JSON object to the JSONArray
                mJsonArray.put(jObject);

                //Write the JSON Array to the file
                fos.write((mJsonArray.toString()).getBytes());

                //close the file
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                // writing exception to log
                e.printStackTrace();
                Log.v(TAG, "Exception = "+e.toString());
            } catch (JSONException e) {
                // writing exception to log
                e.printStackTrace();
                Log.v(TAG, "Exception = "+e.toString());
            }
        }

        public void uploadToServer() {
            String serverUrl = mContext.getString(R.string.server_url);

            if (serverUrl != null && serverUrl.length() > 0) {
                //spawn a background thread for uploading file to the server
                new ServerUploadTask().execute(serverUrl);
            }
        }

        class ServerUploadTask extends AsyncTask <String, Void, Void> {

            @Override
            protected Void doInBackground(String... params) {

                //Getting all the params
                String serverUrl = params[0];
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPut httpPut = new HttpPut(serverUrl);
                HttpResponse response;

                try {
                    JSONArray mJsonArray = readFromFile();

                    StringEntity se = new StringEntity(mJsonArray.toString());
                    se.setContentType("application/json;charset=UTF-8");

                    httpPut.setEntity(se);

                    // Execute HTTP Put Request
                    response = httpClient.execute(httpPut);

                    //Get the status code
                    StatusLine statusLine = response.getStatusLine();

                    //Check if we recieved  status code of 200 or something else
                    if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                       //Closes the connection.
                       response.getEntity().getContent().close();
                       // delete the file
                        mContext.deleteFile(FILENAME);
                        // check unregister receiver and common flag between the class
                        if (mNeedToUnregisterReceiver  &&  mFromBroadcastReciever) {
                            mContext.unregisterReceiver(mNetworkChangeReceiver);
                            mNeedToUnregisterReceiver = false;
                            mFromBroadcastReciever = false;
                        }
                    } else {
                        if (!mNeedToUnregisterReceiver) {
                            // for the usecase when wifi was present initially but could not upload the server
                            // for resons like either server is down or wifi connection is flaky
                            mNeedToUnregisterReceiver = true;
                            //activating the listener
                            mContext.registerReceiver(mNetworkChangeReceiver, mIntentFilter);
                        }
                        throw new IOException(statusLine.getReasonPhrase());
                    }
                } catch (ClientProtocolException e) {
                    // writing exception to log
                    e.printStackTrace();
                    Log.v(TAG, "Exception = "+e.toString());
                } catch (IOException e) {
                    // writing exception to log
                    e.printStackTrace();
                    Log.v(TAG, "Exception = "+e.toString());
                }
                return null;
            }
        }
    }

    // Should be called only once
    public static CrashHandler create(Context context) {
        if (sCrashHandler == null) {
            sCrashHandler = new CrashHandler(context);
        }
        return sCrashHandler;
    }

    public static CrashHandler getInstance() {
        return sCrashHandler;
    }

    private CrashHandler(Context context) {
        mContext = context;
        mExceptionInfo = new ExceptionInfo();
        // ensure CrashHandler is singleton class
        if (sCrashHandler != null) {
            Logger.error("CrashHandler already created");
            return;
        }
        sCrashHandler = this;
        mDefaultUEH = Thread.getDefaultUncaughtExceptionHandler();

        //Intiating Listener and intent for listener
        mNetworkChangeReceiver = new NetworkChangeReceiver();
        mIntentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    public static boolean isConnectedToNetwork() {
        // check Wifi
        final ConnectivityManager connMgr = (ConnectivityManager)
            mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connMgr.getActiveNetworkInfo();

        boolean isWiFi = false;
        boolean isConnected = false;
        try{
            isWiFi = (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
            isConnected = activeNetwork.isConnected();
        } catch(Exception ex) {
            Logger.dumpTrace(ex);
        }

        return (isWiFi && isConnected);
    }

    /**
    * Allowing exception to be thrown from try catch
    **/
    public static void throwException(Exception e) {
        mExceptionInfo.processException(e);
    }

    public static void handleServerUpload() {

        // check if file exists
        File logFile = mContext.getFileStreamPath(FILENAME);
        if (logFile.exists()) {
            if (isConnectedToNetwork()) {
                mNeedToUnregisterReceiver = false;
                boolean fromBroadcastReceiver = false;
                //upload to the server
                initiateUpload(fromBroadcastReceiver);
            } else {
                mNeedToUnregisterReceiver = true;
                //activating the listener
                mContext.registerReceiver(mNetworkChangeReceiver, mIntentFilter);
            }
        }
    }

    public static void initiateUpload(boolean fromBroadcastReceiver) {
        mFromBroadcastReciever = fromBroadcastReceiver;
        mExceptionInfo.uploadToServer();
    }

    public void uncaughtException(Thread t, Throwable e) {
        Exception ex = new Exception(e);
        mExceptionInfo.processException(ex);
        mDefaultUEH.uncaughtException(t, e);
    }

}