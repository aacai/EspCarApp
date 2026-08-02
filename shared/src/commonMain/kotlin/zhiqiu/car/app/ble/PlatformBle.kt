package zhiqiu.car.app.ble

/** 平台是否支持 BLE（KMP 各目标均有实现，默认 true；可随平台细化）。 */
internal expect val platformBleSupported: Boolean

/** 平台设备发现方式：Web(wasmJs) 用系统选择器 Pick，其余用扫描 Scan。 */
internal expect val platformDiscoveryMode: DiscoveryMode
