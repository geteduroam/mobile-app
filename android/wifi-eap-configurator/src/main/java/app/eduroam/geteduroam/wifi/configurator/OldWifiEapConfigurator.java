package app.eduroam.geteduroam.wifi.configurator;

import static androidx.core.content.PermissionChecker.checkSelfPermission;

import android.Manifest;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiNetworkSuggestion;
import android.net.wifi.hotspot2.PasspointConfiguration;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import app.eduroam.geteduroam.wifi.configurator.config.AbstractConfigurator;
import app.eduroam.geteduroam.wifi.configurator.config.IntentConfigurator;
import app.eduroam.geteduroam.wifi.configurator.config.LegacyConfigurator;
import app.eduroam.geteduroam.wifi.configurator.config.SuggestionConfigurator;
import app.eduroam.geteduroam.wifi.configurator.config.WifiProfile;
import app.eduroam.geteduroam.wifi.configurator.exception.EapConfigCAException;
import app.eduroam.geteduroam.wifi.configurator.exception.EapConfigClientCertificateException;
import app.eduroam.geteduroam.wifi.configurator.exception.EapConfigValueException;
import app.eduroam.geteduroam.wifi.configurator.exception.NetworkConfigurationException;
import app.eduroam.geteduroam.wifi.configurator.exception.NetworkInterfaceException;
import app.eduroam.geteduroam.wifi.configurator.exception.NetworkSuggestionException;

/**
 * Class for communicating between ionic and the Android Wi-Fi API
 * <p>
 * This is the only class that can import from the package {@code com.getcapacitor},
 * and the only class allowed to handle objects from it.  It is responsible for parsing incoming
 * calls from ionic (although this parsing can be as simple as extracting a JSONObject from it) and
 * returning a result back.
 * <p>
 * All methods marked with {code @PluginMethod} can be called from ionic.  Returned is a JSON object
 * that will always contain the boolean {code success} and string {code message}, where
 * {code message} is a lower-case string in the form {@code "plugin.wifieapconfigurator."},
 * followed by either {@code "success"} or {@code "error"}, followed by one or more keywords
 * indicating what the result applies to.
 */
@RequiresApi(api = Build.VERSION_CODES.O)
public class OldWifiEapConfigurator {

