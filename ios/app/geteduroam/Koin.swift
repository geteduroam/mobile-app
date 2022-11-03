//
//  KoinApplication.swift
//  KaMPStarteriOS
//
//  Created by Russell Wolf on 6/18/20.
//  Copyright © 2020 Touchlab. All rights reserved.
//

import Foundation
import core

func startKoin() {
    let iosAppInfo = IosAppInfo()
    let doOnStartup = { }

    let koinApplication = KoinIOSKt.doInitKoinIos(
        appInfo: iosAppInfo,
        doOnStartup: doOnStartup
    )
    _koin = koinApplication.koin
}

private var _koin: Koin_coreKoin?
var koin: Koin_coreKoin {
    return _koin!
}

class IosAppInfo: AppInfo {
    let appId: String = Bundle.main.bundleIdentifier!
}
