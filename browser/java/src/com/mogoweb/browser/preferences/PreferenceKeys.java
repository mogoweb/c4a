// Copyright (c) 2013 mogoweb. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
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

package com.mogoweb.browser.preferences;

public interface PreferenceKeys {

    // ----------------------
    // Keys for basic preferences
    // ----------------------
    static final String PREF_ENABLE_AUTOFILL = "enable_autofill";

    static final String PREF_ENABLE_GEOLOCATION = "enable_geolocation";
    static final String PREF_BLOCK_POPUPS = "block_popups";
    static final String PREF_ENABLE_JAVASCRIPT = "enable_javascript";
    static final String PREF_USER_AGENT = "user_agent";
    static final String PREF_ENABLE_MEMORY_MONITOR = "enable_memory_monitor";

}
