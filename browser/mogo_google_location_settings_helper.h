// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef C4A_MOGO_MOGO_GOOGLE_LOCATION_SETTINGS_HELPER_H_
#define C4A_MOGO_MOGO_GOOGLE_LOCATION_SETTINGS_HELPER_H_

#include "chrome/browser/android/google_location_settings_helper.h"

// Stub implementation of GoogleLocationSettingsHelper for mogo.
class MogoGoogleLocationSettingsHelper
    : public GoogleLocationSettingsHelper {
 public:
  // GoogleLocationSettingsHelper implementation:
  virtual std::string GetAcceptButtonLabel() OVERRIDE;
  virtual void ShowGoogleLocationSettings() OVERRIDE;
  virtual bool IsMasterLocationSettingEnabled() OVERRIDE;
  virtual bool IsGoogleAppsLocationSettingEnabled() OVERRIDE;

 protected:
  MogoGoogleLocationSettingsHelper();
  virtual ~MogoGoogleLocationSettingsHelper();

 private:
  friend class GoogleLocationSettingsHelper;

  DISALLOW_COPY_AND_ASSIGN(MogoGoogleLocationSettingsHelper);
};

#endif  // C4A_MOGO_MOGO_GOOGLE_LOCATION_SETTINGS_HELPER_H_
