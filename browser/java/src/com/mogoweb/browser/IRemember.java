/*
 *  Copyright (c) 2012, The Linux Foundation. All rights reserved.
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

import android.content.Context;
import android.content.SharedPreferences;

public class IRemember {

  private static final String SHAP_NAME = "swesp";

  // BrowserUI
  public static final String KEY_U_BALLOON_DISMISSED  = "user-balloon-dismissed-X";
  public static final String KEY_U_HOMESCREEN_COUNTER = "user-home-screen-counter-X2";


  /**
   * Sets the value of a Boolean variable and returns the previous value.
   *
   * @param context needed
   * @param key the key to remember
   * @param value the value to store
   * @param defValue the value to return if key was not present
   * @return the previous value, or the defValue if missing
   */
  static boolean getAndSetBoolean(Context context, String key, boolean value, boolean defValue) {
    SharedPreferences p = sp(context);
    boolean prevValue = defValue;
    if (p.contains(key)) {
      try { prevValue = p.getBoolean(key, defValue); } catch (Exception e) {}
    }
    p.edit().putBoolean(key, value).commit();
    return prevValue;
  }

  public static boolean getBoolean(Context context, String key, boolean defValue) {
    SharedPreferences p = sp(context);
    boolean value = defValue;
    try { value = p.getBoolean(key, defValue); } catch (Exception e) {}
    return value;
  }

  public static void setBoolean(Context context, String key, boolean b) {
    spe(context).putBoolean(key, b).commit();
  }

  /* String */

  public static String getString(Context context, String key, String defValue) {
    SharedPreferences p = sp(context);
    String value = defValue;
    try { value = p.getString(key, defValue); } catch (Exception e) {}
    return value;
  }

  /**
   * @param value The value to assign. If null the preference key will be removed.
   */
  public static void setString(Context context, String key, String value) {
    if (value != null)
      spe(context).putString(key, value).commit();
    else
      remove(context, key);
  }

  /* integers */

  public static int getInt(Context context, String key, int defValue) {
    SharedPreferences p = sp(context);
    int value = defValue;
    try { value = p.getInt(key, defValue); } catch (Exception e) {}
    return value;
  }

  public static void setInt(Context context, String key, int value) {
    spe(context).putInt(key, value).commit();
  }

  public static int getIntAndIncrement(Context context, String key, int defValue) {
    SharedPreferences p = sp(context);
    int value = defValue;
    try { value = p.getInt(key, defValue); } catch (Exception e) { }
    p.edit().putInt(key, value + 1).commit();
    return value;
  }

  /* Dates */

  /*public static Date testDate(Context context, String key, Date defValue) {
      SharedPreferences p = sp(context);
      Date value = defValue;
      try { String encoded = p.getString(key, null); if (encoded != null) value = Numbers.stringToLocaleDate(encoded); } catch (Exception e) {}
      return value;
  }

  public static void setDate(Context context, String key, Date value) {
    if (value != null)
      spe(context).putString(key, Numbers.dateToLocaleString(value)).commit();
    else
      remove(context, key);
  }*/

  /* Convenience functions */

  public static boolean contains(Context context, String key) {
    return sp(context).contains(key);
  }

  public static void remove(Context context, String key) {
    spe(context).remove(key).commit();
  }

  private static SharedPreferences sp(Context context) {
    return context.getSharedPreferences(SHAP_NAME, Context.MODE_PRIVATE /*Context.MODE_MULTI_PROCESS*/);
  }

  private static SharedPreferences.Editor spe(Context context) {
    return context.getSharedPreferences(SHAP_NAME, Context.MODE_PRIVATE /*Context.MODE_MULTI_PROCESS*/).edit();
  }

}
