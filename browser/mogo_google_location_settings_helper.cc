// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "c4a/browser/mogo_google_location_settings_helper.h"

// Factory function
GoogleLocationSettingsHelper* GoogleLocationSettingsHelper::Create() {
  return new MogoGoogleLocationSettingsHelper();
}

MogoGoogleLocationSettingsHelper::MogoGoogleLocationSettingsHelper()
    : GoogleLocationSettingsHelper() {
}

MogoGoogleLocationSettingsHelper::
    ~MogoGoogleLocationSettingsHelper() {
}

std::string MogoGoogleLocationSettingsHelper::GetAcceptButtonLabel() {
  return "Allow";
}

void MogoGoogleLocationSettingsHelper::ShowGoogleLocationSettings() {
}

bool MogoGoogleLocationSettingsHelper::
    IsGoogleAppsLocationSettingEnabled() {
  return true;
}

bool MogoGoogleLocationSettingsHelper::IsMasterLocationSettingEnabled() {
  return true;
}