    /**
     * Configure networks using the given profile
     *
     * @param profile Wi-Fi configuration
     * @throws SecurityException             When adding the network was disallowed
     * @throws NetworkConfigurationException The network connection was not created
     * @throws NetworkSuggestionException    One or more {@code NetworkSuggestions} could not be installed
     * @throws NetworkInterfaceException     The network interface does not support Passpoint
     */
    @RequiresPermission(anyOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CHANGE_NETWORK_STATE})
    public void configureAP(WifiProfile profile) throws SecurityException, NetworkConfigurationException, NetworkSuggestionException, NetworkInterfaceException {
        SharedPreferences.Editor editor = getPreferences().edit();
        int targetSDK = getContext().getApplicationContext().getApplicationInfo().targetSdkVersion;

        // We prefer the legacy method, because it's more stable.
        // But Android blocks legacy SSID configurations from version Q,
        // and legacy Passpoint configurations from version R;
        // on and above these versions we have to use WifiNetworkSuggestions.

        // Suggestion API, required from Android 11 (API 30)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && targetSDK >= Build.VERSION_CODES.R) {
            //requestPermission(Manifest.permission.CHANGE_NETWORK_STATE);

            List<WifiNetworkSuggestion> suggestions = profile.buildSSIDSuggestions();
            WifiNetworkSuggestion passpointSuggestion = profile.buildPasspointSuggestion();
            if (passpointSuggestion != null) suggestions.add(passpointSuggestion);

            try {
                IntentConfigurator intentConfigurator = new IntentConfigurator(getContext());
                intentConfigurator.installSuggestions(suggestions);
            } catch (IllegalStateException e) {
                // A bug in older builds of Android 11 prevents us from using intents
                // https://issuetracker.google.com/issues/171375137?pli=1#comment14
                SuggestionConfigurator suggestionConfigurator = new SuggestionConfigurator(getContext());
                suggestionConfigurator.installSuggestions(suggestions);
            }
        } else
            // Suggestion API for SSID, legacy API for Passpoint, required for Android 10 (API 29)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSDK >= Build.VERSION_CODES.Q) {
                requestPermission(Manifest.permission.CHANGE_NETWORK_STATE);

                // We have to do SSIDs with suggestions and Passpoint with legacy
                SuggestionConfigurator suggestionConfigurator = new SuggestionConfigurator(getContext());
                LegacyConfigurator legacyConfigurator = new LegacyConfigurator(getContext());

                List<WifiNetworkSuggestion> suggestions = profile.buildSSIDSuggestions();
                suggestionConfigurator.installSuggestions(suggestions);

                PasspointConfiguration passpointConfig = profile.buildPasspointConfig();
                if (passpointConfig != null) {
                    configurePasspoint(legacyConfigurator, passpointConfig);
                }
            } else
            // Legacy API, allowed for Android <= 9 (API <= 28)
            {
                requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);

                // Everything below Q (below Android 10, below API version 29)
                // We get to use the legacy API for everything. YAY!

                LegacyConfigurator legacyConfigurator = new LegacyConfigurator(getContext());
                String[] ssids = profile.getSSIDs();

                PasspointConfiguration passpointConfig = profile.buildPasspointConfig();

                try {
                    legacyConfigurator.removeNetwork(ssids);
                } catch (RuntimeException | NetworkConfigurationException e) {
                    /* ignore exceptions when removing the network,
                     * since many Android versions don't let us remove them,
                     * but allow us to override them
                     */
                }

                int firstWifiId = -1;
                for (WifiConfiguration config : profile.buildWifiConfigurations()) {
                    // throws NetworkConfigurationException if we weren't able to override the network
                    int wifiId = legacyConfigurator.configureNetworkConfiguration(config);

                    // TODO There can be more than one SSID, but we can only store one
                    // (we're in a loop, we will only consider the first)
                    if (wifiId != -1 && firstWifiId == -1) {
                        editor.putInt("netId", wifiId);
                        legacyConfigurator.connectNetwork(wifiId);
                        firstWifiId = wifiId;
                    }
                }
                // Now we're out of the SSID loop, store the changes we made in the loop
                editor.apply();

                if (passpointConfig != null)
                    configurePasspoint(legacyConfigurator, passpointConfig);
            }

    }

    /**
     * Validate the passphrase on a client certificate
     * <p>
     * Call must contain these variables:
     * {@code String certificate} Base64 encoded PKCS12 store
     * {@code String passPhrase} Passphrase to test
     *
     * @see WifiProfile#validatePassPhrase(String, String)
     */
    @PluginMethod
    public void validatePassPhrase(PluginCall call) {
        JSObject object = new JSObject();

        boolean success = WifiProfile.validatePassPhrase(call.getString("certificate"), call.getString("passPhrase"));

        object.put("success", success);
        if (success) {
            object.put("message", "plugin.wifieapconfigurator.success.passphrase.validation");
        } else {
            object.put("message", "plugin.wifieapconfigurator.error.passphrase.validation");
        }

        call.success(object);
    }

    /**
     * Remove an SSID network
     * <p>
     * Call must contain {@code String ssid}.
     *
     * @see AbstractConfigurator#removeNetwork(String...)
     */
    @PluginMethod
    public void removeNetwork(PluginCall call) {
        JSObject object = new JSObject();
        object.put("success", false);
        String ssid = call.getString("ssid");
        if (ssid == null || "".equals(ssid)) {
            object.put("message", "plugin.wifieapconfigurator.error.ssid.missing");
        } else {
            try {
                getSSIDConfigurator().removeNetwork(call.getString("ssid"));

                object.put("success", true);
                object.put("message", "plugin.wifieapconfigurator.success.network.removed");
            } catch (NetworkConfigurationException e) {
                object.put("message", "plugin.wifieapconfigurator.error.network.remove");
            }
        }
        call.success(object);
    }

    /**
     * Enable the Wi-Fi interface
     * <p>
     * Call does not need to provide anything
     *
     * @see AbstractConfigurator#enableWifi()
     */
    @PluginMethod
    public void enableWifi(PluginCall call) {
        JSObject object = new JSObject();
        object.put("success", false);
        try {
            getSSIDConfigurator().enableWifi();
            object.put("success", true);
            object.put("message", "plugin.wifieapconfigurator.success.wifi.enabled");
        } catch (NetworkInterfaceException e) {
            object.put("message", "plugin.wifieapconfigurator.error.wifi.disabled");
        }

        call.success(object);
    }

    /**
     * Check if an SSID network is associated
     * <p>
     * Call must contain {@code String ssid}.
     * <p>
     * TODO: Document, it is unclear whether "associated" or "configured" is meant
     * Probably "configured" is meant, right?
     *
     * @see AbstractConfigurator#areAnyNetworksConfigured(String...)
     */
    @PluginMethod
    public boolean isNetworkAssociated(PluginCall call) throws JSONException {
        Object[] ssids = call.getArray("ssid").toList().toArray();
        return getSSIDConfigurator().areAnyNetworksConfigured(Arrays.copyOf(ssids, ssids.length, String[].class));
    }

    /**
     * Determines whether a certain SSID is reachable
     * <p>
     * Call must contain {@code String ssid}.
     *
     * @see AbstractConfigurator#reachableSSID(String)
     */
    @PluginMethod
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void reachableSSID(PluginCall call) {
        JSObject object = new JSObject();
        object.put("success", false);
        try {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            if (getSSIDConfigurator().reachableSSID(call.getString("ssid"))) {
                object.put("success", true);
                object.put("message", "plugin.wifieapconfigurator.success.network.reachable");
            } else {
                object.put("message", "plugin.wifieapconfigurator.success.network.missing");
            }
        } catch (SecurityException e) {
            Log.e(e.getClass().getSimpleName(), e.getMessage());

            object.put("message", "plugin.wifieapconfigurator.error.permission.notGranted");
            //call.error("plugin.wifieapconfigurator.error.permission.notGranted", e);
        }
        call.success(object);
    }

    /**
     * Determines if the device is connected to an SSID
     * <p>
     * Call must contain {@code String ssid}.
     * <p>
     * TODO: Documentation, this is the actual "associated" test?
     *
     * @see AbstractConfigurator#isConnectedSSID(String)
     */
    @PluginMethod
    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    public void isConnectedSSID(PluginCall call) {
        JSObject object = new JSObject();
        object.put("success", true);
        try {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION);

            if (getSSIDConfigurator().isConnectedSSID(call.getString("ssid"))) {
                object.put("isConnected", true);
                object.put("message", "plugin.wifieapconfigurator.success.network.connected");
            } else {
                object.put("isConnected", false);
                object.put("message", "plugin.wifieapconfigurator.error.network.notConnected");
            }
        } catch (SecurityException e) {
            Log.e(e.getClass().getSimpleName(), e.getMessage());

            object.put("message", "plugin.wifieapconfigurator.error.permission.notGranted");
            //call.error("plugin.wifieapconfigurator.error.permission.notGranted", e);
        }
    }

    @PluginMethod
    public void sendNotification(PluginCall call) {
        String stringDate = call.getString("date");
        String title = call.getString("title");
        String message = call.getString("message");

        // TODO this has nothing with SSIDs to do,
        // TODO so this should use an SSIDConfigurator, or any AbstractConfigurator
        getSSIDConfigurator().sendNotification(stringDate, title, message);

        // TODO no callback needed?
    }

    @PluginMethod()
    public void writeToSharedPref(PluginCall call) {
        // TODO this has nothing with SSIDs to do,
        // TODO so this should use an SSIDConfigurator, or any AbstractConfigurator
        getSSIDConfigurator().writeToSharedPref(call.getString("id"));

        JSObject object = new JSObject();
        object.put("success", true);
        object.put("message", "plugin.wifieapconfigurator.success.writing");
        call.success(object);
    }

    @PluginMethod()
    public void readFromSharedPref(PluginCall call) {
        JSObject object = new JSObject();

        // TODO this has nothing with SSIDs to do,
        // TODO so this should use an SSIDConfigurator, or any AbstractConfigurator
        String id = getSSIDConfigurator().readFromSharedPref();
        if (id == null || "".equals(id)) {
            object.put("success", false);
            object.put("message", "plugin.wifieapconfigurator.error.reading");
        } else {
            object.put("success", true);
            object.put("message", "plugin.wifieapconfigurator.success.reading");
            object.put("id", id);
        }
        call.success(object);
    }

    @PluginMethod()
    public void checkIfOpenThroughNotifications(PluginCall call) {
        boolean openFromNot = !getActivity().getComponentName().getClassName().contains("MainActivity");
        JSObject object = new JSObject();
        object.put("fromNotification", openFromNot);
        call.success(object);
    }

    /**
     * Get the shared preferences object.
     * Call edit() on the result to get the editor.
     *
     * @return Preferences object
     */
    private SharedPreferences getPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(getContext());

    }

    /**
     * Install a Passpoint network
     * <p>
     * This is a helper function that executes {@code LegacyConfigurator#configurePasspoint}
     * and stores the FQDN used.
     *
     * @param legacyConfigurator The configurator to use
     * @param passpointConfig    The passpoint configuration to install
     * @throws SecurityException         When adding the network was disallowed
     * @throws NetworkInterfaceException The network interface does not support Passpoint
     * @see LegacyConfigurator#configurePasspoint(PasspointConfiguration)
     */
    private void configurePasspoint(LegacyConfigurator legacyConfigurator, PasspointConfiguration passpointConfig) throws SecurityException, NetworkInterfaceException {
        SharedPreferences.Editor editor = getPreferences().edit();
        String fqdn = passpointConfig.getHomeSp().getFqdn();

        boolean success = false;
        try {
            legacyConfigurator.configurePasspoint(passpointConfig);
            success = true;

            editor.putString("fqdn", fqdn);
        } finally {
            if (!success) editor.remove("fqdn");
            editor.apply();
        }
    }

    /**
     * Requests the requested permissions from the operating system
     * <p>
     * // TODO Do we need this, or is it enough to just use WifiEapConfigurator#checkSelfPermission(Context,String)
     *
     * @param permissions All permissions to request
     * @throws SecurityException When not all requested permissions are granted
     */
    private void requestPermission(String... permissions) {
        ArrayList<String> requestPermissions = new ArrayList<>(permissions.length);
        for (String permission : permissions) {
            if (!(checkSelfPermission(getContext(), permission) == PermissionChecker.PERMISSION_GRANTED)) {
                requestPermissions.add(permission);
            }
        }

        if (!requestPermissions.isEmpty()) {
            // TODO implement onRequestPermissionsResult somewhere
            ActivityCompat.requestPermissions(getActivity(), requestPermissions.toArray(new String[0]), 0);

            throw new SecurityException("Not yet granted: " + String.join(", ", requestPermissions));
        }
    }

    /**
     * Get the most appropriate configurator to configure SSIDs on this device
     *
     * @return A configurator that can configure SSIDs on this device
     */
    private AbstractConfigurator getSSIDConfigurator() {
        int targetSDK = getContext().getApplicationContext().getApplicationInfo().targetSdkVersion;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && targetSDK >= Build.VERSION_CODES.Q) {
            return new SuggestionConfigurator(getContext());
        }
        return new LegacyConfigurator(getContext());
    }
}
