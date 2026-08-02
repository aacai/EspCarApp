package zhiqiu.car.app.ble

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class InMemoryKeyValueStore : KeyValueStore {
    private val map = mutableMapOf<String, String>()
    override fun getString(key: String): String? = map[key]
    override fun putString(key: String, value: String) { map[key] = value }
    override fun getBoolean(key: String, default: Boolean): Boolean =
        map[key]?.toBooleanStrictOrNull() ?: default
    override fun putBoolean(key: String, value: Boolean) { map[key] = value.toString() }
    override fun remove(key: String) { map.remove(key) }
}

private class MutableClock(var now: Long = 1000L) {
    @Suppress("unused")
    fun advance(ms: Long) { now += ms }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CarControllerTest {

    private fun controller(
        dispatcher: TestDispatcher,
        client: BleClient = FakeBleClient(),
        settings: CarSettings = CarSettings(InMemoryKeyValueStore()),
        clock: (() -> Long)? = null,
    ): Pair<CarController, FakeBleClient> {
        val c = if (clock != null) {
            CarController(client, settings, dispatcher, clock)
        } else {
            CarController(client, settings, dispatcher)
        }
        return c to client as FakeBleClient
    }

    @Test
    fun pressSendsDirectionReleaseSendsStop() = runTest {
        val (controller, client) = controller(StandardTestDispatcher(testScheduler))
        controller.connect(client.devices.first())
        runCurrent()
        controller.pressDirection(CarDirection.Forward)
        runCurrent()
        assertTrue(client.writtenCommands.last().decodeToString().contains("F"))
        controller.releaseDirection()
        runCurrent()
        assertTrue(client.writtenCommands.last().decodeToString().contains("S"))
    }

    @Test
    fun keepAliveResendsWhileHeld() = runTest {
        val (controller, client) = controller(StandardTestDispatcher(testScheduler))
        controller.connect(client.devices.first())
        runCurrent()
        controller.pressDirection(CarDirection.Left)
        runCurrent()
        val afterPress = client.writtenCommands.size
        advanceTimeBy(CarController.KEEPALIVE_MS + 60)
        runCurrent()
        assertTrue(client.writtenCommands.size > afterPress, "保活应持续重发方向指令")
        controller.releaseDirection()
        runCurrent()
    }

    @Test
    fun watchdogSendsStopOnStaleMovement() = runTest {
        val clock = MutableClock(1000L)
        val (controller, client) = controller(
            StandardTestDispatcher(testScheduler),
            clock = { clock.now },
        )
        controller.connect(client.devices.first())
        runCurrent()
        assertEquals("前进", controller.status.value?.action)
        controller.testSimulateStaleMovement(CarDirection.Forward)
        advanceTimeBy(CarController.WATCHDOG_INTERVAL_MS + 60)
        runCurrent()
        assertTrue(
            client.writtenCommands.any { it.decodeToString().contains("S") },
            "看门狗应在运动中静止超时后补发 S",
        )
    }

    @Test
    fun connectPopulatesStatus() = runTest {
        val (controller, client) = controller(StandardTestDispatcher(testScheduler))
        controller.connect(client.devices.first())
        runCurrent()
        assertEquals(ConnectionState.Connected, controller.connectionState.value)
        assertEquals("192.168.50.100", controller.status.value?.ip)
        assertEquals(60, controller.status.value?.speed)
    }

    @Test
    fun disconnectResetsStatusAndState() = runTest {
        val (controller, client) = controller(StandardTestDispatcher(testScheduler))
        controller.connect(client.devices.first())
        runCurrent()
        assertTrue(controller.status.value != null)
        controller.disconnect()
        runCurrent()
        assertEquals(ConnectionState.Disconnected, controller.connectionState.value)
        assertNull(controller.status.value)
    }

    @Test
    fun autoReconnectConnectsToRememberedDevice() = runTest {
        val client = FakeBleClient()
        val settings = CarSettings(InMemoryKeyValueStore())
        val (first, _) = controller(StandardTestDispatcher(testScheduler), client, settings)
        first.connect(client.devices.first())
        runCurrent()
        assertTrue(settings.lastDeviceId != null)

        val (second, _) = controller(StandardTestDispatcher(testScheduler), client, settings)
        second.autoReconnectIfNeeded()
        advanceTimeBy(300)
        runCurrent()
        advanceTimeBy(300)
        runCurrent()
        assertEquals(ConnectionState.Connected, second.connectionState.value)
    }

    @Test
    fun scanListsDevices() = runTest {
        val (controller, client) = controller(StandardTestDispatcher(testScheduler))
        controller.startScan()
        runCurrent()
        assertEquals(2, controller.scanDevices.value.size)
        controller.stopScan()
    }
}
